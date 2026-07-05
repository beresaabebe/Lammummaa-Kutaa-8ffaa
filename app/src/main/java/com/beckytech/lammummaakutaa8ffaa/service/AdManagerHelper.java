package com.beckytech.lammummaakutaa8ffaa.service;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import android.os.Bundle;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.UserMessagingPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class AdManagerHelper {

    private static InterstitialAd mInterstitialAd;
    private static int clickThreshold = 3;
    private static int adInterval = 8;
    private static final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private static final AtomicBoolean isMobileAdsInitialized = new AtomicBoolean(false);
    private static final List<Runnable> initCallbacks = new ArrayList<>();
    private static ConsentInformation consentInformation;

    public static void initialize(Activity activity, Runnable onInitComplete) {
        setupRemoteConfig();
        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        consentInformation.requestConsentInfoUpdate(
                activity,
                new com.google.android.ump.ConsentRequestParameters.Builder().build(),
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        loadAndShowError -> {
                            if (consentInformation.canRequestAds()) {
                                startMobileAdsSdk(activity, onInitComplete);
                            } else {
                                if (onInitComplete != null) onInitComplete.run();
                            }
                        }
                ),
                requestConsentError -> {
                    if (consentInformation.canRequestAds()) {
                        startMobileAdsSdk(activity, onInitComplete);
                    } else {
                        if (onInitComplete != null) onInitComplete.run();
                    }
                });

        if (consentInformation.canRequestAds()) {
            startMobileAdsSdk(activity, onInitComplete);
        }
    }

    public static void initMobileAds(Context context, Runnable onInitComplete) {
        startMobileAdsSdk(context, onInitComplete);
    }

    private static void setupRemoteConfig() {
        com.google.firebase.remoteconfig.FirebaseRemoteConfig remoteConfig = com.google.firebase.remoteconfig.FirebaseRemoteConfig.getInstance();
        com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings configSettings = new com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        remoteConfig.setConfigSettingsAsync(configSettings);
        java.util.HashMap<String, Object> defaults = new java.util.HashMap<>();
        defaults.put("click_threshold", 3);
        defaults.put("ad_interval", 8);
        remoteConfig.setDefaultsAsync(defaults);
        remoteConfig.fetchAndActivate().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                clickThreshold = (int) remoteConfig.getLong("click_threshold");
                adInterval = (int) remoteConfig.getLong("ad_interval");
            }
        });
    }

    public static int getAdInterval() {
        return adInterval;
    }

    private static void startMobileAdsSdk(Context context, Runnable onInitComplete) {
        synchronized (initCallbacks) {
            if (isMobileAdsInitialized.get()) {
                if (onInitComplete != null) onInitComplete.run();
                return;
            }
            
            if (onInitComplete != null) {
                initCallbacks.add(onInitComplete);
            }
            
            if (isMobileAdsInitializeCalled.getAndSet(true)) {
                return;
            }
        }

        MobileAds.initialize(context, initializationStatus -> {
            isMobileAdsInitialized.set(true);
            synchronized (initCallbacks) {
                for (Runnable callback : initCallbacks) {
                    callback.run();
                }
                initCallbacks.clear();
            }
        });
    }

    public static void loadAdaptiveBanner(Activity activity, ViewGroup container, String adUnitId, boolean isCollapsible) {
        AdView adView = new AdView(activity);
        adView.setAdUnitId(adUnitId);
        container.removeAllViews();
        container.addView(adView);
        
        AdSize adSize = getAdSize(activity, container);
        adView.setAdSize(adSize);
        
        AdRequest.Builder builder = new AdRequest.Builder();
        if (isCollapsible) {
            Bundle extras = new Bundle();
            extras.putString("collapsible", "bottom");
            builder.addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter.class, extras);
        }
        
        adView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Potential fallback to a standard unit could be added here
            }
        });
        
        adView.loadAd(builder.build());
    }

    private static AdSize getAdSize(Activity activity, ViewGroup container) {
        float widthPixels = container.getWidth();
        if (widthPixels == 0) {
            widthPixels = activity.getResources().getDisplayMetrics().widthPixels;
        }
        float density = activity.getResources().getDisplayMetrics().density;
        int adWidth = (int) (widthPixels / density);
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    public static void loadInterstitial(Activity activity, String adUnitId) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, adUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        // Fallback to the main interstitial unit if a specific one fails
                        String fallbackId = activity.getString(R.string.google_interstitial_ads_unit_id);
                        if (!Objects.equals(adUnitId, fallbackId)) {
                            AdRequest fallbackRequest = new AdRequest.Builder().build();
                            InterstitialAd.load(activity, fallbackId, fallbackRequest, new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                                    mInterstitialAd = interstitialAd;
                                }
                            });
                        }
                    }
                });
    }

    public static void showInterstitial(Activity activity) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
            mInterstitialAd = null;
        }
    }

    public static void showRewardedInterstitial(Activity activity) {
        showInterstitial(activity);
    }

    public static void loadRewardedInterstitial(Activity activity, String adUnitId) {
        loadInterstitial(activity, adUnitId);
    }

    public static void loadRewardedAd(Activity activity, String adUnitId) {
        loadInterstitial(activity, adUnitId);
    }

    public static void showRandomRewardedAd(Activity activity, String rewardedId, String rewardedInterstitialId, String interstitialId) {
        showInterstitial(activity);
        // Reload main interstitial
        loadInterstitial(activity, interstitialId);
    }

    public interface NativeAdListener {
        void onNativeAdLoaded(NativeAd nativeAd);
        void onNativeAdFailed();
    }

    public static void loadNativeAd(Context context, String adUnitId, NativeAdListener listener) {
        com.google.android.gms.ads.AdLoader adLoader = new com.google.android.gms.ads.AdLoader.Builder(context, adUnitId)
                .forNativeAd(nativeAd -> {
                    if (listener != null) listener.onNativeAdLoaded(nativeAd);
                })
                .withAdListener(new com.google.android.gms.ads.AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        if (listener != null) listener.onNativeAdFailed();
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}
