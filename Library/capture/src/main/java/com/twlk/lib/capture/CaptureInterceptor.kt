package com.twlk.lib.capture

import android.util.Log
import androidx.collection.ArrayMap
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import okio.GzipSource

class CaptureInterceptor(private val notify: IRequestNotify) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val data = notify.onRequest(request.url.toString()) ?: return chain.proceed(request)
        handleRequest(request, data)
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            notify.onRequestError(data, Log.getStackTraceString(e))
            throw e
        }
        handleResponse(response, data)
        return response
    }

    private fun handleRequest(request: Request, data: NotifyData) {
        val headerMap = ArrayMap<String, String>()
        headerMap["method"] = request.method
        val headers = request.headers
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            headerMap[name] = headers.value(i)
        }

        var buffer: Buffer? = null
        request.body?.let {
            headerMap["Content-Length"] = it.contentLength().toString()
            it.contentType()?.let { type ->
                headerMap["Content-Type"] = type.toString()
            }

            val buf = Buffer()
            it.writeTo(buf)
            buffer = buf
        }

        notify.onRequestStart(data, headerMap, buffer)
    }

    private fun handleResponse(response: Response, data: NotifyData) {
        val headerMap = ArrayMap<String, String>()
        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        headerMap["Content-Length"] = bodySize
        val headers = response.headers
        for (i in 0 until headers.size) {
            val name = headers.name(i)
            headerMap[name] = headers.value(i)
        }

        val buf: Buffer?
        if (response.promisesBody() || bodyHasUnknownEncoding(headers)) {
            buf = null
        } else {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer

            var gzippedLength: Long? = null
            if ("gzip".equals(headers["Content-Encoding"], ignoreCase = true)) {
                gzippedLength = buffer.size
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }

            if (gzippedLength != null) {
                headerMap["Gzip-Length"] = gzippedLength.toString()
            }

            buf = buffer.clone()
        }
        notify.onRequestFinish(data, headerMap, buf)
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
                !contentEncoding.equals("gzip", ignoreCase = true)
    }
}