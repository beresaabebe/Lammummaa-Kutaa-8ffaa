package com.beckytech.lammummaakutaa8ffaa.activity;

import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.adapter.PdfAdapter;
import com.beckytech.lammummaakutaa8ffaa.model.Model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ChapterFragment extends Fragment {

    private Model model;
    private PdfAdapter pdfAdapter;

    public static ChapterFragment newInstance(Model model) {
        ChapterFragment fragment = new ChapterFragment();
        Bundle args = new Bundle();
        args.putSerializable("model", model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            model = (Model) getArguments().getSerializable("model");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chapter, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (model != null) {
            loadPdf(recyclerView);
        }

        return view;
    }

    private void loadPdf(RecyclerView recyclerView) {
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
            File file = new File(requireContext().getCacheDir(), "temp.pdf");
            if (!file.exists()) {
                InputStream is = requireContext().getAssets().open("lm8.pdf");
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
            pdfAdapter = new PdfAdapter(pfd, array, count -> {});
            recyclerView.setAdapter(pdfAdapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (pdfAdapter != null) {
            pdfAdapter.close();
        }
        super.onDestroy();
    }
}
