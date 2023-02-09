package io.agora.metachat.game

import io.agora.metachat.MetachatUserPositionInfo
import io.agora.metachat.game.internal.MChatBaseSceneEventHandler
import io.agora.metachat.global.MChatKeyCenter
import io.agora.spatialaudio.ILocalSpatialAudioEngine
import io.agora.spatialaudio.RemoteVoicePositionInfo

/**
 * @author create by zhangwei03
 */
class MChatSpatialAudio constructor(
    private val localUid: Int,
    private val spatialAudioEngine: ILocalSpatialAudioEngine
) {

    private val chatContext by lazy {
        MChatContext.instance()
    }

    private val mChatSceneEventHandler = object : MChatBaseSceneEventHandler() {
        override fun onUserPositionChanged(uid: String, posInfo: MetachatUserPositionInfo) {
            val userId = uid.toIntOrNull() ?: -1
            if (MChatKeyCenter.curUid == userId) {
                spatialAudioEngine.updateSelfPosition(posInfo.mPosition, posInfo.mForward, posInfo.mRight, posInfo.mUp)
            } else {
                spatialAudioEngine.updateRemotePosition(userId, RemoteVoicePositionInfo().apply {
                    position = posInfo.mPosition
                    forward = posInfo.mForward
                })
            }
        }
    }

    fun initSpatialAudio() {
        chatContext.registerMetaChatSceneEventHandler(mChatSceneEventHandler)
        spatialAudioEngine.muteLocalAudioStream(false)
        spatialAudioEngine.muteAllRemoteAudioStreams(false)
    }

    fun destroy() {
        chatContext.unregisterMetaChatSceneEventHandler(mChatSceneEventHandler)
        ILocalSpatialAudioEngine.destroy()
    }

    fun updateLocalMediaPlayerPosition(id: Int, position: FloatArray, forward: FloatArray) {
        val info = RemoteVoicePositionInfo()
        info.position = position
        info.forward = forward
        spatialAudioEngine.updatePlayerPositionInfo(id, info)
    }

    fun removeRemotePosition(uid: Int): Int {
        return spatialAudioEngine.removeRemotePosition(uid)
    }

    fun muteAllRemoteAudioStreams(mute: Boolean): Int {
        return spatialAudioEngine.muteAllRemoteAudioStreams(mute)
    }

    // 音效距离
    fun setAudioRecvRange(range:Float){
        spatialAudioEngine.setAudioRecvRange(range)
    }

    // 衰减系数
    fun setDistanceUnit(unit:Float){
        spatialAudioEngine.setDistanceUnit(unit)
    }
}