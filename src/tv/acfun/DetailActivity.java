package tv.acfun;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import tv.acfun.util.Parser;
import tv.acfun.util.Util;

import acfun.domain.AcfunContent;
import android.app.Activity;
import android.content.Intent;
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
	private AcfunContent content;
	private TextView up_txt;
	private TextView time_txt;
	private TextView hit_txt;
	private TextView info_txt;
	private TextView fov_txt;
	private String vid;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_layout);
		
		ArrayList<String> infos = getIntent().getStringArrayListExtra("info");
		
		title = (TextView) findViewById(R.id.detail_title_text);
		up_txt = (TextView) findViewById(R.id.detail_up);
		time_txt = (TextView) findViewById(R.id.detail_time);
		hit_txt = (TextView) findViewById(R.id.detail_hit);
		info_txt = (TextView) findViewById(R.id.detail_info);
		fov_txt = (TextView) findViewById(R.id.detail_fov);
		
		return_btn = (Button) findViewById(R.id.detail_return_btn);
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
		
		fromtxt = infos.get(1);
		vid = infos.get(0);
		InitViewData(vid);
		
	}
	
	
	
	public void InitViewData(final String id) {
		new Thread(){
			public void run(){		
				try {
					
					content = Parser.getContent(id);
					
					runOnUiThread(new Runnable() {
						public void run() {
							
							title.setText(content.getArctitle());
							up_txt.setText("投稿:"+content.getUsername());
							Date date = new Date(Long.parseLong(content.getPubdate()));
							 DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,DateFormat.MEDIUM,Locale.CHINA);
							  String dt = df.format(date);
							time_txt.setText("时间:"+dt);
							hit_txt.setText("点击:"+content.getClick());
							info_txt.setText(content.getDescription());
							fov_txt.setText("收藏:"+content.getStow());
							
						} 
					});	
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					runOnUiThread(new Runnable() {
						public void run() {
							
						} 
					});	
					e.printStackTrace();
				}
			}	
		}.start();
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
				String shareurl = "http://www.acfun.tv/v/ac"+vid;
				Intent intent=new Intent(Intent.ACTION_SEND);  
				intent.setType("text/plain");  
				intent.putExtra(Intent.EXTRA_SUBJECT, "分享");  
				intent.putExtra(Intent.EXTRA_TEXT, shareurl);  
				startActivity(Intent.createChooser(intent, getTitle()));  
				
				break;
			default:
				break;
			}
		}
		
	}

}
