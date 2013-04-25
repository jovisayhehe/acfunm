package tv.avfun.fragment;

import java.text.DateFormat;

import tv.avfun.BuildConfig;
import tv.avfun.Channel_Activity;
import tv.avfun.Detail_Activity;
import tv.avfun.R;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Banner;
import tv.avfun.api.Channel;
import tv.avfun.app.AcApp;
import tv.avfun.entity.Contents;
import tv.avfun.util.DataStore;
import tv.avfun.view.BannerIndicator;
import tv.avfun.view.VideoItemView;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.ILoadingLayout;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;

/**
 * 列表模式的首页
 * 
 * @author Yrom
 * 
 */
public class HomeChannelListFragment extends Fragment implements VideoItemView.OnClickListener {

    private static final String     TAG       = HomeChannelListFragment.class.getSimpleName();
    private static final int        ADD       = 1;
    private static final int        REFRESH   = 2;
    private static final int        HIDE_INFO = 4;
    private static final long       LOCK_TIME = 5 * 60000; 
    private String                  mode;
    private long                    updatedTime;
    private View                    mView;
    private DataStore               dataStore;
    private LinearLayout            channelList;
    private View                    headerView;
    private ViewPager               banner;
    private int                     bannerCount;
    private int                     bannerViewIndex;
    private BannerIndicator         bannerIndicator;
    private LayoutInflater          mInflater;
    private Banner[]                banners;
    private Channel[]               channels;
    private View                    loadView;
    private ILoadingLayout          mLoadingLayout;
    private TextView                updateInfo, timeOutView;
    private PullToRefreshScrollView mPtr;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataStore = DataStore.getInstance();
        setHasOptionsMenu(false);
        mode = AcApp.getConfig().getString("home_display_mode", "1");
    }

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == ADD)
                addChannelItem(msg.arg1);
            else if (msg.what == REFRESH)
                channelList.removeAllViews();
            else if (msg.what == HIDE_INFO)
                hideUpdateInfo();
        }
    };

    /** 0为当前时间 */
    private void setLastUpdatedLabel(long updatedTime) {
        if (updatedTime == 0)
            updatedTime = System.currentTimeMillis();
        else if (updatedTime < 0)
            return;
        this.updatedTime = updatedTime;
        String update = DateFormat.getDateTimeInstance().format(updatedTime);
        mLoadingLayout.setLastUpdatedLabel("上次更新:" + update);
    }

    private void initView() {
        // header (banner)
        if (this.banners != null) {
            headerView = mInflater.inflate(R.layout.home_banner_view, channelList, false);
            banner = (ViewPager) headerView.findViewById(R.id.banner);
            bannerIndicator = (BannerIndicator) headerView.findViewById(R.id.banner_indicator);
            // TODO: set banner adapter
            this.bannerCount = 0;
            this.bannerViewIndex = -1;
            this.bannerCount = this.banners.length;
            this.bannerIndicator.setIndicatorNum(this.bannerCount);
            this.channelList.addView(this.headerView);
        }
    }

    private Animation fadeIn;
    private Animation fadeOut;
    private long showDuration = 2000;

    private void initFadeAnim() {

        fadeIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in);
        fadeIn.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {}

            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                handler.sendMessageDelayed(Message.obtain(handler, HIDE_INFO), showDuration);
            }
        });

        fadeOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out);
        fadeOut.setAnimationListener(new AnimationListener() {

            public void onAnimationStart(Animation animation) {}

            public void onAnimationRepeat(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                updateInfo.setVisibility(View.GONE);
            }
        });
    }

    private class RefreshData extends AsyncTask<Void, Void, Boolean> {

        @Override
        public void onPreExecute() {
            if (System.currentTimeMillis() - updatedTime < LOCK_TIME) {
                this.cancel(false);
                updateInfo.setText(getString(R.string.update_lock));
                showUpdateInfo();
                mPtr.onRefreshComplete();
            }
            timeOutView.setVisibility(View.GONE);

        }

        protected Boolean doInBackground(Void... params) {
            Channel[] cs = ApiParser.getRecommendChannels(3, mode);
            if (cs != null) {
                channels = cs;
                handler.sendEmptyMessage(REFRESH);
                updateList();
                return DataStore.getInstance().saveChannelList(channels);
            } else
                return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            showUpdateInfo();
            mPtr.onRefreshComplete();
        }

    }
    private void loadData() {
        new LoadData().execute();
    }
    
    private class LoadData extends AsyncTask<Void,Long,Boolean>{
        boolean isCached;
        @Override
        protected void onPreExecute() {
            
            if(!(isCached = dataStore.isChannelListCached())){
                if(BuildConfig.DEBUG) Log.i(TAG, "loading new data");
                showLoadingView();
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            showUpdateInfo();
            hideLoadingView();
            if(!result) showTimeOutView();
            else setLastUpdatedLabel(0);
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            if(isCached){
                if(BuildConfig.DEBUG) Log.i(TAG, "read channel list cache");
                updateInfo.setText(getString(R.string.read_cache));
                if(!readCache()) return false;
            }else{
                Channel[] recommendChannels = ApiParser.getRecommendChannels(3, mode);
                if(recommendChannels == null){
                    updateInfo.setText(getString(R.string.update_fail));
                    // 获取不到在线数据，尝试读缓存
                    if(!readCache()) {
                        updateInfo.setText(R.string.update_error);
                        return false;
                    }
                }else{
                    updateInfo.setText(getString(R.string.update_success));
                    channels = recommendChannels;
                    dataStore.saveChannelList(channels);
                    publishProgress(System.currentTimeMillis());
                }
            }
            updateList();
            return true;
        }
        @Override
        protected void onProgressUpdate(Long... values) {
            setLastUpdatedLabel(values[0]);
        }
        boolean readCache(){
            Channel[] cachedList = dataStore.loadChannelList();
            if(cachedList != null){
                
                channels = cachedList;
                publishProgress(dataStore.getChannelListLastUpdateTime());
                return true;
            }
            else 
                return false;
        }
    }
    public View findViewById(int id) {
        if (mView != null)
            return mView.findViewById(id);
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.list_home_layout, container, false);
        mInflater = inflater;
        channelList = (LinearLayout) findViewById(R.id.channel_list);
        loadView = findViewById(R.id.load_view);
        updateInfo = (TextView) findViewById(R.id.update_info);
        timeOutView = (TextView) findViewById(R.id.time_out_text);
        initView();
        initFadeAnim();
        mPtr = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
        
        mPtr.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                new RefreshData().execute();
            }

        });
        mLoadingLayout = mPtr.getLoadingLayoutProxy();
        mLoadingLayout.setRefreshingLabel(getString(R.string.refreshing));
        mLoadingLayout.setPullLabel(getString(R.string.pull_refresh));
        mLoadingLayout.setReleaseLabel(getString(R.string.release_refresh));
        return mView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }
    
    private void showLoadingView() {
        if (loadView != null) {
            loadView.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingView() {
        if (loadView != null && loadView.getVisibility() == View.VISIBLE) {
            loadView.setVisibility(View.GONE);
        }
    }

    @SuppressWarnings("deprecation")
    private void addChannelItem(int position) {

        LinearLayout channelItem = (LinearLayout) mInflater.inflate(R.layout.home_channel_item,
                channelList, false);
        VideoItemView left = (VideoItemView) channelItem.findViewById(R.id.row_left);
        VideoItemView mid = (VideoItemView) channelItem.findViewById(R.id.row_middle);
        VideoItemView right = (VideoItemView) channelItem.findViewById(R.id.row_right);
        TextView title = (TextView) channelItem.findViewById(R.id.channel_title);
        left.setOnClickListener(this);
        mid.setOnClickListener(this);
        right.setOnClickListener(this);

        View more = channelItem.findViewById(R.id.more);
        more.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                int position = (Integer) v.getTag();
                Intent intent = new Intent(getActivity(), Channel_Activity.class);
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
        more.setTag(position);

        this.channelList.addView(channelItem);
        Channel channel = this.channels[position];
        left.setContents(channel.contents.get(0));
        mid.setContents(channel.contents.get(1));
        right.setContents(channel.contents.get(2));
        title.setText(channel.getTitle());

        // 不应该出现！
        if (channel.titleBgResId == 0) {
            channel.titleBgResId = R.drawable.title_bg_none;
        }
        title.setBackgroundDrawable(getResources().getDrawable(channel.titleBgResId));
    }

    @Override
    public void onClick(View view, Contents c) {

        Intent intent = new Intent(getActivity(), Detail_Activity.class);
        intent.putExtra("contents", c);
        startActivity(intent);
    }

    private void updateList() {
        for (int i = 0; i < channels.length; i++) {
            Message msg = Message.obtain(handler);
            msg.what = ADD;
            msg.arg1 = i;
            msg.sendToTarget();
        }
    }

    private void showUpdateInfo() {
        updateInfo.setVisibility(View.VISIBLE);
        updateInfo.startAnimation(fadeIn);
    }

    private void hideUpdateInfo() {
        updateInfo.startAnimation(fadeOut);
    }

    private void showTimeOutView() {
        timeOutView.setText(getString(R.string.update_fail));
        timeOutView.setVisibility(View.VISIBLE);
    }
}
