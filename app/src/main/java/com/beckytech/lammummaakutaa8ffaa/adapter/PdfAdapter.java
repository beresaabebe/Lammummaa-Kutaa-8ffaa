package com.beckytech.lammummaakutaa8ffaa.adapter;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import android.util.LruCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.vungle.ads.internal.ui.view.MediaView;

public class PdfAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_PAGE = 0;
    private static final int TYPE_NATIVE_AD = 1;
    private static final int TYPE_BANNER_AD = 2;
    private static final int TYPE_VUNGLE_NATIVE_AD = 3;
    private static final int AD_INTERVAL = 4;

    private final ParcelFileDescriptor fileDescriptor;
    private final PdfRenderer pdfRenderer;
    private final List<Object> items;
    private final LruCache<Integer, Bitmap> bitmapCache;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int pagesReadCount = 0;
    private final OnPagesReadListener pagesReadListener;
    private boolean isClosed = false;

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

        int adCount = 0;
        int maxAds = 3; // Limit ads per chapter
        for (int i = 0; i < pages.length; i++) {
            items.add(pages[i]);
            if ((i + 1) % AD_INTERVAL == 0 && adCount < maxAds) {
                items.add("AD_PLACEHOLDER");
                adCount++;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_PAGE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pdf_page_item, parent, false);
            return new PdfViewHolder(view);
        } else if (viewType == TYPE_BANNER_AD) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_banner_container, parent, false);
            return new BannerViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_layout, parent, false);
            return new NativeAdViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_PAGE) {
            int pageIndex = (int) items.get(position);
            PdfViewHolder pdfViewHolder = (PdfViewHolder) holder;
            
            int pageCount;
            synchronized (pdfRenderer) {
                if (isClosed) return;
                pageCount = pdfRenderer.getPageCount();
            }
            
            if (pageIndex < 0 || pageIndex >= pageCount) return;

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
        } else if (viewType == TYPE_NATIVE_AD) {
            NativeAd nativeAd = (NativeAd) items.get(position);
            populateNativeAdView(nativeAd, (NativeAdView) holder.itemView);
        } else if (viewType == TYPE_VUNGLE_NATIVE_AD) {
            com.vungle.ads.NativeAd vungleNativeAd = (com.vungle.ads.NativeAd) items.get(position);
            populateVungleNativeAdView(vungleNativeAd, holder.itemView);
        } else if (viewType == TYPE_BANNER_AD) {
            View adView = (View) items.get(position);
            BannerViewHolder bannerViewHolder = (BannerViewHolder) holder;
            ViewGroup container = (ViewGroup) bannerViewHolder.itemView;
            if (container.getChildCount() > 0) container.removeAllViews();
            if (adView.getParent() != null) ((ViewGroup) adView.getParent()).removeView(adView);
            container.addView(adView);
        } else {
            // AD_PLACEHOLDER
            loadAdAtPosition(position, holder.itemView.getContext());
        }
    }

    private void populateVungleNativeAdView(com.vungle.ads.NativeAd nativeAd, View adView) {
        TextView title = adView.findViewById(R.id.ad_headline);
        TextView body = adView.findViewById(R.id.ad_body);
        Button cta = adView.findViewById(R.id.ad_call_to_action);
        ImageView icon = adView.findViewById(R.id.ad_app_icon);

        title.setText(nativeAd.getAdTitle());
        body.setText(nativeAd.getAdBodyText());
        cta.setText(nativeAd.getAdCallToActionText());
        
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(title);
        clickableViews.add(cta);
        
        // Vungle SDK 7 uses its own MediaView.
        MediaView vungleMediaView = nativeAd.getMediaView();
        if (vungleMediaView != null && adView instanceof FrameLayout) {
            nativeAd.registerViewForInteraction((FrameLayout) adView, vungleMediaView, icon, clickableViews);
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
            if (isClosed) return null;
            try (PdfRenderer.Page page = pdfRenderer.openPage(pageIndex)) {
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth() * 2, page.getHeight() * 2, Bitmap.Config.ARGB_8888);
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                return bitmap;
            } catch (Exception e) {
                return null;
            }
        }
    }

    private void loadAdAtPosition(int position, android.content.Context context) {
        AdManagerHelper.loadNativeAd(context, context.getString(R.string.google_native_ads_unit_id), new AdManagerHelper.FlexibleAdListener() {
            @Override
            public void onAdLoaded(Object ad) {
                synchronized (pdfRenderer) {
                    if (isClosed) {
                        if (ad instanceof NativeAd) ((NativeAd) ad).destroy();
                        return;
                    }
                }
                Object oldItem = items.get(position);
                if (oldItem instanceof NativeAd) {
                    ((NativeAd) oldItem).destroy();
                }
                items.set(position, ad);
                notifyItemChanged(position);
            }

            @Override
            public void onAdFailed() {
                synchronized (pdfRenderer) {
                    if (isClosed) return;
                }
                items.remove(position);
                notifyItemRemoved(position);
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
        Object item = items.get(position);
        if (item instanceof Integer) return TYPE_PAGE;
        if (item instanceof NativeAd) return TYPE_NATIVE_AD;
        if (item instanceof com.vungle.ads.NativeAd) return TYPE_VUNGLE_NATIVE_AD;
        if (item instanceof View) return TYPE_BANNER_AD;
        return -1; // AD_PLACEHOLDER or unknown
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void close() {
        synchronized (pdfRenderer) {
            if (isClosed) return;
            isClosed = true;
            executorService.shutdownNow();
            
            for (Object item : items) {
                if (item instanceof NativeAd) {
                    ((NativeAd) item).destroy();
                } else if (item instanceof com.vungle.ads.NativeAd) {
                    // Vungle ads are usually destroyed when the View is detached or via their own lifecycle, 
                    // but we should check if there's an explicit destroy.
                    // For Vungle SDK 7, the ad object itself doesn't have a destroy() but it's good to null it.
                }
            }

            try {
                pdfRenderer.close();
                fileDescriptor.close();
            } catch (IOException | IllegalStateException e) {
                e.printStackTrace();
            }
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

    static class BannerViewHolder extends RecyclerView.ViewHolder {
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
