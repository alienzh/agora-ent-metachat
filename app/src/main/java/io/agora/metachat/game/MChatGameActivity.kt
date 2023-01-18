package io.agora.metachat.game

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.databinding.MchatActivityGameBinding
import io.agora.metachat.global.MChatConstant

/**
 * @author create by zhangwei03
 *
 * unity game
 */
class MChatGameActivity : BaseUiActivity<MchatActivityGameBinding>() {

    companion object {

        fun startActivity(
            context: Context, roomId: String, nickName: String, portraitIndex: Int,
            badgeIndex: Int, gender: Int, virtualAvatarIndex: Int
        ) {
            val intent = Intent(context, MChatGameActivity::class.java).apply {
                putExtra(MChatConstant.Params.KEY_ROOM_ID, roomId)
                putExtra(MChatConstant.Params.KEY_NICKNAME, nickName)
                putExtra(MChatConstant.Params.KEY_PORTRAIT_INDEX, portraitIndex)
                putExtra(MChatConstant.Params.KEY_BADGE_INDEX, badgeIndex)
                putExtra(MChatConstant.Params.KEY_GENDER, gender)
                putExtra(MChatConstant.Params.KEY_VIRTUAL_AVATAR, virtualAvatarIndex)
            }
            context.startActivity(intent)
        }
    }

    override fun getViewBinding(inflater: LayoutInflater): MchatActivityGameBinding? {
        return MchatActivityGameBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val insetsController = WindowCompat.getInsetsController(window,window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        super.onCreate(savedInstanceState)
    }
}