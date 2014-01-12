/*
 * Copyright (C) 2013 YROM.NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.acfun.video.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Yrom
 *
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "ac.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_USER = "user";
    public static final String TABLE_FAV = "fav";
    public static final String TABLE_HISTORY = "history";
    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_USER+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,uid INTEGER,name VARCHAR(25),avatar TEXT,signature TEXT,cookies TEXT,time INTEGER)");
        db.execSQL("CREATE TABLE "+TABLE_FAV+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,aid INTEGER UNIQUE,title VARCHAR(200),desc TEXT, preview TEXT, channelId INTEGER, favs INTEGER, time INTEGER)");
        db.execSQL("CREATE TABLE "+TABLE_HISTORY+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,aid INTEGER UNIQUE,title VARCHAR(200), channelId INTEGER, preview TEXT, time INTEGER)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
