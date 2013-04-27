package tv.avfun.app;


import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.widget.Toast;

/**
 * 自定义Application
 * @author Yrom
 */
public class AcApp extends Application {

    private static Context   mContext;
    private static Resources mResources;
    private static String    mSDcardDir, mExternalFilesDir;
    private static AcApp     instance;
    private static SharedPreferences sp;
    public static final String LOG = "/Logs"; 
    public static final String IMAGE = "/Imgs";
    public static final String VIDEO = "/Videos";
    /**
     * <b>NOTE:</b>在 <code>getApplicationContext()</code> 调用一次之后才能用这个方便的方法
     */
    public static AcApp instance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        Thread.currentThread().setUncaughtExceptionHandler(CrashExceptionHandler.instance());
        mContext = instance = this;
        mResources = getResources();
        sp = PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    
    @Override
    public void onLowMemory() {
        System.gc();
        super.onLowMemory();
    }
    
    public String getVersionName(){
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName;
        }catch (Exception e) {}
        return "";
    }
    
    // ====================================
    // config SharedPreferences
    // ====================================
    
    public static SharedPreferences getConfig(){
        return sp;
    }
    public static void putString(String key, String value){
        sp.edit().putString(key, value).commit();
    }
    public static void putBoolean(String key, boolean value){
        sp.edit().putBoolean(key, value).commit();
    }
    public static void putInt(String key, int value){
        sp.edit().putInt(key, value).commit();
    }
    public static void putFloat(String key, float value){
        sp.edit().putFloat(key, value).commit();
    }
    /**
     * 获得视频文件下载路径，默认为/sdcard/Download/AcFun/videos/{aid}/{vid}
     * @return
     */
    public static File getDownloadPath(String aid, String vid){
        File path = new File(sp.getString("download_path", getSDcardDir()+"/Download/AcFun"+VIDEO+"/"+aid+"/"+vid));
        path.mkdirs();
        return path;
    }
    /**
     * 获得首页展示模式
     * @return 1.最新发布 2.热门 3.最新回复
     */
    public static String getHomeDisplayMode(){
        return sp.getString("home_display_mode", "1");
    }
    
    // ====================================
    // statics
    // ====================================
    
    public static Context context() {
        return mContext;
    }

    public static Resources getR() {
        return mResources;
    }

    /**
     * 外部存储是否可用
     */
    public static boolean isExternalStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 获得外部存储的files目录 <br/>
     * <b>NOTE:</b>请先调用 {@link #isExternalStorageAvailable()} 判断是否可用
     * 
     * @return
     */
    public static String getExternalFilesDir() {
        if (mExternalFilesDir == null)
            mExternalFilesDir = mContext.getExternalFilesDir(null).getAbsolutePath();
        return mExternalFilesDir;
    }
    
    /**
     * 获得缓存目录 <br>
     * <b>NOTE:</b>请先调用 {@link #isExternalStorageAvailable()} 判断是否可用
     * @param type {@link #IMAGE} {@link #VIDEO} and so on. 
     * @return
     */
    public static File getExternalCacheDir(String type){
        File cacheDir = new File(mContext.getExternalCacheDir(),type);
        cacheDir.mkdirs();
        return cacheDir;
    }
    /**
     * 获得SDcard根目录 <br>
     * <b>NOTE:</b>请先调用 {@link #isExternalStorageAvailable()} 判断是否可用
     * 
     * @return SDcard Dir
     */
    public static String getSDcardDir() {
        if (mSDcardDir == null)
            mSDcardDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        return mSDcardDir;
    }
    /**
     * log存放路径
     * @return
     */
    public static String getLogsDir(){
        File folder = null;
        if(isExternalStorageAvailable())
            folder = new File(getExternalFilesDir(),LOG);
        else
            folder =  new File(mContext.getFilesDir(),LOG);
        folder.mkdirs();
        return folder.getAbsolutePath();
    }
    
    /**
     * 获取当前默认的日期时间显示
     * eg. 20130411-110445
     * @return
     */
    public static String getCurDateTime(){
        return getCurDateTime("yyyyMMdd-kkmmss");
    }
    /**
     * 获取当前日期时间
     * @param format {@link android.text.format.DateFormat}
     * @return
     */
    public static String getCurDateTime(CharSequence format){
        return DateFormat.format(format,System.currentTimeMillis()).toString();
    }
    
    public static void showToast(String msg){
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }
}
