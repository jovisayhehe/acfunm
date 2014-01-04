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

import io.vov.vitamio.LibsChecker;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import tv.acfun.video.adapter.BaseArrayAdapter;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Video;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.player.MediaList.OnResolvedListener;
import tv.acfun.video.player.MediaList.Resolver;
import tv.acfun.video.player.VideoView;
import tv.acfun.video.player.resolver.BaseResolver;
import tv.acfun.video.player.resolver.ResolverType;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.net.Connectivity;
import tv.acfun.video.util.net.FastJsonRequest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.astuetz.PagerSlidingTabStrip;

/**
 * @author Yrom TODO :
 */
public class PlayerActivity extends ActionBarActivity implements OnClickListener {
    private static final String TAG = "PlayerActivity";
    
    public static void start(Context context, Video video) {
        Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
        intent.putExtra("acid", video.acId);
        intent.putExtra("preview", video.previewurl);
        context.startActivity(intent);
    }

    private ViewPager mPager;
    private Video mVideo;
    private View mProgress;
    private ImageView previewImage;
    private int w,h;
    private boolean isPlaying;
    private TextView mProgressText;
    private Resolver mResolver;
    private VideoPart mCurrentPart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.activity_player);
        initDisplayer();
        initViews();
        setVideoSize();
        initVideo();
    }
    private void initViews() {
        mProgress = findViewById(R.id.progressBar);
        mVideoFrame = findViewById(R.id.frame_video);
        findViewById(R.id.play_btn).setOnClickListener(this);
        mVideoView = (VideoView) findViewById(R.id.video);
        mVideoView.setMediaBufferingIndicator(findViewById(R.id.buffering_indicator));
        mProgressText = (TextView)findViewById(R.id.progress_text);
        
    }
    private void setVideoSize() {
        int height = w / 16 * 9;
        LayoutParams params = mVideoFrame.getLayoutParams();
        params.height = height;
        mVideoFrame.setLayoutParams(params);
        Log.i(TAG, "height = "+height);
    }
    private void initVideo() {
        int acId = getIntent().getIntExtra("acid", 0);
        getSupportActionBar().setTitle("ac"+acId);
        String preview = getIntent().getStringExtra("preview");
        previewImage = (ImageView) findViewById(R.id.preview);
        AcApp.getGloableLoader().get(preview, ImageLoader.getImageListener(previewImage,0,0));
        AcApp.addRequest(new VideoDetailsRequest(acId, listener, errorListner));
    }
    
    private void initDisplayer() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        w = Math.min(displayMetrics.widthPixels,displayMetrics.heightPixels);
        h = Math.max(displayMetrics.widthPixels,displayMetrics.heightPixels);
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
    
    OnResolvedListener OnResolved = new OnResolvedListener() {
        @Override
        public void onResolved(Resolver resolver) {
                if(resolver.getMediaList() != null){
                    mProgressText.setText(mProgressText.getText()
                            +"完毕.\n"
                            +"开始加载弹幕...");
                    addDanmakusRequest();
                    
                }else{
                    mProgressText.setText(mProgressText.getText()+"失败!");
                    // TODO: show retry
                }
        }

    };
    private Animation animation;
    private void initAnimation() {
        animation = AnimationUtils.loadAnimation(mProgressText.getContext(), R.anim.fade_out);
        animation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mProgressText.setVisibility(View.GONE);
            }
            
        });
    }

    private void hideTextDelayed() {
        mProgressText.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressText.startAnimation(animation);
            }
        }, 1500);
    }

    private void resolveVideos(VideoPart part) {
        ResolverType type = null;
        String sourceType = part.type;
        try {
            type = ResolverType.valueOf(sourceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if(type == null){
            mProgressText.setText(mProgressText.getText()+"\n尚不支持该视频源...");
            return;
        }
        mResolver = type.getResolver(part.sourceId);
        mResolver.setOnResolvedListener(OnResolved);
        mResolver.resolveAsync(this.getApplicationContext());
        mProgressText.setText(mProgressText.getText()+"\n视频分段解析中...");
    }
    private void addDanmakusRequest() {
        String url = "http://comment.acfun.tv/" + mCurrentPart.commentId + ".json";
        // TODO: acfun lock danmakus
        StringRequest request = new StringRequest(url, new Listener<String>() {
            @Override
            public void onResponse(String response) {
                mProgressText.setText(mProgressText.getText()
                        +"\n弹幕文件下载完毕."
                        +"\n开始解析...");
                try {
//                    DMSiteType type = DMSiteType.valueOf(mSite.toUpperCase());
//                    ILoader loader = type.getLoader();
//                    loader.loadData(response);
//                    BaseDanmakuParser parser = type.getParser().load(loader.getDataSource());
//                    mDMView.prepare(parser);
                    throw new RuntimeException();
                } catch (Exception e) {
                    Log.e("Play", "解析失败",e);
                    mProgressText.setText(mProgressText.getText()
                            +"\n解析失败...");
                        
                }
                startPlay();
            }
        }, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mProgressText.setText(mProgressText.getText() + "\n弹幕文件下载失败！");
                startPlay();
                hideTextDelayed();
            }
        }) {
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parsed;
                parsed = new String(response.data, Charset.defaultCharset());
                return Response.success(parsed, Connectivity.newCache(response, 60));
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                if (headers == null || headers.equals(Collections.EMPTY_MAP)) {
                    headers = new HashMap<String, String>();
                }
                headers.put("User-Agent", BaseResolver.UA_DEFAULT);
                return headers;
            }
        };
        Connectivity.addRequest(request);
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
    private View mVideoFrame;
    private VideoView mVideoView;
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
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
        mCurrentPart = part;
        resolveVideos(part);
//        try {
//            /*
//             * 调用测试播放器
//             */
//            Intent intent = new Intent("tv.danmaku.dmplayer.action.PLAY", Uri.parse("dm://"+part.type+"/"+part.sourceId+"/"+part.commentId));
//            intent.addCategory(Intent.CATEGORY_DEFAULT);
//            startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(getApplicationContext(), "请先安装弹幕播放器！", 0).show();
//        }
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
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.play_btn:
            if(!isPlaying){
                v.setBackgroundResource(R.drawable.btn_pause_selector);
                play();
            }else{
                v.setBackgroundResource(R.drawable.btn_play_selector);
                pausePlay();
            }
            break;
        default:
            break;
        }
    }
    private void play() {
        if (previewImage.getVisibility() == View.VISIBLE) {
            previewImage.setVisibility(View.GONE);
        }
        if(mCurrentPart == null || mResolver.getMediaList() == null){
            onPlay(mVideo.episodes.get(0));
        }else{
            mVideoView.start();
            isPlaying = true;
        }
    }
    private void pausePlay() {
        isPlaying = false;
        mVideoView.pause();
    }
    private void startPlay() {
        
        mProgressText.setText(mProgressText.getText()
                +"\n开始缓冲视频..请稍候...");
        if (!LibsChecker.checkVitamioLibs(this)){
            mProgressText.setText(mProgressText.getText()
                    +"\n播放器初始化失败！");
            return;
        }
        mVideoView.setMediaList(mResolver.getMediaList());
        play();
    }
}
