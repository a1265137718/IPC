package messsage.binli.com.aidlserver;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.Nullable;

/**
 * Created by doudou on 2017/11/24.
 */

public class GuideServer extends Service{
    private IGuideListener iGuideListener;

    private RemoteCallbackList<IGuideListener> mRemoteCallbackList = new RemoteCallbackList<>();

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
                            loginSuccessCallback("登录成功: "+userName);
                            //iGuideListener.onLogoutSuccess();
                        } else {
                            loginFailCallback("登录失败: "+userName);
                            //iGuideListener.onLoginFail("登录失败: "+userName);
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
                            logoutSuccessCallback("登出成功！");
                            //iGuideListener.onLogoutSuccess("登出成功！");
                        }else{
                            logoutFailCallback("登出失败！");
                            //iGuideListener.onLogoutSuccess("登出失败！");
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
                mRemoteCallbackList.register(listener);
            }
        }

        @Override
        public void unregisterListener(IGuideListener listener) throws RemoteException {
            if (listener != null) {
                mRemoteCallbackList.unregister(listener);
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

    private void loginSuccessCallback(String msg) throws RemoteException{
        final int N = mRemoteCallbackList.beginBroadcast();
        for(int i = 0 ;i < N; i++){
            IGuideListener broadcastItem = mRemoteCallbackList.getBroadcastItem(i);
            if(broadcastItem != null){
                try{
                    broadcastItem.onLogoutSuccess(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            mRemoteCallbackList.finishBroadcast();
        }
    }

    private void loginFailCallback(String msg) throws RemoteException{
        final int N = mRemoteCallbackList.beginBroadcast();
        for(int i = 0 ;i < N; i++){
            IGuideListener broadcastItem = mRemoteCallbackList.getBroadcastItem(i);
            if(broadcastItem != null){
                try{
                    broadcastItem.onLogoutSuccess(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
        mRemoteCallbackList.finishBroadcast();
    }

    private void logoutSuccessCallback(String msg) throws RemoteException{
        final int N = mRemoteCallbackList.beginBroadcast();
        for(int i = 0 ;i < N; i++){
            IGuideListener broadcastItem = mRemoteCallbackList.getBroadcastItem(i);
            if(broadcastItem != null){
                try{
                    broadcastItem.onLogoutSuccess(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            mRemoteCallbackList.finishBroadcast();
        }
    }

    private void logoutFailCallback(String msg) throws RemoteException{
        final int N = mRemoteCallbackList.beginBroadcast();
        for(int i = 0 ;i < N; i++){
            IGuideListener broadcastItem = mRemoteCallbackList.getBroadcastItem(i);
            if(broadcastItem != null){
                try{
                    broadcastItem.onLogoutFail(msg);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            mRemoteCallbackList.finishBroadcast();
        }
    }

}

