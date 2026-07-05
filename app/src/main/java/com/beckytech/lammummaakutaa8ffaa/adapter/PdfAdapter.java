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

import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.beckytech.lammummaakutaa8ffaa.service.AdManagerHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PdfAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PAGE = 0;
    private static final int TYPE_AD = 1;
    private static final int AD_INTERVAL = 4;

    private final ParcelFileDescriptor fileDescriptor;
    private final PdfRenderer pdfRenderer;
    private final List<Object> items;
    private final LruCache<Integer, Bitmap> bitmapCache;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int pagesReadCount = 0;
    private final OnPagesReadListener pagesReadListener;

    public interface OnPagesReadListener {
        void onPageRead(int count);
    }

    public PdfAdapter(ParcelFileDescriptor fileDescriptor, int[] pages, OnPagesReadListener listener) throws IOException {
        this.fileDescriptor = fileDescriptor;
        this.pdfRenderer = new PdfRenderer(fileDescriptor);
        this.items = new ArrayList<>();
        this.pagesReadListener = listener;
        
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        this.bitmapCache = new LruCache<Integer, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(Integer key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };

        for (int i = 0; i < pages.length; i++) {
            items.add(pages[i]);
            if ((i + 1) % AD_INTERVAL == 0) {
                items.add("AD_PLACEHOLDER");
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_page_item, parent, false);
            return new PdfViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_layout, parent, false);
            return new NativeAdViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_PAGE) {
            int pageIndex = (int) items.get(position);
            PdfViewHolder pdfViewHolder = (PdfViewHolder) holder;
            if (pageIndex < 0 || pageIndex >= pdfRenderer.getPageCount()) return;

            Bitmap cachedBitmap = bitmapCache.get(pageIndex);
            if (cachedBitmap != null) {
                pdfViewHolder.photoView.setImageBitmap(cachedBitmap);
                updatePagesRead();
            } else {
                pdfViewHolder.photoView.setImageResource(android.R.color.transparent);
                executorService.execute(() -> {
                    Bitmap bitmap = renderPage(pageIndex);
                    if (bitmap != null) {
                        bitmapCache.put(pageIndex, bitmap);
                        pdfViewHolder.itemView.post(() -> {
                            pdfViewHolder.photoView.setImageBitmap(bitmap);
                            updatePagesRead();
                        });
                    }
                });
            }
        } else {
            Object item = items.get(position);
            if (item instanceof NativeAd) {
                populateNativeAdView((NativeAd) item, (NativeAdView) holder.itemView);
            } else if (item.equals("AD_PLACEHOLDER")) {
                loadAdAtPosition(position, holder.itemView.getContext());
            }
        }
    }

    private void updatePagesRead() {
        pagesReadCount++;
        if (pagesReadListener != null) {
            pagesReadListener.onPageRead(pagesReadCount);
        }
    }

    private Bitmap renderPage(int pageIndex) {
        synchronized (pdfRenderer) {
            PdfRenderer.Page page = pdfRenderer.openPage(pageIndex);
            Bitmap bitmap = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            page.close();
            return bitmap;
        }
    }

    private void loadAdAtPosition(int position, android.content.Context context) {
        AdManagerHelper.loadNativeAd(context, context.getString(R.string.google_native_ads_unit_id), new AdManagerHelper.NativeAdListener() {
            @Override
            public void onNativeAdLoaded(NativeAd nativeAd) {
                items.set(position, nativeAd);
                notifyItemChanged(position);
            }

            @Override
            public void onNativeAdFailed() {
                // Fallback to banner
                AdView adView = new AdView(context);
                adView.setAdUnitId(context.getString(R.string.google_banner_ad_unit_id_main));
                adView.setAdSize(AdSize.BANNER);
                adView.loadAd(new AdRequest.Builder().build());
                items.set(position, adView);
                notifyItemChanged(position);
            }
        });
    }

    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView()).setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof Integer ? TYPE_PAGE : TYPE_AD;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void close() {
        executorService.shutdown();
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

    static class NativeAdViewHolder extends RecyclerView.ViewHolder {
        public NativeAdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
