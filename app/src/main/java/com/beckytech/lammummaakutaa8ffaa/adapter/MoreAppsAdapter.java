package com.beckytech.lammummaakutaa8ffaa.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.beckytech.lammummaakutaa8ffaa.R;
import com.beckytech.lammummaakutaa8ffaa.model.MoreAppsModel;

import java.util.List;

public class MoreAppsAdapter extends RecyclerView.Adapter<MoreAppsAdapter.AppsViewHolder> {
    private final List<MoreAppsModel> modelList;
    private final MoreAppsClicked moreAppsClicked;

    public MoreAppsAdapter(List<MoreAppsModel> modelList, MoreAppsClicked moreAppsClicked) {
        this.modelList = modelList;
        this.moreAppsClicked = moreAppsClicked;
    }

    @NonNull
    @Override
    public AppsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AppsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.moreapps_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AppsViewHolder holder, int position) {
        MoreAppsModel model = modelList.get(position);
        holder.appName.setText(model.getAppName());
        holder.appImages.setImageResource(model.getImages());
        holder.itemView.setOnClickListener(v -> moreAppsClicked.appClicked(model));
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public interface MoreAppsClicked {
        void appClicked(MoreAppsModel model);
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
