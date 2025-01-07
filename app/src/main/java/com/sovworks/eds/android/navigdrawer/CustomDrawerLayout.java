package com.sovworks.eds.android.navigdrawer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import java.lang.reflect.Field;

import timber.log.Timber;

public class CustomDrawerLayout extends DrawerLayout {
    private float mInitialX;
    private float mInitialY;
    private int mTouchSlop;

    public CustomDrawerLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public CustomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CustomDrawerLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        ViewConfiguration config = ViewConfiguration.get(getContext());
        mTouchSlop = config.getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mInitialX = ev.getX();
                mInitialY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(ev.getX() - mInitialX);
                float dy = Math.abs(ev.getY() - mInitialY);
                if (dx > mTouchSlop && dx > dy) {
                    Timber.e("捕获触摸事件");
                    return true; // 捕获触摸事件
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
    /*private float mLastMotionX;
    private float mLastMotionY;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 其实就是吧原来的实现放到一个新的方法里，然后添加自己的逻辑。
        try {
            final float x = ev.getX();
            final float y = ev.getY();

            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mLastMotionX = x;
                    mLastMotionY = y;
                    break;

                case MotionEvent.ACTION_MOVE:
                    // 这里的判断拦截的逻辑是滑动的角度小于等于30°就是横向滑动，肯定拦截
                    // 否者使用原来的逻辑，调用interceptTouchEvent
                    // 这样写主要是有垂直滚动的RecyclewrView
                    // 具体怎么处理，看自己具体需求
                    float xDiff = Math.abs(x - mLastMotionX);
                    float yDiff = Math.abs(y - mLastMotionY);
                    return xDiff > 0 && xDiff >= yDiff * Math.sqrt(3);
            }
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            return false;
        }
    }*/

    public static void setCustomLeftEdgeSize(@NonNull DrawerLayout drawerLayout, float displayWidthPercentage) {
        try {
            // find ViewDragHelper and set it accessible
            Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            if (leftDraggerField == null) {
                return;
            }
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
            // find edgesize and set is accessible
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            // set new edgesize
            int widthPixels = DisplayUtils.getWindowWidth(drawerLayout.getContext());
            edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (widthPixels * displayWidthPercentage)));
        } catch (Exception e) {
            Timber.e(e);
        }
    }
}

class DisplayUtils {
    public static int getWindowWidth(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}