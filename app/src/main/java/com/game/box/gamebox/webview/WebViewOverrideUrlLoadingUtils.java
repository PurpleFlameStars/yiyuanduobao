package com.game.box.gamebox.webview;

import android.content.Intent;
import android.net.Uri;

import android.webkit.URLUtil;
import android.webkit.WebView;

public class WebViewOverrideUrlLoadingUtils {
    public static boolean shouldOverrideUrlLoading(WebView view, String url) {
        try {
            // 处理自定义scheme协议
            if (!URLUtil.isNetworkUrl(url)) {
                try {
                    // 以下固定写法
                    final Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    view.getContext().startActivity(intent);
                } catch (Exception e) {
                    // 防止没有安装的情况
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
