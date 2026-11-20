package com.rabindra.links;

import android.content.Context;
import android.content.SharedPreferences;

public class RewardManager {

    private static final String PREF_NAME = "rabindra_rewards";
    private static final String KEY_POINTS = "user_points";
    private static final String KEY_TASKS_COMPLETED = "tasks_completed";
    private static final String KEY_LAST_DAILY = "last_daily_claim_ms";

    private final SharedPreferences prefs;

    public RewardManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public int getPoints() {
        return prefs.getInt(KEY_POINTS, 100); // 100 starter points
    }

    public void addPoints(int amount) {
        int current = getPoints();
        prefs.edit().putInt(KEY_POINTS, current + amount).apply();
        incrementTasks();
    }

    public boolean deductPoints(int amount) {
        int current = getPoints();
        if (current >= amount) {
            prefs.edit().putInt(KEY_POINTS, current - amount).apply();
            return true;
        }
        return false;
    }

    public int getTasksCompleted() {
        return prefs.getInt(KEY_TASKS_COMPLETED, 0);
    }

    private void incrementTasks() {
        int current = getTasksCompleted();
        prefs.edit().putInt(KEY_TASKS_COMPLETED, current + 1).apply();
    }

    public boolean canClaimDaily() {
        long lastClaim = prefs.getLong(KEY_LAST_DAILY, 0);
        long now = System.currentTimeMillis();
        // 24 hours = 86400000 ms
        return (now - lastClaim) >= (24 * 60 * 60 * 1000);
    }

    public void recordDailyClaim() {
        prefs.edit().putLong(KEY_LAST_DAILY, System.currentTimeMillis()).apply();
        addPoints(50);
    }
}
