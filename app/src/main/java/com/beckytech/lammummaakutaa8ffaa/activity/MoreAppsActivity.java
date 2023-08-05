package com.beckytech.lammummaakutaa8ffaa.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.adapter.MoreAppsAdapter;
import com.beckytech.lammummaakutaa8ffaa.contents.MoreAppTitle;
import com.beckytech.lammummaakutaa8ffaa.contents.MoreAppUrl;
import com.beckytech.lammummaakutaa8ffaa.contents.MoreAppsBgColor;
import com.beckytech.lammummaakutaa8ffaa.contents.MoreAppsImage;
import com.beckytech.lammummaakutaa8ffaa.model.MoreAppsModel;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import java.util.ArrayList;
import java.util.List;

public class MoreAppsActivity extends AppCompatActivity implements MoreAppsAdapter.OnAppClicked {
    private final MoreAppsImage image = new MoreAppsImage();
    private final MoreAppTitle title = new MoreAppTitle();
    private final MoreAppUrl url = new MoreAppUrl();
    private final MoreAppsBgColor color = new MoreAppsBgColor();
    private List<MoreAppsModel> moreAppsModelList;
    private AdView adView;
    private InterstitialAd mInterstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_apps);

        MobileAds.initialize(this);

        FrameLayout adContainerView = findViewById(R.id.adView_container);
        adView = new AdView(this);
        adContainerView.addView(adView);
        adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
        allContents();
        loadBanner();
        setAds();
    }

    private void allContents() {
        ImageButton ib_back = findViewById(R.id.ib_back);
        ib_back.setOnClickListener(view -> onBackPressed());

        RecyclerView moreAppsRecycler = findViewById(R.id.more_app_recyclerView);
        getMoreApps();
        MoreAppsAdapter moreAppsAdapter = new MoreAppsAdapter(moreAppsModelList, this, this);
        moreAppsRecycler.setAdapter(moreAppsAdapter);
    }

    private void getMoreApps() {
        moreAppsModelList  = new ArrayList<>();
        for (int i = 0; i < title.title.length; i++) {
            moreAppsModelList.add(new MoreAppsModel(title.title[i],
                    url.url[i],
                    image.images[i],
                    color.color[i]));
        }
    }
    private void loadBanner() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();

        AdSize adSize = getAdSize();
        // Set the adaptive ad size to the ad view.
        adView.setAdSize(adSize);

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }
    private AdSize getAdSize() {
        //Determine the screen width to use for the ad width.
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        //you can also pass your selected width here in dp
        int adWidth = (int) (widthPixels / density);

        //return the optimal size depends on your orientation (landscape or portrait)
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }
    private void setAds() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, getString(R.string.test_interstitial_ads_unit_id), adRequest,
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
    @Override
    public void clickedApp(MoreAppsModel model) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(model.getUrl());
        String url = "http://play.google.com/store/apps/details?id=";
        if (intent == null) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + model.getUrl()));
        }
        startActivity(intent);
    }
}