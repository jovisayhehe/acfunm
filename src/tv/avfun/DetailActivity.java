package tv.avfun;

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


import tv.avfun.R;
import tv.acfun.db.DBService;
import tv.acfun.util.Parser;
import tv.acfun.util.Util;

import acfun.domain.AcfunContent;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.ImageView;
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
	//private AcfunContent content;
	private TextView up_txt;
	private TextView time_txt;
	private TextView hit_txt;
	private TextView info_txt;
	private TextView fov_txt;
	private String vid;
	private TableRow part_row;
	private TableRow fpart_row;
	private ImageView imgview;
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
		byte[] b = getIntent().getByteArrayExtra("bitmap");
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
		imgview = (ImageView) findViewById(R.id.detail_img);
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
		title.setText(infos.get(2));
		if(fromtxt!="favorites"&&!fromtxt.equals("favorites")){
			up_txt.setText("投稿:"+infos.get(3));
			Date date = new Date(Long.parseLong(infos.get(4)));
			time_txt.setText("投稿时间:"+date.toLocaleString());
			hit_txt.setText("点击:"+infos.get(5));
			info_txt.setText(infos.get(6));
			fov_txt.setText("收藏:"+infos.get(7));	
			imgview.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));	
		}
		
		
		
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
					
					//content = Parser.getContent(id);
					partlist = Parser.ParserAcId(id);
					runOnUiThread(new Runnable() {
						public void run() {
							loadlay.setVisibility(View.INVISIBLE);
							if(partlist!=null&&!partlist.isEmpty()){
								HashMap<String, String> map = partlist.get(0);
								if(map!=null){
									notfound = map.get("type")==null||map.get("id")==null||map.get("type").equals("")||map.get("id")=="";
								}else{
									notfound=true;
								}
							}else{
								notfound=false;
							}
							if(notfound){
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
								

									if(typeid!=13){
										
										if(partlist.size()==1){
											TextView tv = new TextView(DetailActivity.this);
											TableRow.LayoutParams params = new TableRow.LayoutParams(156, 56);
											params.setMargins(10, 4, 0, 4);
											tv.setLayoutParams(params);
											tv.setBackgroundResource(R.drawable.detail_part_item_bg);
											tv.setGravity(Gravity.CENTER);
											tv.setTextSize(22);
											tv.setTextColor(Color.BLACK);
											tv.setText("点击播放");
											tv.setTag(partlist.get(0));
											tv.setOnClickListener(new PartListener());
											parts.add(tv);
											part_row.addView(tv);
										}else{
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
							Toast.makeText(DetailActivity.this, "网络连接超时..请重试...", 1).show();
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
				Intent cintent =new Intent(DetailActivity.this, CommentActivity.class);
				cintent.putExtra("id", vid);
				startActivity(cintent);
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
						fpart_row.removeAllViews();
						arg0.setAnimation(localAnimation);
						final HashMap<String, String> map = (HashMap<String, String>) arg0.getTag();
						new Thread(){
							public void run(){		
								try {
									fpartlist = Parser.ParserVideopath(map.get("type"), map.get("id"));
									runOnUiThread(new Runnable() {
										public void run() {
											
											if(fpartlist.size()==1){
												String flvpath = (String) fpartlist.get(0);
												Intent it = new Intent(Intent.ACTION_VIEW);  
										        Uri uri = Uri.parse(flvpath);  
										        it.setDataAndType(uri , "video/flv");  
										        startActivity(it);
											}else{
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
											}
											
											for(TextView tv:parts){
												if(tv!=arg0){
													tv.setEnabled(true);
													tv.setBackgroundResource(R.drawable.detail_part_item_bg);
												}
											}
											arg0.clearAnimation();
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
											
											Toast.makeText(DetailActivity.this, "网络连接超时...请重试...", 1).show();
											
										} 
									});	
									e.printStackTrace();
								}
							}	
						}.start();

		}
		
	}
	
	private final class FPartListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String flvpath = (String) v.getTag();
				String playlink = (String) v.getTag();
				Intent it = new Intent(Intent.ACTION_VIEW);  
		        Uri uri = Uri.parse(flvpath);  
		        it.setDataAndType(uri , "video/flv");  
		        startActivity(it);
		}
		
	}
	
	public static Bitmap getPicFromBytes(byte[] bytes,
			BitmapFactory.Options opts) {
		if (bytes != null)
			if (opts != null)
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
						opts);
			else
				return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return null;
	}

}
