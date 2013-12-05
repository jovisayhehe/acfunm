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

import com.alibaba.fastjson.JSON;
import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import tv.acfun.video.adapter.MenuAdapter;
import tv.acfun.video.entity.Category;
import tv.acfun.video.fragment.CategoriesFragment.CategoriesRequest;
import tv.acfun.video.util.CommonUtil;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;


/**
 * @author Yrom
 *
 */
public class HomeActivity extends ActionBarActivity implements OnItemClickListener {
    
    private DrawerLayout mDrawer;
    private ListView mMenuList;
    private ActionBarDrawerToggle mDrawerToggle;
    public static String[] sTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMenuList = (ListView) findViewById(R.id.left_drawer);
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
        
        if (savedInstanceState == null && sCategories != null) {
            select(0);
        }
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
            for(int i=sTitles.length-1; i>=0 ;i--){
                String title = sTitles[i];
                Category cat = new Category(10086+i, title);
                response.add(0, cat);
            }
            sCategories = response;
            ListAdapter adapter = new MenuAdapter(HomeActivity.this, sCategories);
            mMenuList.setAdapter(adapter);
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
        select(position);
    }
    
    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        getSupportActionBar().setTitle(title);
    }
    
    private void select(int position){
        Category cat = sCategories.get(position);
        setTitle(cat.name);
        mMenuList.setItemChecked(position, true);
        Fragment f = new CategoryFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, f).commit();
        mDrawer.closeDrawer(mMenuList);
    }
    
    public static class CategoryFragment extends ListFragment{
        public CategoryFragment() {
        }
        
    }
    
}
