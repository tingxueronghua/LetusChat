package com.example.letuschat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;

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
        db.close();
    }
    public void db_delete(String tablename, String id)
    {
        SQLiteDatabase db = dbhelper.getWritableDatabase();
        String[] ids = {id};
        db.delete(tablename, "_id=?", ids);
        db.close();
    }
    public ArrayList<String> db_all_friend(String tablename)
    {
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.query(tablename, new String[]{"name"}, "kind=?", new String[]{"friend"}, null, null, null, null);
        ArrayList<String> friends = new ArrayList<>();
        while (c.moveToNext())
        {
            friends.add(c.getString(c.getColumnIndexOrThrow("name")));
        }
        c.close();
        db.close();
        return friends;
    }
}
