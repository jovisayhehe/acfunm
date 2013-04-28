
package tv.avfun.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tv.avfun.BuildConfig;
import tv.avfun.ChannelActivity;
import tv.avfun.DetailActivity;
import tv.avfun.R;
import tv.avfun.WebViewActivity;
import tv.avfun.adapter.ChannelContentListAdaper;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Channel;
import tv.avfun.app.AcApp;
import tv.avfun.entity.Contents;
import tv.avfun.util.DataStore;
import tv.avfun.util.NetWorkUtil;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChannelContentFragment extends Fragment implements OnItemClickListener, OnScrollListener, OnClickListener {

    private static final int         LOAD_ERROR    = 1;
    private static final int         LOAD_SUCCESS  = 2;
    private static final int         LOAD_TIME_OUT = 3;
    private static final String      TAG           = ChannelContentFragment.class.getSimpleName();
    public static final int          LOAD_FAIL     = 4;
    private Channel                  channel;
    private Activity                 activity;
    private View                     mView;
    private View                     mLoadView, mFootView;
    private int                      indexpage     = 1;
    private ListView                 mListView;
    private TextView                 mTimeOutView;
    private ProgressBar              mProgress;
    private ChannelContentListAdaper mAdapter;
    private List<Contents>           data;
    private LayoutInflater           mInflater;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        data = new ArrayList<Contents>(10);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        mView = inflater.inflate(R.layout.list_layout, null);
        mLoadView = findViewById(R.id.load_view);
        mTimeOutView = (TextView) findViewById(R.id.time_out_text);
        mTimeOutView.setOnClickListener(this);
        mProgress = (ProgressBar) findViewById(R.id.time_progress);
        initView();
        return mView;
    }

    private void initView() {
        mListView = (ListView) findViewById(android.R.id.list);
        mListView.setDivider(AcApp.getR().getDrawable(R.drawable.listview_divider));
        mListView.setDividerHeight(2);

        LinearLayout header = (LinearLayout) mInflater.inflate(R.layout.list_header, mListView, false);
        TextView headTitle = (TextView) header.findViewById(R.id.listheader_text);
        headTitle.setText("今日最热");
        mListView.addHeaderView(header);
        mListView.setHeaderDividersEnabled(false);

        mAdapter = new ChannelContentListAdaper(AcApp.context(), data);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        mFootView = mInflater.inflate(R.layout.list_footerview, mListView, false);
        mFootView.setVisibility(View.GONE);
        // mFootView.setOnClickListener(this);
        mFootView.setClickable(false);
        mListView.addFooterView(mFootView);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.activity = getActivity();
        this.channel = (Channel) getArguments().getSerializable("channel");
        loadData(1, false);
    }

    private void loadData(int page, boolean isAdd) {
        new RefreshDataTask(page, isAdd).execute();
    }

    public View findViewById(int id) {
        if (mView == null)
            return null;
        return mView.findViewById(id);
    }

    public static Fragment newInstance(Channel channel) {
        ChannelContentFragment fragment = new ChannelContentFragment();
        Bundle args = new Bundle();
        args.putSerializable("channel", channel);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        loadData(1, false);
    }

    private boolean isLoading   = false;
    private boolean isLoadError = false;
    private boolean isLoadFail  = false;

    /**
     * 刷新数据。异步任务
     */
    private class RefreshDataTask extends AsyncTask<Void, Void, Integer> {

        int     page;
        boolean isAdd;

        public RefreshDataTask(int page, boolean isAdd) {
            this.page = page;
            this.isAdd = isAdd;
        }

        @Override
        protected void onPreExecute() {
            isLoading = true;
            if (!isAdd) {
                mTimeOutView.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected Integer doInBackground(Void... params) {
            try {
                // 先尝试读在线数据
                List<Contents> templist = ApiParser.getChannelContents(channel.getUrl() + page);
                if (!isAdd) { // 第一次获取数据
                    List<Contents> hotlist = ApiParser.getChannelHotList(channel.channelId, 10);
                    if (hotlist != null)
                        data = hotlist;
                    // 获取失败定会抛出nullpointer
                    data.addAll(templist);
                } else { // 下拉到底部刷新数据
                    data.addAll(templist);
                }
                channel.contents = data;
                if (BuildConfig.DEBUG)
                    Log.d(TAG, "loaded data of channel " + channel.channelId);
                channel.pageIndex = indexpage;
                DataStore.saveChannel(channel); // 每次刷新都保存数据到缓存
                // 读取成功
                return LOAD_SUCCESS;
            } catch (Exception e) {
                if (BuildConfig.DEBUG)
                    Log.e(TAG, "failed to load data of channel " + channel.channelId, e);
                if (isAdd)
                    return LOAD_ERROR; // 下拉到底部拉取数据失败
                // 再尝试读缓存
                File file = new File(DataStore.getChannelCacheFile(channel.channelId));
                if (file.exists() && file.length() > 0) {
                    List<Contents> contents = DataStore.getCachedChannel(channel.channelId).contents;
                    data = contents;
                    return LOAD_FAIL;
                } else
                    // 既没有缓存 也读不到数据，显示 timeout
                    return LOAD_TIME_OUT;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            mProgress.setVisibility(View.GONE);
            TextView info = (TextView) mFootView.findViewById(R.id.list_footview_text);
            switch (result.intValue()) {
            case LOAD_TIME_OUT: // 超时

                mTimeOutView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.INVISIBLE);
                break;
            case LOAD_FAIL: // 读取在线数据失败，但有缓存
                mListView.setVisibility(View.VISIBLE);
                mAdapter.setData(data);
                mAdapter.notifyDataSetChanged();
                mFootView.setVisibility(View.VISIBLE);
                mFootView.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
                Toast.makeText(activity, activity.getString(R.string.update_fail), 0).show();
                info.setText(R.string.reloading);
                isLoadFail = true;
                isLoadError = false;
                break;
            case LOAD_SUCCESS:
                mFootView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                mAdapter.setData(data);
                mAdapter.notifyDataSetChanged();
                isLoading = false;
                isLoadError = false;
                isLoadFail = false;
                break;
            case LOAD_ERROR: // 下拉到底部拉取数据失败
                mFootView.setVisibility(View.VISIBLE);
                mFootView.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
                Toast.makeText(activity, activity.getString(R.string.update_error), 0).show();
                info.setText(R.string.reloading);
                isLoadError = true;
                isLoadFail = false;
                break;
            default:
                break;
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {

        int position = pos - 1;
        if (position == parent.getCount() - 2) {
            if (isLoadError) {
                mFootView.findViewById(R.id.list_footview_progress).setVisibility(View.VISIBLE);
                TextView textview = (TextView) mFootView.findViewById(R.id.list_footview_text);
                textview.setText(R.string.loading);
                loadData(indexpage, true);
            }else if(isLoadFail){
                mFootView.findViewById(R.id.list_footview_progress).setVisibility(View.VISIBLE);
                TextView textview = (TextView) mFootView.findViewById(R.id.list_footview_text);
                textview.setText(R.string.loading);
                indexpage  = 1;
                loadData(indexpage, false);
            }
        } else {
            Contents c = data.get(position);
            if (ChannelActivity.isarticle) {

                Intent intent = new Intent(activity, WebViewActivity.class);
                intent.putExtra("modecode", ChannelActivity.modecode);
                intent.putExtra("aid", c.getAid());
                intent.putExtra("title", c.getTitle());
                intent.putExtra("channelId", c.getChannelId() + ""); // int?
                startActivity(intent);

            } else {
                Intent intent = new Intent(activity, DetailActivity.class);
                intent.putExtra("contents", c);
                startActivity(intent);
            }

        }

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // 到底部 刷新数据 只有在成功获取在线数据后才生效
        if (view.getLastVisiblePosition() == (view.getCount() - 1) && !isLoading) {
            Log.i(TAG, "add data");
            loadData(++indexpage, true);
        }
    }
}
