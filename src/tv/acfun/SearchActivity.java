package tv.acfun;

import java.io.IOException;
import java.util.ArrayList;

import tv.acfun.util.GetLinkandTitle;

import acfun.domain.SearchResults;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class SearchActivity extends Activity implements OnEditorActionListener{
	private EditText search_text;
	private ImageView clear_btn;
	private Button search_btn;
	private Button searchset_btn;
	private ListView search_list;
	private SearchListViewAdaper adapter;
	private ArrayList<SearchResults> data;
	private Animation localAnimation;
	private LinearLayout setline;
	private Animation mShowAction = null;   
    private Animation mHiddenAction = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_layout);
		
		
		mShowAction = new TranslateAnimation(Animation.ZORDER_BOTTOM, 0.0f,   
                Animation.ZORDER_BOTTOM, 0.0f, Animation.ZORDER_BOTTOM,   
                0.0f, Animation.ZORDER_BOTTOM, 0.0f);   
        mShowAction.setDuration(500);   
        mHiddenAction = new TranslateAnimation(Animation.ZORDER_BOTTOM,   
                0.0f, Animation.ZORDER_BOTTOM, 0.0f,   
                Animation.ZORDER_BOTTOM, 0.0f, Animation.ZORDER_BOTTOM,   
                0.0f);   
        mHiddenAction.setDuration(500);   
		
		search_text = (EditText) findViewById(R.id.search_text);
		clear_btn = (ImageView) findViewById(R.id.clear_search);
		search_btn = (Button) findViewById(R.id.search_button);
		searchset_btn = (Button) findViewById(R.id.searchset_button);
		setline = (LinearLayout) findViewById(R.id.search_set_menu);
		localAnimation = AnimationUtils.loadAnimation(this, R.anim.title_press);
		search_list = (ListView) findViewById(R.id.searchlistviw);
		search_list.setCacheColorHint(0);
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
		
		searchset_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(setline.getVisibility()==View.GONE){
					setline.startAnimation(mShowAction);   
					setline.setVisibility(View.VISIBLE);
				}else if(setline.getVisibility()==View.VISIBLE){
					setline.startAnimation(mHiddenAction);   
					setline.setVisibility(View.GONE);
				}
				
			}
		});
		
		search_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				String id1 = (String) view.getTag(view.getId());
				ArrayList<String> infos = new ArrayList<String>();
				infos.add(id1);
				infos.add("search");
				Intent intent = new Intent(SearchActivity.this, DetailActivity.class);
				intent.putStringArrayListExtra("info", infos);
				SearchActivity.this.startActivity(intent);
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
		search_btn.startAnimation(localAnimation);
		final GetLinkandTitle linkandTitle = new GetLinkandTitle();
		new Thread(){
			public void run(){
				try {
					data = linkandTitle.GetSearchResults(word, "088d7595-3b27-46c6-a7c5-0cb1bb5dbcff", "-1", 0);
					search_btn.clearAnimation();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							search_btn.clearAnimation();
							Toast.makeText(SearchActivity.this, "网络连接超时..", 1).show();
						}
					});
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
