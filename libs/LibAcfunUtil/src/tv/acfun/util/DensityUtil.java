package tv.acfun.util;

import android.content.Context;

public class DensityUtil {
    private static float density = 1;

    public static int dip2px(Context context, float dipValue) {
        if(density == 1f){
            density  = context.getResources().getDisplayMetrics().density;
        }
        return (int) (dipValue * density + 0.5f);
    }

    public static int px2dip(Context context, float pxValue) {
        if(density == 1f){
            density  = context.getResources().getDisplayMetrics().density;
        }
        return (int) (pxValue / density + 0.5f);
    }

}
