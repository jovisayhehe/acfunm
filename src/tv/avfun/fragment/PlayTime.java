package tv.avfun.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import tv.avfun.R;
import tv.avfun.TimeListAdaper;
import tv.avfun.api.ApiParser;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class PlayTime extends SherlockFragment{

	private View main_v;
	private ArrayList<ArrayList<HashMap<String, String>>> data;
	private ListView list;
	private ProgressBar progressBar;
	private Activity activity;
	private TextView time_outtext;
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
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		
		this.activity = getActivity();
	    list = (ListView)this.main_v.findViewById(R.id.list);
	    progressBar = (ProgressBar) this.main_v.findViewById(R.id.time_progress);
	    time_outtext = (TextView) this.main_v.findViewById(R.id.time_out_text);
	    time_outtext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				time_outtext.setVisibility(View.GONE);
				initList();
			}
		});
	    initList();
	}

	public void initList() {
		progressBar.setVisibility(View.VISIBLE);	
		new Thread() {
			public void run() {
				try {
					
					data = ApiParser.getTimedate();
					activity.runOnUiThread(new Runnable() {
						public void run() {
							
						progressBar.setVisibility(View.GONE);		
						list.setAdapter(new TimeListAdaper(activity, data));
						}
					});

				} catch (Exception e) {
					// TODO Auto-generated catch block
					activity.runOnUiThread(new Runnable() {
						public void run() {

							progressBar.setVisibility(View.GONE);
							time_outtext.setVisibility(View.VISIBLE);
						}
					});
					e.printStackTrace();
				}
			}
		}.start();

	}
}
