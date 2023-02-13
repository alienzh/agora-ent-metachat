package io.agora.metachat.baseui.dialog

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogCenterFragmentAlertBinding
import io.agora.metachat.tools.DeviceTools

/**
 * @author create by zhangwei03
 *
 * 中间弹框，确认/取消按钮
 */
class CommonFragmentAlertDialog constructor() : BaseFragmentDialog<MchatDialogCenterFragmentAlertBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogCenterFragmentAlertBinding {
        return MchatDialogCenterFragmentAlertBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        binding?.apply {
            setDialogSize(view)
            if (!TextUtils.isEmpty(titleText)) {
                mtTitle.text = titleText
            } else {
                mtTitle.isVisible = false
                // 更改间距
                val layoutParams: ConstraintLayout.LayoutParams = mbLeft.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.setMargins(layoutParams.marginStart, DeviceTools.dp2px(34).toInt(), layoutParams.marginEnd, layoutParams.bottomMargin)
                mbLeft.layoutParams = layoutParams
            }
            if (!TextUtils.isEmpty(contentText)) {
                mtContent.text = contentText
            }
            if (!TextUtils.isEmpty(leftText)) {
                mbLeft.text = leftText
            }
            if (!TextUtils.isEmpty(rightText)) {
                mbRight.text = rightText
            }
            mbLeft.setOnClickListener {
                dismiss()
                clickListener?.onCancelClick()
            }
            mbRight.setOnClickListener {
                dismiss()
                clickListener?.onConfirmClick()
            }
        }
    }

    private fun setDialogSize(view: View) {
        val layoutParams: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = DeviceTools.dp2px(300).toInt()
        view.layoutParams = layoutParams
    }

    private var titleText: String = ""
    private var contentText: String = ""
    private var leftText: String = ""
    private var rightText: String = ""
    private var clickListener: OnClickBottomListener? = null

    fun titleText(titleText: String) = apply {
        this.titleText = titleText
    }

    fun contentText(contentText: String) = apply {
        this.contentText = contentText
    }

    fun leftText(leftText: String) = apply {
        this.leftText = leftText
    }

    fun rightText(rightText: String) = apply {
        this.rightText = rightText
    }

    fun setOnClickListener(clickListener: OnClickBottomListener) = apply {
        this.clickListener = clickListener
    }

    interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        fun onConfirmClick()

        /**
         * 点击取消按钮事件
         */
        fun onCancelClick() {}
    }
}