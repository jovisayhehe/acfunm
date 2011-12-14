package tv.acfun;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONObject;
import org.stagex.danmaku.activity.PlayerActivity;

import tv.acfun.db.DBService;
import tv.acfun.util.Parser;
import tv.acfun.util.Util;

import acfun.domain.AcfunContent;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
	private TableRow part_row;
	private TableRow fpart_row;
	private ArrayList<HashMap<String, String>> partlist;
	private ArrayList<String> fpartlist;
	private int typeid;
	private ArrayList<TextView> parts;
	private Animation localAnimation;
	private LinearLayout loadlay;
	private LinearLayout detail_item_line;
	private boolean isgame;
	private int playcode;
	private boolean notfound;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreennt(this);
		setContentView(R.layout.detail_layout);
		
		ArrayList<String> infos = getIntent().getStringArrayListExtra("info");
		parts = new  ArrayList<TextView>();
		title = (TextView) findViewById(R.id.detail_title_text);
		up_txt = (TextView) findViewById(R.id.detail_up);
		time_txt = (TextView) findViewById(R.id.detail_time);
		hit_txt = (TextView) findViewById(R.id.detail_hit);
		info_txt = (TextView) findViewById(R.id.detail_info);
		fov_txt = (TextView) findViewById(R.id.detail_fov);
		loadlay = (LinearLayout) findViewById(R.id.detail_loading);
		part_row = (TableRow) findViewById(R.id.detail_part_row);
		fpart_row = (TableRow) findViewById(R.id.detail_fpart_row);
		localAnimation = AnimationUtils.loadAnimation(this, R.anim.title_press);
		return_btn = (Button) findViewById(R.id.detail_return_btn);
		fov_btn = (TextView) findViewById(R.id.detail_fov_btn);
		down_btn = (TextView) findViewById(R.id.detail_downloads_btn);
		reply_btn = (TextView) findViewById(R.id.detail_reply_btn);
		share_btn = (TextView) findViewById(R.id.detail_share_btn);
		detail_item_line = (LinearLayout) findViewById(R.id.detail_item_line);
		ButtonListener listener = new ButtonListener();
		return_btn.setOnClickListener(listener);
		fov_btn.setOnClickListener(listener);
		down_btn.setOnClickListener(listener);
		reply_btn.setOnClickListener(listener);
		share_btn.setOnClickListener(listener);
		fov_btn.setEnabled(false);
		fromtxt = infos.get(1);
		vid = infos.get(0);
		if(fromtxt!="history"&&!fromtxt.equals("history")){
			new DBService(DetailActivity.this).addtoHis(vid, infos.get(2), String.valueOf(System.currentTimeMillis()));
		}
    	SharedPreferences sharedPreferences = getSharedPreferences(  
                "config", Activity.MODE_PRIVATE);
    	playcode = sharedPreferences.getInt("playcode", 0);
		InitViewData(vid);
		
	}
	

	
	public void InitViewData(final String id) {
		new Thread(){
			public void run(){		
				try {
					
					content = Parser.getContent(id);
					partlist = Parser.ParserAcId(id);
					runOnUiThread(new Runnable() {
						public void run() {
							loadlay.setVisibility(View.GONE);
							typeid =Integer.parseInt(content.getTypeid());
							notfound = partlist.get(0).get("type").equals("")||partlist.get(0).get("id")=="";
							if(notfound&&typeid==0){
								detail_item_line.setVisibility(View.INVISIBLE);
								part_row.setVisibility(View.INVISIBLE);
								fpart_row.setVisibility(View.INVISIBLE);
								up_txt.setVisibility(View.INVISIBLE);
								time_txt.setVisibility(View.INVISIBLE);
								hit_txt.setVisibility(View.INVISIBLE);
								fov_txt.setVisibility(View.INVISIBLE);
								
								info_txt.setTextColor(Color.RED);
								info_txt.setTextSize(25);
								info_txt.setText("你找的视频不纯在....>,<");
								
								
							}else{
								if(new DBService(DetailActivity.this).isFoved(vid)){
									fov_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.detail_item_favorites_l, 0, 0, 0);
									fov_btn.setEnabled(false);
								}else{
									fov_btn.setEnabled(true);
								}
								
								isgame = partlist.get(0).get("type").equals("game")||partlist.get(0).get("type")=="game";
								title.setText(content.getArctitle());
								up_txt.setText("投稿:"+content.getUsername());
								Date date = new Date(Long.parseLong(content.getPubdate()));
								 SimpleDateFormat dateformat1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								  String a1=dateformat1.format(date);
								time_txt.setText("时间:"+a1);
								hit_txt.setText("点击:"+content.getClick());
								info_txt.setText(content.getDescription());
								fov_txt.setText("收藏:"+content.getStow());
								
								if(isgame){
									if(Util.isSmallertfroyo()){
										String playpath = partlist.get(0).get("id");
										TextView tv = new TextView(DetailActivity.this);
										TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, 56);
										params.setMargins(10, 4, 0, 4);
										tv.setLayoutParams(params);
										tv.setBackgroundColor(Color.GRAY);
										tv.setGravity(Gravity.CENTER);
										tv.setTextSize(20);
										tv.setText("版本低于2.2无法使用Flash");
										part_row.addView(tv);
									}else{
										String playpath = partlist.get(0).get("id");
										TextView tv = new TextView(DetailActivity.this);
										TableRow.LayoutParams params = new TableRow.LayoutParams(200, 56);
										params.setMargins(10, 4, 0, 4);
										tv.setLayoutParams(params);
										tv.setBackgroundColor(Color.GRAY);
										tv.setGravity(Gravity.CENTER);
										tv.setTextSize(20);
										tv.setText("PLAY");
										tv.setTag(playpath);
										tv.setOnClickListener(new PartListener());
										part_row.addView(tv);
									}
								}else{
									if(typeid!=13){
										for(int i=0;i<partlist.size();i++){
											TextView tv = new TextView(DetailActivity.this);
											TableRow.LayoutParams params = new TableRow.LayoutParams(56, 56);
											params.setMargins(10, 4, 0, 4);
											tv.setLayoutParams(params);
											tv.setBackgroundResource(R.drawable.detail_part_item_bg);
											tv.setGravity(Gravity.CENTER);
											tv.setTextSize(22);
											tv.setTextColor(Color.BLACK);
											tv.setText(String.valueOf(i+1));
											tv.setTag(partlist.get(i));
											tv.setOnClickListener(new PartListener());
											parts.add(tv);
											part_row.addView(tv);
										}
									}else{
										TextView tv = new TextView(DetailActivity.this);
										TableRow.LayoutParams params = new TableRow.LayoutParams(200, 56);
										params.setMargins(10, 4, 0, 4);
										tv.setLayoutParams(params);
										tv.setBackgroundColor(Color.GRAY);
										tv.setGravity(Gravity.CENTER);
										tv.setTextSize(20);
										tv.setText("查看文章");
										tv.setTag(id);
										tv.setOnClickListener(new PartListener());
										part_row.addView(tv);
									}
								}
							}
							
						} 
					});	
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					runOnUiThread(new Runnable() {
						public void run() {
							loadlay.setVisibility(View.GONE);
							Toast.makeText(DetailActivity.this, "网络连接超时..", 1).show();
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
//				if(fromtxt.endsWith("channel")){
//					((MainActivity)DetailActivity.this.getParent()).addActivity(fromtxt, ChannelActivity.class,null);
//				}else if(fromtxt.endsWith("home")){
//					((MainActivity)DetailActivity.this.getParent()).addActivity(fromtxt, HomeActivity.class,null);
//				}else if(fromtxt.endsWith("search")){
//					((MainActivity)DetailActivity.this.getParent()).addActivity(fromtxt, SearchActivity.class,null);
//				}
				DetailActivity.this.finish();
				
				break;
			case R.id.detail_fov_btn:
				fov_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.detail_item_favorites_l, 0, 0, 0);
				fov_btn.setEnabled(false);
				if(!new DBService(DetailActivity.this).isFoved(vid)){
					new DBService(DetailActivity.this).addtoFov(vid, title.getText().toString(), String.valueOf(System.currentTimeMillis()));
					Toast.makeText(DetailActivity.this, "收藏成功", 1).show();
				}
				
				break;
			case R.id.detail_downloads_btn:
				
				break;
			case R.id.detail_reply_btn:
				
				break;
			case R.id.detail_share_btn:
				String shareurl =title.getText().toString()+"http://www.acfun.tv/v/ac"+vid+"/"+"[acfun android]";
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
	
	
	private final class PartListener implements OnClickListener{

		@Override
		public void onClick(final View arg0) {
			// TODO Auto-generated method stub
			if(isgame){
				String path = (String) arg0.getTag();
				Intent intent = new Intent(DetailActivity.this, WebViewActivity.class);
				intent.putExtra("path", path);
				startActivity(intent);
				
			}else{
				if(typeid==13){
					String id = (String) arg0.getTag();
					String ur = "http://www.acfun.tv/m/art.php?aid="+id;
					Uri uri =Uri.parse(ur); 
					Intent it = new Intent(Intent.ACTION_VIEW,uri); 
					startActivity(it);
				}else{
					
					if(playcode==2){
						final HashMap<String, String> map = (HashMap<String, String>) arg0.getTag();
						String path = "http://www.acfun.tv/newflvplayer/playert.swf?"+map.get("vars");
			//			String path = "http://static.acfun.tv/ACFlashPlayer.swf?"+map.get("vars");
						
						Intent intent = new Intent(DetailActivity.this, WebViewActivity.class);
						intent.putExtra("path", path);
						startActivity(intent);
					}else{
						fpart_row.removeAllViews();
						arg0.setAnimation(localAnimation);
						final HashMap<String, String> map = (HashMap<String, String>) arg0.getTag();
						new Thread(){
							public void run(){		
								try {
									fpartlist = Parser.ParserVideopath(map.get("type"), map.get("id"));
									runOnUiThread(new Runnable() {
										public void run() {
											for(int i=0;i<fpartlist.size();i++){
												TextView tv = new TextView(DetailActivity.this);
												TableRow.LayoutParams params = new TableRow.LayoutParams(56, 56);
												params.setMargins(10, 4, 0, 4);
												tv.setLayoutParams(params);
												tv.setBackgroundColor(Color.BLACK);
												tv.setGravity(Gravity.CENTER);
												tv.setTextSize(20);
												tv.setText(String.valueOf(i+1));
												tv.setTag(fpartlist.get(i));
												tv.setOnClickListener(new FPartListener());
												fpart_row.addView(tv);
											}
											
											for(TextView tv:parts){
												if(tv!=arg0){
													tv.setEnabled(true);
													tv.setBackgroundResource(R.drawable.detail_part_item_bg);
												}
											}
											arg0.clearAnimation();
											arg0.setEnabled(false);
											arg0.setBackgroundColor(Color.WHITE);
										} 
									});	
									
								} catch (Exception e) {
									// TODO Auto-generated catch block
									runOnUiThread(new Runnable() {
										public void run() {
											arg0.clearAnimation();
											for(TextView tv:parts){
													tv.setEnabled(true);
													tv.setBackgroundResource(R.drawable.detail_part_item_bg);
											}
											
											Toast.makeText(DetailActivity.this, "网络连接超时..", 1).show();
											
										} 
									});	
									e.printStackTrace();
								}
							}	
						}.start();
					}
				}
			}

		}
		
	}
	
	private final class FPartListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String flvpath = (String) v.getTag();
//			//Test
			if(playcode==0){
				Uri uri = Uri.parse(flvpath);
				Intent intent = new Intent(DetailActivity.this, PlayerActivity.class);
				intent.setAction(Intent.ACTION_VIEW);
				intent.setData(uri);
				DetailActivity.this.startActivity(intent);
			}else if(playcode==1){
				String playlink = (String) v.getTag();
				Intent it = new Intent(Intent.ACTION_VIEW);  
		        Uri uri = Uri.parse(flvpath);  
		        it.setDataAndType(uri , "video/flv");  
		        startActivity(it);
			}
		}
		
	}

}
