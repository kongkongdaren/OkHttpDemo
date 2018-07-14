package com.wen.asyl.okhttpdemo;

import android.util.Log;

/**
 * Description：xx <br/>
 * Copyright (c) 2018<br/>
 * This program is protected by copyright laws <br/>
 * Date:2018-07-12 15:53
 *
 * @author 姜文莒
 * @version : 1.0
 */
public class L {
    private static final  String TAG="okhttp";
    private static boolean debug=true;
    public static void e(String msg){
        if (debug){
            Log.e(TAG,msg);
        }

    }
}
