package io.agora.metachat.tools

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import java.util.*

/**
 * @author create by zhangwei03
 *
 */
object ResourcesTools {

    private var isZh = false

    @JvmStatic
    fun getIsZh(): Boolean = isZh

    @JvmStatic
    @ColorInt
    fun getColor(resources: Resources, @ColorRes id: Int, theme: Resources.Theme? = null): Int {
        return ResourcesCompat.getColor(resources, id, theme)
    }

    @JvmStatic
    fun getDrawableId(context: Context, name: String): Int {
        return context.resources.getIdentifier(name, "drawable", context.packageName)
    }

    @JvmStatic
    fun isZh(context: Context): Boolean {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales.get(0)
        } else {
            context.resources.configuration.locale
        }
        isZh = locale.country == "CN"
        return isZh
    }
}