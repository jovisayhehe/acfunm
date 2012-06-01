package tv.avfun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tv.avfun.R;
import tv.avfun.ListViewAdaper.ListViewHolder;
import tv.acfun.util.GetLinkandTitle;
import tv.acfun.util.Util;

import acfun.domain.AcContent;
import acfun.domain.Article;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
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
	private int[] pid ={1,1,1,1,1,1};
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
				imgprogress.setEnabled(false);
				imgprogress.startAnimation(localAnimation);
				switch (state) {
//				case 0:
//				pid[state]++;
//				String path = "http://www.acfun.tv/api/channel.aspx?query=63&currentPage="+String.valueOf(pid[state]);
//				addtolist(path, state);
//					break;
				case 0:
					pid[state]++;
					String path1 = "http://www.acfun.tv/api/channel.aspx?query=60&currentPage="+String.valueOf(pid[state]);
					addtolist(path1, state);
					break;
				case 1:
					pid[state]++;
					String path2 = "http://www.acfun.tv/api/channel.aspx?query=68&currentPage="+String.valueOf(pid[state]);
					addtolist(path2, state);
					break;
				case 2:
					pid[state]++;
					String path3 = "http://www.acfun.tv/api/channel.aspx?query=1&currentPage="+String.valueOf(pid[state]);
					addtolist(path3, state);
					break;
				case 3:
					pid[state]++;
					String path4 = "http://www.acfun.tv/api/channel.aspx?query=58&currentPage="+String.valueOf(pid[state]);
					addtolist(path4, state);
					break;
				case 4:
					pid[state]++;
					String path5 = "http://www.acfun.tv/api/channel.aspx?query=59&currentPage="+String.valueOf(pid[state]);
					addtolist(path5, state);
					break;
				case 5:
					pid[state]++;
					String path6 = "http://www.acfun.tv/api/channel.aspx?query=67&currentPage="+String.valueOf(pid[state]);
					addtolist(path6, state);
					break;

				default:
					break;
				}
			}
		});
		
		
		adaper = new Channell_ContentListViewAdaper(ChannelActivity.this, lists.get(0),channellist_content);
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
//				case 0:
//					//文章
//					state=position;
//					refreshList("文章", position, "http://www.acfun.tv/api/channel.aspx?query=63&currentPage=1");
//					
//					break;
				case 0:
					//娱乐
					state=position;
					refreshList("娱乐", position, "http://www.acfun.tv/api/channel.aspx?query=60&currentPage=1");
					break;
				case 1:
					//短影
					state=position;
					refreshList("短影", position, "http://www.acfun.tv/api/channel.aspx?query=68&currentPage=1");
					break;
				case 2:
					//动画
					state=position;
					refreshList("动画", position, "http://www.acfun.tv/api/channel.aspx?query=1&currentPage=1");
					break;
				case 3:
					//音乐
					state=position;
					refreshList("音乐", position, "http://www.acfun.tv/api/channel.aspx?query=58&currentPage=1");
					break;
				case 4:
					//游戏
					state=position;
					refreshList("游戏", position, "http://www.acfun.tv/api/channel.aspx?query=59&currentPage=1");
					break;
				case 5:
					//番剧
					state=position;
					refreshList("番剧", position, "http://www.acfun.tv/api/channel.aspx?query=67&currentPage=1");
					break;
				case 6:
					
					new Thread(){
						public void run(){
							try {
								MainActivity.getHotdata("http://124.238.214.35/acfun/hot.html?type=week");
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										titleview.clearAnimation();
										Intent intent = new Intent(ChannelActivity.this, HotActivity.class);
										startActivity(intent);
									}
								});
							} catch (IOException e) {
								// TODO Auto-generated catch block
								titleview.clearAnimation();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										// TODO Auto-generated method stub
										Toast.makeText(ChannelActivity.this, "网络连接超时..", 1).show();
									}
								});
								e.printStackTrace();
							}
						}
					}.start();
					
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
				ArrayList<String> infos = new ArrayList<String>();
				infos.add(id1);
				infos.add("channel");
				infos.add((String) lists.get(state).get(position).get("title"));
				infos.add((String) lists.get(state).get(position).get("art"));
				infos.add((String) lists.get(state).get(position).get("uptime"));
				infos.add((String) lists.get(state).get(position).get("views"));
				infos.add((String) lists.get(state).get(position).get("dsescription"));
				infos.add((String) lists.get(state).get(position).get("stows"));
				TextView tv = (TextView) view.findViewById(R.id.channellist_content_item_title);
				String ti  = tv.getText().toString();
				infos.add(ti);
				Intent intent = new Intent(ChannelActivity.this, DetailActivity.class);
				intent.putStringArrayListExtra("info", infos);
				ImageView img = (ImageView) view.findViewById(R.id.channellist_item_img);
				Drawable da = img.getDrawable();
				BitmapDrawable bd = (BitmapDrawable) da;
				Bitmap bm = bd.getBitmap();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				intent.putExtra("bitmap", baos.toByteArray());
				ChannelActivity.this.startActivity(intent);
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
			
//			HashMap<String, Object> map = new HashMap<String, Object>();
//			map.put("txt", "文  章");
//			titledata.add(map);
			
			HashMap<String, Object> map1 = new HashMap<String, Object>();
			map1.put("txt", "娱  乐");
			titledata.add(map1);
			
			HashMap<String, Object> map2 = new HashMap<String, Object>();
			map2.put("txt", "短  影");
			titledata.add(map2);
			
			HashMap<String, Object> map3 = new HashMap<String, Object>();
			map3.put("txt", "动  画");
			titledata.add(map3);
			
			HashMap<String, Object> map4 = new HashMap<String, Object>();
			map4.put("txt", "音  乐");
			titledata.add(map4);
			
			HashMap<String, Object> map5 = new HashMap<String, Object>();
			map5.put("txt", "游  戏");
			titledata.add(map5);
			
			HashMap<String, Object> map6 = new HashMap<String, Object>();
			map6.put("txt", "番  剧");
			titledata.add(map6);
			
//			HashMap<String, Object> map7 = new HashMap<String, Object>();
//			map7.put("txt", "热  门");
//			titledata.add(map7);
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
				TextView textView = (TextView) convertView.findViewById(R.id.channelist_item_txt);
				textView.setText((String) titledata.get(arg0).get("txt"));
				return convertView;
		}
		
	}
	
	 private void getListData(String address,int pos) throws IOException {
		 	
	        GetLinkandTitle linkandTitle = new GetLinkandTitle();
	        List<AcContent> contents;
	        contents = linkandTitle.getTitleandLink(address);
		        if(!contents.isEmpty()){
			        for(AcContent content:contents){
			        	Map<String, Object> map = new HashMap<String, Object>();
			        	map.put("title", content.getTitle());
			        	map.put("link", content.getUrl());
			        	map.put("art", content.getUsername());
			        	map.put("views", content.getViews());
			        	map.put("uptime", content.getReleaseDate());
			        	map.put("titleimg", content.getTitleImg());
			        	map.put("stows", content.getStows());
			        	map.put("dsescription", content.getDescription());
			        	map.put("id", content.getUrl().substring(3));
			        	lists.get(pos).add(map);
			        }
			        }

	    }
	
	 private void refreshList(String title,final int position,final String address){
			TitletextView.setText(title);
			if(isfrists.get(position)){
				isfrists.remove(position);
				isfrists.add(position, false);
				new Thread(){
					public void run(){
						try {
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
						} catch (IOException e) {
							// TODO Auto-generated catch block
							titleview.clearAnimation();
							isfrists.remove(position);
							isfrists.add(position, true);
							runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(ChannelActivity.this, "网络连接超时..", 1).show();
								}
							});
							e.printStackTrace();
						}
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
				 try {
					getListData(path, sta);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							imgprogress.clearAnimation();
							imgprogress.setEnabled(true);
							Toast.makeText(ChannelActivity.this, "网络连接超时..", 1).show();
						}
					});
					e.printStackTrace();
				}
				 runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						adaper.setData(lists.get(sta));
						adaper.notifyDataSetChanged();
						imgprogress.clearAnimation();
						imgprogress.setEnabled(true);
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
