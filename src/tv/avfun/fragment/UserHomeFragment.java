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
import tv.avfun.animation.ExpandAnimation;
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
import android.support.v4.app.Fragment;
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

public class UserHomeFragment extends Fragment implements OnClickListener {

    private LinearLayout            login_ui;
    private View                    mContent;
    private EditText                username;
    private EditText                password;
    private TextView                vlogin_btn;
    private TextView                login_btn;
    private ImageView               avatar;
    private TextView                textusername;
    private TextView                textsignature;
    private HashMap<String, Object> map; //TODO 又是map =_=#
    private ImageLoader             imageLoader;
    private boolean                 islogin;
    private Activity                activity;

    public static UserHomeFragment newInstance() {
        UserHomeFragment f = new UserHomeFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContent = inflater.inflate(R.layout.member_layout, container, false);

        return mContent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        this.activity = getActivity();
        imageLoader = ImageLoader.getInstance();
        login_ui = (LinearLayout) mContent.findViewById(R.id.member_line);
        ((LinearLayout.LayoutParams)login_ui.getLayoutParams()).bottomMargin = - 100;
        vlogin_btn = (TextView) mContent.findViewById(R.id.mem_login_vbtn);
        
        TextView set_btn = (TextView) mContent.findViewById(R.id.mem_set_btn);
        TextView subs_btn = (TextView) mContent.findViewById(R.id.mem_btn_subs);
        TextView fov_btn = (TextView) mContent.findViewById(R.id.mem_btn_fov);
        TextView his_btn = (TextView) mContent.findViewById(R.id.mem_btn_his);
        
        set_btn.setOnClickListener(this);
        subs_btn.setOnClickListener(this);
        fov_btn.setOnClickListener(this);
        his_btn.setOnClickListener(this);
        

        username = (EditText) mContent.findViewById(R.id.mem_edit_username);
        password = (EditText) mContent.findViewById(R.id.mem_edit_password);
        login_btn = (TextView) mContent.findViewById(R.id.mem_login_btn);
        
        login_btn.setOnClickListener(this);
        vlogin_btn.setOnClickListener(this);

        textusername = (TextView) mContent.findViewById(R.id.mem_text_username);
        textsignature = (TextView) mContent.findViewById(R.id.mem_text_signature);

        avatar = (ImageView) mContent.findViewById(R.id.mem_account_avatar);
        avatar.setOnClickListener(this);
        map = new DBService(activity).getUser();
        if (map == null) {
            islogin = false;
        } else {
            islogin = true;
            buidview(map);
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
        case R.id.mem_account_avatar:
        case R.id.mem_login_vbtn:
            if (islogin) {
                Toast.makeText(activity, "注销完成", Toast.LENGTH_SHORT).show();
                vlogin_btn.setText("登陆");
                islogin = false;
                unbuidview();
                new DBService(activity).user_cancel();
            } else {
                animateLoginUI();
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

    public void animateLoginUI() {
        Animation anim = new ExpandAnimation(login_ui, 600);
        login_ui.startAnimation(anim);
        
    }

    public void buidview(HashMap<String, Object> vmap) {
        textusername.setText((String) vmap.get("uname"));
        textsignature.setText((String) vmap.get("signature"));
        imageLoader.displayImage((String) vmap.get("avatar"), avatar);
    }

    public void unbuidview() {
        textusername.setText(R.string.boy);
        textsignature.setText(R.string.isboy);
        BitmapDrawable bd = (BitmapDrawable) getResources().getDrawable(R.drawable.mem_account_avatar);
        Bitmap bm = bd.getBitmap();
        avatar.setImageBitmap(bm);
    }

    public void login() {
        final String userstr = username.getEditableText().toString();
        final String passstr = password.getEditableText().toString();
        if (userstr.equals("")) {
            Toast.makeText(activity, "用户名不能空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passstr.equals("")) {
            Toast.makeText(activity, "密码不能空", Toast.LENGTH_SHORT).show();
            return;
        }

        vlogin_btn.setEnabled(false);
        vlogin_btn.setText("登陆中");
        InputMethodManager imm = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(login_btn.getApplicationWindowToken(), 0);
        }

        new Thread() {

            public void run() {
                try {
                    map = Login_And_Comments.login(userstr, passstr);

                    activity.runOnUiThread(new Runnable() {

                        public void run() {
                            boolean success = (Boolean) map.get("success");
                            animateLoginUI();
                            if (success) {
                                Toast.makeText(activity, "登陆成功", Toast.LENGTH_SHORT).show();
                                islogin = true;
                                vlogin_btn.setEnabled(true);
                                buidview(map);
                                vlogin_btn.setText("注销");
                                new DBService(activity).saveUser(map);
                            } else {
                                vlogin_btn.setEnabled(true);
                                vlogin_btn.setText("登陆");
                                String result = (String) map.get("result");
                                Toast.makeText(activity, result, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                } catch (HttpException e) {

                    e.printStackTrace();

                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            animateLoginUI();
                            Toast.makeText(activity, "(=ﾟωﾟ)= AC娘不想理你...", Toast.LENGTH_SHORT)
                                    .show();
                            vlogin_btn.setEnabled(true);
                            vlogin_btn.setText("登陆");
                        }
                    });
                } catch (UnknownHostException e) {
                 

                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            animateLoginUI();
                            Toast.makeText(activity, "(=ﾟωﾟ)= 啊！连不上网...", Toast.LENGTH_SHORT)
                                    .show();
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
