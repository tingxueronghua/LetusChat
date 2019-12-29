package com.example.letuschat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
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
        verifyStoragePermissions(Main2Activity.this);
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
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.CAMERA",
            "android.permission.RECORD_AUDIO"};


    //ask for storage permissions
    public static void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                    Toast.makeText(Main2Activity.this, "successfully", Toast.LENGTH_LONG).show();
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
        TcpClientThread client_thread = new TcpClientThread(mhandler, address, port, 0, address);
        client_thread.set_send_mode(2);
        client_thread.setmsg(query);
        client_thread.start();
    }

    public void logout(View v){
        EditText text3 = findViewById(R.id.editText3);
        String query = "logout"+text3.getText();
        TcpClientThread client_thread = new TcpClientThread(mhandler, address, port, 2, address);
        client_thread.set_send_mode(2);
        client_thread.setmsg(query);
        client_thread.start();
    }
}
