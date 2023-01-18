package io.agora.metachat

import android.app.Application

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
    }
}