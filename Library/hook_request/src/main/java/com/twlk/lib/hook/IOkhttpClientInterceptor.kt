package com.twlk.lib.hook

import okhttp3.OkHttpClient

interface IOkhttpClientInterceptor {

    fun onDisposeClient(builder: OkHttpClient.Builder)
}

interface IClientInterceptorAdd {
    fun addInterceptor(interceptor: IOkhttpClientInterceptor)
}