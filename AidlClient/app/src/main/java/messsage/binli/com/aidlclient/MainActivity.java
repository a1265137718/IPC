package messsage.binli.com.aidlclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import messsage.binli.com.aidlserver.IGuideAidlInterface;
import messsage.binli.com.aidlserver.IGuideListener;

public class MainActivity extends AppCompatActivity {
    private TextView tv_style;
    private EditText et_accotun;
    private EditText et_password;
    private Button btn_login;
    private Button btn_logout;

    private IGuideAidlInterface iGuideAidlInterface;
    boolean isConnect;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            iGuideAidlInterface = IGuideAidlInterface.Stub.asInterface(iBinder);
            tv_style.setText("当前状态：\n连接成功");
            isConnect = true;
            try {
                iGuideAidlInterface.registerListener(iGuideListener);
            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            tv_style.setText("当前状态：\n连接断开");

            isConnect = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_style = findViewById(R.id.tv_style);
        et_accotun = findViewById(R.id.et_accotun);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_logout = findViewById(R.id.btn_logout);

        Intent intent = new Intent();
        intent.setAction("com.ecarx.membercenter.action.GUIDE");
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnect){
                    try {
                        Log.e("TAG","btn_login");
                        iGuideAidlInterface.login(String.valueOf(et_accotun.getText()),String.valueOf(et_password.getText()) , getPackageName());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isConnect){
                    try {
                        iGuideAidlInterface.logout(getPackageName());
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

        try {
            iGuideAidlInterface.unregisterListener(iGuideListener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        unbindService(serviceConnection);
    }

    private IGuideListener iGuideListener = new IGuideListener.Stub() {
        @Override
        public void onLoginSuccess(final String msg) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_style.setText("当前状态：\n连接成功\n"+msg);

                }
            });
            Toast.makeText(MainActivity.this,"客户端登录成功回调====" + msg,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLoginFail(final String msg) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_style.setText("当前状态：\n连接成功\n"+msg);
                    Toast.makeText(MainActivity.this,"客户端登录失败回调====" + msg,Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onLogoutSuccess(final String msg) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_style.setText("当前状态：\n连接成功\n"+msg);
                    Toast.makeText(MainActivity.this,"客户端登出成功回调====" + msg,Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onLogoutFail(final String msg) throws RemoteException {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_style.setText("当前状态：\n连接成功\n"+msg);
                    Toast.makeText(MainActivity.this,"客户端登出失败回调====" + msg,Toast.LENGTH_LONG).show();
                }
            });
        }
    };
}
