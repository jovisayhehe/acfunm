/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tv.acfun.video;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import tv.ac.fun.BuildConfig;
import tv.ac.fun.R;
import tv.acfun.video.adapter.MenuAdapter;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Category;
import tv.acfun.video.entity.User;
import tv.acfun.video.fragment.ChannelFragment;
import tv.acfun.video.fragment.FavoritesFragment;
import tv.acfun.video.fragment.HistoryFragment;
import tv.acfun.video.fragment.HomeFragment;
import tv.acfun.video.fragment.NotCompleteFragment;
import tv.acfun.video.fragment.PushContentFragment;
import tv.acfun.video.fragment.SearchFragment;
import tv.acfun.video.fragment.VideosFragment;
import tv.acfun.video.util.CommonUtil;
import tv.acfun.video.util.net.CategoriesRequest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.umeng.analytics.MobclickAgent;
import com.umeng.fb.FeedbackAgent;
import com.umeng.fb.model.Conversation;
import com.umeng.fb.model.Conversation.SyncListener;
import com.umeng.fb.model.DevReply;
import com.umeng.fb.model.Reply;
import com.umeng.update.UmengUpdateAgent;


/**
 * @author Yrom
 *
 */
public class HomeActivity extends ActionBarActivity implements OnItemClickListener {
    
    private static final String STACK_NAME = "p";
    private DrawerLayout mDrawer;
    private ListView mMenuList;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mAvatarFrame;
    private ImageView mAvatar;
    private TextView mNameText;
    private static String KEY_STATE_POSITION = "key_state_position";
    public static String[] sTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mUser = AcApp.getUser();
        mAvatarFrame = findViewById(R.id.avatar);
        mAvatar = (ImageView) mAvatarFrame.findViewById(android.R.id.icon);
        mNameText = (TextView) mAvatarFrame.findViewById(android.R.id.text1);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.RIGHT);
        initDrawer();
        mMenuList = (ListView) findViewById(android.R.id.list);
        mProgress = findViewById(android.R.id.progress);
        mMenuList.setOnItemClickListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  
                mDrawer,         
                R.drawable.ic_drawer, 
                R.string.drawer_open,  
                R.string.drawer_close 
                ) {

            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(getTitle());
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(R.string.drawer_open);
            }
        };
        mDrawer.setDrawerListener(mDrawerToggle);
        if(sTitles == null)
            sTitles = getResources().getStringArray(R.array.titles);
        if(sCategories != null){
            ListAdapter adapter = new MenuAdapter(HomeActivity.this, sCategories);
            mMenuList.setAdapter(adapter);
            mProgress.setVisibility(View.GONE);
            ((ViewGroup) findViewById(R.id.content_frame)).removeAllViews();
            int position = savedInstanceState==null? 0 : savedInstanceState.getInt(KEY_STATE_POSITION, 0);
            select(position);
        }
        setUserInfo();
        initUmeng();
    }

    private void initDrawer() {
        try {
            Field mDragger = mDrawer.getClass().getDeclaredField("mLeftDragger");
            mDragger.setAccessible(true);
            ViewDragHelper draggerObj = (ViewDragHelper) mDragger.get(mDrawer);
            Field mEdgeSize = draggerObj.getClass().getDeclaredField("mEdgeSize");
            mEdgeSize.setAccessible(true);
            int edge = mEdgeSize.getInt(draggerObj);
            mEdgeSize.setInt(draggerObj, edge * 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUmeng() {
        MobclickAgent.setDebugMode(BuildConfig.DEBUG);
        UmengUpdateAgent.update(this);
        MobclickAgent.onError(this);
        SyncListener listener = new Conversation.SyncListener() {

            @Override
            public void onSendUserReply(List<Reply> replyList) {
            }

            @Override
            public void onReceiveDevReply(List<DevReply> replyList) {
                if(replyList == null || replyList.isEmpty()){
                    return;
                }
                Intent intent = new Intent(HomeActivity.this, ConversationActivity.class);
                String text = replyList.get(0).getContent();
                AcApp.showNotification(intent, 
                        R.id.comments_content, 
                        text, 
                        R.drawable.notify_chat,
                        getString(R.string.umeng_fb_notification_ticker_text));
            }
        };
        new FeedbackAgent(this).getDefaultConversation().sync(listener);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int checkedItemPosition = mMenuList.getCheckedItemPosition();
        if(checkedItemPosition != sTitles.length -1)
            outState.putInt(KEY_STATE_POSITION, checkedItemPosition);
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(sCategories == null || sCategories.isEmpty()){
            Request<?> request = new CategoriesRequest(listener, errorListener);
            AcApp.addRequest(request);
        }
    }
    private static List<Category> sCategories;
    
    Listener<List<Category>> listener = new Listener<List<Category>>() {

        @Override
        public void onResponse(List<Category> response) {
            mProgress.setVisibility(View.GONE);
            for(int i=sTitles.length-1; i>=0 ;i--){
                String title = sTitles[i];
                Category cat = new Category(1023+i, title);
                response.add(0, cat);
            }
            sCategories = response;
            ListAdapter adapter = new MenuAdapter(HomeActivity.this, sCategories);
            mMenuList.setAdapter(adapter);
            ((ViewGroup) findViewById(R.id.content_frame)).removeAllViews();
            select(0);
        }
        
    };
    
    ErrorListener errorListener = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            try {
                InputStream stream = getAssets().open("cats.json");
                List<Category> array = JSON.parseArray(CommonUtil.getString(stream), Category.class);
                listener.onResponse(array);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }};
    private View mProgress;
    private User mUser;
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...
        switch (item.getItemId()) {
        case R.id.action_settings:
            SettingsActivity.start(this);
            break;
        case R.id.action_feedback:
            startActivity(new Intent(this, ConversationActivity.class));
            break;
        case R.id.action_download_manager:
            startActivity(new Intent(this, DownloadManActivity.class));
            
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position == sTitles.length-1){
            ((ListView)parent).smoothScrollBy(view.getTop(), 100);
        }else
            select(position);
        
    }
    
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        getSupportActionBar().setTitle(title);
    }
    
    private void select(int position){
        Category cat = sCategories.get(position);
        Fragment f = null;
        switch (cat.id) {
        case 1023:
            f = new HomeFragment();
            break;
        case 1025:
            f = new PushContentFragment();
            break;
        case 1026:
            f = new FavoritesFragment();
            break;
        case 1027:
            f = new HistoryFragment();
            break;
        case 1028:
            f = NotCompleteFragment.newInstance(cat.id);
            break;
        case 1029:
            f = new SearchFragment();
            break;
        default:
            if (cat.id != 63 || !handleArea63Click()) {
                f = getFragment(cat);
            }
            break;
        }
        if(f == null) return;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, f);
        // pop stack
        getSupportFragmentManager().popBackStack(STACK_NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        if(cat.id != 1023){
            transaction.addToBackStack(STACK_NAME);
        }
        transaction.commit();
        mDrawer.closeDrawer(GravityCompat.START);
        setTitle(cat.name);
        mMenuList.setItemChecked(position, true);
    }
    public void selectFragmentByChannelId(int id){
        int position = findChannelPosition(id);
        select(position);
    }
    private Fragment getFragment(Category cat) {
        Fragment f = null;
        Bundle args = new Bundle();
        if(cat.subclasse != null && !cat.subclasse.isEmpty()){
            /*
             * 有子分类的
             */
            f = new ChannelFragment();
            int[] ids = new int[cat.subclasse.size()];
            for(int i =0;i<ids.length;i++){
                ids[i] = cat.subclasse.get(i).id;
            }
            args.putIntArray(API.EXTRAS_CATEGORY_IDS, ids);
            f.setArguments(args);
            f.setRetainInstance(true);
            
        }else{
            f = VideosFragment.newInstance(cat);
        }
        
        return f;
    }
    private boolean handleArea63Click() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        AcApp.startArea63(this, "tv.acfun.a63.MainActivity", intent);
        return true;
    }
    public String findChannelNameById(int channelId){
        if(sCategories == null || sCategories.isEmpty())
            return null;
        
        for(int i=0;i<sCategories.size();i++){
            Category category = sCategories.get(i);
            if(category.id == channelId)
                return category.name;
            if(category.subclasse != null)
            for (int j = 0; j < category.subclasse.size(); j++) {
                Category category2 = category.subclasse.get(j);
                if(category2.id == channelId)
                    return category2.name;
            }
        }
        return null;
    }
    public boolean isArticleChannel(int channelId){
        if(channelId == 63) return true;
        for(int i=0;i<sCategories.size();i++){
            Category category = sCategories.get(i);
            if(category.id == 63){
                for (int j = 0; j < category.subclasse.size(); j++) {
                    Category category2 = category.subclasse.get(j);
                    if(category2.id == channelId)
                        return true;
                }
            }
        }
        return false;
    }
    
    public int findChannelPosition(int channelId){
        for(int i=0;i<sCategories.size();i++){
            Category category = sCategories.get(i);
            if(category.id == channelId)
                return i;
            if(category.subclasse != null)
            for (int j = 0; j < category.subclasse.size(); j++) {
                Category category2 = category.subclasse.get(j);
                if(category2.id == channelId)
                    return i;
            }
        }
        return 0;
    }
    public void onAvatarClick(View v){
        if(mUser == null)
            startActivityForResult(SigninActivity.createIntent(this.getApplicationContext()),SigninActivity.REQUEST_SIGN_IN);
        else{
            DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == DialogInterface.BUTTON_POSITIVE){
                        mUser = null;
                        AcApp.logout();
                        invalidateAvatarFrame();
                    }
                    dialog.dismiss();
                }
            };
            new AlertDialog.Builder(this)
                .setTitle("确定要注销吗？")
                .setMessage("注销后无法同步收藏和发表评论")
                .setNegativeButton("取消", clickListener)
                .setPositiveButton("注销", clickListener).show();
        }
    }
    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if(result == RESULT_OK ){
            if(request == SigninActivity.REQUEST_SIGN_IN){
                mUser = data.getExtras().getParcelable("user");
                setUserInfo();
            }else{
                invalidateAvatarFrame();
            }
        }
    }
    private void invalidateAvatarFrame() {
        mUser = null;
        RelativeLayout leftDrawer = (RelativeLayout)mDrawer.findViewById(R.id.left_drawer);
        leftDrawer.removeViewAt(0);
        mAvatarFrame = getLayoutInflater().inflate(R.layout.avatar, leftDrawer,false);
        leftDrawer.addView(mAvatarFrame, 0);
        mAvatar = (ImageView) mAvatarFrame.findViewById(android.R.id.icon);
        mNameText = (TextView) mAvatarFrame.findViewById(android.R.id.text1);
    }

    private void setUserInfo() {
        if(mUser != null){
            AcApp.getGloableLoader().get(mUser.avatar, ImageLoader.getImageListener(mAvatar, R.drawable.acgirl, R.drawable.acgirl));
            mNameText.setText(mUser.name);
            if(mUser.isExpired()){
                new AlertDialog.Builder(this)
                .setTitle("您的账户已过期！")
                .setMessage("过期后会出现无法评论、签到等问题，请注销后重新登录！")
                .setPositiveButton("注销", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mUser = null;
                        AcApp.logout();
                        dialog.dismiss();
                        invalidateAvatarFrame();
                    }
                }).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
    private long mLastBackPressedMs;
    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().popBackStackImmediate()){
            setTitle(sTitles[0]);
            mMenuList.setItemChecked(0, true);
            return ;
        }
        long currentTimeMillis = System.currentTimeMillis();
        if(currentTimeMillis - mLastBackPressedMs < 1500){
            super.onBackPressed();
        }else{
            mLastBackPressedMs =currentTimeMillis;
            Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
            
        }
    }
}
