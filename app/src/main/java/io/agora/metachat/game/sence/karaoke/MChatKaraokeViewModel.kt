package io.agora.metachat.game.sence.karaoke

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.agora.metachat.game.sence.MChatContext
import io.agora.metachat.service.MChatServiceProtocol
import io.agora.metachat.tools.SingleLiveData

/**
 * @author create by zhangwei03
 */
class MChatKaraokeViewModel : ViewModel() {

    private val chatServiceProtocol: MChatServiceProtocol = MChatServiceProtocol.getImplInstance()

    private val chatContext by lazy {
        MChatContext.instance()
    }

    private val _originalSinging = SingleLiveData<Boolean>()
    private val _earphoneMonitoring = SingleLiveData<Boolean>()
    private val _keyOfSong = SingleLiveData<Int>()
    private val _accompanimentMusic = SingleLiveData<Int>()
    private val _audioEffect = SingleLiveData<Int>()

    fun originalSingingObservable(): LiveData<Boolean> = _originalSinging
    fun earphoneMonitoringObservable(): LiveData<Boolean> = _earphoneMonitoring
    fun keyOfSongObservable(): LiveData<Int> = _keyOfSong
    fun accompanimentMusicObservable(): LiveData<Int> = _accompanimentMusic
    fun audioEffectObservable(): LiveData<Int> = _audioEffect

    // 打开/关闭原唱
    fun operationOriginalSinging(original: Boolean) {
        chatServiceProtocol.enableOriginalSinging {

        }
        _originalSinging.postValue(original)
    }

    // 打开/关闭耳返
    fun operationEarphoneMonitoring(value: Boolean) {
        chatServiceProtocol.enableEarphoneMonitoring {

        }
        _earphoneMonitoring.postValue(value)
    }

    // 设置升级调
    fun changePitchSong(value: Int) {
        chatServiceProtocol.changePitchSong(value){

        }
        _keyOfSong.postValue(value)
    }

    // 设置音量
    fun changeVolume(value: Int) {

    }

    // 设置伴奏音量
    fun changeAccompaniment(value: Int) {
        chatServiceProtocol.changeAccompanimentVolume(value){

        }
        _accompanimentMusic.postValue(value)
    }

    // 更改音效
    fun changeAudioEffect(effect: MChatAudioEffect) {
        chatServiceProtocol.changeAudioEffect(effect.value){

        }
        _audioEffect.postValue(effect.value)
    }
}