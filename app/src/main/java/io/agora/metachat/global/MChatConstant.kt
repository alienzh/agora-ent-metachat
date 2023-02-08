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
        const val KEY_IS_CREATE: String = "key_is_create"
    }

    object Scene {
        const val SCENE_NONE = -1
        const val SCENE_GAME = 0
        const val SCENE_DRESS = 1
    }

    /**
     * https://test.cdn.sbnh.cn/9ad87c1738bf0485b7f243ee5cfb409f.mp4
     * http://accktvpic.oss-cn-beijing.aliyuncs.com/pic/meta/demo/fulldemoStatic/15c116aca7590992f261143935d6f2cb.mov
     */
    const val VIDEO_URL = "http://agora.fronted.love/yyl.mov"
    const val DEFAULT_PORTRAIT = "https://accpic.sd-rtn.com/pic/test/png/2.png"
    const val PLAY_ADVERTISING_VIDEO_REPEAT = -1
    const val KEY_UNITY_MESSAGE_DRESS_SETTING = "dressSetting"

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