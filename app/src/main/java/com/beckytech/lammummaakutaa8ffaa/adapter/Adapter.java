package com.beckytech.lammummaakutaa8ffaa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.model.Model;
import com.google.android.gms.ads.AdView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Object> list;
    private final onBookClicked bookClicked;

    private static final int ITEM_TYPE_BOOK = 0;
    private static final int ITEM_TYPE_BANNER = 1;

    public Adapter(List<Object> list, onBookClicked bookClicked) {
        this.list = list;
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
        } else {
            View bannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_banner_container, parent, false);
            return new AdViewHolder(bannerView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ITEM_TYPE_BOOK) {
            Model model = (Model) list.get(position);
            PageViewHolder pageViewHolder = (PageViewHolder) holder;
            pageViewHolder.title.setText(model.getTitle());
            pageViewHolder.subTitle.setText(model.getSubTitle());
            pageViewHolder.itemView.setOnClickListener(v -> bookClicked.clickedBook(model));
        } else {
            AdViewHolder adViewHolder = (AdViewHolder) holder;
            AdView adView = (AdView) list.get(position);
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

    @Override
    public int getItemViewType(int position) {
        return (list.get(position) instanceof Model) ? ITEM_TYPE_BOOK : ITEM_TYPE_BANNER;
    }

    @Override
    public int getItemCount() {
        return list.size();
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
}
