
package tv.avfun.adapter;

import java.util.List;
import java.util.Map;

import tv.avfun.R;
import tv.avfun.entity.VideoInfo.VideoItem;
import tv.avfun.util.Maps;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DetailAdaper extends BaseAdapter {

    private static final int      TAG_VID            = 100;
    private static final int      STATUS_NONE        = 0;
    private static final int      STATUS_DOWNLOADED  = 1;
    private static final int      STATUS_DOWNLOADING = 2;
    private static final String TAG = DetailAdaper.class.getSimpleName();
    private List<VideoItem>       mData;
    private LayoutInflater        mInflater;
    private OnStatusClickListener mListener;

    public DetailAdaper(LayoutInflater inflater, List<VideoItem> items) {
        if (items == null)
            throw new NullPointerException("items cannot be null!!!");
        this.mData = items;
        this.mInflater = inflater;
    }

    /**
     * 设置新的data，同时通知ListView刷新数据
     * 
     * @param data
     *            items
     */
    public void setData(List<VideoItem> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public VideoItem getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final VideoItem item = getItem(position);
        ItemViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.detail_video_list_item, parent, false);
            holder = new ItemViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.detail_video_list_item_title);
            holder.vtype = (TextView) convertView.findViewById(R.id.detail_video_list_item_vtype);
            convertView.setTag(holder);
        } else {
            holder = (ItemViewHolder) convertView.getTag();
        }
        ItemHolder iHolder= new ItemHolder();
        iHolder.status = (TextView) convertView.findViewById(R.id.detail_status);
        iHolder.bar = (ProgressBar) convertView.findViewById(R.id.detail_progress);
        if (item.isdownloaded) {
            iHolder.status.setText("已下载");
            iHolder.status.setTag(STATUS_DOWNLOADED);
        } else {
            iHolder.status.setText("下载");
            iHolder.status.setTag(STATUS_NONE);
        }
        iHolder.status.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null)
                    return;
                Integer status = (Integer) tag;
                switch (status.intValue()) {
                case STATUS_NONE:
                    startDownload(item.vid);
                    v.setTag(STATUS_DOWNLOADING);
                    ((TextView)v).setText("取消");
                    break;
                case STATUS_DOWNLOADED:
                    viewDownload(item.vid);
                    break;
                case STATUS_DOWNLOADING:
                    cancelDownload(item.vid);
                    v.setTag(STATUS_NONE);
                    ((TextView)v).setText("下载");
                    break;
                }
            }
        });
        items.put(item.vid, iHolder);
        
        //holder.status.setTag(TAG_VID, item.vid);
        holder.title.setText(item.subtitle);
        holder.vtype.setText(item.vtype);
        return convertView;
    }

    private Map<String, ItemHolder> items = Maps.newHashMap(); 
    /**
     * 找到跟vid对应的进度条
     * 
     * @param vid
     * @return
     */
    public ProgressBar findPB(String vid) {
        ItemHolder ih = items.get(vid);
        if(ih!=null)
            return ih.bar;
        return null;
    }
    public TextView findStatus(String vid){
        ItemHolder ih = items.get(vid);
        if(ih!=null)
            return ih.status;
        return null;
    }
    public void setProgress(String vid, int progress, int total) {
        ProgressBar bar = findPB(vid);
        if (bar == null)
            return;
        bar.setIndeterminate(false);
        bar.setMax(total);
        bar.setProgress(progress);
    }

    public void showProgress(String vid) {
        ProgressBar bar = findPB(vid);
        if (bar == null)
            return;
        bar.setIndeterminate(true);
        bar.setVisibility(View.VISIBLE);
    }

    public void hideProgress(String vid) {
        ProgressBar bar = findPB(vid);
        if (bar == null)
            return;
        bar.setVisibility(View.INVISIBLE);
    }


    static class ItemViewHolder {

        public TextView title;
        public TextView vtype;
    }
    class ItemHolder{
        ProgressBar bar;
        TextView status;
    }
    private void startDownload(String vid) {
        if (mListener != null)
            mListener.doStartDownload(vid);
        Log.i(TAG, "开始下载..."+vid);
    }

    private void cancelDownload(String vid) {
        if (mListener != null)
            mListener.doCancelDownload(vid);
        Log.i(TAG, "取消下载..."+vid);
    }

    private void viewDownload(String vid) {
        if (mListener != null)
            mListener.doViewDownloadInfo(vid);
        Log.i(TAG, "查看下载..."+vid);
    }

    public void setOnStatusClickListener(OnStatusClickListener l) {
        this.mListener = l;
    }

    public interface OnStatusClickListener {

        /**
         * 开始下载
         * 
         * @param view
         * @param vid
         */
        void doStartDownload(String vid);

        /**
         * 取消（删除）下载
         * 
         * @param view
         * @param vid
         */
        void doCancelDownload(String vid);

        void doViewDownloadInfo(String vid);
    }
}
