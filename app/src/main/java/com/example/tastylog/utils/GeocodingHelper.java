package com.example.tastylog.utils;

import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;

public class GeocodingHelper {
    private static final String TAG = "GeocodingHelper";
    
    // 主要城市坐标映射表
    private static final Map<String, GeoPoint> CITY_COORDS = new HashMap<>();
    
    static {
        // 初始化中国主要城市坐标
        // 直辖市
        CITY_COORDS.put("北京", new GeoPoint(39.904989, 116.405285));
        CITY_COORDS.put("上海", new GeoPoint(31.230416, 121.473701));
        CITY_COORDS.put("天津", new GeoPoint(39.125596, 117.190182));
        CITY_COORDS.put("重庆", new GeoPoint(29.563010, 106.551556));
        
        // 主要城市/地区
        CITY_COORDS.put("广州", new GeoPoint(23.129110, 113.264385));
        CITY_COORDS.put("深圳", new GeoPoint(22.543096, 114.057865));
        CITY_COORDS.put("杭州", new GeoPoint(30.274084, 120.155070));
        CITY_COORDS.put("南京", new GeoPoint(32.060255, 118.796877));
        CITY_COORDS.put("武汉", new GeoPoint(30.592849, 114.305539));
        CITY_COORDS.put("成都", new GeoPoint(30.572816, 104.066801));
        CITY_COORDS.put("西安", new GeoPoint(34.341575, 108.940175));
        CITY_COORDS.put("顺德", new GeoPoint(22.7653, 113.2418)); // 广东佛山顺德区
        CITY_COORDS.put("佛山", new GeoPoint(23.0218, 113.1218)); // 广东佛山
        
        // 各省会城市
        CITY_COORDS.put("长春", new GeoPoint(43.817071, 125.323544));
        CITY_COORDS.put("长沙", new GeoPoint(28.228209, 112.938814));
        CITY_COORDS.put("福州", new GeoPoint(26.074208, 119.296494));
        CITY_COORDS.put("合肥", new GeoPoint(31.820587, 117.227239));
        CITY_COORDS.put("济南", new GeoPoint(36.651216, 117.120095));
        CITY_COORDS.put("昆明", new GeoPoint(24.880095, 102.832891));
        CITY_COORDS.put("兰州", new GeoPoint(36.061089, 103.834304));
        CITY_COORDS.put("南昌", new GeoPoint(28.682892, 115.858197));
        CITY_COORDS.put("南宁", new GeoPoint(22.817002, 108.366543));
        CITY_COORDS.put("沈阳", new GeoPoint(41.805698, 123.431474));
        CITY_COORDS.put("石家庄", new GeoPoint(38.042307, 114.515358));
        CITY_COORDS.put("太原", new GeoPoint(37.870590, 112.548879));
        CITY_COORDS.put("乌鲁木齐", new GeoPoint(43.825592, 87.616848));
        CITY_COORDS.put("厦门", new GeoPoint(24.479834, 118.089425));
        CITY_COORDS.put("郑州", new GeoPoint(34.746611, 113.625328));
        CITY_COORDS.put("哈尔滨", new GeoPoint(45.803775, 126.534967));
        CITY_COORDS.put("海口", new GeoPoint(20.044001, 110.198293));
        CITY_COORDS.put("贵阳", new GeoPoint(26.578343, 106.713478));
        CITY_COORDS.put("西宁", new GeoPoint(36.617144, 101.778228));
        CITY_COORDS.put("银川", new GeoPoint(38.487194, 106.230909));
        CITY_COORDS.put("拉萨", new GeoPoint(29.645554, 91.140856));
        CITY_COORDS.put("呼和浩特", new GeoPoint(40.842585, 111.749181));
    }
    
    /**
     * 根据地点名称获取地理坐标
     * @param location 地点名称
     * @return 地理坐标点
     */
    public static GeoPoint getGeoPoint(String location) {
        if (location == null || location.isEmpty()) {
            return null;
        }
        
        // 尝试直接匹配城市名
        for (Map.Entry<String, GeoPoint> entry : CITY_COORDS.entrySet()) {
            if (location.contains(entry.getKey())) {
                Log.d(TAG, "找到位置匹配: " + location + " -> " + entry.getKey());
                return entry.getValue();
            }
        }
        
        // 如果没有匹配，返回null
        Log.w(TAG, "未找到匹配位置: " + location);
        return null;
    }
} 