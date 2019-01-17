package me.gavin.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.view.ViewConfiguration
import kotlin.math.roundToInt


fun Float.dp2px() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

fun Float.px2dp() = (this / Resources.getSystem().displayMetrics.density).roundToInt()

fun Float.sp2px() = (this * Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

fun Float.px2sp() = (this / Resources.getSystem().displayMetrics.scaledDensity).roundToInt()

fun getScreenWidth() = Resources.getSystem().displayMetrics.widthPixels

fun getScreenHeight() = Resources.getSystem().displayMetrics.heightPixels

@SuppressLint("PrivateApi")
fun getStatusHeight() = try {
    val cla = Class.forName("com.android.internal.R\$dimen")
    val obj = cla.newInstance()
    val field = cla.getField("status_bar_height")
    val x = field.get(obj) as Int
    Resources.getSystem().getDimensionPixelSize(x)
} catch (e: Exception) {
    e.printStackTrace()
    0
}

fun Activity.getSoftKeyboardHeight(): Int {
    val rect = Rect() //获取当前界面可视部分
    window.decorView.getWindowVisibleDisplayFrame(rect)
    return getScreenHeight() - rect.bottom - getNavigationBarHeight()
}

fun Context.getNavigationBarHeight(): Int {
    var result = 0
    if (hasNavigationBar(this)) {
        val res = resources
        val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = res.getDimensionPixelSize(resourceId)
        }
    }
    return result
}

private fun hasNavigationBar(context: Context): Boolean {
    val res = context.resources
    val resourceId = res.getIdentifier("config_showNavigationBar", "bool", "android")
    return if (resourceId != 0) {
        var hasNav = res.getBoolean(resourceId)
        val sNavBarOverride = getNavigationBarOverride()
        if ("1" == sNavBarOverride) {
            hasNav = false
        } else if ("0" == sNavBarOverride) {
            hasNav = true
        }
        hasNav
    } else { // fallback
        !ViewConfiguration.get(context).hasPermanentMenuKey()
    }
}

/**
 * 判断虚拟按键栏是否重写
 *
 * @return
 */
@SuppressLint("PrivateApi")
private fun getNavigationBarOverride(): String? = try {
    val c = Class.forName("android.os.SystemProperties")
    val m = c.getDeclaredMethod("get", String::class.java)
    m.isAccessible = true
    m.invoke(null, "qemu.hw.mainkeys") as String
} catch (e: Throwable) {
    null
}
