package com.beckytech.lammummaakutaa8ffaa.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.BuildConfig;
import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.adapter.AboutAdapter;
import com.beckytech.lammummaakutaa8ffaa.contents.AboutImages;
import com.beckytech.lammummaakutaa8ffaa.contents.AboutName;
import com.beckytech.lammummaakutaa8ffaa.contents.AboutUrlContents;
import com.beckytech.lammummaakutaa8ffaa.model.AboutModel;
import com.beckytech.lammummaakutaa8ffaa.service.AdManagerHelper;
import com.beckytech.lammummaakutaa8ffaa.service.LocaleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AboutActivity extends AppCompatActivity implements AboutAdapter.OnLinkClicked {
    private List<AboutModel> modelList;
    private final AboutImages images = new AboutImages();
    private final AboutName name = new AboutName();
    private final AboutUrlContents urlContents = new AboutUrlContents();

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        AdManagerHelper.loadBanner(findViewById(R.id.banner_container), getString(R.string.google_banner_about_unit_id));
        AdManagerHelper.loadInterstitial(this, getString(R.string.google_interstitial_ads_unit_id));

        findViewById(R.id.ib_back).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        TextView title = findViewById(R.id.tv_title);
        title.setText(R.string.about_us);

        WebView webView = findViewById(R.id.webView);
        if (LocaleHelper.getLanguage(this).equals("om")) {
            webView.loadUrl("file:///android_asset/about_om.html");
        } else {
            webView.loadUrl("file:///android_asset/about.html");
        }

        TextView version = findViewById(R.id.version_tv);
        version.setText(String.format(Locale.ENGLISH, "Version: %s", BuildConfig.VERSION_NAME));

        findViewById(R.id.imageView).setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, "Check out this app: https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(intent, "Share via"));
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_about);
        getData();
        AboutAdapter adapter = new AboutAdapter(modelList, this);
        recyclerView.setAdapter(adapter);
    }

    private void getData() {
        modelList = new ArrayList<>();
        String[] names = name.getNames(this);
        for (int i = 0; i < names.length; i++) {
            modelList.add(new AboutModel(images.images[i], names[i], urlContents.url[i]));
        }
    }

    @Override
    public void linkClicked(AboutModel model) {
        AdManagerHelper.showInterstitial(this);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(model.getUrl()));
        startActivity(intent);
    }
}
