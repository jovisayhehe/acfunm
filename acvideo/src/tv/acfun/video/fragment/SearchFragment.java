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

import java.util.List;

import tv.ac.fun.BuildConfig;
import tv.ac.fun.R;
import tv.acfun.video.AcApp;
import tv.acfun.video.DetailsActivity;
import tv.acfun.video.HomeActivity;
import tv.acfun.video.adapter.BaseArrayAdapter;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Content;
import tv.acfun.video.entity.Contents;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.net.FastJsonRequest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

/**
 * @author Yrom
 * 
 */
public class SearchFragment extends Fragment implements OnClickListener, OnEditorActionListener, OnScrollListener, OnItemClickListener {
    private static final String TAG = SearchFragment.class.getSimpleName();
    private View mBtnClear;
    private EditText mSearchText;
    private View mProgress;
    private ListView mResultList;
    private int mPage;
    private int mTotalCount;
    private SearchResultAdapter mAdapter;
    private Activity mActivity;
    private boolean mLastItemVisible;
    private View mEmptyView;
    private boolean mIsLoading;
    
    TextWatcher watcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                mBtnClear.setVisibility(View.VISIBLE);
            } else {
                mBtnClear.setVisibility(View.GONE);
                mProgress.setVisibility(View.GONE);
            }
            mResultList.setVisibility(View.GONE);
            AcApp.cancelAllRequest(TAG);
             if(mAdapter != null && !mAdapter.isEmpty())
                 mAdapter.clear();
        }
    };
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_search, container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mBtnClear = view.findViewById(R.id.btn_search_clear);
        mBtnClear.setOnClickListener(this);
        mSearchText = (EditText) view.findViewById(R.id.search_text);
        mSearchText.addTextChangedListener(watcher);
        mSearchText.setOnEditorActionListener(this);
        mProgress = view.findViewById(R.id.search_plate_progress);
        mResultList = (ListView) view.findViewById(android.R.id.list);
        mResultList.setOnScrollListener(this);
        mResultList.setOnItemClickListener(this);
        view.findViewById(R.id.search_logo).setOnClickListener(this);
        mEmptyView = view.findViewById(R.id.time_out_text);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btn_search_clear:
            mSearchText.setText("");
            break;
        case R.id.search_logo:
            startSearch(mSearchText.getText().toString(), 1);
            break;
        default:
            break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            startSearch(v.getText().toString(), 1);
        }
        return false;
    }

    private void startSearch(String query, int page) {
        mIsLoading = true;
        mInputMethod.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
        mProgress.setVisibility(View.VISIBLE);
        mPage = page;
        String url = API.getSearchUrl(query, 2, 1, mPage, 20);
        if (BuildConfig.DEBUG) Log.d(TAG, "query url=" + url);
        Request<?> request = new FastJsonRequest<Contents>(url, Contents.class, listener, errorListner);
        request.setTag(TAG);
        AcApp.addRequest(request);
    }

    Listener<Contents> listener = new Listener<Contents>() {
        @Override
        public void onResponse(Contents response) {
            Log.i(TAG, "onResponse::"+response.totalcount);
            mIsLoading =false;
            mTotalCount = response.totalcount;
            mResultList.setVisibility(View.VISIBLE);
            mProgress.setVisibility(View.GONE);
            if (mPage == 1) Toast.makeText(mActivity, getString(R.string.search_result, mTotalCount), 0).show();
            if (mAdapter == null) {
                mAdapter = new SearchResultAdapter(mActivity, response.contents);
                mResultList.setAdapter(mAdapter);
            }else if (mPage > 1) {
                mAdapter.addData(response.contents);
            } else {
                mAdapter.setData(response.contents);
                mResultList.smoothScrollToPosition(0);
            }

        }
    };
    
    ErrorListener errorListner = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.i(TAG, "onErrorResponse");
            mIsLoading =false;
            if(mAdapter == null){
                mAdapter = new SearchResultAdapter(mActivity, null);
                
            }
            mResultList.setEmptyView(mEmptyView);
        }
    };
    private InputMethodManager mInputMethod;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && mLastItemVisible && !mIsLoading) {
            startSearch(mSearchText.getText().toString(),mPage+1);
        }
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mLastItemVisible = (totalItemCount > 0)
                && (firstVisibleItem + visibleItemCount >= totalItemCount - 1)
                && totalItemCount < mTotalCount;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object obj = parent.getItemAtPosition(position);
        if(obj != null && obj instanceof Content){
            Content c = (Content)obj;
            DetailsActivity.start(mActivity, c.toVideo());
        }
        
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAdapter != null){
            mAdapter.destory();
        }
    }
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mInputMethod = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    private class SearchResultAdapter extends BaseArrayAdapter<Content> {
        public SearchResultAdapter(Context context, List<Content> items) {
            super(context, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.item_contents, parent, false);
                holder = new ViewHolder();
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.desc = (TextView) convertView.findViewById(R.id.desc);
                holder.channel =  (TextView) convertView.findViewById(R.id.channel);
                holder.image =  (NetworkImageView) convertView.findViewById(R.id.image);
                holder.image.setDefaultImageResId(R.drawable.cover_night);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            Content item = getItem(position);
            if(mActivity instanceof HomeActivity){
                String channel = ((HomeActivity)mActivity).findChannelNameById(item.channelId);
                holder.channel.setText(channel == null? "未知频道" : channel);
            }
            holder.title.setText(item.title);
            holder.time.setText(item.username +" / 发布于 "+AcApp.getPubDate(item.releaseDate));
            holder.desc.setText(Html.fromHtml(TextViewUtils.getSource(item.description)));
            holder.image.setImageUrl(item.titleImg, AcApp.getGloableLoader());
            return convertView;
        }
    }

    private static class ViewHolder{
        TextView title, time, desc,channel;
        NetworkImageView image;
    }
}
