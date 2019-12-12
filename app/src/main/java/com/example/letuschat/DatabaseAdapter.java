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
    public ArrayList<String> db_find_name(String tablename, String friend_name)
    {
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.query(tablename, new String[]{"name"}, "name=?", new String[]{friend_name}, null, null, null, null);
        ArrayList<String> id_number = new ArrayList<>();
        while(c.moveToNext())
        {
            id_number.add(c.getString(c.getColumnIndexOrThrow("name")));
        }
        return id_number;
    }
    public String db_find_id(String tablename, String id)
    {
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        Cursor c = db.query(tablename, new String[]{"name"}, "_id=?", new String[]{id}, null, null, null, null);
        String str="";
        if(c.moveToNext())
        {
            str = c.getString(c.getColumnIndexOrThrow("name"));
        }
        return str;
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
