package tv.avfun;

import java.io.IOException;
import java.util.ArrayList;

import tv.avfun.R;
import tv.acfun.util.Util;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class HotActivity extends Activity {
	private TextView wtetext;
	private TextView mtetext;
	private TextView ytetext;
	private TextView htetext;
	
	private TextView antext;
	private TextView mutext;
	private TextView gatext;
	private TextView futext;
	private TextView attext;
	private TextView fltext;
	
	private Button re_btn;
	private ArrayList<TextView> texts = new ArrayList<TextView>();
	private ArrayList<TextView> ptexts = new ArrayList<TextView>();
	
	private ListView listview;
	private HotListViewAdaper spadapter;
	
	private HorizontalScrollView hscroview;
	
	private Animation localAnimation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreennt(this);
		setContentView(R.layout.hot_layout);
		wtetext = (TextView) this.findViewById(R.id.hot_wtext);
		mtetext = (TextView) this.findViewById(R.id.hot_mtext);
		ytetext = (TextView) this.findViewById(R.id.hot_ytext);
		htetext = (TextView) this.findViewById(R.id.hot_htext);
		TextListener listener = new TextListener();
		wtetext.setOnClickListener(listener);
		mtetext.setOnClickListener(listener);
		ytetext.setOnClickListener(listener);
		htetext.setOnClickListener(listener);
		texts.add(wtetext);
		texts.add(mtetext);
		texts.add(ytetext);
		texts.add(htetext);
		
		
		antext = (TextView) this.findViewById(R.id.an_htext);
		mutext = (TextView) this.findViewById(R.id.mu_htext);
		gatext = (TextView) this.findViewById(R.id.ga_htext);
		futext = (TextView) this.findViewById(R.id.fu_htext);
		attext = (TextView) this.findViewById(R.id.at_htext);
		fltext = (TextView) this.findViewById(R.id.fl_htext);
		PTextListener listener2 = new PTextListener();
		antext.setOnClickListener(listener2);
		mutext.setOnClickListener(listener2);
		gatext.setOnClickListener(listener2);
		futext.setOnClickListener(listener2);
		attext.setOnClickListener(listener2);
		fltext.setOnClickListener(listener2);
		
		ptexts.add(antext);
		ptexts.add(mutext);
		ptexts.add(gatext);
		ptexts.add(futext);
		ptexts.add(attext);
		ptexts.add(fltext);
		localAnimation = AnimationUtils.loadAnimation(this, R.anim.title_press);
		re_btn = (Button) this.findViewById(R.id.hot_return_btn);
		re_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HotActivity.this.finish();
			}
		});
		listview = (ListView) this.findViewById(R.id.hot_listview);
		
		spadapter = new HotListViewAdaper(this, MainActivity.hotdata.get(0));
		listview.setAdapter(spadapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String id1 = (String) view.getTag(view.getId());
				ArrayList<String> infos = new ArrayList<String>();
				infos.add(id1);
				infos.add("hot");
				TextView tv = (TextView) view.findViewById(R.id.hotlist_item_text);
				String ti  = tv.getText().toString();
				infos.add(ti);
				Intent intent = new Intent(HotActivity.this, DetailActivity.class);
				intent.putStringArrayListExtra("info", infos);
				HotActivity.this.startActivity(intent);
			}
			
		});
		
		hscroview = (HorizontalScrollView) this.findViewById(R.id.hot_bottom_sv);
		
		wtetext.setBackgroundColor(Color.parseColor("#262626"));
		wtetext.setEnabled(false);
		antext.setBackgroundColor(Color.parseColor("#000000"));
		antext.setEnabled(false);
	}
	
	
	private final class TextListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			for(TextView tv:texts){
				if(tv!=v){
					tv.setEnabled(true);
					tv.setBackgroundResource(R.drawable.hot_part_item_bg);
				}
				v.setEnabled(false);
				v.setBackgroundColor(Color.parseColor("#262626"));
				
			}
			MainActivity.hotdata.clear();
			switch (v.getId()) {
			case R.id.hot_wtext:
				refreshhotdata("http://124.238.214.35/acfun/hot.html?type=week",v);
				
				break;
			case R.id.hot_mtext:
				refreshhotdata("http://124.238.214.35/acfun/hot.html?type=month",v);
				
				break;
			case R.id.hot_ytext:
				refreshhotdata("http://124.238.214.35/acfun/hot.html?type=year",v);
				
				break;
			case R.id.hot_htext:
				refreshhotdata("http://124.238.214.35/acfun/hot.html?type=all",v);
				break;

			default:
				break;
			}
		}
		
	}
	private final class PTextListener implements OnClickListener{
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			for(TextView tv:ptexts){
				if(tv!=v){
					tv.setEnabled(true);
					tv.setBackgroundResource(R.drawable.hot_part_itemt_bg);
				}
				v.setBackgroundColor(Color.parseColor("#000000"));
				v.setEnabled(false);
			}
			
			switch (v.getId()) {
			case R.id.an_htext:
				spadapter.setData(MainActivity.hotdata.get(0));
				spadapter.notifyDataSetInvalidated();
				break;
			case R.id.mu_htext:
				spadapter.setData(MainActivity.hotdata.get(1));
				spadapter.notifyDataSetInvalidated();
				break;
			case R.id.ga_htext:
				spadapter.setData(MainActivity.hotdata.get(2));
				spadapter.notifyDataSetInvalidated();
				break;
			case R.id.fu_htext:
				spadapter.setData(MainActivity.hotdata.get(3));
				spadapter.notifyDataSetInvalidated();
				break;
			case R.id.at_htext:
				spadapter.setData(MainActivity.hotdata.get(4));
				spadapter.notifyDataSetInvalidated();
				break;
			case R.id.fl_htext:
				spadapter.setData(MainActivity.hotdata.get(5));
				spadapter.notifyDataSetInvalidated();
				break;

			default:
				break;
			}
			listview.setSelection(0);
		}
		
	}
	
	private void refreshhotdata(final String address,final View v){
		v.startAnimation(localAnimation);
		setEnabledsll();
		new Thread(){
			public void run(){
				try {
					MainActivity.getHotdata(address);
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							v.clearAnimation();
							spadapter.setData(MainActivity.hotdata.get(0));
							spadapter.notifyDataSetInvalidated();
							listview.setSelection(0);
							initptexts();
						}
					});
				} catch (IOException e) {
					// TODO Auto-generated catch block
					v.clearAnimation();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(HotActivity.this, "网络连接超时..", 1).show();
						}
					});
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	private void setEnabledsll(){
		for(TextView v:ptexts){
			v.setEnabled(false);
		}
	}
	
	private void initptexts(){
		for(TextView tv1:ptexts){
			if(tv1!=antext){
				tv1.setEnabled(true);
				tv1.setBackgroundResource(R.drawable.hot_part_itemt_bg);
			}
			antext.setBackgroundColor(Color.parseColor("#000000"));
			antext.setEnabled(false);
		}
		hscroview.fling(-2000);
	}
}
