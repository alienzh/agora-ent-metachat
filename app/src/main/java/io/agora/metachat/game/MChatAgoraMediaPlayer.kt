package io.agora.metachat.game

import io.agora.base.VideoFrame
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerObserver
import io.agora.mediaplayer.IMediaPlayerVideoFrameObserver
import io.agora.mediaplayer.data.PlayerUpdatedInfo
import io.agora.mediaplayer.data.SrcInfo
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.tools.LogTools
import io.agora.rtc2.RtcEngine

/**
 * @author create by zhangwei03
 */
class MChatAgoraMediaPlayer : IMediaPlayerObserver, IMediaPlayerVideoFrameObserver {

    private var mediaPlayer: IMediaPlayer? = null
    private var onMediaVideoFramePushListener: OnMediaVideoFramePushListener? = null

    companion object {

        private const val TAG = "MChatAgoraMediaPlayer"

        fun instance(): MChatAgoraMediaPlayer {
            return agoraMediaPlayer
        }

        private val agoraMediaPlayer by lazy {
            MChatAgoraMediaPlayer()
        }
    }

    private var mediaIsPlaying: Boolean = false
    private var playerAudioPitch: Int = 0
    private var useOriginalTrack: Boolean = true
    private var playerVolume: Int = 100

    fun initMediaPlayer(rtcEngine: RtcEngine) {
        mediaPlayer = rtcEngine.createMediaPlayer()
        mediaPlayer?.registerPlayerObserver(this)
        mediaPlayer?.registerVideoFrameObserver(this)
    }

    fun setOnMediaVideoFramePushListener(onMediaVideoFramePushListener: OnMediaVideoFramePushListener) = apply {
        this.onMediaVideoFramePushListener = onMediaVideoFramePushListener
    }

    fun play(url: String, startPos: Long = 0) {
        mediaPlayer?.open(url, startPos)?.also {
            if (it == io.agora.rtc2.Constants.ERR_OK) {
                mediaPlayer?.setLoopCount(MChatConstant.PLAY_ADVERTISING_VIDEO_REPEAT)
                mediaPlayer?.play()
                mediaIsPlaying = true
            }
        }
    }

    fun stop() {
        mediaPlayer?.let {
            it.registerVideoFrameObserver(null)
            it.unRegisterPlayerObserver(this)
            it.stop()
            it.destroy()
            mediaIsPlaying = false
        }
        mediaPlayer = null
    }

    fun pause() {
        mediaPlayer?.let {
            it.pause()
            it.registerPlayerObserver(null)
        }
    }

    fun resume() {
        mediaPlayer?.let {
            it.resume()
            it.registerVideoFrameObserver(this)
        }
    }

    /**
     * @param pitch ranged from -12 to 12, values from
     * outside of this range will be constrained to
     * -12 or 12 respectively
     */
    @Synchronized
    fun setPlayerAudioPitch(pitch: Int): Int {
        playerAudioPitch = if (pitch < -12) {
            -12
        } else if (pitch > 12) {
            12
        } else {
            pitch
        }

        var result = io.agora.rtc2.Constants.ERR_OK
        if (mediaIsPlaying) {
            result = mediaPlayer?.setAudioPitch(playerAudioPitch) ?: io.agora.rtc2.Constants.ERR_FAILED
        }
        return result
    }

    override fun onPlayerStateChanged(state: Constants.MediaPlayerState?, error: Constants.MediaPlayerError?) {
        LogTools.d(TAG, "onPlayerStateChanged state:$state,error:$error")
        if (Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED == state) {
            if (mediaPlayer?.play() != io.agora.rtc2.Constants.ERR_OK) {
                LogTools.d(TAG, "onPlayerStateChanged play success")
            }
        }
    }

    override fun onPositionChanged(position_ms: Long) {
    }

    override fun onPlayerEvent(eventCode: Constants.MediaPlayerEvent?, elapsedTime: Long, message: String?) {
    }

    override fun onMetaData(type: Constants.MediaPlayerMetadataType?, data: ByteArray?) {
    }

    override fun onPlayBufferUpdated(playCachedBuffer: Long) {
    }

    override fun onPreloadEvent(src: String?, event: Constants.MediaPlayerPreloadEvent?) {
    }

    override fun onAgoraCDNTokenWillExpire() {
    }

    override fun onPlayerSrcInfoChanged(from: SrcInfo?, to: SrcInfo?) {
    }

    override fun onPlayerInfoUpdated(info: PlayerUpdatedInfo?) {
    }

    override fun onAudioVolumeIndication(volume: Int) {
    }

    override fun onFrame(frame: VideoFrame?) {
        onMediaVideoFramePushListener?.onMediaVideoFramePushed(frame)
    }


    interface OnMediaVideoFramePushListener {
        fun onMediaVideoFramePushed(frame: VideoFrame?)
    }
}