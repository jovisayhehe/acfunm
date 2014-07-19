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

import org.apache.commons.httpclient.Cookie;

import tv.ac.fun.R;
import tv.acfun.video.AcApp;
import tv.acfun.video.DetailsActivity;
import tv.acfun.video.HomeActivity;
import tv.acfun.video.adapter.BaseArrayAdapter;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Content;
import tv.acfun.video.entity.Contents;
import tv.acfun.video.entity.User;
import tv.acfun.video.util.MemberUtils;
import tv.acfun.video.util.TextViewUtils;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.android.volley.toolbox.NetworkImageView;

/**
 * @author Yrom
 *
 */
public class PushContentFragment extends ListFragment {
    protected Cookie[] mCookies;
    public PushContentAdapter mAdapter;
    private int mCurrentPage = 1;
    private boolean hasNextPage;
    private View mFooterView;
    private boolean isLoading;
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = AcApp.getUser();
        if(user == null){
            setListAdapter(new PushContentAdapter(mActivity, null));
            setEmptyText("您还没有登录");
        }else{
            mCookies = JSON.parseObject(user.cookies, Cookie[].class);
            mFooterView = getLayoutInflater(savedInstanceState).inflate(R.layout.list_footerview, getListView(),false);
            getListView().addFooterView(mFooterView);
        }
    }
    private class PushContentAdapter extends BaseArrayAdapter<Content>{

        public PushContentAdapter(Context context, List<Content> items) {
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
    protected void init() {
        if(mCookies == null) return;
        load(mCurrentPage);
    }
    private void load(int page){
        new PushContentTask().execute(page);
    }
    private class PushContentTask extends AsyncTask<Integer, Void, Contents>{
        @Override
        protected void onPreExecute() {
            isLoading = true;
        }
        @Override
        protected Contents doInBackground(Integer... params) {
            return MemberUtils.getPushContents(API.getDomainRoot(mActivity), mCookies, params[0]);
        }

        @Override
        protected void onPostExecute(Contents result) {
            if(mCurrentPage == 1 || mAdapter== null){
                mAdapter = new PushContentAdapter(mActivity, result.contents);
                setListAdapter(mAdapter);
                if(result == null || result.contents.isEmpty()){
                    setEmptyText("你还没有关注过Up");
                }
            }else{
                mAdapter.addData(result.contents);
            }
            isLoading = false;
            hasNextPage = result.totalpage > mCurrentPage;
        }
    }
    @Override
    protected void onLastItemVisible() {
        if(hasNextPage){
            if(!isLoading)
            load(++mCurrentPage);
        }else{
            mFooterView.findViewById(R.id.list_footview_progress).setVisibility(View.GONE);
            ((TextView)mFooterView.findViewById(R.id.list_footview_text)).setText("没有了");
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object o = parent.getItemAtPosition(position);
        if(o != null && o instanceof Content){
            
            Content c = (Content)o;
            if(mActivity instanceof HomeActivity){
                boolean isArticleChannel = ((HomeActivity)mActivity).isArticleChannel(c.channelId);
                if(isArticleChannel){
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("ac://ac"+c.aid));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        AcApp.area63Alert(mActivity);
                    }
                    return;
                }
            }
            DetailsActivity.start(mActivity, c.toVideo());
        }
    }
}
