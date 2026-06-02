package com.beckytech.lammummaakutaa8ffaa.contents;

import android.content.Context;
import com.beckytech.lammummaakutaa8ffaa.R;

public class SubTitleContents {
    public String[] getSubTitles(Context context) {
        return context.getResources().getStringArray(R.array.subtitles);
    }

    // Deprecated
    public String[] subTitle = {
            "Chapter 1",
            "Chapter 2",
            "Chapter 3",
            "Chapter 4",
            "Chapter 5",
            "Chapter 6",
            "Chapter 7"
    };
}
