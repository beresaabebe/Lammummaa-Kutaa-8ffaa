package com.beckytech.lammummaakutaa8ffaa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.MainActivity;
import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.model.Model;
import com.facebook.ads.AdView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Object> list;
    private final onBookClicked bookClicked;

    private final int ITEM_TYPE_BOOK = 0;
    private final int ITEM_TYPE_BANNER = 1;
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
        switch (viewType) {
            case ITEM_TYPE_BOOK:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
                return new PageViewHolder(view);
            case ITEM_TYPE_BANNER:
            default:
                View bannerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_banner_fb, parent, false);
                return new AdviewHolder(bannerView);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ITEM_TYPE_BOOK:
                if (list.get(position) instanceof Model) {
                    PageViewHolder pageViewHolder = (PageViewHolder) holder;
                    Model model = (Model) list.get(position);
                    pageViewHolder.title.setText(model.getTitle());
                    pageViewHolder.subTitle.setText(model.getSubTitle());
                    pageViewHolder.itemView.setOnClickListener(v -> bookClicked.clickedBook(model));
                }
                break;
            case ITEM_TYPE_BANNER:
                if (list.get(position) instanceof AdView) {
                    AdviewHolder adviewHolder = (AdviewHolder) holder;
                    AdView adView = (AdView) list.get(position);
                    ViewGroup viewGroup = (ViewGroup) adviewHolder.itemView;
                    if (viewGroup.getChildCount() > 0) {
                        viewGroup.removeAllViews();
                    }
                    if (viewGroup.getParent() != null) {
                        ((ViewGroup)adView.getParent()).removeView(adView);
                    }
                    viewGroup.addView(adView);
                }
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0 || list.get(position) instanceof Model) {
            return ITEM_TYPE_BOOK;
        }
        else {
            return (position % MainActivity.ADS_PER_ITEM == 0) ? ITEM_TYPE_BANNER : ITEM_TYPE_BOOK;
        }
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
            title.setSelected(true);
            subTitle = itemView.findViewById(R.id.subTitle);
            imageView = itemView.findViewById(R.id.image);
        }
    }
    static class AdviewHolder extends RecyclerView.ViewHolder {
        public AdviewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
