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

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.startup.Initializer
import com.ding.library.internal.utils.CaptureContext
import java.util.*

class ProxyInitializer : Initializer<Proxy> {
    override fun create(context: Context): Proxy {
        val proxy = Proxy()
        CaptureContext.appContext = context
        RequestHook.init(context)
        proxy.start(context)
        return proxy
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = Collections.emptyList()
}

class Proxy : Application.ActivityLifecycleCallbacks {

    private var count = 0

    fun start(context: Context) {
        ProxyService.start(context)
        if (context is Application) {
            context.registerActivityLifecycleCallbacks(this)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        count++
        if (count == 1) {
            ProxyService.setCancelable(false, activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        count--
        if (count == 0) {
            ProxyService.setCancelable(true, activity)
        }
    }


}