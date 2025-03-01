package com.example.tastylog.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tastylog.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MineFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine, container, false);
        
        initSwitches(view);
        initButtons(view);
        
        return view;
    }

    @Override
    protected String getToolbarTitle() {
        return "Profile";
    }

    private void initSwitches(View view) {
        SwitchMaterial switchBackup = view.findViewById(R.id.switch_backup);
        SwitchMaterial switchWatermark = view.findViewById(R.id.switch_watermark);
        
        switchBackup.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(requireContext(), "自动备份：" + isChecked, Toast.LENGTH_SHORT).show();
        });

        switchWatermark.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Toast.makeText(requireContext(), "水印功能：" + isChecked, Toast.LENGTH_SHORT).show();
        });
    }

    private void initButtons(View view) {
        // 导出格式按钮
        View btnExportFormat = view.findViewById(R.id.btn_export_format);
        TextView tvExportFormat = btnExportFormat.findViewById(R.id.tv_export_format);
        
        btnExportFormat.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(requireContext(), v);
            popup.getMenu().add("PDF");
            popup.getMenu().add("Excel");
            popup.getMenu().add("CSV");
            popup.setOnMenuItemClickListener(item -> {
                tvExportFormat.setText(item.getTitle());
                return true;
            });
            popup.show();
        });

        // 主题颜色按钮
        View btnThemeColor = view.findViewById(R.id.btn_theme_color);
        btnThemeColor.setOnClickListener(v -> {
            // TODO: 实现主题颜色选择
            Toast.makeText(requireContext(), "主题颜色选择功能开发中", Toast.LENGTH_SHORT).show();
        });
    }
} 