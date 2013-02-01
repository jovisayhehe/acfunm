package tv.avfun;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import tv.avfun.animation.ExpandCollapseAnimation;



import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeListAdaper extends BaseAdapter{
	private ArrayList<ArrayList<HashMap<String, String>>> data;
	private LayoutInflater mInflater;
	private Context context;
	private int selectItem = -1;
	private int stac = -1;
	private static View lastOpen = null;
	private int dayOfWeek;
	private boolean fs = true;
	public TimeListAdaper(Context context,ArrayList<ArrayList<HashMap<String, String>>> data) {
		this.mInflater =LayoutInflater.from(context);
		this.data = data;
		this.context = context;
		 Calendar calendar = Calendar.getInstance(); 
	       Date date = new Date(); 
	       calendar.setTime(date); 
	       dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK); 
	}
	public void setSelectItem(int selectItem) {
		this.selectItem = selectItem;

	}
	
	public void setItemSta(int stac) {
		this.stac = stac;
	}
	
	public void setData(ArrayList<ArrayList<HashMap<String, String>>> data){
		this.data.clear();
		this.data = data;
	}
	
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
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		convertView = mInflater.inflate(R.layout.expandable_list_item,
				null);
		TextView txt = (TextView) convertView.findViewById(R.id.text);
		txt.setText((CharSequence) data.get(position).get(0).get("title"));
		txt.setTag(data.get(position).get(0).get("id"));
		txt.setOnClickListener(new ButtonListener(position,0));
		final LinearLayout expandable = (LinearLayout) convertView.findViewById(R.id.expandable);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		
		for (int i = 1; i < data.get(position).size(); i++) {
			HashMap<String, String> map = data.get(position).get(i);
			TextView title = new TextView(context);
			title.setPadding(15, 8, 5, 8);
			title.setTextColor(Color.BLACK);
			title.setLayoutParams(params);
			title.setText(map.get("title"));
			title.setTextSize(15);
			title.setTextColor(Color.parseColor("#3C6D9D"));
			title.setBackgroundResource(R.drawable.selectable_background);
			title.setTag(map.get("id"));
			title.setOnClickListener(new ButtonListener(position,i));
			expandable.addView(title);
			
		}
		
		
		Button day_btn = (Button) convertView.findViewById(R.id.day_button);
		
		switch (position) {
		case 0:
			day_btn.setText("周日");
			break;
		case 1:
			day_btn.setText("周一");
			convertView.setBackgroundColor(Color.argb(255, 225, 225, 225));
			break;
		case 2:
			day_btn.setText("周二");
			break;
		case 3:
			day_btn.setText("周三");
			convertView.setBackgroundColor(Color.argb(255, 225, 225, 225));
			break;
		case 4:
			day_btn.setText("周四");
			break;
		case 5:
			day_btn.setText("周五");
			convertView.setBackgroundColor(Color.argb(255, 225, 225, 225));
			break;
		case 6:
			day_btn.setText("周六");
			break;

		default:
			break;
		}
		
		day_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				view.setAnimation(null);
				int type = expandable.getVisibility() == View.VISIBLE ? ExpandCollapseAnimation.COLLAPSE : ExpandCollapseAnimation.EXPAND;
				Animation anim = new ExpandCollapseAnimation(expandable, 500, type);
				if(type == ExpandCollapseAnimation.EXPAND) {
					if(lastOpen != null && lastOpen != expandable && lastOpen.getVisibility() == View.VISIBLE) {
						lastOpen.startAnimation(new ExpandCollapseAnimation(lastOpen, 400, ExpandCollapseAnimation.COLLAPSE));
					}
					lastOpen = expandable;
				} else if(lastOpen == view) {
					lastOpen = null;
				}
				view.startAnimation(anim);
			}
		});
		
		if(fs&&position == dayOfWeek-1){
			day_btn.setBackgroundResource(R.drawable.listitembtnselectable_background_r);
			this.setSelectItem(position);
			lastOpen = expandable;
			Animation anim = new ExpandCollapseAnimation(expandable, 400, ExpandCollapseAnimation.EXPAND);
			expandable.setAnimation(anim);
			expandable.setVisibility(View.VISIBLE);
			fs = false;	
		}else if(position == dayOfWeek-1){
			day_btn.setBackgroundResource(R.drawable.listitembtnselectable_background_r);
			expandable.setVisibility(View.GONE);
		}else{
			expandable.setVisibility(View.GONE);
			day_btn.setBackgroundResource(R.drawable.listitembtnselectable_background);
		}
		
		return convertView;
	}
	
	private final class ButtonListener implements OnClickListener{
		private int position;
		private int i;
		public ButtonListener(int position, int i) {
			// TODO Auto-generated constructor stub
			this.position = position;
			this.i = i;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(context, Detail_Activity.class);
			intent.putExtra("title", data.get(position).get(i).get("title"));
			intent.putExtra("aid", v.getTag().toString());
			intent.putExtra("from", 1);
			intent.putExtra("channelId", "67");
			context.startActivity(intent);
		}
		
	}

}
