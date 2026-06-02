package com.beckytech.lammummaakutaa8ffaa.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.adapter.PdfAdapter;
import com.beckytech.lammummaakutaa8ffaa.model.Model;
import com.beckytech.lammummaakutaa8ffaa.service.AdManagerHelper;
import com.beckytech.lammummaakutaa8ffaa.service.LocaleHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BookDetailActivity extends AppCompatActivity {
    private final String TAG = BookDetailActivity.class.getSimpleName();
    private PdfAdapter pdfAdapter;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        allMainContents();
        
        AdManagerHelper.loadBanner(findViewById(R.id.banner_container), getString(R.string.google_banner_detail_unit_id));
        new Handler().postDelayed(() -> AdManagerHelper.loadInterstitial(this, getString(R.string.google_interstitial_ads_unit_id)), 5000);
        new Handler().postDelayed(() -> AdManagerHelper.showInterstitial(this), 30000);
    }

    private void allMainContents() {
        findViewById(R.id.back_book_detail).setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        Intent intent = getIntent();
        Model model = (Model) intent.getSerializableExtra("data");

        if (model == null) return;

        TextView title = findViewById(R.id.title_book_detail);
        title.setSelected(true);
        title.setText(model.getTitle());

        TextView subTitle = findViewById(R.id.sub_title_book_detail);
        subTitle.setSelected(true);
        subTitle.setText(model.getSubTitle());

        RecyclerView recyclerView = findViewById(R.id.pdf_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        int start = model.getStartPage();
        int end = model.getEndPage();

        List<Integer> list = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            list.add(i);
        }

        int[] array = new int[list.size()];
        for (int j = 0; j < array.length; j++) {
            array[j] = list.get(j);
        }

        try {
            File file = new File(getCacheDir(), "temp.pdf");
            if (!file.exists()) {
                InputStream is = getAssets().open("lm8.pdf");
                FileOutputStream os = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                is.close();
                os.flush();
                os.close();
            }
            
            ParcelFileDescriptor pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            pdfAdapter = new PdfAdapter(pfd, array);
            recyclerView.setAdapter(pdfAdapter);
        } catch (IOException e) {
            Log.e(TAG, "Error loading PDF", e);
        }
    }

    @Override
    protected void onDestroy() {
        if (pdfAdapter != null) {
            pdfAdapter.close();
        }
        super.onDestroy();
    }
}
