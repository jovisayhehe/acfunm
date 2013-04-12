package tv.avfun.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetWorkUtil {
    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        return (info != null) && (info.isConnected());
    }
}
