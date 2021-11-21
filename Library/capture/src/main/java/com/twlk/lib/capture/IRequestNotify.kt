package com.twlk.lib.capture

import android.net.Uri
import okio.Buffer

interface IRequestNotify {

    fun onRequest(url: String): NotifyData?

    fun onRequestStart(data: NotifyData, headers: Map<String, String>, body: Buffer?)
    fun onRequestFinish(data: NotifyData, headers: Map<String, String>, body: Buffer?)

    fun onRequestError(data: NotifyData, error: String)
}

data class NotifyData(val url: String, val id: String, val requestUri: Uri, val responseUri: Uri)