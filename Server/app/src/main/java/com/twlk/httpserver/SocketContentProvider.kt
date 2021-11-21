package com.twlk.httpserver

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.room.Room
import com.twlk.httpserver.provider.AbsProvider
import com.twlk.httpserver.provider.HttpProvider
import com.twlk.httpserver.room.AppDatabase
import com.twlk.httpserver.utils.DBUtils

class SocketContentProvider : ContentProvider() {

    private lateinit var db: AppDatabase

    private val realProviders: MutableMap<Int, AbsProvider> by lazy {
        HashMap()
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun attachInfo(context: Context, info: ProviderInfo?) {
        super.attachInfo(context, info)
        db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "httpData.db"
        ).build()
        DBUtils.init(db)
        realProviders[HttpProvider.HTTP_CODE] = HttpProvider(matcher, db)
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val ret = matcher.match(uri)
        if (ret < 0) {
            return null
        }
        realProviders[ret]?.let {
            return it.query(uri, projection, selection, selectionArgs, sortOrder)
        }
        return null
    }

    override fun getType(uri: Uri): String? {
        Log.e("jhk", "==getType==$uri")
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val ret = matcher.match(uri)
        if (ret < 0) {
            return null
        }
        realProviders[ret]?.let {
            return it.insert(uri, values).also { u ->
                if (u != null) {
                    context?.contentResolver?.notifyChange(u, null)
                }
            }
        }
        return uri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val ret = matcher.match(uri)
        if (ret < 0) {
            return 0
        }
        realProviders[ret]?.let {
            return it.delete(uri, selection, selectionArgs).also {
                context?.contentResolver?.notifyChange(uri, null)
            }
        }
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val ret = matcher.match(uri)
        if (ret < 0) {
            return 0
        }
        realProviders[ret]?.let {
            return it.update(uri, values, selection, selectionArgs).also {
                context?.contentResolver?.notifyChange(uri, null)
            }
        }
        return 0
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        val ret = matcher.match(uri)
        if (ret < 0) {
            return null
        }
        realProviders[ret]?.let {
            return it.openFile(uri, mode)
        }
        return null
    }

    override fun openFile(
        uri: Uri,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor? {
        val ret = matcher.match(uri)
        if (ret < 0) {
            return null
        }
        realProviders[ret]?.let {
            return it.openFile(uri, mode)
        }
        return null
    }


    companion object {
        val matcher = UriMatcher(UriMatcher.NO_MATCH)
    }
}