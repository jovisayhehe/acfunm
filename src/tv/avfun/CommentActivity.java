package tv.avfun;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import tv.acfun.util.GetLinkandTitle;
import tv.acfun.util.Parser;
import tv.acfun.util.Util;
import tv.avfun.R;
import acfun.domain.SearchResults;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class CommentActivity extends Activity {
	private Button re_btn;
	private ListView listview;
	private SimpleAdapter adapter;
	private ArrayList<HashMap<String, String>> data;
	private ImageView imgprogress;
	private int page = 1;
	private String commenturl;
	private Animation imgAnimation;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Util.fullScreennt(this);
		setContentView(R.layout.comment_layout);
		listview = (ListView) this.findViewById(R.id.comment_listview);
		re_btn = (Button) this.findViewById(R.id.comment_return_btn);
		ButtonListener listener = new ButtonListener();
		re_btn.setOnClickListener(listener);
		imgprogress = new ImageView(this);
		imgAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh_drawable_default);
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.manman);
		imgprogress.setImageBitmap(bitmap);
		imgprogress.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!commenturl.equals("")&&commenturl!=""){
					imgprogress.setEnabled(false);
					imgprogress.startAnimation(imgAnimation);
					page+=1;
					addtolist(page);
				}
				
			}
		});
		listview.addFooterView(imgprogress);
		String id = getIntent().getStringExtra("id");
		getComments(id);
	}
	
	
	
	private void getComments(final String id){
		new Thread(){
			public void run(){
				try {
					data = Parser.getComment(id);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Toast.makeText(CommentActivity.this, "网络连接超时..", 1).show();
						}
					});
					e.printStackTrace();
				}
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
							commenturl = data.get(data.size()-1).get("nextpage");
							data.remove(data.size()-1);
							if(!commenturl.equals("")&&commenturl!=""){
								commenturl = commenturl.substring(17);
							}else{
								imgprogress.setVisibility(View.GONE);
							}
						   adapter = new SimpleAdapter(CommentActivity.this, data,R.layout.commentlist_item, new String[] {"comment"},   
					                
					                new int[] {R.id.comment_listview_item_comment});
						    listview.setAdapter(adapter);
					}
				});
			}
		}.start();
	}
	
	 private void addtolist(final int page){
		 new Thread(){
			 public void run(){
				 try {
					 String url = "http://www.acfun.tv/m/art.php?nowpage="+page +commenturl;
					 Parser.getCommentwithpage(url);
					data.addAll(Parser.getCommentwithpage(url));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							imgprogress.clearAnimation();
							imgprogress.setEnabled(true);
							Toast.makeText(CommentActivity.this, "网络连接超时..", 1).show();
						}
					});
					e.printStackTrace();
				}
				 runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						adapter.notifyDataSetChanged();
						imgprogress.clearAnimation();
						imgprogress.setEnabled(true);
					}
				});
			 }
		 }.start();
	 }
	
	private final class ButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.comment_return_btn:
				CommentActivity.this.finish();
				break;
				
			default:
				break;
			}
		}
		
	}
}
