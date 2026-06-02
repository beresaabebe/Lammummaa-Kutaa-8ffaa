package com.beckytech.lammummaakutaa8ffaa.contents;

import android.content.Context;
import com.beckytech.lammummaakutaa8ffaa.R;

public class TitleContents {
    public String[] getTitles(Context context) {
        return context.getResources().getStringArray(R.array.titles);
    }

    // Deprecated: for backward compatibility during transition if needed
    public String[] title = {
            "Civic Virtues",
            "State and Government Administration",
            "Democracy",
            "Constitutionalism",
            "Human Rights",
            "Conflict Resolution and Peacebuilding",
            "Critical Thinking and Problem Solving Skills"
    };
}
