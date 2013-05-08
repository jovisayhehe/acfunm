package tv.avfun.adapter;

import java.util.List;

import tv.avfun.entity.VideoInfo.VideoItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;


public class DetailAdaper extends BaseAdapter {
    private List<VideoItem> data;
    public DetailAdaper(List<VideoItem> items){
        if(items == null) throw new NullPointerException("items cannot be null!!!");
        this.data = items;
    }
    /**
     * 设置新的data，同时通知ListView刷新数据
     * @param data items
     */
    public void setData(List<VideoItem> data){
        this.data = data;
        this.notifyDataSetChanged();
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public VideoItem getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        return null;
    }

}
