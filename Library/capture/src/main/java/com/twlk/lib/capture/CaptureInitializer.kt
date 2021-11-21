package com.twlk.lib.capture

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.startup.Initializer
import com.twlk.core.Constant
import com.twlk.lib.hook.IOkhttpClientInterceptor
import com.twlk.lib.hook.OkhttpInitializer
import com.twlk.lib.hook.RequestHook

class CaptureInitializer : Initializer<Any>, IOkhttpClientInterceptor {

    private lateinit var notify: IRequestNotify
    private lateinit var context: Context

    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            service?.linkToDeath({ startService() }, 0)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            startService()
        }
    }

    override fun create(context: Context): Any {
        this.context = context
        RequestHook.getChain().addInterceptor(this)
        notify = OkhttpRequestNotify(context)
        startService()
        return 0
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        val list = ArrayList<Class<out Initializer<*>>>()
        list.add(OkhttpInitializer::class.java)
        return list
    }

    override fun onDisposeClient(builder: okhttp3.OkHttpClient.Builder) {
        builder.addInterceptor(CaptureInterceptor(notify))
    }

    private fun startService() {
        val intent = Intent()
        intent.action = Constant.SERVER_ACTION
        intent.setPackage(Constant.SERVER_PACKAGE)
        val manager: PackageManager = context.packageManager
        val list = manager.queryIntentServices(intent, 0)
        if (list.isEmpty()) {
            return
        }
        val ret = context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
}