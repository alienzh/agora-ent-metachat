package io.agora.metachat.game

import android.content.Context
import android.view.TextureView
import io.agora.base.VideoFrame
import io.agora.metachat.*
import io.agora.metachat.game.model.EnterSceneExtraInfo
import io.agora.metachat.game.model.RoleInfo
import io.agora.metachat.game.model.UnityMessage
import io.agora.metachat.game.model.UnityRoleInfo
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.tools.GsonTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.spatialaudio.ILocalSpatialAudioEngine
import io.agora.spatialaudio.LocalSpatialAudioConfig
import io.agora.spatialaudio.RemoteVoicePositionInfo
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author create by zhangwei03
 */
class MChatContext private constructor() : IMetachatEventHandler, IMetachatSceneEventHandler,
    MChatAgoraMediaPlayer.OnMediaVideoFramePushListener {

    companion object {
        private const val TAG = "MChatContext"
        private const val enableSpatialAudio: Boolean = true

        private val chatContext by lazy {
            MChatContext()
        }

        @JvmStatic
        fun instance(): MChatContext = chatContext
    }

    private var rtcEngine: RtcEngine? = null
    private var spatialAudioEngine: ILocalSpatialAudioEngine? = null
    private var metaChatService: IMetachatService? = null
    private var metaChatScene: IMetachatScene? = null
    private var sceneInfo: MetachatSceneInfo? = null
    private var modelInfo: AvatarModelInfo? = null
    private var userInfo: MetachatUserInfo? = null
    private var roomName: String = ""
    private var sceneView: TextureView? = null
    private val mchatEventHandlerMap = ConcurrentHashMap<IMetachatEventHandler, Int>()
    private val mchatSceneEventHandlerMap = ConcurrentHashMap<IMetachatSceneEventHandler, Int>()
    private var mJoinedRtc = false
    private var localUserAvatar: ILocalUserAvatar? = null
    private var isInScene = false
    private var currentScene = MChatConstant.Scene.SCENE_NONE
    private var nextScene = MChatConstant.Scene.SCENE_NONE
    private var roleInfo: RoleInfo? = null
    private val roleInfoList = mutableListOf<RoleInfo>()

    fun initialize(context: Context): Boolean {
        var ret = Constants.ERR_OK
        if (rtcEngine == null) {
            try {
                rtcEngine = RtcEngine.create(context, MChatKeyCenter.RTC_APP_ID, object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                        LogTools.d(TAG, "onJoinChannelSuccess channel:$channel,uid:$uid")
                        mJoinedRtc = true
                    }

                    override fun onUserOffline(uid: Int, reason: Int) {
                        LogTools.d(TAG, "onUserOffline uid:$uid,reason:$reason")
                        spatialAudioEngine?.removeRemotePosition(uid)
                    }

                    override fun onAudioRouteChanged(routing: Int) {
                        LogTools.d(TAG, "onAudioRouteChanged routing:$routing")
                    }
                })
                rtcEngine?.apply {
                    setParameters("{\"rtc.enable_debug_log\":true}")
                    enableAudio()
                    disableVideo()
                    setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                    setAudioProfile(Constants.AUDIO_PROFILE_DEFAULT, Constants.AUDIO_SCENARIO_GAME_STREAMING)
                }
                metaChatService = IMetachatService.create()
                val config: MetachatConfig = MetachatConfig().apply {
                    mRtcEngine = rtcEngine
                    mAppId = MChatKeyCenter.RTC_APP_ID
                    mRtmToken = MChatKeyCenter.RTM_TOKEN
                    mLocalDownloadPath = context.filesDir?.path ?: ""
                    mUserId = MChatKeyCenter.curUid.toString()
                    mEventHandler = this@MChatContext
                }
                ret += metaChatService?.initialize(config) ?: -1
                LogTools.d(TAG, "launcher version:${metaChatService?.getLauncherVersion(context)}")
                rtcEngine?.let {
                    MChatAgoraMediaPlayer.instance().initMediaPlayer(it)
                    MChatAgoraMediaPlayer.instance().setOnMediaVideoFramePushListener(this)
                }
            } catch (e: Exception) {
                LogTools.e(TAG, "rtcEngine initialize error:${e.message}")
                return false
            }
        }
        return ret == Constants.ERR_OK
    }

    fun destroy() {
        if (ThreadTools.get().isMainThread){
            innerDestroy()
        }else{
            ThreadTools.get().runOnMainThread {
                innerDestroy()
            }
        }
    }

    private fun innerDestroy(){
        IMetachatService.destroy()
        metaChatService = null
        RtcEngine.destroy()
        rtcEngine = null
    }

    fun registerMetaChatEventHandler(eventHandler: IMetachatEventHandler) {
        mchatEventHandlerMap[eventHandler] = 0
    }

    fun unregisterMetaChatEventHandler(eventHandler: IMetachatEventHandler) {
        mchatEventHandlerMap.remove(eventHandler)
    }

    fun registerMetaChatSceneEventHandler(eventHandler: IMetachatSceneEventHandler) {
        mchatSceneEventHandlerMap[eventHandler] = 0
    }

    fun unregisterMetaChatSceneEventHandler(eventHandler: IMetachatSceneEventHandler) {
        mchatSceneEventHandlerMap.remove(eventHandler)
    }

    fun getSceneInfo(): MetachatSceneInfo {
        return sceneInfo ?: MetachatSceneInfo()
    }

    fun getSceneInfos(): Boolean {
        return metaChatService?.sceneInfos == Constants.ERR_OK
    }

    fun isSceneDownloaded(sceneInfo: MetachatSceneInfo): Boolean {
        return (metaChatService?.isSceneDownloaded(sceneInfo.mSceneId) ?: 0) > 0
    }

    fun downloadScene(sceneInfo: MetachatSceneInfo): Boolean {
        return metaChatService?.downloadScene(sceneInfo.mSceneId) == Constants.ERR_OK
    }

    fun cancelDownloadScene(sceneInfo: MetachatSceneInfo): Boolean {
        return metaChatService?.cancelDownloadScene(sceneInfo.mSceneId) == Constants.ERR_OK
    }

    fun prepareScene(sceneInfo: MetachatSceneInfo?, modelInfo: AvatarModelInfo?, userInfo: MetachatUserInfo?) {
        this.sceneInfo = sceneInfo
        this.modelInfo = modelInfo
        this.userInfo = userInfo
    }

    /**
     * create scene
     */
    fun createScene(activityContext: Context, roomName: String, tv: TextureView): Boolean {
        LogTools.d(TAG, "createScene $roomName")
        this.roomName = roomName
        sceneView = tv
        if (spatialAudioEngine == null && enableSpatialAudio) {
            spatialAudioEngine = ILocalSpatialAudioEngine.create()
            val config: LocalSpatialAudioConfig = LocalSpatialAudioConfig().apply {
                mRtcEngine = rtcEngine
            }
            spatialAudioEngine?.let {
                it.initialize(config)
                it.muteLocalAudioStream(false)
                it.muteAllRemoteAudioStreams(false)
            }
        }
        val sceneConfig = MetachatSceneConfig()
        sceneConfig.mActivityContext = activityContext
        val ret = metaChatService?.createScene(sceneConfig) ?: -1
        mJoinedRtc = false
        return ret == Constants.ERR_OK
    }

    /**
     * enter scene
     */
    fun enterScene() {
        LogTools.d(TAG, "enterScene $roomName")
        localUserAvatar?.let {
            it.userInfo = userInfo
            //该model的mBundleType为MetachatBundleInfo.BundleType.BUNDLE_TYPE_AVATAR类型
            it.modelInfo = modelInfo
            if (null != roleInfo) {
                //设置服装信息
                val dressInfo = DressInfo()
                dressInfo.mExtraCustomInfo = GsonTools.beanToString(getUnityRoleInfo())?.toByteArray()
                it.dressInfo = dressInfo
            }
        }
        metaChatScene?.let {
            //使能位置信息回调功能
            it.enableUserPositionNotification(MChatConstant.Scene.SCENE_GAME == currentScene)
            //设置回调接口
            it.addEventHandler(this@MChatContext)
            val config = EnterSceneConfig().apply {
                //sceneView必须为Texture类型，为渲染unity显示的view
                mSceneView = sceneView
                //rtc加入channel的ID
                mRoomName = roomName
                //内容中心对应的ID
                mSceneId = sceneInfo?.mSceneId ?: 0
            }

            /*
             *仅为示例格式，具体格式以项目实际为准
             *   "extraCustomInfo":{
             *     "sceneIndex":0  //0为默认场景，在这里指咖啡厅，1为换装设置场景
             *   }
             */
            val extraInfo = EnterSceneExtraInfo().apply {
                if (MChatConstant.Scene.SCENE_DRESS == currentScene) {
                    sceneIndex = MChatConstant.Scene.SCENE_DRESS
                } else if (MChatConstant.Scene.SCENE_GAME == currentScene) {
                    sceneIndex = MChatConstant.Scene.SCENE_GAME
                }
            }
            //加载的场景index
            config.mExtraCustomInfo = GsonTools.beanToString(extraInfo)?.toByteArray()
            it.enterScene(config)
        }
    }

    fun updateRole(role: Int): Boolean {
        var ret = Constants.ERR_OK
        //是否为broadcaster
        val isBroadcaster = role == Constants.CLIENT_ROLE_BROADCASTER
        ret += rtcEngine?.updateChannelMediaOptions(ChannelMediaOptions().apply {
            publishMicrophoneTrack = isBroadcaster
            clientRoleType = role
        }) ?: -1
        modelInfo?.mLocalVisible = true
        if (MChatConstant.Scene.SCENE_GAME == currentScene) {
            modelInfo?.mRemoteVisible = isBroadcaster
            modelInfo?.mSyncPosition = isBroadcaster
        } else if (MChatConstant.Scene.SCENE_DRESS == currentScene) {
            modelInfo?.mRemoteVisible = false
            modelInfo?.mSyncPosition = false
        }
        if (null != localUserAvatar && Constants.ERR_OK == localUserAvatar?.setModelInfo(modelInfo)) {
            ret += localUserAvatar?.applyInfo() ?: -1
        }
        return ret == Constants.ERR_OK
    }

    fun enableLocalAudio(enabled: Boolean): Boolean {
        return rtcEngine?.enableLocalAudio(enabled) == Constants.ERR_OK
    }

    fun muteAllRemoteAudioStreams(mute: Boolean): Boolean {
        return if (spatialAudioEngine != null) {
            spatialAudioEngine?.muteAllRemoteAudioStreams(mute) == Constants.ERR_OK
        } else {
            rtcEngine?.muteAllRemoteAudioStreams(mute) == Constants.ERR_OK
        }
    }

    fun leaveScene(): Boolean {
        var ret = Constants.ERR_OK
        metaChatScene?.let {
            ret += rtcEngine?.leaveChannel() ?: -1
            ret += it.leaveScene()
        }
        if (spatialAudioEngine != null) {
            ILocalSpatialAudioEngine.destroy()
            spatialAudioEngine = null
        }
        LogTools.d(TAG, "leaveScene success $roomName")
        return ret == Constants.ERR_OK
    }

    fun pauseMedia() {
        MChatAgoraMediaPlayer.instance().pause()
    }

    fun resumeMedia() {
        MChatAgoraMediaPlayer.instance().resume()
    }

    fun isInScene(): Boolean {
        return isInScene
    }

    fun initRoleInfo(nickname: String, userGender: Int) {
        currentScene = MChatConstant.Scene.SCENE_GAME
        roleInfo = RoleInfo().apply {
            name = nickname
            gender = userGender
            //dress default id is 1
            hair = 1
            tops = 1
            shoes = 1
            lower = 1
        }.also {
            roleInfoList.add(it)
        }
    }

    fun getRoleInfo(): RoleInfo? {
        return roleInfo
    }

    fun getCurrentScene(): Int {
        return currentScene
    }

    fun getNextScene(): Int {
        return nextScene
    }

    fun setCurrentScene(currentScene: Int) {
        this.currentScene = currentScene
    }

    fun setNextScene(nextScene: Int) {
        this.nextScene = nextScene
    }

    fun sendRoleDressInfo() {
        //注意该协议格式需要和unity协商一致
        val message = UnityMessage().apply {
            key = MChatConstant.KEY_UNITY_MESSAGE_DRESS_SETTING
            value = GsonTools.beanToString(getUnityRoleInfo())
        }
        sendSceneMessage(GsonTools.beanToString(message) ?: "")
    }

    override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {
        LogTools.d(TAG, "onCreateSceneResult errorCode:$errorCode")
        metaChatScene = scene
        localUserAvatar = metaChatScene?.localUserAvatar
        for (handler in mchatEventHandlerMap.keys) {
            handler.onCreateSceneResult(scene, errorCode)
        }
    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {
        LogTools.d(TAG, "onConnectionStateChanged state:$state reason:$reason")
        for (handler in mchatEventHandlerMap.keys) {
            handler.onConnectionStateChanged(state, reason)
        }
    }

    override fun onRequestToken() {
        LogTools.d(TAG, "onRequestToken")
        for (handler in mchatEventHandlerMap.keys) {
            handler.onRequestToken()
        }
    }

    override fun onGetSceneInfosResult(scenes: Array<out MetachatSceneInfo>?, errorCode: Int) {
        LogTools.d(TAG, "onGetSceneInfosResult errorCode:$errorCode")
        for (handler in mchatEventHandlerMap.keys) {
            handler.onGetSceneInfosResult(scenes, errorCode)
        }
    }

    override fun onDownloadSceneProgress(sceneId: Long, progress: Int, state: Int) {
        for (handler in mchatEventHandlerMap.keys) {
            handler.onDownloadSceneProgress(sceneId, progress, state)
        }
    }

    override fun onEnterSceneResult(errorCode: Int) {
        LogTools.d(TAG, "onEnterSceneResult errorCode:$errorCode")
        if (errorCode == 0) {
            isInScene = true
            rtcEngine?.joinChannel(
                MChatKeyCenter.getRtcToken(roomName), roomName, MChatKeyCenter.curUid,
                ChannelMediaOptions().apply {
                    autoSubscribeAudio = true
                    clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                })
            spatialAudioEngine?.let {
                // audio的mute状态交给ILocalSpatialAudioEngine统一管理
                rtcEngine?.muteAllRemoteAudioStreams(true)
            }
            if (MChatConstant.Scene.SCENE_GAME == currentScene) {
                // todo Just for test
                metaChatScene?.enableVideoDisplay("1", true)
                MChatAgoraMediaPlayer.instance().play(MChatConstant.VIDEO_URL, 0)
            }
        }
        for (handler in mchatSceneEventHandlerMap.keys) {
            handler.onEnterSceneResult(errorCode)
        }
    }

    override fun onLeaveSceneResult(errorCode: Int) {
        LogTools.d(TAG, "onLeaveSceneResult errorCode:$errorCode")
        isInScene = false
        MChatAgoraMediaPlayer.instance().stop()
        if (errorCode == 0) {
            metaChatScene?.release()
            metaChatScene = null
        }
        for (handler in mchatSceneEventHandlerMap.keys) {
            handler.onLeaveSceneResult(errorCode)
        }
    }

    override fun onRecvMessageFromScene(message: ByteArray?) {
        val msg = if (message != null) String(message) else ""
        LogTools.d(TAG, "onRecvMessageFromScene message:$msg")
        for (handler in mchatSceneEventHandlerMap.keys) {
            handler.onRecvMessageFromScene(message)
        }
    }

    override fun onUserPositionChanged(uid: String?, posInfo: MetachatUserPositionInfo) {
        LogTools.d(
            TAG,
            "onUserPositionChanged uid:$uid,${Arrays.toString(posInfo.mPosition)},${Arrays.toString(posInfo.mForward)},${
                Arrays.toString(posInfo.mRight)
            },${Arrays.toString(posInfo.mUp)}"
        )
        spatialAudioEngine?.let {
            val userId = uid?.toIntOrNull() ?: -1
            if (MChatKeyCenter.curUid == userId) {
                it.updateSelfPosition(posInfo.mPosition, posInfo.mForward, posInfo.mRight, posInfo.mUp)
            } else if (mJoinedRtc) {
                it.updateRemotePosition(userId, RemoteVoicePositionInfo().apply {
                    position = posInfo.mPosition
                    forward = posInfo.mForward
                })
            } else {
            }
        }

        for (handler in mchatSceneEventHandlerMap.keys) {
            handler.onUserPositionChanged(uid, posInfo)
        }
    }

    override fun onEnumerateVideoDisplaysResult(displayIds: Array<out String>?) {
    }

    override fun onReleasedScene(status: Int) {
        LogTools.d(TAG, "onReleasedScene status:$status")
        for (handler in mchatSceneEventHandlerMap.keys) {
            handler.onReleasedScene(status)
        }
    }

    override fun onMediaVideoFramePushed(frame: VideoFrame?) {
        metaChatScene?.pushVideoFrameToDisplay("1", frame)
    }

    fun sendSceneMessage(msg: String) {
        if (metaChatScene == null) {
            LogTools.e(TAG, "sendMessageToScene metaChatScene is null")
            return
        } else {
            if (metaChatScene?.sendMessageToScene(msg.toByteArray()) == 0) {
                LogTools.d(TAG, "sendMessageToScene $msg successful")
            } else {
                LogTools.e(TAG, "sendMessageToScene $msg failed")
            }
        }
    }

    fun getUnityRoleInfo(): UnityRoleInfo? {
        val unityRoleInfo = UnityRoleInfo()
        roleInfo?.let {
            unityRoleInfo.gender = it.gender
            unityRoleInfo.hair = it.hair
            unityRoleInfo.tops = it.tops
            unityRoleInfo.lower = it.lower
            unityRoleInfo.shoes = it.shoes
        }
        return unityRoleInfo
    }

    fun resetRoleInfo() {
        roleInfoList.clear()
        roleInfo = null
    }
}