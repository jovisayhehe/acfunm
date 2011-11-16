package tv.acfun;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONObject;

import tv.acfun.util.Parser;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

public class DetailActivity extends Activity {
	
	private Button return_btn;
	private TextView title;
	private TextView fov_btn;
	private TextView down_btn;
	private TextView reply_btn;
	private TextView share_btn;
	private String fromtxt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_layout);
		
		ArrayList<String> infos = getIntent().getStringArrayListExtra("info");
		
		return_btn = (Button) findViewById(R.id.detail_return_btn);
		title = (TextView) findViewById(R.id.detail_title_text);
		fov_btn = (TextView) findViewById(R.id.detail_fov_btn);
		down_btn = (TextView) findViewById(R.id.detail_downloads_btn);
		reply_btn = (TextView) findViewById(R.id.detail_reply_btn);
		share_btn = (TextView) findViewById(R.id.detail_share_btn);
		ButtonListener listener = new ButtonListener();
		return_btn.setOnClickListener(listener);
		fov_btn.setOnClickListener(listener);
		down_btn.setOnClickListener(listener);
		reply_btn.setOnClickListener(listener);
		share_btn.setOnClickListener(listener);
		
		title.setText(infos.get(0));
		fromtxt = infos.get(1);
		try {
			Parser.ParserYoukuvideo("XMzIyMjY0Njk2");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void startAnm(View v){
		Animation am = new TranslateAnimation ( 0, 380, 0, 800);
		am.setDetachWallpaper(true);
	    am.setDuration(400);
	    am.setRepeatCount(0);
	    v.setAnimation(am);
	    am.start();
	    am.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
			}
		});
	}
	
	
	private final class ButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.detail_return_btn:
				if(fromtxt.endsWith("channel")){
					((MainActivity)DetailActivity.this.getParent()).addActivity(fromtxt, ChannelActivity.class,null);
				}else if(fromtxt.endsWith("home")){
					((MainActivity)DetailActivity.this.getParent()).addActivity(fromtxt, HomeActivity.class,null);
				}
				
				break;
			case R.id.detail_fov_btn:
				fov_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.detail_item_favorites_l, 0, 0, 0);
				fov_btn.setEnabled(false);
				break;
			case R.id.detail_downloads_btn:
				
				break;
			case R.id.detail_reply_btn:
				
				break;
			case R.id.detail_share_btn:
				
				break;
			default:
				break;
			}
		}
		
	}

}
