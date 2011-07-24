package tv.acfun.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Window;
import android.view.WindowManager;

public class Util {
	
	public static boolean isNetWorkActive(Context context){
        ConnectivityManager cManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE); 
        NetworkInfo info = cManager.getActiveNetworkInfo(); 
          if (info == null || !info.isAvailable()){ 
        	 return false;
          }else{
        	  return true;
          }
	}
	
	
	public static void fullScreen(Activity act){
		
        final Window win = act.getWindow();
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
     		   WindowManager.LayoutParams.FLAG_FULLSCREEN);
        act.requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
	
	
	public static void backScreen(Activity act){
		
		act.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
}
