package tv.avfun.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import tv.avfun.R;
import tv.avfun.util.ArrayUtil;
import tv.avfun.util.FileUtil;
import tv.avfun.util.download.DownloadEntry;
import tv.avfun.util.download.DownloadJob;
import android.app.Activity;
import android.view.LayoutInflater;
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
public class DownloadJobAdapter extends ArrayListAdapter<DownloadJob> implements OnCheckedChangeListener {
    private List<DownloadJob> checkedJobs;
    private OnItemCheckedListener mListener;
	public DownloadJobAdapter(Activity activity) {
		this(activity, null);
	}
	public DownloadJobAdapter(Activity activity, ArrayList<DownloadJob> list){
	    super(activity);
	    mList = list;
	    checkedJobs = Collections.synchronizedList(new LinkedList<DownloadJob>());
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row=convertView;
		DownloadJob job = mList.get(position);
		ViewHolder holder;

		if (row==null) {
			LayoutInflater inflater = mContext.getLayoutInflater();
			row=inflater.inflate(R.layout.download_man_list_item, parent,false);

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
		
		if(job.getProgress() == 100){
			holder.progressBar.setVisibility(View.GONE);
			holder.progressText.setText(FileUtil.formetFileSize(job.getTotalSize())+"/已完成");
		} else {
			holder.progressBar.setVisibility(View.VISIBLE);
			holder.progressBar.setMax(100);
			holder.progressBar.setProgress(mList.get(position).getProgress());
			holder.progressText.setText(mList.get(position).getProgress()+"% - " + FileUtil.formetFileSize(job.getTotalSize()));
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
	public void unSelectAll(){
	    checkedJobs.clear();
	    notifyDataSetChanged();
	}
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        Object o = buttonView.getTag();
        int position = 0;
        if(o!=null && o instanceof Integer){
            position = (Integer) o;
        }
        if(isChecked)
            checkedJobs.add(mList.get(position));
        else
            checkedJobs.remove(mList.get(position));
        if(mListener!= null)
            mListener.onCheckedChanged(buttonView, isChecked);
    }
}