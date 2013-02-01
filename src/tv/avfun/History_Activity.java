package tv.avfun;

import java.util.ArrayList;

import tv.avfun.db.DBService;
import tv.avfun.entity.History;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class History_Activity extends SherlockActivity implements OnItemClickListener{
	private ArrayList<History> data = new ArrayList<History>();
	private ListView list;
	private ProgressBar progressBar;
	private AssistAdaper adaper;
	private final static int HISTORYID = 400;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
			getSupportActionBar().setTitle("播放历史");
		
		progressBar = (ProgressBar)findViewById(R.id.time_progress);
		list = (ListView)findViewById(R.id.list);
		list.setVisibility(View.INVISIBLE);
		list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
		list.setDividerHeight(2);
		adaper = new AssistAdaper(this,data);
		list.setAdapter(adaper);
		list.setOnItemClickListener(this);
		
		
		new Thread() {
			public void run() {
					
				data = new DBService(History_Activity.this).getHiss();
				
					runOnUiThread(new Runnable() {
						public void run() {
							list.setVisibility(View.VISIBLE);
							adaper.setData(data);
							adaper.notifyDataSetChanged();
							progressBar.setVisibility(View.GONE);
						}
					});
			}
		}.start();
		
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
    	 menu.add(1, History_Activity.HISTORYID, 1,"清除历史")
	      .setIcon(R.drawable.history_clear)
	      .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case History_Activity.HISTORYID:
			new DBService(this).cleanHis();
			data.clear();
			adaper.setData(data);
			adaper.notifyDataSetChanged();
			Toast.makeText(this, "清除完成", Toast.LENGTH_SHORT).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		
		if(data.get(position).getTpye()==0){
			Intent intent = new Intent(History_Activity.this, Detail_Activity.class);
			intent.putExtra("aid", data.get(position).getAid());
			intent.putExtra("title", data.get(position).getTitle());
			intent.putExtra("channelId", data.get(position).getChannelid());
			intent.putExtra("from", 1);
			startActivity(intent);
			
		}else{
			Intent intent = new Intent(History_Activity.this, WebView_Activity.class);
			intent.putExtra("modecode", Channel_Activity.modecode);
			intent.putExtra("aid", data.get(position).getAid());
			intent.putExtra("title", data.get(position).getTitle());
			intent.putExtra("channelId", data.get(position).getChannelid());
			startActivity(intent);
		}
	}
	
	
	private final class AssistAdaper extends BaseAdapter{
		private LayoutInflater mInflater;
		private ArrayList<History> data;
		
		public AssistAdaper(Context context,ArrayList<History> data) {
			this.mInflater =LayoutInflater.from(context);
			this.data = data;
		}
		
		
		public void setData(ArrayList<History> data){
			this.data = data;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return this.data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final ListViewHolder holder;
			if(convertView==null){
				convertView = mInflater.inflate(R.layout.history_listitem, null);
				holder = new ListViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.his_title);
				holder.time = (TextView) convertView.findViewById(R.id.his_time);
				convertView.setTag(holder);
			}else {
				holder = (ListViewHolder) convertView.getTag();
			}
			
			holder.title.setText(data.get(position).getTitle());
			holder.time.setText("时间: "+data.get(position).getTime());
			
			return convertView;
		}
		
	}
	
	static class ListViewHolder {
		TextView title;
		TextView time;
		}
	
}
