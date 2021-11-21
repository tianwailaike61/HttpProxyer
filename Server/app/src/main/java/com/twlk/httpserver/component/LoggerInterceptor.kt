package com.twlk.httpserver.component

import com.yanzhenjie.andserver.annotation.Interceptor
import com.yanzhenjie.andserver.framework.HandlerInterceptor
import com.yanzhenjie.andserver.framework.handler.RequestHandler
import com.yanzhenjie.andserver.http.HttpMethod
import com.yanzhenjie.andserver.http.HttpRequest
import com.yanzhenjie.andserver.http.HttpResponse
import com.yanzhenjie.andserver.util.MultiValueMap

@Interceptor
class LoggerInterceptor : HandlerInterceptor {
    override fun onIntercept(
        request: HttpRequest,
        response: HttpResponse,
        handler: RequestHandler
    ): Boolean {
        val path = request.path
        val method: HttpMethod = request.method
        val valueMap: MultiValueMap<String, String> = request.parameter
//        Log.e("jhk","Path: $path")
//        Log.e("jhk","Method: " + method.value())
//        Log.e("jhk","Param: " + JSON.toJSONString(valueMap))
        return false
    }
}