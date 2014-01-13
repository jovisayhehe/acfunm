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

import com.android.volley.toolbox.NetworkImageView;

import tv.ac.fun.R;
import tv.acfun.video.AcApp;
import tv.acfun.video.DetailsActivity;
import tv.acfun.video.HomeActivity;
import tv.acfun.video.adapter.BaseArrayAdapter;
import tv.acfun.video.db.DB;
import tv.acfun.video.entity.Video;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * @author Yrom
 * 
 */
public class HistoryFragment extends ListFragment implements OnItemClickListener {
    private DB mDb;
    private ListAdapter mAdapter;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mDb = new DB(mActivity);
    }
    
    protected void init() {
        new AsyncTask<Void, Void, List<Video>>() {
            @Override
            protected List<Video> doInBackground(Void... params) {
                List<Video> history = mDb.getHistory();
                return history;
            }

            @Override
            protected void onPostExecute(List<Video> result) {
                mAdapter = new HistoryAdapter(mActivity, result);
                setListAdapter(mAdapter);
                if(result == null || result.isEmpty()){
                    setEmptyText("你还没有浏览过视频");
                }
                

            }
        }.execute();
    }
    private class HistoryAdapter extends BaseArrayAdapter<Video>{

        public HistoryAdapter(Context context, List<Video> items) {
            super(context, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Video item = getItem(position);
            ViewHolder holder = null;
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.item_history, parent,false);
                holder = new ViewHolder();
                holder.channel = (TextView) convertView.findViewById(R.id.channel);
                holder.title = (TextView) convertView.findViewById(R.id.title);
                holder.time = (TextView) convertView.findViewById(R.id.time);
                holder.image = (NetworkImageView) convertView.findViewById(R.id.image);
                holder.image.setDefaultImageResId(R.drawable.cover_night);
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }
            if(mActivity instanceof HomeActivity){
                String channel = ((HomeActivity)mActivity).findChannelNameById(item.channelId);
                holder.channel.setText(channel);
            }
            holder.title.setText(item.name);
            holder.time.setText("浏览于 "+AcApp.getPubDate(item.createtime));
            holder.image.setImageUrl(item.previewurl, AcApp.getGloableLoader());
            return convertView;
        }
        
    }
    
    private static class ViewHolder{
        TextView channel,title,time; 
        NetworkImageView image;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object o = parent.getItemAtPosition(position);
        if(o != null && o instanceof Video){
            DetailsActivity.start(mActivity, (Video)o);
        }
    }
}
