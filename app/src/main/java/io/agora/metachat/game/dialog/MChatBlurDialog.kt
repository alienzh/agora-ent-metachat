package io.agora.metachat.game.dialog

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import io.agora.metachat.baseui.BaseFragmentDialog

/**
 * @author create by zhangwei03
 */
abstract class MChatBlurDialog<B : ViewBinding> : BaseFragmentDialog<B>() {

    private var mBlurType: Int = 0

    /**
     * 高斯模糊的类型
     * 0代表只模糊背景
     * 1代表之模糊后方屏幕
     * 2代表同时模糊背景和后方屏幕
     */
    companion object {
        const val BLUR_TYPE_BLUR_BACKGROUND = 0;
        const val BLUR_TYPE_BLUR_BEHIND = 1
        const val BLUR_TYPE_BLUR_BACKGROUND_AND_BEHIND = 2

        //窗口背景高斯模糊程度，数值越高越模糊且越消耗性能
        private const val mBackgroundBlurRadius = 90

        //窗口周边背景高斯模糊程度
        private const val mBlurBehindRadius = 20

        //根据窗口高斯模糊功能是否开启来设置窗口周边暗色的程度
        private const val mDimAmountWithBlur = 0f
        private const val mDimAmountNoBlur = 0.4f

        // 根据窗口高斯模糊功能是否开启来为窗口设置不同的不透明度
        private const val mWindowBackgroundAlphaWithBlur = 170
        private const val mWindowBackgroundAlphaNoBlur = 255
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // android 12
            initBlur()
        }
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initBlur() {
        //允许背景模糊，也可以通过样式属性R.attr#windowBlurBehindEnabled来实现
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        // 允许背景变暗，也可以通过样式属性R.attr#backgroundDimEnabled来实现
        dialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        val blurConsumer: (Boolean) -> Unit = this::updateWindowForBlurs
        dialog?.window?.decorView?.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {

            override fun onViewAttachedToWindow(v: View) {
                dialog?.window?.windowManager?.addCrossWindowBlurEnabledListener(blurConsumer)
            }

            override fun onViewDetachedFromWindow(v: View) {
                dialog?.window?.windowManager?.removeCrossWindowBlurEnabledListener(blurConsumer)
            }
        });
    }

    private fun updateWindowForBlurs(blursEnabled: Boolean) {
        when(mBlurType){
            BLUR_TYPE_BLUR_BACKGROUND ->{  //仅模糊背景
                dialog?.window?.let {
                    it.setDimAmount(if (blursEnabled) mDimAmountWithBlur else mDimAmountNoBlur)  //调整背景周边昏暗的程度
                    it.setBackgroundBlurRadius(mBackgroundBlurRadius) //设置背景模糊程度
                }
            }
            BLUR_TYPE_BLUR_BEHIND ->{   //仅模糊后方屏幕
                dialog?.window?.let {
                    it.setDimAmount(if (blursEnabled) mDimAmountWithBlur else mDimAmountNoBlur)  //调整背景周边昏暗的程度
                    it.attributes.blurBehindRadius = mBlurBehindRadius //设置背景周边模糊程度
                    it.attributes = it.attributes //让上面的高斯模糊效果生效
                }
            }
            BLUR_TYPE_BLUR_BACKGROUND_AND_BEHIND ->{  //同时模糊背景和后方屏幕
                dialog?.window?.let {
                    it.setDimAmount(if (blursEnabled) mDimAmountWithBlur else mDimAmountNoBlur)  //调整背景周边昏暗的程度
                    it.setBackgroundBlurRadius(mBackgroundBlurRadius) //设置背景模糊程度
                    it.attributes.blurBehindRadius = mBlurBehindRadius //设置背景周边模糊程度
                    it.attributes = it.attributes //让上面的高斯模糊效果生效
                }
            }
        }
    }
}