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

package tv.acfun.video.adapter;

import java.util.List;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;

import tv.ac.fun.R;
import tv.acfun.video.AcApp;
import tv.acfun.video.entity.Comment;
import tv.acfun.video.util.TextViewUtils;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Yrom
 * 
 */
public class CommentsAdapter extends BaseAdapter {

    protected LayoutInflater mInflater;
    private SparseArray<Comment> data;
    private List<Integer> commentIdList;

    public CommentsAdapter(Context context, SparseArray<Comment> data, List<Integer> commentIdList) {
        this.mInflater = LayoutInflater.from(context);
        this.data = data;
        this.commentIdList = commentIdList;
    }

    public void setData(SparseArray<Comment> data, List<Integer> commentIdList) {
        this.data = data;
        this.commentIdList = commentIdList;
    }

    @Override
    public int getCount() {
        if(commentIdList == null) return 0;
        return commentIdList.size();
    }

    @Override
    public Comment getItem(int position) {
        try {
            Integer id = commentIdList.get(position);
            if (id != null)
                return data.get(id);
        } catch (IndexOutOfBoundsException e) {}
        return null;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Comment c = getItem(position);

        CommentViewHolder holder = null;
        if (convertView == null) {
            holder = new CommentViewHolder();
            convertView = mInflater.inflate(R.layout.item_comments, parent,false);
            holder.user = (TextView) convertView.findViewById(R.id.user_name);
            holder.content = (TextView) convertView.findViewById(R.id.comments_content);
            holder.avatar = (ImageView) convertView.findViewById(R.id.user_avatar);
            convertView.setTag(holder);
        } else {
            holder = (CommentViewHolder) convertView.getTag();
        }
        holder.user.setText("#" + c.count + " " + c.userName);
        TextViewUtils.setCommentContent(holder.content, c);
        if(holder.container != null && !c.userImg.equals(holder.container.getRequestUrl()) && holder.container.getBitmap() == null){
            holder.container.cancelRequest();
        }
        ImageContainer container = AcApp.getGloableLoader().get(c.userImg, ImageLoader.getImageListener(holder.avatar, R.drawable.acgirl, R.drawable.acgirl));
        holder.container = container;
        
        return convertView;
    }


    static class CommentViewHolder {
        ImageView avatar;
        TextView user;
        TextView content;
        ImageContainer container;

    }
}
