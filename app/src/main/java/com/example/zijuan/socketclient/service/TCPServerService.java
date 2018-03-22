package com.example.zijuan.socketclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * Created by zijuan on 2018/3/20.
 */

public class TCPServerService extends Service {
    private boolean mIsServiceDestroyed = false;
    private String[] mDefinedMsgs = new String[] {
            "你好呀，哈哈哈",
            "今天真是个好天气",
            "听说，下雨天和巧克力更配哦",
            "给你讲个笑话吧",
            "从前有座山，山里有个庙"
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        new Thread(new TcpServer()).start();
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed = true;
        super.onDestroy();
    }

    private class TcpServer implements Runnable {

        @Override
        public void run() {
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(23333);
            } catch (IOException e) {
                System.out.println("establish tcp server failed, port:23333");
                e.printStackTrace();
                return;
            }

            // 接收客户端的请求
            while (!mIsServiceDestroyed) {
                try {
                    Socket client = serverSocket.accept();
                    System.out.println("accepts");

                    responseClient(client);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        private void responseClient(Socket client) throws IOException {
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
            out.println("欢迎来到聊天室");

            while (!mIsServiceDestroyed) {
                String str = in.readLine();
                System.out.println("msg from client：" + str);

                int i = new Random().nextInt(mDefinedMsgs.length);
                out.println(mDefinedMsgs[i]);

            }
        }
    }
}
