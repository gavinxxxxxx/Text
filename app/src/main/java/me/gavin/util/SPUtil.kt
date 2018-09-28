package me.gavin.util

import android.content.Context
import android.content.SharedPreferences

import me.gavin.base.App

/**
 * SharedPreferences 数据存储工具类
 *
 * @author gavin.xiong
 */
object SPUtil {

    private val sharedPreferences: SharedPreferences
        get() = App.app.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)

    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()

    private operator fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    private fun remove(key: String) {
        editor.remove(key).apply()
    }

    private fun removeIfPutNull(key: String, value: Any?): Boolean {
        if (value == null) {
            remove(key)
            return false
        }
        return true
    }

    fun getInt(key: String): Int? {
        return if (contains(key)) sharedPreferences.getInt(key, 0) else null
    }

    fun getInt(key: String, defVal: Int): Int {
        return sharedPreferences.getInt(key, defVal)
    }

    fun putInt(key: String, value: Int?) {
        if (removeIfPutNull(key, value)) {
            editor.putInt(key, value!!).apply()
        }
    }

    fun getLong(key: String): Long? {
        return if (contains(key)) sharedPreferences.getLong(key, 0L) else null
    }

    fun getLong(key: String, defVal: Long): Long {
        return sharedPreferences.getLong(key, defVal)
    }

    fun putLong(key: String, value: Long?) {
        if (removeIfPutNull(key, value)) {
            editor.putLong(key, value!!).apply()
        }
    }

    fun getFloat(key: String): Float? {
        return if (contains(key)) sharedPreferences.getFloat(key, 0f) else null
    }

    fun getFloat(key: String, defVal: Float): Float {
        return sharedPreferences.getFloat(key, defVal)
    }

    fun putFloat(key: String, value: Float?) {
        if (removeIfPutNull(key, value)) {
            editor.putFloat(key, value!!).apply()
        }
    }

    fun getBoolean(key: String): Boolean? {
        return if (contains(key)) sharedPreferences.getBoolean(key, false) else null
    }

    fun getBoolean(key: String, defVal: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defVal)
    }

    fun putBoolean(key: String, value: Boolean?) {
        if (removeIfPutNull(key, value)) {
            editor.putBoolean(key, value!!).apply()
        }
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun putString(key: String, value: String) {
        editor.putString(key, value).apply()
    }

}
