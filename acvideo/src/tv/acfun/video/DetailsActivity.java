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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import master.flame.danmaku.danmaku.util.IOUtils;

import org.apache.commons.httpclient.Cookie;

import tv.ac.fun.R;
import tv.acfun.video.api.API;
import tv.acfun.video.entity.Comment;
import tv.acfun.video.entity.Comments;
import tv.acfun.video.entity.User;
import tv.acfun.video.entity.Video;
import tv.acfun.video.entity.VideoPart;
import tv.acfun.video.player.MediaList;
import tv.acfun.video.player.MediaList.OnResolvedListener;
import tv.acfun.video.player.MediaList.Resolver;
import tv.acfun.video.player.resolver.BaseResolver;
import tv.acfun.video.player.resolver.ResolverType;
import tv.acfun.video.util.FadingActionBarHelper;
import tv.acfun.video.util.FileUtil;
import tv.acfun.video.util.MemberUtils;
import tv.acfun.video.util.TextViewUtils;
import tv.acfun.video.util.download.DownloadEntry;
import tv.acfun.video.util.download.DownloadManager;
import tv.acfun.video.util.net.CommentsRequest;
import tv.acfun.video.util.net.FastJsonRequest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.umeng.analytics.MobclickAgent;

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
    private Cookie[] mCookies;
    private boolean isFaved;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel);
        initActionBar();
        User user = AcApp.getUser();
        if (user != null) mCookies = JSON.parseObject(user.cookies, Cookie[].class);
        AcApp.addRequest(new VideoDetailsRequest(mAcId, mVideoListener, mErrorListener));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
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
        if(TextUtils.isEmpty(preview) || mVideo != null) 
            preview = mVideo.previewurl;
        AcApp.getGloableLoader().get(preview, ImageLoader.getImageListener(mHeaderImage, R.drawable.cover_night, 0));
    }

    private void initActionBar() {
        mHelper = new FadingActionBarHelper().actionBarBackground(R.drawable.ab_solid_styled).headerLayout(R.layout.details_header)
                .headerOverlayLayout(R.layout.header_overlay).contentLayout(R.layout.activity_details);
        mHelper.initActionBar(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(Intent.ACTION_VIEW.equalsIgnoreCase(getIntent().getAction())
                &&getIntent().getData()!=null &&  getIntent().getData().getScheme().equals("av")){
            mAcId = Integer.parseInt(getIntent().getDataString().substring(7));
        }else{
            mAcId = getIntent().getIntExtra("acid", 0);
        }
        if (mAcId == 0)
            throw new IllegalArgumentException("没有 id");
        getSupportActionBar().setTitle("ac" + mAcId);
        
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
            if(mShareMenuItem != null)
                mShareMenuItem.setEnabled(true);
            initContent();
            initViews();
            mTitleView.setText(response.name);
            String info =getString(R.string.details_info,/* String.format("<font color=\"#ff8800\">%s</font> / 发布于 %s <br/>%d次播放，%d条评论，%d人收藏",*/ response.creator.name,
                    AcApp.getPubDate(response.createtime), response.viewernum, response.commentnum, response.collectnum);
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
        View commentView = getLayoutInflater().inflate(R.layout.item_comments, mCommentsGroup, false);
        TextView name = (TextView) commentView.findViewById(R.id.user_name);
        TextView content = (TextView) commentView.findViewById(R.id.comments_content);
        ImageView avatar = (ImageView) commentView.findViewById(R.id.user_avatar);
        name.setText("#" + comment.count + "  " + comment.userName);
        if (!TextUtils.isEmpty(comment.userImg)) AcApp.getGloableLoader().get(comment.userImg, ImageLoader.getImageListener(avatar, 0, 0));
        TextViewUtils.setCommentContent(content, comment);
        mCommentsGroup.addView(commentView);
    }

    private void addPart(int position, VideoPart item) {
        View partView = getLayoutInflater().inflate(R.layout.item_videoparts, mPartsGroup, false);
        partView.setOnClickListener(this);
        partView.setTag(item);
        TextView name = (TextView) partView.findViewById(R.id.part_name);
        TextView desc = (TextView) partView.findViewById(R.id.part_desc);
        if(TextUtils.isEmpty(item.name)){
            item.name = "Part "+ (position + 1);
        }
        String text = (position + 1) + ". " + item.name;
        name.setText(text);
        desc.setText("来源: " + item.type);
        partView.findViewById(R.id.part_overlow).setOnClickListener(this);
        mPartsGroup.addView(partView);
    }

    ErrorListener mErrorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            findViewById(R.id.loading).setVisibility(View.GONE);
            View retry = findViewById(R.id.tips_retry);
            if (retry == null) {
                ViewStub stub = (ViewStub) findViewById(R.id.view_stub);
                stub.setLayoutResource(R.layout.tips_retry);
                stub.setInflatedId(R.id.tips_retry);
                View view = stub.inflate();
                view.findViewById(android.R.id.button1).setOnClickListener(DetailsActivity.this);
            } else {
                retry.setVisibility(View.VISIBLE);
            }
        }
    };
    private int mAcId;
    private DownloadManager manager;
    private MenuItem mShareMenuItem;

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
            if (tag != null && tag instanceof Integer) {
                addParts(((Integer) tag).intValue());
            } else {
//                Intent intent = new Intent();
//                intent.putExtra("aid", mVideo.acId);
//                AcApp.startArea63(this, "tv.acfun.a63.CommentsActivity", intent);
                CommentsActivity.start(this, mAcId);
            }
            break;
        case R.id.item_part:
            tag = v.getTag();
            if (tag != null && tag instanceof VideoPart) {
                onPartClick((VideoPart) tag);
            }
            break;
        case android.R.id.button1:
            if (mVideo == null) {
                findViewById(R.id.loading).setVisibility(View.VISIBLE);
                findViewById(R.id.tips_retry).setVisibility(View.GONE);
                AcApp.addRequest(new VideoDetailsRequest(mAcId, mVideoListener, mErrorListener));
            } else {
                requestComments();
                mCommentsGroup.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
                mCommentsGroup.removeViewAt(mCommentsGroup.getChildCount() - 1);
            }
            break;
        case R.id.play_btn:
            onPartClick(mVideo.episodes.get(0));
            break;
        case R.id.part_overlow:
            PopupMenu menu = new PopupMenu(v.getContext(), v);
            menu.inflate(R.menu.menu_details_download);
            tag = ((View)v.getParent()).getTag();
            menu.setOnMenuItemClickListener(new OnDownloadMenuClick((VideoPart) tag));
            menu.show();
            break;
        default:
            break;
        }
    }
    private class OnDownloadMenuClick implements OnMenuItemClickListener{
        VideoPart mPart;
        public OnDownloadMenuClick(VideoPart part){
            mPart = part;
        }
        @Override
        public boolean onMenuItemClick(MenuItem arg0) {
            if(arg0.getItemId() == R.id.menu_download){
                startDownload(mPart);
            }else if(arg0.getItemId() == R.id.menu_watch_later){
            }
            return false;
        }
        
    }
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.details, menu);
        if (mCookies != null) new Thread() {
            public void run() {
                isFaved = MemberUtils.checkFavourite(mCookies, mAcId);
                if (isFaved) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MenuItem item = menu.findItem(R.id.action_fav);
                            item.setTitle("取消收藏");
                            item.setIcon(R.drawable.ic_action_favorited);
                        }
                    });
                }
            }
        }.start();
        mShareMenuItem = menu.findItem(R.id.action_share);
        if(mVideo != null) mShareMenuItem.setEnabled(true);
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void startDownload(final VideoPart part) {
        if(manager==null) manager = AcApp.getDownloadManager();
        if(manager.getProvider().isPartDownloaded(part))
            Toast.makeText(getApplicationContext(), "已下载", 0).show();
        else
            download(part);
    }

    private void download(final VideoPart part) {
        Log.i("D", "start download:::"+part.name);
        ResolverType type = null;
        try {
            type = ResolverType.valueOf(part.type.toUpperCase(Locale.US));
        } catch (Exception e) {
        }
        if(type == null){
            Toast.makeText(getApplicationContext(), getString(R.string.source_type_not_support_yet), Toast.LENGTH_SHORT).show();
            return;
        }
        Resolver resolver = type.getResolver(part.sourceId);
        int resolution = Integer.parseInt(AcApp.getString(getString(R.string.key_resolution_mode), "1"));
        if(resolution < BaseResolver.RESOLUTION_HD2) resolution = BaseResolver.RESOLUTION_HD2;
        ((BaseResolver) resolver).setResolution(resolution);
        resolver.setOnResolvedListener(new OnResolvedListener() {
            @Override
            public void onResolved(Resolver resolver) {
                MediaList list = resolver.getMediaList();
                if(list == null){
                    Toast.makeText(getApplicationContext(), getString(R.string.parsing_failed), Toast.LENGTH_SHORT).show();
                }else{
                    part.segments = list.toSegments();
                    DownloadEntry entry = new DownloadEntry(String.valueOf(mAcId), mVideo.name, part);
                    manager.download(entry);
                    Toast.makeText(getApplicationContext(), String.format("ac%d - %s已加到下载队列",mAcId,part.name), Toast.LENGTH_SHORT).show();
                }
            }
        });
        resolver.resolveAsync(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_settings:
            SettingsActivity.start(this);
            return true;
        case R.id.action_feedback:
            startActivity(new Intent(this, ConversationActivity.class));
            return true;
        case android.R.id.home:
            finish();
            return true;
        case R.id.action_fav:
            handleFav(item);
            return true;
        case R.id.action_comment:
            CommentsActivity.start(this, mAcId);
            return true;
        case R.id.action_download_manager:
            startActivity(new Intent(this, DownloadManActivity.class));
            return true;
        case R.id.action_share:
            Intent share = new Intent(Intent.ACTION_SEND);
            share.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content,mVideo.name,mVideo.acId));
            byte[] data = AcApp.getDataInDiskCache(mVideo.previewurl);
            if(data!= null){
                File tempFile = new File(AcApp.getExternalCacheDir("temp"),"temp"+FileUtil.getUrlExt(mVideo.previewurl, ".jpg"));
                FileOutputStream out = null;
                try {
                    if(tempFile.exists()) {
                        tempFile.delete();
                    }
                    tempFile.createNewFile();
                    out = new FileOutputStream(tempFile);
                    out.write(data);
                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(tempFile));
                    share.setType("image/jpeg");
                } catch (IOException e) {
                    e.printStackTrace();
                    share.setType("text/plain");
                } finally{
                    IOUtils.closeQuietly(out);
                }
            }else{
                share.setType("text/plain");
            }
            Intent intent  = Intent.createChooser(share, getString(R.string.action_share)+"ac"+mVideo.acId);
            startActivity(intent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void handleFav(final MenuItem item) {
        if(isFaved){
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(which == DialogInterface.BUTTON_POSITIVE){
                        new Thread(){
                            public void run() {
                                boolean deleteFavourite = MemberUtils.deleteFavourite(String.valueOf(mAcId), mCookies);
                                //TODO 提示
                                isFaved = !deleteFavourite;
                                Log.i("Delete", "deleteFavourite::"+mAcId+":"+deleteFavourite);
                            }
                        }.start();
                        item.setTitle("收藏");
                        item.setIcon(R.drawable.ic_action_favorite);
                    }
                }
            };
            AcApp.showDeleteFavAlert(this,listener);
        }else{
            if(mCookies == null){
                Toast.makeText(getApplicationContext(), "请先登录", Toast.LENGTH_SHORT).show();
                
            }else{
                new Thread(){
                    public void run() {
                        boolean add = MemberUtils.addFavourite(String.valueOf(mAcId), mCookies);
                      //TODO 提示
                        Log.i("add", "addFavourite::"+mAcId+":"+add);
                    }
                }.start();
                item.setTitle("取消收藏");
                item.setIcon(R.drawable.ic_action_favorited);
            }
        }
    }

    private void onPartClick(final VideoPart item) {
        PlayerActivity.start(DetailsActivity.this, item);
    }
}
