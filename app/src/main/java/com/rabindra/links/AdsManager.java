package com.rabindra.links;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

public class AdsManager {

    private static final String TAG = "AdsManager";

    public static final String GAME_ID = "800446112";
    public static final String BANNER_PLACEMENT = "BP_Banner_Android";
    public static final String INTERSTITIAL_PLACEMENT = "BP_Interstitial_Android";
    public static final String REWARDED_PLACEMENT = "BP_Rewarded_Android";

    // Set testMode to true for development/testing
    public static boolean TEST_MODE = true;

    private static boolean isInitialized = false;
    private static BannerView currentBannerView = null;

    public interface InitCallback {
        void onInitialized();
    }

    public static void initialize(Context context, InitCallback callback) {
        if (isInitialized) {
            if (callback != null) callback.onInitialized();
            return;
        }

        UnityAds.initialize(context.getApplicationContext(), GAME_ID, TEST_MODE, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                isInitialized = true;
                Log.d(TAG, "Unity Ads Initialized Successfully! (TestMode: " + TEST_MODE + ")");
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Unity Ads Ready ✅", Toast.LENGTH_SHORT).show();
                    if (callback != null) {
                        callback.onInitialized();
                    }
                });
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                isInitialized = false;
                Log.e(TAG, "Unity Ads Initialization Failed: " + error + " - " + message);
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Init Error: " + error + "\n" + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    public static void loadBanner(Activity activity, ViewGroup bannerContainer) {
        if (bannerContainer == null) return;
        if (!isInitialized) {
            Log.w(TAG, "Cannot load banner: Unity Ads not initialized yet.");
            return;
        }

        activity.runOnUiThread(() -> {
            try {
                if (currentBannerView != null) {
                    currentBannerView.destroy();
                    bannerContainer.removeAllViews();
                }

                currentBannerView = new BannerView(activity, BANNER_PLACEMENT, new UnityBannerSize(320, 50));
                currentBannerView.setListener(new BannerView.IListener() {
                    @Override
                    public void onBannerLoaded(BannerView bannerAdView) {
                        Log.d(TAG, "Banner loaded successfully");
                    }

                    @Override
                    public void onBannerShown(BannerView bannerAdView) {
                        Log.d(TAG, "Banner shown on screen");
                    }

                    @Override
                    public void onBannerClick(BannerView bannerAdView) {}

                    @Override
                    public void onBannerFailedToLoad(BannerView bannerAdView, BannerErrorInfo errorInfo) {
                        Log.e(TAG, "Banner failed to load: " + errorInfo.errorMessage);
                    }

                    @Override
                    public void onBannerLeftApplication(BannerView bannerView) {}
                });

                bannerContainer.addView(currentBannerView);
                currentBannerView.load();
            } catch (Exception e) {
                Log.e(TAG, "Error loading banner", e);
            }
        });
    }

    private static Dialog createLoadingDialog(Activity activity, String message) {
        Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(50, 40, 50, 40);
        layout.setBackgroundColor(Color.parseColor("#1A1D2E"));

        ProgressBar bar = new ProgressBar(activity);
        layout.addView(bar);

        TextView tv = new TextView(activity);
        tv.setText(message);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(15);
        tv.setPadding(30, 10, 10, 10);
        layout.addView(tv);

        dialog.setContentView(layout);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    public static void showInterstitial(Activity activity, Runnable onComplete) {
        if (!isInitialized) {
            Toast.makeText(activity, "Unity Ads initializing, please wait...", Toast.LENGTH_SHORT).show();
            if (onComplete != null) onComplete.run();
            return;
        }

        Dialog loadingDialog = createLoadingDialog(activity, "Loading ad...");
        try {
            loadingDialog.show();
        } catch (Exception ignored) {}

        UnityAds.load(INTERSTITIAL_PLACEMENT, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                activity.runOnUiThread(() -> {
                    try {
                        if (loadingDialog.isShowing()) loadingDialog.dismiss();
                    } catch (Exception ignored) {}

                    UnityAds.show(activity, INTERSTITIAL_PLACEMENT, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                        @Override
                        public void onUnityAdsShowStart(String pId) {}

                        @Override
                        public void onUnityAdsShowClick(String pId) {}

                        @Override
                        public void onUnityAdsShowComplete(String pId, UnityAds.UnityAdsShowCompletionState state) {
                            if (onComplete != null) activity.runOnUiThread(onComplete);
                        }

                        @Override
                        public void onUnityAdsShowFailure(String pId, UnityAds.UnityAdsShowError error, String message) {
                            Log.e(TAG, "Interstitial show failed: " + error + " - " + message);
                            activity.runOnUiThread(() -> {
                                Toast.makeText(activity, "Ad Error: " + message, Toast.LENGTH_SHORT).show();
                                if (onComplete != null) onComplete.run();
                            });
                        }
                    });
                });
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                activity.runOnUiThread(() -> {
                    try {
                        if (loadingDialog.isShowing()) loadingDialog.dismiss();
                    } catch (Exception ignored) {}
                    Log.e(TAG, "Interstitial load failed: " + error + " - " + message);
                    Toast.makeText(activity, "Ad Load Failed: " + error + " (" + message + ")", Toast.LENGTH_LONG).show();
                    if (onComplete != null) onComplete.run();
                });
            }
        });
    }

    public static void showRewarded(Activity activity, Runnable onRewardEarned, Runnable onFailed) {
        if (!isInitialized) {
            Toast.makeText(activity, "Unity Ads initializing, please wait...", Toast.LENGTH_SHORT).show();
            if (onFailed != null) onFailed.run();
            return;
        }

        Dialog loadingDialog = createLoadingDialog(activity, "Loading sponsor video...");
        try {
            loadingDialog.show();
        } catch (Exception ignored) {}

        UnityAds.load(REWARDED_PLACEMENT, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                activity.runOnUiThread(() -> {
                    try {
                        if (loadingDialog.isShowing()) loadingDialog.dismiss();
                    } catch (Exception ignored) {}

                    UnityAds.show(activity, REWARDED_PLACEMENT, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                        @Override
                        public void onUnityAdsShowStart(String pId) {}

                        @Override
                        public void onUnityAdsShowClick(String pId) {}

                        @Override
                        public void onUnityAdsShowComplete(String pId, UnityAds.UnityAdsShowCompletionState state) {
                            if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                                if (onRewardEarned != null) activity.runOnUiThread(onRewardEarned);
                            } else {
                                if (onFailed != null) activity.runOnUiThread(onFailed);
                            }
                        }

                        @Override
                        public void onUnityAdsShowFailure(String pId, UnityAds.UnityAdsShowError error, String message) {
                            Log.e(TAG, "Rewarded show failed: " + error + " - " + message);
                            activity.runOnUiThread(() -> {
                                Toast.makeText(activity, "Video Show Error: " + message, Toast.LENGTH_SHORT).show();
                                if (onFailed != null) onFailed.run();
                            });
                        }
                    });
                });
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                activity.runOnUiThread(() -> {
                    try {
                        if (loadingDialog.isShowing()) loadingDialog.dismiss();
                    } catch (Exception ignored) {}
                    Log.e(TAG, "Rewarded load failed: " + error + " - " + message);
                    Toast.makeText(activity, "Video Load Failed: " + error + " (" + message + ")", Toast.LENGTH_LONG).show();
                    if (onFailed != null) onFailed.run();
                });
            }
        });
    }
}
