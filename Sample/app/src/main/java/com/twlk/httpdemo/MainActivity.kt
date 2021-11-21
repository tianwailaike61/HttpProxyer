package com.twlk.httpdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.twlk.lib.capture.CaptureInterceptor
import com.twlk.lib.capture.OkhttpRequestNotify
import okhttp3.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var client: OkHttpClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val interceptor = CaptureInterceptor(OkhttpRequestNotify(this.applicationContext))

        val builder = OkHttpClient.Builder()
        builder.addInterceptor(interceptor)
        client = builder.build()

        findViewById<Button>(R.id.bt_baidu).setOnClickListener {
            val request = Request.Builder().url("https://www.baidu.com").build()
            request(request)
        }
        findViewById<Button>(R.id.bt_hao123).setOnClickListener {
            val request = Request.Builder().url("https://www.hao123.com").build()
            request(request)
        }
    }

    private fun request(request: Request) {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {

            }

            override fun onResponse(call: Call, response: Response) {

            }

        })
    }
}