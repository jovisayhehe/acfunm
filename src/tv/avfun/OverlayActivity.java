package tv.avfun;


import tv.ac.fun.R;
import tv.avfun.app.AcApp;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

/** 操作提示，全屏，半透明背景 */
public class OverlayActivity extends Activity {
    OnClickListener l = new View.OnClickListener() {
        
        @Override
        public void onClick(View v) {
            AcApp.putBoolean("first_run", false);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
            
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.overlays);
        findViewById(R.id.close).setOnClickListener(l);
        findViewById(R.id.ok).setOnClickListener(l);
    }
}
