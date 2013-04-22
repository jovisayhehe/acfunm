package tv.avfun.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetWorkUtil {
    
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return (info != null) && (info.isConnected());
    }
    
    public static boolean isWifiConnected(Context context){
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
    }
    public static void showNetWorkError(Activity context) {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle("网络连接失败！").setMessage("请检查您的网络设置。").setCancelable(true)
        .setPositiveButton("知道了", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO 跳转到设置页面
            }
        }).show();
    }
}
