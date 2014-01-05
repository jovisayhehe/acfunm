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

import java.util.List;

import tv.acfun.video.api.API;
import tv.acfun.video.entity.Category;
import tv.acfun.video.util.BitmapCache;
import tv.acfun.video.util.net.Connectivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
    public void onCreate() {
        super.onCreate();
        sContext = sInstance = this;
        Connectivity.getGloableQueue(this);
    }
    public static AcApp instance() {
        return sInstance;
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
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://pan.baidu.com/s/1pFLDT")));
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
}
