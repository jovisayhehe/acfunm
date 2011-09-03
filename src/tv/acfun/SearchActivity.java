package tv.acfun;

import java.io.IOException;
import java.util.ArrayList;

import tv.acfun.util.GetLinkandTitle;

import acfun.domain.SearchResults;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SearchActivity extends Activity implements OnEditorActionListener{
	private EditText search_text;
	private ImageView clear_btn;
	private Button search_btn;
	private ListView search_list;
	private SearchListViewAdaper adapter;
	private ArrayList<SearchResults> data;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_layout);
		search_text = (EditText) findViewById(R.id.search_text);
		clear_btn = (ImageView) findViewById(R.id.clear_search);
		search_btn = (Button) findViewById(R.id.search_button);
		search_list = (ListView) findViewById(R.id.searchlistviw);
		data = new ArrayList<SearchResults>();
		adapter = new SearchListViewAdaper(this, data);
		search_list.setAdapter(adapter);
		search_btn.setEnabled(false);
		search_text.setOnEditorActionListener(this);
		search_text.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				String str = search_text.getText().toString();
				if(str==null||str.length()==0){
					clear_btn.setVisibility(View.GONE);
					search_btn.setEnabled(false);
				}else{
					clear_btn.setVisibility(View.VISIBLE);
					search_btn.setEnabled(true);
				}
				
			}
		});
		
		
		
		clear_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				search_text.setText("");
			}
		});
		
		
		search_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String str = search_text.getText().toString();
				if(str.length()==1){
					Toast.makeText(SearchActivity.this, "关键字最少两个字符", 1).show();
				}else{
					InitList(str);
				}
			}
		});
	}
	
	
	
	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		if(arg1==EditorInfo.IME_ACTION_SEARCH){
			String str = arg0.getText().toString();
			InitList(str);
		}
		return false;
	}
	
	
	
	private void InitList(final String word){
		final GetLinkandTitle linkandTitle = new GetLinkandTitle();
		new Thread(){
			public void run(){
				try {
					data = linkandTitle.GetSearchResults(word, "008d30f9-cdd4-440f-9149-85f5e3a75f42", "-1", 0);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Toast.makeText(SearchActivity.this, "网络超时或异常", 1).show();
					e.printStackTrace();
				}
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						adapter.setData(data);
						adapter.notifyDataSetInvalidated();
					}
				});
			}
		}.start();
		
	}
	
	
	
}
