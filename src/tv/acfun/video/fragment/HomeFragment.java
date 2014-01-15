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

import tv.ac.fun.R;
import tv.acfun.video.AcApp;
import tv.acfun.video.DetailsActivity;
import tv.acfun.video.HomeActivity;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.HomeCat;
import tv.acfun.video.entity.Video;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.net.Connectivity;
import tv.acfun.video.util.net.CustomUARequest;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersBaseAdapter;

/**
 * @author Yrom
 * 
 */
public class HomeFragment extends RefreshActionGridFragment {

    public HomeFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int w = getResources().getDimensionPixelSize(R.dimen.item_cat_width);
        mGridView.setColumnWidth(w);
    }
    private Listener<List<HomeCat>> listener = new Listener<List<HomeCat>>() {

        @Override
        public void onResponse(List<HomeCat> response) {
            hideRefreshAnimation();
            if (mAdapter == null) {
                mAdapter = new HomeAdapter(mActivity.getApplicationContext(), response);
            } else {
                ((HomeAdapter) mAdapter).setData(response);
            }
            setAdapter(mAdapter);
        }
    };

    private ErrorListener errorListner = new ErrorListener() {

        @Override
        public void onErrorResponse(VolleyError error) {
            hideRefreshAnimation();
            if (mAdapter == null || mAdapter.isEmpty()) {
                byte[] data = AcApp.getDataInDiskCache(API.HOME_CATS);
                if (data != null && data.length > 0) {
                    String json = new String(data);
                    List<HomeCat> items = JSON.parseArray(json, HomeCat.class);
                    listener.onResponse(items);
                }
            }
            Toast.makeText(getActivity(), "读取数据失败，请重试！", 0).show();
        }
    };

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Video o = ((HomeAdapter) mAdapter).getVideoItem(arg2);
        DetailsActivity.start(getActivity(), o);
    }

    @Override
    public void onHeaderClick(AdapterView<?> parent, View view, long id) {
        HomeCat c = (HomeCat) mAdapter.getItem((int) id);
        int channelId = c.id;
        
        ((HomeActivity)mActivity).selectFragmentByChannelId(channelId);
    }


    private static class HomeCatsRequest extends CustomUARequest<List<HomeCat>> {

        public HomeCatsRequest(Listener<List<HomeCat>> listener, ErrorListener errorListner) {
            super(API.HOME_CATS, null, listener, errorListner);
        }

        @Override
        protected Response<List<HomeCat>> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(response.data,
                        HttpHeaderParser.parseCharset(response.headers));
                return Response.success(JSON.parseArray(json, HomeCat.class),
                        Connectivity.newCache(response, 120));
            } catch (UnsupportedEncodingException e) {
                String json = new String(response.data);
                return Response.success(JSON.parseArray(json, HomeCat.class),
                        Connectivity.newCache(response, 120));
            } catch (Exception e) {
                return Response.error(new ParseError(e));
            }
        }

    }

    private class HomeAdapter extends BaseAdapter implements StickyGridHeadersBaseAdapter {

        private int mHeaderResId = R.layout.item_home_header;
        private int mItemResId = R.layout.item_home;
        private LayoutInflater mInflater;
        private List<HomeCat> mItems;

        public HomeAdapter(Context context, List<HomeCat> items) {
            mItems = items;
            mInflater = LayoutInflater.from(context);
        }

        public void setData(List<HomeCat> data) {
            if (mItems != null) {
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
        public HomeCat getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(mItemResId, parent, false);
                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
                holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Video item = getVideoItem(position);
            String name = "无题";
            if (item.name != null) {
                name = TextViewUtils.getSource(item.name);
            }
            holder.textView.setText(name);
            /*
             * 滚动时取消上次指定的请求，可能会有流量的浪费...
             */
            if (holder.imageContainer != null) {
                holder.imageContainer.cancelRequest();
            }
            holder.imageContainer = AcApp.getGloableLoader().get(
                    item.previewurl,
                    ImageLoader.getImageListener(holder.imageView, R.drawable.cover_night,
                            R.drawable.cover_night));
            return convertView;
        }

        public Video getVideoItem(int position) {
            int headers = getNumHeaders();
            int size = 0;
            for (int i = 0; i < headers; i++) {
                size += getCountForHeader(i);
                if (position < size) {
                    HomeCat item = getItem(i);
                    position = position - (size - getCountForHeader(i));
                    return item.videos.get(position);
                }
            }
            return null;
        }

        @Override
        public View getHeaderView(int position, View convertView, ViewGroup parent) {
            HeaderViewHolder holder;
            if (convertView == null) {
                convertView = mInflater.inflate(mHeaderResId, parent, false);
                holder = new HeaderViewHolder();
                holder.textView = (TextView) convertView.findViewById(android.R.id.text1);
                convertView.setTag(holder);
            } else {
                holder = (HeaderViewHolder) convertView.getTag();
            }

            HomeCat item = getItem(position);

            holder.textView.setText(item.name);
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return getVideoItem(position).acId;
        }

        @Override
        public int getCountForHeader(int header) {
            return getItem(header).videos.size();
        }

        @Override
        public int getNumHeaders() {
            return mItems.size();
        }

    }

    private static class HeaderViewHolder {
        TextView textView;
    }

    private static class ViewHolder {
        TextView textView;
        ImageView imageView;
        ImageLoader.ImageContainer imageContainer;
    }

    @Override
    protected Request<?> newRequest() {
        return new HomeCatsRequest(listener, errorListner);
    }

    @Override
    protected boolean shouldRefreshCache() {
        return false;
    }
}
