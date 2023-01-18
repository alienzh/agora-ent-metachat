package io.agora.metachat.global

import android.content.Context
import android.content.SharedPreferences
import io.agora.media.RtcTokenBuilder
import io.agora.metachat.BuildConfig
import io.agora.metachat.MChatApp
import io.agora.metachat.tools.LogTools
import io.agora.rtm.RtmTokenBuilder
import java.util.*
import kotlin.math.abs

/**
 * @author create by zhangwei03
 */
object MChatKeyCenter {

    private const val SHARED_NAME = "MChat_Member"

    private fun sp(): SharedPreferences? {
        return MChatApp.instance().getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE)
    }

    fun getUserId(): Int {
        var userId: Int = sp()?.getInt("userId", 0) ?: 0
        if (userId == 0) {
            userId = abs(UUID.randomUUID().hashCode())
            sp()?.edit()?.let {
                it.putInt("userId", userId)?.apply()
            }
        }
        return userId
    }

    val curUserId: Int = getUserId()

    const val RTC_APP_ID: String = BuildConfig.APP_ID
    const val RTC_APP_CERT: String = BuildConfig.APP_CERTIFICATE

    lateinit var RTM_TOKEN: String

    init {
        try {
            RTM_TOKEN = RtmTokenBuilder().buildToken(
                RTC_APP_ID, RTC_APP_CERT, curUserId.toString(),
                RtmTokenBuilder.Role.Rtm_User, 0
            )
        } catch (e: Exception) {
            LogTools.e("rtm token build error:${e.message}")
        }
    }

    /**build rtc token by channelId*/
    fun getRtcToken(channelId: String): String {
        var rtcToken: String = ""
        try {
            rtcToken = RtcTokenBuilder().buildTokenWithUid(
                RTC_APP_ID, RTC_APP_CERT, channelId, curUserId,
                RtcTokenBuilder.Role.Role_Publisher, 0
            )
        } catch (e: Exception) {
            LogTools.e("rtc token build error:${e.message}")
        }
        return rtcToken
    }
}