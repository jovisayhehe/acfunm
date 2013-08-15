package tv.avfun;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpException;

import tv.ac.fun.R;
import tv.avfun.CommentsAdaper3.OnQuoteClickListener;
import tv.avfun.api.ApiParser;
import tv.avfun.api.MemberUtils;
import tv.avfun.db.DBService;
import tv.avfun.entity.Comment;
import tv.avfun.util.ArrayUtil;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.umeng.analytics.MobclickAgent;

public class CommentsActivity extends SherlockActivity  implements OnClickListener,OnScrollListener,OnItemClickListener{
	private ListView list;
	private String aid;
	private CommentsAdaper3 adaper;
	private int indexpage = 1;
	private int totalpage;
	private boolean isload = false;
	private View footview;
	private boolean isreload = false;
	private ProgressBar progressBar;
	private TextView time_outtext;
	private ImageButton send_btn;
	private EditText comment_edit;
	private HashMap<String, Object> umap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);
		aid = getIntent().getStringExtra("aid");
		keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
	    ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle("ac"+aid+" / 评论");
        
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
		adaper = new CommentsAdaper3(this, data,commentIdList);
		adaper.setOnClickListener(new OnQuoteClickListener() {
            
            @Override
            public void onClick(View v, int position) {
                
//                String pre = "re: #"+c.count+" ";
//                comment_edit.setText(pre);
//                comment_edit.setSelection(pre.length());
//                comment_edit.requestFocus();
                list.performItemClick(v, position, adaper.getItemId(position));
            }
        });
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
	
	SparseArray<Comment> data = new SparseArray<Comment>();
	List<Integer> commentIdList = new ArrayList<Integer>();
    private InputMethodManager keyboard;
	public void getdatas(final int page, final boolean isadd) {
		if(!isadd){
			time_outtext.setVisibility(View.GONE);
			progressBar.setVisibility(View.VISIBLE);
		}
		isload = true;
		new Thread() {
			public void run() {
				try {
					final SparseArray<Comment> tempdata = ApiParser.getComments(aid, page);
					runOnUiThread(new Runnable() {

                        public void run() {
							
							if (isadd) {
							    ArrayUtil.putAll(tempdata, data);
							} else {
								data = tempdata;
							}
							if(ApiParser.commentIdList != null ) 
							    commentIdList.addAll(ApiParser.commentIdList);
							
							if (!isadd) {
								progressBar.setVisibility(View.GONE);
								list.setVisibility(View.VISIBLE);
							} else{
								
							}
							if(data!=null && data.size()>0){
								totalpage = ApiParser.commentsTotalPage;
								adaper.setData(data,commentIdList);
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
			    //FIXME: 到登录页
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
		}
			else{
		    
		    Comment c = (Comment) parent.getItemAtPosition(position);
		    String pre = "re: #"+c.count+" ";
		    comment_edit.setText(pre);
		    comment_edit.setSelection(pre.length());
		    view.postDelayed(new Runnable() {
                
                @Override
                public void run() {
                    keyboard.showSoftInput(comment_edit, 0);                    
                }
            }, 200);
		    
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
		String comment = comment_edit.getText().toString();
		Matcher matcher = Pattern.compile("re: #(\\d+)").matcher(comment);
		int count = 0;
		if(matcher.find()){
		    count = Integer.parseInt(matcher.group(1));
		    comment = matcher.replaceAll("");
		}
		final Comment quote = data.get(findCid(count));
		final String rComment = comment;
		if(TextUtils.isEmpty(rComment)){
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
					final boolean suss = MemberUtils.postComments(rComment, quote, aid,(Cookie[])umap.get("cookies"));
					runOnUiThread(new Runnable() {
						public void run() {
							if(suss){
								Toast.makeText(CommentsActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
								send_btn.setEnabled(true);
								comment_edit.setText("");
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
								getdatas(1, false);
//								adaper.setData(data,commentIdList);
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
	int findCid(int floorCount){
	    for(int i=0;i<commentIdList.size();i++){
	        int key = commentIdList.get(i);
	        Comment c = data.get(key);
	        if(c.count == floorCount)
	            return c.cid;
	    }
	    return 0;
	}
}
