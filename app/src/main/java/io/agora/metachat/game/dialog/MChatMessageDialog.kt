package io.agora.metachat.game.dialog

import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
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
class MChatMessageDialog constructor() : BaseFragmentDialog<MchatDialogMessageBinding>() {

    private var messageAdapter: BaseRecyclerAdapter<MchatItemMessageBinding, MChatMessageModel, MChatMessageViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogMessageBinding {
        return MchatDialogMessageBinding.inflate(inflater)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.window?.let { window ->
            window.attributes.windowAnimations = R.style.mchat_anim_bottom_to_top
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
            window.requestFeature(Window.FEATURE_NO_TITLE)
            val params = window.attributes
            params.height = WindowManager.LayoutParams.MATCH_PARENT
            params.width = DeviceTools.dp2px(250).toInt()
            params.gravity = Gravity.START
            window.attributes = params
            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            )
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding?.root?.let {
            setDialogSize(it)
        }
        messageAdapter =
            BaseRecyclerAdapter(MChatGroupIMManager.instance().getAllMsgList(), MChatMessageViewHolder::class.java)
        binding?.rvMessageContent?.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = messageAdapter
        }
        binding?.ivSendMessage?.setOnClickListener(OnIntervalClickListener(this::onClickSend))
    }

    private fun setDialogSize(view: View) {
        val layoutParams: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = DeviceTools.dp2px(250).toInt()
//        activity?.let {
//            layoutParams.height =
//                DeviceTools.screenWidth(it) - DeviceTools.dp2px(60).toInt() - DeviceTools.dp2px(30).toInt()
//        }
        view.layoutParams = layoutParams
        val marginParams: MarginLayoutParams = view.layoutParams as MarginLayoutParams
        layoutParams.topMargin = DeviceTools.dp2px(60).toInt()
        layoutParams.bottomMargin = DeviceTools.dp2px(30).toInt()
        layoutParams.leftMargin = DeviceTools.dp2px(45).toInt()
        view.layoutParams = marginParams
    }

    private fun onClickSend(view: View) {
        val message = binding?.tvMessageContent?.text?.trim().toString() ?: return
        MChatGroupIMManager.instance().sendTxtMsg(message, MChatKeyCenter.nickname) {
            if (it) {
                ThreadTools.get().runOnMainThread {
                    refresh()
                }
            }
        }
    }

    private fun refresh() {
        messageAdapter?.let {
            val startPosition: Int = it.itemCount
            it.dataList.addAll(startPosition, MChatGroupIMManager.instance().getAllMsgList())
            LogTools.e("refresh mesage startPosition:$startPosition")
            if (it.dataList.size > 0) {
                it.notifyItemRangeInserted(startPosition, it.dataList.size)
                binding?.rvMessageContent?.smoothScrollToPosition(it.itemCount - 1)
            }
        }
    }

//    override fun onResume() {
//        super.onResume()
//        dialog?.window?.let { window ->
//            val params = window.attributes
//            params.height = WindowManager.LayoutParams.MATCH_PARENT
//            params.width = DeviceTools.dp2px(250).toInt()
//            params.gravity = Gravity.START
//            params.horizontalMargin = DeviceTools.dp2px(45)
//            params.verticalMargin = DeviceTools.dp2px(30)
//            window.attributes = params
//        }
//    }

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