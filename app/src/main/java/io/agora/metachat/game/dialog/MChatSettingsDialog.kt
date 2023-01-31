package io.agora.metachat.game.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.core.view.isVisible
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.baseui.dialog.CommonFragmentAlertDialog
import io.agora.metachat.databinding.MchatDialogSettingsBinding
import io.agora.metachat.home.dialog.MChatBadgeDialog
import io.agora.metachat.home.dialog.MChatPortraitDialog
import io.agora.metachat.imkit.MChatGroupIMManager
import io.agora.metachat.tools.ToastTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatSettingsDialog constructor() : BaseFragmentDialog<MchatDialogSettingsBinding>() {

    companion object {
        private const val GENERAL = 0
        private const val SOUND = 1
    }

    // 默认选中通用
    private var curSelected = GENERAL

    private var exitCallback: (() -> Unit)? = null

    fun setExitCallback(exitCallback: () -> Unit) = apply {
        this.exitCallback = exitCallback
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogSettingsBinding {
        return MchatDialogSettingsBinding.inflate(inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.let { window ->
            window.attributes.windowAnimations = R.style.mchat_anim_bottom_to_top
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window.requestFeature(Window.FEATURE_NO_TITLE)
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
        }
        isCancelable = false
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    fun initView() {
        binding?.let {
            it.ivSettingsBack.setOnClickListener(OnIntervalClickListener(this::onClickSettingBack))
            it.layoutGeneralTab.setOnClickListener(OnIntervalClickListener(this::onClickGeneralTab))
            it.layoutSoundTab.setOnClickListener(OnIntervalClickListener(this::onClickSoundTab))
            it.layoutUserPortrait.setOnClickListener(OnIntervalClickListener(this::onClickUserPortrait))
            it.layoutUserBadge.setOnClickListener(OnIntervalClickListener(this::onClickUserBadge))
            it.layoutVirtualAvatar.setOnClickListener(OnIntervalClickListener(this::onClickVirtualAvatar))
            it.layoutQuitRoom.setOnClickListener(OnIntervalClickListener(this::onClickExitRoom))
        }
    }

    private fun onClickSettingBack(view: View) {
        dismiss()
    }

    // 通用点击
    private fun onClickGeneralTab(view: View) {
        if (curSelected == GENERAL) return
        curSelected = GENERAL
        binding?.layoutGeneralContent?.isVisible = false
        binding?.layoutSoundContent?.isVisible = true
    }

    // 声音点击
    private fun onClickSoundTab(view: View) {
        if (curSelected == SOUND) return
        curSelected = SOUND
        binding?.layoutGeneralContent?.isVisible = true
        binding?.layoutSoundContent?.isVisible = false
    }

    // 点击更换头像
    private fun onClickUserPortrait(view: View) {
        MChatPortraitDialog().setConfirmCallback {
            ToastTools.showCommon("selected portrait index:$it")
        }.show(childFragmentManager, "portrait dialog")
    }

    // 点击更换徽章
    private fun onClickUserBadge(view: View) {
        MChatBadgeDialog().setConfirmCallback {
            ToastTools.showCommon("selected badge index:$it")
        }.show(childFragmentManager, "badge dialog")
    }

    // 点击更换虚拟形象
    private fun onClickVirtualAvatar(view: View) {
        // TODO:
        ToastTools.showCommon("todo click avatar")
    }

    // 退出房间
    private fun onClickExitRoom(view: View) {
        if (MChatGroupIMManager.instance().isRoomOwner()){
            CommonFragmentAlertDialog()
                .titleText(resources.getString(R.string.mchat_exit_room))
                .contentText(resources.getString(R.string.mchat_exit_room_tips))
                .leftText(resources.getString(R.string.mchat_cancel))
                .rightText(resources.getString(R.string.mchat_confirm))
                .setOnClickListener(object : CommonFragmentAlertDialog.OnClickBottomListener {
                    override fun onConfirmClick() {
                        exitCallback?.invoke()
                    }
                }).show(childFragmentManager, "exit dialog")
        }else{
            exitCallback?.invoke()
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.let { window ->
            val params = window.attributes
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.gravity = Gravity.TOP
            window.attributes = params
        }
    }
}