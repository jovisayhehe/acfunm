package tv.avfun;

import java.io.File;

import com.umeng.fb.NotificationType;
import com.umeng.fb.UMFeedbackService;

import tv.avfun.app.AcApp;
import tv.avfun.util.DataStore;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * splash.进行内容初始化操作
 * @author Yrom
 *
 */
public class SplashActivity extends Activity{
    private Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            // 默认消息为开启Main页面
            if(msg.what==0){
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.slide_out);
                finish();
            }
            super.handleMessage(msg);
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_splash);
        // TODO　init data here
        Message msg = Message.obtain();
        msg.what = 0;
        mHandler.sendMessageDelayed(msg, 1200L);
        //版本号更改后，清除原有缓存
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            int oldVersion = AcApp.getConfig().getInt("versionCode", 0);
            
            if(oldVersion != info.versionCode){
                clearCache();
                AcApp.putInt("versionCode", info.versionCode);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void clearCache() {
        new File(getCacheDir(),DataStore.CHANNEL_LIST_CACHE).delete();
        new File(getCacheDir(),DataStore.TIME_LIST_CACHE).delete();
    }
    
}
