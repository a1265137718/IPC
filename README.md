# IPC
今天这篇文章主要讲一下Messenger与AIDL的区别、优缺点以及各自的使用方法。

项目地址：https://github.com/libin7278/IPC

对binder和IPC还不熟悉的同学可以看一下之前的文章：
[IPC进程间通信/跨进程通信](http://blog.csdn.net/github_33304260/article/details/52895331)
[Android 中的Binder跨进程通信机制与AIDL](http://blog.csdn.net/github_33304260/article/details/53172208)

# Messenger与AIDL的异同

### 一、Messenger与AIDL相同点

>1.都与IPC的调用有关；
2.Messenger 是一种轻量级的 IPC方案，底层实现了AIDL，只是进行了封装，开发的时候不用再写.aidl文件。
3.都支持实时通信；
### 二、Messenger与AIDL不同点

>1.Messenger一次只能处理一个请求（串行）/AIDL一次可以处理多个请求（并行）；
2.Messenger不支持RPC，只能通过message传递消息/AIDL支持RPC；
3.Messenger使用简单，轻量级，不需要创建AIDL文件/AIDL使用复杂，需要创建AIDL文件；
### 三、Messenger与AIDL的优缺点及适用场景

|名称|优点|缺点|适用场景|
|--|--|--|--|
|Messenger|1.功能强大；2.支持实时通信；3.支持一对多并发通信；4.支持RPC（远程过程调用）|1.使用复杂，需创建AIDL文件；2.需处理好线程同步问题|低并发的一对多即时通信，无RPC要求，不需要处理多线程）|
|AIDL|1.使用简单，轻量级；2.支持实时通信；3.支持一对多串行通信|1.功能简单；2.不支持RPC；3.数据通过message传输；4.不支持高并发场景；5.服务端想要回应客户端，必须通过Message的replyTo把服务端的Messenger传递过去|一对多且有RPC需求，想在服务里处理多线程的业务）|


##Messenger与AIDL的用法
###一、Messenger用法
####1、概述
Messenger进程间通信方式（如图）：

![这里写图片描述](http://img.blog.csdn.net/20171123161923369?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ2l0aHViXzMzMzA0MjYw/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

我们可以在客户端发送一个Message给服务端，在服务端的handler中会接收到客户端的消息，然后进行对应的处理，处理完成后，再将结果等数据封装成Message，发送给客户端，客户端的handler中会接收到处理的结果。

>**server端：**
收到的请求是放在Handler的MessageQueue里面，Handler大家都用过，它需要绑定一个Thread，然后不断poll message执行相关操作，这个过程是同步执行的。

>**client端：**
>client端要拿到返回值，需要把client的Messenger作为msg.replyTo参数传递过去，service端处理完之后，在调用客户端的Messenger的send(Message msg)方法把返回值传递回client

#### 2、实例
接下来我们看一下实例代码，一个服务端apk（MessengerServer），一个客户端apk（MessengerClient）。

![这里写图片描述](http://img.blog.csdn.net/20171124105635948?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ2l0aHViXzMzMzA0MjYw/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

=========================**服务端**=========================：

```
public class MessengerServer extends Service {
    private static final int MSG_FROM_CLIENT = 0x10001;
    private static final int MSG_TO_CLIENT = 0x10002;
    private static final String IS_LOGIN = "isLogin";
    private static final String NICK_NAME = "nickName";
    private static final String USER_ID = "userId";

    @SuppressLint("HandlerLeak")
    private Messenger mMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msgfromClient) {
            Message msgToClient = Message.obtain(msgfromClient);//返回给客户端的消息
            switch (msgfromClient.what) {
                //msg 客户端传来的消息
                case MSG_FROM_CLIENT:
                    try {
                        //模拟耗时
                        Thread.sleep(2000);

                        //传递数据
                        Bundle toClicentDate = new Bundle();
                        toClicentDate.putString(NICK_NAME,"张小可");
                        toClicentDate.putBoolean(IS_LOGIN,true);
                        toClicentDate.putInt(USER_ID,10086);
                        msgToClient.setData(toClicentDate);
                        msgToClient.what = MSG_TO_CLIENT;

                        //传回Client
                        msgfromClient.replyTo.send(msgToClient);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }

            super.handleMessage(msgfromClient);
        }
    });

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
```

**注册文件**

```
        <service android:name=".MessengerServer">
            <intent-filter>
                <action android:name="android.intent.action.MESSENGER"/>
            </intent-filter>
        </service>
```
服务端就一个Service，可以看到代码相当的简单，只需要去声明一个Messenger对象，然后onBind方法返回mMessenger.getBinder()；

这里我添加了sleep(2000)模拟耗时,注意在实际使用过程中，可以换成在独立开辟的线程中完成耗时操作，比如和HandlerThread结合使用。

=========================**客户端**=========================：

```
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
                            data.get(IS_LOGIN)+"\n");
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
```

首先bindService，然后在onServiceConnected中拿到回调的service（IBinder）对象，通过service对象去构造一个mService =new Messenger(service);然后就可以使用mService.send(msg)给服务端了。

我们看到在点击事件里面我们往服务端发送消息：

```
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
```
那么服务端会收到消息，处理完成会将结果返回，传到Client端的mMessenger中的Handler的handleMessage方法中。

这样我们就实现了用messenger的双向通信，不过也发现我们前面说的问题，虽然使用简单，不用AIDL文件，但是不支持RPC，那么我们接下来看一下AIDL的用法。
### 二、AIDL的用法
#### 1、概述
>这里的Demo主要功能是在客户端发起登录，登出，服务端处理相应事件，之后将相应事件再回传给客户端。

这里需要先注册两个AIDL文件：
（这里的AIDL的文件相当于一个是客户端的，一个是服务端的）

>IGuideAidlInterface 客户端调用服务端的相关接口
```
package messsage.binli.com.aidlserver;
import messsage.binli.com.aidlserver.IGuideListener;

interface IGuideAidlInterface {

    void login(String userName , String passWord , String packageName);  //登录

    void logout(String packageName);  //登出

    boolean isLogin();  //是否登录

    void registerListener(in IGuideListener listener); //注册接口

    void unregisterListener(in IGuideListener listener); //解注册接口
}
```

>IGuideListener 返回给客户端相应的处理结果
```
// IGuideListener.aidl
package messsage.binli.com.aidlserver;

// Declare any non-default types here with import statements

interface IGuideListener {

     void onLoginSuccess(String msg);

     void onLoginFail(String msg);

     void onLogoutSuccess(String msg);

     void onLogoutFail(String msg);
}
```

⚠️：客户端和服务端都需要AIDL文件且需要一致。把服务端生成的AIDL文件考入到客户端即可（路基必须保持和服务端一致），如图：
![这里写图片描述](http://img.blog.csdn.net/20171127113314708?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ2l0aHViXzMzMzA0MjYw/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![这里写图片描述](http://img.blog.csdn.net/20171127113321653?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ2l0aHViXzMzMzA0MjYw/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
#### 2、实例
接下来我们看一下实例代码，一个服务端apk（AidlServer），一个客户端apk（AidlClient）。 
![这里写图片描述](http://img.blog.csdn.net/20171127113535543?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvZ2l0aHViXzMzMzA0MjYw/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
代码非常简单就不详细讲解了。
======================**服务端**：======================

```
public class GuideServer extends Service{
    private IGuideListener iGuideListener;

    private IGuideAidlInterface.Stub mBinder = new IGuideAidlInterface.Stub() {
        @Override
        public void login(final String userName, final String passWord, String packageName) throws RemoteException {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //模拟延迟任务
                    try {
                        Thread.sleep(2000);

                        if (userName.equals("binli") && passWord.equals("123456")) {
                            iGuideListener.onLogoutSuccess("登录成功: "+userName);
                        } else {
                            iGuideListener.onLoginFail("登录失败: "+userName);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                }
            }).start();
        }

        @Override
        public void logout(final String packageName) throws RemoteException {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //模拟延迟任务
                    try {
                        Thread.sleep(2000);

                        if(packageName.equals("messsage.binli.com.aidlclient")){
                            iGuideListener.onLogoutSuccess("登出成功！");
                        }else{
                            iGuideListener.onLogoutSuccess("登出失败！");

                        }


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }


                }
            }).start();
        }

        @Override
        public boolean isLogin() throws RemoteException {
            return false;
        }

        @Override
        public void registerListener(IGuideListener listener) throws RemoteException {
            if (listener != null) {
                iGuideListener = listener;
            }
        }

        @Override
        public void unregisterListener(IGuideListener listener) throws RemoteException {
            if (listener != null) {
                iGuideListener = null;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}
```

**注册文件**
```
<service
            android:name="messsage.binli.com.aidlserver.GuideServer">
            <intent-filter>
                <action android:name="com.ecarx.membercenter.action.GUIDE"/>
            </intent-filter>
        </service>
```

======================**客户端**======================：

```
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

```

项目地址：
https://github.com/libin7278/IPC

如果有帮助麻烦star一下 ～～
