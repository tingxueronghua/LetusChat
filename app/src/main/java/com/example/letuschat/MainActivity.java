package com.example.letuschat;

import androidx.appcompat.app.AppCompatActivity;

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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public ListView msglistview;
    public EditText inputText;
    public Button sendbtn;
    public MsgAdapter adapter;
    public List<Msg> msgList = new ArrayList<Msg>();
    public String id_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        id_number = intent.getStringExtra("id");
        setContentView(R.layout.activity_main);
        msgList.add(new Msg("I miss you!", Msg.RECEIVED));
        msgList.add(new Msg("I miss you, too!", Msg.SENT));
        adapter = new MsgAdapter(MainActivity.this, R.layout.chat_alone, msgList);
        inputText = (EditText)findViewById(R.id.editText);
        msglistview = findViewById(R.id.list_view);
        sendbtn = findViewById(R.id.button);
        msglistview.setAdapter(adapter);

    }


    public void send_chat_msg(View v){
        String content = inputText.getText().toString();
        if(content.equals(""))
            return;
        Msg msg = new Msg(content, Msg.SENT);
        msgList.add(msg);
        adapter.notifyDataSetChanged();;
        msglistview.setSelection(msgList.size());
        inputText.setText("");
    }


    // 0 for login, 1 for logout, 2 for check, 3 for chatting
    Handler mhandler = new Handler(){
        @Override
        public  void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case(0):{
//                    Button btn = findViewById(R.id.button);
//                    btn.setText(msg.obj.toString());
                    break;
                }
                case(1):{
//                    Button btn=findViewById(R.id.button2);
//                    btn.setText(msg.obj.toString());
                    break;
                }
                case(2):{
//                    Button btn = findViewById(R.id.button3);
//                    btn.setText(msg.obj.toString());
                    break;
                }
                default:
                    break;
            }
        }
    };


    public class TCPThread extends Thread{
        public void run(int threadmode, String student_number){
            Socket socket;
            BufferedReader in;
            PrintWriter out;
            String str;
            try{
                Message msg = new Message();
                msg.what = threadmode;
                msg.obj = "connecting";
                mhandler.sendMessage(msg);
                socket = new Socket("183.173.99.71", 8000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
                if(threadmode == 0)
                    out.write("2017011505_net2019");
                else if(threadmode == 1)
                    out.write("logout2017011505");
                else if(threadmode == 2)
                    out.write("q"+student_number);
                else
                    out.write("2017011505_net2019");
                out.flush();
                socket.shutdownOutput();

                while(!((str = in.readLine()) == null)){
                    Message msg2 = new Message();
                    msg2.what = threadmode;
                    msg2.obj = str;
                    mhandler.sendMessage(msg2);
                    break;
                }
                in.close();
                out.close();
                socket.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

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
