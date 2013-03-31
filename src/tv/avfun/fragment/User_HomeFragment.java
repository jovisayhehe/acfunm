package tv.avfun.fragment;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpException;
import org.json.external.JSONException;

import tv.avfun.Favorite_Activity;
import tv.avfun.History_Activity;
import tv.avfun.R;
import tv.avfun.Settings_Activity;
import tv.avfun.animation.ExpandCollapseAnimation;
import tv.avfun.api.Login_And_Comments;
import tv.avfun.db.DBService;
import tv.avfun.util.lzlist.ImageLoader;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class User_HomeFragment extends SherlockFragment implements OnClickListener{
	private LinearLayout login_ui;
	private View v;
	private EditText username;
	private EditText password;
	private TextView login_btn;
	private ImageView img_face;
	private TextView textusername;
	private TextView textsignature;
	private HashMap<String, Object> map;
	private ImageLoader imageLoader;
	private TextView vlogin_btn;
	private boolean islogin;
	private Activity activity;
	public static User_HomeFragment newInstance() {
		User_HomeFragment f = new User_HomeFragment();
		Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

		v = inflater.inflate(R.layout.member_layout, container, false);

		return v;
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		super.onActivityCreated(savedInstanceState);
		this.activity = getActivity();
		imageLoader=new ImageLoader(activity);	
		vlogin_btn = (TextView) v.findViewById(R.id.mem_login_vbtn);
		login_ui = (LinearLayout) v.findViewById(R.id.member_line);
		vlogin_btn.setOnClickListener(this);
		
		TextView set_btn = (TextView) v.findViewById(R.id.mem_set_btn);
		set_btn.setOnClickListener(this);
		
		TextView subs_btn = (TextView) v.findViewById(R.id.mem_btn_subs);
		TextView fov_btn = (TextView) v.findViewById(R.id.mem_btn_fov);
		TextView his_btn = (TextView) v.findViewById(R.id.mem_btn_his);
		
		subs_btn.setOnClickListener(this);
		fov_btn.setOnClickListener(this);
		his_btn.setOnClickListener(this);
		
		
		username = (EditText) v.findViewById(R.id.mem_edit_username);
		password = (EditText) v.findViewById(R.id.mem_edit_password);
		login_btn = (TextView) v.findViewById(R.id.mem_login_btn);
		login_btn.setOnClickListener(this);
		
		textusername = (TextView) v.findViewById(R.id.mem_text_username);
		textsignature = (TextView) v.findViewById(R.id.mem_text_signature);
		
		img_face = (ImageView) v.findViewById(R.id.mem_img_face);
		
		map = new DBService(activity).getUser();
		if(map==null){
			islogin = false;
		}else{
			islogin = true;
			buidview(map);
			vlogin_btn.setText("注销");
		}
	}

	@Override
	public void onClick(View v) {
		
		switch (v.getId()) {
		case R.id.mem_login_vbtn:
			if(islogin){
				Toast.makeText(activity, "注销完成", Toast.LENGTH_SHORT).show();
				vlogin_btn.setText("登陆");
				islogin = false;
				unbuidview();
				new DBService(activity).user_cancel();
			}else{
				login_ui_visible();
			}
			break;
		case R.id.mem_set_btn:
			Intent intent = new Intent(activity, Settings_Activity.class);
			activity.startActivity(intent);
			break;
			
		case R.id.mem_btn_subs:
			
			break;
		case R.id.mem_btn_fov:
			
			Intent intent2 = new Intent(activity, Favorite_Activity.class);
			startActivity(intent2);
			
			break;
		case R.id.mem_btn_his:
			Intent intent3 = new Intent(activity, History_Activity.class);
			startActivity(intent3);
			break;
		case R.id.mem_login_btn:
			login();
			break;
		default:
			break;
		}
	}
	
	public void login_ui_visible(){
		if(login_ui.getVisibility()==View.GONE){
			Animation anim = new ExpandCollapseAnimation(login_ui, 600, ExpandCollapseAnimation.EXPAND);
			login_ui.setAnimation(anim);
			login_ui.setVisibility(View.VISIBLE);
		}else if(login_ui.getVisibility()==View.VISIBLE){
			Animation anim = new ExpandCollapseAnimation(login_ui, 600, ExpandCollapseAnimation.COLLAPSE);
			login_ui.setAnimation(anim);
			login_ui.setVisibility(View.GONE);
		}
	}
	
	public void buidview(HashMap<String, Object> vmap){
		textusername.setText((String)vmap.get("uname"));
		textsignature.setText((String)vmap.get("signature"));
		imageLoader.DisplayImage((String)vmap.get("avatar"), img_face);
	}
	
	public void unbuidview(){
		textusername.setText(R.string.boy);
		textsignature.setText(R.string.isboy);
		BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.face);
		Bitmap bm = bd.getBitmap();
		img_face.setImageBitmap(bm);
	}
	
	public void login(){
		final String userstr = username.getEditableText().toString();
		final String passstr = password.getEditableText().toString();
		if(userstr.equals("")){
			Toast.makeText(activity, "用户名不能空", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(passstr.equals("")){
			Toast.makeText(activity, "密码不能空", Toast.LENGTH_SHORT).show();
			return;
		}
		
		vlogin_btn.setEnabled(false);
		vlogin_btn.setText("登陆中");
		login_ui_visible();
        InputMethodManager imm = (InputMethodManager)activity.getSystemService( Context.INPUT_METHOD_SERVICE );
        if ( imm.isActive( ) ) {
            imm.hideSoftInputFromWindow(login_btn.getApplicationWindowToken(),0);
        }
        
		new Thread(){
			public void run(){
				try {
					map = Login_And_Comments.login(userstr, passstr);
					
					activity.runOnUiThread(new Runnable() {
						public void run() {
							boolean success = (Boolean) map.get("success");
							if(success){
								Toast.makeText(activity, "登陆成功", Toast.LENGTH_SHORT).show();
								islogin = true;
								vlogin_btn.setEnabled(true);
								buidview(map);
								vlogin_btn.setText("注销");
								new DBService(activity).saveUser(map);
							}else{
								login_ui_visible();
								vlogin_btn.setEnabled(true);
								vlogin_btn.setText("登陆");
								String result = (String) map.get("result");
								Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
							}
						
						}
					});
					
				} catch (HttpException e) {
					
					e.printStackTrace();
					
					activity.runOnUiThread( new Runnable() {
						
						@Override
						public void run() {
							
							Toast.makeText(activity, "(=ﾟωﾟ)= 服务器想应异常...", Toast.LENGTH_SHORT).show();
							login_ui_visible();
							vlogin_btn.setEnabled(true);
							vlogin_btn.setText("登陆");
						}
					});
					
				} catch (UnknownHostException e) {
					
					activity.runOnUiThread( new Runnable() {
						@Override
						public void run() {
							
							Toast.makeText(activity, "(=ﾟωﾟ)= 网络异常,请检查网络...", Toast.LENGTH_SHORT).show();
							login_ui_visible();
							vlogin_btn.setEnabled(true);
							vlogin_btn.setText("登陆");
						}
					});
					e.printStackTrace();
				} catch (IOException e) {
					
					e.printStackTrace();
				} catch (JSONException e) {
					
					e.printStackTrace();
				}
				
			};
			
		}.start();
		
	}
}
