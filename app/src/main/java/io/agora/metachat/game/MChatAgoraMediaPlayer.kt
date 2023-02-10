package io.agora.metachat.game

import android.text.TextUtils
import io.agora.base.VideoFrame
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.mediaplayer.IMediaPlayerVideoFrameObserver
import io.agora.metachat.game.internal.MChatBaseMediaPlayerObserver
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.tools.LogTools
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.RtcEngine

/**
 * @author create by zhangwei03
 */
class MChatAgoraMediaPlayer constructor(val rtcEngine: RtcEngine, val mediaPlayer: IMediaPlayer) {

    companion object {

        private const val TAG = "MChatAgoraMediaPlayer"
    }

    // 媒体播放器的视频数据观测器。
    private var mediaVideoFramePushListener: IMediaPlayerVideoFrameObserver? = null

    // 是否在k歌中
    private var inChargeOfKaraoke = false

    private val mediaPlayerObserver = object : MChatBaseMediaPlayerObserver() {
        override fun onPlayerStateChanged(state: Constants.MediaPlayerState, error: Constants.MediaPlayerError) {
            LogTools.d(TAG, "onPlayerStateChanged state:$state,error:$error")
            if (Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED == state) {
                if (mediaPlayer.play() != io.agora.rtc2.Constants.ERR_OK) {
                    LogTools.d(TAG, "onPlayerStateChanged play success")
                }
            }
        }
    }

    // 媒体播放器的视频数据观测器
    private val mediaPlayerVideoFrameObserver = object : IMediaPlayerVideoFrameObserver {
        override fun onFrame(frame: VideoFrame?) {
            mediaVideoFramePushListener?.onFrame(frame)
        }
    }


    fun initMediaPlayer(volume: Int) {
        setPlayerVolume(volume)
        mediaPlayer.registerPlayerObserver(mediaPlayerObserver)
        mediaPlayer.registerVideoFrameObserver(mediaPlayerVideoFrameObserver)
    }

    fun setOnMediaVideoFramePushListener(mediaVideoFramePushListener: IMediaPlayerVideoFrameObserver) = apply {
        this.mediaVideoFramePushListener = mediaVideoFramePushListener
    }

    fun mediaPlayerId(): Int = mediaPlayer.mediaPlayerId

    fun play(url: String, startPos: Long = 0) {
        mediaPlayer.open(url, startPos).also {
            if (it == io.agora.rtc2.Constants.ERR_OK) {
                mediaPlayer.setLoopCount(MChatConstant.PLAY_ADVERTISING_VIDEO_REPEAT)
                mediaPlayer.play()
                if (!TextUtils.equals(url, MChatConstant.VIDEO_URL)) { // k歌推送视频流
                    val channelOptions = ChannelMediaOptions().apply {
                        autoSubscribeAudio = true
                        autoSubscribeVideo = true
                        autoSubscribeVideo = true
                        publishMediaPlayerId = mediaPlayer.mediaPlayerId
                        publishMediaPlayerAudioTrack = true
                    }
                    rtcEngine.updateChannelMediaOptions(channelOptions)
                }
            }
        }
    }

    fun stop() {
        mediaPlayer.registerVideoFrameObserver(null)
        mediaPlayer.unRegisterPlayerObserver(mediaPlayerObserver)
        mediaPlayer.stop()
        mediaPlayer.destroy()
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
        val playerAudioPitch = if (pitch < -12) {
            -12
        } else if (pitch > 12) {
            12
        } else {
            pitch
        }
        return mediaPlayer.setAudioPitch(playerAudioPitch)
    }

    @Synchronized
    fun destroy(): Int {
        mediaPlayer.unRegisterPlayerObserver(mediaPlayerObserver)
        return mediaPlayer.destroy()
    }

    @Synchronized
    fun startInChargeOfKaraoke() {
        inChargeOfKaraoke = true
    }

    @Synchronized
    fun stopInChargeOfKaraoke() {
        inChargeOfKaraoke = false
    }

    @Synchronized
    fun inChargeOfKaraoke():Boolean = false
}