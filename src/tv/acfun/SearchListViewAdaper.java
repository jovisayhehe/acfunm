package tv.acfun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.acfun.ListViewAdaper.ListViewHolder;

import acfun.domain.SearchResults;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SearchListViewAdaper extends BaseAdapter {
	private LayoutInflater mInflater;
	private ArrayList<SearchResults> data;
	public SearchListViewAdaper(Context context,ArrayList<SearchResults> data) {
		this.mInflater =LayoutInflater.from(context);
		this.data = data;
	}
	
	public void setData(ArrayList<SearchResults> data){
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
			convertView = mInflater.inflate(R.layout.search_listview_item, null);
			holder = new ListViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.seach_list_item_title);
			holder.info = (TextView) convertView.findViewById(R.id.search_list_item_info);
			holder.date = (TextView) convertView.findViewById(R.id.search_list_item_date);
			holder.con = (TextView) convertView.findViewById(R.id.search_list_item_con);
			holder.hit =(TextView) convertView.findViewById(R.id.search_list_item_hit);
			holder.favor = (TextView) convertView.findViewById(R.id.search_list_item_favor);
			convertView.setTag(holder);
		}else {
			holder = (ListViewHolder) convertView.getTag();
		}
		SearchResults sResults = new SearchResults();
		sResults = data.get(position);
		holder.title.setText(Html.fromHtml(sResults.getTitle()));
		holder.info.setText(Html.fromHtml(sResults.getInfo()));
		holder.date.setText(sResults.getDate());
		holder.con.setText(sResults.getCon());
		holder.hit.setText("点击"+sResults.getHit());
		holder.favor.setText("收藏"+sResults.getFavor());
		return convertView;
	}
	
	static class ListViewHolder {
		TextView title;
		TextView info;
		TextView date;
		TextView con;
		TextView hit;
		TextView favor;
		}
}
