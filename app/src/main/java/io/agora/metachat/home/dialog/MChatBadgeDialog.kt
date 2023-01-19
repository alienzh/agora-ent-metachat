package io.agora.metachat.home.dialog

import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.baseui.adapter.BaseRecyclerAdapter
import io.agora.metachat.baseui.adapter.listener.OnItemClickListener
import io.agora.metachat.databinding.MchatDialogSelectBadgeBinding
import io.agora.metachat.databinding.MchatItemBadgeListBinding
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.ToastTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatBadgeDialog : BaseFragmentDialog<MchatDialogSelectBadgeBinding>() {

    private lateinit var badgeArray: TypedArray
    private var defaultBadge = R.drawable.mchat_badge_level0
    private var selBadgeIndex: Int = 0

    private var badgeAdapter: BaseRecyclerAdapter<MchatItemBadgeListBinding, Int, MChatBadgeViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogSelectBadgeBinding {
        return MchatDialogSelectBadgeBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        badgeArray = resources.obtainTypedArray(R.array.mchat_user_badge)
    }

    private fun initView() {
        val portraits = mutableListOf<Int>().apply {
            for (i in 0 until badgeArray.length()) {
                add(getVirtualAvatarRes(i))
            }
        }
        badgeAdapter = BaseRecyclerAdapter(portraits, object : OnItemClickListener<Int> {
            override fun onItemClick(data: Int, view: View, position: Int, viewType: Long) {
                if (selBadgeIndex==position) return
                selBadgeIndex = position
                badgeAdapter?.selectedIndex = selBadgeIndex
                badgeAdapter?.notifyDataSetChanged()
            }
        }, MChatBadgeViewHolder::class.java)
        badgeAdapter?.selectedIndex = selBadgeIndex
        binding.rvBadge.apply {
            addItemDecoration(
                MaterialDividerItemDecoration(binding.root.context, MaterialDividerItemDecoration.HORIZONTAL).apply {
                    dividerThickness = DeviceTools.dp2px(16).toInt()
                    dividerColor = Color.TRANSPARENT
                })
            layoutManager = GridLayoutManager(binding.root.context, 3)
            adapter = badgeAdapter
        }
        binding.mbLeft.setOnClickListener(OnIntervalClickListener(this::onClickCancel))
        binding.mbRight.setOnClickListener(OnIntervalClickListener(this::onClickConfirm))
    }

    private fun onClickCancel(view: View) {
        dismiss()
    }

    private fun onClickConfirm(view: View) {
       ToastTools.showCommon("badge index:$selBadgeIndex")
    }

    @DrawableRes
    private fun getVirtualAvatarRes(avatarIndex: Int): Int {
        val localBadgeIndex = if (avatarIndex >= 0 && avatarIndex < badgeArray.length()) avatarIndex else 0
        return badgeArray.getResourceId(localBadgeIndex, defaultBadge)
    }

    /**badge viewHolder*/
    class MChatBadgeViewHolder constructor(val binding: MchatItemBadgeListBinding) :
        BaseRecyclerAdapter.BaseViewHolder<MchatItemBadgeListBinding, Int>(binding) {
        override fun binding(data: Int?, selectedIndex: Int) {
            data ?: return
            binding.ivUserBadge.setImageResource(data)
            binding.ivBadgeBg.isVisible = selectedIndex == bindingAdapterPosition

        }
    }
}