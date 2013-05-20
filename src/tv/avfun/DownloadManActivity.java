
package tv.avfun;

import java.util.ArrayList;
import java.util.List;

import tv.avfun.DownloadService.IDownloadService;
import tv.avfun.adapter.DownloadJobAdapter;
import tv.avfun.adapter.DownloadJobAdapter.OnItemCheckedListener;
import tv.avfun.app.AcApp;
import tv.avfun.util.download.DownloadEntry;
import tv.avfun.util.download.DownloadJob;
import tv.avfun.util.download.DownloadManager;
import tv.avfun.util.download.DownloadManager.DownloadObserver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class DownloadManActivity extends BaseListActivity implements OnNavigationListener, DownloadObserver {

    private static final String TAG         = DownloadManActivity.class.getSimpleName();
    private DownloadManager     mDownloadMan;
    private String[]            mStateArray;
    private ListView            mListView;
    private TextView            mStateView;
    private DownloadJobAdapter  mAdapter;
    private ActionMode          mMode;
    private Runnable            mUpdateTask = new Runnable() {
    
        @Override
        public void run() {
            updateListView(lastNavPosition);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDownloadMan = AcApp.instance().getDownloadManager();
        mBar = getSupportActionBar();
        mBar.setTitle("下载管理");
        mBar.setDisplayHomeAsUpEnabled(true);
        if(!AcApp.isExternalStorageAvailable()){
            setContentView(R.layout.no_sd_layout);
            return;
        }
        setContentView(R.layout.list_layout);
        Intent service = new Intent(this, DownloadService.class);
        bindService(service, conn, BIND_AUTO_CREATE);
        mStateArray = getResources().getStringArray(R.array.download_state);
        // TODO 换成popupwindow或者其他的显示方式
        ArrayAdapter adapter = ArrayAdapter.createFromResource(mBar.getThemedContext(), R.array.download_state,
                R.layout.sherlock_spinner_item);
        adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mBar.setListNavigationCallbacks(adapter, this);
        initView();
    }

    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // menu.add(0,R.id.edit_query,0,"编辑").setIcon(R.drawable.ic_menu_mark).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    // return true;
    // }
    @Override
    protected void onStart() {
        super.onStart();
        mDownloadMan.registerDownloadObserver(this);
        mAdapter.notifyDataSetChanged();
    }

    private DownloadService   mDownloadService;
    private ServiceConnection conn  = new ServiceConnection() {

          @Override
          public void onServiceDisconnected(ComponentName name) {
              mDownloadService = null;
          }
    
          @Override
          public void onServiceConnected(ComponentName name, IBinder service) {
              if (service instanceof IDownloadService) {
                  mDownloadService = ((IDownloadService) service).getService();
              }
          }
      };
    long lastUpdateTime;
    long UPDATE_INTERVAL = 500; // 500ms刷新一回，以免过快刷新导致崩溃

    @Override
    public void onDownloadChanged(DownloadManager manager) {
        if (System.currentTimeMillis() - lastUpdateTime > UPDATE_INTERVAL) {
            runOnUiThread(mUpdateTask);
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDownloadMan.unregisterDownloadObserver(this);
        try {
            unbindService(conn);
        } catch (Exception e) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unbindService(conn);
        } catch (Exception e) {}
    }

    private void initView() {
        mListView = (ListView) findViewById(android.R.id.list);
        mStateView = (TextView) findViewById(R.id.time_out_text);
        mAdapter = new DownloadJobAdapter(this);
        mAdapter.setOnItemCheckedListener(new OnItemCheckedListener() {

            @Override
            public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
                if (mAdapter.getCheckedCount() < 1) {
                    if (mMode != null)
                        mMode.finish();
                } else if (mMode == null) {
                    mMode = startActionMode(new DownloadActionMode());
                }
            }
        });
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(itemClickListener);

    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Log.i(TAG, mStateArray[itemPosition] + "selected");

        updateListView(itemPosition);
        lastNavPosition = itemPosition;
        return true;
    }

    private int       lastNavPosition = 0;
    private ActionBar mBar;

    private void updateListView(int position) {
        List<DownloadJob> jobs = null;
        switch (position) {
        case 1:
            jobs = mDownloadMan.getQueuedDownloads();
            break;
        case 2:
            jobs = mDownloadMan.getCompletedDownloads();
            break;
        default:
            jobs = mDownloadMan.getAllDownloads();
            break;
        }
        if (lastNavPosition == position && jobs != null && jobs.size() == mAdapter.getCount()) {
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.setList((ArrayList<DownloadJob>) jobs);
            mListView.setVisibility(View.VISIBLE);
            mStateView.setVisibility(View.GONE);
        }
        mBar.setTitle("共" + mAdapter.getCount() + "个下载项");
        if (jobs.isEmpty()) {
            mStateView.setText(getString(R.string.no_download));
            mStateView.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
    }

    private OnItemClickListener itemClickListener = new OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> parent, View view,
                  int position, long id) {
              if (mMode != null) {
                  // mAdapter.checked(position);
                  CheckBox cb = (CheckBox) view
                          .findViewById(R.id.download_checked);
                  cb.setChecked(!cb.isChecked());
              } else {
                  DownloadJob item = mAdapter.getItem(position);
                  if (item != null){
                      startToPlay(item.getEntry());
                  }
              }

          }

      };

    private class DownloadActionMode implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_download_man_action_mode, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
            case R.id.menu_download_delete:

                int i = 0;
                for (DownloadJob job : mAdapter.getCheckedJobs()) {
                    mDownloadMan.deleteDownload(job);
                    i++;
                }
                AcApp.showToast("删除完毕 - 共" + i + "项");
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_download_resume:
                AcApp.showToast("继续");
                for (DownloadJob job : mAdapter.getCheckedJobs()) {
                    if (job.getProgress() != 100) {
                        job.setListener(mDownloadService.mJobListener);
                        job.resume();
                        mDownloadMan.getProvider().resume(job);
                    }
                }
                break;
            case R.id.menu_download_pause:
                AcApp.showToast("暂停");
                for (DownloadJob job : mAdapter.getCheckedJobs()) {
                    if (DownloadManager.isRunningStatus(job.getStatus())) {
                        job.setListener(mDownloadService.mJobListener);
                        job.pause();
                    }
                }
                break;
            case R.id.menu_select_all:
                mAdapter.checkedAll();
                break;
            }

            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            if (mMode != null) {
                mMode = null;
                mAdapter.unCheckedAll();
            }

        }
    }

    protected void startToPlay(DownloadEntry entry) {
        Intent intent = new Intent(this, SectionActivity.class);
        intent.putExtra("item", entry.part);
        startActivity(intent);
    }
}
