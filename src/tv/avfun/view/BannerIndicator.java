package tv.avfun.view;

import tv.ac.fun.R;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

public class BannerIndicator extends LinearLayout {

    private int     curIndicatorIndex = -1;
    private Context context;
    private int     indicatorNum;
    private View[]  indicators;

    public BannerIndicator(Context context, AttributeSet attrs, int defStyle) {
        this(context, attrs);
    }

    public BannerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BannerIndicator(Context context) {
        this(context, null);
    }

    private void init() {
        if (this.indicatorNum > 1) {
            if (this.indicators != null) {
                removeAllViews();
                this.indicators = null;
                this.curIndicatorIndex = -1;
            }
            this.context = getContext();
            Resources res = getResources();
            int width = res.getDimensionPixelSize(R.dimen.banner_indicator_view_width);
            int height = res.getDimensionPixelSize(R.dimen.banner_indicator_view_height);
            int margin = res.getDimensionPixelSize(R.dimen.banner_indicator_view_margin_horizontal);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
            params.setMargins(margin, 0, margin, 0);
            this.indicators = new View[this.indicatorNum];
            for (int m = 0; m < this.indicatorNum; m++) {
                View view = new View(this.context);
                view.setLayoutParams(params);
                view.setBackgroundResource(R.color.banner_indicator_normal);
                addView(view);
                this.indicators[m] = view;
            }
            setCurIndicator(0);
        }
    }

    public void setCurIndicator(int index) {
        if ((this.indicators != null) && (index >= 0) && (index < this.indicatorNum)
                && (this.curIndicatorIndex != index)) {
            if ((this.curIndicatorIndex >= 0) && (this.curIndicatorIndex < this.indicatorNum))
                this.indicators[this.curIndicatorIndex]
                        .setBackgroundResource(R.color.banner_indicator_normal);
            this.indicators[index].setBackgroundResource(R.color.banner_indicator_highlight);
            this.curIndicatorIndex = index;
            return;
        }
    }

    public void setIndicatorNum(int num) {
        this.indicatorNum = num;
        init();
    }
}
