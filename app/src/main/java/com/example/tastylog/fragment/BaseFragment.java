package com.example.tastylog.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.example.tastylog.R;

public abstract class BaseFragment extends Fragment {
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            TextView titleView = toolbar.findViewById(R.id.toolbar_title);
            titleView.setText(getToolbarTitle());
        }
    }
    
    // 子类实现此方法返回Toolbar标题
    protected abstract String getToolbarTitle();
} 