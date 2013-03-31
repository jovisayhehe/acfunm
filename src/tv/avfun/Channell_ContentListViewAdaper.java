package tv.avfun;

import java.util.List;
import java.util.Map;


import tv.avfun.R;
import tv.avfun.util.lzlist.ImageLoader;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Channell_ContentListViewAdaper extends BaseAdapter{
	private LayoutInflater mInflater;
	private List<Map<String, Object>> data;
	public ImageLoader imageLoader;
	public Channell_ContentListViewAdaper(Context context,List<Map<String, Object>> data) {
		this.mInflater =LayoutInflater.from(context);
		this.data = data;
		imageLoader= new ImageLoader(context);
	}
	
	public void setData(List<Map<String, Object>> data){
		this.data = data;
	}
	@Override
	public int getCount() {
		
		return data.size();
	}

	@Override
	public Object getItem(int arg0) {
		
		return data.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		
		final ListViewHolder holder;
		if(convertView==null){
			convertView = mInflater.inflate(R.layout.channellist_content_item, null);
			holder = new ListViewHolder();
			holder.img = (ImageView) convertView.findViewById(R.id.channellist_item_img);
			holder.title = (TextView) convertView.findViewById(R.id.channellist_content_item_title);
			holder.date = (TextView) convertView.findViewById(R.id.channelist_content_item_views);
			holder.upman = (TextView) convertView.findViewById(R.id.channelist_content_item_art);
			holder.dr = (LinearLayout) convertView.findViewById(R.id.channelist_content_dr);
			convertView.setTag(holder);
		}else {
			holder = (ListViewHolder) convertView.getTag();
		}
		if(position==9){
			holder.dr.setVisibility(View.VISIBLE);
		}else{
			holder.dr.setVisibility(View.GONE);
		}
		
		@SuppressWarnings("rawtypes")
		final Map art = data.get(position);
		holder.title.setText(String.valueOf(art.get("title")));
		holder.date.setText("点击:"+ String.valueOf(art.get("views")));
		holder.upman.setText(String.valueOf(art.get("username")));
		final String imageUrl = (String) art.get("titleImg");
		if(imageUrl!= "null"&&!imageUrl.equals("")){
			holder.img.setTag(imageUrl);
			imageLoader.DisplayImage(imageUrl, holder.img);
		}
		return convertView;
		
	}
	
	static class ListViewHolder {
		ImageView img;
		TextView title;
		TextView date;
		TextView upman;
		LinearLayout dr;
		}

}
