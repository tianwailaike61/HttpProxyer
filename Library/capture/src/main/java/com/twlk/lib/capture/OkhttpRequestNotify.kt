package com.twlk.lib.capture

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.collection.ArrayMap
import com.alibaba.fastjson.JSON
import com.twlk.core.Constant
import okhttp3.internal.closeQuietly
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class OkhttpRequestNotify(private val context: Context) : IRequestNotify {

    private val format: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS", Locale.CHINA)

    override fun onRequest(url: String): NotifyData? {
        val value = ContentValues()
        val id = UUID.randomUUID().toString()
        value.put("id", id)
        value.put("url", url)
        try {
            context.contentResolver.insert(Constant.HTTP_URI, value) ?: return null
        } catch (e: IllegalArgumentException) {
            Log.e("jhk", "===" + Log.getStackTraceString(e))
            return null
        }
        val requestUri = Uri.parse("$Constant.HTTP_URL_STR-$id")
        val responseUri = Uri.parse("$Constant.HTTP_URL_STR+$id")
        return NotifyData(url, id, requestUri, responseUri)
    }

    override fun onRequestStart(
        data: NotifyData,
        headers: Map<String, String>,
        body: okio.Buffer?
    ) {
        body?.let {
            writeBody(data.requestUri, it)
        }
        updateRequest(data, Constant.HTTP_START, JSON.toJSONString(headers))
    }

    override fun onRequestFinish(
        data: NotifyData,
        headers: Map<String, String>,
        body: okio.Buffer?
    ) {
        body?.let {
            writeBody(data.responseUri, it)
        }
        updateRequest(data, Constant.HTTP_FINISH, JSON.toJSONString(headers))
    }

    override fun onRequestError(data: NotifyData, error: String) {
        val map = ArrayMap<String, String>(1)
        map["error"] = error
        updateRequest(data, Constant.HTTP_ERROR, JSON.toJSONString(map))
    }

    private fun writeBody(uri: Uri, body: okio.Buffer) {
        if (!body.isProbablyUtf8()) {
            return
        }

        context.contentResolver.openFileDescriptor(uri, "w")?.let { pfd ->
            FileOutputStream(pfd.fileDescriptor).use {
                body.writeTo(it)
            }
            pfd.closeQuietly()
        }
    }


    private fun updateRequest(data: NotifyData, state: Int, result: String) {
        val value = ContentValues()
        value.put("state", state)
        value.put("time", format.format(Date()))
        value.put("result", result)
        context.contentResolver.update(Constant.HTTP_URI, value, "id=?", arrayOf(data.id))
    }
}