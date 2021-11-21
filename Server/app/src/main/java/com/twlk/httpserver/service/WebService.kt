package com.twlk.httpserver.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.twlk.core.Constant
import com.twlk.core.IServer
import com.twlk.httpserver.R
import com.twlk.httpserver.utils.NetUtils
import com.yanzhenjie.andserver.AndServer
import com.yanzhenjie.andserver.Server
import java.util.concurrent.TimeUnit


class WebService : Service() {

    private lateinit var server: Server

    override fun onCreate() {
        super.onCreate()
        showNotification("start")
        server = AndServer.webServer(this)
            .port(8282)
            .timeout(10, TimeUnit.SECONDS)
            .listener(object : Server.ServerListener {
                override fun onStarted() {
                    showNotification("http:/${NetUtils.getLocalIPAddress()}:8282")
                }

                override fun onStopped() {
                    showNotification("stop")
                }

                override fun onException(e: Exception?) {
                    Log.e("jhk", "server error: ${Log.getStackTraceString(e)}")
                }

            })
            .build()
        server.startup()
        contentResolver.registerContentObserver(Constant.HTTP_URI, false,
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    super.onChange(selfChange)
                    httpIndex++
                }
            })
    }

    override fun onBind(intent: Intent?): IBinder {
        return object : IServer.Stub() {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
        }
        if (server.isRunning) {
            server.shutdown()
        }
    }

    private fun showNotification(msg: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            var channel = manager.getNotificationChannel(CHANNEL_ID)
            if (channel == null) {
                channel =
                    NotificationChannel(CHANNEL_ID, "111", NotificationManager.IMPORTANCE_HIGH)
                manager.createNotificationChannel(channel)
            }
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
        builder.setSmallIcon(R.drawable.ic_launcher_background)
            .setContentText("11")
            .setContentText(msg)
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                startForeground(NOTIFICATION_ID, builder.build(), FOREGROUND_SERVICE_TYPE_DATA_SYNC)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                startForeground(NOTIFICATION_ID, builder.build())
            }
            else -> {
                manager.notify(NOTIFICATION_ID, builder.build())
            }
        }

    }

    companion object {
        const val NOTIFICATION_ID = 108
        const val CHANNEL_ID = "test"

        var httpIndex = 0
    }
}