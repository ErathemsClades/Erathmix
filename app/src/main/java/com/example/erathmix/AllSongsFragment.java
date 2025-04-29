package com.example.erathmix;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AllSongsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView tv = new TextView(getContext());
        tv.setText("All Songs will be shown here.");
        tv.setTextColor(0xFFFFFFFF);
        return tv;
    }
}
