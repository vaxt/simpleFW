package com.base.simfw.utils;

import android.util.Log;

import com.base.simfw.config.Constant;


/**
 * Created by zhihua on 16/2/18.
 */
public class LogUtils {

    private static boolean isDebug = Constant.DEBUG;


    // 下面是传入自定义tag的函数
    public static void i(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (isDebug)
            Log.e(tag, msg);
    }

    public static void e(String tag, String msg, Throwable e) {
        if (msg == null) {
            msg = "";
        }
        if (isDebug)
            Log.e(tag, msg, e);
    }

    public static void v(String tag, String msg) {
        if (msg == null) {
            msg = "";
        }
        if (isDebug)
            Log.i(tag, msg);
    }


}
