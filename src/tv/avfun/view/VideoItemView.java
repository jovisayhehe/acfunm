package tv.avfun.view;

import tv.ac.fun.R;
import tv.avfun.entity.Contents;
import tv.avfun.util.lzlist.ImageLoader;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
/**
 * 首页视频各个条目的自定义组合view类
 * @author Yrom
 *
 */
public class VideoItemView extends LinearLayout implements View.OnClickListener{

    @SuppressWarnings("unused")
    private static final String TAG = VideoItemView.class.getSimpleName();
    private TextView title, views, comments;
    private ImageView preview;
    private ImageLoader imageLoader;
    private Contents contents;
    private View clickView;
    private OnClickListener listener;
    /**
     * Just for test. 
     * @param context
     * @param attrs
     * @hide
     */
    public VideoItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.VideoItemView);
        int preImageId = a.getResourceId(R.styleable.VideoItemView_item_preview_image,
                R.drawable.no_picture);
        String text = a.getString(R.styleable.VideoItemView_item_title);
        int views = a.getInt(R.styleable.VideoItemView_item_views, 0);
        int comments = a.getInt(R.styleable.VideoItemView_item_comments, 0);
        
        a.recycle();
        this.preview.setImageResource(preImageId);
        this.title.setText(text);
        this.views.setText(views+"");
        this.comments.setText(comments+"");
    }
    /**
     * call {@link #setContents(Contents)}.
     * @param context
     */
    public VideoItemView(Context context) {
        super(context);
        initView(context);
    }

    private void initView(Context context) {
        imageLoader = ImageLoader.getInstance();
        View view = LayoutInflater.from(context).inflate(R.layout.video_item, this);
        preview = ((ImageView) view.findViewById(R.id.item_video_preview_image));
        title = ((TextView) view.findViewById(R.id.item_video_title));
        views = ((TextView) view.findViewById(R.id.item_video_views));
        comments = ((TextView) view.findViewById(R.id.item_video_comments));
        clickView = view.findViewById(R.id.item_video_clickable);
        clickView.setOnClickListener(this);
    }
    public Contents getContents(){
        return contents;
    }
    public void setContents(Contents contents){
        this.contents = contents;
        String imageUrl = contents.getTitleImg();
        int comments = contents.getComments();
        long views = contents.getViews();
        this.title.setText(contents.getTitle());
        this.comments.setText(comments+"");
        this.views.setText(views+"");
        if(!TextUtils.isEmpty(imageUrl))
            imageLoader.displayImage(imageUrl, preview);
        else preview.setImageResource(R.drawable.no_picture);
        //requestLayout();
        }
    
    @Override
    public void onClick(View v) {
        if(listener != null && this.contents != null){
            listener.onClick(this, this.contents);
        }
        
        
    }
    @Override
    public void setClickable(boolean clickable) {
        this.clickView.setClickable(clickable);
        this.clickView.setBackgroundResource(clickable?R.drawable.clickable_item_bg : R.color.transparent);
    }
    /**
     * custom click listener
     * @author Yrom
     *
     */
    public interface OnClickListener{
        /**
         * callback when this view is clicked.
         * @param view this view
         * @param contents the contents
         */
        void onClick(View view, Contents contents);
    }
    /**
     * register callback when this view is clicked.
     */
    public void setOnClickListener(OnClickListener l) {
        setClickable(true);
        this.listener = l;
    }
}
