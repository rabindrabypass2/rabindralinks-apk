package com.rabindra.links;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MathActivity extends AppCompatActivity {

    private TextView tvMathTimer, tvMathScore, tvMathEquation;
    private Button btnAns1, btnAns2, btnAns3, btnAns4;
    private FrameLayout bannerContainer;

    private RewardManager rewardManager;
    private final Random random = new Random();
    private CountDownTimer timer;

    private int score = 0;
    private int correctAnswer = 0;
    private int questionsCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_math);

        rewardManager = new RewardManager(this);

        tvMathTimer = findViewById(R.id.tvMathTimer);
        tvMathScore = findViewById(R.id.tvMathScore);
        tvMathEquation = findViewById(R.id.tvMathEquation);
        btnAns1 = findViewById(R.id.btnMathAns1);
        btnAns2 = findViewById(R.id.btnMathAns2);
        btnAns3 = findViewById(R.id.btnMathAns3);
        btnAns4 = findViewById(R.id.btnMathAns4);
        bannerContainer = findViewById(R.id.bannerContainerMath);

        findViewById(R.id.btnBackMath).setOnClickListener(v -> finish());

        nextQuestion();
        startTimer();

        AdsManager.loadBanner(this, bannerContainer);
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        timer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvMathTimer.setText("Time: " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                finishGame();
            }
        }.start();
    }

    private void nextQuestion() {
        questionsCount++;
        int a = random.nextInt(40) + 10;
        int b = random.nextInt(30) + 5;
        boolean isAdd = random.nextBoolean();

        if (isAdd) {
            correctAnswer = a + b;
            tvMathEquation.setText(a + " + " + b + " = ?");
        } else {
            correctAnswer = a - b;
            tvMathEquation.setText(a + " - " + b + " = ?");
        }

        int correctSlot = random.nextInt(4);
        Button[] btns = {btnAns1, btnAns2, btnAns3, btnAns4};

        for (int i = 0; i < 4; i++) {
            if (i == correctSlot) {
                btns[i].setText(String.valueOf(correctAnswer));
                final int ans = correctAnswer;
                btns[i].setOnClickListener(v -> checkAnswer(ans));
            } else {
                int wrong = correctAnswer + (random.nextInt(10) + 1) * (random.nextBoolean() ? 1 : -1);
                if (wrong == correctAnswer) wrong += 2;
                btns[i].setText(String.valueOf(wrong));
                final int ans = wrong;
                btns[i].setOnClickListener(v -> checkAnswer(ans));
            }
        }
    }

    private void checkAnswer(int chosen) {
        if (chosen == correctAnswer) {
            score++;
            tvMathScore.setText("Score: " + score);
            Toast.makeText(this, "Correct! ⚡", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Wrong! ❌", Toast.LENGTH_SHORT).show();
        }
        nextQuestion();
    }

    private void finishGame() {
        if (timer != null) timer.cancel();
        int earned = score * 15;
        rewardManager.addPoints(earned);

        AdsManager.showInterstitial(this, () -> {
            new AlertDialog.Builder(MathActivity.this)
                    .setTitle("⏰ Time's Up!")
                    .setMessage("You answered " + score + " math problems correctly!\nYou earned " + earned + " PTS.")
                    .setCancelable(false)
                    .setPositiveButton("Collect Reward", (dialog, which) -> finish())
                    .show();
        });
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        super.onDestroy();
    }
}
