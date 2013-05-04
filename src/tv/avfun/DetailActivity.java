package tv.avfun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.avfun.api.ApiParser;
import tv.avfun.app.AcApp;
import tv.avfun.app.DownloadService;
import tv.avfun.app.DownloadService.DownloadBinder;
import tv.avfun.app.Downloader;
import tv.avfun.app.Downloader.DownloadHandler;
import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
import tv.avfun.entity.VideoInfo;
import tv.avfun.entity.VideoInfo.VideoItem;
import tv.avfun.util.NetWorkUtil;
import tv.avfun.util.StringUtil;
import tv.avfun.util.lzlist.ImageLoader;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.umeng.analytics.MobclickAgent;

public class DetailActivity extends SherlockActivity implements OnClickListener,OnItemClickListener{
	private ListView listview;
	private String aid;
	private String description;
	private List<VideoItem> data = new ArrayList<VideoItem>();
	private int from;
	private String title;
	private DetailAdaper adapter;
	private TextView user_name;
	private ImageView imageView;
	private TextView titleView;
	private TextView views;
	private TextView comments;
	private TextView paly_btn;
    private TextView desc;
	private int channelid;
	private boolean isfavorite = false;
	private VideoInfo video;
	public static final int FAVORITE = 210;
	public static final int SHARE = 211;
    private static final String TAG = "DetailActivity";
	public ImageLoader imageLoader;
	private Intent mIntent;
	private DownloadBinder downloadService;
    private ServiceConnection conn = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, service.getClass().getName());
            downloadService = (DownloadBinder) service;
        }
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        Intent service = new Intent(this,DownloadService.class);
        bindService(service, conn , BIND_AUTO_CREATE);
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
	protected void onStart() {
	    super.onStart();

        registerReceiver(onProgress, new IntentFilter(DownloadService.ACTION_VIEW_PROGRESS));
        registerReceiver(onDownloadFail, new IntentFilter(DownloadService.ACTION_DOWNLOAD_FAIL));
        registerReceiver(onDownloadComplete, new IntentFilter(DownloadService.ACTION_DOWNLOAD_SUCCESS));
        if(adapter != null){
            adapter.notifyDataSetChanged();
        }
        
	}
	@Override
	protected void onStop() {
	    super.onStop();
	    unregisterReceiver(onProgress);
	    unregisterReceiver(onDownloadFail);
	    unregisterReceiver(onDownloadComplete);
	}
	
	public void initview(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        titleView = (TextView) findViewById(R.id.detail_title);
        user_name = (TextView) findViewById(R.id.detail_usename);
        views = (TextView) findViewById(R.id.detail_views);
        comments = (TextView) findViewById(R.id.detail_comment);
        imageView = (ImageView) findViewById(R.id.detail_img);
        paly_btn = (TextView) findViewById(R.id.detail_play_btn);
        desc = (TextView) findViewById(R.id.detail_desc);
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
                title = StringUtil.getSource(c.getTitle());
                aid = c.getAid();
                channelid = c.getChannelId();
                imageLoader.displayImage(c.getTitleImg(), imageView);
                user_name.setText(c.getUsername());
                views.setText("点击" + "" + c.getViews());
                comments.setText("评论" + "" + c.getComments());
                description = c.getDescription();
                paly_btn.setText("正在加载...");
                titleView.setText( c.getTitle());
            }
        }
        getSupportActionBar().setTitle("ac"+aid);

        if (from > 0) {
            imageView.setBackgroundResource(R.drawable.no_picture);
            user_name.setText("正在加载...");
            views.setText("正在加载...");
            comments.setText("正在加载...");
            paly_btn.setText("正在加载...");
            desc.setText("正在加载...");
        }
        listview = (ListView) findViewById(R.id.detail_listview);
        adapter = new DetailAdaper(data);
        listview.setAdapter(adapter);
        listview.setDuplicateParentStateEnabled(true);
        listview.setOnItemClickListener(this);
        mInflater = LayoutInflater.from(DetailActivity.this);
        loadView = findViewById(R.id.load_view);
        btnComment = mInflater.inflate(R.layout.detail_comments_btn_layout, listview,false);
        btnComment.setBackgroundResource(R.drawable.selectable_background);
        btnComment.setTag(101);
        btnComment.setOnClickListener(DetailActivity.this);
        listview.addFooterView(btnComment);
        loadData(aid);
	}

	public void loadData(String aid){
	    new RequestDetailTask().execute(aid);
	}
	private class RequestDetailTask extends AsyncTask<String, Void, Boolean>{
	    
	    private TextView text;
        private View progress;
        @Override
	    protected void onPreExecute() {

            loadView.setEnabled(false);
            progress = loadView.findViewById(R.id.list_loadview_progress);
            progress.setVisibility(View.VISIBLE);
            text = (TextView) loadView.findViewById(R.id.list_loadview_text);
            text.setText(R.string.loading);
            loadView.setVisibility(View.VISIBLE);
            listview.setVisibility(View.INVISIBLE);
	    }
	    

        @Override
        protected Boolean doInBackground(String... params) {
            String aid = params[0];
            try {
                if(NetWorkUtil.isNetworkAvailable(getApplicationContext())){
                    video = ApiParser.getVideoInfoByAid(aid);
                    data = video.parts;
                }
                // 下载功能只有9以上才有
                if(Build.VERSION.SDK_INT>=9){
                    // 先从downloadlist数据库中查aid 对应的video item
                    List<VideoItem> items = new DBService(getApplicationContext()).getVideoItems(aid);
                    // 再从过滤掉已下载的vid
                    if(data!=null){
                        if (items != null && !items.isEmpty()) {
                            for (int i = 0; i < items.size(); i++) {
                                boolean b = data.remove(items.get(i));
                                if (b && BuildConfig.DEBUG)
                                    Log.i(TAG, "过滤掉" + items.get(i).vid + " - " + items.get(i).subtitle);
                            }
                            // 将已下载的item加到data中
                            data.addAll(items);
                        }
                    } else data = items; // 没有解析到在线数据，把数据库查到的赋值给他，maybe empty although 
                }
                return data!=null && !data.isEmpty();
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.w(TAG, "获取数据出错！"+aid,e);
                return false;
            }
            
        }
	    @Override
	    protected void onPostExecute(Boolean result) {

            if (result) {
                if (from > 0) {
                    user_name.setText(video.upman);
                    views.setText(video.views + "");
                    comments.setText(video.comments + "");
                    description = video.description;
                    channelid = video.channelId;
                    title = video.title;
                    titleView.setText(title);
                    String imgurl = video.titleImage;
                    if (!TextUtils.isEmpty(imgurl) || !"null".equals(imgurl)) {
                        imageLoader.displayImage(imgurl, imageView);
                    } else {
                        imageView.setBackgroundResource(R.drawable.no_picture);
                    }

                }
                paly_btn.setText("播放");
                paly_btn.setOnClickListener(DetailActivity.this);
                listview.setVisibility(View.VISIBLE);
                loadView.setVisibility(View.GONE);
                adapter.setData(data);
                adapter.notifyDataSetChanged();
            } else {
                progress.setVisibility(View.GONE);
                text.setText(R.string.reloading);
                loadView.setTag(100);
                loadView.setEnabled(true);
                loadView.setOnClickListener(DetailActivity.this);
            }
	        
	    }
	}
	
/*	
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
	*/
	
	
	class DetailAdaper extends BaseAdapter {
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
			
			return data.size();
		}

		@Override
		public VideoItem getItem(int position) {
			
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
		public void setProgress(String vid,int progress,int total){
		    ProgressBar pb = progressbars.get(vid);
		    if(pb == null) return;
		    pb.setIndeterminate(false);
		    pb.setMax(total);
		    pb.setProgress(progress);
		    //this.notifyDataSetChanged();
		}
		public void hideProgress(boolean isFailed, String vid){
		    if(isFailed){
		        handler.sendEmptyMessage(2);
		    }else{
		        TextView st = statuss.get(vid);
		        if(st == null) return;
		        st.setText("已下载");
		        st.setTag(3);
		        VideoItem item = new DBService(getApplicationContext()).getDownloadedItemById(vid);
                data.remove(item); //去掉原有的
                data.add(item);
                this.notifyDataSetChanged();
		    }
		    ProgressBar pb = progressbars.get(vid);
		    pb.setVisibility(View.GONE);
		    
		}

		Map<String,ProgressBar> progressbars  = new HashMap<String, ProgressBar>();
		Map<String,TextView> statuss = new HashMap<String, TextView>();
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final VideoItem item = data.get(position);
            View itemlayout = LayoutInflater.from(DetailActivity.this).inflate(R.layout.detail_video_list_item, listview,false);
            TextView title = (TextView) itemlayout.findViewById(R.id.detail_video_list_item_title);
            TextView vtype = (TextView) itemlayout.findViewById(R.id.detail_video_list_item_vtype);
            title.setText(StringUtil.getSource(item.subtitle));
            vtype.setText(item.vtype==null?"本地":item.vtype);
            if(Build.VERSION.SDK_INT>=9){
                final TextView status = (TextView) itemlayout.findViewById(R.id.detail_status);
                final ProgressBar progress = (ProgressBar) itemlayout.findViewById(R.id.detail_progress);
                progress.setVisibility(View.GONE);
                progress.setIndeterminate(true);
                progressbars.put(item.vid, progress);
                status.setVisibility(View.VISIBLE);
                statuss.put(item.vid, status);
                if(Downloader.isDownloaded(getApplicationContext(), item.vid)){ // 下载完毕
                    item.isdownloaded = true;
                    status.setText("已下载"); 
                    status.setTag(3);
                }else if(Downloader.isDownloading(getApplicationContext(), item.vid)){ // 下载中
                    status.setText("取消");
                    status.setTag(2);
                    downloadService.doQueryStatus(item.vid);
                    progress.setVisibility(View.VISIBLE);
                }else{
                    // TODO 开启download服务 监听下载进度
                    status.setText("下载");
                    status.setTag(1);
                }
                status.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if((Integer)status.getTag() == 1){
                            status.setText("取消");
                            startDownload(item);
                            progress.setVisibility(View.VISIBLE);
                            status.setTag(2);
                        }else if((Integer)status.getTag() == 2){
                            status.setText("下载");
                            progress.setVisibility(View.INVISIBLE);
                            removeDownload(item.vid);
                            status.setTag(1);
                        }else if((Integer)status.getTag() == 3){
                            startActivity(new Intent(getApplicationContext(), DownloadManActivity.class));
                        }
                    }
                    
                });
               
            }
            return itemlayout;
        }
	}

	protected void removeDownload(String vid) {
	    Downloader.removeDownload(this,vid);
	}
	// TODO 下载
	private void startDownload(final VideoItem item) {

	    new Thread(){
            public void run() {

	            try {
                    int parseMode = 1;
                    if(AcApp.getConfig().getBoolean("isHD", false))
                        parseMode = 2;
	                ApiParser.parseVideoParts(item,parseMode);
	                if(item.urlList != null && !item.urlList.isEmpty()){
	                    Downloader.enqueue(getApplicationContext(), aid, item, handler);
	                    
	                }
	                else
	                    handler.sendEmptyMessage(DownloadHandler.DOWNLOAD_FAIL);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	    }.start();
    }
	private DownloadHandler handler = new DownloadHandler(){
	    
        @TargetApi(9)
	    public void handleMessage(Message msg) {
	        if(msg.what == DOWNLOAD_START){
	            Toast.makeText(AcApp.context(), "(^ω^) 开始下载", 0).show();

	            String vid = (String) msg.obj;
	            downloadService.doQueryStatus(vid);
	        }else if(msg.what == DOWNLOAD_FAIL){
	            Toast.makeText(getApplicationContext(), "(+﹏+) 可恶，下载失败！请换个姿势再来一次！", 0).show();
	        }
	    }
	};

	@Override
	public void onClick(View v) {
		
		switch ((Integer)v.getTag()) {
		case 100:
			v.setEnabled(false);
			loadData(aid);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        VideoItem item = data.get(position);
        startToPlay(item);
    }
	
	//TODO 
    public void startToPlay(VideoItem item){
        addToHistory();
		Intent intent = new Intent(DetailActivity.this, SectionActivity.class);
		intent.putExtra("item",item);
		startActivity(intent);
	}
    /**
     * 下载事件 接收
     */
    private BroadcastReceiver onProgress = new BroadcastReceiver(){

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DownloadService.ACTION_VIEW_PROGRESS)){
                Bundle bundle = intent.getExtras();
                int progress = bundle.getInt("progress");
                int total = bundle.getInt("total");
                Log.i(TAG, "recieve progress: " + progress + "/"+ total);
                String vid = bundle.getString("vid");
                if(adapter !=null)
                    adapter.setProgress(vid,progress,total);
            }
        }
        
    };
    private BroadcastReceiver onDownloadFail = new BroadcastReceiver(){

        @TargetApi(Build.VERSION_CODES.GINGERBREAD)
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DownloadService.ACTION_DOWNLOAD_FAIL)){
                String vid = intent.getExtras().getString("vid");
                adapter.hideProgress(true, vid);
                
            }
        }
    };
    private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(DownloadService.ACTION_DOWNLOAD_SUCCESS)){
                String vid = intent.getExtras().getString("vid");
                adapter.hideProgress(false, vid);
                //TODO 修改下载按钮状态
                
            }
        }
    };
    private LayoutInflater mInflater;
    private View loadView;
    private View btnComment;
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try{
            unbindService(conn);
        }catch (Exception e) {
        }
    }

    private Intent createShareIntent() {
        String shareurl = title+"http://www.acfun.tv/v/ac"+aid;
        Intent shareIntent = new Intent(Intent.ACTION_SEND);  
        shareIntent.setType("text/plain");  
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "分享");  
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareurl);  
        return shareIntent;
    }
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    public void addToHistory(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
        new DBService(this).addtoHis(aid, title, sdf.format(new Date()), 0, channelid);
        isfavorite = new DBService(this).isFoved(aid);
    }
    public void setDescription(TextView text){
        Pattern wiki = Pattern.compile("\\[wiki([^\\[]+)\\]",Pattern.CASE_INSENSITIVE);
        CharSequence localDesc = StringUtil.getSource(description);
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
    
}
