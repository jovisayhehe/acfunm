package tv.avfun;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import tv.ac.fun.R;
import tv.avfun.app.AcApp;
import tv.avfun.entity.Comment;
import tv.avfun.util.TextViewUtils;
import tv.avfun.view.FloorsView;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class CommentsAdaper3 extends BaseAdapter {

	private LayoutInflater mInflater;
	private TreeMap<Integer, Comment> data;
	private List<Comment> comments = new ArrayList<Comment>();
	private Context mContext;
	private int maxNumOfFloor = 25;

	public CommentsAdaper3(Context context, TreeMap<Integer, Comment> data) {
		this.mInflater = LayoutInflater.from(context);
		this.mContext = context;
		this.data = data;
		for (Map.Entry<Integer, Comment> e : data.entrySet()) {
			comments.add(e.getValue());
		}
	}

	public void setData(TreeMap<Integer, Comment> data) {
		this.data = data;
		comments.clear();
		for (Map.Entry<Integer, Comment> e : data.entrySet()) {
			comments.add(e.getValue());
		}
	}

	@Override
	public int getCount() {

		return data.size();
	}

	@Override
	public Comment getItem(int position) {
		return comments.get(position);
	}

	@Override
	public long getItemId(int position) {

		return position;
	}

	private int frameId = R.id.floor;

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
			convertView.setTag(holder);
		} else {
			holder = (CommentViewHolder) convertView.getTag();
			if (holder.hasQuote && holder.quoteFrame != null) {
				holder.quoteFrame.removeAllViews();
			}
			convertView.findViewById(R.id.requote).setVisibility(View.GONE);
		}
		holder.user.setText("#" + c.count + " " + c.userName);

		TextViewUtils.setCommentContent(holder.content, c);
		int quoteId = c.quoteId;
		holder.hasQuote = quoteId > 0;
		List<View> quoteList = new ArrayList<View>();
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
		return convertView;
	}

	private RelativeLayout generateQuoteFrame(Comment quote) {
		RelativeLayout quoteFrame = (RelativeLayout) mInflater.inflate(
				R.layout.comments_listitem, null);
		TextView username = (TextView) quoteFrame.findViewById(R.id.user_name);
		username.setText("#" + quote.count + " " + quote.userName);
		TextView content = (TextView) quoteFrame
				.findViewById(R.id.comments_content);
		TextViewUtils.setCommentContent(content, quote);
		return quoteFrame;
	}

	static class CommentViewHolder {
		TextView user;
		TextView content;
		boolean hasQuote;
		FloorsView quoteFrame;

	}
}
