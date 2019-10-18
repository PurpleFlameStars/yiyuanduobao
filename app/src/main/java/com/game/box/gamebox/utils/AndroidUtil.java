
package com.game.box.gamebox.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.Vibrator;
import android.provider.Settings.Secure;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;


import com.game.box.gamebox.App;
import com.sihai.shopping.snatch.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.crypto.Cipher;

/**
 * 跟andoird系统相关的工具类
 */
public class AndroidUtil {

    private static AudioManager audioManager = null;
    private static String mDeviceImei;
    private static String TAG = "AndroidUtil";

    public static String getApplicationMetaData(String key) {
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        try {
            Context context = App.getInstance();
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            return appInfo.metaData.getString(key);
        } catch (NameNotFoundException e) {
            return "";
        }
    }

    public static void sendSMS(String phoneNume, String content, Context context) {
        String phone_number = phoneNume.trim();
        String sms_content = content.trim();
        if (phone_number.equals("")) {
            Toast.makeText(context, String.valueOf(R.string.common_send_sms_fail), Toast.LENGTH_SHORT).show();
        } else {
            Uri uri = Uri.parse("smsto:" + phoneNume);
            Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
            intent.putExtra("sms_body", sms_content);
            context.startActivity(intent);
        }
    }

    /**
     * 发送直接短信，不会调起本机短信程序
     *
     * @param phoneNumber 接收方号码
     * @param content     内容
     */
    public static void sendDirectSms(String phoneNumber, String content) {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> divideContents = smsManager.divideMessage(content);
        for (String text : divideContents) {
            smsManager.sendTextMessage(phoneNumber, null, text, null, null);
        }
    }

    /**
     * 调用本地短信app发送短信，无内容
     *
     * @param phoneNumber 手机号码，多个用分号分隔
     * @param context     上下文
     */
    public static void sendSmsByLocalApp(String phoneNumber, Context context) {
        Uri uri = Uri.parse("smsto:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        try {
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }

    /**
     * 调用系统拨号程序
     *
     * @param number  电话号码
     * @param context 上下文
     */
    public static void call(String number, Context context) {
        //调用拨号程序
        try {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.DIAL");
            intent.setData(Uri.parse("tel:" + number));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取Ip.
     *
     * @return
     */
    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return "";
    }

    /**
     * @return 获取这个应用的imei
     */
    public static String getDeviceId() {
        return getDeviceId(App.getInstance());
    }

    /**
     * 获取安卓设备唯一标识（这里读写SharedPerference逻辑别动）
     *
     * @param context
     * @return
     */
    public static String getDeviceId(Context context) {
        if (uuid == null) {
            //读取上次保存的UUID
            final String id = SharedPreferencesUtil.getInstance(context).getString(SharedPreferencesUtil.DEVICE_ID);
            if (!TextUtils.isEmpty(id)) {
                uuid = id;
            } else {
                //ANDROID_ID是设备第一次启动时产生和存储的64bit的一个数，当设备被wipe后该数重置
                //它在Android <=2.1 or Android >=2.3的版本是可靠、稳定的，但在2.2的版本并不是100%可靠的
                //在主流厂商生产的设备上，有一个很经常的bug，就是每个设备都会产生相同的ANDROID_ID：9774d56d682e549c
                try {
                    final String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
                    if (!"9774d56d682e549c".equals(androidId)) {
                        uuid = androidId;
                    } else {
                        //权限： 获取DEVICE_ID需要READ_PHONE_STATE权限，但如果我们只为了获取它，没有用到其他的通话功能，那这个权限有点大才小用
                        //bug：在少数的一些手机设备上，该实现有漏洞，会返回垃圾，如:zeros或者asterisks的产品
                        final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
                                .getDeviceId();
                        if (!TextUtils.isEmpty(deviceId)) {
                            uuid = deviceId;
                        } else {
                            uuid = UUID.randomUUID().toString();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    uuid = UUID.randomUUID().toString();
                }
                //保存的UUID
                SharedPreferencesUtil.getInstance(context).setString(SharedPreferencesUtil.DEVICE_ID, uuid);
            }
        }
        return uuid;
    }

    /**
     * sdcard是否可读写
     *
     * @return 是否可读写
     */
    public static boolean isSdcardReady() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    /**
     * 由于写入日志接口使用该方法，这里加个缓存数据，避免每次读取开销
     */
    private static int mIsSdcardAvailable = -1;

    /**
     * 判断sd卡剩余空间是否足够
     *
     * @return boolean
     */
    @SuppressWarnings("deprecation")
    public static boolean isSdcardAvailable() {
        if (mIsSdcardAvailable < 0) {
            mIsSdcardAvailable = 0;
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                File sdcardDir = Environment.getExternalStorageDirectory();
                StatFs sf = new StatFs(sdcardDir.getPath());
                long availCount = sf.getAvailableBlocks();
                long blockSize = sf.getBlockSize();
                long availSize = availCount * blockSize / 1024;

                if (availSize >= 3072L) {
                    mIsSdcardAvailable = 1;
                }
            }
        }

        return (mIsSdcardAvailable == 1);
    }

    /**
     * 获取文件系统的剩余空间，单位：KB
     *
     * @param dirName 文件目录
     * @return 剩余空间
     */
    @SuppressWarnings("deprecation")
    public static long getFileSystemAvailableSize(File dirName) {
        long availableSize = -1;
        if (dirName != null && dirName.exists()) {
            StatFs sf = new StatFs(dirName.getPath());
            long blockSize = sf.getBlockSize();
            long availableBlocks = sf.getAvailableBlocks();
            availableSize = availableBlocks * blockSize / 1024;
        }
        return availableSize;
    }

    /**
     * 获取播放焦点,使第三方播放器处于暂停状态
     *
     * @param context 上下文
     */
    public static void requestAudioFocus(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    /**
     * 获取播放焦点,使第三方播放器处于暂停状态。播放停止后，第三方音乐重新播放
     *
     * @param context 上下文
     */
    public static void requestAudioFocusDuck(Context context) {
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    }

    /**
     * 释放焦点
     */
    public static void abandonAudioFocus() {
        if (audioManager != null) {
            audioManager.abandonAudioFocus(afChangeListener);
        }
    }

    /**
     * 播放通知声音（系统默认）
     *
     * @param context
     */
    public static void notifySound(Context context) {
        NotificationManager manger = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification();
        notification.defaults = Notification.DEFAULT_SOUND;
        manger.notify(1, notification);
    }

    /**
     * 手机震动
     */
    public static void notifyVibrator(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    /**
     * 获取当前应用版本号
     *
     * @return 当前应用的版本号，如果获取失败则返回0
     */
    public static int getVersionCode() {
        int versionCode = 0;
        PackageManager manager;
        PackageInfo info = null;
        App app = App.getInstance();
        manager = app.getPackageManager();
        try {
            info = manager.getPackageInfo(app.getPackageName(), 0);
            versionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            versionCode = 0;
        }
        return versionCode;
    }

    /**
     * 获取当前应用版本名称
     *
     * @return 当前应用的版本名称，如果获取失败则返回“”
     */
    public static String getVersionName() {
        String versionName = "";
        PackageManager manager;
        PackageInfo info = null;
        App app = App.getInstance();
        manager = app.getPackageManager();
        try {
            info = manager.getPackageInfo(app.getPackageName(), 0);
            versionName = info.versionName;
        } catch (NameNotFoundException e) {
            versionName = "";
        } catch (Exception e) {
        }
        return versionName;
    }

    /**
     * 获取音乐播放器焦点的监听类
     */
    static OnAudioFocusChangeListener afChangeListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            }
        }
    };

    private static String uuid;

    /**
     * 获取手机型号
     *
     * @return
     */
    public static String getModel() {
        return Build.MODEL != null ? Build.MODEL.replace(" ", "") : "unknown";
    }


    public static String getRom() {
        String rom = Build.BRAND;
        return TextUtils.isEmpty(rom) ? "unknown" : rom;
    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Logger.d("VersionInfo", "Exception");
        }
        return versionName;
    }

    /**
     * 获取android系统版本
     *
     * @return
     */
    public static String getSystemVersion() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取屏幕密度，每英寸有多少个显示点
     *
     * @param context
     * @return
     */
    public static float getDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 判断应用是否后台运行
     *
     * @param context
     * @return
     */
    public static boolean isBackground(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
            for (RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
//                    if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
//                        return true;
//                    } else {
//                        return false;
//                    }
                    return appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * APP是否处于前台唤醒状态
     *
     * @return
     */
    public static boolean isAppOnForeground(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
            String packageName = context.getApplicationContext().getPackageName();
            List<RunningAppProcessInfo> appProcesses = activityManager
                    .getRunningAppProcesses();
            if (appProcesses == null)
                return false;

            for (RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(packageName)
                        && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static String cachedChannel;
    private static final String UNKNOWN_CHANNEL_STR = "unknown";

    /**
     * 获取渠道号
     *
     * @return
     */
    public static String getChannel() {
        if (TextUtils.isEmpty(cachedChannel) || UNKNOWN_CHANNEL_STR.equals(cachedChannel)) {
            String channelStr;
            channelStr = getApplicationMetaData("KL_CHANNEL");
            if (TextUtils.isEmpty(channelStr)) {
                channelStr = UNKNOWN_CHANNEL_STR;
            } else {
                cachedChannel = channelStr;
            }

            return channelStr;
        } else {
            return cachedChannel;
        }
    }

    /**
     * 获取status bar 高度
     */

    public static int getSystemStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    /**
     * 判断是否拥有某种权限
     *
     * @param permission 例如 android.permission.READ_CONTACTS
     * @return
     */
    public static boolean hasPermission(String permission) {
        PackageManager pm = App.getInstance().getPackageManager();
        return (PackageManager.PERMISSION_GRANTED == pm.checkPermission(permission, "com.kuaima.kuailai"));
    }

    /**
     * 判断是否拥有某种权限,推荐开新的线程进行，比较耗时
     * <p>
     * //     * @param permission 例如 android.permission.READ_CONTACTS
     */
//    public static boolean hasRealPermission(String permission) {
//        boolean hasPermission = false;
//        boolean permissionStrategy1 = false;
//        PackageManager pm = App.getInstance().getPackageManager();
//        permissionStrategy1 = (PackageManager.PERMISSION_GRANTED == pm.checkPermission(permission, "com.kuaima.kuailai"));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            boolean permissionStrategy2 = false;
//            permissionStrategy2 = ContextCompat.checkSelfPermission(App.getInstance(), permission) == PackageManager.PERMISSION_GRANTED;
//            hasPermission = permissionStrategy1 & permissionStrategy2;
//        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            hasPermission = permissionStrategy1 & PermissionUtils.isPermissionGranted(App.getInstance(), permission);
//        }
//        Log.d("hasRealPermission", "2 permission=" + permission + "/" + hasPermission);
//        return hasPermission;
//
//    }
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            View focusView = activity.getCurrentFocus();
            if (focusView != null) {
                imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
            }
        }
    }

    /**
     * 获取一个字符串的base64编码形式
     *
     * @param str
     * @return
     */
    public static String getBase64EncodeStr(String str) {
        byte[] encode = Base64.encode(str.getBytes(), Base64.DEFAULT);
        String enc = new String(encode);
        enc = enc.replace("\n", "");
        enc = enc.replace(" ", "");
        return enc;
    }

    /**
     * @param str
     * @return
     * @Description:
     */
    public static byte[] decodeHEX(String str) {
        if (str == null)
            return null;
        str = str.trim();
        int len = str.length();
        if (len == 0 || len % 2 == 1)
            return null;
        byte[] b = new byte[len / 2];
        try {
            for (int i = 0; i < str.length(); i += 2) {
                b[i / 2] = (byte) Integer.decode("0x" + str.substring(i, i + 2)).intValue();
            }
            return b;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加密<br>
     * 用公钥加密
     *
     * @param data
     * @param publickey
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publickey) throws Exception {
        // 对公钥解密
        byte[] keyBytes = decodeHEX(publickey);
        // 取得公钥
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        Key publicKey = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * 获取base64解码
     *
     * @param base64Str
     * @return
     */
    public static String getDecodeBase64Str(String base64Str) {

        try {
            byte[] baseByte = Base64.decode(base64Str, Base64.DEFAULT);
            return new String(baseByte);
        } catch (Exception e) {
            Logger.e("AndroidUtil", "还原base64str错误:", base64Str);
            e.printStackTrace();
            return base64Str;
        }
    }

    /**
     * @param b
     * @return
     * @Description:
     */
    public static String encodeHEX(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1)
                hs = hs + "0" + stmp;
            else
                hs = hs + stmp;
        }
        return hs;
    }

    public static String getLinuxKernalInfo() {
        Process process = null;
        String result = "";
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");

            // get the output line
            InputStream outs = process.getInputStream();
            InputStreamReader isrout = new InputStreamReader(outs);
            BufferedReader brout = new BufferedReader(isrout, 8 * 1024);
            String line;
            // get the whole standard output string
            while ((line = brout.readLine()) != null) {
                result += line;
                // result += "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     * 判断当前是否使用的是 WIFI网络
     */
    public static boolean isWifiActive() {
        ConnectivityManager connectivity = (ConnectivityManager) App.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info;
        if (connectivity != null) {
            info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取ip
     *
     * @return
     */
    public static String getIP() {
        if (isWifiActive()) return getWifiIp();
        return getLocalIpAddress();
    }

    /**
     * 获取wifi的ip地址
     *
     * @return
     */
    public static String getWifiIp() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    /**
     * 获取当前链接wifi的名称
     */
    public static String[] getConnectionWiFiInfo() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) App.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String[] result = new String[2];
        result[0] = wifiInfo.getSSID();
        result[1] = wifiInfo.getBSSID();
        return result;
    }

    //返回值 -1：没有网络  1：WIFI网络2：wap网络3：net网络
    public static int getNetype() {
        int netType = -1;
        ConnectivityManager connMgr = (ConnectivityManager) App.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            if (networkInfo.getExtraInfo().toLowerCase().equals("cmnet")) {
                netType = 3;
            } else {
                netType = 2;
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = 1;
        }
        return netType;
    }

    //返回值 -1：没有网络  1：WIFI网络2：wap网络3：net网络
    public static String getNetypeString() {
        int netType = getNetype();
        switch (netType) {
            case -1:
                return "none";
            case 1:
                return "WIFI";
            case 2:
                return "wap";
            case 3:
                return "net";
        }
        return "unknown";
    }

    public static boolean isApkDebugable(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    public static class NetInfoVo {
        public String mobileInfo;
        public String wifiInfo;
        public String hostInfo;
        public String portInfo;
    }

    public static NetInfoVo getNetInfo() {
        NetInfoVo netInfoVo = new NetInfoVo();
        ConnectivityManager connectivity = (ConnectivityManager) App.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info;
        if (connectivity != null) {
            info = connectivity.getAllNetworkInfo();
            for (int i = 0; i < info.length; i++) {
                if ("MOBILE".equals(info[i].getTypeName())) {
                    netInfoVo.mobileInfo = (info[i].toString());
                } else if ("WIFI".equals(info[i].getTypeName())) {
                    netInfoVo.wifiInfo = (info[i].toString());
                }
            }
        }
        String host = Proxy.getDefaultHost();
        int port = Proxy.getDefaultPort();
        netInfoVo.hostInfo = (host);
        netInfoVo.portInfo = (port + "");
        return netInfoVo;
    }

    /**
     * 判断手机上是否安装了此应用
     *
     * @param context 上下文
     * @param appName 应用的包名
     * @return
     */
    public static boolean isAppAvilible(Context context, String appName) {
        final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                if (pn.equals(appName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 启动某一个应用
     *
     * @param context        上下文
     * @param appName        应用的名字
     * @param appLaucherName 启动应用的启用页面的名字
     * @param ext            携带的数据
     */
    public static void startAppLauncherUI(Context context, String appName, String appLaucherName, Map<String, String> ext) {
        Intent intent = new Intent();
        ComponentName cmp = new ComponentName(appName, appLaucherName);
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (ext != null) {
            for (Map.Entry<String, String> entry : ext.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                    continue;
                }
                intent.putExtra(key, value);
            }
        }
        intent.setComponent(cmp);
        context.startActivity(intent);
    }


    /**
     * 获取手机厂商
     *
     * @return 手机厂商
     */
    public static String getDeviceBrand() {
        StringBuilder sb = new StringBuilder();
        sb.append(Build.BRAND != null ? Build.BRAND.replace(" ", "") : "unknown");
        return sb.toString();
    }

    public static String getAndroidId() {
        //return com.youkantou.adunion.utils.AndroidUtil.getAndroidId();
//        return "";
        return android.provider.Settings.System.getString(App.getInstance().getContentResolver(), Secure.ANDROID_ID);
    }

    private static String m2Cache = "";

    public static String getM2() {
        if (!TextUtils.isEmpty(m2Cache)) {
            return m2Cache;
        }

        String imei = getM2Imei();
        String androidId = android.provider.Settings.System.getString(App.getInstance().getContentResolver(), Secure.ANDROID_ID);
        String serialNo = getSerialNumber();
        String total = "" + imei + androidId + serialNo;
        String md5 = MD5Utils.Md5(total, total.length());
        if (!TextUtils.isEmpty(md5)) {
            m2Cache = md5;
        }
        return m2Cache;
    }

    private static String cacheImei = "";

    public static String getM2Imei() {
        if (!TextUtils.isEmpty(cacheImei)) {
            return cacheImei;
        }
        String imei = "";
        try {
            boolean quanxian = com.game.box.gamebox.novel.PermissionUtil.hasPermission(App.getInstance(), Manifest.permission.READ_PHONE_STATE);
            if (quanxian) {
                imei = ((TelephonyManager) App.getInstance().getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            }
        } catch (Exception e) {
        }
        if (!TextUtils.isEmpty(imei)) {
            cacheImei = imei;
        }
        return imei;
    }

    public static String getSerialNumber() {
        String serial = "";
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return serial;
    }

    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 获取蓝牙地址，需要打开蓝牙才能获取到
     *
     * @return
     */
    public static String getBlueToothAddress() {
        String address = "";
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (adapter != null) {
                address = adapter.getAddress();
            }
        } else {
            Field field = null;
            try {
                field = BluetoothAdapter.class.getDeclaredField("mService");
                field.setAccessible(true);
                Object bluetoothManagerService = field.get(adapter);
                if (bluetoothManagerService == null) {
                    return null;
                }
                Method method = bluetoothManagerService.getClass().getMethod("getAddress");
                if(method != null) {
                    Object obj = method.invoke(bluetoothManagerService);
                    if(obj != null) {
                        return obj.toString();
                    }
                }
            } catch (Exception e) {
            }
        }
        Logger.d(TAG, "getBlueToothAddress: " + address);
        if (address == null) {
            address = "";
        }
        return address;
    }

//    private static String getBlueToothAddressReflection() {
//        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        Object bluetoothManagerService = new Mirror().on(bluetoothAdapter).get().field("mService");
//        if (bluetoothManagerService == null) {
//            Log.w(TAG, "couldn't find bluetoothManagerService");
//            return null;
//        }
//        Object address = new Mirror().on(bluetoothManagerService).invoke().method("getAddress").withoutArgs();
//        if (address != null && address instanceof String) {
//            Log.w(TAG, "using reflection to get the BT MAC address: " + address);
//            return (String) address;
//        } else {
//            return null;
//        }
//    }

    /**
     *获取IMSI信息
     * @return
     */
    public static String getPhoneIMSI() {
        Context context = App.getInstance();
        String imsi = "";
        boolean quanxian = com.game.box.gamebox.novel.PermissionUtil.hasPermission(context, Manifest.permission.READ_PHONE_STATE);
        if (quanxian){
            TelephonyManager mTelephonyMgr = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (mTelephonyMgr != null) {
                imsi = mTelephonyMgr.getSubscriberId();
            }
        }

        if (imsi == null) {
            imsi = "";
        }
        Logger.d(TAG, "get getSubscriberId " + imsi);
        return imsi;
    }
    /**
     * 获取手机mac地址
     * @return
     */
    public static String getLocalMacAddress() {
        Context context = App.getInstance().getApplicationContext();
        String localMac = "02:00:00:00:00:00";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (wifi != null) {
                WifiInfo info = wifi.getConnectionInfo();
                if (info != null) {
                    localMac = info.getMacAddress();
                }
            }
        } else {
            try {
                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all) {

                    if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder sb = new StringBuilder();
                    for (byte b : macBytes) {
                        sb.append(String.format("%02X:", b));
                    }

                    if (sb.length() > 0) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    localMac =  sb.toString();
                    break;
                }
            } catch (Exception ex) {
            }
        }
        Logger.d(TAG, "getLocalMacAddress: " + localMac);
        return localMac;
    }
}
