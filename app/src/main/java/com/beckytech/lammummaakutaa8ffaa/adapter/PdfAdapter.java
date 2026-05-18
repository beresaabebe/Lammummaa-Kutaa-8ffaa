package com.beckytech.lammummaakutaa8ffaa.adapter;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.IOException;

public class PdfAdapter extends RecyclerView.Adapter<PdfAdapter.PdfViewHolder> {

    private final ParcelFileDescriptor fileDescriptor;
    private final PdfRenderer pdfRenderer;
    private final int[] pages;

    public PdfAdapter(ParcelFileDescriptor fileDescriptor, int[] pages) throws IOException {
        this.fileDescriptor = fileDescriptor;
        this.pdfRenderer = new PdfRenderer(fileDescriptor);
        this.pages = pages;
    }

    @NonNull
    @Override
    public PdfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_page_item, parent, false);
        return new PdfViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PdfViewHolder holder, int position) {
        int pageIndex = pages[position];
        if (pageIndex < 0 || pageIndex >= pdfRenderer.getPageCount()) return;

        PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);
        Bitmap bitmap = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        holder.photoView.setImageBitmap(bitmap);
        page.close();
    }

    @Override
    public int getItemCount() {
        return pages.length;
    }

    public void close() {
        try {
            pdfRenderer.close();
            fileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class PdfViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        public PdfViewHolder(@NonNull View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.pdf_page_image);
        }
    }
}
