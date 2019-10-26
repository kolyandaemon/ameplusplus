package com.a.ameplus;

import android.content.*;
import android.net.wifi.*;


import java.lang.reflect.*;

public class HotSpotManager {

    public  WifiConfiguration config;
    public  String hotName;
    public  String hotPass;
    private Context context;
    WifiManager hotManage;

    public HotSpotManager(Context context)
    {
            this.context=context;
            this.hotManage=hotManage = (WifiManager) context.getSystemService(context.WIFI_SERVICE);;

        try {
            Method method = hotManage.getClass().getMethod("getWifiApConfiguration");
            config=(WifiConfiguration) method.invoke(hotManage);
            hotName=config.SSID;
            hotPass=config.preSharedKey;
        } catch (Exception ignore) {


        }
    }


    public  boolean stateWifi() {

        try {
            Method wifiMethod = hotManage.getClass().getDeclaredMethod("isWifiApEnabled");
            wifiMethod.setAccessible(true);
            return (Boolean) wifiMethod.invoke(hotManage);
        }
        catch (Throwable ignored) {}
        return false;
    }


    public  boolean configStateWifi() {

        WifiConfiguration wifiConfig =null;
        try {

            if(stateWifi()) {
                hotManage.setWifiEnabled(false);
            }
            Method wifiMethod = hotManage.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            wifiMethod.invoke(hotManage, wifiConfig, !stateWifi());
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
