package com.base.simfw.network;


/**
 * Created by zdmy on 2017/11/8.
 */

public class HostManager {

    public enum HostType {
        TYPE_LOGIN,
        TYPE_DOWNLOAD
    }

    private static final String HOST_URL = "http://192.168.1.115";

    public static String getHost(HostType type) {
        String host;
        switch (type) {
            case TYPE_LOGIN:
            case TYPE_DOWNLOAD:
                host = HOST_URL;
                break;
            default:
                host = "";
                break;
        }
        return host;
    }
}
