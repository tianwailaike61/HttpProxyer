package com.twlk.httpserver.controller

import android.util.Log
import com.yanzhenjie.andserver.annotation.Controller
import com.yanzhenjie.andserver.annotation.GetMapping
import com.yanzhenjie.andserver.http.HttpRequest

@Controller
class PageController {

    @GetMapping("/")
    fun index(): String {
        return "forward:/index.html"
    }

    @GetMapping("/connection")
    fun getConnection(request: HttpRequest) {
        val localAddr = request.localAddr   // HostAddress
        val localName = request.localName   // HostName
        val localPort = request.localPort   // server's port

        val remoteAddr = request.remoteAddr  // HostAddress
        val remoteHost = request.remoteHost  // Especially HostName, second HostAddress
        val remotePort = request.remotePort  // client's port
        Log.e(
            "jhk", "localAddr:$localAddr localName:$localName localPort:$localPort" +
                    " remoteAddr:$remoteAddr remoteHost:$remoteHost remotePort:$remotePort"
        )
    }
}