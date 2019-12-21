package com.example.letuschat;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpServerStarter extends Thread {
    private Context m_context=null;
    private int mode = 0;
    private int port=20101;
    private String original_id;
    public TcpServerStarter(Context context, int mode, int port, String original_id)
    {
        this.m_context = context;
        this.mode = mode;
        this.port = port;
        this.original_id = original_id;
    }
    @Override
    public void run()
    {
        try{
            ServerSocket serverSocket = new ServerSocket(this.port);
            while(true){
                Log.i("TcpServerStarter", "began to run");
                Socket socket = serverSocket.accept();
                TcpServerThread serverThread = new TcpServerThread(socket, this.m_context, original_id);
                serverThread.set_mode(mode);
                String tem_path = Environment.getExternalStorageDirectory().getAbsolutePath();
                serverThread.set_path(tem_path);
                serverThread.start();
                Log.i("TcpServerStarter", "successfully caught one"+(this.mode));
            }
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
