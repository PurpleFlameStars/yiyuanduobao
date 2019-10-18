package com.game.box.gamebox.webview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ProgressBar;


import com.game.box.gamebox.utils.AndroidUtil;
import com.sihai.shopping.snatch.R;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by ybb
 * Date: 16-11-1.
 */
public class ZCMProgressWebView extends FrameLayout {
    private ProgressBar progressbar;
    private ZCMWebView webView;
    private Context mContext;
    private LayoutInflater mInflater;
    private WebViewClient webViewClient;
    private WebChromeClient webChromeClient;

    public void setWebViewClient(WebViewClient webViewClient) {
        this.webViewClient = webViewClient;
    }

    public void setWebChromeClient(WebChromeClient webChromeClient) {
        this.webChromeClient = webChromeClient;
    }

    public ZCMWebView getOrignalWebView() {
        return this.webView;
    }

    public ZCMProgressWebView(Context context) {
        this(context, (AttributeSet)null);
    }

    public ZCMProgressWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ZCMProgressWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initView(context, attrs);
    }
//
    private void initView(Context context, AttributeSet attrs) {
        this.mContext = context;
        /*
        * http://stackoverflow.com/questions/18038772/leaked-window-exception-even-though-no-service-is-used
        * webview 打开updateJavaScriptEnabled(true)时，会绑定语音识别的service，此处使用Application的Context防止内存泄漏
        * */
        this.mInflater = LayoutInflater.from(mContext);
        this.mInflater.inflate(R.layout.zcm_progress_web_view_layout, this);
        this.progressbar = (ProgressBar)this.findViewById(R.id.zcm_webview_progress);
        this.webView = (ZCMWebView)this.findViewById(R.id.zcm_webview);
        this.webView.setWebChromeClient(new ZCMProgressWebView.IMWebChromeClient());
        this.webView.setWebViewClient(new ZCMProgressWebView.IMWebViewClient());
    }

    public void loadUrl(String url) {
        if(this.webView != null) {
            Map<String, String> headerMap = new HashMap<>();
            /*headerMap.put("x-klapp-token", User.getInstance().getToken());
            headerMap.put("uid", User.getInstance().getUid());*/
            headerMap.put("imei", AndroidUtil.getDeviceId());
            headerMap.put("realimei", AndroidUtil.getM2Imei());
            headerMap.put("version", AndroidUtil.getVersionName());
            headerMap.put("versionCode", String.valueOf(AndroidUtil.getVersionCode()));

            this.webView.loadUrl(url, headerMap);
        }
    }

    public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String historyUrl) {
        if (this.webView != null) {
            this.webView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl);
        }
    }

    public void reload() {
        if(this.webView != null) {
            this.webView.reload();
        }

    }

    public void loadUrlWithCookie(String url, String cookie) {
        if(cookie != null && "".equals(cookie)) {
            CookieSyncManager.createInstance(this.mContext);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            cookieManager.setCookie(url, cookie);
            CookieSyncManager.getInstance().sync();
            HashMap map = new HashMap();
            map.put("cookie", cookie);
            this.webView.loadUrl(url, map);
        } else {
            this.webView.loadUrl(url);
        }

    }

    public WebSettings getSettings() {
        return this.webView != null?this.webView.getSettings():null;
    }

    public void addJavascriptInterface(JsObject object, String name) {
        if(this.webView != null) {
            this.webView.addJavascriptInterface(object, name);
        }

    }

    private int dip2px(float dpValue) {
        float scale = this.getContext().getResources().getDisplayMetrics().density;
        return (int)(dpValue * scale + 0.5F);
    }

    public void destroy() {
        if(this.webView != null) {
            this.webView.destroy();
        }

    }

    public class JsObject {
        public JsObject() {
        }

        @JavascriptInterface
        public String demo() {
            return "injectedObject";
        }
    }

    public class IMWebChromeClient extends WebChromeClient {
        public IMWebChromeClient() {
        }

        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressbar.setProgress(newProgress);
            if(newProgress >= 100) {
                progressbar.setVisibility(GONE);
                progressbar.setProgress(0);
            } else {
                progressbar.setVisibility(VISIBLE);
            }

            if(webChromeClient != null) {
                webChromeClient.onProgressChanged(view, newProgress);
            }

        }

        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            return webChromeClient != null? webChromeClient.onConsoleMessage(consoleMessage):super.onConsoleMessage(consoleMessage);
        }
    }

    public class IMWebViewClient extends WebViewClient {
        public IMWebViewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(webViewClient != null) {
                return webViewClient.shouldOverrideUrlLoading(view, url);
            } else {
                if(!url.startsWith("mailto:") && !url.startsWith("geo:") && !url.startsWith("tel:")) {
                    view.loadUrl(url);
                } else {
                    Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                    if(getContext() != null) {
                        getContext().startActivity(intent);
                    }
                }

                return true;
            }
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            if(webViewClient != null) {
                webViewClient.onPageStarted(view, url, favicon);
            }

        }

        public void onPageFinished(WebView view, String url) {
            if(webViewClient != null) {
                webViewClient.onPageFinished(view, url);
            }

        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            if(webViewClient != null) {
                webViewClient.onReceivedError(view, errorCode, description, failingUrl);
            }

        }
    }
}
