package io.agora.metachat.game

import io.agora.metachat.IMetachatScene
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.GsonTools
import io.agora.metachat.tools.LogTools

class MChatUnityCmd constructor(private val scene: IMetachatScene) {
    private val tag = "MChatUnityCmd"
    private val messageListeners: MutableSet<SceneCmdListener> = mutableSetOf()

    // 停止k歌
    fun stopKaraoke() {
        val value = mutableMapOf<String, Any>().apply {
            put("actionId", KaraokeAction.Stop.value)
        }
        GsonTools.beanToString(value)?.let {
            val obj = SceneMessageRequestBody(SceneMessageType.Karaoke.value, it)
            sendSceneMessage(GsonTools.beanToString(obj))
        }
    }

    // 发送消息
    fun sendMessage(message: String) {
        val value = mutableMapOf<String, Any>().apply {
            put("content", message)
        }
        GsonTools.beanToString(value)?.let {
            val obj = SceneMessageRequestBody(SceneMessageType.SendMessage.value, it)
            sendSceneMessage(GsonTools.beanToString(obj))
        }
    }

    // 系统语言
    fun changeLanguage() {
        val value = mutableMapOf<String, Any>().apply {
            put("lang", DeviceTools.getLanguageCode())
        }
        GsonTools.beanToString(value)?.let {
            val obj = SceneMessageRequestBody(SceneMessageType.Language.value, it)
            sendSceneMessage(GsonTools.beanToString(obj))
        }
    }

    private fun sendSceneMessage(msg: String?) {
        if (scene.sendMessageToScene(msg?.toByteArray()) == 0) {
            LogTools.d(tag, "sendSceneMessage done, $msg")
        } else {
            LogTools.e(tag, "sendSceneMessage fail, $msg")
        }
    }

    fun handleSceneMessage(message: String) {
        LogTools.d(tag, "ready to handle scene message, $message")
        GsonTools.toBean(message, SceneMessageReceiveBody::class.java)?.let { body ->
            when (body.key) {
                SceneMessageType.Position.value -> {
                    GsonTools.toBean(body.value.toString(), SceneMessageReceivedObjectPositions::class.java)?.let { positions ->
                        messageListeners.forEach {
                            it.onObjectPositionAcquired(positions)
                        }
                    }
                }
                SceneMessageType.Karaoke.value -> {
                    GsonTools.toBean(body.value.toString(), SceneMessageReceiveKaraoke::class.java)?.let { karaoke ->
                        messageListeners.forEach {
                            if (karaoke.actionId == KaraokeAction.Start.value) {
                                it.onKaraokeStarted()
                            } else {
                                it.onKaraokeStopped()
                            }
                        }
                    }
                }
                else -> {}
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

enum class SceneMessageType(val value: String) {
    Position("objectLocation"),
    Karaoke("songAction"),
    SendMessage("chat"),
    Language("systemLang")
}

enum class KaraokeAction(val value: Int) {
    Start(1),
    Stop(2)
}

data class SceneManagerBodyCoordinate constructor(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
)

data class SceneMessageRequestBody constructor(
    val key: String,
    val value: Any
)

data class SceneMessageReceiveBody constructor(
    val key: String,
    val value: Any
)

data class SceneMessageReceiveKaraoke constructor(
    val actionId: Int,
)

data class SceneMessageReceivedObjectPositions(
    val objectId: Int,
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