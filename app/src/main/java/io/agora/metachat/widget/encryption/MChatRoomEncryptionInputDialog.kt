package io.agora.metachat.widget.encryption

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.databinding.MchatDialogEncryptionBinding

/**
 * 输入密码 dialog
 */
class MChatRoomEncryptionInputDialog constructor() : BaseFragmentDialog<MchatDialogEncryptionBinding>() {
    private val BOND = 1
    private var isCancel = false

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogEncryptionBinding? {
        return MchatDialogEncryptionBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)

        mBinding?.apply {
            if (!TextUtils.isEmpty(titleText)) {
                tvTitle.text = titleText
            }

            if (!TextUtils.isEmpty(leftText)) {
                mbLeft.text = leftText
            }
            if (!TextUtils.isEmpty(rightText)) {
                mbRight.text = rightText
            }

            mbLeft.setOnClickListener {
                clickListener?.onCancelClick()
                dismiss()
            }
            mbRight.setOnClickListener {
                clickListener?.onConfirmClick(etInputPassword.text.toString())
                dismiss()
            }
            etInputPassword.requestFocus()
            dialog?.setCancelable(isCancel)
            dialog?.setCanceledOnTouchOutside(isCancel)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.sendEmptyMessageDelayed(BOND,200)
    }

    private var clickListener: OnClickBottomListener? = null
    private var leftText: String = ""
    private var rightText: String = ""
    private var titleText: String = ""

    fun setOnClickListener(clickListener: OnClickBottomListener) = apply {
        this.clickListener = clickListener
    }

    fun leftText(leftText: String) = apply {
        this.leftText = leftText
    }

    fun rightText(rightText: String) = apply {
        this.rightText = rightText
    }

    fun titleText(titleText: String) = apply {
        this.titleText = titleText
    }

    fun setDialogCancelable(isCancel: Boolean) = apply {
        this.isCancel = isCancel;
    }

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                BOND -> {
                    val inputMethodManager =
                        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
                }
            }
        }
    }

    interface OnClickBottomListener {
        /**
         * 点击确定按钮事件
         */
        fun onConfirmClick(password: String)

        /**
         * 点击取消按钮事件
         */
        fun onCancelClick() {}
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}