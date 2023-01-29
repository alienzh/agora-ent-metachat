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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.baseui.adapter.BaseRecyclerAdapter
import io.agora.metachat.baseui.adapter.listener.OnItemClickListener
import io.agora.metachat.databinding.MchatActivityVirtualAvatarBinding
import io.agora.metachat.databinding.MchatItemVirtualAvatarListBinding
import io.agora.metachat.game.MChatGameActivity
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 *
 * virtual avatar
 */
class MChatVirtualAvatarActivity : BaseUiActivity<MchatActivityVirtualAvatarBinding>() {

    companion object {

        fun startActivity(
            context: Context, isCreate: Boolean, roomName: String, roomCoverIndex: Int, roomPassword: String,
            nickName: String, portraitIndex: Int, badgeIndex: Int, gender: Int
        ) {
            val intent = Intent(context, MChatVirtualAvatarActivity::class.java).apply {
                putExtra(MChatConstant.Params.KEY_IS_CREATE, isCreate)
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

    private lateinit var mChatViewModel: MChatRoomCreateViewModel

    private val roomId by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_ID) ?: "" }
    private val roomName by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_NAME) ?: "" }
    private val roomCoverIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_ROOM_COVER_INDEX, 0) }
    private val roomPassword by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_PASSWORD) ?: "" }
    private val nickName by lazy { intent.getStringExtra(MChatConstant.Params.KEY_NICKNAME) ?: "" }
    private val portraitIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_PORTRAIT_INDEX, 0) }
    private val badgeIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_BADGE_INDEX, 0) }
    private val userGender by lazy { intent.getIntExtra(MChatConstant.Params.KEY_GENDER, MChatConstant.Gender.FEMALE) }
    private val isFromCreate: Boolean by lazy { intent.getBooleanExtra(MChatConstant.Params.KEY_IS_CREATE, false) }

    /**virtual avatar */
    private lateinit var virtualAvatarArray: TypedArray
    private var selVirtualAvatarIndex: Int = 0

    private var defaultVirtualAvatar = R.drawable.mchat_female0

    private var avatarAdapter: BaseRecyclerAdapter<MchatItemVirtualAvatarListBinding, Int, MChatVirtualAvatarViewHolder>? =
        null

    override fun getViewBinding(inflater: LayoutInflater): MchatActivityVirtualAvatarBinding? {
        return MchatActivityVirtualAvatarBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        LogTools.d("$roomName $roomPassword $roomCoverIndex $nickName $portraitIndex $badgeIndex $userGender")
        super.onCreate(savedInstanceState)
        mChatViewModel = ViewModelProvider(this).get(MChatRoomCreateViewModel::class.java)
        initData()
        initView()
        roomObservable()
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
        binding.tvChooseAvatarTips.text = resources.getString(R.string.mchat_choose_your_avatar, nickName)
        binding.ivCurrentAvatar.setImageResource(
            virtualAvatarArray.getResourceId(selVirtualAvatarIndex, defaultVirtualAvatar)
        )
        avatarAdapter = BaseRecyclerAdapter(virtualAvatars, object : OnItemClickListener<Int> {
            override fun onItemClick(data: Int, view: View, position: Int, viewType: Long) {
                if (selVirtualAvatarIndex == position) return
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

    private fun roomObservable() {
        mChatViewModel.createRoomObservable().observe(this){
            mChatViewModel.joinRoom(it.roomId,it.password)
        }
        mChatViewModel.joinRoomObservable().observe(this){
            dismissLoading()
            MChatGameActivity.startActivity(
                context = this,
                roomId = it.roomId,
                nickName = nickName,
                portraitIndex = portraitIndex,
                badgeIndex = badgeIndex,
                gender = userGender,
                virtualAvatarIndex = selVirtualAvatarIndex
            )
        }
    }

    private fun onClickEnterGame(view: View) {
        showLoading(false)
        if (isFromCreate) {
            mChatViewModel.createRoom(roomName, roomCoverIndex, roomPassword)
        } else {
            mChatViewModel.joinRoom(roomId, roomPassword)
        }
        // TODO: test
//        MChatBeginnerDialog(MChatBeginnerDialog.BEGINNER_TYPE).show(supportFragmentManager,"beginner")
    }

    private fun onClickBack(view: View) {
        finish()
        // TODO: test
//        MChatBeginnerDialog(MChatBeginnerDialog.VISITOR_TYPE).show(supportFragmentManager,"visitor")
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissLoading()
    }

    /**virtual avatar viewHolder*/
    class MChatVirtualAvatarViewHolder constructor(val binding: MchatItemVirtualAvatarListBinding) :
        BaseRecyclerAdapter.BaseViewHolder<MchatItemVirtualAvatarListBinding, Int>(binding) {
        override fun binding(data: Int?, selectedIndex: Int) {
            data ?: return
            binding.ivUserAvatar.setImageResource(data)
            binding.ivAvatarBg.isVisible = selectedIndex == bindingAdapterPosition
        }
    }
}