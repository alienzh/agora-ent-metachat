package io.agora.metachat.game

import android.content.Context
import android.view.TextureView
import io.agora.base.VideoFrame
import io.agora.metachat.*
import io.agora.metachat.game.internal.MChatBaseEventHandler
import io.agora.metachat.game.internal.MChatBaseSceneEventHandler
import io.agora.metachat.game.internal.MChatBaseVideoFrameObserver
import io.agora.metachat.game.model.EnterSceneExtraInfo
import io.agora.metachat.game.model.RoleInfo
import io.agora.metachat.game.model.UnityRoleInfo
import io.agora.metachat.game.npc.MChatLocalSourceMediaPlayer
import io.agora.metachat.game.npc.MChatNpcManager
import io.agora.metachat.game.npc.NpcListener
import io.agora.metachat.global.*
import io.agora.metachat.tools.GsonTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.spatialaudio.ILocalSpatialAudioEngine
import io.agora.spatialaudio.LocalSpatialAudioConfig

/**
 * @author create by zhangwei03
 */
class MChatContext private constructor() {

    companion object {
        private const val TAG = "MChatContext"

        private val chatContext by lazy {
            MChatContext()
        }

        @JvmStatic
        fun instance(): MChatContext = chatContext
    }

    private var rtcEngine: RtcEngine? = null
    private var metaChatService: IMetachatService? = null
    private var metaChatScene: IMetachatScene? = null
    private var sceneInfo: MetachatSceneInfo? = null
    private var modelInfo: AvatarModelInfo? = null
    private var userInfo: MetachatUserInfo? = null
    private var rtcRoomId: String = ""
    private var sceneTextureView: TextureView? = null
    private val mchatEventHandlerSet = mutableSetOf<IMetachatEventHandler>()
    private val mchatSceneEventHandlerSet = mutableSetOf<IMetachatSceneEventHandler>()
    private var mJoinedRtc = false
    private var localUserAvatar: ILocalUserAvatar? = null
    private var isInScene = false
    private var currentScene = MChatConstant.Scene.SCENE_NONE
    private var roleInfo: RoleInfo? = null
    private var unityCmd: MChatUnityCmd? = null

    private var chatMediaPlayer: MChatAgoraMediaPlayer? = null
    private var chatSpatialAudio: MChatSpatialAudio? = null
    private var npcManager: MChatNpcManager? = null

    fun getLocalUserAvatar(): ILocalUserAvatar? = localUserAvatar

    fun getUnityCmd(): MChatUnityCmd? = unityCmd

    fun chatMediaPlayer(): MChatAgoraMediaPlayer? = chatMediaPlayer

    fun chatSpatialAudio(): MChatSpatialAudio? = chatSpatialAudio

    fun chatNpcManager(): MChatNpcManager? = npcManager

    fun rtcEngine(): RtcEngine? = rtcEngine

    // 电视音量
    var tvVolume: Int = MChatConstant.DefaultValue.DEFAULT_TV_VOLUME

    // npc 音量
    var npcVolume: Int = MChatConstant.DefaultValue.DEFAULT_NPC_VOLUME

    // 音效距离
    var recvRange: Float = MChatConstant.DefaultValue.DEFAULT_RECV_RANGE

    // 衰减系数
    var distanceUnit: Float = MChatConstant.DefaultValue.DEFAULT_DISTANCE_UNIT

    private val mChatEventHandler = object : MChatBaseEventHandler() {
        override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {
            LogTools.d(TAG, "onCreateSceneResult errorCode:$errorCode")
            metaChatScene = scene
            metaChatScene?.let {
                unityCmd = MChatUnityCmd(it)
                unityCmd?.changeLanguage()
            }
            localUserAvatar = metaChatScene?.localUserAvatar
            mchatEventHandlerSet.forEach {
                it.onCreateSceneResult(scene, errorCode)
            }
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            LogTools.d(TAG, "onConnectionStateChanged state:$state reason:$reason")
            mchatEventHandlerSet.forEach {
                it.onConnectionStateChanged(state, reason)
            }
        }

        override fun onRequestToken() {
            LogTools.d(TAG, "onRequestToken")
            mchatEventHandlerSet.forEach {
                it.onRequestToken()
            }
        }

        override fun onGetSceneInfosResult(scenes: Array<out MetachatSceneInfo>, errorCode: Int) {
            LogTools.d(TAG, "onGetSceneInfosResult errorCode:$errorCode")
            mchatEventHandlerSet.forEach {
                it.onGetSceneInfosResult(scenes, errorCode)
            }
        }

        override fun onDownloadSceneProgress(sceneId: Long, progress: Int, state: Int) {
            mchatEventHandlerSet.forEach {
                it.onDownloadSceneProgress(sceneId, progress, state)
            }
        }

    }

    private val mChatSceneEventHandler = object : MChatBaseSceneEventHandler() {
        override fun onEnterSceneResult(errorCode: Int) {
            LogTools.d(TAG, "onEnterSceneResult errorCode:$errorCode")
            if (errorCode == 0) {
                isInScene = true
                rtcEngine?.joinChannel(
                    MChatKeyCenter.getRtcToken(rtcRoomId), rtcRoomId, MChatKeyCenter.curUid,
                    ChannelMediaOptions().apply {
                        autoSubscribeAudio = true
                        clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                    })
                // audio的mute状态交给ILocalSpatialAudioEngine统一管理
                chatSpatialAudio()?.muteAllRemoteAudioStreams(true)
                if (MChatConstant.Scene.SCENE_GAME == currentScene) {
                    metaChatScene?.enableVideoDisplay(MChatConstant.DefaultValue.VIDEO_DISPLAY_ID, true)
                    chatMediaPlayer()?.play(MChatConstant.VIDEO_URL, 0)
                }
            }
            npcManager = MChatNpcManager()
            npcManager?.initNpcMediaPlayer(MChatApp.instance(), this@MChatContext, object : NpcListener {
                override fun onNpcReady(id: Int, sourceName: String) {
                    npcManager?.play(id)
                }

                override fun onNpcFail() {
                }

            })
            mchatSceneEventHandlerSet.forEach {
                it.onEnterSceneResult(errorCode)
            }
        }

        override fun onLeaveSceneResult(errorCode: Int) {
            LogTools.d(TAG, "onLeaveSceneResult errorCode:$errorCode")
            isInScene = false
            chatMediaPlayer()?.stop()
            if (errorCode == 0) {
                metaChatScene?.release()
                metaChatScene?.removeEventHandler(this)
                metaChatScene = null
            }
            unityCmd = null
            mchatSceneEventHandlerSet.forEach {
                it.onLeaveSceneResult(errorCode)
            }
        }

        override fun onRecvMessageFromScene(message: ByteArray?) {
            val msg = if (message != null) String(message) else ""
            LogTools.d(TAG, "onRecvMessageFromScene message:$msg")
            mchatSceneEventHandlerSet.forEach {
                it.onRecvMessageFromScene(message)
            }
            getUnityCmd()?.handleSceneMessage(msg)
        }

        override fun onUserPositionChanged(uid: String, posInfo: MetachatUserPositionInfo) {
//        LogTools.d(
//            TAG,
//            "onUserPositionChanged uid:$uid,${Arrays.toString(posInfo.mPosition)},${Arrays.toString(posInfo.mForward)},${
//                Arrays.toString(posInfo.mRight)
//            },${Arrays.toString(posInfo.mUp)}"
//        )
            mchatSceneEventHandlerSet.forEach {
                it.onUserPositionChanged(uid, posInfo)
            }
        }

        override fun onReleasedScene(status: Int) {
            LogTools.d(TAG, "onReleasedScene status:$status")
            mchatSceneEventHandlerSet.forEach {
                it.onReleasedScene(status)
            }
        }
    }

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
                        chatSpatialAudio()?.removeRemotePosition(uid)
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
                metaChatService?.addEventHandler(mChatEventHandler)
                val config: MetachatConfig = MetachatConfig().apply {
                    mRtcEngine = rtcEngine
                    mAppId = MChatKeyCenter.RTC_APP_ID
                    mRtmToken = MChatKeyCenter.RTM_TOKEN
                    mLocalDownloadPath = context.filesDir?.path ?: ""
                    mUserId = MChatKeyCenter.curUid.toString()
                    mEventHandler = mChatEventHandler
                }
                ret += metaChatService?.initialize(config) ?: -1
                LogTools.d(TAG, "launcher version:${metaChatService?.getLauncherVersion(context)}")
                rtcEngine?.let { rtc ->
                    rtc.createMediaPlayer()?.let { mediaPLayer ->
                        chatMediaPlayer = MChatAgoraMediaPlayer(rtc, mediaPLayer)
                        chatMediaPlayer?.initMediaPlayer(tvVolume)
                        chatMediaPlayer?.setOnMediaVideoFramePushListener { frame ->
                            metaChatScene?.pushVideoFrameToDisplay(MChatConstant.DefaultValue.VIDEO_DISPLAY_ID, frame)
                        }
                    }
                    rtc.registerVideoFrameObserver(object : MChatBaseVideoFrameObserver() {
                        override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame, mediaPlayerId: Int): Boolean {
                            if (chatMediaPlayer()?.inChargeOfKaraoke() == false) {
                                // 在k歌中不需要往场景内推送原始视频帧
                                metaChatScene?.pushVideoFrameToDisplay(
                                    MChatConstant.DefaultValue.VIDEO_DISPLAY_ID, videoFrame
                                )
                                return true
                            }
                            return false
                        }
                    })
                    ILocalSpatialAudioEngine.create()?.let { localSpatialAudio ->
                        val localSpatialAudioConfig = LocalSpatialAudioConfig().apply {
                            mRtcEngine = rtc
                        }
                        localSpatialAudio.initialize(localSpatialAudioConfig)
                        chatSpatialAudio = MChatSpatialAudio(MChatKeyCenter.curUid, localSpatialAudio)
                        chatSpatialAudio?.initSpatialAudio()
                    }
                }
            } catch (e: Exception) {
                LogTools.e(TAG, "rtcEngine initialize error:${e.message}")
                return false
            }
        }
        return ret == Constants.ERR_OK
    }

    // npc media player
    fun createLocalSourcePlayer(id: Int, sourcePath: String): MChatLocalSourceMediaPlayer? {
        return rtcEngine?.let { it ->
            val player = it.createMediaPlayer()
            return MChatLocalSourceMediaPlayer(id, player, sourcePath)
        }
    }

    fun destroy() {
        if (ThreadTools.get().isMainThread) {
            innerDestroy()
        } else {
            ThreadTools.get().runOnMainThread {
                innerDestroy()
            }
        }
    }

    private fun innerDestroy() {
        IMetachatService.destroy()
        metaChatService?.removeEventHandler(mChatEventHandler)
        metaChatService = null
        npcManager?.stopAll()
        npcManager?.destroy()
        npcManager = null
        chatMediaPlayer?.destroy()
        chatMediaPlayer = null
        chatSpatialAudio?.destroy()
        chatSpatialAudio = null
        RtcEngine.destroy()
        rtcEngine = null
    }

    fun registerMetaChatEventHandler(eventHandler: IMetachatEventHandler) {
        mchatEventHandlerSet.add(eventHandler)
    }

    fun unregisterMetaChatEventHandler(eventHandler: IMetachatEventHandler) {
        mchatEventHandlerSet.remove(eventHandler)
    }

    fun registerMetaChatSceneEventHandler(eventHandler: IMetachatSceneEventHandler) {
        mchatSceneEventHandlerSet.add(eventHandler)
    }

    fun unregisterMetaChatSceneEventHandler(eventHandler: IMetachatSceneEventHandler) {
        mchatSceneEventHandlerSet.remove(eventHandler)
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
    fun createScene(activityContext: Context, roomId: String, tv: TextureView): Boolean {
        LogTools.d(TAG, "createScene $roomId")
        this.rtcRoomId = roomId
        sceneTextureView = tv
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
        LogTools.d(TAG, "enterScene $rtcRoomId")
        localUserAvatar?.let {
            it.userInfo = userInfo
            //该model的mBundleType为MetachatBundleInfo.BundleType.BUNDLE_TYPE_AVATAR类型
            it.modelInfo = modelInfo
            // TODO:
//            if (null != roleInfo) {
//                //设置服装信息
//                val dressInfo = DressInfo()
//                dressInfo.mExtraCustomInfo = GsonTools.beanToString(getUnityRoleInfo())?.toByteArray()
//                it.dressInfo = dressInfo
//            }
        }
        metaChatScene?.let {
            //使能位置信息回调功能
            it.enableUserPositionNotification(MChatConstant.Scene.SCENE_GAME == currentScene)
            //设置回调接口
            it.addEventHandler(mChatSceneEventHandler)
            val config = EnterSceneConfig()
            config.mSceneView = sceneTextureView  //sceneView必须为Texture类型，为渲染unity显示的view
            config.mRoomName = rtcRoomId  //rtc加入channel的ID
            config.mSceneId = sceneInfo?.mSceneId ?: 0   //内容中心对应的ID
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
            // TODO:
            modelInfo?.mRemoteVisible = false
            modelInfo?.mSyncPosition = false
        }
        if (null != localUserAvatar && Constants.ERR_OK == localUserAvatar?.setModelInfo(modelInfo)) {
            ret += localUserAvatar!!.applyInfo()
        }
        return ret == Constants.ERR_OK
    }

    fun enableLocalAudio(enabled: Boolean): Boolean {
        return rtcEngine?.enableLocalAudio(enabled) == Constants.ERR_OK
    }

    fun muteAllRemoteAudioStreams(mute: Boolean): Boolean {
        return if (chatSpatialAudio() != null) {
            chatSpatialAudio()?.muteAllRemoteAudioStreams(mute) == Constants.ERR_OK
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
        LogTools.d(TAG, "leaveScene success $rtcRoomId")
        resetRoleInfo()
        return ret == Constants.ERR_OK
    }

    fun isInScene(): Boolean = isInScene

    fun initRoleInfo(nickname: String, userGender: Int, badgeIndex: Int) {
        currentScene = MChatConstant.Scene.SCENE_GAME
        roleInfo = RoleInfo().apply {
            name = nickname
            gender = userGender
            avatar = MChatConstant.getBadgeUrl(badgeIndex) // todo 头像需要url,这里传徽章
            //dress default id is 1
            hair = 1
            tops = 1
            shoes = 1
            lower = 1
        }
    }

    fun getRoleInfo(): RoleInfo? = roleInfo

    fun getCurrentScene(): Int = currentScene

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

    private fun resetRoleInfo() {
        roleInfo = null
    }
}