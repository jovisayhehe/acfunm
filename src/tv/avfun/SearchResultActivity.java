package tv.avfun;



import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.avfun.api.ApiParser;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.umeng.analytics.MobclickAgent;

public class SearchResultActivity extends BaseListActivity  implements OnClickListener,OnItemClickListener,OnScrollListener{
	private String word;
	private ProgressBar progressBar;
	private TextView time_outtext;
	private ListView list;
	private List<Map<String, Object>> data = new ArrayList<Map<String,Object>>();
	private Channell_ContentListViewAdaper adaper;
	private int indexpage = 1;
	private boolean isload = false;
	private View footview;
	private int totalpage;
	private boolean isreload = false;
	private int channelId;
	private ArrayList<Object> objs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		   Intent intent = getIntent();
		    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		    	word = intent.getStringExtra(SearchManager.QUERY);
		    }
		    
		    ActionBar ab = getSupportActionBar();
	        ab.setDisplayHomeAsUpEnabled(true);
	        ab.setTitle("搜索结果");
	        
			 progressBar = (ProgressBar)findViewById(R.id.time_progress);
			 time_outtext = (TextView)findViewById(R.id.time_out_text);
			 time_outtext.setOnClickListener(this);
			list = (ListView)findViewById(R.id.list);
			list.setVisibility(View.INVISIBLE);
			list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
			list.setDividerHeight(2);
			footview = LayoutInflater.from(this).inflate(R.layout.list_footerview, list, false);
			footview.setOnClickListener(this);
			list.addFooterView(footview);
			footview.setClickable(false);
			list.setFooterDividersEnabled(false);
			adaper = new Channell_ContentListViewAdaper(this, data);
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

					if (isadd) {
						data.addAll((List<Map<String, Object>>)ApiParser.getSearchResults(word, page).get(0));
					} else {
						objs = ApiParser.getSearchResults(word, page);
					}
					runOnUiThread(new Runnable() {
						public void run() {
							if (!isadd) {
								progressBar.setVisibility(View.GONE);
								list.setVisibility(View.VISIBLE);
							} else{

							}
							if(objs!=null){
								totalpage = (Integer) objs.get(1);
								data = (List<Map<String, Object>>) objs.get(0);
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
					// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		if(position == parent.getCount()-1){
			if(isreload){
				footview.findViewById(R.id.list_footview_progress).setVisibility(View.VISIBLE);
				TextView textview = (TextView) footview.findViewById(R.id.list_footview_text);
				textview.setText(R.string.loading);
				getdatas(indexpage, true);
			}
		}else{
			channelId = (Integer) data.get(position).get("channelId");
			if(channelId!=63&&channelId!=73&&channelId!=74&&channelId!=75){
				
				Intent intent = new Intent(this, Detail_Activity.class);
				ImageView img = (ImageView) view.findViewById(R.id.channellist_item_img);
				Drawable da = img.getDrawable();
				BitmapDrawable bd = (BitmapDrawable) da;
				Bitmap bm = bd.getBitmap();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				intent.putExtra("thumb", baos.toByteArray());
				intent.putExtra("aid", data.get(position).get("aid").toString());
				intent.putExtra("title", data.get(position).get("title").toString());
				intent.putExtra("username", data.get(position).get("username").toString());
				intent.putExtra("views", data.get(position).get("views").toString());
				intent.putExtra("comments", data.get(position).get("comments").toString());
				intent.putExtra("description", data.get(position).get("description").toString());
				intent.putExtra("channelId", String.valueOf(channelId));
				startActivity(intent);
			}else{
				
				Intent intent = new Intent(SearchResultActivity.this, WebView_Activity.class);
				intent.putExtra("modecode", Channel_Activity.modecode);
				intent.putExtra("aid", data.get(position).get("aid").toString());
				intent.putExtra("title", data.get(position).get("title").toString());
				intent.putExtra("channelId", String.valueOf(channelId));
				startActivity(intent);
			}

		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.time_out_text:
			getdatas(1, false);
			break;
		default:
			break;
		}
	}

}
