package tv.acfun.video.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import tv.ac.fun.R;
import tv.acfun.video.util.FileUtil;
import tv.acfun.video.util.download.DownloadDB;
import tv.acfun.video.util.download.DownloadEntry;
import tv.acfun.video.util.download.DownloadJob;
import tv.acfun.video.util.download.DownloadManager;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
/**
 * Download job list adapter
 * @author Yrom
 *
 */
public class DownloadJobAdapter extends BaseArrayAdapter<DownloadJob> implements OnCheckedChangeListener {
    private List<DownloadJob> checkedJobs;
    private OnItemCheckedListener mListener;
	public DownloadJobAdapter(Activity activity) {
		this(activity, null);
	}
	public DownloadJobAdapter(Activity activity, ArrayList<DownloadJob> list){
	    super(activity, list);
	    checkedJobs = Collections.synchronizedList(new LinkedList<DownloadJob>());
	}
	public void removeJob(DownloadJob job){
	    mItems.remove(job);
	    checkedJobs.remove(job);
	    notifyDataSetChanged();
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;
		DownloadJob job = getItem(position);
		ViewHolder holder;

		if (row==null) {
			row=mInflater.inflate(R.layout.download_man_list_item, parent,false);

			holder = new ViewHolder();
			holder.title = (TextView)row.findViewById(R.id.title);
			holder.acid = (TextView)row.findViewById(R.id.acid);
			holder.progressText = (TextView)row.findViewById(R.id.download_size);
			holder.progressBar = (ProgressBar)row.findViewById(R.id.progress);
			row.setTag(holder);
		}
		else{
			holder = (ViewHolder) row.getTag();
		}
		DownloadEntry entry = job.getEntry();
		holder.title.setText(entry.title);
		holder.acid.setText("ac"+entry.aid);
		// TODO 尝试把checkbox也放到holder中
		CheckBox cb = (CheckBox) row.findViewById(R.id.download_checked);
		
		cb.setTag(Integer.valueOf(position));
		cb.setOnCheckedChangeListener(this);
	    cb.setChecked(checkedJobs.contains(job));
	    int totalSize = job.getTotalSize();
	    int downloadSize = job.getDownloadedSize();
		if(job.getProgress() == 100){
			holder.progressBar.setVisibility(View.GONE);
			holder.progressText.setText(FileUtil.formatFileSize(totalSize)+"/已完成");
		}else if(downloadSize== 0 && DownloadManager.isRunningStatus(job.getStatus())){
		    holder.progressBar.setVisibility(View.VISIBLE);
		    holder.progressBar.setIndeterminate(true);
		}else if(DownloadManager.isErrorStatus(job.getStatus())){
		    holder.progressBar.setVisibility(View.GONE);
		    if(job.getStatus() == DownloadDB.STATUS_CANCELED)
		        holder.progressText.setText("下载取消 ");
		    else
		        holder.progressText.setText("下载失败 - " + job.getStatus());
            
		}else if(totalSize >0 ){
		    if(DownloadManager.isRunningStatus(job.getStatus())){
		        holder.progressBar.setVisibility(View.VISIBLE);
		        holder.progressBar.setIndeterminate(false);   
		        holder.progressBar.setProgress(job.getProgress());
		    }
			holder.progressText.setText(FileUtil.formatFileSize(downloadSize)+"/" + FileUtil.formatFileSize(totalSize));
		} else{
		    holder.progressBar.setVisibility(View.GONE);
		    holder.progressText.setText("未知大小");
		}
		return row;
	}
	
	
	/**
	 * Class implementing holder pattern,
	 * performance boost
	 * 
	 * @author Lukasz Wisniewski
	 */
	static class ViewHolder {
		TextView title;
		TextView acid;
		TextView progressText;
		ProgressBar progressBar;
		
	}
	/**
	 * 下载项被选中监听者
	 *
	 */
	public interface OnItemCheckedListener {
	    void onCheckedChanged(CompoundButton cb , boolean isChecked);
	}
	public void setOnItemCheckedListener(OnItemCheckedListener l){
	    mListener = l;
	}
	public void unCheckedAll(){
	    checkedJobs.clear();
	    notifyDataSetChanged();
	}
	public int getCheckedCount(){
	    return checkedJobs.size();
	}
	public void checked(int position){
	    if(position>mItems.size()) return;
	    DownloadJob job =getItem(position);
	    if(!checkedJobs.remove(job))
	        checkedJobs.add(job);
	    notifyDataSetChanged();
	}
	public void checkedAll(){
	    checkedJobs.addAll(mItems);
	    notifyDataSetChanged();
	}
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Object o = buttonView.getTag();
        int position = 0;
        if(o!=null && o instanceof Integer){
            position = (Integer) o;
        }
        if(isChecked)
            checkedJobs.add(getItem(position));
        else
            checkedJobs.remove(getItem(position));
        if(mListener!= null)
            mListener.onCheckedChanged(buttonView, isChecked);
    }
    public List<DownloadJob> getCheckedJobs() {
        return checkedJobs;
    }
}