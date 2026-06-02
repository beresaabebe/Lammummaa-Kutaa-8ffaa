package com.beckytech.lammummaakutaa8ffaa.contents;

import android.content.Context;
import com.beckytech.lammummaakutaa8ffaa.R;

public class AboutName {
    public String[] getNames(Context context) {
        return context.getResources().getStringArray(R.array.about_names);
    }

    // Deprecated
    public String[] name = {
            "Join Facebook",
            "Learn Afaan Oromoo",
            "Yoosaad.com",
            "Follow us on Facebook",
            "Join Telegram",
            "Call us",
            "SBOO - Oromo Orthodox",
            "Subscribe Mezmur Channel"
    };
}
