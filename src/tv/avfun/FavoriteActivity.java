package tv.avfun;

import java.util.ArrayList;
import java.util.List;

import tv.avfun.api.Channel;
import tv.avfun.api.ChannelApi;
import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
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

public class FavoriteActivity extends SherlockActivity implements OnItemClickListener{
	private List<Favorite> data = new ArrayList<Favorite>();
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
		list = (ListView)findViewById(android.R.id.list);
		list.setVisibility(View.INVISIBLE);
		list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
		list.setDividerHeight(2);
		adaper = new AssistAdaper(this,data);
		list.setAdapter(adaper);
		list.setOnItemClickListener(this);
		
		
		new Thread() {
			public void run() {
					
				data = new DBService(FavoriteActivity.this).getFovs();
				
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
	    Favorite favorite = data.get(position);
		if(data.get(position).getTpye()==0){
			Intent intent = new Intent(FavoriteActivity.this, DetailActivity.class);
			Contents c = new Contents();
			c.setAid(favorite.aid);
			c.setTitle(favorite.title);
			c.setChannelId(favorite.channelid);
			intent.putExtra("from", 1);
			intent.putExtra("contents", c);
			startActivity(intent);
			
		}else{
		    //TODO 改为contents
			Intent intent = new Intent(FavoriteActivity.this, WebViewActivity.class);
			intent.putExtra("modecode", ChannelActivity.modecode);
			intent.putExtra("aid", data.get(position).getAid());
			intent.putExtra("title", data.get(position).getTitle());
			intent.putExtra("channelId", data.get(position).getChannelid());
			startActivity(intent);
		}
	}
	
	
	private final class AssistAdaper extends BaseAdapter{
		private LayoutInflater mInflater;
		private List<Favorite> data;
		
		public AssistAdaper(Context context,List<Favorite> data) {
			this.mInflater =LayoutInflater.from(context);
			this.data = data;
		}
		
		
		public void setData(List<Favorite> data){
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
			Channel channel = ChannelApi.channels.get(data.get(position).getChannelid());
			if(channel == null)
			    // 不知名的id 暂且隐藏起来
			    holder.channel.setVisibility(View.INVISIBLE);
			else {
			    holder.channel.setText(channel.title);
			    holder.channel.setVisibility(View.VISIBLE);
			}
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
