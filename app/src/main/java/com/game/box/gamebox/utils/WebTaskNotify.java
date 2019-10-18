package com.game.box.gamebox.utils;


import com.game.box.gamebox.webview.SwipeRefreshWebView;

/**
 * 通知web页面任务更新处理类
 */
public class WebTaskNotify {
    private static final String TAG = "WebTaskNotify";

    /**
     * 当前webview调用一个javascript方法
     * @param webView webview
     * @param accessToken Token
     * @param object object
     */
    public static void notifyLogin(SwipeRefreshWebView webView, String accessToken,String object) {
        if (webView == null) {
            return;
        }
        String javascript = "javascript:facebookLogin('" + accessToken + "','"+object+"')";
        Logger.d(TAG, "javascript: " + javascript);
        webView.loadUrl(javascript);
    }
}
