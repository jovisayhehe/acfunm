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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * @author Yrom
 * 
 */
public abstract class BaseArrayAdapter<T> extends BaseAdapter {
    protected List<T> mItems;
    protected LayoutInflater mInflater;

    public BaseArrayAdapter(Context context, List<T> items) {
        mInflater = LayoutInflater.from(context);
        mItems = items;
    }

    @Override
    public int getCount() {
        if (mItems == null || mItems.isEmpty()) return 0;
        return mItems.size();
    }

    @Override
    public T getItem(int position) {
        return mItems.get(position);
    }

    public T remove(int position) {
        if (mItems == null) return null;
        T t = mItems.remove(position);
        notifyDataSetChanged();
        return t;
    }

    public void setData(List<T> data) {
        clear();
        mItems.addAll(data);
    }

    public void clear() {
        if (mItems != null) {
            mItems.clear();
            notifyDataSetChanged();
        }
    }

    // TODO : 去重
    public void addData(List<T> data) {
        if (mItems != null) {
            mItems.addAll(data);
            notifyDataSetChanged();
        }
    }

    /**
     * @return position
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract View getView(int position, View convertView, ViewGroup parent);
}
