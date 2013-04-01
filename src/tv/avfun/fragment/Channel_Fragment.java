package tv.avfun.fragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import tv.avfun.Channel_Activity;
import tv.avfun.Channell_ContentListViewAdaper;
import tv.avfun.Detail_Activity;
import tv.avfun.R;
import tv.avfun.WebView_Activity;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Channel;



import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class Channel_Fragment extends BaseListFragment implements OnClickListener,OnItemClickListener,OnScrollListener{
	private static final String TAG = "Channel_Fragment";
    private String url;
	private ProgressBar progressBar;
	private TextView time_outtext;
	private ListView list;
	private List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
	private Activity activity;
	private Channell_ContentListViewAdaper adaper;
	private int indexpage = 1;
	private boolean isload = false;
	private View footview;
	private boolean isreload = false;
	private LayoutInflater inflater;
	private int channelid;
	private View main_v;
	public static Channel_Fragment newInstance(String url) {
		Channel_Fragment f = new Channel_Fragment();
		Bundle args = new Bundle();
		args.putString("url", url);
        f.setArguments(args);
        return f;
    }
	
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	this.inflater = inflater;
    	this.main_v = inflater.inflate(R.layout.list_layout, container, false);
		
		return this.main_v;
    }
    

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		url = getArguments().getString("url");
		Channel channel = (Channel) getArguments().getSerializable("channel");
		if(url == null && channel != null){
		    url = channel.getUrl();
		    channelid = channel.getChannelId();
		}else{
		    channelid = Integer.valueOf(url.substring(43, url.length()-13));
		}
		// Log.d(TAG, "url: " + url + "\nchannelid:"+ channelid);
		this.activity = getActivity();
		 progressBar = (ProgressBar) this.main_v.findViewById(R.id.time_progress);
		 time_outtext = (TextView) this.main_v.findViewById(R.id.time_out_text);
		 time_outtext.setOnClickListener(this);
		list = (ListView) this.main_v.findViewById(R.id.list);
		list.setVisibility(View.INVISIBLE);
		list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
		list.setDividerHeight(2);
		footview = this.inflater.inflate(R.layout.list_footerview, list, false);
		footview.setOnClickListener(this);
		list.addFooterView(footview);
		footview.setClickable(false);
		list.setFooterDividersEnabled(false);
		
		LinearLayout listheader = (LinearLayout) this.inflater.inflate(R.layout.list_header, list,false);
		TextView headertitle = (TextView) listheader.findViewById(R.id.listheader_text);
		headertitle.setText("今日最热");
		list.addHeaderView(listheader);
		list.setHeaderDividersEnabled(false);
		
		adaper = new Channell_ContentListViewAdaper(this.activity, data);
		list.setAdapter(adaper);
		list.setOnItemClickListener(this);
		list.setOnScrollListener(this);
		getdatas(1, false);
		
	}
	
	public void getdatas(final int page, final boolean isadd) {
		if(!isadd){
			time_outtext.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
		}
		isload = true;
		new Thread() {
			public void run() {
				try {
					final List<Map<String, Object>> templist = ApiParser.getChannelList(url+page);
					if (!isadd) {
						data = ApiParser.getChannelList("http://www.acfun.tv/api/getlistbyorder.aspx?orderby=7&channelIds="+channelid+"&count=10");
						data.addAll(ApiParser.getChannelList(url+page));
					} 
					activity.runOnUiThread(new Runnable() {
						public void run() {
							
							if (isadd) {
								data.addAll(templist);
							}
							
							if (!isadd) {
								progressBar.setVisibility(View.GONE);
								list.setVisibility(View.VISIBLE);
							} else{

							}
							adaper.setData(data);
							list.setVisibility(View.GONE);
							adaper.notifyDataSetChanged();
							list.setVisibility(View.VISIBLE);
							isload = false;
							isreload = false;
						}
					});

				} catch (Exception e) {
					
					activity.runOnUiThread(new Runnable() {
						public void run() {

							if (!isadd) {
								progressBar.setVisibility(View.GONE);
								time_outtext.setVisibility(View.VISIBLE);
								list.setVisibility(View.INVISIBLE);
							} else {
								isreload = true;
								footview.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
								TextView textview = (TextView) footview.findViewById(R.id.list_footview_text);
								textview.setText(R.string.reloading);
							}
						}
					});
					e.printStackTrace();
				}
			}
		}.start();

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		 if (view.getLastVisiblePosition() == (view.getCount() - 1)&&!isload){
			 indexpage+=1;
			 getdatas(indexpage, true);
		 }
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos,
			long id) {
		
			int position = pos-1;
			if(position == parent.getCount()-2){
				if(isreload){
					footview.findViewById(R.id.list_footview_progress).setVisibility(View.VISIBLE);
					TextView textview = (TextView) footview.findViewById(R.id.list_footview_text);
					textview.setText(R.string.loading);
					getdatas(indexpage, true);
				}
			}else{
				Map<String, Object> map = data.get(position);
				if(Channel_Activity.isarticle){
					 
					Intent intent = new Intent(activity, WebView_Activity.class);
					intent.putExtra("modecode", Channel_Activity.modecode);
					intent.putExtra("aid", map.get("aid").toString());
					intent.putExtra("title", map.get("title").toString());
					intent.putExtra("channelId", map.get("channelId").toString());
					startActivity(intent);
					
				}else{
					Intent intent = new Intent(activity, Detail_Activity.class);
					ImageView img = (ImageView) view.findViewById(R.id.channellist_item_img);
					Drawable da = img.getDrawable();
					BitmapDrawable bd = (BitmapDrawable) da;
					Bitmap bm = bd.getBitmap();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					intent.putExtra("thumb", baos.toByteArray());
					
					intent.putExtra("aid", map.get("aid").toString());
					intent.putExtra("title", map.get("title").toString());
					intent.putExtra("username", map.get("username").toString());
					intent.putExtra("views", map.get("views").toString());
					intent.putExtra("comments", map.get("comments").toString());
					intent.putExtra("channelId", map.get("channelId").toString());
					intent.putExtra("description", map.get("description").toString());
					
					startActivity(intent);
				}

			}

			
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.time_out_text:
			getdatas(1, false);
			break;
		default:
			break;
		}
	}



    public static Fragment newInstance(Channel channel) {
        Channel_Fragment f = new Channel_Fragment();
        Bundle args = new Bundle();
        args.putSerializable("channel", channel);
        f.setArguments(args);
        return f;
    }
	
}
