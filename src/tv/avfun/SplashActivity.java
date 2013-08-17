
package tv.avfun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import tv.ac.fun.R;
import tv.avfun.app.AcApp;
import tv.avfun.util.DataStore;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;

/**
 * splash.进行内容初始化操作
 * 
 * @author Yrom
 * 
 */
public class SplashActivity extends Activity {
    private boolean flag = false;
    private static String appDb      = "/acfun.db";
    private static String downloadDb = "/download.db";
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
    private String dbDir;
    private String backup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File filesDir = getFilesDir();
        String dataDir = filesDir.getParent();
        dbDir = dataDir + "/databases";
        backup = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Acfun";
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
        File bak = new File(backup+appDb);
        if(bak.exists()){
            new Thread(){
                public void run() {
                    copy(backup+appDb,dbDir+appDb);
                    copy(backup+downloadDb,dbDir+downloadDb);
                    new File(backup+appDb).delete();
                    new File(backup+downloadDb).delete();
                }
            }.start();
            
        }
    }


    private void clearCache() {
        
        new File(getCacheDir(), DataStore.CHANNEL_LIST_CACHE).delete();
        new File(getCacheDir(), DataStore.TIME_LIST_CACHE_0).delete();
        new File(getCacheDir(), DataStore.TIME_LIST_CACHE_1).delete();
    }
    private void copy(String source, String dest) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(source);
            out = new FileOutputStream(dest);
            int len = -1;
            byte[] buffer = new byte[8096];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException e) {}
            try {
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
