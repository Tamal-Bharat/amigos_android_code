package com.amigos.attendance.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

public class NetworkUtils {

    public static boolean isInternetAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(connectivityManager == null){
            return false;
        }

        Network network = connectivityManager.getActiveNetwork();

        if(network == null){
            return false;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        return capabilities != null &&
                capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_VALIDATED);
    }

}
