package io.agora.metachat.game

import io.agora.base.VideoFrame
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerVideoFrameObserver
import io.agora.metachat.game.internal.MChatBaseMediaPlayerObserver
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.tools.LogTools

/**
 * @author create by zhangwei03
 */
class MChatAgoraMediaPlayer constructor(val mediaPlayer: IMediaPlayer) {

    private var onMediaVideoFramePushListener: IMediaPlayerVideoFrameObserver? = null

    companion object {

        private const val TAG = "MChatAgoraMediaPlayer"
    }

    private var mediaIsPlaying: Boolean = false
    private var playerAudioPitch: Int = 0
    private var useOriginalTrack: Boolean = true

    private val mediaPlayerObserver = object : MChatBaseMediaPlayerObserver() {
        override fun onPlayerStateChanged(state: Constants.MediaPlayerState, error: Constants.MediaPlayerError) {
            LogTools.d(TAG, "onPlayerStateChanged state:$state,error:$error")
            if (Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED == state) {
                if (mediaPlayer.play() != io.agora.rtc2.Constants.ERR_OK) {
                    LogTools.d(TAG, "onPlayerStateChanged play success")
                }
            }
        }

        override fun onPositionChanged(position_ms: Long) {
        }
    }

    private val mediaPlayerVideoFrameObserver = object : IMediaPlayerVideoFrameObserver {
        override fun onFrame(frame: VideoFrame?) {
            onMediaVideoFramePushListener?.onFrame(frame)
        }

    }

    fun initMediaPlayer() {
        mediaPlayer.registerPlayerObserver(mediaPlayerObserver)
        mediaPlayer.registerVideoFrameObserver(mediaPlayerVideoFrameObserver)
    }

    fun setOnMediaVideoFramePushListener(onMediaVideoFramePushListener: IMediaPlayerVideoFrameObserver) = apply {
        this.onMediaVideoFramePushListener = onMediaVideoFramePushListener
    }

    fun mediaPlayerId(): Int {
        return mediaPlayer.mediaPlayerId
    }

    fun play(url: String, startPos: Long = 0) {
        mediaPlayer.open(url, startPos).also {
            if (it == io.agora.rtc2.Constants.ERR_OK) {
                mediaPlayer.setLoopCount(MChatConstant.PLAY_ADVERTISING_VIDEO_REPEAT)
                mediaPlayer.play()
                mediaIsPlaying = true
            }
        }
    }

    fun stop() {
        mediaPlayer.registerVideoFrameObserver(null)
        mediaPlayer.unRegisterPlayerObserver(mediaPlayerObserver)
        mediaPlayer.stop()
        mediaPlayer.destroy()
        mediaIsPlaying = false
    }

    fun pause() {
        mediaPlayer.pause()
        mediaPlayer.registerPlayerObserver(null)
    }

    fun resume() {
        mediaPlayer.resume()
        mediaPlayer.registerVideoFrameObserver(mediaPlayerVideoFrameObserver)
    }

    fun setPlayerVolume(volume: Int): Int {
        return mediaPlayer.adjustPlayoutVolume(volume)
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
            result = mediaPlayer.setAudioPitch(playerAudioPitch)
        }
        return result
    }

    @Synchronized
    fun destroy(): Int {
        mediaPlayer.unRegisterPlayerObserver(mediaPlayerObserver)
        return mediaPlayer.destroy()
    }
}