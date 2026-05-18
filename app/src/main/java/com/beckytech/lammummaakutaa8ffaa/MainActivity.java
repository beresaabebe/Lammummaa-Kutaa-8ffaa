package com.beckytech.lammummaakutaa8ffaa;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
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
import com.beckytech.lammummaakutaa8ffaa.service.AdManagerHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Adapter.onBookClicked {
    public static final int ADS_PER_ITEM = 5;
    private final TitleContents titleContents = new TitleContents();
    private final SubTitleContents subTitleContent = new SubTitleContents();
    private final ContentStartPage startPage = new ContentStartPage();
    private final ContentEndPage endPage = new ContentEndPage();
    private List<Object> modelList;
    private ReviewInfo reviewInfo;
    private ReviewManager manager;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        AppRate.app_launched(this);
        activateReviewInfo();
        toolBarAndDrawerNavigation();
        booksDataRecycler();

        AdManagerHelper.loadInterstitial(this, getString(R.string.google_interstitial_ads_unit_id));
        new Handler().postDelayed(() -> AdManagerHelper.showInterstitial(this), 10000);
    }

    private void toolBarAndDrawerNavigation() {
        Toolbar toolbar = findViewById(R.id.toolbar);
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
        nav.findViewById(R.id.back_btn).setOnClickListener(view -> drawerLayout.closeDrawer(GravityCompat.START));
        nav.findViewById(R.id.share_btn).setOnClickListener(view -> shareBtn());
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
        insertAds();
        Adapter adapter = new Adapter(modelList, this);
        recyclerView.setAdapter(adapter);
    }

    private void getData() {
        modelList = new ArrayList<>();
        for (int i = 0; i < titleContents.title.length; i++) {
            modelList.add(new Model(
                    titleContents.title[i],
                    subTitleContent.subTitle[i],
                    startPage.pageStart[i],
                    endPage.pageEnd[i]));
        }
    }

    private void insertAds() {
        java.util.Random random = new java.util.Random();
        for (int i = ADS_PER_ITEM; i <= modelList.size(); i += ADS_PER_ITEM + 1) {
            AdView adView = new AdView(this);
            adView.setAdUnitId(getString(R.string.google_banner_ad_unit_id_main));
            if (random.nextBoolean()) {
                adView.setAdSize(AdSize.BANNER);
            } else {
                adView.setAdSize(AdSize.MEDIUM_RECTANGLE);
            }
            adView.loadAd(new AdRequest.Builder().build());
            modelList.add(i, adView);
        }
    }

    void MenuOptions(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        if (id == R.id.action_about_us) {
            startActivity(new Intent(this, AboutActivity.class));
        } else if (id == R.id.action_rate) {
            startReviewFlow();
        } else if (id == R.id.action_more_apps) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6669279757479011928"));
            startActivity(intent);
        } else if (id == R.id.action_share) {
            shareBtn();
        } else if (id == R.id.action_privacy) {
            startActivity(new Intent(this, PrivacyActivity.class));
        } else if (id == R.id.action_exit) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Exit")
                    .setMessage("Do you want to close?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Override
    public void clickedBook(Model model) {
        AdManagerHelper.showInterstitial(this);
        startActivity(new Intent(this, BookDetailActivity.class).putExtra("data", model));
    }

    void activateReviewInfo() {
        manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> manaInfoTask = manager.requestReviewFlow();
        manaInfoTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                reviewInfo = task.getResult();
            }
        });
    }

    void startReviewFlow() {
        if (reviewInfo != null) {
            manager.launchReviewFlow(this, reviewInfo).addOnCompleteListener(task -> {
                Toast.makeText(this, "Rating is complete!", Toast.LENGTH_SHORT).show();
            });
        } else {
            String pkg = getPackageName();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + pkg)));
        }
    }
}
