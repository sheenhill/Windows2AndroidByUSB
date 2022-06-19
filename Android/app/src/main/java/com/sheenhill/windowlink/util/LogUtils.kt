package com.sheenhill.windowlink.util

import android.util.Log

private const val debug = true // TODO:正式上线为false

private const val TAG = "松鼠鳜鱼"

fun logI(msg: String) {
    if (debug) {
        Log.i(TAG, msg)
    }
}

fun logE(msg: String) {
    if (debug) {
        Log.e(TAG, msg)
    }
}