package io.agora.metachat.game

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.ViewGroup
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import io.agora.metachat.baseui.BaseUiActivity
import io.agora.metachat.databinding.MchatActivityGameBinding
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.tools.LogTools
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.PermissionRequest

/**
 * @author create by zhangwei03
 *
 * unity game
 */
class MChatGameActivity : BaseUiActivity<MchatActivityGameBinding>(), EasyPermissions.PermissionCallbacks,
    EasyPermissions.RationaleCallbacks {

    companion object {

        const val RC_PERMISSIONS = 101
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

    private var mTextureView: TextureView? = null
    private lateinit var gameViewModel: MChatGameViewModel

    private val roomId by lazy { intent.getStringExtra(MChatConstant.Params.KEY_ROOM_ID) ?: "" }
    private val nickName by lazy { intent.getStringExtra(MChatConstant.Params.KEY_NICKNAME) ?: "" }
    private val portraitIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_PORTRAIT_INDEX, 0) }
    private val badgeIndex by lazy { intent.getIntExtra(MChatConstant.Params.KEY_BADGE_INDEX, 0) }
    private val userGender by lazy { intent.getIntExtra(MChatConstant.Params.KEY_GENDER, MChatConstant.Gender.FEMALE) }
    private val virtualAvatar by lazy { intent.getIntExtra(MChatConstant.Params.KEY_VIRTUAL_AVATAR, 0) }

    private var mReCreateScene = false
    private var mSurfaceSizeChange = false

    override fun getViewBinding(inflater: LayoutInflater): MchatActivityGameBinding {
        return MchatActivityGameBinding.inflate(inflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        super.onCreate(savedInstanceState)
        gameViewModel = ViewModelProvider(this).get(MChatGameViewModel::class.java)
        gameViewModel.resetSceneState()
        requestPermission()
        initUnityView()
        gameObservable()
    }

    private fun gameObservable() {
        gameViewModel.isEnterSceneObservable().observe(this){}
        gameViewModel.enableMicObservable().observe(this){}
        gameViewModel.enableSpeakerObservable().observe(this){}
        gameViewModel.isBroadcasterObservable().observe(this){}
        gameViewModel.exitGameObservable().observe(this){
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        mReCreateScene = true
        //just for call setRequestedOrientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        super.onNewIntent(intent)
        mTextureView?.let {
            gameViewModel.maybeCreateScene(this@MChatGameActivity, roomId, it)
        }
    }

    private fun requestPermission() {
        val perms = arrayOf(Manifest.permission.RECORD_AUDIO)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            onPermissionGrant()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(PermissionRequest.Builder(this, RC_PERMISSIONS, *perms).build())
        }
    }

    private fun initUnityView() {
        mTextureView = TextureView(this).also {
            it.surfaceTextureListener = object : SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
                    gameViewModel.createScene(this@MChatGameActivity, roomId, it)
                }

                override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, i: Int, i1: Int) {
                    mSurfaceSizeChange = true
                    gameViewModel.maybeCreateScene(this@MChatGameActivity, roomId, it)
                }

                override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                    return false
                }

                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
            }
            binding.unityContainer.removeAllViews()
            val layoutParams =
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            binding.unityContainer.addView(it, layoutParams)
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun onPermissionGrant() {
        gameViewModel.initMChatScene()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        LogTools.d("onPermissionsGranted requestCode$requestCode $perms")
        if (requestCode == RC_PERMISSIONS) {
            onPermissionGrant()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        LogTools.d("onPermissionsDenied requestCode:$requestCode $perms")
    }

    override fun onRationaleAccepted(requestCode: Int) {
        LogTools.d("onRationaleAccepted requestCode:$requestCode")
        if (requestCode == RC_PERMISSIONS) {
            onPermissionGrant()
        }
    }

    override fun onRationaleDenied(requestCode: Int) {
        LogTools.d("onRationaleDenied requestCode:$requestCode")
    }

    override fun onResume() {
        super.onResume()
        if (MChatContext.instance().isInScene()) {
            MChatContext.instance().resumeMedia()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}