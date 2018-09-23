package me.gavin.base

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

/**
 * ClientAPI
 *
 * @author gavin.xiong 2016/12/9
 */
interface ClientAPI {

    /**
     * 获取
     */
    @Headers(
            "Accept-Encoding: gzip, deflate",
            "User-Agent: Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.139 Safari/537.36"
    )
    @GET
    fun get(@Url url: String, @Header("Cache-Control") cacheControl: String): Observable<String>

    /**
     * 下载
     */
    @Streaming
    @GET
    fun download(@Url url: String): Observable<ResponseBody>
}
