package com.rabindra.links;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PuzzleActivity extends AppCompatActivity {

    private GridLayout gridLayout;
    private TextView tvMoves, tvPairs;
    private FrameLayout bannerContainer;
    private RewardManager rewardManager;

    private final String[] ICONS = {"🚀", "💎", "⚡", "🔥", "🍀", "🎯"};
    private final List<String> deck = new ArrayList<>();
    private final Button[] buttons = new Button[12];

    private int firstCardIndex = -1;
    private int moves = 0;
    private int matchedPairs = 0;
    private boolean isBusy = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        rewardManager = new RewardManager(this);

        gridLayout = findViewById(R.id.gridPuzzle);
        tvMoves = findViewById(R.id.tvMoves);
        tvPairs = findViewById(R.id.tvPairs);
        bannerContainer = findViewById(R.id.bannerContainerPuzzle);

        findViewById(R.id.btnBackPuzzle).setOnClickListener(v -> finish());
        findViewById(R.id.btnRestartPuzzle).setOnClickListener(v -> startNewGame());

        startNewGame();
        AdsManager.loadBanner(this, bannerContainer);
    }

    private void startNewGame() {
        deck.clear();
        deck.addAll(Arrays.asList(ICONS));
        deck.addAll(Arrays.asList(ICONS));
        Collections.shuffle(deck);

        moves = 0;
        matchedPairs = 0;
        firstCardIndex = -1;
        isBusy = false;

        tvMoves.setText("Moves: 0");
        tvPairs.setText("Pairs: 0/6");
        gridLayout.removeAllViews();

        for (int i = 0; i < 12; i++) {
            final int index = i;
            Button btn = new Button(this);
            btn.setText("❓");
            btn.setTextSize(24);
            btn.setTextColor(Color.WHITE);
            btn.setBackgroundColor(Color.parseColor("#1A1D2E"));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(i % 3, 1f);
            params.rowSpec = GridLayout.spec(i / 3, 1f);
            params.setMargins(10, 10, 10, 10);
            btn.setLayoutParams(params);

            btn.setOnClickListener(v -> handleCardClick(index));
            buttons[i] = btn;
            gridLayout.addView(btn);
        }
    }

    private void handleCardClick(int index) {
        if (isBusy || buttons[index].getText().toString().equals(deck.get(index)) || index == firstCardIndex) {
            return;
        }

        buttons[index].setText(deck.get(index));
        buttons[index].setBackgroundColor(Color.parseColor("#3A3897"));

        if (firstCardIndex == -1) {
            firstCardIndex = index;
        } else {
            moves++;
            tvMoves.setText("Moves: " + moves);
            isBusy = true;

            int secondCardIndex = index;
            if (deck.get(firstCardIndex).equals(deck.get(secondCardIndex))) {
                matchedPairs++;
                tvPairs.setText("Pairs: " + matchedPairs + "/6");
                buttons[firstCardIndex].setBackgroundColor(Color.parseColor("#00E676"));
                buttons[secondCardIndex].setBackgroundColor(Color.parseColor("#00E676"));
                firstCardIndex = -1;
                isBusy = false;

                if (matchedPairs == 6) {
                    onPuzzleSolved();
                }
            } else {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    buttons[firstCardIndex].setText("❓");
                    buttons[firstCardIndex].setBackgroundColor(Color.parseColor("#1A1D2E"));
                    buttons[secondCardIndex].setText("❓");
                    buttons[secondCardIndex].setBackgroundColor(Color.parseColor("#1A1D2E"));
                    firstCardIndex = -1;
                    isBusy = false;
                }, 800);
            }
        }
    }

    private void onPuzzleSolved() {
        int earned = 150;
        rewardManager.addPoints(earned);

        AdsManager.showInterstitial(this, () -> {
            new AlertDialog.Builder(PuzzleActivity.this)
                    .setTitle("🏆 Puzzle Solved!")
                    .setMessage("Great job! Completed in " + moves + " moves.\nYou earned " + earned + " PTS!\n\nWant 2X double points?")
                    .setCancelable(false)
                    .setPositiveButton("Watch Ad for 2X (+150 PTS)", (dialog, which) -> {
                        AdsManager.showRewarded(PuzzleActivity.this, () -> {
                            rewardManager.addPoints(earned);
                            Toast.makeText(PuzzleActivity.this, "🎉 2X Bonus Claimed! +150 PTS", Toast.LENGTH_LONG).show();
                            finish();
                        }, () -> finish());
                    })
                    .setNegativeButton("Collect & Exit", (dialog, which) -> finish())
                    .show();
        });
    }
}
