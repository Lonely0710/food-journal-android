package com.example.tastylog.utils;

import android.util.Log;

import androidx.fragment.app.Fragment;

public class FragmentUtils {
    
    /**
     * 安全地在Fragment上执行UI操作
     * @param fragment 要执行操作的Fragment
     * @param action 要执行的操作
     * @param errorTag 错误日志标签
     */
    public static void safeUIAction(Fragment fragment, Runnable action, String errorTag) {
        if (fragment == null) {
            Log.e(errorTag, "Fragment已为null");
            return;
        }
        
        if (fragment.isAdded() && fragment.getActivity() != null) {
            fragment.getActivity().runOnUiThread(() -> {
                // 再次检查Fragment状态
                if (fragment.isAdded()) {
                    try {
                        action.run();
                    } catch (Exception e) {
                        Log.e(errorTag, "UI操作执行失败: " + e.getMessage());
                    }
                } else {
                    Log.e(errorTag, "Fragment已分离，无法执行UI操作");
                }
            });
        } else {
            Log.e(errorTag, "Fragment未附加到Activity，无法执行UI操作");
        }
    }
} 