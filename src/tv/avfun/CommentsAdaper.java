package tv.avfun;

import java.util.List;
import java.util.Map;

import tv.ac.fun.R;
import tv.avfun.util.lzlist.ImageLoader;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CommentsAdaper  extends BaseAdapter{
	private LayoutInflater mInflater;
	private List<Map<String, Object>> data;
	public ImageLoader imageLoader;
	public CommentsAdaper(Context context,List<Map<String, Object>> data) {
		this.mInflater =LayoutInflater.from(context);
		this.data = data;
		imageLoader=ImageLoader.getInstance();
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
			convertView = mInflater.inflate(R.layout.comments_listitem, null);
			holder = new ListViewHolder();
			holder.img = (ImageView) convertView.findViewById(R.id.comments_user_img);
			holder.username = (TextView) convertView.findViewById(R.id.comments_user_name);
			holder.comment = (TextView) convertView.findViewById(R.id.comments_content);
			convertView.setTag(holder);
		}else {
			holder = (ListViewHolder) convertView.getTag();
		}
		@SuppressWarnings("rawtypes")
		final Map art = data.get(position); 
		holder.comment.setText(String.valueOf(art.get("content")));
		holder.username.setText(String.valueOf(art.get("userName")));
		
		final String imageUrl =String.valueOf(art.get("userImg"));
		if(imageUrl!= null &&!imageUrl.equals("null")){
			holder.img.setTag(imageUrl);
			imageLoader.displayImage(imageUrl, holder.img);
		}
		return convertView;
		
	}
	
	static class ListViewHolder {
		ImageView img;
		TextView username;
		TextView comment;
		}


}
