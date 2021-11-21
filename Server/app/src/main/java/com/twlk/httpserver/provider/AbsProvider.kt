package com.twlk.httpserver.provider

import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import com.twlk.httpserver.room.AppDatabase

abstract class AbsProvider(matcher: UriMatcher, db: AppDatabase) {

    abstract fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor?

    abstract fun insert(uri: Uri, values: ContentValues?): Uri?

    abstract fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int

    abstract fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int

    abstract fun openFile(uri: Uri, mode: String): ParcelFileDescriptor?

    abstract fun openFile(
        uri: Uri,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor?
}