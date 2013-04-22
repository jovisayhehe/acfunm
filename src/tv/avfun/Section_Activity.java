package tv.avfun;

import java.util.ArrayList;
import java.util.List;

import tv.avfun.api.ApiParser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.umeng.analytics.MobclickAgent;

public class Section_Activity extends SherlockActivity implements OnClickListener{
	private String aid;
	private String vtype;
	private ProgressBar progressBar;
	private TextView time_outtext;
	private ListView list;
	private SectionAdapter adapter;
	private ArrayList<String> data = new ArrayList<String>();
	private int playmode = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		
	    ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(getIntent().getStringExtra("title"));
        aid = getIntent().getStringExtra("vid");
        vtype = getIntent().getStringExtra("vtype");
        playmode = ((AcApp)getApplication()).getConfig().getInt("playmode", 0);
        list = (ListView) findViewById(android.R.id.list);
        progressBar = (ProgressBar)findViewById(R.id.time_progress);
		 time_outtext = (TextView)findViewById(R.id.time_out_text);
		 time_outtext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				getdatas();
			}
		});
		list.setVisibility(View.INVISIBLE);
		list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
		list.setDividerHeight(2);
		adapter = new SectionAdapter(this,data);
		list.setAdapter(adapter);
		getdatas();
	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}
	public void getdatas() {
			time_outtext.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				try {

					data = ApiParser.ParserVideopath(vtype, aid);
						
					runOnUiThread(new Runnable() {
						public void run() {
								progressBar.setVisibility(View.GONE);
								list.setVisibility(View.VISIBLE);
								
							if(data!=null&&!data.isEmpty()&&data.size()>0){
								if(data.size()>1){
//									adapter.setData(data);
//									adapter.notifyDataSetChanged();
									
									Intent intent = new Intent(Section_Activity.this, Play_Activity.class);
//									intent.putExtra("path", data.get(0));
									intent.putStringArrayListExtra("paths", data);
							        startActivity(intent);
							        Section_Activity.this.finish();
								}else{

									if(playmode==0){
										Intent intent = new Intent(Section_Activity.this, Play_Activity.class);
										intent.putExtra("paths", data);
								        startActivity(intent);
								        Section_Activity.this.finish();
									}else{
										Intent it = new Intent(Intent.ACTION_VIEW);  
								        Uri uri = Uri.parse(data.get(0));  
								        it.setDataAndType(uri , "video/flv");
								        startActivity(it);
								        Section_Activity.this.finish();
									}

								}

							}else{
								list.setVisibility(View.GONE);
								time_outtext.setEnabled(false);
								time_outtext.setVisibility(View.VISIBLE);
								time_outtext.setText(R.string.noparser);
							}
	
						}
					});

				} catch (Exception e) {
					
					runOnUiThread(new Runnable() {
						public void run() {
								progressBar.setVisibility(View.GONE);
								time_outtext.setVisibility(View.VISIBLE);
								time_outtext.setEnabled(true);
								list.setVisibility(View.INVISIBLE);
						}
					});
					e.printStackTrace();
				}
			}
		}.start();

	}
	
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private final class SectionAdapter  extends BaseAdapter{
		private List<String> data;
		
		public SectionAdapter(Context context,List<String> data) {
			this.data = data;
		}
		
		public void setData(List<String> data){
			this.data = data;
		}
		
		@Override
		public int getCount() {
			
			return this.data.size();
		}

		@Override
		public Object getItem(int position) {
			
			return this.data.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			
			TextView textView = new TextView(Section_Activity.this);
			
			textView.setText(String.valueOf(position+1));
			textView.setTextSize(24);
			textView.setTypeface(null, Typeface.BOLD);
			textView.setPadding(12, 12, 12, 12);
			textView.setTextColor(Color.BLACK);
			textView.setTag(data.get(position));
			textView.setOnClickListener(Section_Activity.this);
			textView.setBackgroundResource(R.drawable.selectable_background);
			convertView = textView;
			return convertView;
		}
		
	}

	@Override
	public void onClick(View v) {
		
		String flvpath = (String) v.getTag();

        if(playmode==0){
    		Intent intent = new Intent(Section_Activity.this, Play_Activity.class);
    		intent.putExtra("path", flvpath);
            startActivity(intent);
        }else{
    		Intent it = new Intent(Intent.ACTION_VIEW);  
          Uri uri = Uri.parse(flvpath);  
          it.setDataAndType(uri , "video/flv");  
          startActivity(it);
        }
	}
}
