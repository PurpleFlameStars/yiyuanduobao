package com.game.box.gamebox.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

//import com.game.box.gamebox.utils.Logger;
import com.game.box.gamebox.utils.Logger;
import com.game.box.gamebox.utils.NotNullUtils;
import com.sihai.shopping.snatch.BuildConfig;


/**
 * Created by ybb
 * Date: 16-11-1.
 */
public class ZCMWebView extends WebView {
    private static final String TAG = "ZCMWebView";
    private WebChromeClient mWebChromeClient;
    private WebViewClient mWebViewClient;
    private ZCMWebView.onPageFinishedListener pageFinishedListener;

    public ZCMWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init();
    }

    public ZCMWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ZCMWebView(Context context) {
        super(context);
        this.init();
    }

    private String TAGS = "JobInterceptorMonitor" ;

    @SuppressLint({"SetJavaScriptEnabled"})
    private void init() {
        this.mWebChromeClient = new ZCMWebView.IMWebChromeClient();
        this.mWebViewClient = new ZCMWebView.IMWebViewClient();
        WebSettings mWebSettings = this.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= 21) {
            mWebSettings.setMixedContentMode( WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        //处理ua
        addYoukantouUserAgent(mWebSettings);
        this.setWebChromeClient(this.mWebChromeClient);
        this.setWebViewClient(this.mWebViewClient);
        Logger.d("ZCMWebView", "init imwebview....");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            setWebContentsDebuggingEnabled(true);
        }
    }

    private void addYoukantouUserAgent(WebSettings settings) {
        if (settings == null) {
            return;
        }
        String uaString = settings.getUserAgentString();
        uaString = NotNullUtils.getSafetyString(uaString) + " " + UserAgent.getUserAgent();
        settings.setUserAgentString(uaString);
    }

    protected boolean shouldOverrideUrlLoading(String url) {
        Logger.d("ZCMWebView", "shouldOverrideUrlLoading:" + url);
        if(url.startsWith("mailto:") || url.startsWith("geo:") || url.startsWith("tel:")) {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
            if(this.getContext() != null) {
                this.getContext().startActivity(intent);
            }
        }

        return true;
    }

    public void setOnPageFinishedListener(ZCMWebView.onPageFinishedListener l) {
        this.pageFinishedListener = l;
    }

    public interface onPageFinishedListener {
        void onPageFinished();
    }

    private class IMWebChromeClient extends WebChromeClient {
        private IMWebChromeClient() {
        }
    }

    private class IMWebViewClient extends WebViewClient {
        private IMWebViewClient() {
        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.d("ZCMWebView ", "shouldOverrideUrlLoading"+url);
            if(!url.startsWith("mailto:") && !url.startsWith("geo:") && !url.startsWith("tel:")) {
                view.loadUrl(url);
            } else {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
                if(ZCMWebView.this.getContext() != null) {
                    ZCMWebView.this.getContext().startActivity(intent);
                }
            }

            return true;
        }

        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Logger.d("ZCMWebView", "onPageStarted:" + url);
            super.onPageStarted(view, url, favicon);
        }

        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(ZCMWebView.this.pageFinishedListener != null) {
                ZCMWebView.this.pageFinishedListener.onPageFinished();
            }

        }
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChangedListener != null) {
            onScrollChangedListener.onScrollChanged(l, t, oldl, oldt);
        }
    }

    private IOnScrollChangedListener onScrollChangedListener;
    public void setOnScrollChangedListener(IOnScrollChangedListener listener) {
        onScrollChangedListener = listener;
    }

    public interface IOnScrollChangedListener {
        void onScrollChanged(int l, int t, int oldl, int oldt);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (this.getScrollY() <= 0)
                    this.scrollTo(0, 1);
                break;
            case MotionEvent.ACTION_UP:
                //                if(this.getScrollY() == 0)
                //                this.scrollTo(0,-1);
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }
}
