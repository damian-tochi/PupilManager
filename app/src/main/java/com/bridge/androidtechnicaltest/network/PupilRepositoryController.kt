package com.bridge.androidtechnicaltest.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit


class CacheIntercept : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(2, TimeUnit.DAYS)
            .build()
        return response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
}

fun hasNetwork(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val mNetwork      = connectivityManager.activeNetwork ?: return false
    val actNetwork  = connectivityManager.getNetworkCapabilities(mNetwork) ?: return false
    return when {
        actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
        else -> false
    }
}
