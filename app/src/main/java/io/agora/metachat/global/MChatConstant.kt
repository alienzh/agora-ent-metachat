package io.agora.metachat.global

/**
 * @author create by zhangwei03
 */
object MChatConstant {

    object Gender {
        const val MALE = 0
        const val FEMALE = 1
    }

    object Params {
        const val KEY_ROOM_ID: String = "key_room_id"
        const val KEY_ROOM_NAME: String = "key_room_name"
        const val KEY_ROOM_COVER_INDEX: String = "key_room_cover_index"
        const val KEY_ROOM_PASSWORD: String = "key_room_password"
        const val KEY_NICKNAME: String = "key_nickname"
        const val KEY_PORTRAIT_INDEX: String = "key_portrait_index"
        const val KEY_BADGE_INDEX: String = "key_badge_index"
        const val KEY_GENDER: String = "key_gender"
        const val KEY_VIRTUAL_AVATAR: String = "key_virtual_avatar"
    }

    private const val badgeUrl0 =
        "http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_0.png"
    private const val badgeUrl1 =
        "http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_1.png"
    private const val badgeUrl2 =
        "http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/metaChat/login_badge_2.png"

    fun getBadgeUrl(badgeIndex: Int): String {
        return when (badgeIndex) {
            0 -> badgeUrl0
            1 -> badgeUrl1
            2 -> badgeUrl2
            else -> badgeUrl0
        }
    }
}