package tv.acfun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tv.acfun.ListViewAdaper.ListViewHolder;
import tv.acfun.util.GetLinkandTitle;

import acfun.domain.Article;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class ChannelActivity extends Activity {
	private ListView channellist;
	private ListView channellist_content;
	private Channell_ContentListViewAdaper adaper;
	private GetLinkandTitle geter;
	private TextView TitletextView;
	private Button return_btn;
	private ArrayList<ArrayList<Map<String, Object>>> lists = new ArrayList<ArrayList<Map<String,Object>>>();
	private ArrayList<Boolean> isfrists = new ArrayList<Boolean>();
	private TextView footview;
	private ImageView imgprogress;
	private Animation localAnimation;
	private Animation titleAnimation;
	private int state;
	private int[] pid ={1,1,1,1,1,1,1};
	private TextView titleview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.channel_layout);
		
		for(int i=0;i<7;i++){
			isfrists.add(true);
		}
		
		for(int i=0;i<7;i++){
			lists.add(new ArrayList<Map<String,Object>>());
		}
		
		TitletextView = (TextView) findViewById(R.id.title_text);
		TitletextView.setText("频道");
		
		channellist = (ListView) findViewById(R.id.channellistviw);
		channellist.setAdapter(new ChannelListViewAdaper(this));
		
		localAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh_drawable_default);
		//titleAnimation = AnimationUtils.loadAnimation(this, R.anim.title_press);
		channellist_content = (ListView) findViewById(R.id.channel_content_listviw);
		//LinearLayout.LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		//linearLayout.setLayoutParams(params);
		
		imgprogress = new ImageView(ChannelActivity.this);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.manman);
		imgprogress.setImageBitmap(bitmap);
		
		channellist_content.addFooterView(imgprogress);
		
		imgprogress.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				imgprogress.startAnimation(localAnimation);
				switch (state) {
				case 0:
				pid[state]++;
				String path = "http://www.acfun.tv/m/list.php?pid="+String.valueOf(pid[state])+"&tid=7424&order=no&cid=13";
				addtolist(path, state);
					break;
				case 1:
					pid[state]++;
					String path1 = "http://www.acfun.tv/m/list.php?pid="+String.valueOf(pid[state])+"&tid=16087&order=no&cid=10";
					addtolist(path1, state);
					break;
				case 2:
					pid[state]++;
					String path2 = "http://www.acfun.tv/m/list.php?pid="+String.valueOf(pid[state])+"&tid=3461&order=no&cid=14";
					addtolist(path2, state);
					break;
				case 3:
					pid[state]++;
					String path3 = "http://www.acfun.tv/m/list.php?pid="+String.valueOf(pid[state])+"&tid=17818&order=no&cid=1";
					addtolist(path3, state);
					break;
				case 4:
					pid[state]++;
					String path4 = "http://www.acfun.tv/m/list.php?pid="+String.valueOf(pid[state])+"&tid=16171&order=no&cid=8";
					addtolist(path4, state);
					break;
				case 5:
					pid[state]++;
					String path5 = "http://www.acfun.tv/m/list.php?pid="+String.valueOf(pid[state])+"&tid=16676&order=no&cid=9";
					addtolist(path5, state);
					break;
				case 6:
					pid[state]++;
					String path6 = "http://www.acfun.tv/m/list.php?pid="+String.valueOf(pid[state])+"&tid=6151&order=no&cid=7";
					addtolist(path6, state);
					break;

				default:
					break;
				}
			}
		});
		
		
		adaper = new Channell_ContentListViewAdaper(ChannelActivity.this, lists.get(0));
		channellist_content.setAdapter(adaper);
		
		
		
		return_btn = (Button) findViewById(R.id.channell_return_btn);
		return_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				channellist.setVisibility(View.VISIBLE);
				channellist_content.setVisibility(View.GONE);
				return_btn.setVisibility(View.INVISIBLE);
			}
		});
		
		
		channellist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				titleview = (TextView) view.findViewById(R.id.channelist_item_txt);
				titleview.startAnimation(localAnimation);
				switch (position) {
				case 0:
					//文章
					state=position;
					refreshList("文章", position, "http://www.acfun.tv/m/list.php?cid=13");
					
					break;
				case 1:
					//娱乐
					state=position;
					refreshList("娱乐", position, "http://www.acfun.tv/m/list.php?cid=10");
					break;
				case 2:
					//短影
					state=position;
					refreshList("短影", position, "http://www.acfun.tv/m/list.php?cid=14");
					break;
				case 3:
					//动画
					state=position;
					refreshList("动画", position, "http://www.acfun.tv/m/list.php?cid=1");
					break;
				case 4:
					//音乐
					state=position;
					refreshList("音乐", position, "http://www.acfun.tv/m/list.php?cid=8");
					break;
				case 5:
					//游戏
					state=position;
					refreshList("游戏", position, "http://www.acfun.tv/m/list.php?cid=9");
					break;
				case 6:
					//番剧
					state=position;
					refreshList("番剧", position, "http://www.acfun.tv/m/list.php?cid=7");
					break;

				default:
					break;
				}
				view.clearAnimation();
			}
			
		});
		
		channellist_content.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String id1 = (String) view.getTag(view.getId());
				Toast.makeText(ChannelActivity.this, id1, 1).show();
				
				((MainActivity)ChannelActivity.this.getParent()).addActivity("detail", DetailActivity.class);
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
	
	 private void getListData(String address,int pos) {
		 	
	        GetLinkandTitle linkandTitle = new GetLinkandTitle();
	        List<Article> arts;
			try {
				arts = linkandTitle.getTitleandLink(address);
		        if(!arts.isEmpty()){
			        for(Article art:arts){
			        	Map<String, Object> map = new HashMap<String, Object>();
			        	map.put("title", art.getArttitle());
			        	map.put("link", art.getArtlink());
			        	map.put("art", art.getArt());
			        	map.put("uptime", art.getUptime());
			        	lists.get(pos).add(map);
			        }
			        }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						titleview.clearAnimation();
						Toast.makeText(ChannelActivity.this, "网络连接超时..", 1).show();
					}
				});
				isfrists.remove(pos);
				isfrists.add(pos, true);
				e.printStackTrace();
			}
	    }
	
	 private void refreshList(String title,final int position,final String address){
			TitletextView.setText(title);
			if(isfrists.get(position)){
				isfrists.remove(position);
				isfrists.add(position, false);
				new Thread(){
					public void run(){
						getListData(address,position);
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								adaper.setData(lists.get(position));
								titleview.clearAnimation();
								setVisibility();
								adaper.notifyDataSetInvalidated();
							}
						});
					}
				}.start();
			}else{
				adaper.setData(lists.get(position));
				titleview.clearAnimation();
				setVisibility();
				adaper.notifyDataSetInvalidated();
			}
	 }
	 
	 private void addtolist(final String path,final int sta){
		 new Thread(){
			 public void run(){
				 getListData(path, sta);
				 runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						adaper.setData(lists.get(sta));
						adaper.notifyDataSetChanged();
						imgprogress.clearAnimation();
					}
				});
			 }
		 }.start();
	 }

	public void setVisibility() {
		channellist.setVisibility(View.GONE);
		channellist_content.setVisibility(View.VISIBLE);
		return_btn.setVisibility(View.VISIBLE);
	}
	

}
