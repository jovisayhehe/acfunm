package tv.avfun;

import java.util.ArrayList;
import java.util.List;

import tv.ac.fun.R;
import tv.avfun.app.AcApp;
import tv.avfun.entity.Comment;
import tv.avfun.util.TextViewUtils;
import tv.avfun.view.FloorsView;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class CommentsAdaper3 extends BaseAdapter {

	private LayoutInflater mInflater;
	private SparseArray<Comment> data;
	private List<Integer> commentIdList;
	private Context mContext;
	private int maxNumOfFloor;

	public CommentsAdaper3(Context context, SparseArray<Comment> data, List<Integer> commentIdList) {
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.data = data;
		this.commentIdList = commentIdList;
		maxNumOfFloor = AcApp.getConfig().getInt("num_of_floor", 40);
		if(maxNumOfFloor == 0)
		    maxNumOfFloor = 10;
	}

	public void setData(SparseArray<Comment> data, List<Integer> commentIdList) {
		this.data = data;
		this.commentIdList = commentIdList;
	}

	@Override
	public int getCount() {

		return commentIdList.size();
	}

	@Override
	public Comment getItem(int position) {
		try {
            Integer id = commentIdList.get(position);
            if(id != null)
                return data.get(id);
        } catch (IndexOutOfBoundsException e) {
        }
		return null;
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	private int frameId = R.id.floor;
    private View.OnClickListener mListener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
           
            if(mOnClickListener != null){
                int position = (Integer) v.getTag();
                mOnClickListener.onClick(v, position);
            }
        }
        
    };

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Comment c = getItem(position);

		CommentViewHolder holder = null;
		if (convertView == null) {
			holder = new CommentViewHolder();
			convertView = mInflater.inflate(R.layout.comments_listitem, null);
			holder.user = (TextView) convertView.findViewById(R.id.user_name);
			holder.content = (TextView) convertView
					.findViewById(R.id.comments_content);
			holder.quoteImage = convertView.findViewById(R.id.quote_img);
			convertView.setTag(holder);
		} else {
			holder = (CommentViewHolder) convertView.getTag();
			if (holder.hasQuote && holder.quoteFrame != null) {
				holder.quoteFrame.removeAllViews();
			}
			convertView.findViewById(R.id.requote).setVisibility(View.GONE);
		}
		holder.user.setText("#" + c.count + " " + c.userName);
		holder.quoteImage.setTag(position);
		holder.quoteImage.setOnClickListener(mListener);
		TextViewUtils.setCommentContent(holder.content, c);
		int quoteId = c.quoteId;
		holder.hasQuote = quoteId > 0;
		List<View> quoteList = new ArrayList<View>();
		handleQuoteList(position, convertView, holder, quoteId, quoteList);
		holder.quoteFrame.setQuoteList(quoteList);
		if(holder.quoteFrame.getChildCount()>0){
			RelativeLayout.LayoutParams floorsLayoutParams = new LayoutParams(
					-1, -2);
			floorsLayoutParams.setMargins(4, 4, 4, 4);
			((ViewGroup) convertView).addView(holder.quoteFrame,
					floorsLayoutParams);
		}
		RelativeLayout.LayoutParams userLayoutParams = (LayoutParams) holder.user
				.getLayoutParams();
		userLayoutParams.addRule(RelativeLayout.BELOW,holder.quoteFrame.getChildCount()>0?frameId:R.id.requote);
		holder.user.setLayoutParams(userLayoutParams);
//		convertView.setOnClickListener(mListener );
//		convertView.setBackgroundResource(R.drawable.clickable_item_bg);
		return convertView;
	}

    private void handleQuoteList(int position, View convertView,
            CommentViewHolder holder, int quoteId, List<View> quoteList) {
        if (holder.hasQuote || holder.quoteFrame == null) {
			FloorsView floors = new FloorsView(mContext);
			floors.setId(frameId);
			holder.quoteFrame = floors;
		}
		
		int num = 0;
		for (Comment quote = data.get(quoteId); 
				quote != null && num< maxNumOfFloor; 
				num++, quoteId = quote.quoteId, quote = data.get(quoteId)) {

			if (quote.isQuoted) {
				if (quote.beQuotedPosition == position) {
					quoteList.add(generateQuoteFrame(quote));
				} else {
					convertView.findViewById(R.id.requote).setVisibility(
							View.VISIBLE);
				}
			} else {
				quote.isQuoted = true;
				quote.beQuotedPosition = position;
				quoteList.add(generateQuoteFrame(quote));
			}
		}
    }

	private RelativeLayout generateQuoteFrame(Comment quote) {
		RelativeLayout quoteFrame = (RelativeLayout) mInflater.inflate(
				R.layout.comments_quote_item, null);
		TextView username = (TextView) quoteFrame.findViewById(R.id.user_name);
		username.setText("#" + quote.count + " " + quote.userName);
		TextView content = (TextView) quoteFrame
				.findViewById(R.id.comments_content);
		TextViewUtils.setCommentContent(content, quote);
		
		return quoteFrame;
	}
	private OnQuoteClickListener mOnClickListener;
	public void setOnClickListener(OnQuoteClickListener l){
	    mOnClickListener = l;
	}
	
	public interface OnQuoteClickListener{
	    void onClick(View v,int position);
	}
	static class CommentViewHolder {
		TextView user;
		TextView content;
		View quoteImage;
		boolean hasQuote;
		FloorsView quoteFrame;

	}
}
