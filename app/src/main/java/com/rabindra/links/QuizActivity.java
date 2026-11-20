package com.rabindra.links;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestionNumber;
    private TextView tvQuestion;
    private Button btnOption1, btnOption2, btnOption3, btnOption4;
    private ProgressBar progressBar;
    private FrameLayout bannerContainer;

    private RewardManager rewardManager;

    private static class Question {
        String q;
        String[] options;
        int correctIndex;

        Question(String q, String[] options, int correctIndex) {
            this.q = q;
            this.options = options;
            this.correctIndex = correctIndex;
        }
    }

    private final List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean isAnswered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        rewardManager = new RewardManager(this);

        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestion = findViewById(R.id.tvQuestion);
        btnOption1 = findViewById(R.id.btnOption1);
        btnOption2 = findViewById(R.id.btnOption2);
        btnOption3 = findViewById(R.id.btnOption3);
        btnOption4 = findViewById(R.id.btnOption4);
        progressBar = findViewById(R.id.progressBarQuiz);
        bannerContainer = findViewById(R.id.bannerContainerQuiz);

        findViewById(R.id.btnBackQuiz).setOnClickListener(v -> finish());

        setupQuestions();
        displayQuestion();

        AdsManager.loadBanner(this, bannerContainer);
    }

    private void setupQuestions() {
        questionList.add(new Question("What is the capital of Bangladesh?", new String[]{"Dhaka", "Chittagong", "Sylhet", "Rajshahi"}, 0));
        questionList.add(new Question("Which planet is known as the Red Planet?", new String[]{"Venus", "Mars", "Jupiter", "Saturn"}, 1));
        questionList.add(new Question("What does 'HTTP' stand for in Web?", new String[]{"HyperText Transfer Protocol", "High Transfer Text Program", "Hyperlink Text Process", "Home Tool Transfer Protocol"}, 0));
        questionList.add(new Question("How many bits are in 1 Byte?", new String[]{"4", "16", "8", "32"}, 2));
        questionList.add(new Question("Which animal is known as the King of the Jungle?", new String[]{"Tiger", "Elephant", "Lion", "Leopard"}, 2));
        Collections.shuffle(questionList);
    }

    private void displayQuestion() {
        if (currentQuestionIndex >= 5 || currentQuestionIndex >= questionList.size()) {
            finishQuiz();
            return;
        }

        isAnswered = false;
        Question q = questionList.get(currentQuestionIndex);
        tvQuestionNumber.setText("Question " + (currentQuestionIndex + 1) + " / 5");
        progressBar.setProgress((currentQuestionIndex + 1) * 20);
        tvQuestion.setText(q.q);

        resetButton(btnOption1, q.options[0], 0);
        resetButton(btnOption2, q.options[1], 1);
        resetButton(btnOption3, q.options[2], 2);
        resetButton(btnOption4, q.options[3], 3);
    }

    private void resetButton(Button btn, String text, int index) {
        btn.setText(text);
        btn.setBackgroundColor(Color.parseColor("#1A1D2E"));
        btn.setEnabled(true);
        btn.setOnClickListener(v -> checkAnswer(index, btn));
    }

    private void checkAnswer(int selectedIndex, Button selectedBtn) {
        if (isAnswered) return;
        isAnswered = true;

        Question q = questionList.get(currentQuestionIndex);
        if (selectedIndex == q.correctIndex) {
            score++;
            selectedBtn.setBackgroundColor(Color.parseColor("#00E676"));
            Toast.makeText(this, "Correct! +20 Points", Toast.LENGTH_SHORT).show();
        } else {
            selectedBtn.setBackgroundColor(Color.parseColor("#FF5252"));
            highlightCorrect(q.correctIndex);
            Toast.makeText(this, "Incorrect!", Toast.LENGTH_SHORT).show();
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentQuestionIndex++;
            displayQuestion();
        }, 1200);
    }

    private void highlightCorrect(int correctIndex) {
        if (correctIndex == 0) btnOption1.setBackgroundColor(Color.parseColor("#00E676"));
        else if (correctIndex == 1) btnOption2.setBackgroundColor(Color.parseColor("#00E676"));
        else if (correctIndex == 2) btnOption3.setBackgroundColor(Color.parseColor("#00E676"));
        else if (correctIndex == 3) btnOption4.setBackgroundColor(Color.parseColor("#00E676"));
    }

    private void finishQuiz() {
        int earnedPoints = score * 25; // Up to 125 pts
        rewardManager.addPoints(earnedPoints);

        // Show Interstitial ad upon completing quiz
        AdsManager.showInterstitial(this, () -> {
            new AlertDialog.Builder(QuizActivity.this)
                    .setTitle("🎉 Quiz Completed!")
                    .setMessage("You scored " + score + "/5!\nYou earned " + earnedPoints + " PTS.")
                    .setCancelable(false)
                    .setPositiveButton("Awesome", (dialog, which) -> finish())
                    .show();
        });
    }
}
