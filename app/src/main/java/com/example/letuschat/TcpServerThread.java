package com.example.letuschat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class TcpServerThread extends Thread {
    private Socket socket;
    //private Handler mhandler;
    Context m_context;
    private int mode=0;
    private String path;
    private String original_id;
    public TcpServerThread(Socket socket, Context context, String original_id)
    {
        this.socket = socket;
        //this.mhandler = handler;
        this.m_context = context;
        this.original_id = original_id;
    }
    public void set_mode(int mode)
    {
        this.mode = mode;
    }
    public void set_path(String tem)
    {
        this.path = tem;
    }
    private void sendMsg(int what, String object, String id_number) {
        Intent intent = new Intent();
        intent.setAction("com.example.letuschat.action.msg");
        intent.putExtra("msg", object);
        intent.putExtra("id_number", id_number);
        m_context.sendBroadcast(intent);
        MyIntentService.startActionMSG(this.m_context, original_id, object, id_number);
    }
    public void receive_file()
    {
        try
        {
            Log.i("TcpServerThread", "客户端已连接");
            int buffersize = 8192;
            byte[] buf = new byte[buffersize];
            long donelen=0;//完成的文件的长度
            long filelen=0;//文件的长度
            //获得各个流
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
//            path = getApplicationContext().getFilesDir().getAbsolutePath();
            String fileDir = path+"/"+socket.getInetAddress().toString().substring(1, socket.getInetAddress().toString().length());
            File file = new File(fileDir);
            if (!file.exists())
            {
                file.mkdirs();
            }
            // read the friend's id_number
            String id_number = dataInputStream.readUTF();
            String filename = dataInputStream.readUTF();
            String filepath = fileDir+"/"+filename;
            //file = new File(filepath);
            file = new File(path+"/"+filename);
            if(!file.exists())
            {
                file.createNewFile();
            }
            DataOutputStream dataOutputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
            filelen = dataInputStream.readLong();
            Log.i("TcpServerThread", "文件长度为："+filelen+"\n");
            Log.i("TcpServerThread", "开始接受文件\n");
            DataOutputStream ack = new DataOutputStream(socket.getOutputStream());
            while(true)
            {
                int read=0;
                if(dataInputStream!=null)
                {
                    read = dataInputStream.read(buf);
                    ack.writeUTF("OK");
                }
                if(read == -1)
                {
                    break;
                }
                donelen+=read;
                dataOutputStream.write(buf, 0, read);
            }
            if(donelen==filelen)
                Log.i("TcpServerThread", "接收完成，文件存为"+file+"\n");
            else
            {
                Log.i("TcpServerThread", "失去连接");
                file.delete();
            }
            ack.close();
            dataInputStream.close();
            dataOutputStream.close();
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void plain_msg()
    {
        InputStreamReader reader = null;
        BufferedReader bufReader = null;
        OutputStream os = null;
        try {
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
//            String id_number = dataInputStream.readUTF();
//            String text_record = dataInputStream.readUTF();

            InputStream inputstream = socket.getInputStream();
            reader = new InputStreamReader(inputstream);
            BufferedReader bufferreader = new BufferedReader(reader);
            String tem = null;
            final StringBuffer text_record = new StringBuffer();
            while((tem=bufferreader.readLine())!=null){
                text_record.append(tem);
            }
            String tem_record = text_record.toString();
            String id_name = tem_record.substring(0, 10);
            String record = text_record.substring(10);



            DataOutputStream ack = new DataOutputStream(socket.getOutputStream());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(ack);
            outputStreamWriter.write("OK，发送给我的数据是"+record);
            outputStreamWriter.flush();
            outputStreamWriter.close();
            dataInputStream.close();
            sendMsg(1, record, id_name);

//            reader = new InputStreamReader(socket.getInputStream());
//            bufReader = new BufferedReader(reader);
//            String s = null;
//            StringBuffer sb = new StringBuffer();
//            while ((s = bufReader.readLine()) != null) {
//                sb.append(s);
//            }
//            System.out.println("服务器：" + sb.toString());
//            // 关闭输入流
//            socket.shutdownInput();
//
//            // 返回给客户端数据
//            os = socket.getOutputStream();
//            sendMsg(1, sb.toString());
//            os.write(("我是服务端,客户端发给我的数据就是：" + sb.toString()).getBytes());
//            os.flush();
//            socket.shutdownOutput();
        } catch (IOException e2) {
            e2.printStackTrace();
        } finally {// 关闭IO资源
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    @Override
    public void run() {
        super.run();
        if (mode!=1){
            plain_msg();
        }
        else
        {
            receive_file();
        }
    }
}
