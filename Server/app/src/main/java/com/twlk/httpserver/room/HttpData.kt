package com.twlk.httpserver.room

import android.content.ContentValues
import androidx.collection.ArrayMap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.alibaba.fastjson.JSON
import com.twlk.core.Constant
import com.twlk.core.HttpColumn

@Entity
class HttpData {

    @PrimaryKey
    @ColumnInfo(name = HttpColumn.id)
    var id: String = ""

    @ColumnInfo(name = HttpColumn.state)
    var state: Int = -1

    @ColumnInfo(name = HttpColumn.domain)
    var domain: String = ""

    @ColumnInfo(name = HttpColumn.url)
    var url: String = ""

    @ColumnInfo(name = HttpColumn.requestTime)
    var requestTime: String = ""

    @ColumnInfo(name = HttpColumn.requestData)
    var requestData: String = ""

    @ColumnInfo(name = HttpColumn.responseTime)
    var responseTime: String = ""

    @ColumnInfo(name = HttpColumn.responseData)
    var responseData: String = ""

    fun update(values: ContentValues?, index: Int) {
        values ?: return
        state = values.getAsInteger(HttpColumn.state)
        when (state) {
            Constant.HTTP_START -> {
                doFillRequestData(values)
            }
            Constant.HTTP_FINISH -> {
                doFillResponseData(values)
            }
            Constant.HTTP_ERROR -> {
                doFillError(values)
            }
            else -> {
            }
        }
    }

    private fun doFillRequestData(values: ContentValues) {
        if (values.containsKey("time")) {
            requestTime = values.getAsString("time")
        }
        if (values.containsKey("result")) {
            requestData = values.getAsString("result")
        }
    }

    private fun doFillResponseData(values: ContentValues) {
        if (values.containsKey("time")) {
            responseTime = values.getAsString("time")
        }
        if (values.containsKey("result")) {
            responseData = values.getAsString("result")
        }
    }

    private fun doFillError(values: ContentValues) {
        if (values.containsKey("time")) {
            responseTime = values.getAsString("time")
        }
        if (values.containsKey("error")) {
            responseData = values.getAsString("error")
        }
    }

    fun toMap(): Map<String, Any> {
        val map = ArrayMap<String, Any>(2)
        val requestMap = ArrayMap<String, Any>()
        requestMap["time"] = requestTime
        if (requestData.isNotEmpty()) {
            val dataMap = JSON.parseObject(requestData, Map::class.java) as Map<String, String>
            requestMap.putAll(dataMap)
        }
        map["request"] = requestMap
        val responseMap = ArrayMap<String, Any>()
        responseMap["time"] = responseTime
        if (state == Constant.HTTP_FINISH && responseData.isNotEmpty()) {
            val dataMap =
                JSON.parseObject(responseData, Map::class.java) as Map<String, String>
            responseMap.putAll(dataMap)
        }
        map["response"] = responseMap
        return map
    }

    companion object {
        fun get(values: ContentValues): HttpData {
            val data = HttpData()
            data.id = values.getAsString(HttpColumn.id)
            val url = values.getAsString(HttpColumn.url)
            val start = url.indexOf("://")
            val domainSplit = url.indexOf('/', start + 3)
            data.domain = url.substring(0, domainSplit)
            data.url = url.substring(domainSplit, url.length)

            return data
        }
    }
}