package tv.avfun.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class MLinearLayout extends LinearLayout{

	public MLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub    
        
        super.onDraw(canvas);
        Rect rec=canvas.getClipBounds();
        RectF rectF = new RectF(rec); 
        Paint paint=new Paint();
        paint.setColor(Color.parseColor("#C7D0D2"));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(rectF, 2, 2, paint);
    }

}
