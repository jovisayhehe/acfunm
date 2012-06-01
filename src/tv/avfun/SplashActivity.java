package tv.avfun;

import tv.acfun.util.Util;
import tv.avfun.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;

public class SplashActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreent(this);
		setContentView(R.layout.splash_layout);
    	LinearLayout startan = (LinearLayout) findViewById(R.id.splash_start_an);
    	int acfaces[] = {R.drawable.a,R.drawable.b,R.drawable.c,R.drawable.d,R.drawable.e,R.drawable.f};
    	int n = (int)(Math.random()*5);
    	startan.setBackgroundResource(acfaces[n]);
    	
    	
    	 new Handler().postDelayed(new Runnable(){   
    		    
             @Override   
             public void run() {   
                 SplashActivity.this.finish();
             }   
                  
            }, 3);   
	}
}
