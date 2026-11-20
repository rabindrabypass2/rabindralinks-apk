package com.rabindra.links;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

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

    // When testing before Google Play publishing, TEST_MODE must be true to avoid NO_FILL errors
    public static boolean TEST_MODE = true;

    private static boolean isInitialized = false;
    private static BannerView currentBannerView = null;

    public static void initialize(Context context) {
        if (isInitialized) return;

        UnityAds.initialize(context.getApplicationContext(), GAME_ID, TEST_MODE, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                isInitialized = true;
                Log.d(TAG, "Unity Ads Initialized Successfully! TestMode: " + TEST_MODE);
                preloadInterstitial();
                preloadRewarded();
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                isInitialized = false;
                Log.e(TAG, "Unity Ads Initialization Failed: " + error + " - " + message);
            }
        });
    }

    public static void preloadInterstitial() {
        UnityAds.load(INTERSTITIAL_PLACEMENT, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                Log.d(TAG, "Interstitial loaded: " + placementId);
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                Log.w(TAG, "Interstitial failed to load: " + error + " | " + message);
            }
        });
    }

    public static void preloadRewarded() {
        UnityAds.load(REWARDED_PLACEMENT, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String placementId) {
                Log.d(TAG, "Rewarded loaded: " + placementId);
            }

            @Override
            public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
                Log.w(TAG, "Rewarded failed to load: " + error + " | " + message);
            }
        });
    }

    public static void loadBanner(Activity activity, ViewGroup bannerContainer) {
        if (bannerContainer == null) return;
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
                    Log.d(TAG, "Banner shown");
                }

                @Override
                public void onBannerClick(BannerView bannerAdView) {
                    Log.d(TAG, "Banner clicked");
                }

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
    }

    public static void showInterstitial(Activity activity, Runnable onComplete) {
        UnityAds.show(activity, INTERSTITIAL_PLACEMENT, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowStart(String placementId) {
                Log.d(TAG, "Interstitial started");
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {}

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                preloadInterstitial();
                if (onComplete != null) {
                    activity.runOnUiThread(onComplete);
                }
            }

            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.w(TAG, "Interstitial show failed: " + error + " - " + message);
                preloadInterstitial();
                if (onComplete != null) {
                    activity.runOnUiThread(onComplete);
                }
            }
        });
    }

    public static void showRewarded(Activity activity, Runnable onRewardEarned, Runnable onFailed) {
        UnityAds.show(activity, REWARDED_PLACEMENT, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowStart(String placementId) {
                Log.d(TAG, "Rewarded ad started");
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {}

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
                preloadRewarded();
                if (state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                    if (onRewardEarned != null) {
                        activity.runOnUiThread(onRewardEarned);
                    }
                } else {
                    if (onFailed != null) {
                        activity.runOnUiThread(onFailed);
                    }
                }
            }

            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
                Log.w(TAG, "Rewarded show failed: " + error + " - " + message);
                preloadRewarded();
                activity.runOnUiThread(() -> {
                    Toast.makeText(activity, "Ad Error: " + error + " (" + message + ")", Toast.LENGTH_SHORT).show();
                });
                if (onFailed != null) {
                    activity.runOnUiThread(onFailed);
                }
            }
        });
    }
}
