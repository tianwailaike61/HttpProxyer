package com.twlk.httpserver.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {HttpData.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract HttpDao httpDao();
}
