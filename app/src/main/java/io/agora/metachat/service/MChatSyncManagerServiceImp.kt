package io.agora.metachat.service

import android.content.Context
import kotlin.random.Random

/**
 * @author create by zhangwei03
 */
class MChatSyncManagerServiceImp constructor(
    context: Context,
    errorHandler: ((Exception?) -> Unit)
) : MChatServiceProtocol {

    @Volatile
    private var syncUtilsInit = false

    override fun fetchRoomList(completion: (error: Int, list: List<MChatRoomModel>?) -> Unit) {
        // todo test
        val listSize = Random.nextInt(50)
        val list = mutableListOf<MChatRoomModel>()
        for (i in 0 until listSize) {
            val roomModel = MChatRoomModel(
                roomName = "Chat$i",
                roomId = Random.nextInt(10000).toString(),
                roomMembers = Random.nextInt(100),
                createdAt = System.currentTimeMillis(),
                roomCoverIndex = Random.nextInt(4),
                roomPassword = if (i % 2 == 0) "1234" else "",
                isPrivate = i % 2 == 0,
            )
            list.add(roomModel)
        }
        completion.invoke(MChatServiceProtocol.ERR_OK, list)
    }

    override fun createRoom(
        inputModel: MChatCreateRoomInputModel,
        completion: (error: Int, result: MChatCreateRoomOutputModel) -> Unit
    ) {

    }

    override fun joinRoom(
        inputModel: MChatJoinRoomInputModel,
        completion: (error: Int, result: MChatJoinRoomOutputModel?) -> Unit
    ) {

    }

    override fun leaveRoom(completion: (error: Int) -> Unit) {
    }

    override fun muteLocal(completion: (error: Int) -> Unit) {
    }

    override fun unMuteLocal(completion: (error: Int) -> Unit) {
    }

    override fun startMic(completion: (error: Int) -> Unit) {
    }

    override fun leaveMic(completion: (error: Int) -> Unit) {
    }
}