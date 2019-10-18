package com.game.box.gamebox.utils;

import android.text.TextUtils;

public class NotNullUtils {
    public static String getSafetyString(String str) {
        return TextUtils.isEmpty(str) ? "" : str;
    }
}
