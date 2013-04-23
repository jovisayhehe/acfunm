package tv.avfun.fragment;

import java.text.DateFormat;

import tv.avfun.Channel_Activity;
import tv.avfun.Detail_Activity;
import tv.avfun.R;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Banner;
import tv.avfun.api.Channel;
import tv.avfun.app.AcApp;
import tv.avfun.entity.Contents;
import tv.avfun.util.DataStore;
import tv.avfun.util.MyAsyncTask;
import tv.avfun.view.BannerIndicator;
import tv.avfun.view.VideoItemView;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
public class HomeChannelListFragment extends Fragment implements View.OnClickListener {

    private static final String     TAG       = HomeChannelListFragment.class.getSimpleName();
    private static final int        ADD       = 1;
    private static final int        REFRESH   = 2;
    private static final int        HIDE_INFO = 4;
    private static final long       LOCK_TIME = 5*60000; // 5 min
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
        mode = AcApp.instance().getConfig().getString("home_display_mode", "1");
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

    private void loadData() {
        new MyAsyncTask() {
            boolean isCached = dataStore.isChannelListCached();
            protected void onPreExecute() {
                if(!isCached)
                    showLoadingView();
            }
            protected void onPostExecute() {
                hideLoadingView();
            }
            @Override
            public void doInBackground() {
                if (isCached)
                    channels = dataStore.loadChannelList();
                else {
                    channels = ApiParser.getRecommendChannels(3, mode);
                    if (channels != null)
                        DataStore.getInstance().saveChannelList(channels);
                    else
                        // 读取数据不成功，再次尝试读取缓存
                        channels = dataStore.loadChannelList();
                }
                boolean b = false;
                if (b = channels != null)
                    updateList();
                publishResult(b);
            }

            @Override
            public void onPublishResult(boolean succeeded) {
                if (!succeeded) {
                    if (channels == null) {
                        updateInfo.setText(getString(R.string.net_work_error));
                        showUpdateInfo();
                        showTimeOutView();
                    }
                }
                setLastUpdatedLabel(dataStore.getChannelListLastUpdateTime());
            }
        }.execute();
    }

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

    private class RefreshData extends MyAsyncTask {
        @Override
        protected void onPreExecute() {
            if(System.currentTimeMillis() - updatedTime < LOCK_TIME){
                this.cancel();
                updateInfo.setText(getString(R.string.update_lock));
                showUpdateInfo();
                mPtr.onRefreshComplete();
            }
            timeOutView.setVisibility(View.GONE);
                
        }
        @Override
        public void doInBackground() {
            Channel[] cs = ApiParser.getRecommendChannels(3, mode);
            if (cs != null) {
                channels = cs;
                publishResult(DataStore.getInstance().saveChannelList(channels));
                handler.sendEmptyMessage(REFRESH);
                updateList();
            } else
                publishResult(false);

        }

        public void onPublishResult(boolean succeeded) {
            if (succeeded) {
                setLastUpdatedLabel(0);
                updateInfo.setText(getString(R.string.update_success));
            } else
                updateInfo.setText(getString(R.string.update_fail));
            showUpdateInfo();
            mPtr.onRefreshComplete();
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
        loadData();

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
        left.setOnClickListener(this.listener);
        mid.setOnClickListener(this.listener);
        right.setOnClickListener(this.listener);

        View more = channelItem.findViewById(R.id.more);
        more.setOnClickListener(this);
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

    private VideoItemView.OnClickListener listener = new VideoItemView.OnClickListener() {

        @Override
        public void onClick(View view, Contents c) {
            Toast.makeText(getActivity(), c.getTitle(), 0).show();
            Intent intent = new Intent(getActivity(), Detail_Activity.class);
            intent.putExtra("contents", c);
            startActivity(intent);
        }
    };


    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag();
        Intent intent = new Intent(getActivity(), Channel_Activity.class);
        intent.putExtra("position", position);
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
