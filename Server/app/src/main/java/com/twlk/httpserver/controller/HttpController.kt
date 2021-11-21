package com.twlk.httpserver.controller

import androidx.collection.ArrayMap
import com.twlk.core.Constant
import com.twlk.httpserver.room.HttpData
import com.twlk.httpserver.service.WebService
import com.twlk.httpserver.utils.DBUtils
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.annotation.RequestMapping
import com.yanzhenjie.andserver.annotation.RequestParam
import com.yanzhenjie.andserver.annotation.RestController
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.util.MediaType
import java.io.File
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@RestController
@RequestMapping(path = ["/http"])
class HttpController {

    @GetMapping(path = ["/urls"], produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun getAllUrls(@RequestParam(name = "index") index: Int = 0): Map<String, Any> {
        val map = ArrayMap<String, Any>(2)
        map["index"] = WebService.httpIndex
        map["urls"] = emptyList<Map<String, Any>>()
        if (index == WebService.httpIndex) {
            return map
        }
        val list = DBUtils.httpDao().getAll()
        if (list.isNullOrEmpty()) {
            return map

        }
        val data = ArrayList<ArrayMap<String, Any>>()
        val dataMap = HashMap<String, MutableList<HttpData>>()
        list.forEach {
            val l: MutableList<HttpData>
            dataMap[it.domain].run {
                if (this != null) {
                    l = this
                } else {
                    l = ArrayList()
                    val m = ArrayMap<String, Any>()
                    m["key"] = it.domain
                    m["state"] = it.state
                    m["list"] = l
                    data.add(m)
                    dataMap[it.domain] = l
                }
            }
            l.add(it)
        }
        map["urls"] = data
        return map
    }

    private fun readBody(id: String, isRequest: Boolean): String {
        val file = File("", id)
        if (!file.exists()) {
            return "no data"
        }
        return file.readText()
    }

    @GetMapping("/detail")
    fun httpDetail(
        request: HttpRequest,
        response: HttpResponse,
        @RequestParam(name = "id") id: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        val data = DBUtils.httpDao().findById(id)
        if (data == null) {
            map["state"] = Constant.HTTP_ERROR
            map["head"] = "no request"
            return map
        } else {
            map["state"] = data.state
            map["head"] = data.toMap()
        }
        map["request"] = readBody(id, true)
        when (data.state) {
            Constant.HTTP_START -> {
                map["response"] = "loading"
            }
            Constant.HTTP_FINISH -> {
                map["response"] = readBody(id, false)
            }
            else -> {
                map["response"] = data.responseData
            }
        }
        return map
    }

    @GetMapping("/response")
    fun responseDetail(
        request: HttpRequest,
        response: HttpResponse,
        @RequestParam(name = "id") id: String
    ): Map<String, Any> {
        val map = HashMap<String, Any>()
        val data = DBUtils.httpDao().findById(id)
        if (data == null) {
            map["state"] = Constant.HTTP_ERROR
            map["response"] = ""
        } else {
            map["state"] = data.state
            when (data.state) {
                Constant.HTTP_START -> {
                    map["response"] = "loading"
                }
                Constant.HTTP_FINISH -> {
                    map["response"] = readBody(id, false)
                }
                else -> {
                    map["response"] = data.responseData
                }
            }
        }
        return map
    }

    @GetMapping("/clear")
    fun clear(): String {
        DBUtils.httpDao().deleteAll()
        return "Success"
    }
}