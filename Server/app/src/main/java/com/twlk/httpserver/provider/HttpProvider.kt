package com.twlk.httpserver.provider

import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.util.Log
import com.twlk.core.Constant
import com.twlk.httpserver.room.AppDatabase
import com.twlk.httpserver.room.HttpDao
import com.twlk.httpserver.room.HttpData

class HttpProvider(matcher: UriMatcher, db: AppDatabase) : AbsProvider(matcher, db) {

    private var dao: HttpDao = db.httpDao()

    init {
        matcher.addURI(Constant.AUTHORITY, "http", HTTP_CODE)

//        val ret = matcher.match(Uri.parse("content://" + SocketContentProvider.AUTHORITY + "/http"))
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        if (selection == null || selectionArgs.isNullOrEmpty()) {
            return null
        }
        return dao.query(selectionArgs[0])
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return if (values == null) {
            null
        } else {
            val data = HttpData.get(values)
            dao.insertAll(data)
            uri
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return if (selectionArgs == null) {
            0
        } else {
            var count = 0
            selectionArgs.forEach { id ->
                dao.findById(id)?.let { data ->
                    data.update(values, count)
                    dao.update(data)
                    count++
                }

            }
            count
        }
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        Log.e("jhk", " openFile==$uri")
        return null
    }

    override fun openFile(
        uri: Uri,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor? {
        return null
    }

    companion object {
        const val HTTP_CODE = 1
    }
}