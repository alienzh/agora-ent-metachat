package io.agora.metachat.tools

import android.content.Context
import android.content.res.Resources
import android.util.Size
import android.util.TypedValue

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
}

