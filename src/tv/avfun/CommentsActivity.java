package tv.avfun;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpException;

import tv.ac.fun.R;
import tv.avfun.api.ApiParser;
import tv.avfun.api.Login_And_Comments;
import tv.avfun.db.DBService;
import tv.avfun.entity.Comment;
import android.content.Context;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.umeng.analytics.MobclickAgent;

public class CommentsActivity extends SherlockActivity  implements OnClickListener,OnScrollListener,OnItemClickListener{
	private ListView list;
	private String aid;
	private CommentsAdaper2 adaper;
	private int indexpage = 1;
	private int totalpage;
	private boolean isload = false;
	private View footview;
	private boolean isreload = false;
	private ProgressBar progressBar;
	private TextView time_outtext;
	private RelativeLayout relalay;
	private static final int COMMENTID = 301;
	private LinearLayout bottomline;
	private ImageButton send_btn;
	private EditText comment_edit;
	private HashMap<String, Object> umap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.list_layout);
		aid = getIntent().getStringExtra("aid");
		
	    ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("评论");
        
        relalay = (RelativeLayout) findViewById(R.id.list_relative);
        bottomline = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.comments_bottom, null);
        LayoutParams rlpar = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        rlpar.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        relalay.addView(bottomline,rlpar);
        send_btn = (ImageButton) findViewById(R.id.comments_send_btn);
        comment_edit = (EditText) findViewById(R.id.comments_edit);
        send_btn.setOnClickListener(this);
        
        list = (ListView) findViewById(android.R.id.list);
        progressBar = (ProgressBar)findViewById(R.id.time_progress);
		 time_outtext = (TextView)findViewById(R.id.time_out_text);
		 time_outtext.setOnClickListener(this);
		list.setVisibility(View.INVISIBLE);
		list.setDivider(getResources().getDrawable(R.drawable.listview_divider));
		list.setDividerHeight(2);
		footview = LayoutInflater.from(this).inflate(R.layout.list_footerview, list, false);
		footview.setOnClickListener(this);
		list.addFooterView(footview);
		footview.setClickable(false);
		list.setFooterDividersEnabled(false);
		adaper = new CommentsAdaper2(this, data);
		list.setAdapter(adaper);
		list.setOnScrollListener(this);
		list.setOnItemClickListener(this);
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
	public void bottomlinevisible(){
		if(bottomline.getVisibility()==View.GONE){
			bottomline.setVisibility(View.VISIBLE);
		}else if(bottomline.getVisibility()==View.VISIBLE){
			bottomline.setVisibility(View.GONE);
		}
	}
	TreeMap<Integer, Comment> data = new TreeMap<Integer, Comment>();
	public void getdatas(final int page, final boolean isadd) {
		if(!isadd){
			time_outtext.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
		}
		isload = true;
		new Thread() {
			public void run() {
				try {
					final TreeMap<Integer, Comment> tempdata = ApiParser.getComments(aid, page);
					runOnUiThread(new Runnable() {

                        public void run() {
							
							if (isadd) {
							    data.putAll(tempdata);
							} else {
								data = tempdata;
							}
							
							if (!isadd) {
								progressBar.setVisibility(View.GONE);
								list.setVisibility(View.VISIBLE);
							} else{
								
							}
							if(data!=null && data.size()>0){
								totalpage = ApiParser.commentsTotalPage;
								adaper.setData(data);
								adaper.notifyDataSetChanged();
								isload = false;
								isreload = false;
							}else{
								list.setVisibility(View.GONE);
								time_outtext.setEnabled(false);
								time_outtext.setVisibility(View.VISIBLE);
								time_outtext.setText(R.string.nocomments);
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
	
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
//		case CommentsActivity.COMMENTID:
//			bottomlinevisible();
//			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
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
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.time_out_text:
			getdatas(1, false);
			break;
		case R.id.comments_send_btn:
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        if(imm.isActive()){
	            imm.hideSoftInputFromWindow(send_btn.getApplicationWindowToken(),0);
	        }
			umap = new DBService(this).getUser();
			if(islogin()){
				postcomment();
			}else{
				bottomlinevisible();
				Toast.makeText(this, "ﾟ ∀ﾟ)ノ 还没有登陆无法发表评论", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
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
		    
		    Comment c = (Comment) parent.getItemAtPosition(position);
		    comment_edit.setText(">>"+c.cid+" ");
		}
	}
	public boolean islogin(){
		if(umap==null){
			return false;
		}else{
			return true;
		}
	}
	
	public void postcomment(){
		String comment = comment_edit.getEditableText().toString();
		Matcher matcher = Pattern.compile(">>(\\d+)").matcher(comment);
		int cid =  0;
		if(matcher.find()){
		    cid = Integer.parseInt(matcher.group(1));
		    comment = comment.replace(">>"+cid, "");
		}
		final Comment quote = data.get(cid);
		final String rComment = comment;
		if(rComment==""){
			Toast.makeText(this, "评论不能为空哦", Toast.LENGTH_SHORT).show();
			return;
		}
		if(rComment.length()<5){
			Toast.makeText(this, "要五个字以上哦", Toast.LENGTH_SHORT).show();
			return;
		}
		send_btn.setEnabled(false);
		Toast.makeText(CommentsActivity.this, "发送中...", Toast.LENGTH_SHORT).show();
		new Thread(){
			public void run(){
				try {
					final boolean suss = Login_And_Comments.postComments(rComment, quote, aid,(Cookie[])umap.get("cookies"));
					runOnUiThread(new Runnable() {
						public void run() {
							if(suss){
								Toast.makeText(CommentsActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
								send_btn.setEnabled(true);
								bottomlinevisible();
//								Map<String, Object> cmap = new HashMap<String, Object>();
//								cmap.put("userName", umap.get("uname"));
//								cmap.put("content", comment);
//								cmap.put("userImg", cmap.get("avatar"));
//								data.add(0, cmap);
								totalpage =1;
								time_outtext.setVisibility(View.GONE);
								list.setVisibility(View.VISIBLE);
								footview.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
								TextView textview = (TextView) footview.findViewById(R.id.list_footview_text);
								textview.setText(R.string.nomorecomments);
//								adaper.setData(data);
//								adaper.notifyDataSetChanged();
							}
						}
					});
					
				} catch (HttpException e) {
					
					runOnUiThread( new Runnable() {
						@Override
						public void run() {
							
							send_btn.setEnabled(true);
							Toast.makeText(CommentsActivity.this, "(=ﾟωﾟ)= 服务器响应异常...", Toast.LENGTH_SHORT).show();
						}
					});
					e.printStackTrace();
				} catch (UnknownHostException e) {
					
					runOnUiThread( new Runnable() {
						@Override
						public void run() {
							
							send_btn.setEnabled(true);
							Toast.makeText(CommentsActivity.this, "(=ﾟωﾟ)= 网络异常,请检查网络...", Toast.LENGTH_SHORT).show();
						}
					});
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				
			}
			
		}.start();
	}
	
}
