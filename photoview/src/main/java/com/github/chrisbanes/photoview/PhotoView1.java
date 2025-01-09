/*
 Copyright 2011, 2012 Chris Banes.
 <p>
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 <p>
 http://www.apache.org/licenses/LICENSE-2.0
 <p>
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.github.chrisbanes.photoview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.GestureDetector;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

/**
 * A zoomable ImageView. See {@link PhotoViewAttacher} for most of the details on how the zooming
 * is accomplished
 */
@SuppressWarnings("unused")
public class PhotoView1 extends AppCompatImageView implements ValueAnimator.AnimatorUpdateListener {
    private Paint mProgressPaint;
    private int startAngle = 0;

    private PhotoViewAttacher attacher;
    private ScaleType pendingScaleType;

    public PhotoView1(Context context) {
        this(context, null);
    }

    public PhotoView1(Context context, AttributeSet attr) {
        this(context, attr, 0);
    }

    public PhotoView1(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        init();
    }

    private void init() {
        attacher = new PhotoViewAttacher(this);
        // We always pose as a Matrix scale type, though we can change to another scale type
        // via the attacher
        super.setScaleType(ScaleType.MATRIX);
        // apply the previously applied scale type
        if (pendingScaleType != null) {
            setScaleType(pendingScaleType);
            pendingScaleType = null;
        }

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

    /**
     * Get the current {@link PhotoViewAttacher} for this view. Be wary of holding on to references
     * to this attacher, as it has a reference to this view, which, if a reference is held in the
     * wrong place, can cause memory leaks.
     *
     * @return the attacher.
     */
    public PhotoViewAttacher getAttacher() {
        return attacher;
    }

    @Override
    public ScaleType getScaleType() {
        return attacher.getScaleType();
    }

    @Override
    public Matrix getImageMatrix() {
        return attacher.getImageMatrix();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        attacher.setOnLongClickListener(l);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        attacher.setOnClickListener(l);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (attacher == null) {
            pendingScaleType = scaleType;
        } else {
            attacher.setScaleType(scaleType);
        }
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        // setImageBitmap calls through to this method
        if (attacher != null) {
            attacher.update();
        }
    }

    @Override
    public void setImageResource(int resId) {
        super.setImageResource(resId);
        if (attacher != null) {
            attacher.update();
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        if (attacher != null) {
            attacher.update();
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        boolean changed = super.setFrame(l, t, r, b);
        if (changed) {
            attacher.update();
        }
        return changed;
    }

    public void setRotationTo(float rotationDegree) {
        attacher.setRotationTo(rotationDegree);
    }

    public void setRotationBy(float rotationDegree) {
        attacher.setRotationBy(rotationDegree);
    }

    public boolean isZoomable() {
        return attacher.isZoomable();
    }

    public void setZoomable(boolean zoomable) {
        attacher.setZoomable(zoomable);
    }

    public RectF getDisplayRect() {
        return attacher.getDisplayRect();
    }

    public void getDisplayMatrix(Matrix matrix) {
        attacher.getDisplayMatrix(matrix);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return attacher.setDisplayMatrix(finalRectangle);
    }

    public void getSuppMatrix(Matrix matrix) {
        attacher.getSuppMatrix(matrix);
    }

    public boolean setSuppMatrix(Matrix matrix) {
        return attacher.setDisplayMatrix(matrix);
    }

    public float getMinimumScale() {
        return attacher.getMinimumScale();
    }

    public float getMediumScale() {
        return attacher.getMediumScale();
    }

    public float getMaximumScale() {
        return attacher.getMaximumScale();
    }

    public float getScale() {
        return attacher.getScale();
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        attacher.setAllowParentInterceptOnEdge(allow);
    }

    public void setMinimumScale(float minimumScale) {
        attacher.setMinimumScale(minimumScale);
    }

    public void setMediumScale(float mediumScale) {
        attacher.setMediumScale(mediumScale);
    }

    public void setMaximumScale(float maximumScale) {
        attacher.setMaximumScale(maximumScale);
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        attacher.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        attacher.setOnMatrixChangeListener(listener);
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        attacher.setOnPhotoTapListener(listener);
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener listener) {
        attacher.setOnOutsidePhotoTapListener(listener);
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        attacher.setOnViewTapListener(listener);
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        attacher.setOnViewDragListener(listener);
    }

    public void setScale(float scale) {
        attacher.setScale(scale);
    }

    public void setScale(float scale, boolean animate) {
        attacher.setScale(scale, animate);
    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        attacher.setScale(scale, focalX, focalY, animate);
    }

    public void setZoomTransitionDuration(int milliseconds) {
        attacher.setZoomTransitionDuration(milliseconds);
    }

    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
        attacher.setOnDoubleTapListener(onDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangedListener) {
        attacher.setOnScaleChangeListener(onScaleChangedListener);
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        attacher.setOnSingleFlingListener(onSingleFlingListener);
    }

}
