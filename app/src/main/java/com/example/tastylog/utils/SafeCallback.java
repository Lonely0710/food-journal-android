package com.example.tastylog.utils;

import android.util.Log;

import androidx.fragment.app.Fragment;

/**
 * 安全回调工具类，用于处理Fragment生命周期问题
 */
public class SafeCallback {
    private static final String TAG = "SafeCallback";
    
    /**
     * 安全地在Fragment中执行操作，确保Fragment仍附加到Activity
     * @param fragment 目标Fragment
     * @param runnable 要执行的操作
     * @return 是否成功执行
     */
    public static boolean runIfFragmentAlive(Fragment fragment, Runnable runnable) {
        if (fragment == null) {
            Log.d(TAG, "Fragment为null，跳过操作");
            return false;
        }
        
        if (!fragment.isAdded()) {
            Log.d(TAG, "Fragment未附加到Activity，跳过操作");
            return false;
        }
        
        if (fragment.getActivity() == null) {
            Log.d(TAG, "Fragment的Activity为null，跳过操作");
            return false;
        }
        
        if (fragment.isDetached()) {
            Log.d(TAG, "Fragment已分离，跳过操作");
            return false;
        }
        
        if (fragment.isRemoving()) {
            Log.d(TAG, "Fragment正在被移除，跳过操作");
            return false;
        }
        
        try {
            runnable.run();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "执行Fragment操作时出错", e);
            return false;
        }
    }
} 