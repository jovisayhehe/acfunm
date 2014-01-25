/*
 * Copyright (C) 2014 YROM.NET
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

package tv.acfun.video.fragment;

import tv.ac.fun.R;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


/**
 * Grid fragment with a refresh option menu
 * @author Yrom
 *
 */
public abstract class RefreshActionGridFragment extends GridFragment {
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.refresh, menu);
        mRefreshItem = menu.findItem(R.id.action_refresh);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void loadData() {
        super.loadData();
        if(mRefreshItem != null)
            showRefreshAnimation(mRefreshItem);
        
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void showRefreshAnimation(MenuItem item) {
        if (mRefreshItem == null) mRefreshItem = item;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 
            mRefreshItem.setActionView(R.layout.progress);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    protected void hideRefreshAnimation() {
        mLoadingView.setVisibility(View.GONE);
        mGridView.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && mRefreshItem != null) {
            mRefreshItem.setActionView(null);
        }
    }
    private MenuItem mRefreshItem;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
