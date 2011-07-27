package tv.acfun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.acfun.ListViewAdaper.ListViewHolder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ChannelActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channellayout);
		
		TextView textView = (TextView) findViewById(R.id.title_text);
		textView.setText("频道");
		
		ListView channellist = (ListView) findViewById(R.id.channellistviw);
		channellist.setAdapter(new ChannelListViewAdaper(this));
		
		
		
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);

        animation = new TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, -1.0f,Animation.RELATIVE_TO_SELF, 0.0f
        );
        animation.setDuration(100);
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        channellist.setLayoutAnimation(controller);
		
	}
	
	
	public class ChannelListViewAdaper extends BaseAdapter{
		private LayoutInflater mInflater;
		private ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String,Object>>();
		public ChannelListViewAdaper(Context context) {
			this.mInflater =LayoutInflater.from(context);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("img", R.drawable.art);
			map.put("txt", "文  章");
			data.add(map);
			
			HashMap<String, Object> map1 = new HashMap<String, Object>();
			map1.put("img", R.drawable.fun);
			map1.put("txt", "娱  乐");
			data.add(map1);
			
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			map2.put("img", R.drawable.movie);
			map2.put("txt", "短  影");
			data.add(map2);
			
			HashMap<String, Object> map3 = new HashMap<String, Object>();
			map3.put("img", R.drawable.an);
			map3.put("txt", "动  画");
			data.add(map3);
			
			HashMap<String, Object> map4 = new HashMap<String, Object>();
			map4.put("img", R.drawable.music);
			map4.put("txt", "音  乐");
			data.add(map4);
			
			HashMap<String, Object> map5 = new HashMap<String, Object>();
			map5.put("img", R.drawable.game);
			map5.put("txt", "游  戏");
			data.add(map5);
			
			HashMap<String, Object> map6 = new HashMap<String, Object>();
			map6.put("img", R.drawable.fanju);
			map6.put("txt", "番  剧");
			data.add(map6);
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
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
				convertView = mInflater.inflate(R.layout.channellist_item, null);
				ImageView imgview = (ImageView) convertView.findViewById(R.id.channelist_item_img);
				TextView textView = (TextView) convertView.findViewById(R.id.channelist_item_txt);
				imgview.setBackgroundResource((Integer) data.get(arg0).get("img"));
				textView.setText((String) data.get(arg0).get("txt"));
				return convertView;
		}
		
	}

}
