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

package tv.acfun.video;

import java.util.ArrayList;

import com.android.volley.VolleyError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.ImageLoader;

import tv.acfun.video.api.API;
import tv.acfun.video.entity.Video;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.util.FadingActionBarHelper;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.net.FastJsonRequest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Yrom
 * 
 */
public class DetailsActivity extends ActionBarActivity implements OnClickListener {
    private ImageView mHeaderImage;
    private TextView mTitleView,mUpInfoView,mDetailView;
    protected Video mVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
        initViews();
        
        String preview = getIntent().getStringExtra("preview");
        AcApp.getGloableLoader().get(preview, ImageLoader.getImageListener(mHeaderImage,0,0));
        int acId = getIntent().getIntExtra("acid", 0);
        getSupportActionBar().setTitle("ac"+acId);
        AcApp.addRequest(new VideoDetailsRequest(acId, listener, errorListner));
    }

    private void initViews() {
        mHeaderImage = (ImageView) findViewById(R.id.image_header);
        mTitleView = (TextView) findViewById(R.id.title);
        mUpInfoView = (TextView) findViewById(R.id.up_info);
        mDetailView = (TextView) findViewById(R.id.details);
        mPartsGroup = (LinearLayout) findViewById(R.id.parts);
        int height = getResources().getDisplayMetrics().widthPixels / 4 * 3;
        LayoutParams params = mHeaderImage.getLayoutParams();
        params.height = height;
        mHeaderImage.setLayoutParams(params);
    }

    private void initActionBar() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FadingActionBarHelper helper = new FadingActionBarHelper().actionBarBackground(R.drawable.ab_solid_styled)
                .headerLayout(R.layout.details_header).contentLayout(R.layout.activity_details);
        setContentView(helper.createView(this));
        helper.initActionBar(this);
    }
    
    public static void start(Context context, Video video) {
        Intent intent = new Intent(context.getApplicationContext(), DetailsActivity.class);
        intent.putExtra("acid", video.acId);
        intent.putExtra("preview", video.previewurl);
        context.startActivity(intent);
    }
    Listener<Video> listener = new Listener<Video>() {
        @Override
        public void onResponse(Video response) {
            mVideo = response;
            mTitleView.setText(response.name);

            String info = String.format("%s / 发布于 %s / %d次播放，%d条评论，%d人收藏",
                    response.creator.name,
                    AcApp.getPubDate(response.createtime),
                    response.viewernum,
                    response.commentnum,
                    response.collectnum);
            mUpInfoView.setText(info);
            mDetailView.setText(Html.fromHtml(TextViewUtils.getSource(response.desc)));
            addParts(0);
        }
    };
    private void addParts(int start) {
        ArrayList<VideoPart> episodes = mVideo.episodes;
        if(start > 0 )
        mPartsGroup.removeViewAt(mPartsGroup.getChildCount()-1);
        for(int i=0;i<episodes.size()-start;i++){
            if(i != 0) getLayoutInflater().inflate(R.layout.item_divider_h, mPartsGroup);
            if(i == 10) {
                View more = getLayoutInflater().inflate(R.layout.item_more, mPartsGroup,false);
                more.setTag(i+start);
                more.setOnClickListener(DetailsActivity.this);
                mPartsGroup.addView(more);
                break;
            }
            VideoPart part = episodes.get(i+start);
            addPart(i+start,part);
        }
    }
    private void addPart(int position, VideoPart item) {
        View partView = getLayoutInflater().inflate(R.layout.item_videoparts, mPartsGroup,false);
        partView.setOnClickListener(this);
        partView.setTag(item);
        TextView name = (TextView) partView.findViewById(R.id.part_name);
        TextView desc = (TextView) partView.findViewById(R.id.part_desc);
        String text = TextUtils.isEmpty(item.name) ? "点击查看视频" : (position + 1) + ". " + item.name;
        name.setText(text);
        desc.setText("视频源: " + item.type);
        mPartsGroup.addView(partView);
    }
    ErrorListener errorListner = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            // TODO Auto-generated method stub
        }
    };
    private LinearLayout mPartsGroup;

    public static class VideoDetailsRequest extends FastJsonRequest<Video> {
        public VideoDetailsRequest(int acId, Listener<Video> listener, ErrorListener errorListner) {
            super(API.getVideoDetailsUrl(acId), Video.class, listener, errorListner);
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
        case R.id.item_more:
            Integer i = (Integer) v.getTag();
            addParts(i.intValue());
            break;
        case R.id.item_part:
            Object tag = v.getTag();
            if(tag != null && tag instanceof VideoPart){
                onPartClick((VideoPart) tag);
            }
            break;
        default:
            break;
        }
    }
    private void onPartClick(VideoPart item){
        Toast.makeText(this, "click::"+item.name, 0).show();
    }
}
