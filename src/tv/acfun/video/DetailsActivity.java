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

import tv.acfun.video.api.API;
import tv.acfun.video.entity.Comment;
import tv.acfun.video.entity.Comments;
import tv.acfun.video.entity.Video;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.util.FadingActionBarHelper;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.net.CommentsRequest;
import tv.acfun.video.util.net.FastJsonRequest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;

/**
 * @author Yrom
 * 
 */
public class DetailsActivity extends ActionBarActivity implements OnClickListener {
    private ImageView mHeaderImage;
    private TextView mTitleView, mUpInfoView, mDetailView;
    private Video mVideo;
    private LinearLayout mPartsGroup, mCommentsGroup;
    private FadingActionBarHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        initActionBar();
    }

    private void initViews() {
        mHeaderImage = (ImageView) findViewById(R.id.image_header);
        mTitleView = (TextView) findViewById(R.id.title);
        mUpInfoView = (TextView) findViewById(R.id.up_info);
        mDetailView = (TextView) findViewById(R.id.details);
        mPartsGroup = (LinearLayout) findViewById(R.id.parts);
        mCommentsGroup = (LinearLayout) findViewById(R.id.comments);
        findViewById(R.id.play_btn).setOnClickListener(this);
        int height = getResources().getDisplayMetrics().widthPixels / 16 * 9;
        LayoutParams params = mHeaderImage.getLayoutParams();
        params.height = height;
        mHeaderImage.setLayoutParams(params);
        String preview = getIntent().getStringExtra("preview");
        AcApp.getGloableLoader().get(preview, ImageLoader.getImageListener(mHeaderImage, R.drawable.cover_night, 0));
    }

    private void initActionBar() {
        mHelper = new FadingActionBarHelper()
                .actionBarBackground(R.drawable.ab_solid_styled)
                .headerLayout(R.layout.details_header)
                .headerOverlayLayout(R.layout.header_overlay)
                .contentLayout(R.layout.activity_details);
        mHelper.initActionBar(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mAcId = getIntent().getIntExtra("acid", 0);
        getSupportActionBar().setTitle("ac" + mAcId);
        AcApp.addRequest(new VideoDetailsRequest(mAcId, mVideoListener, mErrorListener));
    }

    private void initContent() {
        setContentView(mHelper.createView(this));
    }

    public static void start(Context context, Video video) {
        Intent intent = new Intent(context.getApplicationContext(), DetailsActivity.class);
        intent.putExtra("acid", video.acId);
        intent.putExtra("preview", video.previewurl);
        context.startActivity(intent);
    }

    Listener<Video> mVideoListener = new Listener<Video>() {
        @Override
        public void onResponse(Video response) {
            mVideo = response;
            initContent();
            initViews();
            mTitleView.setText(response.name);
            String info = String.format("<font color=\"#ff8800\">%s</font> / 发布于 %s <br/>%d次播放，%d条评论，%d人收藏", 
                    response.creator.name,
                    AcApp.getPubDate(response.createtime),
                    response.viewernum, 
                    response.commentnum, 
                    response.collectnum);
            mUpInfoView.setText(Html.fromHtml(info));
            mDetailView.setText(Html.fromHtml(TextViewUtils.getSource(response.desc)));
            addParts(0);
            requestComments();
        }

    };
    private void requestComments() {
        AcApp.addRequest(new CommentsRequest(mVideo.acId, 1, mCommentListener, new ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mCommentsGroup.findViewById(R.id.progressBar).setVisibility(View.GONE);
                getLayoutInflater().inflate(R.layout.tips_retry, mCommentsGroup);
                mCommentsGroup.findViewById(android.R.id.button1).setOnClickListener(DetailsActivity.this);
            }
        }));
    }
    Listener<Comments> mCommentListener = new Listener<Comments>() {
        @Override
        public void onResponse(Comments response) {
            mCommentsGroup.findViewById(R.id.progressBar).setVisibility(View.GONE);
            if (response.totalCount == 0) {
                Toast.makeText(getApplicationContext(), "目前尚未有评论。", Toast.LENGTH_SHORT).show();
                return;
            }
            for (int i = 0; i < response.totalCount; i++) {
                if (i != 0) getLayoutInflater().inflate(R.layout.item_divider_h, mCommentsGroup);
                if (i >= 15) {
                    View more = getLayoutInflater().inflate(R.layout.item_more, mCommentsGroup, false);
                    more.setOnClickListener(DetailsActivity.this);
                    mCommentsGroup.addView(more);
                    break;
                }
                int id = response.commentList[i];
                Comment comment = response.commentArr.get(id);
                addComment(comment);
            }
        }
    };

    private void addParts(int start) {
        ArrayList<VideoPart> episodes = mVideo.episodes;
        if (start > 0) mPartsGroup.removeViewAt(mPartsGroup.getChildCount() - 1);
        for (int i = 0; i < episodes.size() - start; i++) {
            if (i != 0) getLayoutInflater().inflate(R.layout.item_divider_h, mPartsGroup);
            if (i == 10) {
                View more = getLayoutInflater().inflate(R.layout.item_more, mPartsGroup, false);
                more.setTag(i + start);
                more.setOnClickListener(DetailsActivity.this);
                mPartsGroup.addView(more);
                break;
            }
            VideoPart part = episodes.get(i + start);
            addPart(i + start, part);
        }
    }

    private void addComment(Comment comment) {
        View commentView = getLayoutInflater().inflate(R.layout.item_comments,mCommentsGroup,false);
        TextView name = (TextView) commentView.findViewById(R.id.user_name);
        TextView content = (TextView) commentView.findViewById(R.id.comments_content);
        ImageView avatar = (ImageView) commentView.findViewById(R.id.user_avatar);
        name.setText("#"+comment.count+"  "+comment.userName);
        if(!TextUtils.isEmpty(comment.userImg))
        AcApp.getGloableLoader().get(comment.userImg, ImageLoader.getImageListener(avatar, 0, 0));
        TextViewUtils.setCommentContent(content, comment);
        mCommentsGroup.addView(commentView);
    }

    private void addPart(int position, VideoPart item) {
        View partView = getLayoutInflater().inflate(R.layout.item_videoparts, mPartsGroup, false);
        partView.setOnClickListener(this);
        partView.setTag(item);
        TextView name = (TextView) partView.findViewById(R.id.part_name);
        TextView desc = (TextView) partView.findViewById(R.id.part_desc);
        String text = TextUtils.isEmpty(item.name) ? "点击查看视频" : (position + 1) + ". " + item.name;
        name.setText(text);
        desc.setText("来源: " + item.type);
        mPartsGroup.addView(partView);
    }

    ErrorListener mErrorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            findViewById(R.id.loading).setVisibility(View.GONE);
            View retry = findViewById(R.id.tips_retry);
            if(retry == null){
                ViewStub stub = (ViewStub) findViewById(R.id.view_stub);
                stub.setLayoutResource(R.layout.tips_retry);
                stub.setInflatedId(R.id.tips_retry);
                View view = stub.inflate();
                view.findViewById(android.R.id.button1).setOnClickListener(DetailsActivity.this);
            }else{
                retry.setVisibility(View.VISIBLE);
            }
        }
    };
    private int mAcId;

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
            Object tag = v.getTag();
            if(tag != null && tag instanceof Integer){
                addParts(((Integer) tag).intValue());
            }else{
                Intent intent = new Intent();
                intent.putExtra("aid", mVideo.acId);
                AcApp.startArea63(this, "tv.acfun.a63.CommentsActivity", intent);

            }
            break;
        case R.id.item_part:
            tag = v.getTag();
            if (tag != null && tag instanceof VideoPart) {
                onPartClick((VideoPart) tag);
            }
            break;
        case android.R.id.button1:
            if(mVideo == null){
                findViewById(R.id.loading).setVisibility(View.VISIBLE);
                findViewById(R.id.tips_retry).setVisibility(View.GONE);
                AcApp.addRequest(new VideoDetailsRequest(mAcId, mVideoListener, mErrorListener));
            }else{
                requestComments();
                mCommentsGroup.findViewById(R.id.loading).setVisibility(View.VISIBLE);
                mCommentsGroup.removeViewAt(mCommentsGroup.getChildCount()-1);
            }
            break;
        case R.id.play_btn:
            onPartClick(mVideo.episodes.get(0));
            break;
        default:
            break;
        }
    }

    private void onPartClick(final VideoPart item) {
        Toast.makeText(this, "click::" + item.name, 0).show();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_NEUTRAL) {
                    PlayerSysActivity.start(DetailsActivity.this, item);
                } else
                    PlayerActivity.start(DetailsActivity.this, item, which == DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            }
        };
        new AlertDialog.Builder(this)
                .setTitle("是否开启硬解")
                .setMessage("软件硬解 和系统硬解")
                .setPositiveButton("软件", listener)
                .setNeutralButton("系统", listener)
                .setNegativeButton("否", listener)
                .show();
    }
}
