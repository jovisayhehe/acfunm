
package tv.avfun.adapter;

import java.util.List;
import java.util.Map;

import tv.ac.fun.R;
import tv.avfun.entity.VideoPart;
import tv.avfun.util.Maps;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * 详情页 listview 的adapter
 * @author Yrom
 *
 */
public class DetailAdaper extends BaseAdapter {

    private static final int      STATUS_NONE        = 0;
    private static final int      STATUS_DOWNLOADED  = 1;
    private static final int      STATUS_DOWNLOADING = 2;
    private static final String TAG = DetailAdaper.class.getSimpleName();
    private List<VideoPart>       mData;
    private LayoutInflater        mInflater;
    private OnStatusClickListener mListener;

    public DetailAdaper(LayoutInflater inflater, List<VideoPart> items) {
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
    public void setData(List<VideoPart> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public VideoPart getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final VideoPart item = getItem(position);
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
        if (item.isDownloaded) {
            iHolder.status.setText("已下载");
            iHolder.status.setTag(STATUS_DOWNLOADED);
        }else if(item.isDownloading){
            iHolder.status.setText("下载中");
            iHolder.status.setTag(STATUS_DOWNLOADING);
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
                    startDownload(v, item);
                    v.setTag(STATUS_DOWNLOADING);
                    ((TextView)v).setText("下载中");
                    break;
                case STATUS_DOWNLOADING:
//                    将取消的操作交给Download Manager Activity来管理
//                    cancelDownload(item.vid);
//                    v.setTag(STATUS_NONE);
//                    ((TextView)v).setText("下载");
                case STATUS_DOWNLOADED:
                    viewDownload(item.vid);
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
    private void startDownload(View v, VideoPart item) {
        if (mListener != null)
            mListener.doStartDownload(v, item);
        Log.i(TAG, "开始下载..."+item.vid);
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
        void doStartDownload(View v, VideoPart item);

        /**
         * 查看下载
         * @param vid
         */
        void doViewDownloadInfo(String vid);
    }
}
