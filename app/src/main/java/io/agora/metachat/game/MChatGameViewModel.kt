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
    private val _enableMic = SingleLiveData<Boolean>()
    private val _enableSpeaker = SingleLiveData<Boolean>()
    private val _isBroadcaster = SingleLiveData<Boolean>()
    private val _exitGame = SingleLiveData<Boolean>()

    fun isEnterSceneObservable(): LiveData<Boolean> = _isEnterScene
    fun enableMicObservable(): LiveData<Boolean> = _enableMic
    fun enableSpeakerObservable(): LiveData<Boolean> = _enableSpeaker
    fun isBroadcasterObservable(): LiveData<Boolean> = _isBroadcaster
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
            _enableMic.postValue(true)
            _enableSpeaker.postValue(true)
            _isBroadcaster.postValue(true)
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


}