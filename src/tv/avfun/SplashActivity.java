
package tv.avfun;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jsoup.nodes.Document;

import tv.ac.fun.R;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Bangumi;
import tv.avfun.api.net.Connectivity;
import tv.avfun.api.net.UserAgent;
import tv.avfun.app.AcApp;
import tv.avfun.util.DataStore;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;

/**
 * splash.进行内容初始化操作
 * 
 * @author Yrom
 * 
 */
public class SplashActivity extends Activity {
    private boolean flag = false;
    private Handler mHandler = new Handler() {

         public void handleMessage(android.os.Message msg) {
             // 默认消息为开启Main页面
             if (msg.what == 0 && !flag) {
                 Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                 startActivity(intent);
                 overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
                 flag = true;
                 finish();
             }
         }
     };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mHandler.sendEmptyMessageDelayed(0, 1200L);
        // 版本号更改后，清除原有缓存
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            int oldVersion = AcApp.getConfig().getInt("versionCode", 0);
            if (oldVersion != info.versionCode) {
                clearCache();
                AcApp.putInt("versionCode", info.versionCode);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if(!DataStore.getInstance().isBangumiListCached())
            init();
    }

    private void init() {
        new Thread() {

            @Override
            public void run() {
                try {
                    
                    Document doc = Connectivity.getDoc("http://www.acfun.tv", UserAgent.MY_UA);
                    List<Bangumi[]> timeList = ApiParser.getBangumiTimeList(doc);
                    DataStore.getInstance().saveTimeList(timeList);
                    DataStore.writeToFile(new File(getCacheDir(), DataStore.HOME_CACHE).getAbsolutePath(), doc.data());
                    doc.empty();
                    doc = null;
                    mHandler.sendEmptyMessage(0);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }.start();
        
    }

    private void clearCache() {
        
        new File(getCacheDir(), DataStore.CHANNEL_LIST_CACHE).delete();
        new File(getCacheDir(), DataStore.TIME_LIST_CACHE).delete();
    }

}
