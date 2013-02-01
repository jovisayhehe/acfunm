package tv.avfun.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.ImageView;

public class BorderImageViewL extends ImageView {
	private String namespace="http://yoooo.org";
    private int color;
    private float roundPx =3;
	
    public BorderImageViewL(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		color=Color.parseColor(attrs.getAttributeValue(namespace, "BorderColor"));
	}

	@Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub    
        
        super.onDraw(canvas);
        Rect rec=canvas.getClipBounds();
        RectF rectF = new RectF(rec); 
        Paint paint=new Paint();
        paint.setColor(color);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(9);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        
        Rect rec1=canvas.getClipBounds();
        RectF rectF1 = new RectF(rec1); 
        Paint paint1=new Paint();
        paint1.setColor(Color.parseColor("#C7D0D2"));
        paint1.setAntiAlias(true);
        paint1.setStrokeWidth(2);
        paint1.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rectF1, 2, 2, paint1);
    }
}
