package tv.acfun;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

public class DetailActivity extends Activity {
	
	private Button return_btn;
	private Button fov_btn;
	private Button fov_btn_v;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_layout);
		return_btn = (Button) findViewById(R.id.detail_return_btn);
		fov_btn = (Button) findViewById(R.id.detail_fov_btn);
		fov_btn_v = (Button) findViewById(R.id.detail_fov_btn_v);
		ButtonListener listener = new ButtonListener();
		return_btn.setOnClickListener(listener);
		fov_btn.setOnClickListener(listener);
	}
	
	
	private void startAnm(){
		Animation am = new TranslateAnimation ( 0, 380, 0, 800);
		am.setDetachWallpaper(true);
	    am.setDuration(400);
	    am.setRepeatCount(0);
	    fov_btn.setAnimation(am);
	    am.start();
	    am.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				fov_btn_v.setVisibility(View.VISIBLE);
				fov_btn.setVisibility(View.GONE);
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				fov_btn_v.setEnabled(false);
			}
		});
	}
	
	
	private final class ButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.detail_return_btn:
				((MainActivity)DetailActivity.this.getParent()).addActivity("channel", ChannelActivity.class);
				
				break;
			case R.id.detail_fov_btn:
				startAnm();
				break;
			default:
				break;
			}
		}
		
	}

}
