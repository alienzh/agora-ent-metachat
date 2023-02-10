package io.agora.metachat.game

import io.agora.metachat.IMetachatScene
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.GsonTools
import io.agora.metachat.tools.LogTools

class MChatUnityCmd constructor(private val scene: IMetachatScene) {
    private val tag = "MChatUnityCmd"
    private val messageListeners: MutableSet<SceneCmdListener> = mutableSetOf()

    fun acquireObjectPosition(objectId: Int) {
        val obj = SceneMessageRequestBody(SceneMessageType.Position.value)
        obj.params["id"] = objectId
        sendSceneMessage(GsonTools.beanToString(obj))
    }

    fun startKaraoke() {
        val obj = SceneMessageRequestBody(SceneMessageType.StartKaraoke.value)
        sendSceneMessage(GsonTools.beanToString(obj))
    }

    fun stopKaraoke() {
        val obj = SceneMessageRequestBody(SceneMessageType.StopKaraoke.value)
        sendSceneMessage(GsonTools.beanToString(obj))
    }

    fun sendMessage(message: String) {
        val obj = SceneMessageRequestBody(SceneMessageType.SendMessage.value)
        obj.params["userId"] = MChatKeyCenter.imUid
        obj.params["content"] = message
        sendSceneMessage(GsonTools.beanToString(obj))
    }

    fun updateUserInfo() {
        val obj = SceneMessageRequestBody(SceneMessageType.ChangeUserInfo.value)
        obj.params["userId"] = MChatKeyCenter.imUid
        obj.params["badge"] = MChatConstant.getBadgeUrl(MChatKeyCenter.badgeIndex)
        obj.params["name"] = MChatKeyCenter.nickname
        sendSceneMessage(GsonTools.beanToString(obj))
    }

    fun changeLanguage(){
        val obj = SceneMessageRequestBody(SceneMessageType.ChangeUserInfo.value)
        obj.params["lang"] = DeviceTools.getLanguageCode()
        sendSceneMessage(GsonTools.beanToString(obj))
    }

    private fun sendSceneMessage(msg: String?) {
        if (scene.sendMessageToScene(msg?.toByteArray()) == 0) {
            LogTools.d(tag, "sendSceneMessage done, $msg")
        } else {
            LogTools.e(tag, "sendSceneMessage fail, $msg")
        }
    }

    fun handleSceneMessage(message: String) {
        LogTools.d(tag,"ready to handle scene message, $message")
        GsonTools.toBean(message, SceneMessageReceiveBaseBody::class.java)?.let { obj ->
            GsonTools.toBean(obj.message, SceneMessageReceiveBody::class.java)?.let { body ->
                when (body.type) {
                    SceneMessageType.Position.value -> {
                        val dataStr = GsonTools.beanToString(body.data)
                        GsonTools.toBean(dataStr, SceneMessageReceivedObjectPositions::class.java)?.let { positions ->
                            messageListeners.forEach {
                                it.onObjectPositionAcquired(positions)
                            }
                        }
                    }
                    SceneMessageType.StartKaraoke.value -> {
                        messageListeners.forEach {
                            it.onKaraokeStarted()
                        }
                    }
                    SceneMessageType.StopKaraoke.value -> {
                        messageListeners.forEach {
                            stopKaraoke()
                            it.onKaraokeStopped()
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }

    fun registerListener(listener: SceneCmdListener) {
        messageListeners.add(listener)
    }

    fun unregisterListener(listener: SceneCmdListener) {
        messageListeners.remove(listener)
    }
}

enum class SceneMessageType(val value: Int) {
    Position(1),
    StartKaraoke(2),
    StopKaraoke(3),
    ChangeUserInfo(4),
    SendMessage(5),
    Language(6)
}

data class SceneManagerBodyCoordinate(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

data class SceneMessageRequestBody(
    val type: Int,
    val params: MutableMap<String, Any> = mutableMapOf()
)

data class SceneMessageReceiveBaseBody(
    val messageType: Int = 0,
    val message: String
)

data class SceneMessageReceiveBody(
    val type: Int,
    val data: Any
)

data class SceneMessageReceivedObjectPositions(
    val id: Int,
    val position: SceneManagerBodyCoordinate,
    val forward: SceneManagerBodyCoordinate,
    val right: SceneManagerBodyCoordinate,
    val up: SceneManagerBodyCoordinate,
)

enum class SceneObjectId(val value: Int) {
    TV(1),
    NPC1(2),
    NPC2(3),
    NPC3(4),
}

interface SceneCmdListener {
    fun onObjectPositionAcquired(position: SceneMessageReceivedObjectPositions)

    fun onKaraokeStarted()

    fun onKaraokeStopped()
}