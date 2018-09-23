package me.gavin.util.okhttp

import android.content.Context
import android.net.ConnectivityManager
import android.text.TextUtils
import me.gavin.base.App
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


/**
 * OkHttp3 缓存拦截器 使用[ClientAPIModule]
 * 配合 [retrofit2.http.Header]、[retrofit2.http.Headers]
 *
 * @author gavin.xiong 2017/4/28
 */
class OKHttpCacheInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        if (!isNetworkAvailable(App.app)) {
            // 网络不可用时强制使用缓存
            request = request.newBuilder()
                    .cacheControl(CacheControl.FORCE_CACHE)
                    .build()
        } else if (TextUtils.isEmpty(request.header("Cache-Control"))) {
            // 网络可用 && 未设置复用时间 -> 默认复用时间为 10s
            request = request.newBuilder()
                    .header("Cache-Control", "private, max-stale=10")
                    .build()
        }
        val response = chain.proceed(request)
        if (response.code() == 504) {
            throw IOException("网络连接不可用")
        }
        return response.newBuilder()
                .removeHeader("Pragma")
                .build()
    }

    /**
     * 判断网络是否有效
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return cm?.activeNetworkInfo?.isAvailable ?: false
    }
}
