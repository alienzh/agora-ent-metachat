package io.agora.metachat.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.metachat.*
import io.agora.metachat.IMetachatEventHandler.SceneDownloadState
import io.agora.metachat.game.MChatContext
import io.agora.metachat.global.MChatConstant
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.service.*
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.SingleLiveData
import io.agora.metachat.tools.ToastTools

/**
 * @author create by zhangwei03
 */
class MChatRoomCreateViewModel : ViewModel(), IMetachatEventHandler {

    private val chatServiceProtocol: MChatServiceProtocol = MChatServiceProtocol.getImplInstance()

    private val _roomList = SingleLiveData<List<MChatRoomModel>>()
    private val _createRoom = SingleLiveData<MChatCreateRoomOutputModel>()
    private val _joinRoom = SingleLiveData<MChatJoinRoomOutputModel>()
    private val _sceneList = SingleLiveData<List<MetachatSceneInfo>>()
    private val _selectScene = SingleLiveData<Long>()
    private val _requestDownloading = SingleLiveData<Boolean>()
    private val _downloadingProgress = SingleLiveData<Int>()

    fun roomListObservable(): LiveData<List<MChatRoomModel>> = _roomList
    fun createRoomObservable(): LiveData<MChatCreateRoomOutputModel> = _createRoom
    fun joinRoomObservable(): LiveData<MChatJoinRoomOutputModel> = _joinRoom
    fun sceneListObservable(): LiveData<List<MetachatSceneInfo>> = _sceneList
    fun selectSceneObservable(): LiveData<Long> = _selectScene
    fun requestDownloadingObservable(): LiveData<Boolean> = _requestDownloading
    fun downloadingProgressObservable(): LiveData<Int> = _downloadingProgress

    private val mchatContext by lazy {
        MChatContext.instance()
    }

    /**房间列表*/
    fun fetchRoomList() {
        chatServiceProtocol.fetchRoomList { error, list ->
            if (error == MChatServiceProtocol.ERR_OK) {
                _roomList.postValue(list)
            } else {
                LogTools.e("fetch room list failed")
            }
        }
    }

    /**创建房间*/
    fun createRoom(roomName: String, roomIconIndex: Int, password: String? = null) {
        val createRoomModel = MChatCreateRoomInputModel(
            roomName = roomName,
            roomCoverIndex = roomIconIndex,
            isPrivate = !password.isNullOrEmpty(),
            password = password ?: ""
        )
        chatServiceProtocol.createRoom(createRoomModel) { error, result ->
            when (error) {
                MChatServiceProtocol.ERR_OK -> _createRoom.postValue(result)
                else -> {
                    ToastTools.showError(R.string.mchat_room_create_error)
                    LogTools.e(R.string.mchat_room_create_error)
                    _createRoom.postValue(null)
                }
            }
        }
    }

    /**加入房间*/
    fun joinRoom(roomId: String, password: String?) {
        val joinRoomInputModel = MChatJoinRoomInputModel(
            roomId = roomId,
            password = password ?: ""
        )
        chatServiceProtocol.joinRoom(joinRoomInputModel) { error, result ->
            when (error) {
                MChatServiceProtocol.ERR_OK -> _joinRoom.postValue(result)
                MChatServiceProtocol.ERR_GROUP_UNAVAILABLE -> {
                    ToastTools.showError(R.string.mchat_room_unavailable_tip)
                    LogTools.e(R.string.mchat_room_unavailable_tip)
                    _joinRoom.postValue(null)
                }
                MChatServiceProtocol.ERR_PASSWORD_ERROR -> {
                    ToastTools.showError(R.string.mchat_room_incorrect_password)
                    LogTools.e(R.string.mchat_room_incorrect_password)
                    _joinRoom.postValue(null)
                }
                else -> {
                    ToastTools.showError(R.string.mchat_join_room_failed)
                    LogTools.e(R.string.mchat_join_room_failed)
                    _joinRoom.postValue(null)
                }
            }
        }
    }

    fun getScenes() {
        mchatContext.registerMetaChatEventHandler(this)
        if (mchatContext.initialize(MChatApp.instance())) {
            mchatContext.getSceneInfos()
        }
    }

    fun prepareScene(sceneInfo: MetachatSceneInfo) {
        mchatContext.prepareScene(sceneInfo, AvatarModelInfo().apply {
            // TODO choose one
            val bundles = sceneInfo.mBundles
            for (bundleInfo in bundles) {
                if (bundleInfo.mBundleType == MetachatBundleInfo.BundleType.BUNDLE_TYPE_AVATAR) {
                    mBundleCode = bundleInfo.mBundleCode
                    break
                }
            }
            mLocalVisible = true
            if (MChatConstant.Scene.SCENE_GAME == mchatContext.getCurrentScene()) {
                mRemoteVisible = true
                mSyncPosition = true
            } else if (MChatConstant.Scene.SCENE_DRESS == mchatContext.getCurrentScene()) {
                mRemoteVisible = false
                mSyncPosition = false
            }
        }, MetachatUserInfo().apply {
            mUserId = MChatKeyCenter.curUid.toString()
            mUserName = mchatContext.getRoleInfo()?.name ?: mUserId
            mUserIconUrl = mchatContext.getRoleInfo()?.avatar ?: MChatConstant.DEFAULT_PORTRAIT
        })
        if (mchatContext.isSceneDownloaded(sceneInfo)) {
            _selectScene.postValue(sceneInfo.mSceneId)
        } else {
            _requestDownloading.postValue(true)
        }
    }

    fun downloadScene(sceneInfo: MetachatSceneInfo) {
        mchatContext.downloadScene(sceneInfo)
    }

    fun cancelDownloadScene(sceneInfo: MetachatSceneInfo) {
        mchatContext.cancelDownloadScene(sceneInfo)
    }

    override fun onCreateSceneResult(scene: IMetachatScene?, errorCode: Int) {

    }

    override fun onConnectionStateChanged(state: Int, reason: Int) {

    }

    override fun onRequestToken() {

    }

    override fun onGetSceneInfosResult(sceneInfos: Array<out MetachatSceneInfo>, errorCode: Int) {
        LogTools.d("onGetSceneInfosResult sceneInfos size:${sceneInfos.size},errorCode:$errorCode")
        _sceneList.postValue(listOf(*sceneInfos))
    }

    override fun onDownloadSceneProgress(sceneId: Long, progress: Int, state: Int) {
        LogTools.d("onDownloadSceneProgress sceneId:Long,progress:$progress,state:$state")
        if (state == SceneDownloadState.METACHAT_SCENE_DOWNLOAD_STATE_FAILED) {
            _downloadingProgress.postValue(-1)
            return
        }
        _downloadingProgress.postValue(progress)
        if (state == SceneDownloadState.METACHAT_SCENE_DOWNLOAD_STATE_DOWNLOADED) {
            _selectScene.postValue(sceneId)
        }
    }
}