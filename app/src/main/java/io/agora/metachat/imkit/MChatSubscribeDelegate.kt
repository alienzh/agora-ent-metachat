package io.agora.metachat.imkit

/**
 * @author create by zhangwei03
 *
 * im 回调协议
 */
interface MChatSubscribeDelegate {

    /**
     * 收到文本消息
     * @param groupId 环信IM 群组id
     * @param message 环信IM 消息
     */
    fun onReceiveTextMsg(groupId: String, message: ChatMessageData?)

    /**
     * 用户离开群组
     * @param groupId 环信IM 群组id
     * @param chatUid 环信IM 用户uid
     */
    fun onMemberExited(groupId: String, chatUid: String)

    /**
     * 用户加入群组
     * @param groupId 环信IM 群组id
     * @param chatUid 环信IM 用户uid
     */
    fun onMemberJoined(groupId: String, chatUid: String)

    /**
     * 群组解散
     * @param groupId 环信IM 群组id
     */
    fun onGroupDestroyed(groupId: String)

    /**
     * 当前用户被踢出群组
     * @param groupId 环信IM 群组id
     */
    fun onUserRemoved(groupId: String)
}