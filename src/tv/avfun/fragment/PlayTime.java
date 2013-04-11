package tv.avfun.fragment;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

import tv.avfun.R;
import tv.avfun.adapter.TimeListAdaper;
import tv.avfun.api.ApiParser;
import tv.avfun.util.Logger;
import tv.avfun.util.MyAsyncTask;

import android.app.Activity;
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
    // 缓存文件持久时间。这里设为3天
	private static final long CONSTANT_TIME = 3*24*60*60*1000;
    private static final String TAG = PlayTime.class.getSimpleName();
    private View main_v;
	private ArrayList<ArrayList<HashMap<String, String>>> data;
	private ListView list;
	private ProgressBar progressBar;
	private Activity activity;
	private TextView time_outtext;
    private File cache;
	public static PlayTime newInstance() {
		PlayTime f = new PlayTime();
		Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		this.main_v = inflater.inflate(R.layout.list_layout, container, false);

		return this.main_v;
    }
    
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		
		this.activity = getActivity();
	    list = (ListView)this.main_v.findViewById(R.id.list);
	    progressBar = (ProgressBar) this.main_v.findViewById(R.id.time_progress);
	    time_outtext = (TextView) this.main_v.findViewById(R.id.time_out_text);
	    time_outtext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				time_outtext.setVisibility(View.GONE);
				initList();
			}
		});
	    // 缓存文件
	    cache = new File(activity.getCacheDir(),"timedate.dat");
	    initList();
	}
	private boolean isCached(){
	    long lastModified = cache.lastModified();
        return lastModified + CONSTANT_TIME > System.currentTimeMillis();
	}
	public void initList() {
		
		new MyAsyncTask() {
            
            @Override
            public void postExecute() {
                progressBar.setVisibility(View.GONE);       
                list.setAdapter(new TimeListAdaper(activity, data));
            }
            @Override
            public void doInBackground() {
                try {
                    if(!isCached()){
                        progressBar.setVisibility(View.VISIBLE);
                        // 连服务器读新的数据
                        if(Logger.DEBUG) Log.i(TAG,"read new");
                        data = ApiParser.getTimedate();
                        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(cache));
                        out.writeObject(data);
                        out.close();
                    }else{
                        // 缓存数据
                        ObjectInputStream in = new ObjectInputStream(new FileInputStream(cache));
                        if(Logger.DEBUG) Log.i(TAG,"read cache");
                        data = (ArrayList<ArrayList<HashMap<String, String>>>) in.readObject();
                        //Thread.sleep(500); // 读取太快了，反倒感觉不流畅
                        in.close();
                    }
                    publishResult(true);
                } catch (Exception e) {
                    if(Logger.DEBUG)
                        e.printStackTrace();
                    publishResult(false);
                    progressBar.setVisibility(View.GONE);
                }
            }
            public void onPublishResult(boolean succeeded) {
                if(!succeeded){
                    time_outtext.setVisibility(View.VISIBLE);
                }
            }
        }.execute();
	}
}
