package com.example.tastylog.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.tastylog.R;
import com.example.tastylog.model.FoodItem;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class FoodItemInfoWindow extends InfoWindow {
    
    public FoodItemInfoWindow(MapView mapView) {
        super(R.layout.custom_info_window, mapView);
    }
    
    @Override
    public void onOpen(Object item) {
        closeAllInfoWindowsOn(mMapView);
        
        if (item instanceof Marker) {
            Marker marker = (Marker) item;
            Object relatedObject = marker.getRelatedObject();
            
            if (relatedObject instanceof FoodItem) {
                FoodItem foodItem = (FoodItem) relatedObject;
                
                TextView titleTxt = mView.findViewById(R.id.tv_title);
                TextView snippetTxt = mView.findViewById(R.id.tv_snippet);
                
                if (titleTxt != null) {
                    titleTxt.setText(foodItem.getTitle());
                }
                
                if (snippetTxt != null) {
                    StringBuilder details = new StringBuilder();
                    details.append("评分: ").append(foodItem.getRating());
                    
                    if (foodItem.getPrice() != null && !foodItem.getPrice().isEmpty()) {
                        details.append(" | 价格: ").append(foodItem.getPrice());
                    }
                    
                    if (foodItem.getLocation() != null && !foodItem.getLocation().isEmpty()) {
                        details.append("\n位置: ").append(foodItem.getLocation());
                    }
                    
                    snippetTxt.setText(details.toString());
                }
            }
        }
        
        mView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onClose() {
        // 当信息窗口关闭时调用
        mView.setVisibility(View.GONE);
    }
} 