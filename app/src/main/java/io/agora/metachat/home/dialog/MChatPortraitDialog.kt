package io.agora.metachat.home.dialog

import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.baseui.adapter.BaseRecyclerAdapter
import io.agora.metachat.baseui.adapter.listener.OnItemClickListener
import io.agora.metachat.databinding.MchatDialogSelectPortraitBinding
import io.agora.metachat.databinding.MchatItemPortraitListBinding
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.ToastTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 */
class MChatPortraitDialog constructor() : BaseFragmentDialog<MchatDialogSelectPortraitBinding>() {

    private lateinit var portraitArray: TypedArray
    private var defaultPortrait = R.drawable.mchat_portrait0
    private var selPortraitIndex: Int = 0

    private var portraitAdapter: BaseRecyclerAdapter<MchatItemPortraitListBinding, Int, MChatPortraitViewHolder>? =
        null

    private var confirmCallback: ((selPortraitIndex: Int) -> Unit)? = null

    fun setConfirmCallback(confirmCallback: ((selPortraitIndex: Int) -> Unit)) = apply {
        this.confirmCallback = confirmCallback
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogSelectPortraitBinding {
        return MchatDialogSelectPortraitBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)
        initData()
        initView()
    }

    private fun initData() {
        portraitArray = resources.obtainTypedArray(R.array.mchat_portrait)
    }

    private fun initView() {
        val portraits = mutableListOf<Int>().apply {
            for (i in 0 until portraitArray.length()) {
                add(getVirtualAvatarRes(i))
            }
        }
        portraitAdapter = BaseRecyclerAdapter(portraits, object : OnItemClickListener<Int> {
            override fun onItemClick(data: Int, view: View, position: Int, viewType: Long) {
                if (selPortraitIndex == position) return
                selPortraitIndex = position
                portraitAdapter?.selectedIndex = selPortraitIndex
                portraitAdapter?.notifyDataSetChanged()
            }
        }, MChatPortraitViewHolder::class.java)
        portraitAdapter?.selectedIndex = selPortraitIndex
        binding?.apply {
            rvPortrait.apply {
                addItemDecoration(
                    MaterialDividerItemDecoration(root.context, MaterialDividerItemDecoration.HORIZONTAL).apply {
                        dividerThickness = DeviceTools.dp2px(26).toInt()
                        dividerColor = Color.TRANSPARENT
                    })
                layoutManager = GridLayoutManager(root.context, 3)
                adapter = portraitAdapter
            }
            mbLeft.setOnClickListener(OnIntervalClickListener(this@MChatPortraitDialog::onClickCancel))
            mbRight.setOnClickListener(OnIntervalClickListener(this@MChatPortraitDialog::onClickConfirm))
        }
    }

    private fun onClickCancel(view: View) {
        dismiss()
    }

    private fun onClickConfirm(view: View) {
        ToastTools.showCommon("portrait index:$selPortraitIndex")
    }

    @DrawableRes
    private fun getVirtualAvatarRes(avatarIndex: Int): Int {
        val localPortraitIndex = if (avatarIndex >= 0 && avatarIndex < portraitArray.length()) avatarIndex else 0
        return portraitArray.getResourceId(localPortraitIndex, defaultPortrait)
    }

    /**portrait viewHolder*/
    class MChatPortraitViewHolder constructor(val binding: MchatItemPortraitListBinding) :
        BaseRecyclerAdapter.BaseViewHolder<MchatItemPortraitListBinding, Int>(binding) {
        override fun binding(data: Int?, selectedIndex: Int) {
            data ?: return
            binding.ivUserPortrait.setImageResource(data)

            binding.ivPortraitBg.isInvisible = selectedIndex != bindingAdapterPosition
        }
    }
}