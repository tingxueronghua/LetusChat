package com.example.letuschat;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Iterator;
import java.util.Map;

public class chatting_utils {
    public static void save_note(Context context, String filename, Map<String, String> map)
    {
        SharedPreferences.Editor note = context.getSharedPreferences(filename, Context.MODE_PRIVATE).edit();
        Iterator<Map.Entry<String, String>> it=map.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            note.putString(entry.getKey(), entry.getValue());
        }
        note.commit();
    }
    public static String get_note(Context context, String filename, String dataname)
    {
        SharedPreferences read=context.getSharedPreferences(filename, context.MODE_PRIVATE);
        return read.getString(dataname, null);
    }

}
