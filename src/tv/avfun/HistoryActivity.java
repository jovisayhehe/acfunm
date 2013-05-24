package tv.avfun;

import java.util.ArrayList;

import tv.avfun.db.DBService;
import tv.avfun.entity.Contents;
import tv.avfun.entity.History;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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

public class HistoryActivity extends SherlockActivity implements OnItemClickListener{
	private ArrayList<History> data = new ArrayList<History>();
	private ListView list;
	private ProgressBar progressBar;
	private AssistAdaper adaper;
	private final static int HISTORYID = 400;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("播放历史");
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
					
				data = new DBService(HistoryActivity.this).getHiss();
				
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
    	
    	 menu.add(1, HistoryActivity.HISTORYID, 1,"清除历史")
	      .setIcon(R.drawable.history_clear)
	      .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case HistoryActivity.HISTORYID:
		    showDialog();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	private void clear(){
        new DBService(this).cleanHis();
        data.clear();
        adaper.setData(data);
        adaper.notifyDataSetChanged();
        Toast.makeText(this, "清除完成", Toast.LENGTH_SHORT).show();
	}
	private void showDialog(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("确认清空历史吗？")
                .setNegativeButton("取消", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("清除", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clear();
                        dialog.dismiss();
                    }
                }).show();
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		
		if(data.get(position).getTpye()==0){
			Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
			Contents c = new Contents();
			c.setAid(data.get(position).getAid());
			c.setTitle(data.get(position).getTitle());
			c.setChannelId(Integer.parseInt(data.get(position).getChannelid()));
			intent.putExtra("contents", c); // 居然忘了加=。=
			intent.putExtra("from", 1);
			startActivity(intent);
			
		}else{
			Intent intent = new Intent(HistoryActivity.this, WebViewActivity.class);
			intent.putExtra("modecode", ChannelActivity.modecode);
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
