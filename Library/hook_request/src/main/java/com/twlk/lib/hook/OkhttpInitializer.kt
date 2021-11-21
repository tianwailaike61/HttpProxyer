package com.twlk.lib.hook

import android.content.Context
import androidx.startup.Initializer
import java.util.*

class OkhttpInitializer : Initializer<Any> {
    override fun create(context: Context): Any {
        RequestHook.init(context)
        return 0
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = Collections.emptyList()
}