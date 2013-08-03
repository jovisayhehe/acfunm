package tv.avfun.fragment;

import java.text.DateFormat;

import tv.ac.fun.BuildConfig;
import tv.ac.fun.R;
import tv.avfun.ChannelActivity;
import tv.avfun.DetailActivity;
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
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
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
public class HomeChannelListFragment extends BaseFragment implements VideoItemView.OnClickListener, OnNavigationListener {

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
    private ActionBar mBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataStore = DataStore.getInstance();
        setHasOptionsMenu(false);
        mBar  = getSherlockActivity().getSupportActionBar();
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(mBar.getThemedContext(), R.array.pref_entries_home_display_mode,
                android.R.layout.simple_spinner_item/*R.layout.sherlock_spinner_item*/);
        adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        mBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);
        mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mBar.setListNavigationCallbacks(adapter, this);
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
        mPtr = (PullToRefreshScrollView) findViewById(R.id.pull_refresh_scrollview);
        
        mPtr.setOnRefreshListener(new OnRefreshListener<ScrollView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                new RefreshData().execute();
            }

        });
        mLoadingLayout = mPtr.getLoadingLayoutProxy();
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mLoadingLayout.setRefreshingLabel(activity.getString(R.string.refreshing));
        mLoadingLayout.setPullLabel(activity.getString(R.string.pull_refresh));
        mLoadingLayout.setReleaseLabel(activity.getString(R.string.release_refresh));
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
                if(isInfoShow) isInfoShow = false;
                updateInfo.setVisibility(View.GONE);
            }
        });
    }

    private class RefreshData extends AsyncTask<Void, Void, Boolean> {

        @Override
        public void onPreExecute() {
            mPtr.setRefreshing();
            mode = AcApp.getHomeDisplayMode();
            if (!DataStore.getInstance().isDisplayModeChanged())            // 显示模式没有改变
            if (System.currentTimeMillis() - updatedTime < LOCK_TIME) {     // 刷新间隔小于锁定时间
                this.cancel(true);
                updateInfo.setText(activity.getString(R.string.update_lock));
                showUpdateInfo();
            }
            timeOutView.setVisibility(View.GONE);
        }

        protected Boolean doInBackground(Void... params) {
            Channel[] cs = ApiParser.getRecommendChannels(3, mode);
            isUpdated = false;
            if (cs != null) {
                channels = cs;
                handler.sendEmptyMessage(REFRESH);
                if(isAdded())
                    updateList();
                return DataStore.getInstance().saveChannelList(channels);
            } else
                return false;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                updateInfo.setText(activity.getString(R.string.update_success));
                setLastUpdatedLabel(0);
                if(isAdded() && !isUpdated){
                    updateList(); 
                }
            }
            else updateInfo.setText(activity.getString(R.string.update_fail));
            showUpdateInfo();
        }

    }
    private void loadData() {
        
        new LoadData().execute();
    }
    /**
     * 加载缓存。刷新数据的操作交由 {@link RefreshData}来做
     */
    private class LoadData extends AsyncTask<Void,Long,Boolean>{
        boolean isCached;
        @Override
        protected void onPreExecute() {
            long cachedTime = dataStore.getChannelListLastUpdateTime(); 
            isCached = cachedTime == -1? false: true;
            mBar.setSelectedNavigationItem(Integer.parseInt(AcApp.getHomeDisplayMode())-1);
            setLastUpdatedLabel(cachedTime);
            if(!isCached){
                this.cancel(true);
                new RefreshData().execute();
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result && !isUpdated)
                updateList();
            updateInfo.setText(activity.getString(R.string.read_cache));
            showUpdateInfo();
            if (System.currentTimeMillis() - updatedTime > LOCK_TIME) {
                if(BuildConfig.DEBUG) Log.i(TAG, "going to refresh data");
                new RefreshData().execute();
            }
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            
            if (BuildConfig.DEBUG)
                Log.i(TAG, "try to read channel list cache ");
            if (readCache()) {
                if(isAdded())
                    
                    updateList();
                return true;
            }
            return false;
        }
        boolean readCache(){
            if(isCached){
                Channel[] cachedList = dataStore.loadChannelList();
                if(cachedList != null){
                    channels = cachedList;
                    return true;
                }
            }
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
        return mView;
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
                Intent intent = new Intent(getActivity(), ChannelActivity.class);
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
        title.setBackgroundDrawable(activity.getResources().getDrawable(channel.titleBgResId));
    }

    @Override
    public void onClick(View view, Contents c) {

        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("contents", c);
        startActivity(intent);
    }
    private boolean isUpdated =false;
    private void updateList() {
        for (int i = 0; i < channels.length; i++) {
            Message msg = Message.obtain(handler);
            msg.what = ADD;
            msg.arg1 = i;
            msg.sendToTarget();
        }
        
        isUpdated = true;
    }
    private boolean isInfoShow;
    private void showUpdateInfo() {
        if(!isInfoShow){
            isInfoShow = true;
            updateInfo.setVisibility(View.VISIBLE);
            updateInfo.startAnimation(fadeIn);
        }
        mPtr.onRefreshComplete();
    }

    private void hideUpdateInfo() {
        
        updateInfo.startAnimation(fadeOut);
    }

    private void showTimeOutView() {
        timeOutView.setText(getString(R.string.update_fail));
        timeOutView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onShow() {
        loadData();
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Log.i(TAG, String.format("positoin = %d, item id = %d", itemPosition, itemId));
        if(!AcApp.getHomeDisplayMode().equals(String.valueOf(itemPosition+1))){
            AcApp.putString("home_display_mode",String.valueOf(itemPosition+1));
            new RefreshData().execute();
            return true;
        }
        return  false;
    }
}
