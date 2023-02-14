package io.agora.metachat.game.sence.karaoke

import android.content.Context
import io.agora.metachat.R
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.game.model.MusicDetail
import io.agora.metachat.game.sence.MChatMediaPlayerListener
import io.agora.rtc2.Constants

/**
 * @author create by zhangwei03
 */
class MChatKaraokeManager constructor(val chatContext: MChatContext) {

    companion object {
        // 歌曲类别
        val songTypeList: List<MChatSongType> by lazy {
            MChatKaraokeConstructor.buildSongType()
        }

        // 歌曲详情集合，按类别
        val songListMap: Map<MChatSongType, List<MusicDetail>> by lazy {
            MChatKaraokeConstructor.buildAllSongMap()
        }
    }

    // 是否打开原唱
    var useOriginal: Boolean = true

    // 是否打开耳返
    var enableEarMonitor: Boolean = false

    // 升降调
    var pitchValue: Int = 0

    // k歌音量
    var recordingSignalVolume: Int = 100

    // 伴奏音量
    var accompanimentVolume: Int = 100

    // 音效
    var effect: Int = Constants.ROOM_ACOUSTICS_STUDIO

    // 当前选择的歌曲
    val songListPlaylist by lazy {
        mutableListOf<MusicDetail>()
    }

    // 在播放中的歌曲
    private val playSongCode: Long = -1

    private val chatMediaPlayerListener = object : MChatMediaPlayerListener{
        override fun onPlayCompleted(url: String) {
            removeIfPlaying(url)
            findAndPlayFirstMusic()
        }
    }

    init {
        chatContext.chatMediaPlayer()?.registerListener(chatMediaPlayerListener)
    }

    // 加入播放列表
    @Synchronized
    fun addPlayList(musicDetail: MusicDetail, index: Int = -1) {
        if (index < 0 || index == songListPlaylist.size) {
            songListPlaylist.add(musicDetail)
        } else if (index < songListPlaylist.size) {
            songListPlaylist.add(index, musicDetail)
        }
        findAndPlayFirstMusic()
    }

    // 移除播放列表
    @Synchronized
    fun deletePlaylist(musicDetail: MusicDetail, resetPlay: Boolean) {
        var target = -1
        songListPlaylist.forEachIndexed { index, detail ->
            if (detail.songCode == musicDetail.songCode) {
                target = index
            }
        }

        if (target == -1) return
        songListPlaylist.removeAt(target)
        if (resetPlay) {
            findAndPlayFirstMusic()
        }
    }

    @Synchronized
    private fun removeIfPlaying(url: String) {
        if (songListPlaylist.isNotEmpty()) {
            val music = songListPlaylist[0]
            if (music.mvUrl == url) {
                songListPlaylist.removeAt(0)
            }
        }
    }

    // 找到第一首可以播放的歌曲
    private fun findAndPlayFirstMusic() {
        if (songListPlaylist.isNotEmpty()) {
            val song = songListPlaylist[0]
            if (playSongCode != song.songCode) {
                chatContext.chatMediaPlayer()?.switchPlayKaraoke(song.mvUrl)
            }
        } else {
            chatContext.chatMediaPlayer()?.switchPlayAdvertise()
        }
    }

    @Synchronized
    fun startKaraoke() {
        chatContext.chatMediaPlayer()?.startInChargeOfKaraoke()
    }

    @Synchronized
    fun stopKaraoke() {
        chatContext.chatMediaPlayer()?.stopInChargeOfKaraoke()
    }

    /**
     * 打开原唱
     */
    fun setUseOriginal(original: Boolean, forced: Boolean = false): Boolean {
        var result = false
        if (forced || this.useOriginal != original) {
            chatContext.chatMediaPlayer()?.userOriginalTrack(original)?.also {
                if (Constants.ERR_OK == it) {
                    this.useOriginal = original
                    result = true
                }
            }
        }
        return result
    }

    /**
     * 打开耳返
     */
    fun enableInEarMonitoring(earMonitor: Boolean, forced: Boolean = false): Boolean {
        var result = false
        if (forced || this.enableEarMonitor != earMonitor) {
            chatContext.rtcEngine()?.enableInEarMonitoring(earMonitor)?.also {
                if (Constants.ERR_OK == it) {
                    this.enableEarMonitor = earMonitor
                    result = true
                }
            }
        }
        return result
    }

    /**
     * 设置升降调
     */
    fun setAudioPitch(pitch: Int, forced: Boolean = false): Boolean {
        var result = false
        if (forced || this.pitchValue != pitch) {
            chatContext.chatMediaPlayer()?.setPlayerAudioPitch(pitch)?.also {
                if (Constants.ERR_OK == it) {
                    this.pitchValue = pitch
                    result = true
                }
            }
        }
        return result
    }

    /**
     * 调节音频采集信号音量。
     */
    fun adjustRecordingSignalVolume(value: Int, forced: Boolean = false): Boolean {
        var result = false
        if (forced || this.recordingSignalVolume != value) {
            chatContext.rtcEngine()?.adjustRecordingSignalVolume(value)?.also {
                if (Constants.ERR_OK == it) {
                    this.recordingSignalVolume = value
                    result = true
                }
            }
        }
        return result
    }

    /**
     * 调整伴奏音量
     */
    fun adjustAccompanyVolume(value: Int, forced: Boolean = false): Boolean {
        var result = false
        if (forced || this.accompanimentVolume != value) {
            chatContext.chatMediaPlayer()?.setPlayerVolume(value)?.also {
                if (Constants.ERR_OK == it) {
                    this.accompanimentVolume = value
                    result = true
                }
            }
        }
        return result
    }

    /**
     * 设置音效
     */
    fun setEffect(effect: MChatAudioEffect, forced: Boolean = false): Boolean {
        var result = false
        if (forced || this.effect != effect.value) {
            chatContext.rtcEngine()?.setAudioEffectPreset(effect.value)?.also {
                if (Constants.ERR_OK == it) {
                    this.effect = effect.value
                    result = true
                }
            }

        }
        return result
    }

    fun registerListener(karaokeListener: MChatMediaPlayerListener) {
        chatContext.chatMediaPlayer()?.registerListener(karaokeListener)
    }

    fun unregisterListener(karaokeListener: MChatMediaPlayerListener) {
        chatContext.chatMediaPlayer()?.unregisterListener(karaokeListener)
    }
}

enum class MChatSongType {
    All, TikTok, Hot, Ktv, Christmas;

    fun toTypeString(context: Context): String {
        return when (this) {
            TikTok -> context.getString(R.string.mchat_tiktok_hot_songs)
            Hot -> context.getString(R.string.mchat_recommend_songs)
            Ktv -> context.getString(R.string.mchat_ktv_popular_songs)
            Christmas -> context.getString(R.string.mchat_christmas_songs)
            else -> context.getString(R.string.mchat_all_songs)
        }
    }
}

enum class MChatAudioEffect constructor(val value: Int) {
    None(Constants.AUDIO_EFFECT_OFF),
    Studio(Constants.ROOM_ACOUSTICS_STUDIO),
    Concert(Constants.ROOM_ACOUSTICS_VOCAL_CONCERT),
    KTV(Constants.ROOM_ACOUSTICS_KTV),
    Spatial(Constants.ROOM_ACOUSTICS_SPACIAL);

    companion object {
        fun fromValue(value: Int): MChatAudioEffect {
            return when (value) {
                Studio.value -> Studio
                Concert.value -> Concert
                KTV.value -> KTV
                Spatial.value -> Spatial
                else -> None
            }
        }
    }
}