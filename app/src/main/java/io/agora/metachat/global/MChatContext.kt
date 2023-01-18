package io.agora.metachat.global

import io.agora.base.VideoFrame
import io.agora.metachat.*

/**
 * @author create by zhangwei03
 */
class MChatContext: IMetachatEventHandler, IMetachatSceneEventHandler, MChatAgoraMediaPlayer.OnMediaVideoFramePushListener {
    override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
    }

    override fun onRequestToken() {
    }

    override fun onGetSceneInfosResult(sceneInfos: Array<out MetachatSceneInfo>?, errorCode: Int) {
    }

    override fun onDownloadSceneProgress(sceneId: Long, progress: Int, state: Int) {
    }

    override fun onEnterSceneResult(errorCode: Int) {
    }

    override fun onLeaveSceneResult(errorCode: Int) {
    }

    override fun onRecvMessageFromScene(message: ByteArray?) {
    }

    override fun onUserPositionChanged(uid: String?, posInfo: MetachatUserPositionInfo?) {
    }

    override fun onEnumerateVideoDisplaysResult(displayIds: Array<out String>?) {
    }

    override fun onReleasedScene(status: Int) {
    }

    override fun onMediaVideoFramePushed(frame: VideoFrame?) {
    }
}