package io.agora.metachat.game.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseFragmentDialog
import io.agora.metachat.baseui.adapter.BaseRecyclerAdapter
import io.agora.metachat.databinding.MchatDialogBeginnerGuideBinding
import io.agora.metachat.databinding.MchatItemBeginnerGuideBinding
import io.agora.metachat.tools.DeviceTools

/**
 * @author create by zhangwei03
 */
class MChatBeginnerDialog constructor(val type: Int) : BaseFragmentDialog<MchatDialogBeginnerGuideBinding>() {
    companion object {
        const val NOVICE_TYPE = 1
        const val VISITOR_TYPE = 2
    }

    private lateinit var guideArray: Array<String>

    private var guideAdapter: BaseRecyclerAdapter<MchatItemBeginnerGuideBinding, String, MChatGuideViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatDialogBeginnerGuideBinding {
        return MchatDialogBeginnerGuideBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.root?.let {
            setDialogSize(it)
        }
        initData()
        initView()
    }

    private fun initData() {
        guideArray = if (type == NOVICE_TYPE) {
            resources.getStringArray(R.array.mchat_beginner_guide_content)
        } else {
            resources.getStringArray(R.array.mchat_visitor_mode_content)
        }
    }

    private fun initView() {
        binding?.apply {
            tvTitle.text =
                if (type == NOVICE_TYPE) {
                    resources.getString(R.string.mchat_beginner_guide)
                } else {
                    resources.getString(R.string.mchat_visitor_mode_title)
                }
            ivClose.setOnClickListener {
                dismiss()
            }
            val contents = mutableListOf<String>().apply {
                if (type == NOVICE_TYPE) {
                    add(resources.getString(R.string.mchat_beginner_guide_precautions))
                } else {
                    add(resources.getString(R.string.mchat_visitor_mode_precautions))
                }
                for (i in guideArray.indices) {
                    add(guideArray[i])
                }
            }
            guideAdapter = BaseRecyclerAdapter(contents, null, MChatGuideViewHolder::class.java)
            rvContent.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            rvContent.adapter = guideAdapter
        }
    }

    private fun setDialogSize(view: View) {
        val layoutParams: FrameLayout.LayoutParams = view.layoutParams as FrameLayout.LayoutParams
        layoutParams.width = DeviceTools.dp2px(400).toInt()
        layoutParams.height = DeviceTools.dp2px(255).toInt()
        view.layoutParams = layoutParams
    }

    /**guide viewHolder*/
    class MChatGuideViewHolder constructor(val binding: MchatItemBeginnerGuideBinding) :
        BaseRecyclerAdapter.BaseViewHolder<MchatItemBeginnerGuideBinding, String>(binding) {
        override fun binding(data: String?, selectedIndex: Int) {
            data ?: return
            if (bindingAdapterPosition == 0) {
                binding.tvGuideTitle.isVisible = true
                binding.ivGuideNumber.isVisible = false
                binding.tvGuideContent.isVisible = false
                binding.tvGuideTitle.text = data
            } else {
                binding.tvGuideTitle.isVisible = false
                binding.ivGuideNumber.isVisible = true
                binding.tvGuideContent.isVisible = true
                val numberResName = "mchat_guide_$bindingAdapterPosition"
                var numberDrawable = DeviceTools.getDrawableId(context, numberResName)
                if (numberDrawable == 0) numberDrawable = R.drawable.mchat_guide_1
                binding.ivGuideNumber.setImageResource(numberDrawable)
                binding.tvGuideContent.text = data
            }
        }
    }
}