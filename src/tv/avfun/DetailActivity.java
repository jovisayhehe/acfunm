package tv.avfun;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.avfun.api.ApiParser;
import tv.avfun.api.net.UserAgent;
import tv.avfun.app.AcApp;
import tv.avfun.app.Downloader;
import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
import tv.avfun.entity.VideoInfo;
import tv.avfun.entity.VideoInfo.VideoItem;
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
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
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
	private List<VideoItem> data = new ArrayList<VideoItem>();
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
	private VideoInfo video;
	public static final int FAVORITE = 210;
	public static final int SHARE = 211;
    private static final String TAG = "Detail";
	public ImageLoader imageLoader;
	private Intent mIntent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_layout);
		mIntent = getIntent();
		if(Intent.ACTION_VIEW.equals(mIntent.getAction())){
		    from = "av".equalsIgnoreCase(mIntent.getScheme())?2:0;
		    if(BuildConfig.DEBUG)
		    Log.i(TAG, "看av: "+mIntent.getDataString());
		}else{
    		from = mIntent.getIntExtra("from", 0);
		}
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
        if(from == 2){
            // av://ac000000
            aid = mIntent.getDataString().substring(7);
        }
        else{
            Contents c = (Contents) getIntent().getExtras().get("contents");
            if (c == null) {
                throw new IllegalArgumentException("你从异次元来的吗？");
            } else {
                title = c.getTitle();
                aid = c.getAid();
                channelid = c.getChannelId() + "";
                imageLoader.displayImage(c.getTitleImg(), imageView);
                user_name.setText(c.getUsername());
                views.setText("点击" + "" + c.getViews());
                comments.setText("评论" + "" + c.getComments());
                description = c.getDescription();
                paly_btn.setText("正在加载...");
                titleView.setText(title);
            }
        }
        getSupportActionBar().setTitle("ac"+aid);

        if (from > 0) {
            imageView.setBackgroundResource(R.drawable.no_picture);
            user_name.setText("正在加载...");
            views.setText("正在加载...");
            comments.setText("正在加载...");
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
			public void run() {
				try {
				    video = ApiParser.getVideoInfoByAid(aid);
					data = video.parts;
					runOnUiThread(new Runnable() {
						public void run() {
							if(data!=null && !data.isEmpty()){
								if(from > 0){
									 user_name.setText(video.upman);
									 views.setText(video.views+"");
									 comments.setText(video.comments+"");
									 description = video.description;
									 channelid = video.channelId;
									 title = video.title;
									 titleView.setText(title);
									 String imgurl = video.titleImage;
									 if(!TextUtils.isEmpty(imgurl) || !"null".equals(imgurl)){
										 imageLoader.displayImage(imgurl, imageView);
									 }else{
										 imageView.setBackgroundResource(R.drawable.no_picture);
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
		private List<VideoItem> data;
		private boolean iserror;
		public DetailAdaper(List<VideoItem> data) {
			
			this.data = data;
		}
		
		public void setData(List<VideoItem> data){
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
				
				int dpx = DensityUtil.dip2px(DetailActivity.this, 8);
				descriptiontext.setPadding(0, dpx, 0, dpx);
				setDescription(descriptiontext);
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
						final VideoItem item = data.get(i);
						
						LinearLayout itemlayout = (LinearLayout) LayoutInflater.from(DetailActivity.this).inflate(R.layout.detail_video_list_item, null);
						itemlayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,DensityUtil.dip2px(getApplicationContext(), 40)));
						TextView title = (TextView) itemlayout.findViewById(R.id.detail_video_list_item_title);
						title.setLines(1);
						title.setText(item.subtitle);
						
						TextView vtype = (TextView) itemlayout.findViewById(R.id.detail_video_list_item_vtype);
						vtype.setText(item.vtype);
						
						itemlayout.setTag(item);
						itemlayout.setOnClickListener(new PrtckListener());
						layout.addView(itemlayout);
						ImageView btnDown = (ImageView) itemlayout.findViewById(R.id.detail_btn_downlaod);
						if(Downloader.hasDownload(aid,item.vid)){
						    btnDown.setVisibility(View.GONE);
						    itemlayout.findViewById(R.id.detail_downloaded).setVisibility(View.VISIBLE);
						}
						else{
    						if(Build.VERSION.SDK_INT>=9){
    						    btnDown.setVisibility(View.VISIBLE);
    						    btnDown.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        v.setVisibility(View.GONE);
                                        startDownload(item);
                                    }
                                });
    						}
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
	private void startDownload(final VideoItem item) {
	    new Thread(){
            public void run() {

	            try {
	                ApiParser.parseVideoParts(item);
	                if(item.urlList!=null&& !item.urlList.isEmpty()){
	                    // item.files
	                    handler.obtainMessage(1, item).sendToTarget();
	                }
	                else handler.sendEmptyMessage(2);
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
	            VideoItem info = (VideoItem) msg.obj;
	            Toast.makeText(getApplicationContext(), info.subtitle+"已开始下载！", 0).show();
	            // urls 不应为null
	            List<String> urls = info.urlList;
	            File file = AcApp.getDownloadPath(aid, info.vid);
	            file.mkdirs();
	            for(int i = 0 ; i< urls.size(); i++){
                    String url = urls.get(i);
                    String filename = i+FileUtil.getUrlExt(url);
                    if(BuildConfig.DEBUG) Log.i(TAG, url);
                    DownloadManager downloadMan = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    Request request = new Request(Uri.parse(url))
                    .addRequestHeader("User-Agent", UserAgent.DEFAULT)
                    .setAllowedNetworkTypes(Request.NETWORK_WIFI)
                    .setAllowedOverRoaming(false)
                    .setDescription(title)
                    .setTitle(info.subtitle+"_"+filename)
                    .setDestinationInExternalPublicDir("Download/AcFun/Videos/"+aid+"/"+info.vid, filename);
                    // TODO 将id存起来监听下载进度
                    downloadMan.enqueue(request);
                }
	            //TODO 改变界面btn状态为 下载中
	        }else if(msg.what == 2){
	            Toast.makeText(getApplicationContext(), "可恶，解析视频地址失败！", 0).show();
	        }
	    }
	};
	private final class PrtckListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			
			VideoItem item =(VideoItem) v.getTag();
			startToPlay(item);
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
	public void setDescription(TextView text){
	    Pattern wiki = Pattern.compile("\\[wiki(.+)\\]",Pattern.CASE_INSENSITIVE);
	    String localDesc = description;
	    text.setText(localDesc);
	    Linkify.addLinks(text, wiki,null,null,new TransformFilter() {
	        @Override
	        public String transformUrl(Matcher match, String url) {
	            String t = match.group(1);
	            return "http://wiki.acfun.tv/index.php/"+t;
	        }
	    });
	    Pattern http = Pattern.compile("(http://(?:[a-z0-9.-]+[.][a-z]{2,}+(?::[0-9]+)?)(?:/\\S*)?)",Pattern.CASE_INSENSITIVE);
	    Linkify.addLinks(text,http,"http://");
        Linkify.addLinks(text, Pattern.compile("(ac\\d{5,})", Pattern.CASE_INSENSITIVE),"av://");
        
	}
	public void addToHistory(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
        new DBService(this).addtoHis(aid, title, sdf.format(new Date()), 0, channelid);
        isfavorite = new DBService(this).isFoved(aid);
	}

    public void startToPlay(VideoItem item){
        addToHistory();
		String vtype = item.vtype;
		String vid = item.vid;
		String title = item.subtitle;
		Intent intent = new Intent(DetailActivity.this, SectionActivity.class);
		intent.putExtra("title", title);
		intent.putExtra("vid", vid);
		intent.putExtra("vtype", vtype);
		intent.putExtra("aid", aid);
		startActivity(intent);
	}
}
