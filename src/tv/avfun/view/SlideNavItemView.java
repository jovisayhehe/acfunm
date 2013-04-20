package tv.avfun.view;

import tv.avfun.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 侧边导航条目。自定义组合控件
 * 
 * @author Yrom
 * 
 */
public class SlideNavItemView extends RelativeLayout {
    private String mText;
    private int mIconId;
    private View view,mHint;
    
    public SlideNavItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SlideNavItemView);
        mText = ta.getString(R.styleable.SlideNavItemView_item_text);
        mIconId = ta.getResourceId(R.styleable.SlideNavItemView_item_icon, 0);
        
        initView(context);
        ta.recycle();

    }

    public SlideNavItemView(Context context,  String text, int iconResId) {
        super(context);
        this.mText = text;
        this.mIconId = iconResId;
        initView(context);
    }
    
    private void initView(Context context) {
        setClickable(true);
        setGravity(Gravity.CENTER_VERTICAL);
        view = LayoutInflater.from(context).inflate(R.layout.slide_nav_list_item, this);
        if (mText == null)
            throw new IllegalArgumentException("item_text没有设置");
        if (mIconId == 0)
            throw new IllegalArgumentException("item_icon没有设置！");

        ((TextView) view.findViewById(R.id.nav_item_text)).setText(mText);
        ((ImageView) view.findViewById(R.id.nav_item_icon)).setImageResource(mIconId);
        mHint = view.findViewById(R.id.nav_item_hint);
    }

    /**
     * 是否启用提示。在条目左边来个选中状态颜色
     * @param enabled
     */
    public void setHintEnabled(boolean enabled) {

        mHint.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }
}
