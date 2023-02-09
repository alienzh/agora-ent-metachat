package io.agora.metachat.game.npc

import android.content.Context
import io.agora.mediaplayer.Constants
import io.agora.mediaplayer.IMediaPlayer
import io.agora.metachat.game.MChatContext
import io.agora.metachat.game.SceneObjectId
import io.agora.metachat.game.internal.MChatBaseMediaPlayerObserver
import io.agora.metachat.tools.FileResultListener
import io.agora.metachat.tools.FileUtil
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools
import java.io.File

/**
 * @author create by zhangwei03
 */
class MChatNpcManager constructor() {

    companion object {
        const val TAG = "MChatNpcManager"
    }

    private lateinit var npc1: Npc
    private lateinit var npc2: Npc
    private lateinit var npc3: Npc


    fun initNpcMediaPlayer(context: Context, chatContext: MChatContext, npcListener: NpcListener) {
        npc1 = Npc(context, chatContext, SceneObjectId.NPC1.value, "npc_id_2.m4a", npcListener)
        npc2 = Npc(context, chatContext, SceneObjectId.NPC2.value, "npc_id_3.m4a", npcListener)
        npc3 = Npc(context, chatContext, SceneObjectId.NPC3.value, "npc_id_4.m4a", npcListener)
    }

    fun getNpc(id: Int): Npc? {
        return when (id) {
            SceneObjectId.NPC1.value -> npc1
            SceneObjectId.NPC2.value -> npc2
            SceneObjectId.NPC3.value -> npc3
            else -> null
        }
    }

    //圆桌npc
    fun roundTableNpc():Npc{
        return npc2
    }

    fun play(id: Int) {
        when (id) {
            SceneObjectId.NPC1.value -> {
                npc1.play()
            }
            SceneObjectId.NPC2.value -> {
                npc2.play()
            }
            SceneObjectId.NPC3.value -> {
                npc3.play()
            }
        }
    }

    fun stop(id: Int) {
        when (id) {
            SceneObjectId.NPC1.value -> {
                npc1.stop()
            }
            SceneObjectId.NPC2.value -> {
                npc2.stop()
            }
            SceneObjectId.NPC3.value -> {
                npc3.stop()
            }
        }
    }

    fun stopAll() {
        npc1.stop()
        npc2.stop()
        npc3.stop()
    }

    fun destroy() {
        npc1.destroy()
        npc2.destroy()
        npc3.destroy()
    }

    inner class Npc constructor(
        private val context: Context,
        private val metaChatContext: MChatContext,
        private val id: Int,
        private val sourceName: String,
        private val npcListener: NpcListener
    ) {

        private var player: LocalSourceMediaPlayer? = null

        init {
            ThreadTools.get().runOnIOThread {
                FileUtil.copyAssetFile(context, sourceName, false,
                    object : FileResultListener {
                        override fun onSuccess() {
                            player = metaChatContext.createLocalSourcePlayer(id, getSourceFilePath(sourceName))
                            npcListener.onNpcReady(id, getSourceFilePath(sourceName))
                            LogTools.d(MChatNpcManager.TAG, "npc source $sourceName copy success")
                        }

                        override fun onFail() {
                            npcListener.onNpcFail()
                            LogTools.e(MChatNpcManager.TAG, "npc source $sourceName copy failed")
                        }
                    })
            }
        }

        fun getSourceFilePath(name: String): String {
            return File(context.filesDir, name).absolutePath
        }

        fun playerId(): Int {
            return player?.playerId() ?: -1
        }

        fun play() {
            player?.play(true)
        }

        fun stop() {
            player?.stop()
        }

        fun destroy() {
            player?.destroy()
        }

        fun setPlayerVolume(volume: Int) {
            player?.setPlayerVolume(volume)
        }
    }
}

interface NpcListener {
    fun onNpcReady(id: Int, sourceName: String)

    fun onNpcFail()
}


class LocalSourceMediaPlayer(
    private val sourceId: Int,
    private val mediaPlayer: IMediaPlayer,
    private val sourceUrl: String
) {

    private val playerObserver = object : MChatBaseMediaPlayerObserver() {
        override fun onPlayerStateChanged(
            state: Constants.MediaPlayerState,
            error: Constants.MediaPlayerError
        ) {
            if (state == Constants.MediaPlayerState.PLAYER_STATE_OPEN_COMPLETED) {
                mediaPlayer.mute(true)
                mediaPlayer.play()
            }
        }
    }

    init {
        mediaPlayer.registerPlayerObserver(playerObserver)
    }

    fun sourceId(): Int {
        return sourceId
    }

    fun playerId(): Int {
        return mediaPlayer.mediaPlayerId
    }

    fun play(loop: Boolean) {
        mediaPlayer.setLoopCount(if (loop) -1 else 0)
        mediaPlayer.open(sourceUrl, 0)
    }

    fun stop() {
        mediaPlayer.stop()
    }

    fun destroy() {
        mediaPlayer.unRegisterPlayerObserver(playerObserver)
        mediaPlayer.destroy()
    }

    fun setPlayerVolume(volume: Int): Int {
        return mediaPlayer.adjustPlayoutVolume(volume)
    }
}