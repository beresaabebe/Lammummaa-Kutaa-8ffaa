package com.beckytech.lammummaakutaa8ffaa.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.model.Model;
import com.beckytech.lammummaakutaa8ffaa.service.AdManagerHelper;
import com.beckytech.lammummaakutaa8ffaa.service.LocaleHelper;
import com.beckytech.lammummaakutaa8ffaa.contents.ContentEndPage;
import com.beckytech.lammummaakutaa8ffaa.contents.ContentStartPage;
import com.beckytech.lammummaakutaa8ffaa.contents.SubTitleContents;
import com.beckytech.lammummaakutaa8ffaa.contents.TitleContents;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {
    private final String TAG = BookDetailActivity.class.getSimpleName();
    private FirebaseAnalytics mFirebaseAnalytics;
    private ViewPager2 viewPager;
    private List<Model> modelList;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        
        allMainContents();
        
        findViewById(R.id.banner_container).post(() -> 
            AdManagerHelper.loadAdaptiveBanner(this, findViewById(R.id.banner_container), getString(R.string.google_banner_detail_unit_id), true)
        );
        new Handler().postDelayed(() -> AdManagerHelper.loadInterstitial(this, getString(R.string.google_interstitial_ads_unit_id)), 5000);
    }

    private void shareApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String url = "https://play.google.com/store/apps/details?id=" + getPackageName();
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        intent.putExtra(Intent.EXTRA_TEXT, "Read " + getString(R.string.app_name) + " on Play store \n" + url);
        startActivity(Intent.createChooser(intent, "Share via"));
    }

    private void allMainContents() {
        findViewById(R.id.back_book_detail).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        findViewById(R.id.share_book_detail).setOnClickListener(v -> shareApp());

        Intent intent = getIntent();
        Model currentModel = (Model) intent.getSerializableExtra("data");

        if (currentModel == null) return;

        TextView title = findViewById(R.id.title_book_detail);
        title.setSelected(true);

        TextView subTitle = findViewById(R.id.sub_title_book_detail);
        subTitle.setSelected(true);

        getData();

        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public androidx.fragment.app.Fragment createFragment(int position) {
                return ChapterFragment.newInstance(modelList.get(position));
            }

            @Override
            public int getItemCount() {
                return modelList.size();
            }
        });

        int currentPos = 0;
        for (int i = 0; i < modelList.size(); i++) {
            if (modelList.get(i).getTitle().equals(currentModel.getTitle())) {
                currentPos = i;
                break;
            }
        }
        viewPager.setCurrentItem(currentPos, false);
        title.setText(modelList.get(currentPos).getTitle());
        subTitle.setText(modelList.get(currentPos).getSubTitle());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                title.setText(modelList.get(position).getTitle());
                subTitle.setText(modelList.get(position).getSubTitle());
                
                AdManagerHelper.showRandomRewardedAd(BookDetailActivity.this, 
                        getString(R.string.google_rewarded_ads_unit_id),
                        getString(R.string.google_rewarded_interstitial_ads_unit_id),
                        getString(R.string.google_interstitial_ads_unit_id));
            }
        });
    }

    private void getData() {
        modelList = new ArrayList<>();
        TitleContents titleContents = new TitleContents();
        SubTitleContents subTitleContent = new SubTitleContents();
        ContentStartPage startPage = new ContentStartPage();
        ContentEndPage endPage = new ContentEndPage();
        
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
}

