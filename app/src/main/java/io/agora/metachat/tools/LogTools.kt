package io.agora.metachat.tools

import android.util.Log
import androidx.annotation.StringRes
import io.agora.metachat.MChatApp

/**
 * @author create by zhangwei03
 *
 */
object LogTools {

    private const val TAG = "MChat-Android"

    @JvmStatic
    fun d(message: String) {
        Log.d(TAG, message)
    }

    @JvmStatic
    fun d(@StringRes stringRes: Int) {
        Log.d(TAG, MChatApp.instance().getString(stringRes))
    }

    @JvmStatic
    fun e(message: String) {
        Log.e(TAG, message)
    }

    @JvmStatic
    fun e(@StringRes stringRes: Int) {
        Log.e(TAG, MChatApp.instance().getString(stringRes))
    }

    @JvmStatic
    fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    @JvmStatic
    fun d(tag: String, @StringRes stringRes: Int) {
        Log.d(tag, MChatApp.instance().getString(stringRes))
    }

    @JvmStatic
    fun e(tag: String, message: String) {
        Log.e(tag, message)
    }

    @JvmStatic
    fun e(tag: String, @StringRes stringRes: Int) {
        Log.e(tag, MChatApp.instance().getString(stringRes))
    }
}