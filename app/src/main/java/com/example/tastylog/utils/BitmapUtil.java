package com.example.tastylog.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 图片处理工具类
 * 用于压缩图片和转换图片格式
 */
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";
    
    // 默认最大宽度和高度
    private static final int DEFAULT_MAX_WIDTH = 1024;
    private static final int DEFAULT_MAX_HEIGHT = 1024;
    
    // 默认压缩质量
    private static final int DEFAULT_QUALITY = 80;
    
    /**
     * 从Uri获取压缩后的Bitmap
     * 
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 压缩后的Bitmap
     */
    public static Bitmap getCompressedBitmap(Context context, Uri imageUri) {
        return getCompressedBitmap(context, imageUri, DEFAULT_MAX_WIDTH, DEFAULT_MAX_HEIGHT);
    }
    
    /**
     * 从Uri获取压缩后的Bitmap，可指定最大宽高
     * 
     * @param context 上下文
     * @param imageUri 图片Uri
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 压缩后的Bitmap
     */
    public static Bitmap getCompressedBitmap(Context context, Uri imageUri, int maxWidth, int maxHeight) {
        try {
            // 获取图片的输入流
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            
            // 获取图片的原始尺寸
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            // 计算采样率
            int sampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            
            // 使用采样率重新加载图片
            options.inJustDecodeBounds = false;
            options.inSampleSize = sampleSize;
            
            inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
            
            // 处理图片旋转
            bitmap = rotateImageIfRequired(context, bitmap, imageUri);
            
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "压缩图片失败: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 计算图片的采样率
     * 
     * @param options BitmapFactory.Options，包含原始图片的宽高
     * @param reqWidth 目标宽度
     * @param reqHeight 目标高度
     * @return 采样率
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 原始图片的宽高
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            // 计算最大的采样率，保证采样后的图片宽高都不小于目标宽高
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    /**
     * 根据EXIF信息旋转图片
     * 
     * @param context 上下文
     * @param bitmap 原始Bitmap
     * @param imageUri 图片Uri
     * @return 旋转后的Bitmap
     */
    private static Bitmap rotateImageIfRequired(Context context, Bitmap bitmap, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            ExifInterface ei = new ExifInterface(inputStream);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return rotateImage(bitmap, 90);
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return rotateImage(bitmap, 180);
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return rotateImage(bitmap, 270);
                default:
                    return bitmap;
            }
        } catch (IOException e) {
            Log.e(TAG, "读取EXIF信息失败: " + e.getMessage(), e);
            return bitmap;
        }
    }
    
    /**
     * 旋转图片
     * 
     * @param bitmap 原始Bitmap
     * @param angle 旋转角度
     * @return 旋转后的Bitmap
     */
    private static Bitmap rotateImage(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
    
    /**
     * 将Bitmap转换为字节数组
     * 
     * @param bitmap Bitmap对象
     * @return 字节数组
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        return bitmapToByteArray(bitmap, DEFAULT_QUALITY);
    }
    
    /**
     * 将Bitmap转换为字节数组，可指定压缩质量
     * 
     * @param bitmap Bitmap对象
     * @param quality 压缩质量，0-100
     * @return 字节数组
     */
    public static byte[] bitmapToByteArray(Bitmap bitmap, int quality) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        return outputStream.toByteArray();
    }
    
    /**
     * 从Uri获取压缩后的字节数组，用于上传
     * 
     * @param context 上下文
     * @param imageUri 图片Uri
     * @return 压缩后的字节数组
     */
    public static byte[] getCompressedImageBytes(Context context, Uri imageUri) {
        Bitmap bitmap = getCompressedBitmap(context, imageUri);
        if (bitmap != null) {
            byte[] bytes = bitmapToByteArray(bitmap);
            bitmap.recycle(); // 释放Bitmap内存
            return bytes;
        }
        return null;
    }
    
    /**
     * 从Uri获取压缩后的字节数组，可指定最大宽高和压缩质量
     * 
     * @param context 上下文
     * @param imageUri 图片Uri
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @param quality 压缩质量，0-100
     * @return 压缩后的字节数组
     */
    public static byte[] getCompressedImageBytes(Context context, Uri imageUri, int maxWidth, int maxHeight, int quality) {
        Bitmap bitmap = getCompressedBitmap(context, imageUri, maxWidth, maxHeight);
        if (bitmap != null) {
            byte[] bytes = bitmapToByteArray(bitmap, quality);
            bitmap.recycle(); // 释放Bitmap内存
            return bytes;
        }
        return null;
    }
} 