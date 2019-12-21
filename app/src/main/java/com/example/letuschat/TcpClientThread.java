package com.example.letuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Context;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
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
import java.io.BufferedWriter;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
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
    private int msg_what;
    private int SEND_FILE=0;
    private String file_path;
    private Uri file_uri;
    private String original_name;

    public TcpClientThread(Handler handler, String address, int port, int msg_what, String original_name) {
        this.mHandler = handler;
        this.address = address;
        this.port = port;
        this.msg_what = msg_what;
        this.original_name = original_name;
    }
    public void set_send_mode(int mode)
    {
        this.SEND_FILE = mode;
    }
    public void set_file_path(String file_name)
    {
        this.file_path = file_name;
    }
    public void set_file_uri(Uri uri)
    {
        this.file_uri = uri;
    }

    @Override
    public void run() {
        super.run();
        if(this.SEND_FILE==1)
        {
            uploadFile();
        }
        else
        {
            sendSocket();
        }
    }
    public void setmsg(String input_msg)
    {
        msg = input_msg;
    }

    //send file
    private void uploadFile()
    {
        String path = file_path;
        InputStream reader = null;
        BufferedReader bufferedReader = null;
        Socket socket = null;
        try{
            socket = new Socket(address, port);
            int bufferSize=8192;
            byte[] buf = new byte[bufferSize];
            File file  = new File(path);
//            File file = new File(this.file_uri);
            Log.i("tcpclientfile", "文件长度"+(int)file.length());
            DataInputStream file_input = new DataInputStream(new FileInputStream(path));
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            // 传输id
            dataOutputStream.writeUTF(original_name);
            dataOutputStream.flush();
            //传输文件名
            dataOutputStream.writeUTF(file.getName());
            dataOutputStream.flush();
            //将文件长度传输过去
            dataOutputStream.writeLong((long)file.length());
            dataOutputStream.flush();
            //传输文件
            int readSize=0;
            while(true)
            {
                if (file_input!=null)
                {
                    readSize = file_input.read(buf);
                }
                if(readSize==-1)
                {
                    break;
                }
                dataOutputStream.write(buf, 0, readSize);
                if(!dataInputStream.readUTF().equals("OK"))
                {
                    Log.i("tcpclientfile", "服务器"+address+"失去连接！");
                    break;
                }
            }
            dataOutputStream.flush();
            file_input.close();
            dataOutputStream.close();
            socket.close();
            dataInputStream.close();
            Log.i("tcpclientfile", "文件传输完成");
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            try{
                if(bufferedReader!=null)
                    bufferedReader.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
    }

    // send a text message
    private void sendSocket()
    {
        InputStreamReader reader = null;
        BufferedReader bufferreader = null;
        Socket socket  = null;
        try{
            socket = new Socket(address, port);
            OutputStream outputstream = socket.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputstream);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(dataOutputStream);
            String tem_msg = "";
            if(SEND_FILE==0)
            {
                tem_msg = original_name;
            }
            tem_msg = tem_msg+msg;
//            outputstream.write(msg.getBytes());
//            outputstream.flush();
            //dataOutputStream.writeUTF(msg);
            //dataOutputStream.flush();
            outputStreamWriter.write(tem_msg);
            outputStreamWriter.flush();
            //DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            //InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
            //dataOutputStream.close();
            socket.shutdownOutput();

            InputStream inputstream = socket.getInputStream();
            reader = new InputStreamReader(inputstream);
            bufferreader = new BufferedReader(reader);
            String tem = null;
            final StringBuffer stringb = new StringBuffer();
            while((tem=bufferreader.readLine())!=null){
                stringb.append(tem);
            }
            sendMsg(msg_what, stringb.toString());
            dataOutputStream.flush();
            dataOutputStream.close();
            socket.close();
            inputstream.close();
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