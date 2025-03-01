package com.example.tastylog.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.tastylog.R;

public class StatsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);
        return view;
    }

    @Override
    protected String getToolbarTitle() {
        return "Statistics";
    }
} 