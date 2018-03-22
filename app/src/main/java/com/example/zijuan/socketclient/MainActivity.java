package com.example.zijuan.socketclient;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.zijuan.socketclient.service.TCPServerService;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MESSAGE_RECEIVE_NEW_MSG = 1;
    private static final int MESSAGE_SOCKET_CONNECTED = 2;

    private static final String HOST = "127.0.0.1";
    private static final int PORT = 23333;

    private Socket mClientSocket;
    private PrintWriter mPrintWriter;

    private TextView mMessageTextView;
    private Button mSendButton;
    private EditText mMessageEditText;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_RECEIVE_NEW_MSG:
                    mMessageTextView.setText(mMessageTextView.getText() + "\n" + msg.obj);
                    break;
                case MESSAGE_SOCKET_CONNECTED:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMessageTextView = (TextView) findViewById(R.id.msg_container);
        mSendButton = (Button) findViewById(R.id.send);
        mMessageEditText = (EditText) findViewById(R.id.msg);
        mSendButton.setOnClickListener(this);

        Intent service = new Intent(this, TCPServerService.class);
        startService(service);

        new Thread(new Runnable() {
            @Override
            public void run() {
                connectTCPServer();
            }
        }).start();
    }

    private void connectTCPServer() {
        Socket socket = null;
        while (socket == null) {
            try {
                Log.e("hzjhzj ", "start connect");
                socket = new Socket(HOST, PORT);
                mClientSocket = socket;

                mPrintWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mClientSocket.getOutputStream())), true);

                mHandler.sendEmptyMessage(MESSAGE_SOCKET_CONNECTED);
                System.out.println("connect server success");


            } catch (IOException e) {
                e.printStackTrace();
                SystemClock.sleep(1000);
                System.out.println("connect server failed, retry...");
            }
        }

        // 接收服务端消息
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (!this.isFinishing()) {
                if (!mClientSocket.isClosed() && mClientSocket.isConnected() && !mClientSocket.isInputShutdown()) {;
                    String msg = br.readLine();
                    System.out.println("receive：" + msg);
                    if (msg != null) {
                        mHandler.obtainMessage(MESSAGE_RECEIVE_NEW_MSG, msg).sendToTarget();
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (mPrintWriter != null) {
                mPrintWriter.flush();
                mPrintWriter.close();
            }

            if (mClientSocket != null) {
                mClientSocket.shutdownInput();
                mClientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send:
                String sendMsg = mMessageEditText.getText().toString();
                if (!TextUtils.isEmpty(sendMsg) && mPrintWriter != null) {
                    mPrintWriter.println(sendMsg);

                    mMessageEditText.setText("");

                    mMessageTextView.setText(mMessageTextView.getText() + "\n" + sendMsg);

                }
                break;
        }
    }
}
