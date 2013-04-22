package tv.avfun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import tv.avfun.api.ApiParser;
import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
import tv.avfun.util.DensityUtil;
import tv.avfun.util.lzlist.ImageLoader;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.umeng.analytics.MobclickAgent;

public class Detail_Activity extends SherlockActivity implements OnClickListener{
	private ListView listview;
	private String aid;
	private String description;
	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String,Object>>();
	private int from;
	private String title;
	private DetailAdaper adaper;
	private TextView user_name;
	private ImageView imageView;
	private TextView views;
	private TextView comments;
	private TextView paly_btn;
	private String channelid;
	private boolean isfavorite = false;
	private HashMap<String, Object> video;
	private HashMap<String, String> info ;
	public static final int FAVORITE = 210;
	public static final int SHARE = 211;
	public ImageLoader imageLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_layout);
		from = getIntent().getIntExtra("from", 0);
		imageLoader=ImageLoader.getInstance();
		initview();
	}
		
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);
    	MenuItem actionItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider actionProvider = (ShareActionProvider) actionItem.getActionProvider();
        actionProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        actionProvider.setShareIntent(createShareIntent());
    	
        if(isfavorite){
        	 menu.findItem(R.id.menu_item_fov_action_provider_action_bar).setIcon(R.drawable.rating_favorite_p);
        }
        return super.onCreateOptionsMenu(menu);
    }
    
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
			
		case R.id.menu_item_fov_action_provider_action_bar:
			if(isfavorite){
				new DBService(this).delFov(aid);
				isfavorite = false;
				item.setIcon(R.drawable.rating_favorite);
				Toast.makeText(this, "取消成功", Toast.LENGTH_SHORT).show();
			}else{
				new DBService(this).addtoFov(aid, title, 0, channelid);
				isfavorite = true;
				item.setIcon(R.drawable.rating_favorite_p);
				Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
			}

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
    private Intent createShareIntent() {
    	String shareurl = title+"http://www.acfun.tv/v/ac"+aid;
		Intent shareIntent = new Intent(Intent.ACTION_SEND);  
		shareIntent.setType("text/plain");  
		shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");  
		shareIntent.putExtra(Intent.EXTRA_TEXT, shareurl);  
        return shareIntent;
    }

	
	public void initview(){
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		 
		 user_name = (TextView) findViewById(R.id.detail_usename);
		 views = (TextView) findViewById(R.id.detail_views);
		 comments = (TextView) findViewById(R.id.detail_comment);
		 imageView = (ImageView) findViewById(R.id.detail_img);
		 paly_btn = (TextView) findViewById(R.id.detail_play_btn);
		 paly_btn.setTag(123);
		 Contents c = (Contents) getIntent().getExtras().get("contents");
		 boolean flag = false;
		 if(flag = c == null) {
		 title = getIntent().getStringExtra("title");
		 aid = getIntent().getStringExtra("aid");
		 channelid = getIntent().getStringExtra("channelId");
		 }else{
		 title = c.getTitle();
		 aid = c.getAid();
		 channelid = c.getChannelId()+"";
		 }
		 getSupportActionBar().setTitle(title);
		 SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
		 new DBService(this).addtoHis(aid, title, sdf.format(new Date()),0,channelid);
		 isfavorite = new DBService(this).isFoved(aid);
		 
		 if(from==1){
			 imageView.setBackgroundResource(R.drawable.face);
			 user_name.setText("正在加载...");
			 views.setText("正在加载...");
			 comments.setText("正在加载...");
			 paly_btn.setText("正在加载...");
			 
		 }else{
		     user_name.setText(Html.fromHtml("<font color=\"#ABABAB\">up主: </font>"));
		     views.setText(Html.fromHtml("<font color=\"#ABABAB\">点击: </font>"));
		     comments.setText(Html.fromHtml("<font color=\"#ABABAB\">评论: </font>"));
		     if(flag){
			 byte[] b = getIntent().getByteArrayExtra("thumb");
			 imageView.setImageBitmap(BitmapFactory.decodeByteArray(b, 0, b.length));
			 user_name.setText(user_name.getText()+getIntent().getStringExtra("username"));
			 views.setText(views.getText()+getIntent().getStringExtra("views"));
			 comments.setText(comments.getText()+getIntent().getStringExtra("comments"));
			 description = getIntent().getStringExtra("description");
		     }else{
		     imageLoader.displayImage(c.getTitleImg(), imageView);
		     user_name.setText(user_name.getText()+c.getUsername());
		     views.setText(views.getText()+""+c.getViews());
		     comments.setText(comments.getText()+""+c.getComments());
		     description = c.getDescription();
		     }
			 paly_btn.setText("正在加载...");
		 }
		 listview = (ListView) findViewById(R.id.detail_listview);
		 adaper = new DetailAdaper(data);
		 listview.setAdapter(adaper);
		 getdatas(aid);
	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}
	
	
	public void getdatas(final String aid){
		new Thread() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					if(from!=1){
						video = ApiParser.ParserAcId(aid,false);
					}else{
						video = ApiParser.ParserAcId(aid,true);
						info = (HashMap<String, String>) video.get("info");
					}
					
					data = (ArrayList<HashMap<String, Object>>) video.get("pts");
					
					runOnUiThread(new Runnable() {
						public void run() {
							if(data!=null){
								if(from==1){
									 user_name.setText(Html.fromHtml("<font color=\"#ABABAB\">up主: </font>"+info.get("username")));
									 views.setText(Html.fromHtml("<font color=\"#ABABAB\">点击: </font>"+info.get("views")));
									 comments.setText(Html.fromHtml("<font color=\"#ABABAB\">评论: </font>"+info.get("comments")));
									 description = info.get("description");
									 String imgurl = info.get("titleimage");
									 if(imgurl!=""&&!imgurl.equals("")){
										 imageLoader.displayImage(imgurl, imageView);
									 }else{
										 imageView.setBackgroundResource(R.drawable.face);
									 }
									 
								}
								paly_btn.setText("播放");
								paly_btn.setOnClickListener(Detail_Activity.this);
								adaper.setData(data);
								adaper.notifyDataSetChanged();
							}else{
								adaper.setIserror(true);
								adaper.notifyDataSetChanged();
							}
	
						}
					});

				} catch (Exception e) {
					
					runOnUiThread(new Runnable() {
						public void run() {
							adaper.setIserror(true);
							adaper.notifyDataSetChanged();
							
						}
					});
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	
	
	private final class DetailAdaper extends BaseAdapter {
		private ArrayList<HashMap<String, Object>> data;
		private boolean iserror;
		public DetailAdaper(ArrayList<HashMap<String, Object>> data) {
			
			this.data = data;
		}
		
		public void setData(ArrayList<HashMap<String, Object>> data){
			this.data = data;
		}
		
		public void setIserror(boolean iserror){
			this.iserror = iserror;
		}
		@Override
		public int getCount() {
			
			return 3;
		}

		@Override
		public Object getItem(int position) {
			
			return null;
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LinearLayout layout = new LinearLayout(Detail_Activity.this);
			layout.setOrientation(LinearLayout.VERTICAL);
			
			switch (position) {
			case 0:
				TextView descriptiontext = new TextView(Detail_Activity.this);
				descriptiontext.setText(description);
				int dpx = DensityUtil.dip2px(Detail_Activity.this, 8);
				descriptiontext.setPadding(dpx, dpx, dpx, dpx);
				
				TextView pttext = new TextView(Detail_Activity.this);
				pttext.setText("视频段落");
				pttext.setTextSize(15);
				pttext.setTextColor(Color.parseColor("#FF9A03"));
				pttext.setPadding(dpx, dpx, dpx, 3);
				
				View ylline = new View(Detail_Activity.this);
				ylline.setLayoutParams(new LinearLayout.LayoutParams(-1, 2));
				ylline.setBackgroundColor(Color.parseColor("#FF9A03"));
				
				layout.addView(descriptiontext);
				layout.addView(pttext);
				layout.addView(ylline);
				convertView = layout;
				break;
			case 1:
				if(data!=null&&!data.isEmpty()){
					for (int i = 0; i < data.size(); i++) {
						HashMap<String, Object> map = data.get(i);
						
						LinearLayout itemlayout = (LinearLayout) LayoutInflater.from(Detail_Activity.this).inflate(R.layout.detail_video_list_item, null);
						
						TextView title = (TextView) itemlayout.findViewById(R.id.detail_video_list_item_title);
						title.setText(map.get("title").toString());
						
						TextView vtype = (TextView) itemlayout.findViewById(R.id.detail_video_list_item_vtype);
						vtype.setText(map.get("vtype").toString());
						
						itemlayout.setTag(map);
						itemlayout.setOnClickListener(new PrtckListener());
						layout.addView(itemlayout);
						//添加分割线
						if(i!=data.size()-1){
							View lineView = new View(Detail_Activity.this);
							lineView.setBackgroundResource(R.drawable.listview_divider);
							lineView.setLayoutParams(new LinearLayout.LayoutParams(-1, DensityUtil.dip2px(Detail_Activity.this, 1)));
							layout.addView(lineView);
						}
					}
					convertView = layout;
				}else{
					if(iserror){
						LayoutInflater mInflater = LayoutInflater.from(Detail_Activity.this);
						convertView = mInflater.inflate(R.layout.list_footerview, null);
						convertView.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
						TextView textview = (TextView) convertView.findViewById(R.id.list_footview_text);
						textview.setText(R.string.reloading);
						convertView.setTag(100);
						convertView.setEnabled(true);
						convertView.setOnClickListener(Detail_Activity.this);
						
					}else{
						LayoutInflater mInflater = LayoutInflater.from(Detail_Activity.this);
						convertView = mInflater.inflate(R.layout.list_footerview, null);;
						convertView.setEnabled(false);
					}
			
				}
				break;
			case 2:
				TextView comments = new TextView(Detail_Activity.this);
				int cpx = DensityUtil.dip2px(Detail_Activity.this, 8);
				comments.setPadding(cpx, cpx, cpx, cpx);
				comments.setText("查看评论");
				comments.setTextColor(Color.parseColor("#FF9A03"));
				comments.setTextSize(17);
				comments.setGravity(Gravity.CENTER);
				layout.addView(comments);
				layout.setBackgroundResource(R.drawable.selectable_background);
				convertView = layout;
				convertView.setTag(101);
				convertView.setOnClickListener(Detail_Activity.this);
				break;
			default:
				break;
			}
			return convertView;
		}
		
	}

	
	private final class PrtckListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			HashMap<String, Object> map = (HashMap<String, Object>) v.getTag();
			startToPlay(map);
		}
		
	}

	@Override
	public void onClick(View v) {
		
		switch ((Integer)v.getTag()) {
		case 100:
			v.setEnabled(false);
			adaper.setIserror(false);
			adaper.notifyDataSetChanged();
			getdatas(aid);
			break;
		case 101:
			Intent intent = new Intent(Detail_Activity.this, Comments_Activity.class);
			intent.putExtra("aid", aid);
			startActivity(intent);
			break;
		case 123:
			startToPlay(data.get(0));
			break;
		default:
			break;
		}
	}
	
	public void startToPlay(HashMap<String, Object> map){
		String vtype = (String) map.get("vtype");
		String vid = (String) map.get("vid");
		String title = (String) map.get("title"); 
		Intent intent = new Intent(Detail_Activity.this, Section_Activity.class);
		intent.putExtra("title", title);
		intent.putExtra("vid", vid);
		intent.putExtra("vtype", vtype);
		startActivity(intent);
	}
}
