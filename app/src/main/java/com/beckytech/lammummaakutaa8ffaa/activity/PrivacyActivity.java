package com.beckytech.lammummaakutaa8ffaa.activity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.beckytech.lammummaakutaa8ffaa.R;

public class PrivacyActivity extends AppCompatActivity {

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy);

        findViewById(R.id.ib_back).setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        ProgressBar progressBar = findViewById(R.id.progress_horizontal);

        TextView tv_title = findViewById(R.id.tv_title);
        tv_title.setText(R.string.privacy_title);

        WebView webView = findViewById(R.id.webView_privacy);
        webView.loadUrl("https://yoosaad.com/beresa-android-website-privacy-policy/");
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webView.loadUrl("file:///android_asset/error.html");
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
