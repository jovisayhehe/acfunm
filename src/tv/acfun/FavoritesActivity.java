package tv.acfun;

import java.util.ArrayList;
import java.util.HashMap;

import tv.acfun.db.DBService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FavoritesActivity extends Activity {
	private ListView listview;
	private ArrayList<HashMap<String, String>> listdata;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fov_layout);
		TextView titel = (TextView) findViewById(R.id.title_text);
		titel.setText("收藏");
		listview = (ListView) findViewById(R.id.fov_listview);
		listdata = new DBService(this).getFovs();
	    SimpleAdapter adapter = new SimpleAdapter(this	, listdata, android.R.layout.simple_list_item_1, new String[] {"title"},   
                 
	                new int[] {android.R.id.text1});
	     listview.setAdapter(adapter);
	     listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				String id = (String) listdata.get(arg2).get("id");
				String atitle = (String) listdata.get(arg2).get("title");
				ArrayList<String> infos = new ArrayList<String>();
				infos.add(id);
				infos.add("favorites");
				infos.add(atitle);
				Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
				intent.putStringArrayListExtra("info", infos);
				FavoritesActivity.this.startActivity(intent);
			}
		});
	}

}
