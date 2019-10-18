
package com.game.box.gamebox.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;


import com.game.box.gamebox.App;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 网络状态检测工具
 *
 * @date: 2014年8月14日 下午3:34:41
 */
public final class NetworkDetection {
    private static String TAG = "NetworkDetection";

    private NetworkDetection() {
    }

    /**
     * 获取网络信息
     *
     * @param context context
     * @return 网络信息
     */
    private static NetworkInfo getNetworkInfo(Context context) {
        // 获取网络状态管理
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取网络信息
        //NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return connectivityManager.getActiveNetworkInfo();
    }

    /**
     * 通过类型获取网络信息
     *
     * @param context context
     * @param type    网络类型 所有的type常量在ConnectivityManager中
     * @return 网络信息
     */
    private static NetworkInfo getNetworkInfo(Context context, int type) {
        // 获取网络状态管理
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取网络信息
        //NetworkInfo networkInfo = connectivityManager.getNetworkInfo(type);

        return connectivityManager.getNetworkInfo(type);
    }

    /**
     * 获取网络是否可用
     *
     * @param context context
     * @return 网络是否可用
     */
    public static Boolean getIsNetworkConnected(Context context) {
        if (context == null) {
            return false;
        }

        NetworkInfo networkInfo = getNetworkInfo(context);

//        if (networkInfo != null) {
//            return networkInfo.isConnected();
//        } else {
//            return false;
//        }

        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * 获取Wifi网络状态
     *
     * @param context context
     * @return 网络是否可用
     * @author 段屈直
     */
    public static Boolean getIsWifiConnected(Context context) {
        if (context == null) {
            return false;
        }

        NetworkInfo networkInfo = getNetworkInfo(context,
                ConnectivityManager.TYPE_WIFI);
//        if (networkInfo != null) {
//            return networkInfo.isConnected();
//        } else {
//            return false;
//        }

        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * 获取移动网络状态
     *
     * @param context context
     * @return 网络是否可用
     * @author 段屈直
     */
    public static Boolean getIsMobileConnected(Context context) {
        if (context == null) {
            return false;
        }

        NetworkInfo networkInfo = getNetworkInfo(context,
                ConnectivityManager.TYPE_MOBILE);
//        if (networkInfo != null) {
//            return networkInfo.isConnected();
//        } else {
//            return false;
//        }

        return networkInfo != null && networkInfo.isConnected();
    }

    /**
     * 获取网络是否链接
     * <p>
     * by liyaxing
     *
     * @return
     */
    public static Boolean getIsConnected() {
        return getIsMobileConnected(App.getInstance()) || getIsWifiConnected(App.getInstance());
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    public static String getMACAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions

        return "";
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    public static String getDeviceMAC() {
        return "[wlan0:" + getMACAddress("wlan0")
                + "] [eth0:" + getMACAddress("eth0") + "]";
    }

    public static String getDeviceIp() {
        return "[ipv4:" + getIPAddress(true)
                + "] [ipv6:" + getIPAddress(false) + "]";
    }

    public static String getNetWorkInfo(Context context) {
        if (context != null) {
            if (getIsWifiConnected(context)) {
                WifiManager wifii = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                DhcpInfo d = wifii.getDhcpInfo();

                StringBuilder netInfo = new StringBuilder();
                netInfo.append("wifi [DNS_1:").append(String.valueOf(intToIp(d.dns1)));
                netInfo.append(" DNS_2:").append(String.valueOf(intToIp(d.dns2)));
                netInfo.append(" Default_Gateway:").append(String.valueOf(intToIp(d.gateway)));
                netInfo.append(" IP_Address:").append(String.valueOf(intToIp(d.ipAddress)));
                netInfo.append(" Lease_Time:").append(String.valueOf(d.leaseDuration));
                netInfo.append(" Subnet_Mask:").append(String.valueOf(intToIp(d.netmask)));
                netInfo.append(" Server_IP:").append(String.valueOf(intToIp(d.serverAddress)));
                netInfo.append("] ");

                return netInfo.toString();
            } else if (getIsMobileConnected(context)) {
                ArrayList<String> servers = new ArrayList<>();
                try {
                    Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
                    Method method = SystemProperties.getMethod("get", new Class[]{String.class});

                    for (String name : new String[]{"net.dns1", "net.dns2", "net.dns3", "net.dns4",}) {
                        String value = (String) method.invoke(null, name);
                        if (value != null && !"".equals(value) && !servers.contains(value))
                            servers.add(value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return "sim [IP_Address:" + getDeviceIp() + " dns:" + servers.toString() + "] ";
            } else {
                return "net_disconnect";
            }
        } else {
            return "";
        }
    }

    public static String intToIp(int addr) {
        return ((addr & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF) + "." +
                ((addr >>>= 8) & 0xFF));
    }

    //没有网络连接
    public static final int NETWORN_NONE = 0;
    //wifi连接
    public static final int NETWORN_WIFI = 1;
    //手机网络数据连接类型
    public static final int NETWORN_2G = 2;
    public static final int NETWORN_3G = 3;
    public static final int NETWORN_4G = 4;
    public static final int NETWORN_MOBILE = 5;

    public static String getNetworkType(Context context) {
        String type = "NONE";
        int state = getNetworkState(context);
        switch (state) {
            case NETWORN_WIFI:
                type = "WIFI";
                break;
            case NETWORN_2G:
                type = "2G";
                break;
            case NETWORN_3G:
                type = "3G";
                break;
            case NETWORN_4G:
                type = "4G";
                break;

        }
        return type;
    }

    /**
     * 获取当前网络连接类型
     *
     * @param context
     * @return
     */
    public static int getNetworkState(Context context) {
        //获取系统的网络服务
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        //如果当前没有网络
        if (null == connManager)
            return NETWORN_NONE;

        //获取当前网络类型，如果为空，返回无网络
        NetworkInfo activeNetInfo = connManager.getActiveNetworkInfo();
        if (activeNetInfo == null || !activeNetInfo.isAvailable()) {
            return NETWORN_NONE;
        }

        // 判断是不是连接的是不是wifi
        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (null != wifiInfo) {
            NetworkInfo.State state = wifiInfo.getState();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    return NETWORN_WIFI;
                }
        }

        // 如果不是wifi，则判断当前连接的是运营商的哪种网络2g、3g、4g等
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (null != networkInfo) {
            NetworkInfo.State state = networkInfo.getState();
            String strSubTypeName = networkInfo.getSubtypeName();
            if (null != state)
                if (state == NetworkInfo.State.CONNECTED || state == NetworkInfo.State.CONNECTING) {
                    switch (activeNetInfo.getSubtype()) {
                        //如果是2g类型
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            return NETWORN_2G;
                        //如果是3g类型
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            return NETWORN_3G;
                        //如果是4g类型
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            return NETWORN_4G;
                        default:
                            //中国移动 联通 电信 三种3G制式
                            if (strSubTypeName.equalsIgnoreCase("TD-SCDMA") || strSubTypeName.equalsIgnoreCase("WCDMA") || strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                return NETWORN_3G;
                            } else {
                                return NETWORN_MOBILE;
                            }
                    }
                }
        }
        return NETWORN_NONE;
    }

    public static String getProvidersName(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String providersName = "";
        String imsi; // 返回唯一的用户ID;就是这张卡的编号神马的
        try {
            imsi = telephonyManager.getSubscriberId();
            if (imsi == null)
                return "unknow";
            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。其中
            if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
                providersName = "中国移动";
            } else if (imsi.startsWith("46001")) {
                providersName = "中国联通";
            } else if (imsi.startsWith("46003")) {
                providersName = "中国电信";
            }
        } catch (Exception e) {
        }
        return providersName;
    }
}
