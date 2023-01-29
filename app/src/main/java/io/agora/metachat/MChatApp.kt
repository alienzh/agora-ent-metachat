package io.agora.metachat

import android.app.Application
import io.agora.metachat.global.MChatKeyCenter
import io.agora.metachat.imkit.MChatGroupIMManager
import io.agora.metachat.tools.DeviceTools

/**
 * @author create by zhangwei03
 */
class MChatApp : Application() {

    companion object {
        private lateinit var app: Application

        fun instance(): Application {
            return app
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        MChatGroupIMManager.instance().initConfig(this,MChatKeyCenter.IM_APP_KEY)
        DeviceTools.isZh(this)
    }
}