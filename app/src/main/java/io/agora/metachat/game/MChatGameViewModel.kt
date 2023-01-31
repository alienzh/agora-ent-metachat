package io.agora.metachat.game

import android.app.Activity
import android.view.TextureView
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.metachat.*
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.SingleLiveData
import io.agora.metachat.tools.ThreadTools
import io.agora.rtc2.IRtcEngineEventHandler.ErrorCode

/**
 * @author create by zhangwei03
 */
class MChatGameViewModel : ViewModel(), IMetachatSceneEventHandler, IMetachatEventHandler {

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

    override fun onCleared() {
        MChatContext.instance().unregisterMetaChatEventHandler(this)
        MChatContext.instance().unregisterMetaChatSceneEventHandler(this)
        super.onCleared()
    }

    fun initMChatScene() {
        MChatContext.instance().registerMetaChatSceneEventHandler(this)
        MChatContext.instance().registerMetaChatEventHandler(this)
    }

    fun createScene(activity: Activity, roomId: String, tv: TextureView) {
        resetSceneState()
        MChatContext.instance().createScene(activity, roomId, tv)
    }

    fun resetSceneState() {
        mReCreateScene = false
        mSurfaceSizeChange = false
    }

    fun maybeCreateScene(activity: Activity, roomId: String, tv: TextureView) {
        if (mReCreateScene && mSurfaceSizeChange) {
            createScene(activity, roomId, tv)
        }
    }

    override fun onEnterSceneResult(errorCode: Int) {
        LogTools.d("enter scene failed:$errorCode")
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
            _isEnterScene.postValue(true)
            _exitGame.postValue(true)
        }
    }

    override fun onRecvMessageFromScene(message: ByteArray?) {}

    override fun onUserPositionChanged(uid: String?, posInfo: MetachatUserPositionInfo?) {}

    override fun onEnumerateVideoDisplaysResult(displayIds: Array<out String>?) {}

    override fun onReleasedScene(status: Int) {
        if (status == ErrorCode.ERR_OK) {
            MChatContext.instance().destroy()
        }
    }

    override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {
        ThreadTools.get().runOnMainThread {
            MChatContext.instance().enterScene()
        }
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {}

    override fun onRequestToken() {}

    override fun onGetSceneInfosResult(sceneInfos: Array<out MetachatSceneInfo>?, errorCode: Int) {}

    override fun onDownloadSceneProgress(sceneId: Long, progress: Int, state: Int) {}

    // 发送上下麦
    fun sendOnlineEvent() {
        if (_onlineMic.value == true) {
            _onlineMic.postValue(false)
        } else {
            _onlineMic.postValue(true)
        }
    }

    // 远端静音
    fun sendMuteRemoteEvent() {
        if (_muteRemote.value == true) {
            _muteRemote.postValue(false)
        } else {
            _muteRemote.postValue(true)
        }
    }

    // 本地静音
    fun sendMuteLocalEvent() {
        if (_muteLocal.value == true) {
            _muteLocal.postValue(false)
        } else {
            _muteLocal.postValue(true)
        }
    }

    // 退出房间
    fun sendExitRoomEvent() {

    }
}