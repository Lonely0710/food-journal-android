package com.example.tastylog.config;

import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;

public class Config {
    // 天地图API密钥
    public static final String MAP_KEY = "07c7110102c106941a10b4696cf0f415";
    
    // 默认GeoPoint (中国中心位置)
    public static final GeoPoint defaultGeoPoint = new GeoPoint(35.86166, 104.195397);
    
    // 从MapTileIndex中提取X坐标 (OSMdroid内部使用)
    private static int getX(final long pMapTileIndex) {
        return (int) (pMapTileIndex & 0x00000000FFFFFFFFL);
    }
    
    // 从MapTileIndex中提取Y坐标 (OSMdroid内部使用)
    private static int getY(final long pMapTileIndex) {
        return (int) ((pMapTileIndex >> 32) & 0x00000000FFFFFFFFL);
    }
    
    // 从MapTileIndex中提取缩放级别 (OSMdroid内部使用)
    private static int getZoom(final long pMapTileIndex) {
        return (int) ((pMapTileIndex >> 64) & 0xFFL);
    }
    
    /**
     * 天地图 矢量图层（含注记）
     */
    public static OnlineTileSourceBase TDTCIA_W = new XYTileSource(
        "Tian Di Tu CIA",
        0, 20, 256, ".png", 
        new String[]{
            "https://t0.tianditu.gov.cn/cva_w/wmts"
        }
    ) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            int zoom = (int)(Math.log(getMapSize(pMapTileIndex)) / Math.log(2)) - 1;
            int x = getX(pMapTileIndex);
            int y = getY(pMapTileIndex);
            
            return getBaseUrl() 
                + "?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0"
                + "&LAYER=cva&STYLE=default&TILEMATRIXSET=w"
                + "&FORMAT=tiles&TILEMATRIX=" + zoom
                + "&TILEROW=" + y
                + "&TILECOL=" + x
                + "&tk=" + MAP_KEY;
        }
    };
    
    /**
     * 天地图 矢量底图
     */
    public static OnlineTileSourceBase TDTVEC_W = new XYTileSource(
        "Tian Di Tu VEC",
        0, 20, 256, ".png",
        new String[]{
            "https://t0.tianditu.gov.cn/vec_w/wmts"
        }
    ) {
        @Override
        public String getTileURLString(long pMapTileIndex) {
            int zoom = (int)(Math.log(getMapSize(pMapTileIndex)) / Math.log(2)) - 1;
            int x = getX(pMapTileIndex);
            int y = getY(pMapTileIndex);
            
            return getBaseUrl() 
                + "?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0"
                + "&LAYER=vec&STYLE=default&TILEMATRIXSET=w"
                + "&FORMAT=tiles&TILEMATRIX=" + zoom
                + "&TILEROW=" + y
                + "&TILECOL=" + x
                + "&tk=" + MAP_KEY;
        }
    };

    // 获取地图大小的辅助方法
    private static int getMapSize(long pMapTileIndex) {
        return 1 << (getZoom(pMapTileIndex) + 1);
    }
} 