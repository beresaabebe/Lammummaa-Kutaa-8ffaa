package com.beckytech.lammummaakutaa8ffaa;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.gms.tasks.Task;

public class AppRate {
    private static final String APP_PNAME = "com.beckytech.lammummaakutaa8ffaa";

    private final static int DAYS_UNTIL_PROMPT = 3;
    private final static int LAUNCHES_UNTIL_PROMPT = 3;

    public static void app_launched(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences("apprater", 0);
        if (prefs.getBoolean("dontshowagain", false)) {
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();

        long launch_count = prefs.getLong("launch_count", 0) + 1;
        editor.putLong("launch_count", launch_count);

        long date_firstLaunch = prefs.getLong("date_firstlaunch", 0);
        if (date_firstLaunch == 0) {
            date_firstLaunch = System.currentTimeMillis();
            editor.putLong("date_firstlaunch", date_firstLaunch);
        }

        if (launch_count >= LAUNCHES_UNTIL_PROMPT) {
            if (System.currentTimeMillis() >= date_firstLaunch +
                    (DAYS_UNTIL_PROMPT * 24L * 60 * 60 * 1000)) {
                showRateDialog(activity, editor);
            }
        }

        editor.apply();
    }

    public static void showRateDialog(final Activity activity, final SharedPreferences.Editor editor) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_rate, null);
        
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        TextView statusText = view.findViewById(R.id.statusText);
        
        ratingBar.setOnRatingBarChangeListener((ratingBar1, rating, fromUser) -> {
            if (rating <= 1) statusText.setText("Bad");
            else if (rating <= 2) statusText.setText("Not good");
            else if (rating <= 3) statusText.setText("Some what good");
            else if (rating <= 4) statusText.setText("Very good");
            else statusText.setText("Excellent");
        });

        builder.setView(view)
                .setTitle(R.string.rate)
                .setPositiveButton(R.string.rate_now, (dialog, which) -> {
                    if (ratingBar.getRating() >= 4) {
                        launchInAppReview(activity);
                    } else {
                        Toast.makeText(activity, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
                    }
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.apply();
                    }
                })
                .setNeutralButton(R.string.remind_later, null)
                .setNegativeButton(R.string.no_thanks, (dialog, which) -> {
                    if (editor != null) {
                        editor.putBoolean("dontshowagain", true);
                        editor.apply();
                    }
                });
        builder.show();
    }

    public static void launchInAppReview(Activity activity) {
        ReviewManager manager = ReviewManagerFactory.create(activity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(task1 -> {});
            } else {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + APP_PNAME)));
            }
        });
    }
}

