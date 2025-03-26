package com.example.tastylog.utils;

import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;
import java.util.Map;

/**
 * 地理编码辅助类
 * 提供中国主要城市的地理坐标查询功能
 */
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
        
        // 特别行政区
        CITY_COORDS.put("香港", new GeoPoint(22.396428, 114.109497));
        CITY_COORDS.put("澳门", new GeoPoint(22.198745, 113.543873));
        
        // 华南地区主要城市
        CITY_COORDS.put("广州", new GeoPoint(23.129110, 113.264385));
        CITY_COORDS.put("深圳", new GeoPoint(22.543096, 114.057865));
        CITY_COORDS.put("珠海", new GeoPoint(22.270715, 113.576726));
        CITY_COORDS.put("佛山", new GeoPoint(23.021841, 113.121841));
        CITY_COORDS.put("东莞", new GeoPoint(23.020673, 113.751765));
        CITY_COORDS.put("惠州", new GeoPoint(23.111847, 114.416786));
        CITY_COORDS.put("中山", new GeoPoint(22.517645, 113.392782));
        CITY_COORDS.put("江门", new GeoPoint(22.578738, 113.081901));
        CITY_COORDS.put("顺德", new GeoPoint(22.7653, 113.2418));
        
        // 华东地区主要城市
        CITY_COORDS.put("杭州", new GeoPoint(30.274084, 120.155070));
        CITY_COORDS.put("南京", new GeoPoint(32.060255, 118.796877));
        CITY_COORDS.put("苏州", new GeoPoint(31.299379, 120.585316));
        CITY_COORDS.put("无锡", new GeoPoint(31.491169, 120.311910));
        CITY_COORDS.put("宁波", new GeoPoint(29.868336, 121.549792));
        CITY_COORDS.put("温州", new GeoPoint(27.993828, 120.699367));
        CITY_COORDS.put("绍兴", new GeoPoint(30.029752, 120.592467));
        CITY_COORDS.put("嘉兴", new GeoPoint(30.746129, 120.755486));
        CITY_COORDS.put("金华", new GeoPoint(29.079059, 119.649506));
        CITY_COORDS.put("常州", new GeoPoint(31.810689, 119.974061));
        
        // 华中地区主要城市
        CITY_COORDS.put("武汉", new GeoPoint(30.592849, 114.305539));
        CITY_COORDS.put("长沙", new GeoPoint(28.228209, 112.938814));
        CITY_COORDS.put("郑州", new GeoPoint(34.746611, 113.625328));
        CITY_COORDS.put("洛阳", new GeoPoint(34.618124, 112.454420));
        CITY_COORDS.put("株洲", new GeoPoint(27.827433, 113.134002));
        CITY_COORDS.put("岳阳", new GeoPoint(29.357280, 113.128958));
        
        // 西南地区主要城市
        CITY_COORDS.put("成都", new GeoPoint(30.572816, 104.066801));
        CITY_COORDS.put("重庆", new GeoPoint(29.563010, 106.551556));
        CITY_COORDS.put("贵阳", new GeoPoint(26.578343, 106.713478));
        CITY_COORDS.put("昆明", new GeoPoint(24.880095, 102.832891));
        CITY_COORDS.put("乐山", new GeoPoint(29.552115, 103.765568));
        CITY_COORDS.put("绵阳", new GeoPoint(31.467459, 104.679114));
        
        // 华北地区主要城市
        CITY_COORDS.put("石家庄", new GeoPoint(38.042307, 114.515358));
        CITY_COORDS.put("太原", new GeoPoint(37.870590, 112.548879));
        CITY_COORDS.put("呼和浩特", new GeoPoint(40.842585, 111.749181));
        CITY_COORDS.put("保定", new GeoPoint(38.873958, 115.464589));
        CITY_COORDS.put("唐山", new GeoPoint(39.630867, 118.180194));
        CITY_COORDS.put("秦皇岛", new GeoPoint(39.935385, 119.600493));
        
        // 东北地区主要城市
        CITY_COORDS.put("沈阳", new GeoPoint(41.805698, 123.431474));
        CITY_COORDS.put("大连", new GeoPoint(38.914003, 121.614682));
        CITY_COORDS.put("长春", new GeoPoint(43.817071, 125.323544));
        CITY_COORDS.put("哈尔滨", new GeoPoint(45.803775, 126.534967));
        CITY_COORDS.put("鞍山", new GeoPoint(41.107769, 123.007763));
        CITY_COORDS.put("吉林", new GeoPoint(43.837883, 126.549572));
        
        // 西北地区主要城市
        CITY_COORDS.put("西安", new GeoPoint(34.341575, 108.940175));
        CITY_COORDS.put("兰州", new GeoPoint(36.061089, 103.834304));
        CITY_COORDS.put("西宁", new GeoPoint(36.617144, 101.778228));
        CITY_COORDS.put("银川", new GeoPoint(38.487194, 106.230909));
        CITY_COORDS.put("乌鲁木齐", new GeoPoint(43.825592, 87.616848));
        CITY_COORDS.put("咸阳", new GeoPoint(34.329605, 108.708991));
        
        // 著名旅游城市
        CITY_COORDS.put("桂林", new GeoPoint(25.274215, 110.290195));
        CITY_COORDS.put("三亚", new GeoPoint(18.252847, 109.511909));
        CITY_COORDS.put("丽江", new GeoPoint(26.855047, 100.227750));
        CITY_COORDS.put("大理", new GeoPoint(25.606486, 100.267638));
        CITY_COORDS.put("张家界", new GeoPoint(29.117096, 110.479191));
        CITY_COORDS.put("黄山", new GeoPoint(29.714699, 118.337481));
        CITY_COORDS.put("苏州", new GeoPoint(31.299379, 120.585316));
        CITY_COORDS.put("杭州", new GeoPoint(30.274084, 120.155070));
        
        // 其他重要城市
        CITY_COORDS.put("厦门", new GeoPoint(24.479834, 118.089425));
        CITY_COORDS.put("福州", new GeoPoint(26.074208, 119.296494));
        CITY_COORDS.put("泉州", new GeoPoint(24.874132, 118.675675));
        CITY_COORDS.put("莆田", new GeoPoint(25.454085, 119.007558));
        CITY_COORDS.put("南通", new GeoPoint(31.980172, 120.894291));
        CITY_COORDS.put("徐州", new GeoPoint(34.205768, 117.284124));
        CITY_COORDS.put("烟台", new GeoPoint(37.463822, 121.447935));
        CITY_COORDS.put("威海", new GeoPoint(37.513068, 122.120420));
        CITY_COORDS.put("济南", new GeoPoint(36.651216, 117.120095));
        CITY_COORDS.put("青岛", new GeoPoint(36.067082, 120.382639));
    }
    
    /**
     * 根据地点名称获取地理坐标
     * @param location 地点名称
     * @return 地理坐标点，如果未找到匹配则返回null
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