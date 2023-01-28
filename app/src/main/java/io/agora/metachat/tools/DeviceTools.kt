package io.agora.metachat.tools

import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.util.Size
import android.util.TypedValue
import androidx.annotation.RequiresApi

/**
 * @author create by zhangwei03
 *
 */
object DeviceTools {

    @JvmStatic
    fun getDisplaySize(): Size {
        val metrics = Resources.getSystem().displayMetrics
        return Size(metrics.widthPixels, metrics.heightPixels)
    }

    @JvmStatic
    fun dp2px(dp: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            Resources.getSystem().displayMetrics
        )
    }

    @JvmStatic
    fun sp2px(sp: Int): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), Resources.getSystem().displayMetrics)
    }

    @JvmStatic
    fun isMainProcess(context: Context): Boolean {
        val processName: String? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessNameByApplication()
        } else {
            getProcessNameByReflection()
        }
        return context.applicationInfo.packageName == processName
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private fun getProcessNameByApplication(): String? {
        return Application.getProcessName()
    }

    private fun getProcessNameByReflection(): String? {
        var processName: String? = null
        try {
            val declaredMethod = Class.forName(
                "android.app.ActivityThread", false,
                Application::class.java.classLoader
            )
                .getDeclaredMethod("currentProcessName", *arrayOfNulls<Class<*>?>(0))
            declaredMethod.isAccessible = true
            val invoke = declaredMethod.invoke(null, *arrayOfNulls(0))
            if (invoke is String) {
                processName = invoke
            }
        } catch (e: Throwable) {
        }
        return processName
    }
}

