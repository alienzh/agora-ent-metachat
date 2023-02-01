package io.agora.metachat.game.dialog

import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.metachat.R
import io.agora.metachat.baseui.adapter.BaseRecyclerAdapter
import io.agora.metachat.databinding.MchatDialogMessageBinding
import io.agora.metachat.databinding.MchatItemMessageBinding
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.imkit.MChatGroupIMManager
import io.agora.metachat.imkit.MChatMessageModel
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools
import io.agora.metachat.widget.OnIntervalClickListener
import java.text.SimpleDateFormat
import java.util.*

/**
 * @author create by zhangwei03
 */
class MChatMessageDialog constructor() : MChatBlurDialog<MchatDialogMessageBinding>() {

    private var messageAdapter: BaseRecyclerAdapter<MchatItemMessageBinding, MChatMessageModel, MChatMessageViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogMessageBinding {
        return MchatDialogMessageBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(true)
        initView()
    }

    private fun initView() {
        messageAdapter =
            BaseRecyclerAdapter(MChatGroupIMManager.instance().getAllData(), MChatMessageViewHolder::class.java)
        binding?.apply {
            rvMessageContent.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            rvMessageContent.adapter = messageAdapter
            ivSendMessage.setOnClickListener(OnIntervalClickListener(this@MChatMessageDialog::onClickSend))
            etMessage.setOnEditorActionListener { textView, actionId, keyEvent ->
                when (actionId and EditorInfo.IME_MASK_ACTION) {
                    EditorInfo.IME_ACTION_DONE -> {
                        val message = binding?.etMessage?.text?.trim()?.toString()
                        sendMessage(message)
                    }
                    else -> {}
                }
                false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.attributes.windowAnimations = R.style.mchat_anim_bottom_to_top
            val params = window.attributes
            params.width = DeviceTools.dp2px(295).toInt()
            activity?.let {
                params.height = DeviceTools.screenHeight(it)
            }
            params.gravity = Gravity.START
            params.dimAmount = 0f
            window.attributes = params
        }
        binding?.apply {
            setDialogSize(root)
        }
    }

    private fun setDialogSize(view: View) {
//        val layoutParams: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
//        layoutParams.width = DeviceTools.dp2px(250).toInt()
//        activity?.let {
//            layoutParams.height =
//                DeviceTools.screenHeight(it) - DeviceTools.dp2px(60).toInt() - DeviceTools.dp2px(30).toInt()
//        }
//        view.layoutParams = layoutParams
        val marginParams: MarginLayoutParams = view.layoutParams as MarginLayoutParams
        marginParams.topMargin = DeviceTools.dp2px(60).toInt()
        marginParams.bottomMargin = DeviceTools.dp2px(30).toInt()
        marginParams.leftMargin = DeviceTools.dp2px(45).toInt()
        view.layoutParams = marginParams
    }


    // 点击消息输入框，弹出软键盘
    private fun sendMessage(message: String?) {
        if (message.isNullOrEmpty()) return
        MChatGroupIMManager.instance().sendTxtMsg(message, MChatKeyCenter.nickname) {
            if (it) {
                ThreadTools.get().runOnMainThread {
                    binding?.etMessage?.setText("")
                    refreshMessage()
                }
            }
        }
    }

    private fun onClickSend(view: View) {
        val message = binding?.etMessage?.text?.trim()?.toString()
        sendMessage(message)
    }

    private fun refreshMessage() {
        messageAdapter?.let {
            val startPosition: Int = it.itemCount
            it.dataList.addAll(startPosition, MChatGroupIMManager.instance().getAllMsgList())
            LogTools.d("refresh message startPosition:$startPosition")
            if (it.dataList.size > 0) {
                it.notifyItemRangeInserted(startPosition, it.dataList.size)
                binding?.rvMessageContent?.smoothScrollToPosition(it.itemCount - 1)
            }
        }
    }

    /**message list viewHolder*/
    class MChatMessageViewHolder constructor(val binding: MchatItemMessageBinding) :
        BaseRecyclerAdapter.BaseViewHolder<MchatItemMessageBinding, MChatMessageModel>(binding) {

        companion object {
            private const val defaultCover = R.drawable.mchat_room_cover0
        }

        override fun binding(data: MChatMessageModel?, selectedIndex: Int) {
            data ?: return
            binding.ivUserPortrait.setImageResource(getPortraitCoverRes(data.portraitIndex))
            binding.tvNickname.text = data.nickname ?: ""
            binding.tvCurrentUser.isVisible = !data.from.isNullOrEmpty() && data.from == MChatKeyCenter.imUid
            binding.tvMessage.text = data.content ?: ""
            binding.tvSendTime.text = getSendTime(data.timeStamp)
        }

        @DrawableRes
        private fun getPortraitCoverRes(index: Int): Int {
            val coverArray: TypedArray = context.resources.obtainTypedArray(R.array.mchat_portrait)
            val localAvatarIndex = if (index >= 0 && index < coverArray.length()) index else 0
            return coverArray.getResourceId(localAvatarIndex, defaultCover)
        }

        private fun getSendTime(timeStamp: Long): String {
            val dateFormat = SimpleDateFormat("HH:mm")
            return dateFormat.format(Date(timeStamp))
        }
    }
}