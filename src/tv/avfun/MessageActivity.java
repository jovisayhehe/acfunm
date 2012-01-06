
package tv.avfun;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import tv.avfun.R;
import tv.acfun.util.Util;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MessageActivity extends Activity {
	private ListView listView;
	private EditText editText;
	private Button send_btn;
	private ArrayList<HashMap<String, String>> data;
	private ListAdapter adapter;
	private String url="http://www.cn-wp.com/miku/GetReturn.asp";  
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.fullScreennt(this);
        setContentView(R.layout.message_layout);
        
        Button re_btn = (Button) this.findViewById(R.id.message_return_btn);
        re_btn.setOnClickListener(new ButtonListener());
        
        listView = (ListView) this.findViewById(R.id.listview);
        editText = (EditText) this.findViewById(R.id.edit);
        send_btn = (Button) this.findViewById(R.id.send_btn);
        send_btn.setOnClickListener(new ButtonListener());
        data = new ArrayList<HashMap<String,String>>();
        adapter = new ListAdapter();
        listView.setAdapter(adapter);
        HashMap<String , String> map = new HashMap<String, String>();
		map.put("who", "ac");
		map.put("title", "寂寞吗?要和ac娘聊天吗?ac娘时刻与您相伴!");
		data.add(map);
		adapter.notifyDataSetChanged();
    }
    
    
    private final class ListAdapter extends BaseAdapter{
    	private LayoutInflater mInflater;
    	
    	@SuppressWarnings("unused")
		public ListAdapter() {
    		this.mInflater =LayoutInflater.from(MessageActivity.this);
    	}
    	
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HashMap<String, String> map = data.get(position);
			String who = map.get("who");
			
			if(who.equals("self")){
				convertView = mInflater.inflate(R.layout.messagelist_item,
						null);
			TextView message = (TextView) convertView
						.findViewById(R.id.message_list_item_txt);
			
			message.setText(data.get(position).get("title"));
		
			return convertView;
			}else{
				convertView = mInflater.inflate(R.layout.messagelist_item_l,
						null);
			TextView message = (TextView) convertView
						.findViewById(R.id.message_list_item_txt);
			
			message.setText(data.get(position).get("title"));
		
			return convertView;
			}
			
		}
    	
    }
    
    private final class ButtonListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.send_btn:
				final String title = editText.getText().toString();
				
				HashMap<String , String> map = new HashMap<String, String>();
				map.put("who", "self");
				map.put("title", title);
				data.add(map);
				adapter.notifyDataSetChanged();
				editText.setText("");
				editText.clearFocus();
				listView.setSelection(data.size());
                //隐藏软键盘
				InputMethodManager imm = (InputMethodManager) v.getContext()
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm.isActive()) {
					imm.hideSoftInputFromWindow(v.getApplicationWindowToken(),
							0);
				}
				new Thread(){
					public void run(){		
						try {
							
							final String message = getMessage(title);
							
							runOnUiThread(new Runnable() {
								public void run() {
									HashMap<String , String> map = new HashMap<String, String>();
									map.put("who", "ac");
									map.put("title", message);
									data.add(map);
									adapter.notifyDataSetChanged();
									listView.setSelection(data.size());
								} 
							});	
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							runOnUiThread(new Runnable() {
								public void run() {
									
								} 
							});	
							e.printStackTrace();
						}
					}	
				}.start();
				break;
			case R.id.message_return_btn:
				
				MessageActivity.this.finish();
				break;
			default:
				break;
			}
		}
    	
    }
    
    private String getMessage(String str){
	    final String SERVER_URL = "http://www.cn-wp.com/miku/GetReturn.asp"; 
	    HttpPost request = new HttpPost(SERVER_URL); 
	    List params = new ArrayList();
	    params.add(new BasicNameValuePair("Sentence", str)); // 添加必须的参数
	    try {
			request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			HttpResponse httpResponse = new DefaultHttpClient().execute(request); // 发送请求并获取反馈
	        // 解析返回的内容
	        if (httpResponse.getStatusLine().getStatusCode() != 404)
	        {
	                String result = EntityUtils.toString(httpResponse.getEntity());
	                return result;
	        }else{
	        	return String.valueOf(httpResponse.getStatusLine().getStatusCode());
	        }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return "链接失败。。。";
		}
    }
}