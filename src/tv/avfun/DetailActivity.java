package tv.avfun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import tv.avfun.api.ApiParser;
import tv.avfun.api.net.UserAgent;
import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
import tv.avfun.util.DensityUtil;
import tv.avfun.util.FileUtil;
import tv.avfun.util.lzlist.ImageLoader;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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

public class DetailActivity extends SherlockActivity implements OnClickListener{
	private ListView listview;
	private String aid;
	private String description;
	private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String,Object>>();
	private int from;
	private String title;
	private DetailAdaper adaper;
	private TextView user_name;
	private ImageView imageView;
	private TextView titleView;
	private TextView views;
	private TextView comments;
	private TextView paly_btn;
	private String channelid;
	private boolean isfavorite = false;
	private HashMap<String, Object> video;
	private HashMap<String, String> info ;
	public static final int FAVORITE = 210;
	public static final int SHARE = 211;
    private static final String TAG = "Detail";
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
        titleView = (TextView) findViewById(R.id.detail_title);
        user_name = (TextView) findViewById(R.id.detail_usename);
        views = (TextView) findViewById(R.id.detail_views);
        comments = (TextView) findViewById(R.id.detail_comment);
        imageView = (ImageView) findViewById(R.id.detail_img);
        paly_btn = (TextView) findViewById(R.id.detail_play_btn);
        paly_btn.setTag(123);
        Contents c = (Contents) getIntent().getExtras().get("contents");
        if (c == null) {
            throw new IllegalArgumentException("你从异次元来的吗？");
        } else {
            title = c.getTitle();
            aid = c.getAid();
            channelid = c.getChannelId() + "";
        }
        getSupportActionBar().setTitle("详情");
        titleView.setText(title);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
        new DBService(this).addtoHis(aid, title, sdf.format(new Date()), 0, channelid);
        isfavorite = new DBService(this).isFoved(aid);
        if (from == 1) {
            imageView.setBackgroundResource(R.drawable.face);
            user_name.setText("正在加载...");
            views.setText("正在加载...");
            comments.setText("正在加载...");
            paly_btn.setText("正在加载...");
        } else {
            imageLoader.displayImage(c.getTitleImg(), imageView);
            user_name.setText(c.getUsername());
            views.setText("点击" + "" + c.getViews());
            comments.setText("评论" + "" + c.getComments());
            description = c.getDescription();
            paly_btn.setText("正在加载...");
        }
        listview = (ListView) findViewById(R.id.detail_listview);
        adaper = new DetailAdaper(data);
        // TODO 似乎根本就不需要listview嘛
        listview.setAdapter(adaper);
        listview.setDuplicateParentStateEnabled(true);
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
									 user_name.setText(info.get("username"));
									 views.setText(info.get("views"));
									 comments.setText(info.get("comments"));
									 description = info.get("description");
									 String imgurl = info.get("titleimage");
									 if(imgurl!=""&&!imgurl.equals("")){
										 imageLoader.displayImage(imgurl, imageView);
									 }else{
										 imageView.setBackgroundResource(R.drawable.face);
									 }
									 
								}
								paly_btn.setText("播放");
								paly_btn.setOnClickListener(DetailActivity.this);
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
			
			LinearLayout layout = new LinearLayout(DetailActivity.this);
			layout.setOrientation(LinearLayout.VERTICAL);
			
			switch (position) {
			case 0:
				TextView descriptiontext = new TextView(DetailActivity.this);
				descriptiontext.setText(description);
				int dpx = DensityUtil.dip2px(DetailActivity.this, 8);
				descriptiontext.setPadding(0, dpx, 0, dpx);
				
				TextView pttext = new TextView(DetailActivity.this);
				pttext.setText("视频段落");
				pttext.setTextSize(15);
				pttext.setTextColor(0xFFFF9A03);
				pttext.setPadding(0, dpx, dpx, 3);
				
				View ylline = new View(DetailActivity.this);
				ylline.setLayoutParams(new LinearLayout.LayoutParams(-1, 2));
				ylline.setBackgroundColor(0xFFFF9A03);
				
				layout.addView(descriptiontext);
				layout.addView(pttext);
				layout.addView(ylline);
				convertView = layout;
				break;
			case 1:
				if(data!=null&&!data.isEmpty()){
					for (int i = 0; i < data.size(); i++) {
						final HashMap<String, Object> map = data.get(i);
						
						LinearLayout itemlayout = (LinearLayout) LayoutInflater.from(DetailActivity.this).inflate(R.layout.detail_video_list_item, null);
						
						TextView title = (TextView) itemlayout.findViewById(R.id.detail_video_list_item_title);
						title.setLines(1);
						title.setText(map.get("title").toString());
						
						TextView vtype = (TextView) itemlayout.findViewById(R.id.detail_video_list_item_vtype);
						vtype.setText(map.get("vtype").toString());
						
						itemlayout.setTag(map);
						itemlayout.setOnClickListener(new PrtckListener());
						layout.addView(itemlayout);
						View btnDown = itemlayout.findViewById(R.id.detail_btn_downlaod);
						if(Build.VERSION.SDK_INT>=9){
						    btnDown.setVisibility(View.VISIBLE);
						    btnDown.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.setEnabled(false);
                                    startDownload(map.get("vtype").toString(), map.get("vid").toString());
                                }
                            });
						}
						
						//添加分割线
						if(i!=data.size()-1){
							View lineView = new View(DetailActivity.this);
							lineView.setBackgroundResource(R.drawable.listview_divider);
							lineView.setLayoutParams(new LinearLayout.LayoutParams(-1, DensityUtil.dip2px(DetailActivity.this, 1)));
							layout.addView(lineView);
						}
					}
					convertView = layout;
				}else{
					if(iserror){
						LayoutInflater mInflater = LayoutInflater.from(DetailActivity.this);
						convertView = mInflater.inflate(R.layout.list_footerview, null);
						convertView.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
						TextView textview = (TextView) convertView.findViewById(R.id.list_footview_text);
						textview.setText(R.string.reloading);
						convertView.setTag(100);
						convertView.setEnabled(true);
						convertView.setOnClickListener(DetailActivity.this);
						
					}else{
						LayoutInflater mInflater = LayoutInflater.from(DetailActivity.this);
						convertView = mInflater.inflate(R.layout.list_footerview, null);;
						convertView.setEnabled(false);
					}
			
				}
				break;
			case 2:
				View.inflate(DetailActivity.this, R.layout.detail_comments_btn_layout, layout);
				//layout.addView(comments);
				layout.setBackgroundResource(R.drawable.selectable_background);
				convertView = layout;
				convertView.setTag(101);
				convertView.setOnClickListener(DetailActivity.this);
				break;
			default:
				break;
			}
			return convertView;
		}

		
	}
	// TODO 下载
	private void startDownload(final String type, final String vid) {
	    new Thread(){
            public void run() {
	            Environment.getExternalStoragePublicDirectory("AcFun/Download/"+aid).mkdirs();
	            try {
	                List<String> urls = ApiParser.ParserVideopath(type, vid);
	                if(urls!=null)
	                    handler.obtainMessage(1, urls).sendToTarget();
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }.start();
    }
	private Handler handler = new Handler(){
	    @TargetApi(9)
	    public void handleMessage(Message msg) {
	        if(msg.what == 1){
	            Toast.makeText(getApplicationContext(), title+"已开始下载", 0).show();
	            // urls 不应为null
	            List<String> urls = (List<String>)msg.obj;
	            for(int i = 0 ; i< urls.size(); i++){
                    String url = urls.get(i);
                    String filename = i+FileUtil.getUrlExt(url);
                    if(BuildConfig.DEBUG) Log.i(TAG, url);
                    DownloadManager downloadMan = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    Request request = new Request(Uri.parse(url))
                    .addRequestHeader("User-Agent", UserAgent.IPAD)
                    .setAllowedNetworkTypes(Request.NETWORK_WIFI)
                    .setAllowedOverRoaming(false)
                    .setTitle(title+"_"+filename)
                    .setDestinationInExternalPublicDir("AcFun/Download/"+aid, filename);
                    // TODO 将id存起来监听下载进度
                    downloadMan.enqueue(request);
                }
	            //TODO 改变界面btn状态为 下载中
	            
	        }
	    }
	    
	};
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
			Intent intent = new Intent(DetailActivity.this, CommentsActivity.class);
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
		Intent intent = new Intent(DetailActivity.this, SectionActivity.class);
		intent.putExtra("title", title);
		intent.putExtra("vid", vid);
		intent.putExtra("vtype", vtype);
		startActivity(intent);
	}
}
