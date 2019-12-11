package com.example.letuschat;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DatabaseAdapter {
    private dbHelper dbhelper;
    public DatabaseAdapter(Context context, String name)
    {
        dbhelper = new dbHelper(context, name, null, 1);
    }
    public void db_add(String tablename, ContentValues values)
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        db.insert(tablename, null, values);
    }
    public void db_delete(String tablename, String id)
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] ids = {id};
        db.delete(tablename, "_id=?", ids);
    }
}
