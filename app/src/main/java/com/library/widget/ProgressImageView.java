package com.library.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ProgressImageView extends androidx.appcompat.widget.AppCompatImageView implements ValueAnimator.AnimatorUpdateListener {

    private Paint mProgressPaint;
    private int startAngle = 0;

    public ProgressImageView(@NonNull Context context) {
        super(context);
        this.init();
    }

    public ProgressImageView(@NonNull Context context,
                             @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public ProgressImageView(@NonNull Context context,
                             @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    private void init() {
        mProgressPaint = new Paint();
        mProgressPaint.setColor(Color.DKGRAY);
        mProgressPaint.setStyle(Paint.Style.STROKE);

        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setAntiAlias(true);
        mPaint.setColor(0xffaaaaaa);

        mValueAnimator = ValueAnimator.ofInt(30, 3600);
        mValueAnimator.setDuration(10000);
        mValueAnimator.setInterpolator(null);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLoading(canvas);
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
        if (isLoading) {
            start();
        } else {
            stop();
        }
        invalidate();
    }

    private boolean isLoading = false;

    protected int mWidth = 0;
    protected int mHeight = 0;
    protected Path mPath = new Path();
    protected int mProgressDegree = 0;
    protected ValueAnimator mValueAnimator;
    protected Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 绘制加载进度条
     *
     * @param canvas
     */
    private void drawLoading(Canvas canvas) {
        if (!isLoading) {
            return;
        }

        /* float size = 100;// Math.min(rect.width, rect.height);

        float left = (getMeasuredWidth() - size) / 2.0f;
        float top = (getMeasuredHeight() - size) / 2.0f;
        float right = left + size;
        float bottom = top + size;

        RectF oval = new RectF(left, top, right, bottom);

        // 为了绘制出透明度不同的圆环分两部来绘制：

        this.mProgressPaint.setARGB(200, 127, 255, 212);
        float ringWidth = 50.0f;
        this.mProgressPaint.setStrokeWidth(ringWidth);
        // 绘制不透明部分
        canvas.drawArc(oval, startAngle + 180, 90, false, mProgressPaint);
        canvas.drawArc(oval, startAngle, 90, false, mProgressPaint);
        //绘制透明部分
        this.mProgressPaint.setARGB(30, 127, 255, 212);
        canvas.drawArc(oval, startAngle + 90, 90, false, mProgressPaint);
        canvas.drawArc(oval, startAngle + 270, 90, false, mProgressPaint);

        // 上面的代码当startAngle = 0 时，绘制的是一个静态的透明度交替的圆弧。接着要让它转起来。增加代码：
        startAngle += 10;
        if (startAngle == 180) { startAngle = 0; } */

        final int width = 100;
        final int height = 100;
        float size = 100;// Math.min(rect.width, rect.height);

        float left = (getMeasuredWidth() - size) / 2.0f;
        float top = (getMeasuredHeight() - size) / 2.0f;
        float right = left + size;
        float bottom = top + size;

        final float r = Math.max(1f, width / 22f);

        if (mWidth != width || mHeight != height) {
            mPath.reset();
            mPath.addCircle(width - r, height / 2f, r, Path.Direction.CW);
            mPath.addRect(width - 5 * r, height / 2f - r, width - r, height / 2f + r, Path.Direction.CW);
            mPath.addCircle(width - 5 * r, height / 2f, r, Path.Direction.CW);
            mWidth = width;
            mHeight = height;
        }

        canvas.save();
        canvas.translate(left, top);
        canvas.rotate(mProgressDegree, (width) / 2f, (height) / 2f);
        for (int i = 0; i < 12; i++) {
            mPaint.setAlpha((i + 5) * 0x11);
            canvas.rotate(30, (width) / 2f, (height) / 2f);
            canvas.drawPath(mPath, mPaint);
        }
        canvas.translate(-left, -top);
        canvas.restore();
        // 动起来
        invalidate();
    }

    @Override
    public void onAnimationUpdate(@NonNull ValueAnimator animation) {
        int value = (int) animation.getAnimatedValue();
        mProgressDegree = 30 * (value / 30);
    }

    public void start() {
        if (!mValueAnimator.isRunning()) {
            mValueAnimator.addUpdateListener(this);
            mValueAnimator.start();
        }
    }

    public void stop() {
        if (mValueAnimator.isRunning()) {
            Animator animator = mValueAnimator;
            animator.removeAllListeners();
            mValueAnimator.removeAllUpdateListeners();
            mValueAnimator.cancel();
        }
    }

    public boolean isRunning() {
        return mValueAnimator.isRunning();
    }
}
