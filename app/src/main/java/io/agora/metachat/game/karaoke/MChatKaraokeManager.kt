package io.agora.metachat.game.karaoke

import android.content.Context
import io.agora.metachat.R
import io.agora.metachat.game.MChatContext
import io.agora.metachat.game.model.MusicDetail
import io.agora.rtc2.Constants

/**
 * @author create by zhangwei03
 */
class MChatKaraokeManager constructor() {

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

    private val chatContext by lazy {
        MChatContext.instance()
    }

    var useOriginal: Boolean = true
    var earMonitor: Boolean = false
    var pitch: Int = 0
    var voiceVolume: Int = 100
    var playerVolume: Int = 100
    var effect: Int = Constants.ROOM_ACOUSTICS_STUDIO

    // 当前选择的歌曲
    val songListPlaylist by lazy {
        mutableListOf<MusicDetail>()
    }

    private val managerListeners = mutableListOf<MChatKaraokeManagerListener>()

    fun registerListener(listener: MChatKaraokeManagerListener) {
        managerListeners.add(listener)
    }

    fun unregisterListener(listener: MChatKaraokeManagerListener) {
        managerListeners.remove(listener)
    }

    // 在播放中的歌曲
    private val playSongCode: Long = -1

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

    /**
     * @param pitch ranges from -12 to 12
     */
    fun setAudioPitch(pitch: Int, forced: Boolean = false) {
        if (forced || this.pitch != pitch) {
            if (Constants.ERR_OK == chatContext.chatMediaPlayer()?.setPlayerAudioPitch(pitch)) {
                this.pitch = pitch
            }
        }
    }

    private fun findAndPlayFirstMusic() {
        if (songListPlaylist.isNotEmpty()) {
            val song = songListPlaylist[0]
            if (playSongCode != song.songCode) {
                chatContext.chatMediaPlayer()?.play(song.mvUrl)
            }
        } else {
            chatContext.chatMediaPlayer()?.stop()
        }
    }

    fun idleMode() {

    }
}

interface MChatKaraokeManagerListener {

    fun onSongPlayCompleted(name: String, url: String)
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