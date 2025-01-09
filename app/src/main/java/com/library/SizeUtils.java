package com.library;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.Window;
import android.view.WindowInsets;

public class SizeUtils {
    public static int dpToPx(Context context, float dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * 获取状态栏高度
     *
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity) {
        int statusBarHeight = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            if (window != null) {
                WindowInsets insets = window.getDecorView().getRootWindowInsets();
                if (insets != null) {
                    statusBarHeight = insets.getSystemWindowInsetTop();
                }
            }
        }
        return statusBarHeight;
    }

}
