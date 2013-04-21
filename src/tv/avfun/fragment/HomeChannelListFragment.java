package tv.avfun.fragment;

import tv.avfun.Channel_Activity;
import tv.avfun.Detail_Activity;
import tv.avfun.R;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Banner;
import tv.avfun.api.Channel;
import tv.avfun.entity.Contents;
import tv.avfun.util.DataStore;
import tv.avfun.util.DensityUtil;
import tv.avfun.util.MyAsyncTask;
import tv.avfun.util.NetWorkUtil;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 列表模式的首页
 * @author Yrom
 *
 */
public class HomeChannelListFragment extends Fragment implements View.OnClickListener {

    protected static final String TAG        = HomeChannelListFragment.class.getSimpleName();
    protected static final int    ADD        = 1;
    private View                  mView;
    private DataStore             dataStore;
    private LinearLayout          channelList;
    private View                  headerView;
    private ViewPager             banner;
    private int                   bannerCount;
    private int                   bannerViewIndex;
    private BannerIndicator       bannerIndicator;
    private LayoutInflater        mInflater;
    private Banner[]              banners;
    private Channel[]             channels;
    private View                  loadView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataStore = DataStore.getInstance();
        setHasOptionsMenu(false);
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == ADD)
                addChannelItem(msg.arg1);
        }
    };

    private void loadData() {
        new MyAsyncTask() {
            @Override
            public void doInBackground() {
                if (dataStore.isChannelListCached()) 
                    channels = dataStore.loadChannelList();
                else{
                    channels = ApiParser.getRecommendChannels(3);
                    if (channels != null)
                        DataStore.getInstance().saveChannelList(channels);
                    else
                        // 读取数据不成功，再次尝试读取缓存
                        channels = dataStore.loadChannelList();
                }
                boolean b = false;
                if (b = channels != null)
                    for (int i=0; i< channels.length; i++){
                        Message msg = Message.obtain(handler);
                        msg.what =ADD;
                        msg.arg1 = i;
                        msg.sendToTarget();
                    }
                    
                publishResult(b);
            }

            @Override
            public void onPublishResult(boolean succeeded) {
                if (!succeeded) {
                    NetWorkUtil.showNetWorkError(getActivity());
                    if (channels == null) {
                        TextView tips = new TextView(getActivity());
                        tips.setText(R.string.net_work_error);
                        tips.setTextColor(0x88000000);
                        tips.setTextSize(DensityUtil.dip2px(getActivity(), 13));
                        channelList.removeAllViews();
                        channelList.addView(tips);
                    }
                }
            }
        }.execute();
    }

    

    private void initView() {
        // header (banner)
        if (this.banners != null){
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

    public View findViewById(int id) {
        if (mView != null)
            return mView.findViewById(id);
        return null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.list_home_layout, container, false);
        mInflater = inflater;
        channelList = (LinearLayout)findViewById(R.id.channel_list);
        loadView = findViewById(R.id.load_view);
        
        loadData();
        
        initView();

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
    protected void addChannelItem(int position) {

        LinearLayout channelItem = (LinearLayout) mInflater.inflate(R.layout.home_channel_item,
                channelList, false);
        VideoItemView left = (VideoItemView) channelItem.findViewById(R.id.row_left);
        VideoItemView mid = (VideoItemView) channelItem.findViewById(R.id.row_middle);
        VideoItemView right = (VideoItemView) channelItem.findViewById(R.id.row_right);
        left.setOnClickListener(this.listener);
        mid.setOnClickListener(this.listener);
        right.setOnClickListener(this.listener);
        TextView title = (TextView) channelItem.findViewById(R.id.channel_title);
        
        View more = channelItem.findViewById(R.id.more);
        more.setOnClickListener(this);
        more.setTag(position);

        this.channelList.addView(channelItem);
        Channel channel = this.channels[position];
        left.setContents(channel.recommends.get(0));
        mid.setContents(channel.recommends.get(1));
        right.setContents(channel.recommends.get(2));
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
        Intent intent = new Intent(getActivity(),Channel_Activity.class);
        intent.putExtra("position", position);
        startActivity(intent);
    }
}
