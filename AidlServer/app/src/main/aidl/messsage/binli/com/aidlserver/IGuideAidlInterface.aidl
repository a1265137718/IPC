// IGuideAidlInterface.aidl
package messsage.binli.com.aidlserver;
import messsage.binli.com.aidlserver.IGuideListener;

interface IGuideAidlInterface {

    void login(String userName , String passWord , String packageName);  //登录

    void logout(String packageName);  //登出

    boolean isLogin();  //是否登录

    void registerListener(in IGuideListener listener); //注册接口

    void unregisterListener(in IGuideListener listener); //解注册接口
}
