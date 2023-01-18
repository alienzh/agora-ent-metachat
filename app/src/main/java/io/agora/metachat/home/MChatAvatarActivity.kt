package io.agora.metachat.home

import android.content.Context
import android.content.Intent
import android.content.res.TypedArray
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.view.*
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.baseui.adapter.BaseRecyclerAdapter
import io.agora.metachat.baseui.adapter.listener.OnItemClickListener
import io.agora.metachat.databinding.MchatActivityAvatarBinding
import io.agora.metachat.databinding.MchatItemAvatarListBinding
import io.agora.metachat.game.MChatGameActivity
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 *
 * virtual avatar
 */
class MChatAvatarActivity : BaseUiActivity<MchatActivityAvatarBinding>() {

    companion object {

        fun startActivity(
            context: Context, roomName: String, roomCoverIndex: Int, roomPassword: String,
            nickName: String, portraitIndex: Int, badgeIndex: Int, gender: Int
        ) {
            val intent = Intent(context, MChatAvatarActivity::class.java).apply {
                putExtra(MChatConstant.Params.KEY_ROOM_NAME, roomName)
                putExtra(MChatConstant.Params.KEY_ROOM_COVER_INDEX, roomCoverIndex)
                putExtra(MChatConstant.Params.KEY_ROOM_PASSWORD, roomPassword)
                putExtra(MChatConstant.Params.KEY_NICKNAME, nickName)
                putExtra(MChatConstant.Params.KEY_PORTRAIT_INDEX, portraitIndex)
                putExtra(MChatConstant.Params.KEY_BADGE_INDEX, badgeIndex)
                putExtra(MChatConstant.Params.KEY_GENDER, gender)
            }
            context.startActivity(intent)
        }
    }

    private val roomName by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_NAME) ?: "" }
    private val roomCoverIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_ROOM_COVER_INDEX, 0) }
    private val roomPassword by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_PASSWORD) ?: "" }

    private val nickName by lazy { intent.getStringExtra(MChatConstant.Params.KEY_NICKNAME) ?: "" }
    private val portraitIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_PORTRAIT_INDEX, 0) }
    private val badgeIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_BADGE_INDEX, 0) }
    private val userGender by lazy { intent.getIntExtra(MChatConstant.Params.KEY_GENDER, MChatConstant.Gender.FEMALE) }

    /**virtual avatar */
    private lateinit var virtualAvatarArray: TypedArray
    private var selVirtualAvatarIndex: Int = 0

    private var defaultVirtualAvatar = R.drawable.mchat_female0

    private var avatarAdapter: BaseRecyclerAdapter<MchatItemAvatarListBinding, Int, MChatVirtualAvatarViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater): MchatActivityAvatarBinding? {
        return MchatActivityAvatarBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val insetsController = WindowCompat.getInsetsController(window,window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        super.onCreate(savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        virtualAvatarArray = if (userGender == MChatConstant.Gender.MALE) {
            resources.obtainTypedArray(R.array.mchat_avatar_male)
        } else {
            resources.obtainTypedArray(R.array.mchat_avatar_female)
        }
        defaultVirtualAvatar = if (userGender == MChatConstant.Gender.MALE) {
            R.drawable.mchat_male0
        } else {
            R.drawable.mchat_female0
        }
    }

    @DrawableRes
    private fun getVirtualAvatarRes(avatarIndex: Int): Int {
        val localAvatarIndex = if (avatarIndex >= 0 && avatarIndex < virtualAvatarArray.length()) avatarIndex else 0
        return virtualAvatarArray.getResourceId(localAvatarIndex, defaultVirtualAvatar)
    }

    private fun initView() {
        val virtualAvatars = mutableListOf<Int>().apply {
            for (i in 0 until virtualAvatarArray.length()) {
                add(getVirtualAvatarRes(i))
            }
        }
        binding.ivCurrentAvatar.setImageResource(
            virtualAvatarArray.getResourceId(
                selVirtualAvatarIndex, defaultVirtualAvatar
            )
        )
        avatarAdapter = BaseRecyclerAdapter(virtualAvatars, object : OnItemClickListener<Int> {
            override fun onItemClick(data: Int, view: View, position: Int, viewType: Long) {
                selVirtualAvatarIndex = position
                binding.ivCurrentAvatar.setImageResource(data)
                avatarAdapter?.selectedIndex = selVirtualAvatarIndex
                avatarAdapter?.notifyDataSetChanged()
            }
        }, MChatVirtualAvatarViewHolder::class.java)
        avatarAdapter?.selectedIndex = selVirtualAvatarIndex
        binding.rvAvatarList.apply {
            addItemDecoration(
                MaterialDividerItemDecoration(getCurActivity(), MaterialDividerItemDecoration.VERTICAL).apply {
                    dividerThickness = DeviceTools.dp2px(15).toInt()
                    dividerColor = Color.TRANSPARENT
                })
            layoutManager = GridLayoutManager(getCurActivity(), 5)
            adapter = avatarAdapter
        }
        binding.linearEnterRoom.setOnClickListener(OnIntervalClickListener(this::onClickEnterGame))
        binding.linearAvatarBack.setOnClickListener(OnIntervalClickListener(this::onClickBack))
    }

    private fun onClickEnterGame(view: View) {
        val currentTime = System.currentTimeMillis()
        MChatGameActivity.startActivity(
            context = this,
            roomId = currentTime.toString(),
            nickName = nickName,
            portraitIndex = portraitIndex,
            badgeIndex = badgeIndex,
            gender = userGender,
            virtualAvatarIndex = selVirtualAvatarIndex
        )
    }

    private fun onClickBack(view: View) {
        finish()
    }

    /**virtual avatar viewHolder*/
    class MChatVirtualAvatarViewHolder constructor(val binding: MchatItemAvatarListBinding) :
        BaseRecyclerAdapter.BaseViewHolder<MchatItemAvatarListBinding, Int>(binding) {
        override fun binding(data: Int?, selectedIndex: Int) {
            data ?: return
            binding.ivUserAvatar.setImageResource(data)
            binding.ivAvatarBg.isVisible = selectedIndex == bindingAdapterPosition
        }
    }
}