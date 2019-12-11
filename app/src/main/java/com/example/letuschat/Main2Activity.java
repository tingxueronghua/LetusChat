package com.example.letuschat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.AlteredCharSequence;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity {
    private DatabaseAdapter databaseadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Button button = (Button) findViewById(R.id.button3);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TcpClientThread clientthread = new TcpClientThread(mhandler, address, port);
//                clientthread.setmsg("2017011505_net2019");
//                clientthread.run();
//                Intent intent = new Intent(Main2Activity.this, MainActivity.class);
//                startActivity(intent);
//            }
//        });
    }
    Handler mhandler = new Handler(){
        @Override
        public  void handleMessage(Message msg){
            super.handleMessage(msg);
            switch (msg.what){
                case(0):{
//                    Button btn = findViewById(R.id.button3);
//                    btn.setText(msg.obj.toString());
                    if(msg.obj.toString().equals("lol"))
                    {
                        EditText text3 = findViewById(R.id.editText3);
                        String query = text3.getText().toString();
                        databaseadapter = new DatabaseAdapter(Main2Activity.this, query);
                        Intent intent = new Intent(Main2Activity.this, FriendList.class);
                        intent.putExtra("id", query);
                        startActivity(intent);
                    }
                    else
                    {
                        Toast.makeText(Main2Activity.this, "您输入的学号可能有误，也可能是网络未正确连接！", Toast.LENGTH_LONG).show();
                    }
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
    public String address="166.111.140.57";
    public int port = 8000;
    public void login(View v){
        EditText text3 = findViewById(R.id.editText3);
        String query = text3.getText()+"_net2019";
        TcpClientThread client_thread = new TcpClientThread(mhandler, address, port);
        client_thread.setmsg(query);
        client_thread.start();
    }


}
