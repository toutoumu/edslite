package com.library.widget;

import static com.library.widget.SmoothImageView.STATE_TRANSFORM_MOVE;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import timber.log.Timber;

/**
 * 拖拽可以缩放的布局
 */
public class DragFrameLayout extends FrameLayout {
    /**
     * onFling 操作的阈值,绝对值越大速度越大
     */
    private static final int VELOCITY_Y = 100;

    private View mContainer; // 容器
    private SmoothImageView mImageView; // 图片

    private float mStartX; // 按下位置
    private float mStartY; // 按下位置
    private float sx = 0f; // 按下是的位置(中心位置为0.5)
    private float sy = 0f; // 按下是的位置(中心位置为0.5)

    private boolean mHide; // 滑动结束后是否隐藏
    private boolean mIsTransform = false; // 是否正在移动

    private VelocityTracker mVelocityTracker; // 滑动速度监听

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        addMovement(event); // 速度监听
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                return actionDown(event, "onTouchEvent");
            }
            case MotionEvent.ACTION_MOVE: {
                return actionMove(event, "onTouchEvent");
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                return actionUp(event, "onTouchEvent");
            }
            default: {
                return actionCancelOrOther(event, "onTouchEvent");
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        addMovement(event);
        if (mIsTransform) {
            Timber.e("onInterceptTouchEvent -- 拦截事件 %s ", event.getAction());
            return true;
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                return actionDown(event, "onInterceptTouchEvent");
            }
            case MotionEvent.ACTION_MOVE: {
                return actionMove(event, "onInterceptTouchEvent");
            }
            case MotionEvent.ACTION_UP: {
                return actionUp(event, "onInterceptTouchEvent");
            }
            default: {
                return actionCancelOrOther(event, "onInterceptTouchEvent");
            }
        }
    }

    /**
     * 手指按下
     *
     * @param event      .
     * @param methodName 在哪个方法里面调用
     * @return .
     */
    private boolean actionDown(MotionEvent event, String methodName) {
        if (event.getPointerId(event.getActionIndex()) != 0) {
            Timber.e("%s -- ACTION_DOWN -- 手指按下 不处理", methodName);
            return false;
        }
        if (event.getPointerCount() != 1) {
            return false;
        }
        getParent().requestDisallowInterceptTouchEvent(false);
        mStartX = event.getX();
        mStartY = event.getY();
        sx = mStartX / getWidth();
        sy = mStartY / getHeight();
        Timber.e("%s -- ACTION_DOWN -- 手指按下 位置x: %s 位置y: %s 比例x: %s  比例y: %s", methodName, mStartX, mStartY, sx, sy);
        // 注意 onInterceptTouchEvent 是 return mIsTransform;
        // onTouchEvent 返回的是 if (mIsTransform || mHide) { return true; } return super.onTouchEvent(event);

        if (mIsTransform || mHide) {
            return true;
        }
        return false;
    }

    /**
     * 手指移动
     *
     * @param event      .
     * @param methodName 在哪个方法里面调用
     * @return .
     */
    private boolean actionMove(MotionEvent event, String methodName) {
        int index = event.findPointerIndex(0);
        if (index == -1) {
            Timber.e("%s -- ACTION_MOVE -- 手指移动 不处理", methodName);
            return false;
        }
        if (event.getPointerCount() != 1) {
            return false;
        }
        // Timber.e("%s -- ACTION_MOVE -- PointIndex: %s", methodName, index);
        // 计算偏移量
        float transformX = event.getX(index) - mStartX;
        float transformY = event.getY(index) - mStartY;
        if (!mIsTransform && Math.abs(transformY) > Math.abs(transformX)) {
            mIsTransform = true;
            // 开始移动
            if (mImageView.getBeforeTransformListener() != null) {
                mImageView.getBeforeTransformListener().onTransformComplete(STATE_TRANSFORM_MOVE);
            }
            mImageView.setVisibility(VISIBLE);
            mContainer.setVisibility(INVISIBLE);
            getParent().requestDisallowInterceptTouchEvent(true);
            Timber.e("%s -- ACTION_MOVE -- 触发滚动事件 %s %s", methodName, transformX, transformY);
        }

        if (!mIsTransform) {
            getParent().requestDisallowInterceptTouchEvent(false);
            Timber.e("%s -- ACTION_MOVE -- 没有触发滚动事件 %s %s", methodName, transformX, transformY);
            return false;
        }

        float scale = 1.0f - Math.abs(transformY) / getWidth();
        int alpha = (int) (255.0f * (1.0f - Math.abs(transformY) / (getWidth() / 4.0f)));
        // [0, 255]
        alpha = Math.max(alpha, 0);
        alpha = Math.min(alpha, 255);

        // [0.7, 1]
        scale = Math.max(scale, 0.7f);
        scale = Math.min(scale, 1.0f);

        // mImageView.setVisibility(VISIBLE);
        // mContainer.setVisibility(INVISIBLE);
        mImageView.transformMove(scale, alpha, transformX, transformY, sx, sy);
        /*Timber.e("%s ACTION_MOVE -- 手指移动: %s 透明度: %s x偏移: %s y偏移: %s",
                 methodName,
                 scale,
                 alpha,
                 transformX,
                 transformY);*/
        return true;
    }

    /**
     * 手指抬起
     *
     * @param event      .
     * @param methodName 在哪个方法里面调用
     * @return .
     */
    private boolean actionUp(MotionEvent event, String methodName) {
        // 如果不是第一个手指抬起不处理
        if (event.getPointerId(event.getActionIndex()) != 0) {
            Timber.e("%s -- ACTION_MOVE -- 手指抬起 不处理", methodName);
            return true;
        }

        getParent().requestDisallowInterceptTouchEvent(false);
        if (mIsTransform) {
            float transformY = event.getY() - mStartY;
            if (Math.abs(transformY) > getWidth() / 3.0) {
                mHide = true;
                Timber.e("onTouchEvent -- ACTION_UP -- Y方向偏移 %s", transformY);
            } else {
                // 计算1000毫秒内移动速度
                mVelocityTracker.computeCurrentVelocity(1000);
                float yVelocity = mVelocityTracker.getYVelocity();
                // 如果向上滑动速度超过阈值, 或者偏移超过阈值
                mHide = Math.abs(yVelocity) > VELOCITY_Y;
                Timber.e("onTouchEvent -- ACTION_UP -- 滑动速度 %s", yVelocity);

                // 根据滑动速度计算动画时间,速度越快时间越短
                /*if (mHide) {
                    int duration = (int) Math.abs((300 * 1000 / yVelocity));
                    duration = Math.min(duration, 300);
                    duration = Math.max(duration, 200);
                    mImageView.setDuration(duration);
                }*/
            }
        }
        recycleVelocityTracker();
        if (mHide) {// 退出
            mHide = false;
            mIsTransform = false;
            mImageView.transformOut();
            Timber.e("%s -- ACTION_UP -- 退出图片浏览器", methodName);
            return true;
        }
        if (mIsTransform) { // 恢复
            mHide = false;
            mIsTransform = false;
            mImageView.transformRestore();
            Timber.e("%s -- ACTION_UP -- 恢复到图片浏览器", methodName);
            return true;
        }
        Timber.e("%s -- ACTION_UP -- 未处理的事件", methodName);
        return true;
    }

    /**
     * 触摸取消,或者其他
     *
     * @param event      .
     * @param methodName 在哪个方法里面调用
     * @return .
     */
    private boolean actionCancelOrOther(MotionEvent event, String methodName) {
        mHide = false;
        mIsTransform = false;
        recycleVelocityTracker();
        // false: 允许父容器拦截事件
        getParent().requestDisallowInterceptTouchEvent(false);
        Timber.e("%s ACTION_CANCEL -- ", methodName);
        if (event.getActionMasked() != MotionEvent.ACTION_CANCEL) {
            Timber.e("%s -- Action: %s", methodName, event.getAction());
        }
        return false;
    }

    /**
     * 添加速度监听
     *
     * @param ev .
     */
    private void addMovement(MotionEvent ev) {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
    }

    /**
     * 移除速度监听
     */
    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    /**
     * 过渡动画使用的View
     *
     * @return {@link SmoothImageView}
     */
    public SmoothImageView getImageView() {
        return mImageView;
    }

    /**
     * 过渡动画使用的View
     *
     * @param imageView {@link SmoothImageView}
     */
    public void setImageView(SmoothImageView imageView) {
        mImageView = imageView;
    }

    /**
     * 图片浏览器
     *
     * @param container 一般指 ViewPage
     */
    public void setContainer(View container) {
        mContainer = container;
    }

    /**
     * 图片浏览器 一般指 ViewPage
     */
    public View getContainer() {
        return mContainer;
    }

    public DragFrameLayout(@NonNull Context context) {
        super(context);
    }

    public DragFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DragFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
