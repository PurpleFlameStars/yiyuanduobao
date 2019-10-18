package com.game.box.gamebox.webview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.LinearLayout;

import com.game.box.gamebox.novel.Monitor;
import com.game.box.gamebox.novel.PermissionHelper;
//import com.game.box.gamebox.utils.Logger;
import com.game.box.gamebox.utils.Logger;
import com.sihai.shopping.snatch.R;

//import retrofit2.Call;
//import retrofit2.Response;
//import rx.Subscription;

public class RichWebView extends ZCMProgressWebView {
    //        implements OnHandleResponse, OnOAuthResponse, WebViewInterface {
    private final String TAG = "RichWebView";

    //    private JavaScriptUtils mJSUtils;
    private JavaScriptProtocolProcessor mJsUtils;

    private Activity currentHandleActivity = null;
    private String mTag = "RichWebView";

    //文件上传临时变量
    private ValueCallback<Uri> mUploadMessage;

    public ValueCallback<Uri[]> uploadMessage;


    //consts
    private final int FILE_UPLOAD = 32100;
    private final int FILE_UPLOAD_ANDROID_5 = 32101;
    private final int SELECT_PICTURE_REQ = 32121;


    private String mSuccessUrl;

    private String mFailedUrl;

    private ViewGroup mErrorLayout;

    boolean cacheJSAndCSS = true;

    private Handler handler = new Handler(Looper.getMainLooper());

//    private WebProtocol webProtocol;

    private static final String JS_SUFFIX = ".js";
    private static final String CSS_SUFFIX = ".css";

//    public void setOnOAuthResponse(OnOAuthListener listener) {
//        mOnOAuthListener = listener;
//    }

    public RichWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RichWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RichWebView(Context context) {
        super(context);
    }

    //
    public void setRichWebView(RichWebView mWebView, LinearLayout mLayout) {
        setRichWebErrorLayout(mLayout);
    }

    //
//    /**
//     * 移除旧的error布局，将新加的填写进来
//     *
//     * @param mLayout
//     */
    public void setRichWebErrorLayout(ViewGroup mLayout) {
        removeView(mErrorLayout);
        mErrorLayout = mLayout;
        if (mErrorLayout.getParent() != null) {
            ((ViewGroup) mErrorLayout.getParent()).removeView(mErrorLayout);
        }
        addView(mErrorLayout, 1, getOrignalWebView().getLayoutParams());
        View view= mErrorLayout.findViewById(R.id.job_error_layout);
        Button but= (Button) view.findViewById(R.id.error_net_retry);
        but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLoadding) {
                    reload();
                } else {
                    Logger.d(mTag, "isLoading...");
                }
            }
        });
    }

    @Override
    public void loadUrl(String url) {
//        url = formatURL(url);
      //  CookieUtils.synCookies();
        if (currentHandleActivity == null) {
            throw new Error("**************************  RichWebView 使用错误, 必须先调用 init 方法传入一个Activity 才可以正常使用!**********");
        }
        super.loadUrl(url);
    }

    private JsObject javaInterfaceObject = new JsObject() {
        @JavascriptInterface
        public void executeCmd(final String jsonStr) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mJsUtils.executeJSCmdStr(jsonStr);
                }
            });
        }

    };

    @SuppressLint("JavascriptInterface")
    public void init(final Activity activity) {
        currentHandleActivity = activity;
        getOrignalWebView().setWebViewClient(new RichWebViewClient());
        getOrignalWebView().setWebChromeClient(new RichWebChromeClient());
        WebSettings settings = getOrignalWebView().getSettings();
        settings.setTextZoom(100);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
       // getOrignalWebView().setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        settings .setAppCacheMaxSize(1024*1024*20);
        settings.setPluginState(WebSettings.PluginState.ON);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setBlockNetworkImage(true);
        settings.setSupportZoom(false);
        settings.setUseWideViewPort(true);
        settings.setLoadsImagesAutomatically(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        getOrignalWebView().setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    currentHandleActivity.startActivity(intent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        String appCachePath = activity.getApplicationContext().getCacheDir().getAbsolutePath() + "/webview";
        Logger.d("RichWebView", "appCachePath : " + appCachePath);
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            settings.setAllowFileAccessFromFileURLs(false);
            settings.setAllowUniversalAccessFromFileURLs(false);
        }
        settings.setAppCacheEnabled(true);
        settings.setDatabaseEnabled(true);
        mJsUtils = new JavaScriptProtocolProcessor(currentHandleActivity, this);
        mJsUtils.startMonitor(new Monitor() {
            @Override
            public void Success(PermissionHelper permissionHelper) {
                monitor.Success(permissionHelper);
            }
        });
        addJavascriptInterface(javaInterfaceObject, JSCmd.JS_INTERFACE_NAME);

        //在内部添加错误布局
        if (mErrorLayout == null) {
            mErrorLayout = (ViewGroup) LayoutInflater.from(activity).inflate(R.layout.activity_common_richweb_error_layout, null);
            setRichWebErrorLayout(mErrorLayout);
        }
    }

    boolean isLoadding = false;

    public boolean isCacheJSAndCSS() {
        return cacheJSAndCSS;
    }

    /**
     * 设置是否最大本地缓存JS和CSS，默认打开
     *
     * @param cacheJSAndCSS
     */
    public void setCacheJSAndCSS(boolean cacheJSAndCSS) {
        this.cacheJSAndCSS = cacheJSAndCSS;
    }


    private class RichWebViewClient extends ZCMWebViewClient {
        private boolean isError = false;
        private long begintime;
        private long endtime;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.d(TAG, " override url=" + url.substring(0, url.length() > 50 ? 50 : url.length()));
            if (mLoadListenr != null && mLoadListenr.shouldOverrideUrlLoading(view, url)) {
                return true;
            } else {
                return WebViewOverrideUrlLoadingUtils.shouldOverrideUrlLoading(view, url) || super.shouldOverrideUrlLoading(view, url);
            }
        }

        /**
         * @param view
         * @param url
         * @param favicon
         */
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            if (!WebUrlWhiteListUtil.INSTANCE.check(url)) {
//            }
//            Log.d(""," start url="+url.substring(0,url.length()>50?50:url.length()));
            super.onPageStarted(view, url, favicon);
//            ReportTimeUtils.start(url);
//            ReportLoadTime.instance().start(url);
            begintime = System.currentTimeMillis();
            if (mLoadListenr != null) {
                mLoadListenr.onPageStarted(view, url, favicon);
            }
            isError = false;
            isLoadding = true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            isLoadding = false;
            if (mErrorLayout != null) {
                if (isError) {
//                    ReportLoadTime.instance().loadUrlError(url, true);
                    getOrignalWebView().setVisibility(View.GONE);
                    mErrorLayout.setVisibility(View.VISIBLE);
                    if (mLoadFailedListener != null) {
                        mLoadFailedListener.onFail();
                    }
                } else {
                    getOrignalWebView().setVisibility(View.VISIBLE);
                    mErrorLayout.setVisibility(View.GONE);
                }
            }
            endtime = System.currentTimeMillis() - begintime;

            Logger.d(mTag, "loadingUrl:" + url, "loadingTime:" + Long.toString(endtime));

            if (mLoadListenr != null) {
                mLoadListenr.onPageFinished(view, url);
            }
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            this.isError = true;
            super.onReceivedError(view, errorCode, description, failingUrl);
            Logger.d(mTag, "onReceivedError(), description = [" + description + "], url = [" + failingUrl + "]");
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            if (isClearHistrory) {
                isClearHistrory = false;
                RichWebView.this.getOrignalWebView().clearHistory();
            }
        }


        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//            if (isCacheJSAndCSS()) {
//                //去除参数
//                String simpleUrl = url.contains("?") ? url.substring(0, url.indexOf("?")) : url;
//                if (simpleUrl.toLowerCase().endsWith(JS_SUFFIX) || simpleUrl.toLowerCase().endsWith(CSS_SUFFIX)) {
//                    FreedomApi freedomApi = RetrofitApiFactory.createApi(FreedomApi.class);
//                    Call<ResponseBody> call = freedomApi.getMaxCacheData(url, new HashMap<String, String>());
//                    Response<ResponseBody> response = null;
//                    try {
//                        response = call.execute();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    if (response != null && response.code() == HttpURLConnection.HTTP_OK) {
//                        if (simpleUrl.toLowerCase().endsWith(JS_SUFFIX)) {
//                            return new WebResourceResponse("application/javascript", "utf-8", response.body().byteStream());
//                        } else if (simpleUrl.toLowerCase().endsWith(CSS_SUFFIX)) {
//                            return new WebResourceResponse("text/css", "utf-8", response.body().byteStream());
//                        }
//                    }
//                }
//            }
            return null;
        }

    }

    public boolean canGoBack() {
        return getOrignalWebView().canGoBack();
    }

    public void goBack() {
        getOrignalWebView().goBack();
    }

    public void destroy() {
        getOrignalWebView().destroy();
    }

    private class RichWebChromeClient extends IMWebChromeClient {
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            super.onConsoleMessage((ConsoleMessage) consoleMessage);
            if (consoleMessage != null) {
                String message = consoleMessage.message();
                Logger.d(mTag, message, "at " + consoleMessage.sourceId() + " line:" + consoleMessage.lineNumber());
                if (message != null && message.contains("Uncaught") && message.contains("Error")) {
                    Logger.e("js", message);
                }
            }
            return true;
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,  GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, true);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType, String capture) {
            if (mChromeClientListener != null) {
                mChromeClientListener.openFileChooser(uploadMsg, acceptType, capture);
                return;
            }
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            currentHandleActivity.startActivityForResult(
                    Intent.createChooser(intent, "完成操作需要使用"),
                    FILE_UPLOAD);

        }

        // 3.0 + 调用这个方法
        public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                    String acceptType) {
            if (mChromeClientListener != null) {
                mChromeClientListener.openFileChooser(uploadMsg, acceptType);
                return;
            }
            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            currentHandleActivity.startActivityForResult(
                    Intent.createChooser(intent, "完成操作需要使用"),
                    FILE_UPLOAD);
        }

        // Android < 3.0 调用这个方法
        public void openFileChooser(ValueCallback<Uri> uploadMsg) {
            if (mChromeClientListener != null) {
                mChromeClientListener.openFileChooser(uploadMsg);
                return;
            }

            mUploadMessage = uploadMsg;
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            currentHandleActivity.startActivityForResult(
                    Intent.createChooser(intent, "完成操作需要使用"),
                    FILE_UPLOAD);

        }

        // For Lollipop 5.0+ Devices
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            if (mChromeClientListener != null) {
                return mChromeClientListener.onShowFileChooser(mWebView, filePathCallback, fileChooserParams);
            }

            uploadMessage = filePathCallback;
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "选择");

            try {
                currentHandleActivity.startActivityForResult(chooserIntent, FILE_UPLOAD_ANDROID_5);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                return false;
            }
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (newProgress >= 100) {
                getOrignalWebView().getSettings().setBlockNetworkImage(false);
                String url = null;
                try {
                    url = view.getOriginalUrl();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (url != null) {
                    //上报M页加载时间
//                    ReportLoadTime.instance().loadUrlfinish(url, true);
                }
            }
        }
        @Override
        public void onReachedMaxAppCacheSize(long requiredStorage, long quota, WebStorage.QuotaUpdater quotaUpdater) {
            super.onReachedMaxAppCacheSize(requiredStorage, quota, quotaUpdater);
            quotaUpdater.updateQuota(requiredStorage * 2);
        }
    }


//    public interface OnOAuthListener {
//
//        void onSending(Platform.OAuthType type);
//
//        void onCompleted(Platform.OAuthType type, OAuthInfo info, String successUrl, String failedUrl);
//
//        void onCanceled(Platform.OAuthType type);
//
//        void onFailed(Platform.OAuthType type, String reason);
//    }

    public interface OnActivityOperateListener {

        Boolean onContainKey(String key);

        void onOperate(String key, String url);

    }

    /**
     * WebViewClient的对外扩展接口
     */
    public static abstract class WebViewClientListener {

        /**
         * Notify the host application that a page has started loading. This method
         * is called once for each main frame load so a page with iframes or
         * framesets will call onPageStarted one time for the main frame. This also
         * means that onPageStarted will not be called when the contents of an
         * embedded frame changes, i.e. clicking a link whose target is an iframe.
         *
         * @param view    The WebView that is initiating the callback.
         * @param url     The url to be loaded.
         * @param favicon The favicon for this page if it already exists in the
         *                database.
         */
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

        }


        /**
         * Notify the host application that a page has finished loading. This method
         * is called only for main frame. When onPageFinished() is called, the
         * rendering picture may not be updated yet. To get the notification for the
         * new Picture, use {@link WebView.PictureListener#onNewPicture}.
         *
         * @param view The WebView that is initiating the callback.
         * @param url  The url of the page.
         */
        public void onPageFinished(WebView view, String url) {

        }

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }

    }

    private WebViewClientListener mLoadListenr;

    /**
     * 添加WebView加载状态监听回调
     */
    public void setWebViewClientListener(WebViewClientListener listener) {
        this.mLoadListenr = listener;
    }

    private boolean isClearHistrory = false;

    /**
     * 会清理回退栈数据
     *
     * @param url url
     */
    public void clearLoadUrl(String url) {
        this.getOrignalWebView().clearHistory();
        loadUrl(url);
        isClearHistrory = true;
    }

    private IOnLoadPageFailedListener mLoadFailedListener;

    public void setOnLoadFailedListener(IOnLoadPageFailedListener listener) {
        mLoadFailedListener = listener;
    }

    /**
     * 页面失败回调
     */
    public interface IOnLoadPageFailedListener {
        void onFail();
    }

    private WebChromeClientListener mChromeClientListener;

    /**
     * 设置WebChromeClient 相关的事件回调
     *
     * @param listener
     */
    public void setWebChromeClientListener(WebChromeClientListener listener) {
        mChromeClientListener = listener;
    }

    public interface WebChromeClientListener {

        /**
         * For Lollipop 5.0+ Devices
         *
         * @param webView
         * @param filePathCallback
         * @param fileChooserParams
         * @return
         */
        boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                  WebChromeClient.FileChooserParams fileChooserParams);

        /**
         * Android < 3.0 调用这个方法
         *
         * @param uploadMsg
         */
        void openFileChooser(ValueCallback<Uri> uploadMsg);

        /**
         * 3.0 + 调用这个方法
         *
         * @param uploadMsg
         * @param acceptType
         */
        void openFileChooser(ValueCallback<Uri> uploadMsg,
                             String acceptType);

        void openFileChooser(ValueCallback<Uri> uploadMsg,
                             String acceptType, String capture);

    }

    public void setOnScrollChangedListener(ZCMWebView.IOnScrollChangedListener listener) {
        try {
            ZCMWebView webView = getOrignalWebView();
            if (webView != null) {
                webView.setOnScrollChangedListener(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private Monitor monitor;
    public void startMonitor(Monitor monitor){
        this.monitor=monitor;
    }
}
