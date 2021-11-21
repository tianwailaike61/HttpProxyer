package com.twlk.httpserver.utils

import com.twlk.httpserver.room.AppDatabase
import com.twlk.httpserver.room.HttpDao

object DBUtils {
    lateinit var db: AppDatabase

    fun init(db: AppDatabase) {
        this.db = db
    }

    fun httpDao(): HttpDao = db.httpDao()
}