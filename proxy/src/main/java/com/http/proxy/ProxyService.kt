/*
 * MIT License
 *
 * Copyright (c) 2020 tianwailaike61
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.http.proxy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.ding.library.internal.utils.CacheUtils
import com.http.proxy.proxy.HookProxy
import java.io.File
import java.net.InetSocketAddress
import java.net.Proxy

class ProxyService : Service() {

    private lateinit var defaultProxy: HookProxy

    val sp: SharedPreferences by lazy {
        getSharedPreferences("Proxy", MODE_PRIVATE)
    }

    private val appName: String by lazy {
        resources.getString(packageManager.getPackageInfo(packageName, 0).applicationInfo.labelRes)
    }

    private var openProxy = false

    override fun onCreate() {
        super.onCreate()
        openProxy = sp.getBoolean("open", false)
        defaultProxy = if (openProxy) {
            HookProxy(sp.getString("ip", "127.0.0.1")!!, sp.getInt("port", 8080))
        } else {
            val ip = sp.getString("ip", null)
            if (ip.isNullOrBlank()) {
                HookProxy(HookProxy.EMPTY)
            } else {
                HookProxy(ip, sp.getInt("port", 8080))
            }

        }
        showNotification(defaultProxy)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent ?: return super.onStartCommand(intent, flags, startId)
        when (intent.action) {
            UPDATE_PROXY -> {
                openProxy = intent.getBooleanExtra("openProxy", false)
                if (openProxy) {
                    RequestHook.proxy.set(defaultProxy)
                } else {
                    RequestHook.proxy.set(HookProxy.EMPTY)
                }
                showNotification(defaultProxy)
            }
            DELETE_LOG -> {
                CacheUtils.getInstance().cleanCache()
                Toast.makeText(this, R.string.clear_log, Toast.LENGTH_LONG).show()
            }
            SET_CANCELABLE -> {
                canCancel = intent.getBooleanExtra("cancelable", false)
                showNotification(defaultProxy)
            }
            else -> {
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        return ProxyBinder(defaultProxy, object : INotificationCallback {
            override fun update(proxy: HookProxy) {
                val editor = sp.edit()
                if (proxy.type == Proxy.Type.DIRECT) {
                    openProxy = false
                } else {
                    openProxy = true
                    val sa = proxy.sa as InetSocketAddress
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        editor.putString("ip", sa.hostString)
                    }
                    editor.putInt("port", sa.port)
                }
                editor.putBoolean("open", openProxy)
                defaultProxy = proxy
                editor.apply()
                showNotification(proxy)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            val manager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(NOTIFICATION_ID)
        }
    }

    private fun showNotification(proxy: HookProxy) {
        val manager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var channel = manager.getNotificationChannel("httpProxy")
            if (channel == null) {
                channel = NotificationChannel(
                    "httpProxy",
                    "foregroundName",
                    NotificationManager.IMPORTANCE_MIN
                )
                manager.createNotificationChannel(channel)
            }
        }
        val builder = NotificationCompat.Builder(this, "httpProxy")
        val remoteViews = RemoteViews(packageName, R.layout.layout_notification)
        remoteViews.setImageViewResource(R.id.iv_logo, R.drawable.alien_gray)
        remoteViews.setTextViewText(R.id.tv_app_name, appName)
        remoteViews.setTextViewText(R.id.tv_proxy_config, proxy.toString())
        if (openProxy) {
            remoteViews.setTextColor(R.id.tv_proxy_config, resources.getColor(R.color.black))
        } else {
            remoteViews.setTextColor(R.id.tv_proxy_config, resources.getColor(R.color.color_d5d5d5))
        }
        updateSwitch(remoteViews, proxy)
        updateDelete(remoteViews)
        builder.setContent(remoteViews)
            .setSmallIcon(R.mipmap.ic_launcher_round)
        val resultIntent = Intent(this, RequestActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val resultPendingIntent =
            PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(resultPendingIntent)
            .setSmallIcon(R.drawable.alien_gray)
            .setAutoCancel(canCancel)
            .setOngoing(true)
        val notification = builder.build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            startForeground(NOTIFICATION_ID, notification)
        } else {
            manager.notify(NOTIFICATION_ID, notification)
        }
    }

    private fun updateSwitch(remoteViews: RemoteViews, proxy: HookProxy) {
        if (proxy == HookProxy.EMPTY) {
            remoteViews.setViewVisibility(R.id.proxy_switch, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.proxy_switch, View.VISIBLE)
            val resourceId: Int = if (openProxy) {
                android.R.drawable.ic_media_pause
            } else {
                android.R.drawable.ic_media_play
            }
            remoteViews.setImageViewResource(R.id.proxy_switch, resourceId)
        }
        val intent = Intent(this, ProxyService::class.java)
        intent.action = UPDATE_PROXY
        intent.putExtra("openProxy", !openProxy)
        val pendingIntent =
            PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.proxy_switch, pendingIntent)
    }

    private fun updateDelete(remoteViews: RemoteViews) {
        remoteViews.setImageViewResource(R.id.iv_delete, android.R.drawable.ic_menu_delete)
        val intent = Intent(this, ProxyService::class.java)
        intent.action = DELETE_LOG
        val pendingIntent =
            PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.iv_delete, pendingIntent)
    }

    companion object {
        const val NOTIFICATION_ID = 104

        const val UPDATE_PROXY: String = "UPDATE_PROXY"
        const val DELETE_LOG: String = "DELETE_LOG"
        const val SET_CANCELABLE: String = "SET_CANCELABLE"

        private var canCancel: Boolean = false

        fun setCancelable(cancelable: Boolean, context: Context) {
            if (cancelable == canCancel) {
                return
            }
            val intent = Intent(context, ProxyService::class.java)
            intent.action = SET_CANCELABLE
            intent.putExtra("cancelable", cancelable)
            start(context.applicationContext, intent)
        }

        fun start(context: Context) {
            start(context, Intent(context, ProxyService::class.java))
        }

        private fun start(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}

interface INotificationCallback {
    fun update(proxy: HookProxy)
}

class ProxyBinder(
    private var proxy: HookProxy,
    private val callback: INotificationCallback
) :
    Binder() {

    private val proxyList = HashSet<HookProxy>()

    init {
        proxyList.add(HookProxy.EMPTY)
        val file = File("/sdcard/proxy.config")
        if (file.exists()) {
            val list = file.readLines()
            list.forEach {
                val ss = it.trim().split(' ')
                if (ss.size == 2) {
                    proxyList.add(HookProxy(ss[0].trim(), ss[1].trim().toInt()))
                }
            }
        }
    }

    fun getProxyList(): Collection<HookProxy> {
        return proxyList
    }

    fun getCurrentProxy(): HookProxy = proxy

    fun setProxy(proxy: HookProxy) {
        this.proxy = proxy
        RequestHook.proxy.set(proxy)
        callback.update(proxy)
    }
}