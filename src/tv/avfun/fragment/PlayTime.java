package tv.avfun.fragment;


import java.util.List;

import tv.avfun.BuildConfig;
import tv.avfun.R;
import tv.avfun.adapter.TimeListAdaper;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Bangumi;
import tv.avfun.util.DataStore;
import tv.avfun.util.MyAsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PlayTime extends SherlockFragment{
    private static final String TAG = PlayTime.class.getSimpleName();
    private View main_v;
	private List<Bangumi[]> data;
	private ListView list;
	private ProgressBar progressBar;
	private TextView time_outtext;
	public static PlayTime newInstance() {
		PlayTime f = new PlayTime();
		Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		this.main_v = inflater.inflate(R.layout.list_layout, container, false);
        list = (ListView)this.main_v.findViewById(android.R.id.list);
        progressBar = (ProgressBar) this.main_v.findViewById(R.id.time_progress);
        time_outtext = (TextView) this.main_v.findViewById(R.id.time_out_text);
        time_outtext.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                
                time_outtext.setVisibility(View.GONE);
                initList();
            }
        });
        initList();
		return this.main_v;
    }
    
	private boolean isCached = DataStore.getInstance().isBangumiListCached();
	public void initList() {
		new MyAsyncTask() {
            public void onPreExecute() {
                if(!isCached)
                    progressBar.setVisibility(View.VISIBLE);
            }
            @Override
            public void onPostExecute() {
                progressBar.setVisibility(View.GONE);
                list.setAdapter(new TimeListAdaper(getActivity(), data));
            }
            @Override
            public void doInBackground() {
                try {
                    if(!isCached){
                        // 连服务器读新的数据
                        if(BuildConfig.DEBUG) Log.i(TAG,"read new");
                        data = ApiParser.getBangumiTimeList();
                        // 保存
                        DataStore.getInstance().saveTimeList(data);
                        
                    }else{
                        if(BuildConfig.DEBUG) Log.i(TAG,"read cache list");
                        // 读缓存
                        data = DataStore.getInstance().loadTimeList();
                    }
                    publishResult(true);
                } catch (Exception e) {
                    if(BuildConfig.DEBUG)
                        e.printStackTrace();
                    publishResult(false);
                    
                }
            }
            public void onPublishResult(boolean succeeded) {
                if(!succeeded){
                    time_outtext.setVisibility(View.VISIBLE);
                }
                progressBar.setVisibility(View.GONE);
            }
        }.execute();
	}
}
