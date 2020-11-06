//package com.http.proxy
//
//import androidx.collection.ArrayMap
//import com.alibaba.fastjson.JSON
//import okhttp3.*
//import java.io.ByteArrayOutputStream
//import java.io.IOException
//import java.io.OutputStreamWriter
//import java.util.*
//
//class CaptureInfoInterceptor : Interceptor {
//    private val local = ThreadLocal<ByteArrayOutputStream>()
//
//    @Throws(IOException::class)
//    override fun intercept(chain: Interceptor.Chain): Response {
//        val olderRequest = chain.request()
//        val url = olderRequest.url.toString()
//        val tag = UUID.randomUUID().toString()
//        var outputStream = local.get()
//        if (outputStream == null) {
//            outputStream = ByteArrayOutputStream()
//            local.set(outputStream)
//        }
//
//        OutputStreamWriter(outputStream).use { writer ->
//            reportRequest(tag, url, olderRequest, writer, outputStream)
//        }
//        outputStream.reset()
//        val oldResponse: Response = chain.proceed(olderRequest)
//
//        OutputStreamWriter(outputStream).use { writer ->
//            reportResponse(tag, url, oldResponse, writer, outputStream)
//        }
//        outputStream.reset()
//        return oldResponse
//    }
//
//    @Throws(IOException::class)
//    private fun reportRequest(
//        tag: String,
//        url: String,
//        request: Request,
//        writer: OutputStreamWriter,
//        outputStream: ByteArrayOutputStream
//    ) {
//        val requestBody = request.body
//        writer.append("Method:").append(request.method).append('\n')
//        writer.append("Header:")
//        if (requestBody != null) {
//            val type = requestBody.contentType()
//            if (type != null) {
//                writer.append("Content-Type:").append(type.toString()).append(';')
//            }
//            val length = requestBody.contentLength()
//            if (length != -1L) {
//                writer.append("Content-Length:").append(length.toString())
//                    .append(';')
//            }
//        }
//        readHeaders(request.headers, writer)
//        writer.append('\n')
//        if (!bodyEncoded(request.headers)) {
//            val map = readBody(requestBody)
//            if (!map.isEmpty) {
//                writer.append("Param:").append(JSON.toJSONString(map))
//            }
//        }
//        writer.append('\n')
//        writer.flush()
//        RequestDisposer.addRequestLog(RequestType.REQUEST, tag, url, outputStream.toByteArray())
//    }
//
//    @Throws(IOException::class)
//    fun reportResponse(
//        tag: String,
//        url: String,
//        response: Response,
//        writer: OutputStreamWriter,
//        outputStream: ByteArrayOutputStream
//    ) {
//        val code = response.code
//        writer.append("Code:").append(code.toString()).append('\n')
//        writer.append("Header:")
//        val respHeaders = response.headers
//        readHeaders(respHeaders, writer)
//        writer.append('\n')
//        val responseBody = response.body
//        writer.append("Body:")
//        writer.flush()
//        if (!bodyEncoded(respHeaders) && responseBody != null) {
//            val type = responseBody.contentType()
//            if (type != null && type.subtype.toLowerCase(Locale.ROOT) == "json") {
//                val source = responseBody.source()
//                source.request(Long.MAX_VALUE) // Buffer the entire body.
//                source.buffer.copyTo(outputStream)
//            }
//        }
//        writer.append('\n')
//        writer.flush()
//        RequestDisposer.addRequestLog(RequestType.RESPONSE, tag, url, outputStream.toByteArray())
//    }
//
//    private fun bodyEncoded(headers: Headers): Boolean {
//        val contentEncoding = headers["Content-Encoding"]
//        return contentEncoding != null && !contentEncoding.equals("identity", ignoreCase = true)
//    }
//
//    companion object {
//        private val EMPTY_MAP = ArrayMap<String, String>(0)
//        private fun readBody(body: RequestBody?): ArrayMap<String, String> {
//            if (body == null) {
//                return EMPTY_MAP
//            }
//            val map = ArrayMap<String, String>()
//            if (body is FormBody) {
//                readFormBody(body, map)
//            } else if (body is MultipartBody) {
//                readMultipartBody(body, map)
//            }
//            return map
//        }
//
//        private fun readBody(body: RequestBody?, map: ArrayMap<String, String>) {
//            map.clear()
//            if (body == null) {
//                return
//            }
//            if (body is FormBody) {
//                readFormBody(body, map)
//            } else if (body is MultipartBody) {
//                readMultipartBody(body, map)
//            }
//        }
//
//        private fun readFormBody(body: FormBody, map: ArrayMap<String, String>) {
//            map.clear()
//            for (i in 0 until body.size) {
//                map[body.name(i)] = body.value(i)
//            }
//        }
//
//        private fun readMultipartBody(body: MultipartBody, map: ArrayMap<String, String>) {
//            map.clear()
//            var headerStr: String
//            var bodyStr: String
//            val temp = ArrayMap<String, String>()
//            var isUpload = false
//            for (part in body.parts) {
//                val headers = part.headers
//                if (headers != null) {
//                    isUpload = headers.values("filename").isNotEmpty()
//                    readHeaders(part.headers, temp)
//                    headerStr = JSON.toJSONString(temp)
//                } else {
//                    headerStr = ""
//                }
//                bodyStr = if (isUpload) {
//                    readBody(part.body, temp)
//                    JSON.toJSONString(temp)
//                } else {
//                    ""
//                }
//                map[headerStr] = bodyStr
//            }
//        }
//
//        fun readHeaders(headers: Headers?): ArrayMap<String, String> {
//            if (headers == null || headers.size == 0) {
//                return EMPTY_MAP
//            }
//            val map = ArrayMap<String, String>(headers.size)
//            readHeaders(headers, map)
//            return map
//        }
//
//        private fun readHeaders(headers: Headers?, map: ArrayMap<String, String>) {
//            map.clear()
//            if (headers == null || headers.size == 0) {
//                return
//            }
//            var i = 0
//            val count = headers.size
//            while (i < count) {
//                map[headers.name(i)] = headers.value(i)
//                i++
//            }
//        }
//
//        @Throws(IOException::class)
//        fun readHeaders(headers: Headers?, writer: OutputStreamWriter) {
//            if (headers == null || headers.size == 0) {
//                return
//            }
//            var i = 0
//            val count = headers.size
//            while (i < count) {
//                writer.append(headers.name(i)).append(": ").append(headers.value(i)).append(';')
//                i++
//            }
//        }
//    }
//}