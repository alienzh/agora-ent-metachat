package io.agora.metachat.game.karaoke

import android.content.Context
import io.agora.metachat.R
import io.agora.metachat.game.MChatAgoraMediaPlayer
import io.agora.metachat.game.MChatContext
import io.agora.metachat.game.model.MusicDetail

/**
 * @author create by zhangwei03
 */
class MChatKaraokeManager constructor(private val chatContext: MChatContext) {

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

    @Synchronized
    fun addPlayList(musicDetail: MusicDetail, index: Int = -1) {
        if (index < 0 || index == songListPlaylist.size) {
            songListPlaylist.add(musicDetail)
        } else if (index < songListPlaylist.size) {
            songListPlaylist.add(index, musicDetail)
        }
        findAndPlayFirstMusic()
    }

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

    private fun findAndPlayFirstMusic() {
        if (songListPlaylist.isNotEmpty()) {
            val song = songListPlaylist[0]
            if (playSongCode != song.songCode) {
                MChatAgoraMediaPlayer.instance().play(song.mvUrl)
            }
        } else {
            MChatAgoraMediaPlayer.instance().stop()
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