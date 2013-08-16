package tv.avfun.fragment;

import java.util.List;

import tv.ac.fun.BuildConfig;
import tv.ac.fun.R;
import tv.avfun.adapter.TimeListAdaper;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Bangumi;
import tv.avfun.util.DataStore;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

public class PlayTime extends BaseFragment implements OnNavigationListener {

    private static final String TAG = PlayTime.class.getSimpleName();
    private View                main_v;
    private List<Bangumi[]>     data;
    private ListView            list;
    private ProgressBar         progressBar;
    private TextView            time_outtext;
    private ActionBar mBar;

    public static PlayTime newInstance() {
        PlayTime f = new PlayTime();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onSwitch(getSherlockActivity().getSupportActionBar());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.main_v = inflater.inflate(R.layout.list_layout, container, false);
        list = (ListView) this.main_v.findViewById(android.R.id.list);
        progressBar = (ProgressBar) this.main_v.findViewById(R.id.time_progress);
        time_outtext = (TextView) this.main_v.findViewById(R.id.time_out_text);
        time_outtext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                time_outtext.setVisibility(View.GONE);
                initList(index);
            }
        });
        
        return this.main_v;
    }

    public void onShow(){
        initList(0);
    }
    private void initList(int index) {
        new LoadTimeListTask().execute(index);
    }

    class LoadTimeListTask extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            if (!DataStore.getInstance().isBangumiListCached(index))
                progressBar.setVisibility(View.VISIBLE);
            list.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            if (!DataStore.getInstance().isBangumiListCached(params[0])) {
                // 连服务器读新的数据
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "read new time list");
                data = ApiParser.getBangumiTimeList(params[0]);
                // 保存
                if (data != null && !data.isEmpty())
                    DataStore.getInstance().saveTimeList(data,params[0]);
            } else {
                if (BuildConfig.DEBUG)
                    Log.i(TAG, "read cache list");
                // 读缓存
                data = DataStore.getInstance().loadTimeList(params[0]);
            }

            return data != null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                TimeListAdaper adapter = new TimeListAdaper(activity, data);
                list.setAdapter(adapter);
            } else
                time_outtext.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            list.setVisibility(View.VISIBLE);
        }
    }
    private int index = 0;
    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId) {
        Log.i(TAG, "NavigationItemSelected: "+ itemPosition+",index= "+index);
        if(index != itemPosition){
            
            index = itemPosition;
            initList(index);
            return true;
        }
        return false;
    }
    @Override
    public void onSwitch(ActionBar bar) {
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(bar.getThemedContext(), R.array.timeListMode,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
        bar.setListNavigationCallbacks(adapter, this);
        bar.setDisplayShowTitleEnabled(false);
    }
}
