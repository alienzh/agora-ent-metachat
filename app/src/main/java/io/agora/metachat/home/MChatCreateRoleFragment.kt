package io.agora.metachat.home

import android.content.res.TypedArray
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.agora.metachat.R
import io.agora.metachat.baseui.BaseUiFragment
import io.agora.metachat.databinding.MchatFragmentCreateRoleBinding
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ToastTools
import io.agora.metachat.widget.OnIntervalClickListener
import java.util.*

/**
 * @author create by zhangwei03
 *
 * create a role
 */
class MChatCreateRoleFragment : BaseUiFragment<MchatFragmentCreateRoleBinding>() {

    companion object{
        private const val defaultPortrait = R.drawable.mchat_portrait0
        private const val defaultBadge = R.drawable.mchat_badge_level0
    }

    private lateinit var mChatViewModel: MChatRoomCreateViewModel

    private var nicknameIllegal = false

    private var portraitIndex = 0
    private var badgeIndex = 0
    private var gender = MChatConstant.Gender.FEMALE

    /**portrait */
    private lateinit var portraitArray: TypedArray

    /**badge */
    private lateinit var badgeArray: TypedArray

    /**nickname*/
    private lateinit var nicknameArray: Array<String>

    private val random by lazy { Random() }

    private val roomName: String by lazy {
        arguments?.getString(MChatConstant.Params.KEY_ROOM_NAME) ?: ""
    }

    private val roomCoverIndex: Int by lazy {
        arguments?.getInt(MChatConstant.Params.KEY_ROOM_COVER_INDEX) ?: 0
    }

    private val roomPassword: String by lazy {
        arguments?.getString(MChatConstant.Params.KEY_ROOM_PASSWORD) ?: ""
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): MchatFragmentCreateRoleBinding? {
        return MchatFragmentCreateRoleBinding.inflate(inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mChatViewModel = ViewModelProvider(this).get(MChatRoomCreateViewModel::class.java)
        initData()
        initView()
        roomObservable()
    }

    private fun initData() {
        portraitArray = resources.obtainTypedArray(R.array.mchat_portrait)
        badgeArray = resources.obtainTypedArray(R.array.mchat_user_badge)
        nicknameArray = resources.getStringArray(R.array.mchat_random_nickname)
    }

    private fun initView() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemInset = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            LogTools.d("systemInset l:${systemInset.left},t:${systemInset.top},r:${systemInset.right},b:${systemInset.bottom}")
            binding.root.setPaddingRelative(0, systemInset.top, 0, systemInset.bottom)
            WindowInsetsCompat.CONSUMED
        }
        binding.ivPortrait.setImageResource(portraitArray.getResourceId(portraitIndex, defaultPortrait))
        binding.ivBadge.setImageResource(badgeArray.getResourceId(badgeIndex, defaultBadge))
        binding.etNickname.setText(nicknameArray[0])
        if (gender == MChatConstant.Gender.MALE) {
            binding.linearMale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple_stroke_red)
            binding.linearFemale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple)
        } else {
            binding.linearMale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple)
            binding.linearFemale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple_stroke_red)
        }
        binding.titleView.setLeftClick(OnIntervalClickListener(this::onClickBack))
        binding.ivPortrait.setOnClickListener(OnIntervalClickListener(this::onClickPortrait))
        binding.ivBadge.setOnClickListener(OnIntervalClickListener(this::onClickBadge))
        binding.ivBadgeArrow.setOnClickListener(OnIntervalClickListener(this::onClickBadge))
        binding.ivNicknameRandom.setOnClickListener(OnIntervalClickListener(this::onClickNicknameRandom))
        binding.linearMale.setOnClickListener(OnIntervalClickListener(this::onClickMale))
        binding.linearFemale.setOnClickListener(OnIntervalClickListener(this::onClickFemale))
        binding.ivSelectAvatar.setOnClickListener(OnIntervalClickListener(this::onClickEnterRoom))
        binding.etNickname.doAfterTextChanged {
            if (it.isNullOrEmpty() || it.length <= 1) {
                // 防止多次更改edittext背景
                if (nicknameIllegal) return@doAfterTextChanged
                nicknameIllegal = true
                binding.tvNicknameIllegal.isVisible = true
                binding.etNickname.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_light_gray_stroke_red)
            } else {
                if (!nicknameIllegal) return@doAfterTextChanged
                nicknameIllegal = false
                binding.tvNicknameIllegal.isVisible = false
                binding.etNickname.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_light_grey)
            }
        }
    }

    private fun onClickBack(view: View) {
        findNavController().popBackStack()
    }

    private fun onClickPortrait(view: View) {
        ToastTools.showCommon("click portrait")
    }

    private fun onClickBadge(view: View) {
        ToastTools.showCommon("click badge")
    }

    private fun onClickNicknameRandom(view: View) {
        binding.etNickname.setText(nicknameArray[random.nextInt(nicknameArray.size)])
    }

    private fun onClickMale(view: View) {
        binding.linearMale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple_stroke_red)
        binding.linearFemale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple)
        gender = MChatConstant.Gender.MALE
    }

    private fun onClickFemale(view: View) {
        binding.linearMale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple)
        binding.linearFemale.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_purple_stroke_red)
        gender = MChatConstant.Gender.FEMALE
    }

    private fun onClickEnterRoom(view: View) {
        val nickname = binding.etNickname.text?.toString() ?: ""
        if (nickname.length <= 1) {
            nicknameIllegal = true
            binding.tvNicknameIllegal.isVisible = true
            binding.etNickname.setBackgroundResource(R.drawable.mchat_bg_rect_radius12_light_gray_stroke_red)
            return
        }
        activity?.let {
            MChatAvatarActivity.startActivity(
                context = it,
                roomName = roomName,
                roomCoverIndex = roomCoverIndex,
                roomPassword = roomPassword,
                nickName = nickname,
                portraitIndex = portraitIndex,
                badgeIndex = badgeIndex,
                gender = gender
            )
        }
    }

    private fun roomObservable() {

    }

    override fun onResume() {
        activity?.let {
            val insetsController = WindowCompat.getInsetsController(it.window, it.window.decorView)
            insetsController.isAppearanceLightStatusBars = false
        }
        super.onResume()
    }
}