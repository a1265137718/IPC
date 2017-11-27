package messsage.binli.com.aidlserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by doudou on 2017/11/24.
 */

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

