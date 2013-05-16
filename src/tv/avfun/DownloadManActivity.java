package tv.avfun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.avfun.adapter.DetailAdaper;
import tv.avfun.app.AcApp;
import tv.avfun.util.download.DownloadJob;
import tv.avfun.util.download.DownloadManager;
import tv.avfun.util.download.DownloadManager.DownloadObserver;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;


@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class DownloadManActivity extends SherlockActivity implements OnNavigationListener {
    private static final String TAG = DownloadManActivity.class.getSimpleName();
    private DownloadManager mDownloadMan;
    private String[] mStateArray;
    private ListView mListView;
    private TextView mStateView;
    private ArrayAdapter<DownloadJob> mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        mStateArray = getResources().getStringArray(R.array.download_state);
        mDownloadMan = AcApp.instance().getDownloadManager();
        mDownloadMan.registerDownloadObserver(new DownloadObserver() {
            
            @Override
            public void onDownloadChanged(DownloadManager manager) {
                Log.d(TAG, "download changed");
                mAdapter.notifyDataSetChanged();
            }
        });
        SpinnerAdapter adapter = ArrayAdapter.createFromResource(this, R.array.download_state, R.layout.sherlock_spinner_item);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(adapter, this);
        initView();
    }

    private void initView() {
        mListView = (ListView) findViewById(android.R.id.list);
        mStateView = (TextView) findViewById(R.id.time_out_text);
        mAdapter = new ArrayAdapter<DownloadJob>(this, R.layout.sherlock_spinner_dropdown_item, mDownloadMan.getAllDownloads());
        mListView.setAdapter(mAdapter);
        if (mAdapter.isEmpty()) {
            mStateView.setText("no download");
            mStateView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        // TODO Auto-generated method stub
        Log.d(TAG, mStateArray[itemPosition]+"selected");
        mAdapter.clear();
        switch (itemPosition) {
        case 1:
            mAdapter.addAll(mDownloadMan.getQueuedDownloads());
            
            break;
        case 2:
            mAdapter.addAll(mDownloadMan.getCompletedDownloads());
            break;
        default:
            mAdapter.addAll(mDownloadMan.getAllDownloads());
            break;
        }
        if (mAdapter.isEmpty()) {
            mStateView.setText("no download");
            mStateView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
        return true;
    }
}
