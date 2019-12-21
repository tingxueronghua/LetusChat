package com.example.letuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendList extends AppCompatActivity {
    public ListView friend_listview;
    private String id_number;
    public FriendMsgAdapter adapter;
    public List<Friend_Msg> friend_msgList = new ArrayList<Friend_Msg>();
    public DatabaseAdapter dbadapter;
    public String address="166.111.140.57";
    public int port = 8000;
    public String friend_address = "";
    public String query;
    public String tem_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);
        Intent intent = getIntent();
        id_number = intent.getStringExtra("id");
        dbadapter = new DatabaseAdapter(FriendList.this, id_number);
        ArrayList<String> friends = dbadapter.db_all_friend("names");
        for(String str:friends){
            friend_msgList.add(new Friend_Msg(str, 1));
        }
        adapter = new FriendMsgAdapter(FriendList.this, R.layout.chat_alone, friend_msgList);
        friend_listview = findViewById(R.id.list_view2);
        friend_listview.setAdapter(adapter);
        friend_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(id == -1) {
                    return;
                }
                int realPosition=(int)id+1;
                tem_id = dbadapter.db_find_id("names", String.valueOf(realPosition));
                Toast.makeText(FriendList.this, tem_id, Toast.LENGTH_LONG).show();

                query = "q"+tem_id;
                TcpClientThread client_thread = new TcpClientThread(mhandler, address, port, 1, id_number);
                client_thread.set_send_mode(2);
                client_thread.setmsg(query);
                client_thread.start();
            }
        });
        // start the service
        MyIntentService.startActionSTART(FriendList.this, id_number);
//        Intent myintent = new Intent(this, MyIntentService.class);
//        startService(myintent);
    }

    Handler mhandler = new Handler(){
        @Override
        public  void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case(0):{
                    if(!msg.obj.toString().equals("n"))
                    {
                        EditText edittext = findViewById(R.id.editText2);
                        String content = edittext.getText().toString();
                        ArrayList<String> arr = dbadapter.db_find_name("names", content);
                        if(!(arr.size()==0))
                        {
                            Toast.makeText(FriendList.this, "请勿戏耍本软件", Toast.LENGTH_LONG).show();
                            return;
                        }
                        // set the name on the screen
                        FriendList.Friend_Msg frimsg=new FriendList.Friend_Msg(content, 1);
                        friend_msgList.add(frimsg);
                        adapter.notifyDataSetChanged();
                        friend_listview.setSelection(friend_msgList.size());
                        edittext.setText("");
                        // set the friend into the database
                        ContentValues values = new ContentValues();
                        values.put("kind", "friend");
                        values.put("name", content);
                        dbadapter.db_add("names", values);
                    }
                    else
                    {
                        Toast.makeText(FriendList.this, "您输入的学号可能有误，也可能是网络未正确连接！", Toast.LENGTH_LONG).show();
                    }
                    break;
                }
                case (1):{
                    if(msg.obj.toString().equals("n"))
                    {
                        Toast.makeText(FriendList.this, "不在线", Toast.LENGTH_LONG).show();
                        return;
                    }
                    else
                    {
                        friend_address = msg.obj.toString();
                        Intent intent = new Intent(FriendList.this, MainActivity.class);
                        intent.putExtra("id", tem_id);
                        intent.putExtra("address", friend_address);
                        intent.putExtra("original_name", id_number);
                        startActivity(intent);
                    }
                    break;
                }
                default:{
                    break;
                }
            }
        }
    };

    public void add_friend_msg(View v)
    {
        EditText edittext = findViewById(R.id.editText2);
        String content = edittext.getText().toString();
        if(content.equals(""))
            return;
        // check whether the friend is online
        String query = "q"+content;
        TcpClientThread client_thread = new TcpClientThread(mhandler, address, port, 0, id_number);
        client_thread.set_send_mode(2);
        client_thread.setmsg(query);
        client_thread.start();
    }
    public class Friend_Msg
    {
        public String content;
        public int type;
        public Friend_Msg(String content, int type){
            this.content = content;
            this.type = type;
        }
    }

    public class FriendMsgAdapter extends ArrayAdapter<FriendList.Friend_Msg> {
        public int resourceId;
        public FriendMsgAdapter(Context context, int textViewId, List<FriendList.Friend_Msg> objects){
            super(context, textViewId, objects);
            resourceId = textViewId;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            FriendList.Friend_Msg msg = getItem(position);
            View view;
            FriendList.FriendMsgAdapter.ViewHolder viewHolder;
            if(convertView == null){
                view = LayoutInflater.from(getContext()).inflate(resourceId, null);
                viewHolder = new ViewHolder();
                viewHolder.leftLayout = (LinearLayout)view.findViewById(R.id.left_layout);
                viewHolder.rightLayout = (LinearLayout)view.findViewById(R.id.right_layout);
                viewHolder.blankLayout = (LinearLayout)view.findViewById(R.id.blank_layout);
                viewHolder.blankMsg = (TextView)view.findViewById(R.id.blank_msg);
                view.setTag(viewHolder);
            }else{
                view = convertView;
                viewHolder = (FriendList.FriendMsgAdapter.ViewHolder) view.getTag();
            }
            viewHolder.blankLayout.setVisibility(View.VISIBLE);
            viewHolder.blankMsg.setText(msg.content);
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.rightLayout.setVisibility(View.GONE);

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
