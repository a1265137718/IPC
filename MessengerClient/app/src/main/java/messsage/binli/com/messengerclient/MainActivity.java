package messsage.binli.com.messengerclient;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final int MSG_FROM_CLIENT = 0x10001;
    private static final int MSG_TO_CLIENT = 0x10002;

    private static final String IS_LOGIN = "isLogin";
    private static final String NICK_NAME = "nickName";
    private static final String USER_ID = "userId";

    private boolean isConn;
    private Messenger mService;

    private TextView tv_state;
    private TextView tv_message;
    private Button btn_send;

    @SuppressLint("HandlerLeak")
    private Messenger mMessenger = new Messenger(new Handler()
    {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msgFromServer)
        {
            switch (msgFromServer.what)
            {
                case MSG_TO_CLIENT:
                    Bundle data = msgFromServer.getData();
                    tv_message.setText("服务器返回内容\n"+
                            data.get(NICK_NAME)+"\n"+
                            data.get(USER_ID)+"\n"+
                            data.get(IS_LOGIN));
                    break;
            }
            super.handleMessage(msgFromServer);
        }
    });


    private ServiceConnection mConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mService = new Messenger(service);
            isConn = true;
            tv_state.setText("连接状态：connected!");
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
            isConn = false;
            tv_state.setText("连接状态：disconnected!");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_state = findViewById(R.id.tv_state);
        tv_message = findViewById(R.id.tv_message);
        btn_send = findViewById(R.id.btn_send);

        //开始绑定服务
        bindServiceInvoked();

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message msgFromClient = new Message();
                msgFromClient.what = MSG_FROM_CLIENT;
                msgFromClient.replyTo = mMessenger;
                if (isConn)
                {
                    //往服务端发送消息
                    try {
                        mService.send(msgFromClient);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConn);
    }

    private void bindServiceInvoked()
    {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MESSENGER");
        bindService(intent, mConn, Context.BIND_AUTO_CREATE);
    }
}
