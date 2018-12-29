package me.gavin.base

import android.app.Activity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.gavin.app.entity.Book
import me.gavin.app.text.Flipper
import me.gavin.util.CacheHelper
import me.gavin.util.okhttp.OKHttpCacheInterceptor
import me.gavin.util.okhttp.OKHttpCacheNetworkInterceptor
import me.gavin.util.okhttp.OKHttpParseInterceptor
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit


private const val BASE_URL = "http://news-at.zhihu.com/api/4/"

object Provider {

    private val clientApi: ClientAPI by lazy {
        Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build()
                .create(ClientAPI::class.java)
    }

    val gson: Gson by lazy {
        GsonBuilder()
                // .excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
                // .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
                // .setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")//时间转化为特定格式
                // .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)//会把字段首字母大写,注:对于实体上使用了@SerializedName注解的不会生效.
                // .setPrettyPrinting() //对json结果格式化.
                // .setVersion(1.0)
                // .disableHtmlEscaping()//默认是GSON把HTML 转义的，但也可以设置不转义
                // .serializeNulls()//把null值也转换，默认是不转换null值的，可以选择也转换,为空时输出为{a:null}，而不是{}
                .create()
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
//                .addInterceptor(logging2)
                .addInterceptor(OKHttpCacheInterceptor())
                .addInterceptor(OKHttpParseInterceptor())
                .addNetworkInterceptor(OKHttpCacheNetworkInterceptor())
                .cache(Cache(File(CacheHelper.getCacheDir(App.app), "responses"), 1024 * 1024 * 200))
                .build()
    }

    fun Any.toJson(): String = gson.toJson(this)

    val DataLayer.api
        get() = clientApi

    val Activity.api
        get() = clientApi

    val Book.api
        get() = clientApi

    val Flipper.api
        get() = clientApi

}

inline fun <reified T> String.fromJson() =
        try {
            Provider.gson.fromJson<T>(this, object : TypeToken<T>() {}.type)
        } catch (e: Exception) {
            null
        }
