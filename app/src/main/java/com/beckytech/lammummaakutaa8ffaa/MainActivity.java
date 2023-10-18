package com.beckytech.lammummaakutaa8ffaa;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.activity.AboutActivity;
import com.beckytech.lammummaakutaa8ffaa.activity.BookDetailActivity;
import com.beckytech.lammummaakutaa8ffaa.activity.PrivacyActivity;
import com.beckytech.lammummaakutaa8ffaa.adapter.Adapter;
import com.beckytech.lammummaakutaa8ffaa.contents.ContentEndPage;
import com.beckytech.lammummaakutaa8ffaa.contents.ContentStartPage;
import com.beckytech.lammummaakutaa8ffaa.contents.SubTitleContents;
import com.beckytech.lammummaakutaa8ffaa.contents.TitleContents;
import com.beckytech.lammummaakutaa8ffaa.model.Model;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Adapter.onBookClicked{
    InterstitialAd interstitialAd;
    String TAG = MainActivity.class.getSimpleName();
    private List<Object> modelList;
    private final TitleContents titleContents = new TitleContents();
    private final SubTitleContents subTitleContent = new SubTitleContents();
    private final ContentStartPage startPage = new ContentStartPage();
    private final ContentEndPage endPage = new ContentEndPage();
    private ReviewInfo reviewInfo;
    private ReviewManager manager;
    private NavigationView navigationView;
    public static final int ADS_PER_ITEM = 6;
    private AdView adView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        scrollViewSmooth();
        callAds();
        activateReviewInfo();
        AppRate.app_launched(this);
        toolBarAndDrawerNavigation();
        booksDataRecycler();
        addBanner(modelList);
        rateReviewInfo();
    }

    private void scrollViewSmooth() {
        ScrollView scrollView = findViewById(R.id.scrollView_main);
        scrollView.fullScroll(View.FOCUS_DOWN);
        scrollView.setSmoothScrollingEnabled(true);
    }

    private void toolBarAndDrawerNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerToggle.syncState();
        drawerToggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.white));
        drawerLayout.addDrawerListener(drawerToggle);

        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(item -> {
            MenuOptions(item);
            return true;
        });
        View nav = navigationView.getHeaderView(0);
        ImageView back_btn = nav.findViewById(R.id.back_btn);
        back_btn.setColorFilter(Color.WHITE);
        back_btn.setOnClickListener(view -> {
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        ImageView share_btn = nav.findViewById(R.id.share_btn);
        share_btn.setColorFilter(Color.WHITE);
        share_btn.setOnClickListener(view -> shareBtn());
    }

    private void shareBtn() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "Download this app from Play store \n" + url);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void booksDataRecycler() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView_main_item);
        getData();
        Adapter adapter = new Adapter(modelList, this);
        recyclerView.setAdapter(adapter);
    }

    private void rateReviewInfo() {
        if (reviewInfo != null) {
            Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
            flow.addOnCompleteListener(task -> {
                Menu menu_rate = navigationView.getMenu();
                menu_rate.findItem(R.id.action_rate).setVisible(false);
            });
        }
    }

    private void addBanner(List<Object> list) {
        int j = 0;
        for (int i = ADS_PER_ITEM; i <= list.size(); i += ADS_PER_ITEM) {
            if (j % 3 != 0)
                adView = new AdView(this, "513372960928869_513374324262066", AdSize.BANNER_HEIGHT_50); // Banner
            else
                adView = new AdView(MainActivity.this, "513372960928869_569823628617135", AdSize.RECTANGLE_HEIGHT_250); // Rectangle
            list.add(i, adView);
            j++;
        }
        loadBanner(list);
    }

    private void loadBanner(List<Object> list) {
        loadBanner(ADS_PER_ITEM, list);
    }

    private void loadBanner(int adsPerItem, List<Object> list) {
        if (adsPerItem >= list.size()) {
            return;
        }
        Object items = list.get(adsPerItem);
        adView = (AdView) items;
        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
//                Toast.makeText(MainActivity.this,"Error: " + adError.getErrorMessage(), Toast.LENGTH_LONG).show();
                loadBanner(adsPerItem + ADS_PER_ITEM, list);
            }

            @Override
            public void onAdLoaded(Ad ad) {
                loadBanner(adsPerItem + ADS_PER_ITEM, list);
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };
        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());
    }


    private void getData() {
        modelList = new ArrayList<>();
        for (int i = 0; i < titleContents.title.length; i++) {
            modelList.add(new Model(titleContents.title[i].substring(0, 1).toUpperCase() + "" + titleContents.title[i].substring(1).toLowerCase(),
                    subTitleContent.subTitle[i].substring(0, 1).toUpperCase() + "" + subTitleContent.subTitle[i].substring(1),
                    startPage.pageStart[i],
                    endPage.pageEnd[i]));
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    void MenuOptions(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        if (item.getItemId() == R.id.action_about_us) {
            showAdWithDelay();
            startActivity(new Intent(this, AboutActivity.class));
        }

        if (item.getItemId() == R.id.action_rate) {
            String pkg = getPackageName();
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + pkg)));
        }

        if (item.getItemId() == R.id.action_more_apps) {
            showAdWithDelay();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://play.google.com/store/apps/dev?id=6669279757479011928"));
            startActivity(intent);
        }

        if (item.getItemId() == R.id.action_share) {
            shareBtn();
        }

        if (item.getItemId() == R.id.action_update) {
            showAdWithDelay();
            SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            int lastVersion = pref.getInt("lastVersion", 9);
            String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
            if (lastVersion < BuildConfig.VERSION_CODE) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                Toast.makeText(this, "New update is available download it from play store!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No update available!", Toast.LENGTH_SHORT).show();
            }
        }
        if (item.getItemId() == R.id.action_exit) {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this, R.style.MyAlertDialog);
            builder.setTitle("Exit")
                    .setMessage("Do you want to close?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        System.exit(0);
                        finish();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setBackground(getResources().getDrawable(R.drawable.nav_header_bg, null))
                    .show();
        }
        if (item.getItemId() == R.id.action_rate) {
            startReviewFlow();
        }
        if (item.getItemId() == R.id.action_privacy) {
            showAdWithDelay();
            startActivity(new Intent(MainActivity.this, PrivacyActivity.class));
        }
    }

    @Override
    public void clickedBook(Model model) {
        showAdWithDelay();
        startActivity(new Intent(this, BookDetailActivity.class).putExtra("data", model));
    }

    private void callAds() {
        AudienceNetworkAds.initialize(this);
        AdView adView_rect = new AdView(this, "513372960928869_569823628617135", AdSize.RECTANGLE_HEIGHT_250);
        LinearLayout adContainer_rect = findViewById(R.id.banner_container_rect);
        adContainer_rect.addView(adView_rect);
        adView_rect.loadAd();

        AdView adView = new AdView(this, "513372960928869_513374324262066", AdSize.BANNER_HEIGHT_50);
        LinearLayout adContainer = findViewById(R.id.banner_container);
        adContainer.addView(adView);
        adView.loadAd();

        interstitialAd = new InterstitialAd(this, "513372960928869_513374487595383");
        // Create listeners for the Interstitial Ad
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
                Log.e(TAG, "Interstitial ad displayed.");
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                Log.e(TAG, "Interstitial ad dismissed.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Interstitial ad is loaded and ready to be displayed
                Log.d(TAG, "Interstitial ad is loaded and ready to be displayed!");
                // Show the ad
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
                Log.d(TAG, "Interstitial ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
                Log.d(TAG, "Interstitial ad impression logged!");
            }
        };

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        interstitialAd.loadAd(
                interstitialAd.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());
    }

    private void showAdWithDelay() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            // Check if interstitialAd has been loaded successfully
            if (interstitialAd == null || !interstitialAd.isAdLoaded()) {
                return;
            }
            // Check if ad is already expired or invalidated, and do not show ad if that is the case. You will not get paid to show an invalidated ad.
            if (interstitialAd.isAdInvalidated()) {
                return;
            }
            // Show the ad
            interstitialAd.show();
        }, 1000 * 60 * 2); // Show the ad after 15 minutes
    }

    void activateReviewInfo() {
        manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> manaInfoTask = manager.requestReviewFlow();
        manaInfoTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInfo = task.getResult();
            } else {
                Log.d(MainActivity.class.getSimpleName(),"Review fail to start!");
//                Toast.makeText(this, "Review fail to start!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void startReviewFlow() {
        if (reviewInfo != null) {
            Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
            flow.addOnCompleteListener(task -> {
                Menu menu_rate = navigationView.getMenu();
                menu_rate.findItem(R.id.action_rate).setVisible(false);
                Toast.makeText(this, "Rating is complete!", Toast.LENGTH_SHORT).show();
            });
        }
    }

}