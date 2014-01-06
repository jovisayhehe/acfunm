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
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.player.MediaList.OnResolvedListener;
import tv.acfun.video.player.MediaList.Resolver;
import tv.acfun.video.player.VideoView;
import tv.acfun.video.player.resolver.BaseResolver;
import tv.acfun.video.player.resolver.ResolverType;
import tv.acfun.video.util.net.Connectivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * @author Yrom TODO :
 */
public class PlayerActivity extends ActionBarActivity implements OnClickListener {
    private static final String EXTRA_PREFER_HW = "prefer_hw";
    private static final String EXTRA_VIDEO = "video";
    private static final String TAG = "PlayerActivity";
    private VideoView mVideoView;
    private View mBufferingIndicator;
    private TextView mProgressText;

    public static void start(Context context, VideoPart video,boolean hw) {
        Intent intent = new Intent(context.getApplicationContext(), PlayerActivity.class);
        intent.putExtra(EXTRA_VIDEO, video);
        intent.putExtra(EXTRA_PREFER_HW, hw);
        context.startActivity(intent);
    }
    OnPreparedListener onPrepared = new OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
//            
//            if(mDMView.isPrepared()){
//                mDMView.start();
//            }
            mp.start();
            hideTextDelayed();
        }
    };
    private Animation mTextAnimation;
    private Resolver mResolver;
    private VideoPart mVideo;
    OnResolvedListener OnResolved = new OnResolvedListener() {
        @Override
        public void onResolved(Resolver resolver) {
                if(resolver.getMediaList() != null){
                    mProgressText.setText(mProgressText.getText()
                            +"完毕..共"
                            +resolver.getMediaList().size()
                            +"段\n"
                            +"开始加载弹幕...");
                    addDanmakusRequest();
                    
                }else{
                    mProgressText.setText(mProgressText.getText()+"失败!");
                    // TODO: show retry
                }
        }

    };
    private boolean mEnabledHW;
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        super.onCreate(savedInstanceState);
        Parcelable extra = getIntent().getParcelableExtra(EXTRA_VIDEO);
        if(extra == null) {
            Toast.makeText(this, "出错了！EXTRA_VIDEO == null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        mEnabledHW = getIntent().getBooleanExtra(EXTRA_PREFER_HW, false);
        setContentView(R.layout.activity_player);
        initVideoView(extra);
        initAnimation();
    }
    private void initVideoView(Parcelable extra) {
        mVideo = (VideoPart)extra;
        LibsChecker.checkVitamioLibs(this);
        mVideoView = (VideoView) findViewById(R.id.video);
        mVideoView.setPreferHWDecoder(mEnabledHW);
        
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("User-Agent", BaseResolver.UA_DEFAULT);
        mVideoView.setVideoHeaders(headers);
        mBufferingIndicator = findViewById(R.id.buffering_indicator);
        mVideoView.setMediaBufferingIndicator(mBufferingIndicator);
        mProgressText = (TextView)findViewById(R.id.progress_text);
        mVideoView.setOnPreparedListener(onPrepared);
    }
    private void addDanmakusRequest() {
        String url ="http://comment.acfun.tv/" + mVideo.commentId + ".json";
        // TODO: acfun lock danmakus
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub
                mProgressText.setText(mProgressText.getText()
                        +"\n弹幕文件下载完毕."
                        +"\n开始解析...");
//                try {
//                    DMSiteType type = DMSiteType.valueOf(mSite.toUpperCase());
//                    ILoader loader = type.getLoader();
//                    loader.loadData(response);
//                    BaseDanmakuParser parser = type.getParser().load(loader.getDataSource());
//                    mDMView.prepare(parser);
//                } catch (Exception e) {
//                    Log.e("Play", "解析失败",e);
                    mProgressText.setText(mProgressText.getText()
                            +"\n解析失败...");
                        
//                }
                startPlay();
            }}, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    mProgressText.setText(mProgressText.getText()
                            +"\n弹幕文件下载失败！");
                    startPlay();
                    
                }}) {
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String parsed;
                parsed = new String(response.data, Charset.defaultCharset());
                return Response.success(parsed, Connectivity.newCache(response, 60));
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                if(headers == null || headers.equals(Collections.EMPTY_MAP)){
                    headers = new HashMap<String, String>();
                }
                headers.put("User-Agent", BaseResolver.UA_DEFAULT);
                return headers;
            }
        };
        Connectivity.addRequest(request);
    }
    private void hideTextDelayed() {
        mProgressText.postDelayed(new Runnable() {
            @Override
            public void run() {
                mProgressText.startAnimation(mTextAnimation);
            }
        }, 1500);
    }
    private void startPlay() {
        mProgressText.setText(mProgressText.getText()
                +"\n开始缓冲视频..请稍候...");
        mVideoView.setMediaList(mResolver.getMediaList());
        mVideoView.requestFocus();
        mVideoView.start();
    }
    private void initAnimation() {
        mTextAnimation = AnimationUtils.loadAnimation(mProgressText.getContext(), R.anim.fade_out);
        mTextAnimation.setAnimationListener(new AnimationListener() {
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
    @Override
    public void onStart() {
        super.onStart();
        if (mResolver != null && mResolver.getMediaList() != null) {
            return;
        }
        resolveVideos();
    }
    private void resolveVideos() {
        String sourceType = mVideo.type;
        
        ResolverType type = null;
        try {
            type = ResolverType.valueOf(sourceType.toUpperCase());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        if(type == null){
            mProgressText.setText(mProgressText.getText()+"\n"+getString(R.string.source_type_not_support_yet));
            Toast.makeText(this, getString(R.string.source_type_not_support_yet), Toast.LENGTH_SHORT).show();
            return;
        }
        mResolver = type.getResolver(mVideo.sourceId);
        mResolver.setOnResolvedListener(OnResolved);
        mResolver.resolveAsync(getApplicationContext());
        mProgressText.setText(mProgressText.getText()+"\n视频分段解析中...");
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }
}
