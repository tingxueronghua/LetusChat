package com.example.letuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
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

        //server settings
        TcpServer tcpServer = new TcpServer(20101);
        tcpServer.start();
    }

    // the server part
    public class TcpServer extends Thread{
        private int port;
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
                    serverThread.start();
                }
            }catch(IOException e)
            {
                e.printStackTrace();
            }
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
