// IGuideListener.aidl
package messsage.binli.com.aidlserver;

// Declare any non-default types here with import statements

interface IGuideListener {

     void onLoginSuccess(String msg);

     void onLoginFail(String msg);

     void onLogoutSuccess(String msg);

     void onLogoutFail(String msg);
}
