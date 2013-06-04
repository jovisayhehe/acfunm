package tv.avfun.app;


import java.io.File;

import tv.avfun.DownloadManActivity;
import tv.ac.fun.R;
import tv.avfun.util.ExtendedImageDownloader;
import tv.avfun.util.download.DownloadManager;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LRULimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Activity;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
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
    public static final String LOG = "Logs"; 
    public static final String IMAGE = "images";
    public static final String VIDEO = "Videos";
    
    private DownloadManager mDownloadManager;
    private static NotificationManager mNotiManager;
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
        mDownloadManager = new DownloadManager(this);
        mDownloadManager.getProvider().loadOldDownloads();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
            .showImageForEmptyUri(R.drawable.no_picture)
            .resetViewBeforeLoading()
            .cacheOnDisc()
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .displayer(new FadeInBitmapDisplayer(300))
            .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
            .threadPriority(Thread.NORM_PRIORITY - 2)
            .memoryCache(new LRULimitedMemoryCache((3 * 1024 * 1024)))
            .denyCacheImageMultipleSizesInMemory()
            //.discCache(new UnlimitedDiscCache(new File(StorageUtils.getCacheDirectory(mContext),"Imgs")))
            .discCacheFileCount(200)
            .discCacheSize(50 * 1024 * 1024)
            .imageDownloader(new ExtendedImageDownloader(getApplicationContext()))
            .tasksProcessingOrder(QueueProcessingType.LIFO)
            .enableLogging() // Not necessary in common
            .defaultDisplayImageOptions(options)
            .build();
        
        ImageLoader.getInstance().init(config);
    }
    
    @Override
    public void onLowMemory() {
        System.gc();
        super.onLowMemory();
    }
    
    public DownloadManager getDownloadManager() {
        return mDownloadManager;
    }
    private String versionName = "";

    public String getVersionName() {
        if (TextUtils.isEmpty(versionName)) {
            PackageInfo info = null;
            try {
                info = getPackageManager().getPackageInfo(getPackageName(), 0);
                versionName = info.versionName;
                return versionName;
            } catch (Exception e) {}
            return "";
        } else
            return versionName;
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
        File path = new File(sp.getString("download_path", getSDcardDir()+"/Download/AcFun/"+VIDEO+"/"+aid+"/"+vid));
        //path.mkdirs();
        return path;
    }
    /**
     * 获得首页展示模式
     * @return 1.最新发布 2.热门 3.最新回复
     */
    public static String getHomeDisplayMode(){
        return sp.getString("home_display_mode", "1");
    }
    /**
     * 0为标清 1高清优先 2 超清优先
     * @return
     */
    public static int getParseMode(){
        int parseMode = Integer.parseInt(sp.getString("parse_mode", "0"));
        return parseMode;
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
    
    public static void addSearchView(Activity activity, com.actionbarsherlock.view.Menu menu) {
        SearchView searchView = new SearchView(activity);
        SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo info = searchManager.getSearchableInfo(activity.getComponentName());
        searchView.setSearchableInfo(info);
        searchView.setQueryHint("搜索...");
        menu.add("Search").setIcon(R.drawable.action_search).setActionView(searchView)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
        View v = searchView.findViewById(R.id.abs__search_plate);
        v.setBackgroundResource(R.drawable.edit_text_holo_light);
    }
    
    public static void showNotification(Intent mIntent, int notificationId, String text,int icon, CharSequence title){
        showNotification(mIntent, notificationId, text, icon, title, Notification.FLAG_AUTO_CANCEL);
    }
    @SuppressWarnings("deprecation")
    public static void showNotification(Intent mIntent, int notificationId, String text,int icon, CharSequence title,int flag){
        Notification notification = new Notification(icon,text,System.currentTimeMillis());
        mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(mContext, title, text, contentIntent);
        notification.flags |= flag;
        if(mNotiManager == null)
            mNotiManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
        mNotiManager.notify(notificationId, notification);
    }
    
}
