package com.beckytech.lammummaakutaa8ffaa.service;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.UserMessagingPlatform;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;
import com.unity3d.services.banners.BannerErrorInfo;
import com.vungle.ads.AdConfig;
import com.vungle.ads.BannerAd;
import com.vungle.ads.BannerAdListener;
import com.vungle.ads.BaseAd;
import com.vungle.ads.InitializationListener;
import com.vungle.ads.InterstitialAdListener;
import com.vungle.ads.NativeAdListener;
import com.vungle.ads.RewardedAdListener;
import com.vungle.ads.VungleAds;
import com.vungle.ads.VungleAdSize;
import com.vungle.ads.VungleError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AdManagerHelper {

    private static InterstitialAd mInterstitialAd;
    private static RewardedInterstitialAd mRewardedInterstitialAd;
    private static RewardedAd mRewardedAd;
    private static boolean isInterstitialLoading = false;
    private static boolean isRewardedLoading = false;
    private static boolean isRewardedInterstitialLoading = false;
    
    private static int clickThreshold = 3;
    private static int adInterval = 8;
    private static final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);
    private static final AtomicBoolean isMobileAdsInitialized = new AtomicBoolean(false);
    private static final List<Runnable> initCallbacks = new ArrayList<>();
    private static ConsentInformation consentInformation;

    public static boolean isMobileAdsInitialized() {
        return isMobileAdsInitialized.get();
    }

    public static void initialize(Activity activity, Runnable onInitComplete) {
        setupRemoteConfig();
        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        consentInformation.requestConsentInfoUpdate(
                activity,
                new com.google.android.ump.ConsentRequestParameters.Builder().build(),
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        loadAndShowError -> startMobileAdsSdk(activity, onInitComplete)
                ),
                requestConsentError -> startMobileAdsSdk(activity, onInitComplete));

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
        HashMap<String, Object> defaults = new HashMap<>();
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

        final AtomicInteger initCounter = new AtomicInteger(3);
        final Runnable checkInitFinished = () -> {
            if (initCounter.decrementAndGet() == 0) {
                isMobileAdsInitialized.set(true);
                synchronized (initCallbacks) {
                    for (Runnable callback : initCallbacks) {
                        callback.run();
                    }
                    initCallbacks.clear();
                }
            }
        };

        MobileAds.initialize(context, initializationStatus -> {
            checkInitFinished.run();
        });

        // Initialize Liftoff (Vungle) Ads
        String vungleAppId = context.getString(R.string.liftoff_app_id);
        VungleAds.init(context, vungleAppId, new InitializationListener() {
            @Override
            public void onSuccess() {
                checkInitFinished.run();
            }

            @Override
            public void onError(@NonNull VungleError vungleError) {
                checkInitFinished.run();
            }
        });

        // Initialize Unity Ads
        String unityGameId = context.getString(R.string.unity_game_id);
        UnityAds.initialize(context, unityGameId, false, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                checkInitFinished.run();
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                checkInitFinished.run();
            }
        });
    }

    public static void loadAdaptiveBanner(Activity activity, ViewGroup container, String adUnitId, boolean isCollapsible) {
        // Competition between AdMob, Liftoff, and Unity
        int choice = new java.util.Random().nextInt(3);
        if (choice == 0) {
            loadAdMobBanner(activity, container, adUnitId, isCollapsible, () -> 
                loadVungleBanner(activity, container, activity.getString(R.string.liftoff_banner_placement_id), () -> 
                    loadUnityBanner(activity, container, activity.getString(R.string.unity_banner_placement_id), null)
                )
            );
        } else if (choice == 1) {
            loadVungleBanner(activity, container, activity.getString(R.string.liftoff_banner_placement_id), () -> 
                loadAdMobBanner(activity, container, adUnitId, isCollapsible, () -> 
                    loadUnityBanner(activity, container, activity.getString(R.string.unity_banner_placement_id), null)
                )
            );
        } else {
            loadUnityBanner(activity, container, activity.getString(R.string.unity_banner_placement_id), () -> 
                loadAdMobBanner(activity, container, adUnitId, isCollapsible, () -> 
                    loadVungleBanner(activity, container, activity.getString(R.string.liftoff_banner_placement_id), null)
                )
            );
        }
    }

    private static void loadAdMobBanner(Activity activity, ViewGroup container, String adUnitId, boolean isCollapsible, Runnable onFail) {
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
                if (onFail != null) onFail.run();
            }
        });
        
        adView.loadAd(builder.build());
    }

    private static void loadVungleBanner(Activity activity, ViewGroup container, String placementId, Runnable onFail) {
        if (!isMobileAdsInitialized.get() || !VungleAds.isInitialized()) {
            if (onFail != null) onFail.run();
            return;
        }
        try {
            BannerAd vungleBanner = new BannerAd(activity, placementId, VungleAdSize.BANNER);
            vungleBanner.setAdListener(new BannerAdListener() {
                @Override
                public void onAdLoaded(@NonNull BaseAd baseAd) {
                    View adView = vungleBanner.getBannerView();
                    if (adView != null) {
                        container.removeAllViews();
                        container.addView(adView);
                    } else {
                        if (onFail != null) onFail.run();
                    }
                }

                @Override
                public void onAdFailedToLoad(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    if (onFail != null) onFail.run();
                }

                @Override public void onAdFailedToPlay(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    if (onFail != null) onFail.run();
                }

                @Override public void onAdClicked(@NonNull BaseAd baseAd) {}
                @Override public void onAdLeftApplication(@NonNull BaseAd baseAd) {}
                @Override public void onAdImpression(@NonNull BaseAd baseAd) {}
                @Override public void onAdStart(@NonNull BaseAd baseAd) {}
                @Override public void onAdEnd(@NonNull BaseAd baseAd) {}
            });
            vungleBanner.load((String) null);
        } catch (Exception e) {
            if (onFail != null) onFail.run();
        }
    }

    private static void loadUnityBanner(Activity activity, ViewGroup container, String placementId, Runnable onFail) {
        UnityBannerSize size = new UnityBannerSize(320, 50);
        BannerView bannerView = new BannerView(activity, placementId, size);
        bannerView.setListener(new BannerView.IListener() {
            @Override
            public void onBannerLoaded(BannerView bannerView) {
                container.removeAllViews();
                container.addView(bannerView);
            }

            @Override
            public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo errorInfo) {
                if (onFail != null) onFail.run();
            }

            @Override public void onBannerClick(BannerView bannerView) {}
            @Override public void onBannerLeftApplication(BannerView bannerView) {}
            @Override public void onBannerShown(BannerView bannerView) {}
        });
        bannerView.load();
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
        if (mInterstitialAd != null || isInterstitialLoading) return;
        isInterstitialLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, adUnitId, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        isInterstitialLoading = false;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        mInterstitialAd = null;
                        isInterstitialLoading = false;
                        loadVungleInterstitial(activity, activity.getString(R.string.liftoff_interstitial_placement_id), () -> 
                            loadUnityInterstitial(activity, activity.getString(R.string.unity_interstitial_placement_id))
                        );
                    }
                });
    }

    private static void loadVungleInterstitial(Activity activity, String placementId, Runnable onFail) {
        if (!isMobileAdsInitialized.get() || !VungleAds.isInitialized()) {
            if (onFail != null) onFail.run();
            return;
        }
        try {
            com.vungle.ads.InterstitialAd interstitialAd = new com.vungle.ads.InterstitialAd(activity, placementId, new AdConfig());
            interstitialAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onAdLoaded(@NonNull BaseAd baseAd) {
                    interstitialAd.play(activity);
                }

                @Override
                public void onAdFailedToLoad(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    if (onFail != null) onFail.run();
                }

                @Override public void onAdFailedToPlay(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    if (onFail != null) onFail.run();
                }

                @Override public void onAdClicked(@NonNull BaseAd baseAd) {}
                @Override public void onAdLeftApplication(@NonNull BaseAd baseAd) {}
                @Override public void onAdImpression(@NonNull BaseAd baseAd) {}
                @Override public void onAdStart(@NonNull BaseAd baseAd) {}
                @Override public void onAdEnd(@NonNull BaseAd baseAd) {}
            });
            interstitialAd.load((String) null);
        } catch (Exception e) {
            if (onFail != null) onFail.run();
        }
    }

    private static void loadUnityInterstitial(Activity activity, String placementId) {
        UnityAds.load(placementId);
    }

    public static void showInterstitial(Activity activity) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
            mInterstitialAd = null;
        } else {
            String unityPlacement = activity.getString(R.string.unity_interstitial_placement_id);
            UnityAds.show(activity, unityPlacement);
        }
    }

    public static void loadRewardedAd(Activity activity, String adUnitId) {
        if (mRewardedAd != null || isRewardedLoading) return;
        isRewardedLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(activity, adUnitId, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                mRewardedAd = rewardedAd;
                isRewardedLoading = false;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mRewardedAd = null;
                isRewardedLoading = false;
                loadVungleRewarded(activity, activity.getString(R.string.liftoff_rewarded_placement_id), () -> 
                    loadUnityRewarded(activity, activity.getString(R.string.unity_rewarded_placement_id))
                );
            }
        });
    }

    private static void loadVungleRewarded(Activity activity, String placementId, Runnable onFail) {
        if (!isMobileAdsInitialized.get() || !VungleAds.isInitialized()) {
            if (onFail != null) onFail.run();
            return;
        }
        try {
            com.vungle.ads.RewardedAd rewardedAd = new com.vungle.ads.RewardedAd(activity, placementId, new AdConfig());
            rewardedAd.setAdListener(new RewardedAdListener() {
                @Override
                public void onAdLoaded(@NonNull BaseAd baseAd) {
                    rewardedAd.play(activity);
                }

                @Override
                public void onAdFailedToLoad(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    if (onFail != null) onFail.run();
                }

                @Override public void onAdFailedToPlay(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    if (onFail != null) onFail.run();
                }

                @Override public void onAdClicked(@NonNull BaseAd baseAd) {}
                @Override public void onAdLeftApplication(@NonNull BaseAd baseAd) {}
                @Override public void onAdImpression(@NonNull BaseAd baseAd) {}
                @Override public void onAdStart(@NonNull BaseAd baseAd) {}
                @Override public void onAdEnd(@NonNull BaseAd baseAd) {}
                @Override public void onAdRewarded(@NonNull BaseAd baseAd) {}
            });
            rewardedAd.load((String) null);
        } catch (Exception e) {
            if (onFail != null) onFail.run();
        }
    }

    private static void loadUnityRewarded(Activity activity, String placementId) {
        UnityAds.load(placementId);
    }

    public static void loadRewardedInterstitial(Activity activity, String adUnitId) {
        if (mRewardedInterstitialAd != null || isRewardedInterstitialLoading) return;
        isRewardedInterstitialLoading = true;
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedInterstitialAd.load(activity, adUnitId, adRequest, new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedInterstitialAd) {
                mRewardedInterstitialAd = rewardedInterstitialAd;
                isRewardedInterstitialLoading = false;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mRewardedInterstitialAd = null;
                isRewardedInterstitialLoading = false;
                loadInterstitial(activity, activity.getString(R.string.google_interstitial_ads_unit_id));
            }
        });
    }

    public static void showRewardedAd(Activity activity) {
        if (mRewardedAd != null) {
            mRewardedAd.show(activity, rewardItem -> {});
            mRewardedAd = null;
        } else if (mRewardedInterstitialAd != null) {
            mRewardedInterstitialAd.show(activity, rewardItem -> {});
            mRewardedInterstitialAd = null;
        } else {
            showInterstitial(activity);
        }
    }

    public static void showRandomRewardedAd(Activity activity, String rewardedId, String rewardedInterstitialId, String interstitialId) {
        showRewardedAd(activity);
        loadAdsByConsent(activity);
    }

    public static void loadAdsByConsent(Activity activity) {
        if (consentInformation != null && consentInformation.canRequestAds()) {
            loadRewardedAd(activity, activity.getString(R.string.google_rewarded_ads_unit_id));
            loadRewardedInterstitial(activity, activity.getString(R.string.google_rewarded_interstitial_ads_unit_id));
            loadInterstitial(activity, activity.getString(R.string.google_interstitial_ads_unit_id));
        } else {
            loadRewardedInterstitial(activity, activity.getString(R.string.google_rewarded_interstitial_ads_unit_id));
            loadInterstitial(activity, activity.getString(R.string.google_interstitial_ads_unit_id));
        }
    }

    public interface FlexibleAdListener {
        void onAdLoaded(Object ad);
        void onAdFailed();
    }

    public static void loadNativeAd(Context context, String adUnitId, FlexibleAdListener listener) {
        // Fallback sequence: Native -> MREC -> Banner
        // Competition: AdMob and Liftoff
        if (new java.util.Random().nextBoolean()) {
            loadAdMobNative(context, adUnitId, new FlexibleAdListener() {
                @Override
                public void onAdLoaded(Object ad) {
                    if (listener != null) listener.onAdLoaded(ad);
                }

                @Override
                public void onAdFailed() {
                    loadVungleNative(context, context.getString(R.string.liftoff_native_placement_id), new FlexibleAdListener() {
                        @Override
                        public void onAdLoaded(Object ad) {
                            if (listener != null) listener.onAdLoaded(ad);
                        }

                        @Override
                        public void onAdFailed() {
                            loadMREC(context, listener);
                        }
                    });
                }
            });
        } else {
            loadVungleNative(context, context.getString(R.string.liftoff_native_placement_id), new FlexibleAdListener() {
                @Override
                public void onAdLoaded(Object ad) {
                    if (listener != null) listener.onAdLoaded(ad);
                }

                @Override
                public void onAdFailed() {
                    loadAdMobNative(context, adUnitId, new FlexibleAdListener() {
                        @Override
                        public void onAdLoaded(Object ad) {
                            if (listener != null) listener.onAdLoaded(ad);
                        }

                        @Override
                        public void onAdFailed() {
                            loadMREC(context, listener);
                        }
                    });
                }
            });
        }
    }

    private static void loadAdMobNative(Context context, String adUnitId, FlexibleAdListener listener) {
        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        new com.google.android.gms.ads.AdLoader.Builder(context, adUnitId)
                .forNativeAd(nativeAd -> {
                    if (listener != null) listener.onAdLoaded(nativeAd);
                })
                .withAdListener(new com.google.android.gms.ads.AdListener() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                        if (listener != null) listener.onAdFailed();
                    }
                })
                .withNativeAdOptions(adOptions)
                .build()
                .loadAd(new AdRequest.Builder().build());
    }

    private static void loadVungleNative(Context context, String placementId, FlexibleAdListener listener) {
        if (!isMobileAdsInitialized.get() || !VungleAds.isInitialized()) {
            if (listener != null) listener.onAdFailed();
            return;
        }
        try {
            com.vungle.ads.NativeAd nativeAd = new com.vungle.ads.NativeAd(context, placementId);
            nativeAd.setAdListener(new NativeAdListener() {
                @Override
                public void onAdLoaded(@NonNull BaseAd baseAd) {
                    if (listener != null) listener.onAdLoaded(nativeAd);
                }

                @Override
                public void onAdFailedToLoad(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    if (listener != null) listener.onAdFailed();
                }

                @Override public void onAdFailedToPlay(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    if (listener != null) listener.onAdFailed();
                }

                @Override public void onAdClicked(@NonNull BaseAd baseAd) {}
                @Override public void onAdLeftApplication(@NonNull BaseAd baseAd) {}
                @Override public void onAdImpression(@NonNull BaseAd baseAd) {}
                @Override public void onAdStart(@NonNull BaseAd baseAd) {}
                @Override public void onAdEnd(@NonNull BaseAd baseAd) {}
            });
            nativeAd.load((String) null);
        } catch (Exception e) {
            if (listener != null) listener.onAdFailed();
        }
    }

    public static void loadMREC(Context context, FlexibleAdListener listener) {
        // Fallback sequence: MREC -> Banner
        // Competition: AdMob and Liftoff
        if (new java.util.Random().nextBoolean()) {
            loadAdMobMREC(context, listener);
        } else {
            loadVungleMREC(context, listener);
        }
    }

    private static void loadAdMobMREC(Context context, FlexibleAdListener listener) {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(context.getString(R.string.google_banner_ad_unit_id));
        adView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                if (listener != null) listener.onAdLoaded(adView);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                loadVungleMREC(context, listener);
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    private static void loadVungleMREC(Context context, FlexibleAdListener listener) {
        if (!isMobileAdsInitialized.get() || !VungleAds.isInitialized()) {
            loadAdMobMREC_Fallback(context, listener);
            return;
        }
        try {
            BannerAd vungleMREC = new BannerAd(context, context.getString(R.string.liftoff_mrec_placement_id), VungleAdSize.MREC);
            vungleMREC.setAdListener(new BannerAdListener() {
                @Override
                public void onAdLoaded(@NonNull BaseAd baseAd) {
                    View mrecView = vungleMREC.getBannerView();
                    if (mrecView != null) {
                        if (listener != null) listener.onAdLoaded(mrecView);
                    } else {
                        loadBannerAsFallback(context, listener);
                    }
                }

                @Override
                public void onAdFailedToLoad(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    loadAdMobMREC_Fallback(context, listener);
                }

                @Override public void onAdFailedToPlay(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    loadAdMobMREC_Fallback(context, listener);
                }

                @Override public void onAdClicked(@NonNull BaseAd baseAd) {}
                @Override public void onAdLeftApplication(@NonNull BaseAd baseAd) {}
                @Override public void onAdImpression(@NonNull BaseAd baseAd) {}
                @Override public void onAdStart(@NonNull BaseAd baseAd) {}
                @Override public void onAdEnd(@NonNull BaseAd baseAd) {}
            });
            vungleMREC.load((String) null);
        } catch (Exception e) {
            loadAdMobMREC_Fallback(context, listener);
        }
    }

    private static void loadAdMobMREC_Fallback(Context context, FlexibleAdListener listener) {
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
        adView.setAdUnitId(context.getString(R.string.google_banner_ad_unit_id));
        adView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                if (listener != null) listener.onAdLoaded(adView);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                loadBannerAsFallback(context, listener);
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    private static void loadBannerAsFallback(Context context, FlexibleAdListener listener) {
        // Fallback for Native/MREC chain: AdMob -> Vungle -> Unity
        AdView adView = new AdView(context);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(context.getString(R.string.google_banner_ad_unit_id));
        adView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdLoaded() {
                if (listener != null) listener.onAdLoaded(adView);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                loadVungleBannerAsFallback(context, listener);
            }
        });
        adView.loadAd(new AdRequest.Builder().build());
    }

    private static void loadVungleBannerAsFallback(Context context, FlexibleAdListener listener) {
        if (!isMobileAdsInitialized.get() || !VungleAds.isInitialized()) {
            loadUnityBannerAsFallback(context, listener);
            return;
        }
        try {
            BannerAd vungleBanner = new BannerAd(context, context.getString(R.string.liftoff_banner_placement_id), VungleAdSize.BANNER);
            vungleBanner.setAdListener(new BannerAdListener() {
                @Override
                public void onAdLoaded(@NonNull BaseAd baseAd) {
                    View bannerView = vungleBanner.getBannerView();
                    if (bannerView != null) {
                        if (listener != null) listener.onAdLoaded(bannerView);
                    } else {
                        loadUnityBannerAsFallback(context, listener);
                    }
                }

                @Override
                public void onAdFailedToLoad(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    loadUnityBannerAsFallback(context, listener);
                }

                @Override public void onAdFailedToPlay(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    loadUnityBannerAsFallback(context, listener);
                }

                @Override public void onAdClicked(@NonNull BaseAd baseAd) {}
                @Override public void onAdLeftApplication(@NonNull BaseAd baseAd) {}
                @Override public void onAdImpression(@NonNull BaseAd baseAd) {}
                @Override public void onAdStart(@NonNull BaseAd baseAd) {}
                @Override public void onAdEnd(@NonNull BaseAd baseAd) {}
            });
            vungleBanner.load((String) null);
        } catch (Exception e) {
            loadUnityBannerAsFallback(context, listener);
        }
    }

    private static void loadUnityBannerAsFallback(Context context, FlexibleAdListener listener) {
        UnityBannerSize size = new UnityBannerSize(320, 50);
        BannerView bannerView = new BannerView(context, context.getString(R.string.unity_banner_placement_id), size);
        bannerView.setListener(new BannerView.IListener() {
            @Override
            public void onBannerLoaded(BannerView bannerView) {
                if (listener != null) listener.onAdLoaded(bannerView);
            }

            @Override
            public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo errorInfo) {
                if (listener != null) listener.onAdFailed();
            }

            @Override public void onBannerClick(BannerView bannerView) {}
            @Override public void onBannerLeftApplication(BannerView bannerView) {}
            @Override public void onBannerShown(BannerView bannerView) {}
        });
        bannerView.load();
    }
}
