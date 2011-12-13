package tv.acfun;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListViewAdaper extends BaseAdapter{
	private LayoutInflater mInflater;
	private List<Map<String, Object>> data;
	public ListViewAdaper(Context context,List<Map<String, Object>> data) {
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
			convertView = mInflater.inflate(R.layout.funlistview_item, null);
			holder = new ListViewHolder();
			holder.title = (TextView) convertView.findViewById(R.id.funlist_item_text);
			holder.positiontitle = (TextView) convertView.findViewById(R.id.funlist_item_position_text);
			convertView.setTag(holder);
		}else {
			holder = (ListViewHolder) convertView.getTag();
		}
		holder.title.setText(String.valueOf(data.get(position).get("title")));
		holder.positiontitle.setText(String.valueOf(position+1)+".");
		convertView.setTag(convertView.getId(),data.get(position).get("link"));
		return convertView;
	}
	
	static class ListViewHolder {
		TextView title;
		TextView positiontitle;
		}
	

}
