package com.beckytech.lammummaakutaa8ffaa.service;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

public class AdManagerHelper {

    private static InterstitialAd mInterstitialAd;

    public static void initialize(Context context) {
        MobileAds.initialize(context, initializationStatus -> {});
    }

    public static void loadBanner(ViewGroup container, String adUnitId) {
        AdView adView = new AdView(container.getContext());
        adView.setAdUnitId(adUnitId);
        adView.setAdSize(AdSize.BANNER);
        container.removeAllViews();
        container.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    public static void loadBanner(ViewGroup container, String adUnitId, AdSize adSize) {
        AdView adView = new AdView(container.getContext());
        adView.setAdUnitId(adUnitId);
        adView.setAdSize(adSize);
        container.removeAllViews();
        container.addView(adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
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
                    }
                });
    }

    public static void showInterstitial(Activity activity) {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
            mInterstitialAd = null;
        }
    }
}
