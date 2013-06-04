package tv.avfun.adapter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import tv.avfun.DetailActivity;
import tv.ac.fun.R;
import tv.avfun.animation.ExpandAnimation;
import tv.avfun.animation.ExpandCollapseAnimation;
import tv.avfun.api.Bangumi;
import tv.avfun.entity.Contents;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeListAdaper extends BaseAdapter{
	private List<Bangumi[]> data;
	private LayoutInflater mInflater;
	private Context context;
	private int selectItem = -1;
	private int stac = -1;
	private static View lastOpen = null;
	private int dayOfWeek;
	private boolean fs = true;
	public TimeListAdaper(Context context,List<Bangumi[]> data) {
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
	
	public void setData(List<Bangumi[]> data){
		this.data.clear();
		this.data = data;
		this.notifyDataSetChanged();
	}
	
	public int getCount() {
		
		return data.size();
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
		
		convertView = mInflater.inflate(R.layout.expandable_list_item,
				null);
		TextView txt = (TextView) convertView.findViewById(R.id.text);
		txt.setText((CharSequence) data.get(position)[0].title);
		txt.setTag(data.get(position)[0].aid);
		txt.setOnClickListener(new ButtonListener(position,0));
		final LinearLayout expandable = (LinearLayout) convertView.findViewById(R.id.expandable);
		
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
		int i = 1;
		for (; i < data.get(position).length; i++) {
			Bangumi bangumi = data.get(position)[i];
			TextView title = new TextView(context);
			title.setPadding(15, 8, 5, 8);
			title.setTextColor(Color.BLACK);
			title.setLayoutParams(params);
			title.setText(bangumi.title);
			title.setTextSize(15);
			title.setTextColor(Color.parseColor("#3C6D9D"));
			title.setBackgroundResource(R.drawable.selectable_background);
			title.setTag(bangumi.aid);
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
		        expandable.clearAnimation();
		        Animation anim = new ExpandAnimation(expandable, 400);
		        expandable.startAnimation(anim);
			}
		});
		
		if(fs&&position == dayOfWeek-1){
			day_btn.setBackgroundResource(R.drawable.listitembtnselectable_background_r);
			this.setSelectItem(position);
			lastOpen = expandable;
			Animation anim = new ExpandCollapseAnimation(expandable, 400, ExpandCollapseAnimation.EXPAND);
			anim.setStartOffset(200);
			expandable.startAnimation(anim);
			fs = false;	
		}else if(position == dayOfWeek-1){
			day_btn.setBackgroundResource(R.drawable.listitembtnselectable_background_r);
			expandable.setVisibility(View.GONE);
			((LinearLayout.LayoutParams) expandable.getLayoutParams()).bottomMargin = - i* 30;
		}else{
			expandable.setVisibility(View.GONE);
			day_btn.setBackgroundResource(R.drawable.listitembtnselectable_background);
			((LinearLayout.LayoutParams) expandable.getLayoutParams()).bottomMargin = - i* 30;
		}
		
		return convertView;
	}
	
	private final class ButtonListener implements OnClickListener{
		private int position;
		private int i;
		public ButtonListener(int position, int i) {
			
			this.position = position;
			this.i = i;
		}

		@Override
		public void onClick(View v) {
			
			Intent intent = new Intent(context, DetailActivity.class);
			Contents c = new Contents();
			c.setTitle(data.get(position)[i].title);
			c.setAid(v.getTag().toString());
			c.setChannelId(67);
			intent.putExtra("from", 1);
			intent.putExtra("contents", c);
			context.startActivity(intent);
		}
		
	}

}
