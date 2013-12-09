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

import java.util.List;

import tv.acfun.video.api.API;
import tv.acfun.video.entity.Category;
import tv.acfun.video.fragment.CategoriesFragment;
import tv.acfun.video.fragment.SubCategoryFragment;
import tv.acfun.video.util.net.CategoriesRequest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

/**
 * @author Yrom
 * 
 */
public class ChannelActivity extends ActionBarActivity implements TabListener {
    private static final String TAG = "ChannelActivity";
    private int mChannelId;
    private String mChannelName;
    private ViewPager mPager;
    private List<Category> mSubCategories;
    Listener<List<Category>> catsListener = new Listener<List<Category>>() {
        @Override
        public void onResponse(List<Category> response) {
            AcApp.setCategories(response);
            initViews();
        }
    };
    ErrorListener errorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.e(TAG, "error response! ", error);
        }
    };
    private PagerAdapter mAdapter;

    public static void start(Context context, int channelId, String channelName) {
        Intent intent = new Intent(context, ChannelActivity.class);
        intent.putExtra(API.EXRAS_CHANNEL_ID, channelId);
        intent.putExtra(API.EXRAS_CHANNEL_NAME, channelName);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mChannelId = getIntent().getIntExtra(API.EXRAS_CHANNEL_ID, 0);
        mChannelName = getIntent().getStringExtra(API.EXRAS_CHANNEL_NAME);
        List<Category> cats = AcApp.getCategories();
        setContentView(R.layout.activity_channel);
        if (cats == null) {
            /*
             * 以防万一
             */
            requestCategories();
        } else {
            initViews();
        }
    }

    private void requestCategories() {
        Request<?> request = new CategoriesRequest(catsListener, errorListener);
        AcApp.addRequest(request);
    }

    private void initViews() {
        mSubCategories = AcApp.getSubCats(mChannelId);
        final ActionBar actionBar = getSupportActionBar();
        if (mSubCategories == null) {
            /*
             * Single fragment.
             */
            setContentView(R.layout.frame_content);
            Fragment fragment = newFragment(mChannelId);
            getSupportFragmentManager().beginTransaction().replace(R.id.content, fragment).commit();
        } else {
            ViewStub stub = (ViewStub) findViewById(R.id.view_stub);
            mPager = (ViewPager) stub.inflate();
            findViewById(R.id.loading).setVisibility(View.GONE);
            mAdapter = new SubCategoryFragmentAdapter(getSupportFragmentManager(), mSubCategories);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    actionBar.setSelectedNavigationItem(position);
                }
            });
            mPager.setAdapter(mAdapter);
            for (int i = 0; i < mAdapter.getCount(); i++) {
                actionBar.addTab(actionBar.newTab().setText(mAdapter.getPageTitle(i)).setTabListener(this));
            }
        }
        actionBar.setTitle(mChannelName);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private Fragment newFragment(int channelId) {
        Fragment item = new SubCategoryFragment();
        Bundle args = new Bundle();
        args.putInt(API.EXRAS_CATEGORY_ID, channelId);
        item.setArguments(args);
        return item;
    }

    class SubCategoryFragmentAdapter extends FragmentPagerAdapter {
        List<Category> mSubCats;

        public SubCategoryFragmentAdapter(FragmentManager fm, List<Category> subCats) {
            super(fm);
            this.mSubCats = subCats;
        }

        @Override
        public Fragment getItem(int position) {
            return newFragment(mSubCats.get(position).id);
        }

        @Override
        public int getCount() {
            return mSubCats.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mSubCats.get(position).name;
        }
    }

    @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {}

    @Override
    public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
        mPager.setCurrentItem(arg0.getPosition());
    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {}
}
