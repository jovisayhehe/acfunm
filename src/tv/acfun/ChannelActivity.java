package tv.acfun;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tv.acfun.ListViewAdaper.ListViewHolder;
import tv.acfun.util.GetLinkandTitle;

import acfun.domain.Article;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ChannelActivity extends Activity {
	private ListView channellist;
	private ListViewAdaper adaper;
	private GetLinkandTitle geter;
	private TextView TitletextView;
	private ArrayList<Map<String, Object>> listdata;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channellayout);
		
		
		TitletextView = (TextView) findViewById(R.id.title_text);
		TitletextView.setText("频道");
		
		channellist = (ListView) findViewById(R.id.channellistviw);
		channellist.setAdapter(new ChannelListViewAdaper(this));
		
		adaper = new ListViewAdaper(ChannelActivity.this, null);
		channellist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				case 0:
					//文章
					TitletextView.setText("文章");
					adaper.setData(getListData("http://www.acfun.tv/plus/list.php?tid=10"));
					channellist.setAdapter(adaper);
					break;
				case 1:
					//娱乐
					
					break;
				case 2:
					//短影
					
					break;
				case 3:
					//动画
					
					break;
				case 4:
					//音乐
					
					break;
				case 5:
					//游戏
					break;
				case 6:
					//番剧
					
					break;

				default:
					break;
				}
			}
			
		});
		
		
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
	
	
	public final class ChannelListViewAdaper extends BaseAdapter{
		private LayoutInflater mInflater;
		private ArrayList<HashMap<String, Object>> titledata = new ArrayList<HashMap<String,Object>>();
		public ChannelListViewAdaper(Context context) {
			this.mInflater =LayoutInflater.from(context);
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("img", R.drawable.art);
			map.put("txt", "文  章");
			titledata.add(map);
			
			HashMap<String, Object> map1 = new HashMap<String, Object>();
			map1.put("img", R.drawable.fun);
			map1.put("txt", "娱  乐");
			titledata.add(map1);
			
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			map2.put("img", R.drawable.movie);
			map2.put("txt", "短  影");
			titledata.add(map2);
			
			HashMap<String, Object> map3 = new HashMap<String, Object>();
			map3.put("img", R.drawable.an);
			map3.put("txt", "动  画");
			titledata.add(map3);
			
			HashMap<String, Object> map4 = new HashMap<String, Object>();
			map4.put("img", R.drawable.music);
			map4.put("txt", "音  乐");
			titledata.add(map4);
			
			HashMap<String, Object> map5 = new HashMap<String, Object>();
			map5.put("img", R.drawable.game);
			map5.put("txt", "游  戏");
			titledata.add(map5);
			
			HashMap<String, Object> map6 = new HashMap<String, Object>();
			map6.put("img", R.drawable.fanju);
			map6.put("txt", "番  剧");
			titledata.add(map6);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return titledata.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return titledata.get(arg0);
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
				imgview.setBackgroundResource((Integer) titledata.get(arg0).get("img"));
				textView.setText((String) titledata.get(arg0).get("txt"));
				return convertView;
		}
		
	}
	
	 private List<Map<String, Object>> getListData(String address) {
		 
	        listdata = new ArrayList<Map<String, Object>>();      
	        GetLinkandTitle linkandTitle = new GetLinkandTitle();
	        List<Article> arts =  linkandTitle.getNewArtTitleandLink(address);
	        if(!arts.isEmpty()){
	        for(Article art:arts){
	        	Map<String, Object> map = new HashMap<String, Object>();
	        	map.put("title", art.getArttitle());
	        	map.put("link", art.getArtlink());
	        	map.put("hit", "n");
	        	listdata.add(map);
	        }
	        }
	       return listdata;
	    }
	

}
