package com.example.tastylog.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.tastylog.R;
import com.example.tastylog.adapter.FoodItemInfoWindow;
import com.example.tastylog.config.Config;
import com.example.tastylog.data.FoodRepository;
import com.example.tastylog.model.FoodItem;
import com.example.tastylog.utils.FragmentUtils;
import com.example.tastylog.utils.GeocodingHelper;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * 地图Fragment
 * 在地图上展示食物记录的位置分布
 */
public class MapFragment extends BaseFragment implements MapListener {

    private static final String TAG = "MapFragment";
    private MapView mapView;
    private List<Marker> markers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_map, container, false);
            
            mapView = view.findViewById(R.id.map_view);
            initMap();
            return view;
        } catch (Exception e) {
            Log.e(TAG, "地图初始化异常: " + e.getMessage(), e);
            // 返回一个备用视图，防止应用崩溃
            TextView errorView = new TextView(requireContext());
            errorView.setText("地图加载失败，请重试");
            errorView.setGravity(android.view.Gravity.CENTER);
            return errorView;
        }
    }

    /**
     * 初始化地图
     * 设置地图基本配置和加载数据
     */
    private void initMap() {
        try {
            // 设置瓦片源，使用OpenStreetMap先测试是否有网络
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            
            // 或使用天地图
            // mapView.setTileSource(Config.TDTVEC_W);
            
            // 添加调试日志
            Log.d(TAG, "瓦片源已设置: " + mapView.getTileProvider().getTileSource().name());
            
            // 添加注记图层（可选）
            // TilesOverlay tilesOverlay = new TilesOverlay(
            //     new MapTileProviderBasic(requireContext(), Config.TDTCIA_W), requireContext());
            // mapView.getOverlays().add(tilesOverlay);
            
            // 启用内置缩放控制
            mapView.setMultiTouchControls(true);
            mapView.setBuiltInZoomControls(true);
            
            // 设置初始缩放级别和中心点（缩小一点，更容易看到地图）
            IMapController mapController = mapView.getController();
            mapController.setZoom(4.0);
            mapController.setCenter(Config.defaultGeoPoint);
            
            // 设置地图监听器
            mapView.addMapListener(this);
            
            // 加载数据
            loadFoodItems();
        } catch (Exception e) {
            Log.e(TAG, "地图初始化失败: " + e.getMessage(), e);
        }
    }

    // 加载美食数据
    private void loadFoodItems() {
        try {
            FoodRepository.getInstance(requireContext()).getAllFoodItems(new FoodRepository.FoodListCallback() {
                @Override
                public void onFoodListLoaded(List<FoodItem> foodItems) {
                    requireActivity().runOnUiThread(() -> {
                        if (foodItems.isEmpty()) {
                            Log.w(TAG, "没有找到美食数据");
                            return;
                        }
                        
                        Log.d(TAG, "开始添加美食标记，共" + foodItems.size() + "个");
                        
                        for (FoodItem item : foodItems) {
                            addFoodItemMarker(item);
                        }
                        
                        // 调整地图以显示所有标记
                        if (!markers.isEmpty()) {
                            zoomToFitAllMarkers();
                        }
                    });
                }
                
                @Override
                public void onError(Exception e) {
                    FragmentUtils.safeUIAction(MapFragment.this, () -> {
                        Toast.makeText(requireContext(), "加载美食数据失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }, "MapFragment");
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "加载美食数据异常: " + e.getMessage(), e);
        }
    }
    
    // 为食品项添加标记
    private void addFoodItemMarker(FoodItem item) {
        String location = item.getLocation();
        if (location == null || location.isEmpty()) {
            Log.w(TAG, "忽略没有位置信息的美食: " + item.getTitle());
            return;
        }
        
        // 使用GeocodingHelper获取真实地理坐标
        GeoPoint point = GeocodingHelper.getGeoPoint(location);
        
        // 如果找不到匹配的位置，使用模拟坐标（作为备用）
        if (point == null) {
            Log.w(TAG, "无法找到位置，使用模拟坐标: " + location);
            point = simulateGeoPoint(location);
        }
        
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(item.getTitle());
        marker.setSnippet("评分: " + item.getRating() + " 价格: " + item.getPrice());
        
        // 设置自定义信息窗口
        marker.setInfoWindow(new FoodItemInfoWindow(mapView));
        
        // 设置标记图标
        Drawable icon = ContextCompat.getDrawable(requireContext(), R.drawable.circular_food_marker);
        if (icon != null) {
            marker.setIcon(icon);
        }
        
        // 关联自定义数据
        marker.setRelatedObject(item);
        
        // 添加到地图和列表
        mapView.getOverlays().add(marker);
        markers.add(marker);
        
        // 通知地图需要重绘
        mapView.invalidate();
    }
    
    // 模拟地理编码，根据地点名生成假坐标点
    // 在实际应用中应替换为真实的地理编码服务
    private GeoPoint simulateGeoPoint(String location) {
        // 基于中国中心点随机生成坐标
        double baseLat = Config.defaultGeoPoint.getLatitude();
        double baseLng = Config.defaultGeoPoint.getLongitude();
        
        // 根据字符串生成相对固定的偏移，避免完全随机
        double latOffset = (location.hashCode() % 1000) / 10000.0;
        double lngOffset = (location.hashCode() % 500) / 5000.0;
        
        return new GeoPoint(baseLat + latOffset, baseLng + lngOffset);
    }
    
    // 缩放地图以显示所有标记
    private void zoomToFitAllMarkers() {
        if (markers.isEmpty()) {
            return;
        }
        
        try {
            // 计算包含所有标记的边界
            double north = -90;  // 最北（最大纬度）
            double south = 90;   // 最南（最小纬度）
            double east = -180;  // 最东（最大经度）
            double west = 180;   // 最西（最小经度）
            
            // 遍历所有标记，找出最大/最小的纬度和经度
            for (Marker marker : markers) {
                GeoPoint position = marker.getPosition();
                double lat = position.getLatitude();
                double lon = position.getLongitude();
                
                north = Math.max(north, lat);
                south = Math.min(south, lat);
                east = Math.max(east, lon);
                west = Math.min(west, lon);
            }
            
            // 创建包含所有点的边界框
            BoundingBox boundingBox = new BoundingBox(north, east, south, west);
            
            // 添加一些边距并设置地图视图
            mapView.zoomToBoundingBox(boundingBox, true, 100);
        } catch (Exception e) {
            Log.e(TAG, "无法调整地图视图: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean onScroll(ScrollEvent event) {
        return false;
    }

    @Override
    public boolean onZoom(ZoomEvent event) {
        return false;
    }

    @Override
    protected String getToolbarTitle() {
        return "FoodMap";
    }
    
    // 生命周期方法
    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }
    
    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
} 