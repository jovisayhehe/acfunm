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
import java.util.List;

import tv.acfun.video.adapter.MenuAdapter;
import tv.acfun.video.entity.Category;
import tv.acfun.video.fragment.VideosFragment;
import tv.acfun.video.util.CommonUtil;
import tv.acfun.video.util.net.CategoriesRequest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;


/**
 * @author Yrom
 *
 */
public class HomeActivity extends ActionBarActivity implements OnItemClickListener {
    
    private DrawerLayout mDrawer;
    private ListView mMenuList;
    private ActionBarDrawerToggle mDrawerToggle;
    private static String KEY_STATE_POSITION = "key_state_position";
    public static String[] sTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMenuList = (ListView) findViewById(android.R.id.list);
        mProgress = findViewById(android.R.id.progress);
        mMenuList.setOnItemClickListener(this);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  
                mDrawer,         
                R.drawable.ic_navigation_drawer, 
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
            int position = savedInstanceState==null? 0 : savedInstanceState.getInt(KEY_STATE_POSITION, 0);
            select(position);
        }
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
                Category cat = new Category(1024+i, title);
                response.add(0, cat);
            }
            sCategories = response;
            ListAdapter adapter = new MenuAdapter(HomeActivity.this, sCategories);
            mMenuList.setAdapter(adapter);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
          return true;
        }
        // Handle your other action bar items...

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
        if(cat.id > 1024){
            // TODO
            Toast.makeText(this, "正在开发中...", 0).show();
        }else{
            if (cat.id != 63 || !handleArea63Click()) {
                Fragment f = VideosFragment.newInstance(cat);
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, f).commit();
                mDrawer.closeDrawer(GravityCompat.START);
                setTitle(cat.name);
                mMenuList.setItemChecked(position, true);
            }
            
        }
    }
    
    private boolean handleArea63Click() {
        try{
            ComponentName cmp = new ComponentName("tv.acfun.a63", "tv.acfun.a63.MainActivity");
            if (getPackageManager().getActivityInfo(cmp, 0) != null) {
                Intent intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.LAUNCHER");
                intent.setComponent(cmp);
                startActivity(intent);
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
            OnClickListener onClick = new OnClickListener() {
                
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == DialogInterface.BUTTON_POSITIVE){
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://pan.baidu.com/s/1pFLDT")));
                    }
                    dialog.dismiss();
                }
            };
            new AlertDialog.Builder(this)
                .setTitle("没有找到文章区客户端")
                .setMessage("是否前往下载安装？")
                .setPositiveButton("好", onClick)
                .setNegativeButton("取消", onClick)
                .show();
            
        }
        return false;
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
    public void onAvatarClick(View v){
        Toast.makeText(this, "正在开发中...", 0).show();
    }
}
