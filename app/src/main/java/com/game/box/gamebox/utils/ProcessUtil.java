package com.game.box.gamebox.utils;

import android.app.ActivityManager;
import android.content.Context;

import com.game.box.gamebox.App;


/**
 * Created by liruidong on 2018/7/4.
 * description: 进程相关工具类
 */

public class ProcessUtil {
    /**
     * 获取当前进程的名称
     * @param context context
     * @return 当前进程名称
     */
    public static String getCurProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                    .getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * 是否是主进程
     * @param context context
     * @return 是否是主进程
     */
    public static boolean isMainProcess(Context context) {
        return context != null && App.MAIN_PROCESS_NAME.equals(ProcessUtil.getCurProcessName(context));
    }
}
