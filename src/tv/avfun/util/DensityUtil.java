package tv.avfun.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class DensityUtil {
	public static int dip2px(Context context, float dipValue){ 
	                final float scale = context.getResources().getDisplayMetrics().density; 
	                return (int)(dipValue * scale + 0.5f); 
	        } 
	        
	    public static int px2dip(Context context, float pxValue){ 
	                final float scale = context.getResources().getDisplayMetrics().density; 
	                return (int)(pxValue / scale + 0.5f); 
	        }
	    

}

