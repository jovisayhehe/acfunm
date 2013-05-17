package tv.avfun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.avfun.adapter.DetailAdaper;
import tv.avfun.adapter.DownloadJobAdapter;
import tv.avfun.adapter.DownloadJobAdapter.OnItemCheckedListener;
import tv.avfun.app.AcApp;
import tv.avfun.util.download.DownloadJob;
import tv.avfun.util.download.DownloadManager;
import tv.avfun.util.download.DownloadManager.DownloadObserver;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;


public class DownloadManActivity extends BaseListActivity implements OnNavigationListener,DownloadObserver {
    private static final String TAG = DownloadManActivity.class.getSimpleName();
    private DownloadManager mDownloadMan;
    private String[] mStateArray;
    private ListView mListView;
    private TextView mStateView;
    private DownloadJobAdapter mAdapter;
    private ActionMode mMode;
    private Runnable mUpdateTask = new Runnable() {
        
        @Override
        public void run() {
            updateListView(lastNavPosition);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_layout);
        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        mStateArray = getResources().getStringArray(R.array.download_state);
        mDownloadMan = AcApp.instance().getDownloadManager();
        ArrayAdapter adapter = ArrayAdapter.createFromResource(bar.getThemedContext(), R.array.download_state, R.layout.sherlock_spinner_item);
        adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        bar.setListNavigationCallbacks(adapter, this);
        initView();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mDownloadMan.registerDownloadObserver(this);
    }
    @Override
    public void onDownloadChanged(DownloadManager manager) {
        Log.d(TAG, "download changed");
        runOnUiThread(mUpdateTask);
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        mDownloadMan.unregisterDownloadObserver(this);
        
    }
    private void initView() {
        mListView = (ListView) findViewById(android.R.id.list);
        mStateView = (TextView) findViewById(R.id.time_out_text);
        mAdapter = new DownloadJobAdapter(this);
        mAdapter.setOnItemCheckedListener(new OnItemCheckedListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
                // TODO start action mode
                mMode = startActionMode(new DownloadActionMode());
            }
        });
        mListView.setAdapter(mAdapter);
        
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Log.i(TAG, mStateArray[itemPosition]+"selected");
        updateListView(itemPosition);
        lastNavPosition = itemPosition;
        return true;
    }
    private int lastNavPosition = 0;
    private void updateListView(int position) {
        // TODO Auto-generated method stub
        List<DownloadJob> jobs = null;
        switch (position) {
        case 1:
            jobs  = mDownloadMan.getQueuedDownloads();
            break;
        case 2:
            jobs  = mDownloadMan.getCompletedDownloads();
            break;
        default:
            jobs  = mDownloadMan.getAllDownloads();
            break;
        }
        if(lastNavPosition == position && jobs != null && jobs.size() == mAdapter.getCount()){
            mAdapter.notifyDataSetChanged();
        }else{
            mAdapter.setList((ArrayList<DownloadJob>) jobs);
            mListView.setVisibility(View.VISIBLE);
            mStateView.setVisibility(View.GONE);
        }
        
        if (jobs.isEmpty()) {
            mStateView.setText("no download");
            mStateView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
    }
    private class DownloadActionMode implements ActionMode.Callback{

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // TODO Auto-generated method stub
            if(mMode!= null){
                mMode = null;
                mAdapter.unSelectAll();
            }
            
        }
        
    }
}
