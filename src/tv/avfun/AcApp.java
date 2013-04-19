package tv.avfun;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Environment;

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
    /**
     * <b>NOTE:</b>在 <code>getApplicationContext()</code> 调用一次之后才能用这个方便的方法
     */
    public static AcApp instance() {
        return instance;
    }

    public void onCreate() {
        super.onCreate();
        mContext = instance = this;
        mResources = getResources();
        sp = getSharedPreferences("config", MODE_PRIVATE);
    }
    
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
    
    @Override
    public void onLowMemory() {
        System.gc();
        super.onLowMemory();
    }
    
    
    

}
