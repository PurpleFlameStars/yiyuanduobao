package com.game.box.gamebox.webview;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.game.box.gamebox.H5Activity;
import com.game.box.gamebox.novel.Monitor;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by liruidong on 2018/5/26.
 * description:
 */

public class JavaScriptProtocolProcessor {
    private static final String TAG = "JSProtocolProcessor";
    private Activity activity;
    private RichWebView webView;

    public JavaScriptProtocolProcessor(Activity activity, RichWebView webView) {
        this.activity = activity;
        this.webView = webView;
    }

    public void executeJSCmdStr(String jsonStr) {
        if (TextUtils.isEmpty(jsonStr)) {
            Log.e(TAG, "executeJSCmdStr: " + jsonStr);
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonStr);
            if (json != null) {
                String cmd = json.optString("cmd");
                JSONObject paramJson = json.optJSONObject("params");
                 if (JSCmd.OPEN_NEW_WEB_PAGE.equals(cmd)) {
                    openWebPage(paramJson);
                }else if (JSCmd.FACEBOOK_LOGIN.equals(cmd)) {
                    login();
                }else if (JSCmd.FACEBOOK_LOGOUT.equals(cmd)) {
                     logOut();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void logOut() {
        LoginManager.getInstance().logOut();
    }


    private void openWebPage(JSONObject paramJson) {
        boolean hideTitle = paramJson.optBoolean("hideTitle", false);
        boolean hideRefresh = paramJson.optBoolean("hideRefresh", false);
        String title = paramJson.optString("title");
        H5Activity.startActivity(activity, title, paramJson.optString("url"), -1, -1, false, hideTitle, hideRefresh);
    }

    private void login(){
        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("public_profile"));
    }

    private Monitor monitor;
    public void startMonitor(Monitor monitor){
        this.monitor=monitor;
    }
}
