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

import java.io.UnsupportedEncodingException;
import java.util.List;

import tv.acfun.video.R;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Category;
import tv.acfun.video.util.CommonUtil;
import tv.acfun.video.util.net.Connectivity;
import tv.acfun.video.util.net.CustomUARequest;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapter;


/**
 * 频道分类
 * @author Yrom
 *
 */
public class CategoriesFragment extends GridFragment{

    public CategoriesFragment(){}
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mGridView.setColumnWidth(getResources().getDimensionPixelSize(R.dimen.item_cat_width));
    }
    @Override
    public void onHeaderClick(AdapterView<?> parent, View view, long id) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub
        
    }
    
    private static class CategoriesRequest extends CustomUARequest<List<Category>> {

        public CategoriesRequest(Listener<List<Category>> listener, ErrorListener errorListner) {
            super(API.CHANNEL_CATS, null, listener, errorListner);
        }

        @Override
        protected Response<List<Category>> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(JSON.parseArray(json, Category.class),
                        Connectivity.newCache(response, 3000));
            } catch (UnsupportedEncodingException e) {
                String json = new String(response.data);
                return Response.success(JSON.parseArray(json, Category.class),
                        Connectivity.newCache(response, 3000));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }

    }

    @Override
    protected Request<?> newRequest() {
        return new CategoriesRequest(listener, errorListner);
    }
    
    
    Listener<List<Category>> listener = new Listener<List<Category>>() {

        @Override
        public void onResponse(List<Category> response) {
            mLoadingView.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
            if(mAdapter == null){
                mAdapter = new CategoriesAdapter(getActivity().getApplicationContext(),response);
            }else{
                ((CategoriesAdapter)mAdapter).setData(response);
            }
            setAdapter(mAdapter);
        }};
    ErrorListener errorListner = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
            
        }};
        
    private class CategoriesAdapter extends BaseAdapter implements StickyGridHeadersBaseAdapter {
        private LayoutInflater mInflater;
        private List<Category> mItems;

        public CategoriesAdapter(Context context, List<Category> items) {
            mItems = items;
            mInflater = LayoutInflater.from(context);
        }
        public void setData(List<Category> data) {
            if(mItems != null){
                mItems.clear();
            }
            mItems.addAll(data);
        }
        @Override
        public int getCount() {
            int headers = getNumHeaders();
            int size = 0;
            for (int i = 0; i < headers; i++) {
                size += getCountForHeader(i);
            }
            return size;
        }

        @Override
        public Category getItem(int position) {
            int headers = getNumHeaders();
            int size = 0;
            for (int i = 0; i < headers; i++) {
                size += getCountForHeader(i);
                if (position < size) {
                    Category item = mItems.get(i);
                    position = position - (size - getCountForHeader(i));
                    return item.subclasse.get(position);
                }
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.item_categories, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Category item = getItem(position);
            holder.textView.setText(item.name);
            return convertView;
        }

        @Override
        public int getCountForHeader(int header) {
            return mItems.get(header).subclasse.size();
        }

        @Override
        public int getNumHeaders() {
            return mItems.size();
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            Category cat = mItems.get(position);
            HeaderViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(tv.acfun.video.R.layout.header, parent, false);
                holder = new HeaderViewHolder();
                holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            holder.textView.setText(cat.name);
            return convertView;
        }
        
    }
    private static class HeaderViewHolder {
        TextView textView;
    }

    private static class ViewHolder {
        TextView textView;
    }
}
