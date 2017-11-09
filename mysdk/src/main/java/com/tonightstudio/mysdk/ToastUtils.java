package com.tonightstudio.mysdk;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by liyiwei
 * on 2017/10/20.
 */

public class ToastUtils {
    public static void showToastWithShort(Context context, String msg) {
        if (context == null) {
            throw new NullPointerException("");
        }
        Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
