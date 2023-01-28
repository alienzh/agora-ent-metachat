package io.agora.metachat.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.agora.metachat.R
import io.agora.metachat.service.*
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ToastTools

/**
 * @author create by zhangwei03
 */
class MChatRoomCreateViewModel : ViewModel() {

    private val chatServiceProtocol: MChatServiceProtocol = MChatServiceProtocol.getImplInstance()

    private val _roomListLiveData = MutableLiveData<List<MChatRoomModel>>()
    private val _createRoomLiveData = MutableLiveData<MChatCreateRoomOutputModel>()
    private val _checkRoomPasswordLiveData = MutableLiveData<Boolean>()
    private val _joinRoomLiveData = MutableLiveData<MChatJoinRoomOutputModel>()

    fun roomListObservable(): LiveData<List<MChatRoomModel>> = _roomListLiveData
    fun createRoomObservable(): LiveData<MChatCreateRoomOutputModel> = _createRoomLiveData
    fun checkRoomPasswordObservable(): LiveData<Boolean> = _checkRoomPasswordLiveData
    fun joinRoomObservable(): LiveData<MChatJoinRoomOutputModel> = _joinRoomLiveData

    /**房间列表*/
    fun fetchRoomList() {
        chatServiceProtocol.fetchRoomList { error, list ->
            if (error == MChatServiceProtocol.ERR_OK) {
                _roomListLiveData.postValue(list)
//                _roomListLiveData.postValue(mutableListOf())
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
                MChatServiceProtocol.ERR_OK -> _createRoomLiveData.postValue(result)
                else -> {
                    ToastTools.showError(R.string.mchat_room_create_error)
                    LogTools.e(R.string.mchat_room_create_error)
                    _createRoomLiveData.postValue(null)
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
                MChatServiceProtocol.ERR_OK -> _joinRoomLiveData.postValue(result)
                MChatServiceProtocol.ERR_GROUP_UNAVAILABLE -> {
                    ToastTools.showError(R.string.mchat_room_unavailable_tip)
                    LogTools.e(R.string.mchat_room_unavailable_tip)
                    _joinRoomLiveData.postValue(null)
                }
                MChatServiceProtocol.ERR_PASSWORD_ERROR -> {
                    ToastTools.showError(R.string.mchat_room_incorrect_password)
                    LogTools.e(R.string.mchat_room_incorrect_password)
                    _joinRoomLiveData.postValue(null)
                }
                else -> {
                    ToastTools.showError(R.string.mchat_join_room_failed)
                    LogTools.e(R.string.mchat_join_room_failed)
                    _joinRoomLiveData.postValue(null)
                }
            }
        }
    }
}