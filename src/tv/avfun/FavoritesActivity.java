package tv.avfun;

import java.util.ArrayList;
import java.util.HashMap;

import tv.avfun.R;
import tv.avfun.SearchListViewAdaper.ListViewHolder;
import tv.acfun.db.DBService;
import tv.acfun.util.Util;

import acfun.domain.SearchResults;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FavoritesActivity extends Activity {
	private ListView listview;
	private ArrayList<HashMap<String, String>> listdata;
	private FovListViewAdaper adapter;
	private Animation myAnimation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fov_layout);
		TextView titel = (TextView) findViewById(R.id.title_text);
		titel.setText("收藏");
		listview = (ListView) findViewById(R.id.fov_listview);
		listdata = new DBService(this).getFovs();
		adapter = new FovListViewAdaper(this, listdata);
	     listview.setAdapter(adapter);
	     listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				String id = (String) listdata.get(arg2).get("id");
				String atitle = (String) listdata.get(arg2).get("title");
				ArrayList<String> infos = new ArrayList<String>();
				infos.add(id);
				infos.add("favorites");
				infos.add(atitle);
				Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
				intent.putStringArrayListExtra("info", infos);
				FavoritesActivity.this.startActivity(intent);
			}
		});
	     
	     listview.setOnTouchListener(new OnTouchListener() {
			
			float x,y,ux,uy;
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			if(event.getAction()==MotionEvent.ACTION_DOWN){
			x=event.getX();
			y=event.getY();
			 myAnimation = new AlphaAnimation((float) 1.0, 0);
		     myAnimation.setDuration(400);
			}
			if(event.getAction()==MotionEvent.ACTION_UP){
			ux=event.getX();uy=event.getY();
			final int p1=listview.pointToPosition((int)x, (int)y);
			int p2=listview.pointToPosition((int)ux, (int)uy);
			if(p1==p2&&Math.abs(x-ux)>75){
				
				View item = listview.getChildAt(p1);
				int[] location = new int[2];
				item.getLocationInWindow(location);
				item.startAnimation(myAnimation);		    
				myAnimation.setAnimationListener(new AnimationListener() {
					
					@Override
					public void onAnimationStart(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					@Override
					public void onAnimationRepeat(Animation animation) {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					public void onAnimationEnd(Animation animation) {
						// TODO Auto-generated method stub
							String vid = listdata.get(p1).get("id");
							listdata.remove(p1);
						     adapter.notifyDataSetChanged();
						     new DBService(FavoritesActivity.this).delFov(vid);
					}
				});
			}
			return false;
			}
			return false;
			}
		});
	     
	     
	     listview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				showDialog(position);
				return false;
			}
	    	 
		});
	}
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		// TODO Auto-generated method stub
			ImageView img = new ImageView(FavoritesActivity.this);
			img.setImageResource(R.drawable.neterror);
			return new AlertDialog.Builder(FavoritesActivity.this)
					.setTitle("确认删除吗?")
					.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String vid = listdata.get(id).get("id");
							listdata.remove(id);
						     adapter.notifyDataSetChanged();
						     new DBService(FavoritesActivity.this).delFov(vid);
						}
					}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							dialog.dismiss();
						
						}
					})
					
					.create();	
	}
	
	private final class FovListViewAdaper extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<HashMap<String, String>> data;
		public FovListViewAdaper(Context context,ArrayList<HashMap<String, String>> data) {
			this.mInflater =LayoutInflater.from(context);
			this.data = data;
		}
		public void setData(ArrayList<HashMap<String, String>> data){
			this.data = data;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
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
		public View getView(final int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			ListViewHolder holder;
			if(convertView==null){
				convertView = mInflater.inflate(R.layout.fovlist_item, null);
				holder = new ListViewHolder();
				holder.title = (TextView) convertView.findViewById(R.id.fov_listview_item_txt);
				convertView.setTag(holder);
			}else {
				holder = (ListViewHolder) convertView.getTag();
			}
			String title = data.get(position).get("title");
			holder.title.setText(title);
			return convertView;
		}
		
	}
	
	static class ListViewHolder {
		TextView title;
		}

}
