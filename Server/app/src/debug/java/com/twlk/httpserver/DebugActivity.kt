package com.twlk.httpserver

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.twlk.core.Constant
import com.twlk.httpserver.service.WebService

class DebugActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        val intent = Intent(this, WebService::class.java)
        startService(intent)

        findViewById<View>(R.id.bt_test).setOnClickListener {
            Thread {
                val cursor = contentResolver.query(
                    Constant.HTTP_URI,
                    null,
                    "id=?",
                    arrayOf("1"),
                    null
                )
                Log.e("jhk", "====$cursor")
                cursor?.close()
            }.start()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}