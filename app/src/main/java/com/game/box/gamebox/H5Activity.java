
package com.game.box.gamebox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
//import android.webkit.WebView;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.game.box.gamebox.novel.Monitor;
import com.game.box.gamebox.novel.PermissionHelper;
import com.game.box.gamebox.statusbar.StatusBarUtil;
import com.game.box.gamebox.utils.Logger;
import com.game.box.gamebox.utils.WebTaskNotify;
import com.sihai.shopping.snatch.R;
import com.game.box.gamebox.webview.RichWebView;
import com.game.box.gamebox.webview.SwipeRefreshWebView;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


public class H5Activity extends Activity {
    public static final String FROM_WHERE = "FROM_WHERE";
    public static final String FROM_PUSH = "FROM_PUSH";
    public static final String PUSH_TYPE = "PUSH_TYPE";
    private SwipeRefreshWebView webView;
    //    private WebView webView;
    private String title;
    private String url;
    private int currentType = 0;
    private boolean hideTitle = false;
    private boolean hideRefresh = false;
    private boolean showCloseBtn = false;
    private String TAG = "H5Activity-ParentActivity";

    private PermissionHelper permissionHelpers;

    private String startUrl="http://yyg.51youkantou.com/wap/index.php?show_prog=1";
    private String bugUrl="http://shop.51youkantou.com/wap/index.php?show_prog=1";
    private String loadUrl;

    private  CallbackManager callbackManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_web_view);
        StatusBarUtil.setRootViewFitsSystemWindows(this,false);
        //设置状态栏透明
        // StatusBarUtil.setTranslucentStatus(this);
        if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
            //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
            //这样半透明+白=灰, 状态栏的文字能看得清
            StatusBarUtil.setStatusBarColor(this,0x55000000);
        }
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
        Logger.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        doLogicWithIntent(getIntent());

    }

    private void doLogicWithIntent(Intent intent) {
        if (intent != null) {
            title = intent.getStringExtra(KEY_TITLE);
            url = intent.getStringExtra(KEY_URL);
            Logger.d(TAG, "doLogicWithIntent() called with: url = [" + url + "]");
            currentType = intent.getIntExtra(KEY_TYPE, 0);
            showCloseBtn = intent.getBooleanExtra(KEY_SHOW_CLOSE_BTN, false);
            hideTitle = intent.getBooleanExtra(KEY_HIDE_TITLE, false);
            hideRefresh = intent.getBooleanExtra(KEY_HIDE_REFRESH, false);

            reportFrom(intent);
        }
        initView();
        login();
    }

    private void reportFrom(Intent intent) {
        if (intent == null) {
            return;
        }
        String fromWhere = intent.getStringExtra(FROM_WHERE);
        if (FROM_PUSH.equals(fromWhere)) {
            Log.d(TAG, "reportFrom() called with: fromWhere = [" + fromWhere + "]");
            //ReportLogData.trace(ReportLogData.LAUNCH_FROM_PUSH, AndroidUtil.getChannel());
            //ReportLogData.trace(ReportLogData.single_click, "single_click");

            String pushType = intent.getStringExtra(PUSH_TYPE);
            /*if (NewsPushVo.TYPE_TITLE_IMG.equals(pushType)) {
                ReportLogData.trace(ReportLogData.single_text_click, "single_text_click");
            } else if (NewsPushVo.TYPE_BIG_IMG.equals(pushType)) {
                ReportLogData.trace(ReportLogData.single_bigimage_click, "single_bigimage_click");
            } else {
            }*/
        }
    }

    private void initView() {
        webView = (SwipeRefreshWebView) findViewById(R.id.common_operations_web_view);
        webView.startMonitor(new Monitor() {
            @Override
            public void Success(PermissionHelper permissionHelper) {
                permissionHelpers=permissionHelper;
            }
        });
        webView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.setRefreshing(true);
                loadPage();
            }
        });
        webView.getRichWebView().setWebViewClientListener(new RichWebView.WebViewClientListener() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.setRefreshing(false);
            }
        });
//        webView.init(this);

        /*if (hideRefresh) {
            webView.setRefreshEnable(false);
        } else {
            webView.setRefreshEnable(true);
        }*/
        webView.setRefreshEnable(false);
        loadPage();

        if (webView.getRichWebView() != null) {
            webView.getRichWebView().setWebViewClientListener(new RichWebView.WebViewClientListener() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    webView.setRefreshing(false);
                    loadUrl=url;
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    return false;
                }
            });
        }
    }

    private void loadPage() {
        Logger.d(TAG, "url: " + url);

        webView.loadUrl(startUrl);
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        doLogicWithIntent(intent);
    }

    @Override
    public void onBackPressed() {
        try {
            if (webView.getRichWebView().canGoBack()) {
                webView.getRichWebView().goBack();
                if (showCloseBtn) {
                }
            } else {
                super.onBackPressed();
            }
        } catch (Exception e) {
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            final ViewGroup viewGroup = (ViewGroup) webView.getParent();
            if (viewGroup != null) {
                viewGroup.removeView(webView);
            }
            if (webView.getRichWebView() != null) {
                webView.getRichWebView().destroy();
            }
        }
    }

    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_URL = "KEY_URL";
    public static final String KEY_TYPE = "KEY_TYPE";
    public static final String KEY_SHOW_CLOSE_BTN = "KEY_SHOW_CLOSE_BTN";
    public static final String KEY_HIDE_TITLE = "KEY_HIDE_TITLE";
    public static final String KEY_HIDE_REFRESH = "KEY_HIDE_REFRESH";

    /**
     * @param context     上下文环境
     * @param title       M页标题
     * @param url         M页url
     * @param requestCode 默认为-1
     */
    public static void startActivity(Context context, String title, @NonNull String url, int requestCode, int type, boolean showCloseBtn, boolean hideTitle, boolean hideRefresh) {
        if (context == null) {
            return;
        }
        Intent intent = new Intent(context, H5Activity.class);
        intent.putExtra(KEY_TITLE, title);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_TYPE, type);
        intent.putExtra(KEY_SHOW_CLOSE_BTN, showCloseBtn);
        intent.putExtra(KEY_HIDE_TITLE, hideTitle);
        intent.putExtra(KEY_HIDE_REFRESH, hideRefresh);
        if (requestCode > 0) {
            //((BaseActivity) context).startActivityForResult(intent, requestCode);
        } else {
            context.startActivity(intent);
        }
    }

    public static void startActivity(Context context, String title, @NonNull String url, int type) {
        startActivity(context, title, url, -1, type, false, false, false);
    }

    public static void startActivity(Context context, String title, @NonNull String url) {
        startActivity(context, title, url, -1, -1, false, false, false);
    }

    /**
     * 隐藏标题
     *
     * @param context
     * @param url
     */
    public static void startActivity(Context context, @NonNull String url) {
        startActivity(context, "", url, -1, -1, false, true, false);
    }

    /**
     * @param context
     * @param url
     * @param hidetitle
     */
    public static void startActivity(Context context, @NonNull String url, boolean hidetitle) {
        startActivity(context, "", url, -1, -1, false, hidetitle, false);
    }

    /**
     * @param context      上下文环境
     * @param title        页面title
     * @param url          页面地址
     * @param showCloseBtn m页回退的时候是否显示x号按钮
     */
    public static void startActivity(Context context, String title, String url, boolean showCloseBtn) {
        startActivity(context, title, url, -1, -1, showCloseBtn, false, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (permissionHelpers!=null){
            permissionHelpers.requestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    private long lastTime = 0L;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //LoginManager.getInstance().logInWithReadPermissions(H5Activity.this, Arrays.asList("public_profile"));
        if (startUrl.equals(loadUrl)){
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if ((System.currentTimeMillis() - lastTime) > 1000) {
                    Toast.makeText(H5Activity.this,
                            "Press again to exit the program", Toast.LENGTH_SHORT).show();
                    lastTime = System.currentTimeMillis();
                    return false;
                    //Press exit again"
                }
            }
            return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    private static final String EMAIL = "email";
    private static final String USER_POSTS = "user_posts";
    private static final String PUBLISH_ACTIONS = "publish_actions";
    private static final String PUBLIC_PROFILE = "public_profile";
    private static final String APP = "app";

    private void login(){
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG, "登录成功: " + loginResult.getAccessToken().getToken());
                Set<String> permissions = AccessToken.getCurrentAccessToken().getPermissions();

                String applicationId = loginResult.getAccessToken().getApplicationId();
                String userId = loginResult.getAccessToken().getUserId();
                getLoginInfo(loginResult.getAccessToken());
                Logger.d("accccc",applicationId+"/"+userId);
            }

            @Override
            public void onCancel() {
                Log.e(TAG, "登录取消");
            }

            @Override
            public void onError(FacebookException error) {
                if (error!=null){
                    Log.e(TAG, "登录错误"+error.toString());
                    if (error instanceof FacebookAuthorizationException) {
                        LoginManager.getInstance().logOut();
                    }
                }

            }
        });

    }
    public void getLoginInfo(final AccessToken accessToken ){

        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (object != null) {
                    String id = object.optString( "id" ) ;   //比如:1565455221565
                    String name = object.optString( "name" ) ;  //比如：Zhang San
                    String gender = object.optString("gender") ;  //性别：比如 male （男）  female （女）
                    String emali = object.optString("email") ;  //邮箱：比如：56236545@qq.com
                    Logger.d("accccc",object.toString());
                    //获取用户头像
                    JSONObject object_pic = object.optJSONObject( "picture" ) ;
                    JSONObject object_data = object_pic.optJSONObject( "data" ) ;
                    String photo = object_data.optString( "url" );

                    //获取地域信息
                    String locale = object.optString( "locale" ) ;   //zh_CN 代表中文简体
                    WebTaskNotify.notifyLogin(webView,accessToken.getToken(),object.toString());
                    ///WebTaskNotify.notifyLogin(webView,name,photo);
                    //Toast.makeText( H5Activity.this , "" + object.toString() , Toast.LENGTH_SHORT).show();
                }
            }
        }) ;

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,birthday,email,picture,locale,updated_time,timezone,age_range,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync() ;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
