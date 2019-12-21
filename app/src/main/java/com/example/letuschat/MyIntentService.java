package com.example.letuschat;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */

public class MyIntentService extends IntentService {
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_MSG = "com.example.letuschat.action.MSG";
    private static final String ACTION_START = "com.example.letuschat.action.START";

    private static final String EXTRA_PARAM1 = "original_id";
    private static final String EXTRA_PARAM2 = "record";
    private static final String EXTRA_PARAM3 = "friend_id";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionMSG(Context context, String original_id, String record, String friend_id) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_MSG);
        intent.putExtra(EXTRA_PARAM1, original_id);
        intent.putExtra(EXTRA_PARAM2, record);
        intent.putExtra(EXTRA_PARAM3, friend_id);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionSTART(Context context, String original_id) {
        Intent intent = new Intent(context, MyIntentService.class);
        intent.setAction(ACTION_START);
        intent.putExtra(EXTRA_PARAM1, original_id);
        context.startService(intent);
    }

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_MSG.equals(action)) {
                final String original_id = intent.getStringExtra(EXTRA_PARAM1);
                final String record = intent.getStringExtra(EXTRA_PARAM2);
                final String friend_id = intent.getStringExtra(EXTRA_PARAM3);
                handleActionMSG(original_id, record, friend_id);
            } else if (ACTION_START.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                handleActionSTART(param1);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionMSG(String original_id, String record, String friend_id) {
        //throw new UnsupportedOperationException("Not yet implemented");
        DatabaseAdapter dbadapter = new DatabaseAdapter(MyIntentService.this, original_id);
        ContentValues values = new ContentValues();
        values.put("name", friend_id);
        values.put("record", record);
        values.put("kind", "receive");
        values.put("date", getDateToString());
        dbadapter.db_add("singlechat", values);
    }
    public static String getDateToString()
    {
        long time = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        return simpleDateFormat.format(date);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionSTART(String param1) {
        //throw new UnsupportedOperationException("Not yet implemented");
        Log.i("Intent_service_start", "the start function is started");
        TcpServerStarter tcpServer = new TcpServerStarter(MyIntentService.this, 0, 20101, param1);
        tcpServer.start();
        TcpServerStarter tcpServer1 = new TcpServerStarter(MyIntentService.this, 1, 20102, param1);
        tcpServer1.start();
    }
    @Override
    public void onDestroy() {
        Log.i("Intentservice", "onDestroy - Thread ID = " + Thread.currentThread().getId());
        super.onDestroy();
    }
}
