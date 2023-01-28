package io.agora.metachat.imkit

import android.content.Context
import com.hyphenate.EMCallBack
import com.hyphenate.EMConnectionListener
import com.hyphenate.EMGroupChangeListener
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.*
import com.hyphenate.chat.adapter.EMAError
import com.hyphenate.exceptions.HyphenateException
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.service.MChatServiceProtocol
import io.agora.metachat.tools.DeviceTools
import io.agora.metachat.tools.LogTools
import io.agora.metachat.tools.ThreadTools

/**
 * @author create by zhangwei03
 */
class MChatGroupIMManager private constructor() : EMConnectionListener, EMMessageListener, EMGroupChangeListener {

    private lateinit var context: Context
    private lateinit var chatServiceProtocol: MChatServiceProtocol
    private var groupId: String = ""
    private var ownerId: String = ""
    private val allNormalList = mutableListOf<ChatMessageData>()

    companion object {
        private const val TAG = "MChatroomIMManager"

        private val groupIMManager by lazy {
            MChatGroupIMManager()
        }

        @JvmStatic
        fun instance(): MChatGroupIMManager = groupIMManager
    }

    fun initConfig(context: Context, imKey: String) {
        this.context = context
        // 在主进程中进行初始化：
        val options = EMOptions().apply {
            appKey = imKey
            autoLogin = false
        }
        if (!DeviceTools.isMainProcess(context)) {
            LogTools.e(TAG, "im init need the main process!")
            return
        }
        EMClient.getInstance().init(context, options)
        // 注册连接状态监听
        EMClient.getInstance().addConnectionListener(this)
        chatServiceProtocol = MChatServiceProtocol.getImplInstance()
    }

    /**
     * 需要在详情页初始化，防止groupId 为空或者不正确
     */
    fun initGroup(groupId: String, ownerId: String) {
        this.groupId = groupId
        this.ownerId = ownerId
        // 注册消息监听
        EMClient.getInstance().chatManager().addMessageListener(this)
        // 注册群组监听
        EMClient.getInstance().groupManager().addGroupChangeListener(this)
    }

    /**
     * 创建房间或者加入房间前需要创建环信账号(如果本地没有的话)，登录环信IM
     * @param loginCallBack on io thread
     */
    fun beforeCreateOrJoinRoomLoginTask(loginCallBack: (error: Int) -> Unit) {
        ThreadTools.get().runOnIOThread {
            val imUid = MChatKeyCenter.curUserId.toString()
            val imPassword = MChatKeyCenter.imPassword
            // 本地没有账号则创建im 账号
            if (!MChatKeyCenter.accountCreated()) {
                try {
                    EMClient.getInstance().createAccount(imUid, imPassword)
                } catch (e: HyphenateException) {
                    LogTools.e(TAG, "im create account error:${e.message}")
                }
            }
            EMClient.getInstance().login(imUid, imPassword, object : EMCallBack {
                override fun onSuccess() {
                    LogTools.d(TAG, "im login success")
                    loginCallBack.invoke(MChatServiceProtocol.ERR_LOGIN_SUCCESS)
                }

                override fun onError(code: Int, error: String?) {
                    if (code == EMAError.USER_ALREADY_LOGIN) {
                        LogTools.d(TAG, "im already login")
                        loginCallBack.invoke(MChatServiceProtocol.ERR_LOGIN_SUCCESS)
                    } else {
                        LogTools.e(TAG, "im login code:$code,error:$error")
                        loginCallBack.invoke(MChatServiceProtocol.ERR_LOGIN_ERROR)
                    }
                }
            })
        }
    }

    //--------------------Connection start-----------------
    override fun onConnected() {
        LogTools.d(TAG, "onConnected")
    }

    override fun onDisconnected(errorCode: Int) {
        LogTools.d(TAG, "onDisconnected errorCode:$errorCode")
    }

    override fun onTokenExpired() {
        LogTools.d(TAG, "onTokenExpired")
    }

    override fun onTokenWillExpire() {
        LogTools.d(TAG, "onTokenWillExpire")
    }

    override fun onLogout(errorCode: Int) {
        LogTools.d(TAG, "onLogout errorCode:$errorCode")
    }
    //--------------------Connection end-----------------


    //--------------------Message start-----------------
    /**
     * 收到消息，遍历消息队列，解析和显示。
     * @param messages 消息队列
     */
    override fun onMessageReceived(messages: List<EMMessage>) {
        for (i in messages.indices) {
            val emMessage = messages[i]
            // 只判断文本消息
            if (emMessage.type != EMMessage.Type.TXT) continue
            val message = parseChatMessage(emMessage)
            allNormalList.add(message)
            for (listener in chatServiceProtocol.getSubscribeDelegates()) {
                listener.onReceiveTextMsg(message.conversationId, message)
            }
        }
    }

    /**
     * 解析环信IM 消息
     * @param chatMessage 环信IM 消息对象，代表一条发送或接收到的消息。
     */
    fun parseChatMessage(chatMessage: EMMessage): ChatMessageData {
        val chatMessageData = ChatMessageData()
        chatMessageData.setForm(chatMessage.from)
        chatMessageData.to = chatMessage.to
        chatMessageData.conversationId = chatMessage.conversationId()
        chatMessageData.messageId = chatMessage.msgId
        if (chatMessage.body is EMTextMessageBody) {
            chatMessageData.type = "text"
            chatMessageData.content = (chatMessage.body as EMTextMessageBody).message
        } else if (chatMessage.body is EMCustomMessageBody) {
            chatMessageData.type = "custom"
            chatMessageData.event = (chatMessage.body as EMCustomMessageBody).event()
            chatMessageData.customParams = (chatMessage.body as EMCustomMessageBody).params
        }
        chatMessageData.ext = chatMessage.ext()
        return chatMessageData
    }
    //--------------------Message end-----------------


    //--------------------Group start-----------------
    // 当前用户收到了入群邀请。受邀用户会收到该回调。例如，用户 B 邀请用户 A 入群，则用户 A 会收到该回调。
    override fun onInvitationReceived(groupId: String?, groupName: String?, inviter: String?, reason: String?) {}

    // 群主或群管理员收到进群申请。群主和所有管理员收到该回调。
    override fun onRequestToJoinReceived(groupId: String?, groupName: String?, applicant: String?, reason: String?) {}

    // 群主或群管理员同意用户的进群申请。申请人、群主和管理员（除操作者）收到该回调。
    override fun onRequestToJoinAccepted(groupId: String?, groupName: String?, accepter: String?) {}

    // 群主或群管理员拒绝用户的进群申请。申请人、群主和管理员（除操作者）收到该回调。
    override fun onRequestToJoinDeclined(groupId: String?, groupName: String?, decliner: String?, reason: String?) {}

    // 用户同意进群邀请。邀请人收到该回调。
    override fun onInvitationAccepted(groupId: String?, invite: String?, reason: String?) {}

    // 用户拒绝进群邀请。邀请人收到该回调。
    override fun onInvitationDeclined(groupId: String?, invitee: String?, reason: String?) {}

    // 有成员被移出群组。被踢出群组的成员会收到该回调。
    override fun onUserRemoved(groupId: String, groupName: String?) {
        LogTools.d(TAG, "onUserRemoved groupId:$groupId,groupName:$groupName")
        for (listener in chatServiceProtocol.getSubscribeDelegates()) {
            listener.onUserRemoved(groupId)
        }
    }

    // 群组解散。群主解散群组时，所有群成员均会收到该回调。
    override fun onGroupDestroyed(groupId: String, groupName: String?) {
        LogTools.d(TAG, "onGroupDestroyed groupId:$groupId,groupName:$groupName")
        for (listener in chatServiceProtocol.getSubscribeDelegates()) {
            listener.onGroupDestroyed(groupId)
        }
    }

    // 有用户自动同意加入群组。邀请人收到该回调。
    override fun onAutoAcceptInvitationFromGroup(groupId: String?, inviter: String?, inviteMessage: String?) {}

    // 有成员被加入群组禁言列表。被禁言的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onMuteListAdded(groupId: String?, mutes: MutableList<String>?, muteExpire: Long) {}

    // 有成员被移出禁言列表。被解除禁言的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onMuteListRemoved(groupId: String?, mutes: MutableList<String>?) {}

    // 有成员被加入群组白名单。被添加的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onWhiteListAdded(groupId: String?, whitelist: MutableList<String>?) {}

    // 有成员被移出群组白名单。被移出的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onWhiteListRemoved(groupId: String?, whitelist: MutableList<String>?) {}

    // 全员禁言状态变化。群组所有成员（除操作者外）会收到该回调。
    override fun onAllMemberMuteStateChanged(groupId: String?, isMuted: Boolean) {}

    // 设置管理员。群主、新管理员和其他管理员会收到该回调。
    override fun onAdminAdded(groupId: String?, administrator: String?) {}

    // 群组管理员被移除。被移出的成员及群主和群管理员（除操作者外）会收到该回调。
    override fun onAdminRemoved(groupId: String?, administrator: String?) {}

    // 群主转移权限。原群主和新群主会收到该回调。
    override fun onOwnerChanged(groupId: String?, newOwner: String?, oldOwner: String?) {}

    // 有新成员加入群组。除了新成员，其他群成员会收到该回调。
    override fun onMemberJoined(groupId: String, member: String) {
        LogTools.d(TAG, "onMemberJoined groupId:$groupId,member:$member")
        for (listener in chatServiceProtocol.getSubscribeDelegates()) {
            listener.onMemberJoined(groupId, member)
        }
    }

    // 有成员主动退出群。除了退群的成员，其他群成员会收到该回调。
    override fun onMemberExited(groupId: String, member: String) {
        LogTools.d(TAG, "onMemberExited groupId:$groupId,member:$member")
        for (listener in chatServiceProtocol.getSubscribeDelegates()) {
            listener.onMemberExited(groupId, member)
        }
    }

    // 群组公告更新。群组所有成员会收到该回调。
    override fun onAnnouncementChanged(groupId: String?, announcement: String?) {}

    // 有成员新上传群组共享文件。群组所有成员会收到该回调。
    override fun onSharedFileAdded(groupId: String?, sharedFile: EMMucSharedFile?) {}

    // 群组共享文件被删除。群组所有成员会收到该回调。
    override fun onSharedFileDeleted(groupId: String?, fileId: String?) {}
    //--------------------Group end-----------------
}