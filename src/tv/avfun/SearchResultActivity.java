package tv.avfun;



import java.util.ArrayList;
import java.util.List;

import tv.ac.fun.R;
import tv.avfun.adapter.ChannelContentListAdaper;
import tv.avfun.api.ApiParser;
import tv.avfun.api.ChannelApi;
import tv.avfun.entity.Contents;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.umeng.analytics.MobclickAgent;

public class SearchResultActivity extends BaseListActivity  implements OnClickListener,OnItemClickListener,OnScrollListener{
	private String word;
	private ProgressBar progressBar;
	private TextView time_outtext;
	private ListView list;
	private List<Contents> data = new ArrayList<Contents>();
	private ChannelContentListAdaper adaper;
	private int indexpage = 1;
	private boolean isload = false;
	private View footview;
	private int totalpage;
	private boolean isreload = false;
	private int channelId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		   Intent intent = getIntent();
		    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		    	word = intent.getStringExtra(SearchManager.QUERY);
		    }
		    
		    ActionBar ab = getSupportActionBar();
	        ab.setDisplayHomeAsUpEnabled(true);
	        ab.setTitle("搜索结果");
	        MobclickAgent.onEvent(this, "view_search");
			 progressBar = (ProgressBar)findViewById(R.id.time_progress);
			 time_outtext = (TextView)findViewById(R.id.time_out_text);
			 time_outtext.setOnClickListener(this);
			list = (ListView)findViewById(android.R.id.list);
			list.setVisibility(View.INVISIBLE);
			list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
			list.setDividerHeight(2);
			footview = LayoutInflater.from(this).inflate(R.layout.list_footerview, list, false);
			footview.setOnClickListener(this);
			list.addFooterView(footview);
			footview.setClickable(false);
			list.setFooterDividersEnabled(false);
			adaper = new ChannelContentListAdaper(this, data,false);
			list.setAdapter(adaper);
			list.setOnItemClickListener(this);
			list.setOnScrollListener(this);
			getdatas(1, false);
	}
	
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onResume(this);
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}
	
	public void getdatas(final int page, final boolean isadd) {
		if(!isadd){
			time_outtext.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
		}
		isload = true;
		new Thread() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
				    final List<Contents> tempdata = ApiParser.getSearchContents(word, page);
				    if(tempdata != null){
				        // isadd标记为false，将data数据清空，这可能是个dead code
				        if(!isadd && !data.isEmpty()){
				            data.clear();
				        }
				        // 无论data是否为空，标记是否为true，都将数据加到data中
				        data.addAll(tempdata);
				    }
					runOnUiThread(new Runnable() {
						public void run() {
							if (!isadd) {
								progressBar.setVisibility(View.GONE);
								list.setVisibility(View.VISIBLE);
							}
							if(tempdata!=null){
								totalpage = ApiParser.getCountPage();
								adaper.setData(data);
								adaper.notifyDataSetChanged();
								isload = false;
								isreload = false;
							}else{
								list.setVisibility(View.GONE);
								time_outtext.setEnabled(false);
								time_outtext.setVisibility(View.VISIBLE);
								time_outtext.setText(R.string.noresult);
							}
						}
					});

				} catch (Exception e) {
					
					runOnUiThread(new Runnable() {
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
	public boolean onCreateOptionsMenu(Menu menu) {
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
	}
	

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		
		 if (view.getLastVisiblePosition() == (view.getCount() - 1)&&!isload){
			 indexpage+=1;
			 if(indexpage>totalpage){
					footview.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
					TextView textview = (TextView) footview.findViewById(R.id.list_footview_text);
					textview.setText(R.string.nomorecomments);
			 }else{
				 getdatas(indexpage, true);
			 }
			
		 }
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if(position == parent.getCount()-1){
			if(isreload){
				footview.findViewById(R.id.list_footview_progress).setVisibility(View.VISIBLE);
				TextView textview = (TextView) footview.findViewById(R.id.list_footview_text);
				textview.setText(R.string.loading);
				getdatas(indexpage, true);
			}
		}else{
		    Contents c = data.get(position);
			channelId = c.getChannelId();
			if(channelId!= ChannelApi.id.ARTICLE.AN_CULTURE && channelId!=ChannelApi.id.ARTICLE.COLLECTION 
			        &&channelId!=ChannelApi.id.ARTICLE.COMIC_LIGHT_NOVEL &&channelId!=ChannelApi.id.ARTICLE.WORK_EMOTION){
				
				Intent intent = new Intent(this, DetailActivity.class);
				intent.putExtra("contents", c);
				startActivity(intent);
			}else{
				
				Intent intent = new Intent(SearchResultActivity.this, WebViewActivity.class);
				intent.putExtra("modecode", ChannelActivity.modecode);
				intent.putExtra("aid", c.getAid());
				intent.putExtra("title", c.getTitle());
				intent.putExtra("channelId", channelId);
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

}
