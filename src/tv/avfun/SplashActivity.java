package tv.avfun;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
        mHandler.sendMessageDelayed(msg, 2000L);
    }
    
    public boolean isNetworkAvailable() {
        NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return (info != null) && (info.isConnected());
    }
}
