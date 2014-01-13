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
 *  TODO: 其实只需建一个video表，然后其他表关联aid即可
 */
public class DBOpenHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "ac.db";
    public static final int DB_VERSION = 2;
    public static final String TABLE_USER = "user";
    public static final String TABLE_FAV = "fav";
    public static final String TABLE_HISTORY = "history";
    public static final String TABLE_WATCH_LATER = "later";
    public DBOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    private static String CREATE_TABLE_WATCH_LATER =
            "CREATE TABLE "+TABLE_WATCH_LATER+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,aid INTEGER UNIQUE,title VARCHAR(200), channelId INTEGER, preview TEXT, time INTEGER)";
    
    private static String CREATE_TABLE_USER = 
            "CREATE TABLE "+TABLE_USER+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,uid INTEGER,name VARCHAR(25),avatar TEXT,signature TEXT,cookies TEXT,time INTEGER)";
    
    private static String CREATE_TABLE_FAV =
            "CREATE TABLE "+TABLE_FAV+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,aid INTEGER UNIQUE,title VARCHAR(200),desc TEXT, preview TEXT, channelId INTEGER, favs INTEGER, time INTEGER)";
    
    private static String CREATE_TABLE_HISTORY =
            "CREATE TABLE "+TABLE_HISTORY+" (_id INTEGER PRIMARY KEY AUTOINCREMENT,aid INTEGER UNIQUE,title VARCHAR(200), channelId INTEGER, preview TEXT, time INTEGER)";
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USER);
        db.execSQL(CREATE_TABLE_FAV);
        db.execSQL(CREATE_TABLE_HISTORY);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}
