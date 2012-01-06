package tv.avfun;

import java.util.ArrayList;
import java.util.HashMap;

import tv.avfun.R;
import tv.acfun.db.DBService;
import tv.acfun.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class HistoryActivity extends Activity {
	private ListView listview;
	private ArrayList<HashMap<String, String>> listdata;
	private SimpleAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreennt(this);
		setContentView(R.layout.history_layout);
		listview = (ListView) findViewById(R.id.his_listview);
		Button ret_btn = (Button) findViewById(R.id.his_return_btn);
		Button clear_btn = (Button) findViewById(R.id.his_clean_button);
		
		listdata = new DBService(this).getHiss();
		adapter = new SimpleAdapter(this, listdata, android.R.layout.simple_list_item_1, new String[] {"title"},   
                
                new int[] {android.R.id.text1});
     listview.setAdapter(adapter);
     listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				String id = (String) listdata.get(arg2).get("id");
				ArrayList<String> infos = new ArrayList<String>();
				infos.add(id);
				infos.add("history");
				Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
				intent.putStringArrayListExtra("info", infos);
				HistoryActivity.this.startActivity(intent);
			}
		});
     
		ret_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HistoryActivity.this.finish();
			}
		});
		
		clear_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new DBService(HistoryActivity.this).cleanHis();
				listdata = new DBService(HistoryActivity.this).getHiss();
				listview.setAdapter(new SimpleAdapter(HistoryActivity.this, listdata, android.R.layout.simple_list_item_1, new String[] {"title"},   
		                
		                new int[] {android.R.id.text1}));
			}
		});
	}

}
