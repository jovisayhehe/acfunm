package tv.avfun.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.webkit.WebView;
/**
 * 
 * @author http://www.dewen.org/q/5473/%E6%80%8E%E4%B9%88%E5%88%A4%E6%96%ADandroid%E4%B8%ADWebView%E6%BB%91%E5%8A%A8%E5%88%B0%E4%BA%86%E4%BD%8E%E7%AB%AF
 *
 */
@SuppressLint("NewApi")
public class SWebView extends WebView{
	
	ScrollInterface mt;
	WContentHeight wh;
	private int currwebheight;
	private boolean isfrist = true;
	private boolean isfristvoer = true;
    public SWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
    }

    public SWebView(Context context) {
        super(context);
        
    }
    
    
    
	@Override
	protected void onDraw(Canvas canvas) {
		
		super.onDraw(canvas);
		if(isfrist){
			int currwebheight = getContentHeight();
			isfrist = false;
		}
		if(isfristvoer){
			
			int tempheight = getContentHeight();
			if(currwebheight!=0&tempheight>currwebheight){
				wh.onDrawOver(tempheight);
				isfristvoer = false;
			}
			
			currwebheight = tempheight;
		}

	}

	@Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        mt.onSChanged(l, t, oldl, oldt);
    }

    
    public void setOnCustomScroolChangeListener(ScrollInterface t){
        this.mt=t;
    }
    
    public void setFisrtOndrawOverListener(WContentHeight wh){
    	this.wh  = wh;
    }
    
    public interface ScrollInterface {
        public void onSChanged(int l, int t, int oldl, int oldt) ;
    }
    
    public interface WContentHeight {
    	 public void onDrawOver(int h) ;
    }
}
