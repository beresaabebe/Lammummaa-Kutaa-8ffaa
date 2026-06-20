package com.beckytech.lammummaakutaa8ffaa;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.beckytech.lammummaakutaa8ffaa.service.LocaleHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.appupdate.AppUpdateOptions;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.ads.nativead.NativeAd;

import com.google.firebase.analytics.FirebaseAnalytics;

import androidx.appcompat.widget.SearchView;

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
    private AppUpdateManager appUpdateManager;
    private Adapter adapter;
    private FirebaseAnalytics mFirebaseAnalytics;

    private final ActivityResultLauncher<IntentSenderRequest> updateLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() != RESULT_OK) {
                    Toast.makeText(this, "Update failed or cancelled", Toast.LENGTH_SHORT).show();
                }
            }
    );

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_drawer);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        AppRate.app_launched(this);
        activateReviewInfo();
        checkUpdate(false);
        toolBarAndDrawerNavigation();
        booksDataRecycler();
        setupSearchView();

        AdManagerHelper.loadInterstitial(this, getString(R.string.google_interstitial_ads_unit_id));
        AdManagerHelper.loadRewardedInterstitial(this, getString(R.string.google_rewarded_interstitial_ads_unit_id));
        new Handler().postDelayed(() -> AdManagerHelper.showInterstitial(this), 10000);
    }

    private void setupSearchView() {
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter != null) {
                    adapter.filter(newText);
                }
                return true;
            }
        });
    }

    private void checkUpdate(boolean showToastIfNoUpdate) {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            updateLauncher,
                            AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                if (showToastIfNoUpdate) {
                    Toast.makeText(this, "App is up to date", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        adapter = new Adapter(modelList, this);
        recyclerView.setAdapter(adapter);
        insertAds();
    }

    private void getData() {
        modelList = new ArrayList<>();
        String[] titles = titleContents.getTitles(this);
        String[] subTitles = subTitleContent.getSubTitles(this);
        for (int i = 0; i < titles.length; i++) {
            modelList.add(new Model(
                    titles[i],
                    subTitles[i],
                    startPage.pageStart[i],
                    endPage.pageEnd[i]));
        }
    }

    private void insertAds() {
        for (int i = ADS_PER_ITEM; i <= modelList.size(); i += ADS_PER_ITEM + 1) {
            final int index = i;
            modelList.add(index, "SHIMMER");
            adapter.notifyItemInserted(index);
            AdManagerHelper.loadNativeAd(this, getString(R.string.google_native_ads_unit_id), new AdManagerHelper.NativeAdListener() {
                @Override
                public void onNativeAdLoaded(NativeAd nativeAd) {
                    modelList.set(index, nativeAd);
                    adapter.notifyItemChanged(index);
                }

                @Override
                public void onNativeAdFailed() {
                    // Fallback to banner if native fails
                    AdView adView = new AdView(MainActivity.this);
                    adView.setAdUnitId(getString(R.string.google_banner_ad_unit_id_main));
                    adView.setAdSize(AdSize.BANNER);
                    adView.loadAd(new AdRequest.Builder().build());
                    modelList.set(index, adView);
                    adapter.notifyItemChanged(index);
                }
            });
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
        } else if (id == R.id.action_language) {
            showLanguageDialog();
        } else if (id == R.id.action_update) {
            checkUpdate(true);
        } else if (id == R.id.action_exit) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.exit)
                    .setMessage(R.string.exit_msg)
                    .setPositiveButton(R.string.yes, (dialog, which) -> finish())
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
    }

    private void showLanguageDialog() {
        String[] languages = {"English", "Afaan Oromoo"};
        int checkedItem = LocaleHelper.getLanguage(this).equals("om") ? 1 : 0;

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.change_language)
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    if (which == 0) {
                        LocaleHelper.setLocale(MainActivity.this, "en");
                    } else {
                        LocaleHelper.setLocale(MainActivity.this, "om");
                    }
                    dialog.dismiss();
                    recreate();
                })
                .show();
    }

    @Override
    public void clickedBook(Model model) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, model.getTitle());
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, model.getTitle());
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "chapter");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        if (new java.util.Random().nextInt(3) == 0) {
            AdManagerHelper.showRewardedInterstitial(this);
            AdManagerHelper.loadRewardedInterstitial(this, getString(R.string.google_rewarded_interstitial_ads_unit_id));
        } else {
            AdManagerHelper.showInterstitial(this);
            AdManagerHelper.loadInterstitial(this, getString(R.string.google_interstitial_ads_unit_id));
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        if (appUpdateManager != null) {
            appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                updateLauncher,
                                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
