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
import com.beckytech.lammummaakutaa8ffaa.model.MoreAppsModel;
import com.facebook.ads.AdView;

import java.util.List;

public class MoreAppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Object> modelList;
    private final MoreAppsClicked moreAppsClicked;
    private final int ITEM_TYPE_APPS = 0;
    private final int ITEM_TYPE_BANNER = 1;
    public MoreAppsAdapter(List<Object> modelList, MoreAppsClicked moreAppsClicked) {
        this.modelList = modelList;
        this.moreAppsClicked = moreAppsClicked;
    }
    public interface MoreAppsClicked {
        public void appClicked(MoreAppsModel model);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_TYPE_APPS:
                return new AppsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.moreapps_list_item, parent, false));
            case ITEM_TYPE_BANNER:
            default:
                return new Adapter.AdviewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ad_banner_fb, parent, false));
        }
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        switch (viewType) {
            case ITEM_TYPE_APPS:
                if (modelList.get(position) instanceof MoreAppsModel) {
                    MoreAppsModel model = (MoreAppsModel) modelList.get(position);
                    ((AppsViewHolder) holder).appName.setText(model.getAppName());
                    ((AppsViewHolder) holder).appImages.setImageResource(model.getImages());
                    ((AppsViewHolder) holder).itemView.setOnClickListener(v -> moreAppsClicked.appClicked(model));
                }
                break;
            case ITEM_TYPE_BANNER:
                if (modelList.get(position) instanceof AdView) {
                    Adapter.AdviewHolder adViewHolder = (Adapter.AdviewHolder) holder;
                    AdView adView = (AdView) modelList.get(position);
                    ViewGroup viewGroup = (ViewGroup)adViewHolder.itemView;
                    if (viewGroup.getChildCount() > 0) {
                        viewGroup.removeAllViews();
                    }
                    if (viewGroup.getParent() != null) {
                        ((ViewGroup) adView.getParent()).removeView(adView);
                    }
                    viewGroup.addView(adView);
                }
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (position == 0 || modelList.get(position) instanceof MoreAppsModel) {
            return ITEM_TYPE_APPS;
        }
        else {
            return (position % MainActivity.ADS_PER_ITEM == 0) ? ITEM_TYPE_BANNER : ITEM_TYPE_APPS;
        }
    }
    @Override
    public int getItemCount() {
        return modelList.size();
    }
    public static class AppsViewHolder extends RecyclerView.ViewHolder {
        ImageView appImages;
        TextView appName;
        public AppsViewHolder(@NonNull View itemView) {
            super(itemView);
            appImages = itemView.findViewById(R.id.more_apps_image);
            appName = itemView.findViewById(R.id.txt_app_name);
        }
    }
}
