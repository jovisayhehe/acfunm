/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video;

import java.io.File;
import java.util.List;

import tv.ac.fun.R;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Category;
import tv.acfun.video.util.BitmapCache;
import tv.acfun.video.util.net.Connectivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.toolbox.ImageLoader;

/**
 * @author Yrom
 *
 */
public class AcApp extends Application {
    private static AcApp sInstance;
    private static Context sContext;
    private static List<Category> sCategories;
    private static SharedPreferences sSharedPreferences;
    public void onCreate() {
        super.onCreate();
        sContext = sInstance = this;
        Connectivity.getGloableQueue(this);
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(sContext);
    }
    public static AcApp instance() {
        return sInstance;
    }
    // ====================================
    // config SharedPreferences
    // ====================================

    public static SharedPreferences getConfig() {
        return sSharedPreferences;
    }

    public static void putString(String key, String value) {
        sSharedPreferences.edit().putString(key, value).commit();
    }
    
    public static String getString(String key, String defValue){
        return sSharedPreferences.getString(key, defValue);
    }
    
    public static void putBoolean(String key, boolean value) {
        sSharedPreferences.edit().putBoolean(key, value).commit();
    }

    public static boolean getBoolean(String key, boolean defValue){
        return sSharedPreferences.getBoolean(key, defValue);
    }
    
    public static int getInt(String key, int defValue){
        return sSharedPreferences.getInt(key, defValue);
    }
    
    public static void putInt(String key, int value) {
        sSharedPreferences.edit().putInt(key, value).commit();
    }
    
    public static float getFloat(String key, float defValue){
        return sSharedPreferences.getFloat(key, defValue);
    }
    
    public static void putFloat(String key, float value) {
        sSharedPreferences.edit().putFloat(key, value).commit();
    }
    
    public static void addRequest(Request<?> request){
        Connectivity.addRequest(request);
    }
    
    public static void cancelAllRequest(RequestFilter filter){
        Connectivity.cancelAllRequest(filter);
    }
    
    public static void cancelAllRequest(Object tag){
        Connectivity.cancelAllRequest(tag);
    }
    
    public static ImageLoader getGloableLoader(){
        return Connectivity.getGloableLoader(sContext);
    }
    
    public static byte[] getDataInDiskCache(String key){
        return Connectivity.getDataInDiskCache(sContext, key);
    }
    
    public static List<Category> getSubCats(int channelId){
        final List<Category> categories = getCategories();
        if(categories == null) return null;
        for(Category cat : categories){
            if(cat.id == channelId)
                return cat.subclasse;
        }
        return null;
    }
    public static void setCategories(List<Category> categories){
        if (sCategories == null || sCategories.isEmpty()) {
            sCategories = categories;
        }else{
            sCategories.clear();
            sCategories.addAll(categories);
        }
    }
    public static List<Category> getCategories(){
        if(sCategories == null){
            byte[] data = getDataInDiskCache(API.CHANNEL_CATS);
            if(data == null || data.length <=0)
                return null;
            
            sCategories = JSON.parseArray(new String(data), Category.class);
        }
        return sCategories;
    }
    
    public static final long _1_min = 60 * 1000;
    public static final long _1_hour = 60 * _1_min;
    public static final long _24_hour = 24 * _1_hour;

    public static String getPubDate(long postTime) {
        long delta = System.currentTimeMillis() - postTime;
        if( delta <  _24_hour && delta >= _1_hour){
            int time = (int) (delta / _1_hour);
            return time+"小时前 ";
        } else if( delta < _1_hour && delta >= _1_min){
            int time = (int) (delta / _1_min);
            return time+"分钟前 ";
        } else if( delta < _1_min){
            return "刚刚 ";
        } else {
            int time = (int) (delta / _24_hour);
            if(time <= 6){
                return time+"天前 " ;
            }else{
                return getDateTime(postTime);
            }
        }
    }
    
    public static String getCurDateTime() {
        return getDateTime("yyyyMMdd-kkmmss", System.currentTimeMillis());
    }
    
    public static String getDateTime(long msec){
        return getDateTime("yyyy年MM月dd日 kk:mm", msec);
    }
    
    /**
     * 获取当前日期时间
     * 
     * @param format
     *            {@link android.text.format.DateFormat}
     * @return
     */
    public static String getDateTime(CharSequence format, long msec) {
        return DateFormat.format(format, msec).toString();
    }
    
    public static void startArea63(final Activity context, String className, Intent intent){
        try{
            ComponentName cmp = new ComponentName("tv.acfun.a63", className);
            if (context.getPackageManager().getActivityInfo(cmp, 0) != null) {
                intent.setComponent(cmp);
                context.startActivity(intent);
            }
        }catch(Exception e){
            e.printStackTrace();
            OnClickListener onClick = new OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == DialogInterface.BUTTON_POSITIVE){
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.url_area63))));
                    }
                    dialog.dismiss();
                }
            };
            new AlertDialog.Builder(context)
                .setTitle("没有找到文章区客户端")
                .setMessage("是否前往下载安装？")
                .setPositiveButton("好", onClick)
                .setNegativeButton("取消", onClick)
                .show();
            
        }
    }
    public static Bitmap getBitmpInCache(String url){
        String key = getCacheKey(url, 0, 0);
        return Connectivity.getBitmap(key);
    }
    public static String getCacheKey(String url, int maxWidth, int maxHeight) {
        return new StringBuilder(url.length() + 12).append("#W").append(maxWidth)
                .append("#H").append(maxHeight).append(url).toString();
    }
    
    public static void putBitmapInCache(String url, Bitmap value){
        String key = getCacheKey(url, 0, 0);
        Connectivity.putBitmap(key, value);
    }
    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }
    /**
     * 获得缓存目录 <br>
     * <b>NOTE:</b>请先调用 {@link #isExternalStorageAvailable()} 判断是否可用
     * 
     * @param type
     *            {@link #IMAGE} {@link #VIDEO} and so on.
     * @return 
     */
    public static File getExternalCacheDir(String type) {
        File cacheDir = new File(sContext.getExternalCacheDir(), type);
        cacheDir.mkdirs();
        return cacheDir;
    }
    private String versionName = "";

    public String getVersionName() {
        if (TextUtils.isEmpty(versionName)) {
            PackageInfo info = null;
            try {
                info = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = info.versionName;
                return versionName;
            } catch (Exception e) {
            }
            return "";
        } else
            return versionName;
    }
    private static NotificationManager sNotiManager;
    
    public static void showNotification(Intent mIntent, int notificationId,
            String text, int icon, CharSequence title) {
        showNotification(mIntent, notificationId, text, icon, title,
                Notification.FLAG_AUTO_CANCEL);
    }
    public static void showNotification(Intent mIntent, int notificationId,
            String text, int icon, CharSequence title, int flag) {
        Notification notification = new Notification(icon, text,
                System.currentTimeMillis());
        mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(sContext, 0,
                mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(sContext, title, text, contentIntent);
        notification.flags |= flag;
        if (sNotiManager == null)
            sNotiManager = (NotificationManager) sContext
                    .getSystemService(NOTIFICATION_SERVICE);
        sNotiManager.notify(notificationId, notification);
    }
}
