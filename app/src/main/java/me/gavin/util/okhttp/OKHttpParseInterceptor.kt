package me.gavin.util.okhttp

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Response
import okhttp3.ResponseBody
import okio.GzipSource
import okio.Okio
import org.mozilla.universalchardet.UniversalDetector
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream

/**
 * OkHttp 拦截器 - 解码
 *
 * @author gavin.xiong 2017/5/2
 */
class OKHttpParseInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response? {
        var response: Response = chain.proceed(chain.request())
        response.body()?.run {
            var bytes = this.bytes()

            if (bytes.size >= 2 && inGZIPFormat(bytes)) { // 反GZIP
                val gzipSource = GzipSource(Okio.source(ByteArrayInputStream(bytes)))
                bytes = Okio.buffer(gzipSource).readByteArray()
            }

            val encoding = getCharsetByJUniversalCharDet(ByteArrayInputStream(bytes)) ?: "UTF-8"
            if (!"UTF-8".equals(encoding, ignoreCase = true)) { // 转码成 utf-8
                val source = Okio.source(ByteArrayInputStream(bytes))
                bytes = Okio.buffer(source).readString(Charset.forName(encoding)).toByteArray()
            }

            response = response.newBuilder()
                    .body(ResponseBody.create(MediaType.parse("text/html"), bytes))
                    .removeHeader("Content-Encoding")
                    .build()

        }
        return response
    }

    private fun inGZIPFormat(bytes: ByteArray): Boolean {
        val header = bytes[1].toInt() and 0xff shl 8 or (bytes[0].toInt() and 0xff)
        return header == GZIPInputStream.GZIP_MAGIC
    }

    /**
     * 获取文本文件编码 - juniversalchardet
     *
     * @link {https://code.google.com/archive/p/juniversalchardet/}
     */
    @Throws(IOException::class)
    private fun getCharsetByJUniversalCharDet(inputStream: InputStream): String? {
        try {
            inputStream.use {
                val buffer = ByteArray(4096)
                val detector = UniversalDetector(null)
                var length: Int = -1
                while ({ length = it.read(buffer);length }() > 0 && !detector.isDone) {
                    detector.handleData(buffer, 0, length)
                }
                detector.dataEnd()
                val encoding = detector.detectedCharset
                detector.reset()
                return encoding
            }
        } catch (e: Exception) {
            return null
        }
    }
}
