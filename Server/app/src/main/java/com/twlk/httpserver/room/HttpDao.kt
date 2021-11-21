package com.twlk.httpserver.room

import android.database.Cursor
import androidx.room.*

@Dao
abstract class HttpDao {

    @Query("SELECT * FROM httpData")
    abstract fun getAll(): List<HttpData>?

    @Query("SELECT * FROM httpData where id =:id")
    abstract fun findById(id: String): HttpData?

    @Query("SELECT * FROM httpData where id =:id")
    abstract fun query(id: String): Cursor?

    @Query("DELETE FROM httpData")
    abstract fun deleteAll()

    @Update
    abstract fun update(data: HttpData)

    @Insert
    abstract fun insertAll(vararg data: HttpData)

}