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

import java.util.ArrayList;

import tv.acfun.video.adapter.BaseArrayAdapter;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Video;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.net.FastJsonRequest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.astuetz.PagerSlidingTabStrip;

/**
 * @author Yrom TODO :
 */
public class PlayerActivity extends ActionBarActivity {
    public static void start(Context context, Video video) {
        Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
        intent.putExtra("acid", video.acId);
        context.startActivity(intent);
    }

    private ViewPager mPager;
    private Video mVideo;
    private View mProgress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        mProgress = findViewById(R.id.progressBar);
        int acId = getIntent().getIntExtra("acid", 0);
        AcApp.addRequest(new VideoDetailsRequest(acId, listener, errorListner));
    }

    private void initTabs() {
        ViewStub stub = (ViewStub) findViewById(R.id.view_stub);
        View view = stub.inflate();
        String[] titles = getResources().getStringArray(R.array.details_titles);
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(new DetailsPagerAdapter(getSupportFragmentManager(), titles));
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mPager);
    }

    private class DetailsPagerAdapter extends FragmentPagerAdapter {
        private String[] mTitles;

        public DetailsPagerAdapter(FragmentManager fm, String[] titles) {
            super(fm);
            mTitles = titles;
        }

        @Override
        public Fragment getItem(int arg0) {
            // TODO Auto-generated method stub
            switch (arg0) {
            case 0:
                return DetailsFragment.newInstance(mVideo);
            case 1:
                return PartsFragment.newInstance(mVideo.episodes);
            default:
                break;
            }
            return new Fragment();
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitles[position];
        }
    }

    Listener<Video> listener = new Listener<Video>() {
        @Override
        public void onResponse(Video response) {
            mVideo = response;
            mProgress.setVisibility(View.GONE);
            initTabs();
        }
    };
    ErrorListener errorListner = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
        }
    };
    

    public static class VideoDetailsRequest extends FastJsonRequest<Video> {
        public VideoDetailsRequest(int acId, Listener<Video> listener, ErrorListener errorListner) {
            super(API.getVideoDetailsUrl(acId), Video.class, listener, errorListner);
        }
    }

    public static class DetailsFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_details, container, false);
        }
    
        public static Fragment newInstance(Video video) {
            DetailsFragment f = new DetailsFragment();
            Bundle args = video.store();
            f.setArguments(args);
            return f;
        }
    
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            TextView title = (TextView) view.findViewById(R.id.title);
            TextView upInfo = (TextView) view.findViewById(R.id.up_info);
            TextView detail = (TextView) view.findViewById(R.id.details);
            
            Bundle args = getArguments();
            title.setText(args.getString("name"));
            
            String info = String.format("%s / 发布于 %s / %d次播放，%d条评论，%d人收藏",
                    args.getString("up"),
                    AcApp.getPubDate(args.getLong("createtime")),
                    args.getInt("views"),
                    args.getInt("comments"),
                    args.getInt("favs"));
            upInfo.setText(info);
            detail.setText(Html.fromHtml(TextViewUtils.getSource(args.getString("desc"))));
        }
    }
    /**
     * call from PartsFragment's list onItemClick
     * @param part
     */
    public void onPlay(VideoPart part){
        try {
            /*
             * 调用测试播放器
             */
            Intent intent = new Intent("tv.danmaku.dmplayer.action.PLAY", Uri.parse("dm://"+part.type+"/"+part.sourceId+"/"+part.commentId));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "请先安装弹幕播放器！", 0).show();
        }
    }
    
    
    public static class PartsFragment extends ListFragment implements OnItemClickListener{

        private ArrayList<VideoPart> list;
        public static Fragment newInstance(ArrayList<VideoPart> episodes) {
            Bundle args = new Bundle();
            args.putParcelableArrayList("parts", episodes);
            
            Fragment f = new PartsFragment();
            f.setArguments(args);
            return f;
        }
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            list = getArguments().getParcelableArrayList("parts");
        }
        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            BaseArrayAdapter<VideoPart> adapter = new BaseArrayAdapter<VideoPart>(getActivity(), list) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    ViewHolder holder = null;
                    if (convertView == null) {
                        convertView = mInflater.inflate(R.layout.item_videoparts, parent, false);
                        holder = new ViewHolder();
                        holder.name = (TextView) convertView.findViewById(R.id.part_name);
                        holder.desc = (TextView) convertView.findViewById(R.id.part_desc);
                        convertView.setTag(holder);
                    } else {
                        holder = (ViewHolder) convertView.getTag();
                    }
                    VideoPart item = getItem(position);
                    String text = TextUtils.isEmpty(item.name) ? "点击查看视频" : (position + 1) + ". " + item.name;
                    holder.name.setText(text);
                    holder.desc.setText("视频源: " + item.type);
                    return convertView;
                }
            };
            setListAdapter(adapter);
            getListView().setOnItemClickListener(this);
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            // TODO Auto-generated method stub
            VideoPart item = (VideoPart) parent.getItemAtPosition(position);
            PlayerActivity activity = (PlayerActivity) getActivity();
            activity.onPlay(item);
        }
        
    }
    private static class ViewHolder{
        TextView name, desc;
    }
}
