package utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


/**
 * Author: snail
 * Data: 18-1-19 下午7:20
 * Des:
 * version:
 */

public class NetUtil  {


    public static boolean isNetworkOpened(Context context) {
        try {
            ConnectivityManager th = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = th.getActiveNetworkInfo();
            if(info != null && info.isAvailable() && info.isConnected()) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable var2) {
            return false;
        }
    }

}
