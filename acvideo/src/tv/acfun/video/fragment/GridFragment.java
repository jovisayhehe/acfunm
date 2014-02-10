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
package tv.acfun.video.fragment;

import tv.ac.fun.R;
import tv.acfun.video.AcApp;
import tv.acfun.video.util.net.Connectivity;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListAdapter;

import com.android.volley.Cache;
import com.android.volley.Request;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView.OnHeaderClickListener;


/**
 * @author Yrom
 *
 */
public abstract class GridFragment extends Fragment implements OnItemClickListener, OnScrollListener, OnHeaderClickListener {
    protected GridView mGridView;
    protected View mLoadingView;
    protected ListAdapter mAdapter;
    protected boolean mIsItemVisible;
    protected Activity mActivity;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_grid, container, false);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mGridView = (GridView) view.findViewById(R.id.asset_grid);
        mGridView.setOnItemClickListener(this);
        mGridView.setOnScrollListener(this);
        mLoadingView = view.findViewById(R.id.loading);
        if(mGridView instanceof StickyGridHeadersGridView)
            ((StickyGridHeadersGridView) mGridView).setOnHeaderClickListener(this);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }
    
    public void setAdapter(ListAdapter adapter){
        mLoadingView.setVisibility(View.GONE);
        mGridView.setVisibility(View.VISIBLE);
        if(mAdapter != adapter){
            mAdapter = adapter;
        }
        mGridView.setAdapter(mAdapter);
        mGridView.requestFocus();
    }
    protected void loadData(){
        mLoadingView.setVisibility(View.VISIBLE);
        mGridView.setVisibility(View.GONE);
        Request<?> request = newRequest();
        if(shouldRefreshCache()){
            String key = request.getCacheKey();
            Cache.Entry entry = Connectivity.getGloadbleCache(getActivity()).get(key);
            if(entry != null && !entry.isExpired()){
                Connectivity.getGloadbleCache(getActivity()).invalidate(key, true);
            }
        }
        AcApp.addRequest(request);
    }
    
    protected boolean shouldRefreshCache() {
        return false;
    }

    protected abstract Request<?> newRequest();

//    @Override
//    public abstract void onHeaderClick(AdapterView<?> parent, View view, long id);

    @Override
    public abstract void onItemClick(AdapterView<?> parent, View view, int position, long id);
    
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mIsItemVisible = totalItemCount > 0
                && (firstVisibleItem + visibleItemCount >= totalItemCount - 1);
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if(scrollState == SCROLL_STATE_IDLE && mIsItemVisible){
            onLastItemVisible();
        }
    }

    protected void onLastItemVisible(){
        // do nothing
    }
    @Override
    public void onHeaderClick(AdapterView<?> parent, View view, long id) {
        // do nothing
    }
}
