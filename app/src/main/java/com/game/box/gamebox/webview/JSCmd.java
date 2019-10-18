package com.game.box.gamebox.webview;

/**
 * Created by liruidong on 2018/5/26.
 * description:
 */

public class JSCmd {
    //用于native和js交互，注入到js环境中的对象
    public static final String JS_INTERFACE_NAME = "klapp";

    //跳转新的web页面
    public static final String OPEN_NEW_WEB_PAGE = "open_new_web_page";

    //Facebook登录
    public static final String FACEBOOK_LOGIN = "facebook_login";
    //Facebook退出登录
    public static final String FACEBOOK_LOGOUT = "facebook_logOut";
}
