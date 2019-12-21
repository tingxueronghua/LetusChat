package com.example.letuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public ListView msglistview;
    public EditText inputText;
    public Button sendbtn;
    public MsgAdapter adapter;
    public List<Msg> msgList = new ArrayList<Msg>();
    public String original_name;
    public String id_number;
    private String address;
    private int friend_port = 20101;
    private DatabaseAdapter dbadapter;
    private String result_string="";
    private Uri result_uri;
    private Button send_file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        id_number = intent.getStringExtra("id");
        address = intent.getStringExtra("address");
        original_name = intent.getStringExtra("original_name");
        setContentView(R.layout.activity_main);
        // database adapter
        dbadapter = new DatabaseAdapter(MainActivity.this, original_name);
        // list view settings
        msgList.add(new Msg("I miss you!", Msg.RECEIVED));
        msgList.add(new Msg("I miss you, too!", Msg.SENT));
        ArrayList<DatabaseUtils> records = dbadapter.db_name_record("singlechat", id_number);
        for(DatabaseUtils utils:records)
        {
            if(utils.piece_kind.equals("send"))
                msgList.add(new Msg(utils.piece_record, Msg.SENT));
            else
                msgList.add(new Msg(utils.piece_record, Msg.RECEIVED));
        }
        //
        adapter = new MsgAdapter(MainActivity.this, R.layout.chat_alone, msgList);
        inputText = (EditText)findViewById(R.id.editText);
        msglistview = findViewById(R.id.list_view);
        sendbtn = findViewById(R.id.button);
        msglistview.setAdapter(adapter);
        // upload file settings
        send_file = findViewById(R.id.sendfile);
        send_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tem_kind = send_file.getText().toString();
                if(tem_kind.equals("select")){
                    Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                    intent1.setType("*/*");
                    intent1.addCategory(Intent.CATEGORY_OPENABLE);
                    startActivityForResult(intent1, 0);
                    send_file.setText("upload");
                }
                else if(tem_kind.equals("upload"))
                {
                    send_file.setText("select");
                    TcpClientThread tcpClientThread = new TcpClientThread(mhandler, address, friend_port+1, 0);
                    tcpClientThread.set_send_mode(1);
                    tcpClientThread.set_file_path(result_string);
                    tcpClientThread.start();
                }
                else
                {
                    Toast.makeText(MainActivity.this, "名称有问题", Toast.LENGTH_LONG).show();
                }
            }
        });

        //server settings
        TcpServer tcpServer = new TcpServer(20101);
        tcpServer.start();
        //file server settings
        TcpServer tcpServer1 = new TcpServer(20102);
        tcpServer1.set_mode(1);
        tcpServer1.start();
    }
    // result for the startactivityforresult
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode!= Activity.RESULT_OK)
        {
            Log.e("VideoActivity", "onActivityResult() error, resultCode: "+resultCode);
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        if(resultCode == Activity.RESULT_OK)
        {
            Uri uri = data.getData();
            Log.i("VideoActivity", "----->"+uri.getPath());
            Log.i("VideoActivity", "----->"+uri.toString());
            String real_path = getPath(MainActivity.this, uri);
            Log.i("VideoActivity", "----->"+real_path);
            result_string = real_path;
            result_uri = uri;
        }
        super.onActivityResult(requestCode, resultCode, data);
        Toast.makeText(MainActivity.this, "结果为"+result_string, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context
     *            The context.
     * @param uri
     *            The Uri to query.
     * @param selection
     *            (Optional) Filter used in the query.
     * @param selectionArgs
     *            (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri
     *            The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
//-----------------------------------------------------------------

    // the server part
    public class TcpServer extends Thread{
        private int port;
        private int mode = 0;
        public TcpServer(int port)
        {
            this.port = port;
        }
        @Override
        public void run()
        {
            try{
                ServerSocket serverSocket = new ServerSocket(this.port);
                while(true){
                    Socket socket = serverSocket.accept();
                    TcpServerThread serverThread = new TcpServerThread(socket, mhandler);
                    serverThread.set_mode(mode);
//                    String tem_path = getApplicationContext().getFilesDir().getAbsolutePath();
//                    String tem_path = "/storage/emulated/0/Pictures/知乎";
//                    serverThread.set_path("/storage/emulated/0/letuschat")
                    String tem_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    serverThread.set_path(tem_path);
                    serverThread.start();
                }
            }catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        public void set_mode(int mode)
        {
            this.mode = mode;
        }
    }
    // get the present time
    public static String getDateToString()
    {
        long time = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        return simpleDateFormat.format(date);
    }

    // the client part
    public void send_chat_msg(View v){
        String content = inputText.getText().toString();
        if(content.equals(""))
            return;
        Msg msg = new Msg(content, Msg.SENT);
        msgList.add(msg);
        adapter.notifyDataSetChanged();
        msglistview.setSelection(msgList.size());
        inputText.setText("");
        TcpClientThread tcpClientThread = new TcpClientThread(mhandler, address, friend_port, 0);
        tcpClientThread.setmsg(content);
        tcpClientThread.start();
        ContentValues values = new ContentValues();
        values.put("name", id_number);
        values.put("record", content);
        values.put("kind", "send");
        values.put("date", getDateToString());
        dbadapter.db_add("singlechat", values);
//        ArrayList<DatabaseUtils> records = dbadapter.db_name_record("singlechat", id_number);
    }


    Handler mhandler = new Handler(){
        @Override
        public  void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case(0):{
                    Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                }
                case(1):{
                    String tem = msg.obj.toString();
                    msgList.add(new Msg(tem, Msg.RECEIVED));
                    adapter.notifyDataSetChanged();
                    msglistview.setSelection(msgList.size());
                    ContentValues values = new ContentValues();
                    values.put("name", id_number);
                    values.put("record", tem);
                    values.put("kind", "receive");
                    values.put("data", getDateToString());
                    dbadapter.db_add("singlechat", values);
                    break;
                }
                default:
                    break;
            }
        }
    };


    public class Msg{
        public static  final int RECEIVED=0;
        public static final int SENT=1;
        public String content;
        public int type;
        public Msg(String content, int type){
            this.content = content;
            this.type = type;
        }
    }

    public class MsgAdapter extends ArrayAdapter<Msg>{
        public int resourceId;
        public MsgAdapter(Context context, int textViewId, List<Msg> objects){
            super(context, textViewId, objects);
            resourceId = textViewId;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            Msg msg = getItem(position);
            View view;
            ViewHolder viewHolder;
            if(convertView == null){
                view = LayoutInflater.from(getContext()).inflate(resourceId, null);
                viewHolder = new ViewHolder();
                viewHolder.leftLayout = (LinearLayout)view.findViewById(R.id.left_layout);
                viewHolder.rightLayout = (LinearLayout)view.findViewById(R.id.right_layout);
                viewHolder.blankLayout = (LinearLayout)view.findViewById(R.id.blank_layout);
                viewHolder.blankMsg = (TextView)view.findViewById(R.id.blank_msg);
                viewHolder.leftMsg = (TextView)view.findViewById(R.id.left_msg);
                viewHolder.rightMsg = (TextView)view.findViewById(R.id.right_msg);
                view.setTag(viewHolder);
            }else{
                view = convertView;
                viewHolder = (ViewHolder) view.getTag();
            }

            if(msg.type==Msg.RECEIVED){
                //如果是收到的消息，则显示左边消息布局，将右边消息布局隐藏
                viewHolder.leftLayout.setVisibility(View.VISIBLE);
                viewHolder.rightLayout.setVisibility(View.GONE);
                viewHolder.leftMsg.setText(msg.content);
            }else if(msg.type==Msg.SENT){
                //如果是发出去的消息，显示右边布局的消息布局，将左边的消息布局隐藏
                viewHolder.rightLayout.setVisibility(View.VISIBLE);
                viewHolder.leftLayout.setVisibility(View.GONE);
                viewHolder.rightMsg.setText(msg.content);
            }
            viewHolder.blankLayout.setVisibility(View.GONE);
            return view;
        }
        public class ViewHolder{
            LinearLayout leftLayout;
            LinearLayout rightLayout;
            LinearLayout blankLayout;
            TextView blankMsg;
            TextView leftMsg;
            TextView rightMsg;
        }
    }
}
