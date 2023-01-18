package io.agora.metachat.home

import android.content.res.TypedArray
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiFragment
import io.agora.metachat.baseui.adapter.BaseRecyclerAdapter
import io.agora.metachat.baseui.adapter.BaseRecyclerAdapter.BaseViewHolder
import io.agora.metachat.baseui.adapter.listener.OnItemClickListener
import io.agora.metachat.databinding.MchatFragmentRoomListBinding
import io.agora.metachat.databinding.MchatItemRoomListBinding
import io.agora.metachat.service.MChatRoomModel
import io.agora.metachat.tools.*
import io.agora.metachat.widget.OnIntervalClickListener

/**
 * @author create by zhangwei03
 *
 * meta chat room list
 */
class MChatRoomListFragment : BaseUiFragment<MchatFragmentRoomListBinding>(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private const val HIDE_REFRESH_DELAY = 3000L
    }

    private lateinit var mChatViewModel: MChatRoomCreateViewModel
    private var roomAdapter: BaseRecyclerAdapter<MchatItemRoomListBinding, MChatRoomModel, MChatRoomViewHolder>? = null

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatFragmentRoomListBinding {
        return MchatFragmentRoomListBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChatViewModel = ViewModelProvider(this).get(MChatRoomCreateViewModel::class.java)
        initView()
        roomObservable()
    }

    private fun initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            LogTools.d("systemInset l:${systemInset.left},t:${systemInset.top},r:${systemInset.right},b:${systemInset.bottom}")
            binding.root.setPaddingRelative(0, systemInset.top, 0, 0)
            WindowInsetsCompat.CONSUMED
        }
        roomAdapter = BaseRecyclerAdapter(null, object : OnItemClickListener<MChatRoomModel> {
            override fun onItemClick(data: MChatRoomModel, view: View, position: Int, viewType: Long) {
                if (FastClickTools.isFastClick(view)) return
                goCreateRole()
            }
        }, MChatRoomViewHolder::class.java)
        binding.rvRoomList.apply {
            val padding: Int = DeviceTools.dp2px(8).toInt()
            val itemDecoration = object : ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.top = padding
                    outRect.bottom = padding
                    outRect.left = padding
                    outRect.right = padding
                }
            }
            addItemDecoration(itemDecoration)
            layoutManager = GridLayoutManager(context, 2)
            adapter = roomAdapter
            setOnEmptyCallback {
                binding.groupRoomIntroduce.isVisible = it
                binding.linearCreateRoomBottom.isVisible = !it
            }
        }

        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.linearCreateRoomBottom.setOnClickListener(OnIntervalClickListener(this::onClickCreateRoom))
        binding.linearCreateRoomIntroduce.setOnClickListener(OnIntervalClickListener(this::onClickCreateRoom))
    }

    private fun goCreateRole() {
        findNavController().navigate(R.id.action_roomListFragment_to_crateRoleFragment)
    }

    private fun onClickCreateRoom(view: View) {
        findNavController().navigate(R.id.action_roomListFragment_to_crateRoomFragment)
    }

    private fun roomObservable() {
        mChatViewModel.roomListObservable().observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
            LogTools.d("meta chat room list size:${it?.size}")
            roomAdapter?.submitListAndPurge(it ?: mutableListOf())
        }
    }

    override fun onResume() {
        activity?.let {
            val insetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
            insetsController.isAppearanceLightStatusBars = true
        }
        super.onResume()
        mChatViewModel.fetchRoomList()
    }

    override fun onRefresh() {
        ThreadTools.get().runOnMainThreadDelay({ mChatViewModel.fetchRoomList() }, HIDE_REFRESH_DELAY)
    }

    /**room list viewHolder*/
    class MChatRoomViewHolder constructor(val binding: MchatItemRoomListBinding) :
        BaseViewHolder<MchatItemRoomListBinding, MChatRoomModel>(binding) {

        companion object {
            private const val defaultCover = R.drawable.mchat_room_cover0
        }

        override fun binding(data: MChatRoomModel?, selectedIndex: Int) {
            data ?: return
            binding.ivRoomLock.isVisible = data.isPrivate
            binding.ivRoomCover.setImageResource(getRoomCoverRes(data.roomCoverIndex))
            binding.tvRoomName.text = data.roomName
            binding.tvRoomId.text = context.getString(R.string.mchat_room_id, data.roomId)
            binding.tvMembers.text = "${data.roomMembers}"
        }

        @DrawableRes
        private fun getRoomCoverRes(index: Int): Int {
            val coverArray: TypedArray = context.resources.obtainTypedArray(R.array.mchat_room_cover)
            val localAvatarIndex = if (index >= 0 && index < coverArray.length()) index else 0
            return coverArray.getResourceId(localAvatarIndex, defaultCover)
        }
    }
}