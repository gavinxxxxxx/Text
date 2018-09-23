package me.gavin.util.okhttp

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException


/**
 * OkHttp3 缓存网络拦截器 使用[ClientAPIModule]
 * 配合 [retrofit2.http.Header]、[retrofit2.http.Headers]
 * 当服务器返回 Cache-Control: must-revalidate 时使用（慎用）
 *
 * @author gavin.xiong 2017/4/28
 */
class OKHttpCacheNetworkInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
                .newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .build()
    }
}
