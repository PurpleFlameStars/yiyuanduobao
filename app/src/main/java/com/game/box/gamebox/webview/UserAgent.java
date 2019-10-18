package com.game.box.gamebox.webview;

public class UserAgent {
    private static String mUserAgent = "funvideo_android";
    public static void setUserAgent(String useragent){
        mUserAgent = useragent;
    }

    public static String getUserAgent(){
        return mUserAgent;
    }
}
