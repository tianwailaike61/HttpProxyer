package com.twlk.lib.hook

import okhttp3.OkHttpClient

class HookChain : IClientInterceptorAdd {
    private val list: MutableList<IOkhttpClientInterceptor> by lazy {
        ArrayList()
    }

    override fun addInterceptor(interceptor: IOkhttpClientInterceptor) {
        list.add(interceptor)
    }

    public fun intercept(builder: OkHttpClient.Builder) {

    }
}