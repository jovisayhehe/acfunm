package tv.avfun;

import java.util.ArrayList;

import tv.avfun.db.DBService;
import tv.avfun.entity.Favorite;

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

import com.actionbarsherlock.app.SherlockActivity;

public class Favorite_Activity extends SherlockActivity implements OnItemClickListener{
	private ArrayList<Favorite> data = new ArrayList<Favorite>();
	private ListView list;
	private ProgressBar progressBar;
	private AssistAdaper adaper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
			getSupportActionBar().setTitle("我的收藏");
		
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
					
				data = new DBService(Favorite_Activity.this).getFovs();
				
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


	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if(data.get(position).getTpye()==0){
			Intent intent = new Intent(Favorite_Activity.this, Detail_Activity.class);
			intent.putExtra("aid", data.get(position).getAid());
			intent.putExtra("title", data.get(position).getTitle());
			intent.putExtra("channelId", data.get(position).getChannelid());
			intent.putExtra("from", 1);
			startActivity(intent);
			
		}else{
			Intent intent = new Intent(Favorite_Activity.this, WebView_Activity.class);
			intent.putExtra("modecode", Channel_Activity.modecode);
			intent.putExtra("aid", data.get(position).getAid());
			intent.putExtra("title", data.get(position).getTitle());
			intent.putExtra("channelId", data.get(position).getChannelid());
			startActivity(intent);
		}
	}
	
	
	private final class AssistAdaper extends BaseAdapter{
		private LayoutInflater mInflater;
		private ArrayList<Favorite> data;
		
		public AssistAdaper(Context context,ArrayList<Favorite> data) {
			this.mInflater =LayoutInflater.from(context);
			this.data = data;
		}
		
		
		public void setData(ArrayList<Favorite> data){
			this.data = data;
		}
		
		@Override
		public int getCount() {
			
			return this.data.size();
		}

		@Override
		public Object getItem(int position) {
			
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			final ListViewHolder holder;
			if(convertView==null){
				convertView = mInflater.inflate(R.layout.favorites_list_item, null);
				holder = new ListViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.favrites_item_title);
				holder.channel = (TextView) convertView.findViewById(R.id.favrites_item_chann);
				holder.user = (TextView) convertView.findViewById(R.id.favrites_item_art);
				convertView.setTag(holder);
			}else {
				holder = (ListViewHolder) convertView.getTag();
			}
			
			holder.title.setText(data.get(position).getTitle());
			holder.channel.setText(data.get(position).getChannelid());
			holder.user.setText("ac"+data.get(position).getAid());
			
			return convertView;
		}
		
	}
	
	static class ListViewHolder {
		TextView title;
		TextView channel;
		TextView user;
		}
	
}
