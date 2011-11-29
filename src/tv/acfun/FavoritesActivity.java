package tv.acfun;

import java.util.ArrayList;
import java.util.HashMap;

import tv.acfun.db.DBService;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FavoritesActivity extends Activity {
	private ListView listview;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fov_layout);
		TextView titel = (TextView) findViewById(R.id.title_text);
		titel.setText("收藏");
		listview = (ListView) findViewById(R.id.fov_listview);
		ArrayList<HashMap<String, String>> listdata = new ArrayList<HashMap<String,String>>();
		listdata = new DBService(this).getFovs();
	     SimpleAdapter adapter = new SimpleAdapter(this	, listdata, android.R.layout.simple_list_item_1, new String[] {"title"},   
                 
	                new int[] {android.R.id.text1});
	     listview.setAdapter(adapter);
	}

}
