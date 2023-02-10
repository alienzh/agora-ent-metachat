package io.agora.metachat.game.npc

import android.content.Context
import io.agora.metachat.game.MChatContext
import io.agora.metachat.tools.FileResultListener
import io.agora.metachat.tools.FileUtil
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools
import java.io.File

/**
 * @author create by zhangwei03
 */
class MchatNpc constructor(
    private val context: Context,
    private val metaChatContext: MChatContext,
    private val id: Int,
    private val sourceName: String,
    private val npcListener: NpcListener
) {

    private var player: MChatLocalSourceMediaPlayer? = null

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