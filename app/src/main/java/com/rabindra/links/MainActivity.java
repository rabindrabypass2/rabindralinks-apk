package com.rabindra.links;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvWalletBalance;
    private TextView tvTasksCompletedCount;
    private FrameLayout bannerContainer;

    private RewardManager rewardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rewardManager = new RewardManager(this);

        tvWalletBalance = findViewById(R.id.tvWalletBalance);
        tvTasksCompletedCount = findViewById(R.id.tvTasksCompletedCount);
        bannerContainer = findViewById(R.id.bannerContainerMain);

        // Initialize Unity Ads SDK with Game ID: 800446112
        AdsManager.initialize(this);
        AdsManager.loadBanner(this, bannerContainer);

        setupClickListeners();
        updateUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        if (tvWalletBalance != null) {
            tvWalletBalance.setText(String.valueOf(rewardManager.getPoints()));
        }
        if (tvTasksCompletedCount != null) {
            tvTasksCompletedCount.setText("Tasks Completed: " + rewardManager.getTasksCompleted());
        }
    }

    private void setupClickListeners() {
        // Daily Check-In
        findViewById(R.id.btnDailyCheckIn).setOnClickListener(v -> handleDailyCheckIn());

        // Task 1: Brain Quiz
        findViewById(R.id.cardQuizTask).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, QuizActivity.class));
        });

        // Task 2: Memory Tile Puzzle
        findViewById(R.id.cardPuzzleTask).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, PuzzleActivity.class));
        });

        // Task 3: Speed Math Sprint
        findViewById(R.id.cardMathTask).setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MathActivity.class));
        });

        // Task 4: Watch Video Ad (Unity Rewarded: BP_Rewarded_Android)
        findViewById(R.id.cardVideoTask).setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Loading sponsored video...", Toast.LENGTH_SHORT).show();
            AdsManager.showRewarded(MainActivity.this, () -> {
                rewardManager.addPoints(200);
                updateUI();
                showSuccessDialog("🎉 Video Reward Claimed!", "You successfully watched the sponsor ad and earned +200 PTS!");
            }, () -> {
                Toast.makeText(MainActivity.this, "Video not ready yet. Please try again in a few seconds.", Toast.LENGTH_SHORT).show();
            });
        });

        // Redeem / Withdraw Dialog
        findViewById(R.id.btnRedeem).setOnClickListener(v -> showWithdrawDialog());
    }

    private void handleDailyCheckIn() {
        if (rewardManager.canClaimDaily()) {
            rewardManager.recordDailyClaim();
            updateUI();
            showSuccessDialog("🎁 Daily Bonus Claimed!", "You received +50 PTS daily streak reward!");
        } else {
            Toast.makeText(this, "Daily reward already claimed today! Check back tomorrow.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showWithdrawDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_withdraw, null);
        EditText etAccount = view.findViewById(R.id.etPayoutAccount);
        EditText etPoints = view.findViewById(R.id.etPointsToRedeem);
        Button btnCancel = view.findViewById(R.id.btnCancelWithdraw);
        Button btnConfirm = view.findViewById(R.id.btnConfirmWithdraw);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(true)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String pointsStr = etPoints.getText().toString().trim();

            if (account.isEmpty()) {
                etAccount.setError("Enter payment account/ID");
                return;
            }
            if (pointsStr.isEmpty()) {
                etPoints.setError("Enter points");
                return;
            }

            int points = Integer.parseInt(pointsStr);
            if (points < 1000) {
                Toast.makeText(MainActivity.this, "Minimum withdrawal is 1,000 PTS", Toast.LENGTH_SHORT).show();
                return;
            }

            if (rewardManager.deductPoints(points)) {
                dialog.dismiss();
                updateUI();
                showSuccessDialog("✅ Payout Requested", "Your redemption request for " + points + " PTS to " + account + " has been submitted!");
            } else {
                Toast.makeText(MainActivity.this, "Insufficient points balance!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showSuccessDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Awesome", null)
                .show();
    }
}
