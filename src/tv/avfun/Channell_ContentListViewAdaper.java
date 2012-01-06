package tv.avfun;

import java.util.List;
import java.util.Map;

import tv.avfun.R;

import acfun.domain.Article;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class Channell_ContentListViewAdaper extends BaseAdapter{
	private LayoutInflater mInflater;
	private List<Map<String, Object>> data;
	public Channell_ContentListViewAdaper(Context context,List<Map<String, Object>> data) {
		this.mInflater =LayoutInflater.from(context);
		this.data = data;
	}
	
	public void setData(List<Map<String, Object>> data){
		this.data = data;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return data.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ListViewHolder holder;
		if(convertView==null){
			convertView = mInflater.inflate(R.layout.channellist_content_item, null);
			holder = new ListViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.channellist_content_item_title);
			holder.date = (TextView) convertView.findViewById(R.id.channelist_content_item_date);
			holder.upman = (TextView) convertView.findViewById(R.id.channelist_content_item_upman);
			convertView.setTag(holder);
		}else {
			holder = (ListViewHolder) convertView.getTag();
		}
		Map art = data.get(position); 
		holder.title.setText(String.valueOf(art.get("title")));
		holder.date.setText(String.valueOf(art.get("uptime")));
		holder.upman.setText(String.valueOf(art.get("art")));
		convertView.setTag(convertView.getId(),art.get("link"));
		return convertView;
	}
	
	static class ListViewHolder {
		TextView title;
		TextView date;
		TextView upman;
		}
	

}
