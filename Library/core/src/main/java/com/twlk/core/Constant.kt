package com.twlk.core

import android.net.Uri

object Constant {
    const val SERVER_ACTION= "com.twlk.httpserver.SERVER_ACTION"
    const val SERVER_PACKAGE= "com.twlk.httpserver"

    const val AUTHORITY = "com.twlk.httpserver"
    const val HTTP_URL_STR = "content://$AUTHORITY/http/"
    val HTTP_URI = Uri.parse(HTTP_URL_STR)

    const val HTTP_START = 1
    const val HTTP_FINISH = 2
    const val HTTP_ERROR = -1
}