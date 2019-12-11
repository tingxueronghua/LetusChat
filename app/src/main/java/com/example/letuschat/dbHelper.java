package com.example.letuschat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME="chatting.db";
    private static final int VERSION=1;
    private static final String CREATE_TABLE_TEST="create table test(_id integer primary key autoincrement,"+ "name text,age integer)";
    private static final String DROP_TABLE_TEST="DROP TABLE IF EXISTS test";
    public dbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
    {
        super(context, DB_NAME, factory, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE_TEST);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE_TEST);
        db.execSQL(CREATE_TABLE_TEST);
    }
}
