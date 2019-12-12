package com.example.letuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TcpClientThread extends Thread {

    //IP地址
    private String address;
    //端口
    private int port;
    //发送内容
    private String msg;
    private Handler mHandler;

    public TcpClientThread(Handler handler, String address, int port) {
        this.mHandler = handler;
        this.address = address;
        this.port = port;
    }

    @Override
    public void run() {
        super.run();
        sendSocket();
    }
    public void setmsg(String input_msg)
    {
        msg = input_msg;
    }

    /**
     * 设置
     */
    private void sendSocket()
    {
        InputStreamReader reader = null;
        BufferedReader bufferreader = null;
        Socket socket  = null;
        try{
            socket = new Socket(address, port);
            OutputStream outputstream = socket.getOutputStream();
            outputstream.write(msg.getBytes());
            outputstream.flush();
            socket.shutdownOutput();
            InputStream inputstream = socket.getInputStream();
            reader = new InputStreamReader(inputstream);
            bufferreader = new BufferedReader(reader);
            String tem = null;
            final StringBuffer stringb = new StringBuffer();
            while((tem=bufferreader.readLine())!=null){
                stringb.append(tem);
            }
            sendMsg(0, stringb.toString());
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(bufferreader!=null)
                    bufferreader.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

    /**
     * 发送消息
     */
    private void sendMsg(int what, Object object) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = object;
        mHandler.sendMessage(msg);
    }
}