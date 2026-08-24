package com.beckytech.lammummaakutaa8ffaa.service;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.activity.SplashActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.vungle.ads.AdConfig;
import com.vungle.ads.BaseAd;
import com.vungle.ads.VungleAds;
import com.vungle.ads.VungleError;
import com.vungle.ads.InterstitialAdListener;
import com.vungle.ads.InterstitialAd;

import java.util.Date;

public class AppOpenAdManager implements DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private final String adUnitId;
    private AppOpenAd appOpenAd = null;
    private InterstitialAd vungleAppOpenAd = null;
    private boolean isLoadingAd = false;
    private boolean isShowingAd = false;
    private long loadTime = 0;
    private final Application myApplication;
    private Activity currentActivity;

    public AppOpenAdManager(Application application, String adUnitId) {
        this.myApplication = application;
        this.adUnitId = adUnitId;
        this.myApplication.registerActivityLifecycleCallbacks(this);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);
    }

    public void fetchAd() {
        if (isAdAvailable() || isLoadingAd) {
            return;
        }

        isLoadingAd = true;
        // Competition: AdMob and Vungle
        if (new java.util.Random().nextBoolean()) {
            loadAdMobAppOpen();
        } else {
            loadVungleAppOpen();
        }
    }

    private void loadAdMobAppOpen() {
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(myApplication, adUnitId, request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        isLoadingAd = false;
                        loadTime = (new Date()).getTime();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        if (vungleAppOpenAd == null) {
                            loadVungleAppOpen();
                        } else {
                            isLoadingAd = false;
                        }
                    }
                });
    }

    private void loadVungleAppOpen() {
        if (myApplication == null || !AdManagerHelper.isMobileAdsInitialized() || !VungleAds.isInitialized()) {
            if (appOpenAd == null) {
                loadAdMobAppOpen_Fallback();
            } else {
                isLoadingAd = false;
            }
            return;
        }

        String placementId = myApplication.getString(R.string.liftoff_app_open_placement_id);
        if (placementId == null || placementId.isEmpty()) {
            if (appOpenAd == null) {
                loadAdMobAppOpen_Fallback();
            } else {
                isLoadingAd = false;
            }
            return;
        }

        try {
            vungleAppOpenAd = new InterstitialAd(myApplication, placementId, new AdConfig());
            vungleAppOpenAd.setAdListener(new InterstitialAdListener() {
                @Override
                public void onAdLoaded(@NonNull BaseAd baseAd) {
                    isLoadingAd = false;
                    loadTime = (new Date()).getTime();
                }

                @Override
                public void onAdFailedToLoad(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    vungleAppOpenAd = null;
                    if (appOpenAd == null) {
                        loadAdMobAppOpen_Fallback();
                    } else {
                        isLoadingAd = false;
                    }
                }

                @Override public void onAdFailedToPlay(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                    vungleAppOpenAd = null;
                    isLoadingAd = false;
                }

                @Override public void onAdClicked(@NonNull BaseAd baseAd) {}
                @Override public void onAdLeftApplication(@NonNull BaseAd baseAd) {}
                @Override public void onAdImpression(@NonNull BaseAd baseAd) {}
                @Override public void onAdStart(@NonNull BaseAd baseAd) {}
                @Override public void onAdEnd(@NonNull BaseAd baseAd) {}
            });
            vungleAppOpenAd.load((String) null);
        } catch (Exception e) {
            vungleAppOpenAd = null;
            if (appOpenAd == null) {
                loadAdMobAppOpen_Fallback();
            } else {
                isLoadingAd = false;
            }
        }
    }

    private void loadAdMobAppOpen_Fallback() {
        AdRequest request = new AdRequest.Builder().build();
        AppOpenAd.load(myApplication, adUnitId, request,
                new AppOpenAd.AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd ad) {
                        appOpenAd = ad;
                        isLoadingAd = false;
                        loadTime = (new Date()).getTime();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        isLoadingAd = false;
                    }
                });
    }

    public void showAdIfAvailable() {
        if (isShowingAd) {
            return;
        }

        if (!isAdAvailable()) {
            fetchAd();
            return;
        }

        if (currentActivity instanceof SplashActivity) {
            return;
        }

        if (appOpenAd != null) {
            showAdMobAppOpen();
        } else if (vungleAppOpenAd != null) {
            showVungleAppOpen();
        }
    }

    private void showAdMobAppOpen() {
        appOpenAd.setFullScreenContentCallback(new com.google.android.gms.ads.FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                appOpenAd = null;
                isShowingAd = false;
                fetchAd();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                isShowingAd = false;
                appOpenAd = null;
                fetchAd();
            }

            @Override
            public void onAdShowedFullScreenContent() {
                isShowingAd = true;
            }
        });
        appOpenAd.show(currentActivity);
    }

    private void showVungleAppOpen() {
        vungleAppOpenAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onAdEnd(@NonNull BaseAd baseAd) {
                vungleAppOpenAd = null;
                isShowingAd = false;
                fetchAd();
            }

            @Override
            public void onAdFailedToPlay(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {
                isShowingAd = false;
                vungleAppOpenAd = null;
                fetchAd();
            }

            @Override public void onAdStart(@NonNull BaseAd baseAd) {
                isShowingAd = true;
            }

            @Override public void onAdLoaded(@NonNull BaseAd baseAd) {}
            @Override public void onAdFailedToLoad(@NonNull BaseAd baseAd, @NonNull VungleError vungleError) {}
            @Override public void onAdClicked(@NonNull BaseAd baseAd) {}
            @Override public void onAdLeftApplication(@NonNull BaseAd baseAd) {}
            @Override public void onAdImpression(@NonNull BaseAd baseAd) {}
        });
        vungleAppOpenAd.play(currentActivity);
    }

    private boolean isAdAvailable() {
        return (appOpenAd != null || vungleAppOpenAd != null) && wasLoadTimeLessThanNHoursAgo(4);
    }

    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        showAdIfAvailable();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        currentActivity = null;
    }
}
