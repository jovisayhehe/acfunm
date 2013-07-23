package tv.avfun.view;

import java.util.List;

import tv.ac.fun.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class FloorsView extends LinearLayout {
	private Drawable mBorder;
	private LayoutInflater mInflater;

	public FloorsView(Context context) {
		this(context, null);
	}

	public FloorsView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	private void initView(Context context) {
		setOrientation(VERTICAL);
	}

	public void setQuoteList(List<View> quoteList) {
		if(quoteList == null || quoteList.isEmpty()) {
			removeAllViewsInLayout();
			return;
		}
		int j = 0;
		for(int i=quoteList.size()-1;i>=0;i--){
			LinearLayout.LayoutParams params = generateDefaultLayoutParams();
			int k = 6 * i;
			params.leftMargin = k;
			params.rightMargin = k;
			params.topMargin = j==0?k:0;
			addViewInLayout(quoteList.get(i), j++, params);
		}
	}

	public void setFloorBorder(Drawable border) {
		this.mBorder = border;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		int i = getChildCount();
		if(this.mBorder == null){
			this.mBorder = getContext().getResources().getDrawable(R.drawable.comment_floor_bg);
		}
		if ((this.mBorder != null) && (i > 0))
			for (int j = i - 1; j >= 0; j--) {
				View child = getChildAt(j);
				this.mBorder.setBounds(child.getLeft(), child.getLeft(),
						child.getRight(), child.getBottom());
				this.mBorder.draw(canvas);
			}
		super.dispatchDraw(canvas);
	}

}
