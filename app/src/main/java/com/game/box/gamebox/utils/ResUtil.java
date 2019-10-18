package com.game.box.gamebox.utils;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import com.game.box.gamebox.App;


public class ResUtil {

    /**
     * 获取drawable资源的id
     * @param v eg:@drawable/aaa
     * @return 资源id
     */
    public static int getDrawableIdByString(String v) {
        if (TextUtils.isEmpty(v) || !v.contains("@drawable")) {
            return 0;
        }
        return getResIdByString(v);
    }

    /**
     * 通过字符串找到resid.
     *
     * @param v eg:@drawable/aaa   @color/white
     */
    public static int getResIdByString(String v) {
        if (TextUtils.isEmpty(v) || !v.contains("@")) {
            return 0;
        }
        Context context = App.getInstance();
        try {
            v = v.substring(1);
            String vs[] = v.split("/");
            Resources res = context.getResources();
            return res.getIdentifier(vs[1], vs[0], context.getPackageName());
        } catch (Exception e) {
            return 0;
        }
    }
}
