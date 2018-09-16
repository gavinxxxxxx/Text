package me.gavin.util

import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import me.gavin.base.App
import java.io.FileWriter
import java.io.PrintWriter
import java.util.*

/**
 * 全局异常捕获工具
 *
 * @author gavin.xiong 2018/9/16.
 */
object CrashHandler : Thread.UncaughtExceptionHandler {

    fun init() {
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        printException2SDCard(e)
    }

    private fun printException2SDCard(throwable: Throwable) {
        // 获取日志文件夹 - /storage/sdcard0/Android/data/com.gavin.xxx/files/crash
        if (Environment.MEDIA_MOUNTED != Environment.getExternalStorageState()) return
        val parent = App.app.getExternalFilesDir("crash") ?: return

        val date = Date()
        val format = "%1\$s/CRASH_%2\$tY.%2\$tm.%2\$td_%2\$tH.%2\$tM.%2\$tS.trace"
        val path = format.format(Locale.getDefault(), parent, date)
        try {
            PrintWriter(FileWriter(path)).use {
                val pm = App.app.packageManager
                val info = pm.getPackageInfo(App.app.packageName, PackageManager.GET_ACTIVITIES)
                it.println(String.format(Locale.getDefault(), "异常时间：%1\$tF %1\$tT", date))
                it.println("应用版本：" + info.versionName)
                it.println("应用版本号：" + info.versionCode)
                it.println("Android版本：" + Build.VERSION.RELEASE)
                it.println("Android版本号：" + Build.VERSION.SDK_INT)
                it.println("手机制造商：" + Build.MANUFACTURER)
                it.println("手机型号：" + Build.MODEL)
                throwable.printStackTrace(it)
                var cause = throwable.cause
                while (cause != null) {
                    cause.printStackTrace(it)
                    cause = cause.cause
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

}