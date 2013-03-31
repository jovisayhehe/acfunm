package tv.avfun.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BorderImageView extends ImageView {
	private String namespace="http://yoooo.org";
    private int color;
    private float roundPx =3;
	
    public BorderImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		color=Color.parseColor(attrs.getAttributeValue(namespace, "BorderColor"));
	}

	@Override
    protected void onDraw(Canvas canvas) {
            
        
        super.onDraw(canvas);
        Rect rec=canvas.getClipBounds();
//        rec.bottom -= 1;
//        rec.right -= 1;
        RectF rectF = new RectF(rec); 
        Paint paint=new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(9);
        paint.setStyle(Paint.Style.STROKE);
       // canvas.drawRect(rec, paint);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
    }
}
