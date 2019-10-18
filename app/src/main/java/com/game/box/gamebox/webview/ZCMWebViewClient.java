package com.game.box.gamebox.webview;

import android.net.http.SslError;


import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ZCMWebViewClient extends WebViewClient {

    public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
//        if (Config.APP_OFF_LINE_MODE) {
//            handler.proceed();
//        }
    }
}
