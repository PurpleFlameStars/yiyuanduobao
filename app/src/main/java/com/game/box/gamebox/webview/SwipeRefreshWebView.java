package com.game.box.gamebox.webview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.webkit.WebView;

import com.game.box.gamebox.novel.Monitor;
import com.game.box.gamebox.novel.PermissionHelper;

import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


/**
 * Created by liruidong on 2018/6/23.
 * description:
 */

public class SwipeRefreshWebView extends SwipeRefreshLayout {
    private Context context;
    private RichWebView richWebView;
    private boolean refreshEnable = false;

    public SwipeRefreshWebView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public SwipeRefreshWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        richWebView = new RichWebView(context);
        richWebView.startMonitor(new Monitor() {
            @Override
            public void Success(PermissionHelper permissionHelper) {
                monitor.Success(permissionHelper);
            }
        });
        if (context instanceof Activity) {
            richWebView.init((Activity) context);
        }
        addView(richWebView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        setOnChildScrollUpCallback(new OnChildScrollUpCallback() {
            @Override
            public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child) {
                return richWebView.getOrignalWebView().getScrollY() > 0;
            }
        });
        scaleTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public void setRefreshing(boolean refreshing) {
        if (refreshEnable) {
            super.setRefreshing(refreshing);
        }
    }

    public void setRefreshEnable(boolean enabled) {
        refreshEnable = enabled;
    }

    public void loadUrl(String url) {
        richWebView.loadUrl(url);
    }

    public RichWebView getRichWebView() {
        return richWebView;
    }

    public void setOnScrollChangedListener(ZCMWebView.IOnScrollChangedListener listener) {
        if (richWebView != null) {
            richWebView.setOnScrollChangedListener(listener);
        }
    }

    private float preX;
    private float scaleTouchSlop;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!refreshEnable) {
            return richWebView.onInterceptTouchEvent(ev);
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = ev.getX();
                float instanceX = Math.abs(moveX - preX);
                if(instanceX > scaleTouchSlop + 60){
                    return false;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
    private Monitor monitor;
    public void startMonitor(Monitor monitor){
        this.monitor=monitor;
    }
    @Override
    public boolean canScrollVertically(int direction) {
        if (richWebView.getOrignalWebView() instanceof WebView) {
            return direction < 0 ? richWebView.getOrignalWebView().getScrollY() > 0
                    : richWebView.getOrignalWebView().getScrollY() < richWebView.getOrignalWebView()
                    .getMeasuredHeight();
        } else {
            return ViewCompat.canScrollVertically(richWebView.getOrignalWebView(), direction);
        }
    }
}
