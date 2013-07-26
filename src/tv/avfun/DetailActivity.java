
package tv.avfun;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.ac.fun.BuildConfig;
import tv.ac.fun.R;
import tv.avfun.adapter.DetailAdaper;
import tv.avfun.adapter.DetailAdaper.OnStatusClickListener;
import tv.avfun.api.ApiParser;
import tv.avfun.api.MemberUtils;
import tv.avfun.api.net.UserAgent;
import tv.avfun.app.AcApp;
import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
import tv.avfun.entity.VideoInfo;
import tv.avfun.entity.VideoPart;
import tv.avfun.util.ArrayUtil;
import tv.avfun.util.NetWorkUtil;
import tv.avfun.util.StringUtil;
import tv.avfun.util.download.DownloadEntry;
import tv.avfun.util.download.DownloadManager;
import tv.avfun.util.lzlist.ImageLoader;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.util.Linkify;
import android.text.util.Linkify.TransformFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.ShareActionProvider;
import com.umeng.analytics.MobclickAgent;

public class DetailActivity extends SherlockActivity implements OnItemClickListener, OnClickListener {

    private static final String TAG        = DetailActivity.class.getSimpleName();
    private static final String LOADING    = "正在加载...";
    private static final int    TAG_RELOAD = 100;
    private static final int    TAG_PLAY   = 200;
    private Intent              mIntent;
    /**
     * 来自：0 首页 1历史、搜索 2 Action_View av://12345
     */
    private int                 from;
    private ImageLoader         mImgLoader;
    private TextView            tvTitle, tvViews, tvComments;
    private TextView            tvUserName, tvBtnPlay, tvDesc;
    private ImageView           ivTitleImg;
    private String              aid;
    private String              title;
    private int                 channelid;
    private String              description;
    private ListView            mListView;
    private List<VideoPart>     mData;
    private DetailAdaper        mAdapter;
    private LayoutInflater      mInflater;
    private View                mLoadView;
    private boolean             isFavorite;
    private VideoInfo           mVideoInfo;
    private DownloadManager     mDownloadManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        
        mDownloadManager = AcApp.instance().getDownloadManager();
        mIntent = getIntent();
        if (Intent.ACTION_VIEW.equals(mIntent.getAction())) {
            from = "av".equalsIgnoreCase(mIntent.getScheme()) ? 2 : 0;
            if (BuildConfig.DEBUG)
                Log.i(TAG, "看av: " + mIntent.getDataString());
        } else {
            from = mIntent.getIntExtra("from", 0);
        }
        mImgLoader = ImageLoader.getInstance();
        MobclickAgent.onEvent(this, "view_detail");
        initBar();
        initview();
        loadData();
    }

    private void initBar() {
        getSupportActionBar().setBackgroundDrawable(this.getResources().getDrawable(R.drawable.ab_transparent));
        Drawable bg = getResources().getDrawable(R.drawable.border_bg);
        getSupportActionBar().setSplitBackgroundDrawable(bg);
    }

    private void loadData() {
        new RequestDetailTask().execute();
    }

    private class RequestDetailTask extends AsyncTask<Void, Void, Boolean> {

        private View     progress;
        private TextView text;

        @Override
        protected void onPreExecute() {
            progress = mLoadView.findViewById(R.id.list_loadview_progress);
            progress.setVisibility(View.VISIBLE);
            text = (TextView) mLoadView.findViewById(R.id.list_loadview_text);
            text.setText(R.string.loading);
            mLoadView.setVisibility(View.VISIBLE);

            mListView.setVisibility(View.INVISIBLE);

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                DownloadManager man = AcApp.instance().getDownloadManager();
                List<VideoPart> downloadParts = man.getVideoParts(aid);
                
                if (NetWorkUtil.isNetworkAvailable(getApplicationContext())) {
                    mVideoInfo = ApiParser.getVideoInfoByAid(aid);
                    if(mVideoInfo == null) return false;
                    mData.addAll(mVideoInfo.parts);
                }
                if(downloadParts != null){
                    for(VideoPart part: downloadParts){
                        mData.remove(part); // 过滤
                    }
                    mData.addAll(downloadParts);
                }

            } catch (Exception e) {
                // TODO 向umeng 发送自定义事件: aid获取失败
                if (BuildConfig.DEBUG)
                    Log.w(TAG, "获取数据出错！" + aid, e);
            }

            return ArrayUtil.validate(mData);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                if (from > 0) {
                    tvUserName.setText(mVideoInfo.upman == null ? "无名氏" : mVideoInfo.upman);
                    tvViews.setText(mVideoInfo.views + "");
                    tvComments.setText(mVideoInfo.comments + "");
                    description = mVideoInfo.description;
                    channelid = mVideoInfo.channelId;
                    title = mVideoInfo.title;
                    tvTitle.setText(title);
                    String imgurl = mVideoInfo.titleImage;
                    if (StringUtil.validate(imgurl)) {
                        mImgLoader.displayImage(imgurl, ivTitleImg);
                    } else {
                        ivTitleImg.setBackgroundResource(R.drawable.no_picture);
                    }
                }
                setDescription(tvDesc);
                tvBtnPlay.setText("播放");
                tvBtnPlay.setOnClickListener(DetailActivity.this);
                mListView.setVisibility(View.VISIBLE);
                mLoadView.setVisibility(View.GONE);
                mAdapter.setData(mData);
            } else {
                progress.setVisibility(View.GONE);
                text.setText(R.string.reloading);
                mLoadView.setTag(TAG_RELOAD);
                mLoadView.setEnabled(true);
                mLoadView.setOnClickListener(DetailActivity.this);
            }

        }
    }

    private void initview() {
        setContentView(R.layout.detail_layout);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tvTitle = (TextView) findViewById(R.id.detail_title);
        tvUserName = (TextView) findViewById(R.id.detail_usename);
        tvViews = (TextView) findViewById(R.id.detail_views);
        tvComments = (TextView) findViewById(R.id.detail_comment);
        ivTitleImg = (ImageView) findViewById(R.id.detail_img);
        tvBtnPlay = (TextView) findViewById(R.id.detail_play_btn);
        tvBtnPlay.setTag(TAG_PLAY);
        tvBtnPlay.setOnClickListener(this);
        tvDesc = (TextView) findViewById(R.id.detail_desc);
        if (from == 2) {
            // av://ac000000
            aid = mIntent.getDataString().substring(7);
        } else {
            Contents c = (Contents) mIntent.getExtras().get("contents");
            if (c == null)
                throw new IllegalArgumentException("你从异次元来的吗？");
            title = StringUtil.getSource(c.getTitle());
            aid = c.getAid();
            channelid = c.getChannelId();
            mImgLoader.displayImage(c.getTitleImg(), ivTitleImg);
            tvUserName.setText(c.getUsername());
            tvViews.setText(String.valueOf(c.getViews()));
            tvComments.setText(String.valueOf(c.getComments()));
            description = c.getDescription();
            if(!StringUtil.validate(description))
                tvDesc.setText(LOADING);
            else setDescription(tvDesc);
            tvBtnPlay.setText(LOADING);
            tvTitle.setText(c.getTitle());
        }
        getSupportActionBar().setTitle("ac" + aid);
        isFavorite = new DBService(this).isFaved(aid);
        if (from > 0) {
            ivTitleImg.setBackgroundResource(R.drawable.no_picture);
            tvUserName.setText(LOADING);
            tvViews.setText(LOADING);
            tvComments.setText(LOADING);
            tvBtnPlay.setText(LOADING);
            tvDesc.setText(LOADING);
        }
        mListView = (ListView) findViewById(R.id.detail_listview);
        mInflater = LayoutInflater.from(DetailActivity.this);
        mData = new ArrayList<VideoPart>();
        mAdapter = new DetailAdaper(mInflater, mData);
        mAdapter.setOnStatusClickListener(slistener);
        mListView.setAdapter(mAdapter);
        mListView.setDuplicateParentStateEnabled(true);
        mListView.setOnItemClickListener(this);
        mLoadView = findViewById(R.id.load_view);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // 播放
        VideoPart item = (VideoPart) parent.getItemAtPosition(position);
        startPlay(item);
    }

    private void startPlay(VideoPart item) {
        addToHistory();
        Intent intent = new Intent(DetailActivity.this, SectionActivity.class);
        intent.putExtra("item", item);
        startActivity(intent);
    }

    private void addToHistory() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
        new DBService(this).addtoHis(aid, title, sdf.format(new Date()), 0, channelid);
    }

    @Override
    public void onClick(View v) {
        Object o = v.getTag();
        if (o == null)
            return;
        switch (((Integer) o).intValue()) {
        // 播放按钮
        case TAG_PLAY:
            if (mData.isEmpty())
                return;
            startPlay(mData.get(0));
            MobclickAgent.onEvent(this,"play");
            break;

        // 重新加载
        case TAG_RELOAD:
            v.setEnabled(false);
            loadData();
            break;
            
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.share_action_provider, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share_action_provider_action_bar);
        ShareActionProvider shareProvider = (ShareActionProvider) shareItem.getActionProvider();
        // shareProvider.setShareHistoryFileName(ShareActionProvider.DEFAULT_SHARE_HISTORY_FILE_NAME);
        shareProvider.setShareHistoryFileName(null);
        shareProvider.setShareIntent(createShareIntent());
        if (isFavorite) {
            menu.findItem(R.id.menu_item_fov_action_provider_action_bar).setIcon(R.drawable.rating_favorite_p);
        }
        AcApp.addSearchView(this, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            this.finish();
            break;
        case R.id.menu_item_share_action_provider_action_bar:
            MobclickAgent.onEvent(DetailActivity.this,"share");
            break;
        case R.id.menu_item_fov_action_provider_action_bar:
            handleFavorite(item);
            break;
        case R.id.menu_item_comment:
            Intent intent = new Intent(DetailActivity.this, CommentsActivity.class);
            intent.putExtra("aid", aid);
            startActivity(intent);
            MobclickAgent.onEvent(DetailActivity.this,"view_comment");
            break;
        }
        return true;
    }

    private void handleFavorite(MenuItem item) {
        if (isFavorite) {
            new DBService(this).delFav(aid);
            isFavorite = false;
            item.setIcon(R.drawable.rating_favorite);
            Toast.makeText(this, "取消成功", Toast.LENGTH_SHORT).show();
            if(AcApp.instance().isLogin()){
                new Thread() {
                    public void run() {
                        MemberUtils.deleteFavourite(aid, AcApp.instance().getCookies());
                    }
                }.start();
            }
            MobclickAgent.onEvent(DetailActivity.this,"delete_favorite");
        } else {
            new DBService(this).addtoFav(aid, title, 0, channelid);
            isFavorite = true;
            item.setIcon(R.drawable.rating_favorite_p);
            Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show();
            if(AcApp.instance().isLogin()){
                new Thread() {
                    public void run() {
                        MemberUtils.addFavourite(aid, AcApp.instance().getCookies());
                    }
                }.start();
            }
            MobclickAgent.onEvent(DetailActivity.this,"add_favorite");
        }
    }

    public void setDescription(TextView text) {
        Pattern wiki = Pattern.compile("\\[wiki([^\\[]+)\\]", Pattern.CASE_INSENSITIVE);
        String localDesc = StringUtil.getSource(description);
        text.setText(Html.fromHtml(localDesc));
        Linkify.addLinks(text, wiki, null, null, new TransformFilter() {

            @Override
            public String transformUrl(Matcher match, String url) {
                String t = match.group(1);
                return "http://wiki.acfun.tv/index.php/" + t;
            }
        });
        Pattern http = Pattern.compile("(http://(?:[a-z0-9.-]+[.][a-z]{2,}+(?::[0-9]+)?)(?:/[^\\s\u3010\u4e00-\u9fa5]*)?)",
                Pattern.CASE_INSENSITIVE);
        Linkify.addLinks(text, http, "http://");
        Linkify.addLinks(text, Pattern.compile("(ac\\d{5,})", Pattern.CASE_INSENSITIVE), "av://");
    }

    private Intent createShareIntent() {
        String shareurl = title + "http://www.acfun.tv/v/ac" + aid;
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

    private OnStatusClickListener slistener = new OnStatusClickListener() {

        @Override
        public void doViewDownloadInfo(String vid) {
            startActivity(new Intent(DetailActivity.this,DownloadManActivity.class));
        }

        @Override
        public void doStartDownload(final VideoPart item) {
            final DownloadEntry entry = new DownloadEntry(aid,title,item);
            MobclickAgent.onEvent(DetailActivity.this,"download");
            if (item.segments == null || item.segments.isEmpty()) {
                new Thread() {

                    public void run() {
                        
                        ApiParser.parseVideoParts(item, AcApp.getParseMode());
                        if (item.segments == null || item.segments.isEmpty())
                            mHanlder.sendEmptyMessage(0);
                        else 
                            mHanlder.obtainMessage(1, entry).sendToTarget();
                    }
                }.start();
            }else
                mDownloadManager.download(entry);
        }

        private Handler mHanlder =  new Handler(){
            public void handleMessage(android.os.Message msg) {
                if(msg.what ==0 ){
                    AcApp.showToast(getString(R.string.download_fail));
                }else if(msg.what == 1){
                    DownloadEntry entry = (DownloadEntry) msg.obj;
                    String ua = null;
                    if("tudou".equals(entry.part.vtype))
                        ua = UserAgent.DEFAULT;
                    mDownloadManager.download(ua,entry);
                }
            }
        };
    };
}
