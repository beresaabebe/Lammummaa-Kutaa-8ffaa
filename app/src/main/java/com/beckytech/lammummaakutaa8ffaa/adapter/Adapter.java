package com.beckytech.lammummaakutaa8ffaa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Button;
import android.widget.RatingBar;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.model.Model;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.ArrayList;
import java.util.List;

import com.vungle.ads.internal.ui.view.MediaView;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Object> list;
    private List<Object> filteredList;
    private final onBookClicked bookClicked;

    private static final int ITEM_TYPE_BOOK = 0;
    private static final int ITEM_TYPE_BANNER = 1;
    private static final int ITEM_TYPE_NATIVE_AD = 2;
    private static final int ITEM_TYPE_SHIMMER = 3;
    private static final int ITEM_TYPE_VUNGLE_NATIVE_AD = 4;

    public Adapter(List<Object> list, onBookClicked bookClicked) {
        this.list = list;
        this.filteredList = new ArrayList<>(list);
        this.bookClicked = bookClicked;
    }

    public interface onBookClicked {
        void clickedBook(Model model);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_BOOK) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new PageViewHolder(view);
        } else if (viewType == ITEM_TYPE_NATIVE_AD) {
            View nativeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_layout, parent, false);
            return new NativeAdViewHolder(nativeView);
        } else if (viewType == ITEM_TYPE_VUNGLE_NATIVE_AD) {
            View vungleNativeView = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_layout, parent, false);
            return new NativeAdViewHolder(vungleNativeView);
        } else if (viewType == ITEM_TYPE_SHIMMER) {
            View shimmerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.native_ad_shimmer, parent, false);
            return new ShimmerViewHolder(shimmerView);
        } else {
            View bannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_banner_container, parent, false);
            return new AdViewHolder(bannerView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == ITEM_TYPE_BOOK) {
            Model model = (Model) filteredList.get(position);
            PageViewHolder pageViewHolder = (PageViewHolder) holder;
            pageViewHolder.title.setText(model.getTitle());
            pageViewHolder.subTitle.setText(model.getSubTitle());
            pageViewHolder.itemView.setOnClickListener(v -> bookClicked.clickedBook(model));
        } else if (viewType == ITEM_TYPE_NATIVE_AD) {
            NativeAd nativeAd = (NativeAd) filteredList.get(position);
            populateNativeAdView(nativeAd, (NativeAdView) holder.itemView);
        } else if (viewType == ITEM_TYPE_VUNGLE_NATIVE_AD) {
            com.vungle.ads.NativeAd vungleNativeAd = (com.vungle.ads.NativeAd) filteredList.get(position);
            populateVungleNativeAdView(vungleNativeAd, holder.itemView);
        } else if (viewType == ITEM_TYPE_SHIMMER) {
            // Shimmer starts automatically
        } else {
            AdViewHolder adViewHolder = (AdViewHolder) holder;
            View adView = (View) filteredList.get(position);
            ViewGroup adContainer = (ViewGroup) adViewHolder.itemView;
            if (adContainer.getChildCount() > 0) {
                adContainer.removeAllViews();
            }
            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
            adContainer.addView(adView);
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
        Object item = filteredList.get(position);
        if (item instanceof Model) {
            return ITEM_TYPE_BOOK;
        } else if (item instanceof NativeAd) {
            return ITEM_TYPE_NATIVE_AD;
        } else if (item instanceof com.vungle.ads.NativeAd) {
            return ITEM_TYPE_VUNGLE_NATIVE_AD;
        } else if (item instanceof String && item.equals("SHIMMER")) {
            return ITEM_TYPE_SHIMMER;
        } else {
            return ITEM_TYPE_BANNER;
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void filter(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(list);
        } else {
            for (Object item : list) {
                if (item instanceof Model) {
                    Model model = (Model) item;
                    if (model.getTitle().toLowerCase().contains(query.toLowerCase()) || 
                        model.getSubTitle().toLowerCase().contains(query.toLowerCase())) {
                        filteredList.add(item);
                    }
                } else {
                    // Optionally include ads in filtered results or exclude them
                    filteredList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        TextView title, subTitle;
        ImageView imageView;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            subTitle = itemView.findViewById(R.id.subTitle);
            imageView = itemView.findViewById(R.id.image);
        }
    }

    static class AdViewHolder extends RecyclerView.ViewHolder {
        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class NativeAdViewHolder extends RecyclerView.ViewHolder {
        public NativeAdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class ShimmerViewHolder extends RecyclerView.ViewHolder {
        public ShimmerViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
