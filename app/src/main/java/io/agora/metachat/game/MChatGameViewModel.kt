package io.agora.metachat.game

import android.app.Activity
import android.view.TextureView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.metachat.*
import io.agora.metachat.game.internal.MChatBaseEventHandler
import io.agora.metachat.game.internal.MChatBaseSceneEventHandler
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.SingleLiveData
import io.agora.metachat.tools.ThreadTools
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler.ErrorCode

/**
 * @author create by zhangwei03
 */
class MChatGameViewModel : ViewModel() {

    private val _isEnterScene = SingleLiveData<Boolean>()
    private val _onlineMic = SingleLiveData<Boolean>()
    private val _muteRemote = SingleLiveData<Boolean>()
    private val _muteLocal = SingleLiveData<Boolean>()
    private val _exitGame = SingleLiveData<Boolean>()

    fun isEnterSceneObservable(): LiveData<Boolean> = _isEnterScene
    fun onlineMicObservable(): LiveData<Boolean> = _onlineMic
    fun muteRemoteObservable(): LiveData<Boolean> = _muteRemote
    fun muteLocalObservable(): LiveData<Boolean> = _muteLocal
    fun exitGameObservable(): LiveData<Boolean> = _exitGame

    private var mReCreateScene = false
    private var mSurfaceSizeChange = false

    private val mchatContext by lazy {
        MChatContext.instance()
    }

    override fun onCleared() {
        mchatContext.unregisterMetaChatEventHandler(mChatEventHandler)
        mchatContext.unregisterMetaChatSceneEventHandler(mChatSceneEventHandler)
        super.onCleared()
    }

    private val mChatEventHandler = object :MChatBaseEventHandler(){
        override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {
            ThreadTools.get().runOnMainThread {
                mchatContext.enterScene()
            }
        }
    }

    private val mChatSceneEventHandler = object : MChatBaseSceneEventHandler(){
        override fun onEnterSceneResult(errorCode: Int) {
            ThreadTools.get().runOnMainThread {
                if (errorCode != ErrorCode.ERR_OK) {
                    LogTools.e("enter scene failed:$errorCode")
                    return@runOnMainThread
                }
                _isEnterScene.postValue(true)
                _onlineMic.postValue(true)
                _muteRemote.postValue(true)
                _muteLocal.postValue(true)
                resetSceneState()
            }
        }

        override fun onLeaveSceneResult(errorCode: Int) {
            ThreadTools.get().runOnMainThread {
                _isEnterScene.postValue(false)
                _exitGame.postValue(true)
            }
        }

        override fun onReleasedScene(status: Int) {
            if (status == ErrorCode.ERR_OK) {
                mchatContext.destroy()
            }
        }

    }

    fun initMChatScene() {
        mchatContext.registerMetaChatEventHandler(mChatEventHandler)
        mchatContext.registerMetaChatSceneEventHandler(mChatSceneEventHandler)
    }

    fun createScene(activity: Activity, roomId: String, tv: TextureView) {
        resetSceneState()
        mchatContext.createScene(activity, roomId, tv)
    }

    fun resetSceneState() {
        mReCreateScene = false
        mSurfaceSizeChange = false
    }

    fun maybeCreateScene(activity: Activity, roomId: String, tv: TextureView): Boolean {
        if (mReCreateScene && mSurfaceSizeChange) {
            createScene(activity, roomId, tv)
            return true
        }
        return false
    }

    // 发送上下麦
    fun sendOnlineEvent() {
        if (_onlineMic.value == true) {
            val result = mchatContext.updateRole(Constants.CLIENT_ROLE_AUDIENCE)
            if (result) {
                _onlineMic.postValue(false)
            } else {
                LogTools.e("offline Mic error")
            }
        } else {
            val result = mchatContext.updateRole(Constants.CLIENT_ROLE_BROADCASTER)
            if (result) {
                _onlineMic.postValue(true)
            } else {
                LogTools.e("online Mic error")
            }
        }
    }

    // 远端静音
    fun sendMuteRemoteEvent() {
        if (_muteRemote.value == true) {
            val result = mchatContext.muteAllRemoteAudioStreams(false)
            if (result) {
                _muteRemote.postValue(false)
            } else {
                LogTools.e("unMute remote error")
            }
        } else {
            val result = mchatContext.muteAllRemoteAudioStreams(true)
            if (result) {
                _muteRemote.postValue(true)
            } else {
                LogTools.e("mute remote error")
            }
        }
    }

    // 本地静音
    fun sendMuteLocalEvent() {
        if (_muteLocal.value == true) {
            val result = mchatContext.enableLocalAudio(false)
            if (result) {
                _muteLocal.postValue(false)
            } else {
                LogTools.e("unMute local error")
            }
        } else {
            val result = mchatContext.enableLocalAudio(true)
            if (result) {
                _muteLocal.postValue(true)
            } else {
                LogTools.e("mute local error")
            }
        }
    }
}