package com.game.box.gamebox.novel;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;

public class PermissionUtil {

	/**
	 * 判断是否有某个权限
	 * @param context
	 * @param permission
	 * @return
	 */
	public static boolean hasPermission(Context context, String permission){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if(context.checkSelfPermission(permission)  != PackageManager.PERMISSION_GRANTED){
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是否有权限组
	 * @param context
	 * @param permissions
	 * @return true-表示没有开权限  false-表示权限已开启
	 */
	public static boolean hasPermissions(Context context, String[] permissions){
		for (String permission : permissions) {
			if (hasPermission(context,permission)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 弹出对话框请求权限
	 * @param activity
	 * @param permissions
	 * @param requestCode
	 */
	public static void requestPermissions(Activity activity, String[] permissions, int requestCode){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			activity.requestPermissions(permissions, requestCode);
		}
	}

	/**
	 * 返回缺失的权限
	 * @param context
	 * @param permissions
	 * @return 返回缺少的权限，null 意味着没有缺少权限
	 */
	public static String[] getDeniedPermissions(Context context, String[] permissions){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			ArrayList<String> deniedPermissionList = new ArrayList<>();
			for(String permission : permissions){
				if(context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED){
					deniedPermissionList.add(permission);
				}
			}
			int size = deniedPermissionList.size();
			if(size > 0){
				return deniedPermissionList.toArray(new String[deniedPermissionList.size()]);
			}
		}
		return null;
	}
}
