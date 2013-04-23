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
 * 
 */
public class AcApp extends Application {

    private static Context   mContext;
    private static Resources mResources;
    private static String    mSDcardDir, mExternalFilesDir;
    private static AcApp     instance;
    private static SharedPreferences sp;
    public static final String LOG = "/logs"; 
    public static final String IMAGE = "/imgs";
    public static final String VIDEO = "/videos";
    public static final String DOWN = "/download";
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
    
    public SharedPreferences getConfig(){
        return sp;
    }
    public void putString(String key, String value){
        sp.edit().putString(key, value).commit();
    }
    public void putBoolean(String key, boolean value){
        sp.edit().putBoolean(key, value).commit();
    }
    public void putInt(String key, int value){
        sp.edit().putInt(key, value).commit();
    }
    public void putFloat(String key, float value){
        sp.edit().putFloat(key, value).commit();
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
