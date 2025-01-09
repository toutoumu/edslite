package com.library.widget;

import android.animation.Animator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.Size;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.why94.glide.drawable.GlidePlaceholderDrawable;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

/**
 * https://blog.csdn.net/lonelyroamer/article/details/25497737
 * <p>
 * 2d平滑变化的显示图片的ImageView
 * 仅限于用于:从一个ScaleType==CENTER_CROP的ImageView，切换到另一个ScaleType=FIT_CENTER的ImageView，或者反之 (当然，得使用同样的图片最好)
 */
public class SmoothImageView extends androidx.appcompat.widget.AppCompatImageView implements ValueAnimator.AnimatorUpdateListener {
    private static final int ANIMATION_DURATION = 300;// 动画时长

    // 状态
    public static final int STATE_NORMAL = 0; // 正常
    public static final int STATE_TRANSFORM_IN = 1; // 进入
    public static final int STATE_TRANSFORM_OUT = 2;// 退出
    public static final int STATE_TRANSFORM_MOVE = 3;// 拖动
    public static final int STATE_TRANSFORM_RESTORE = 4;// 恢复

    private int mState = STATE_NORMAL; // 当前状态
    private boolean mTransformStart = false; // 是否正在变幻

    // 缩略图控件的 frame 属性
    private int mOriginalWidth; // 缩略图View宽度
    private int mOriginalHeight; // 缩略图View高度
    private int mOriginalLocationX; // 缩略图偏移X
    private int mOriginalLocationY; // 缩略图偏移Y
    private int mBitmapWidth; // 图片的宽度(如果图片比例不是1:1那么与mOriginalWidth可能不一样)
    private int mBitmapHeight; // 图片的高度(如果图片比例不是1:1那么与mOriginalHeight可能不一样)

    private Paint mPaint; // 画笔
    private Paint mProgressPaint; // 画笔
    private Matrix mSmoothMatrix;
    private Transform mTransformData; // 变幻数据
    private TransformListener mTransformListener;
    private TransformListener mBeforeTransformListener;

    private int alpha = 0; // 背景透明度 0 透明 255 不透明

    private int duration = ANIMATION_DURATION;

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public SmoothImageView(Context context) {
        super(context);
        init();
    }

    public SmoothImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmoothImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Style.FILL);
        mSmoothMatrix = new Matrix();

        mProgressPaint = new Paint();
        mProgressPaint.setColor(Color.DKGRAY);
        mProgressPaint.setStyle(Style.STROKE);

        mLoadingPaint.setStyle(Style.FILL);
        mLoadingPaint.setAntiAlias(true);
        mLoadingPaint.setColor(0xffaaaaaa);

        mValueAnimator = ValueAnimator.ofInt(30, 3600);
        mValueAnimator.setDuration(10000);
        mValueAnimator.setInterpolator(null);
        mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mValueAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    /**
     * int[] location = new int[2];
     * view.getLocationOnScreen(location);
     * imageView.setOriginTransform(view.getWidth(), view.getHeight(), location[0], location[1]);
     * <p>
     * 注意: 必须在 setOriginalInfo 之前设置图片
     *
     * @param width     缩略图图片宽度
     * @param height    缩略图高度
     * @param locationX 屏幕上的位置x
     * @param locationY 屏幕上的位置y
     * @param offsetX   x方向偏移量
     * @param offsetY   y方向的偏移量(一般指的是状态栏+ 标题栏)
     */
    public void setOriginalInfo(int width, int height, int locationX, int locationY, int offsetX, int offsetY) {
        // 重置一些数据
        mBitmapWidth = 0;
        mBitmapHeight = 0;
        mTransformData = null;
        mSmoothMatrix = new Matrix();

        mOriginalWidth = width;
        mOriginalHeight = height;
        // 因为是屏幕坐标，所以要转换为该视图内的坐标，因为我所用的该视图是MATCH_PARENT，所以不用定位该视图的位置,如果不是的话，还需要定位视图的位置，然后计算mOriginalLocationX和mOriginalLocationY
        mOriginalLocationX = locationX - offsetX;
        mOriginalLocationY = locationY - offsetY;

        initTransform();
    }

    /**
     * 用于开始进入的方法。 调用此方前，需已经调用过setOriginalInfo
     */
    public void transformIn() {
        if (mTransformData == null) {
            throw new NullPointerException("请先调用 setOriginalInfo 进行初始化");
        }
        mState = STATE_TRANSFORM_IN;
        if (mBeforeTransformListener != null) {
            mBeforeTransformListener.onTransformComplete(mState);
        }
        mTransformStart = true;
        invalidate();
    }

    /**
     * 用于开始退出的方法。 调用此方前，需已经调用过setOriginalInfo
     */
    public void transformOut() {
        if (mTransformData == null) {
            throw new NullPointerException("请先调用 setOriginalInfo 进行初始化");
        }
        mState = STATE_TRANSFORM_OUT;
        if (mBeforeTransformListener != null) {
            mBeforeTransformListener.onTransformComplete(mState);
        }
        mTransformStart = true;
        invalidate();
    }

    /**
     * 移动
     *
     * @param scale      缩放
     * @param alpha      透明度 0 透明 255 不透明
     * @param transformX x方向偏移
     * @param transformY y方向偏移
     * @param sx         手指点击位置在视图中的位置(中心点 0.5)
     * @param sy         手指点击位置在视图中的位置(中心点 0.5)
     */
    public void transformMove(float scale, int alpha, float transformX, float transformY, float sx, float sy) {
        if (mTransformData == null) {
            throw new NullPointerException("请先调用 setOriginalInfo 进行初始化");
        }
        mState = STATE_TRANSFORM_MOVE; // 更新状态
        mTransformStart = false; // 不执行动画
        this.alpha = alpha; // 更新透明度

        // 计算并缓存此次拖动的,缩放,偏移,尺寸等信息
        float currentScale = scale * mTransformData.endScale; // 图片的实际缩放值(小图会被拉伸到填充屏幕)
        float targetWidth = mBitmapWidth * currentScale;// 本次移动图片最终的宽度
        float targetHeight = mBitmapHeight * currentScale;// 本次移动图片最终的宽度

        mTransformData.scale = currentScale;
        mTransformData.rect.width = targetWidth;
        mTransformData.rect.height = targetHeight;
        mTransformData.rect.left =
                transformX + (getWidth() - targetWidth) / 2.0f + (mTransformData.endRect.width - targetWidth) * (sx - 0.5f);
        mTransformData.rect.top =
                transformY + (getHeight() - targetHeight) / 2.0f + (mTransformData.endRect.height - targetHeight) * (sy - 0.5f);

        invalidate();
    }

    /**
     * 移动后还原
     */
    public void transformRestore() {
        if (mTransformData == null) {
            throw new NullPointerException("请先调用 setOriginalInfo 进行初始化");
        }
        mState = STATE_TRANSFORM_RESTORE;
        if (mBeforeTransformListener != null) {
            mBeforeTransformListener.onTransformComplete(mState);
        }
        mTransformStart = true;
        startTransform(STATE_TRANSFORM_RESTORE);
    }

    /**
     * 初始化动画所需的信息,进入时的 位置尺寸, 放大后的尺寸
     */
    private void initTransform() {
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }

        if (mBitmapWidth == 0 || mBitmapHeight == 0) {
            Drawable drawable = getDrawable();
            if (drawable == null) {
                mBitmapWidth = 200;
                mBitmapHeight = 200;
            } else if (drawable instanceof TransitionDrawable) {// 貌似是Glide加载的图片就这样
                mBitmapWidth = ((TransitionDrawable) drawable).getCurrent().getIntrinsicWidth();
                mBitmapHeight = ((TransitionDrawable) drawable).getCurrent().getIntrinsicHeight();
            } else if (drawable instanceof BitmapDrawable) {
                Bitmap mBitmap = ((BitmapDrawable) drawable).getBitmap();
                mBitmapWidth = mBitmap.getWidth();
                mBitmapHeight = mBitmap.getHeight();
            } else if (drawable instanceof ColorDrawable) {
                ColorDrawable colorDrawable = (ColorDrawable) drawable;
                mBitmapWidth = colorDrawable.getIntrinsicWidth();
                mBitmapHeight = colorDrawable.getIntrinsicHeight();
            } else if (drawable instanceof GlidePlaceholderDrawable) { // placeholder 为自定义的
                mBitmapWidth = drawable.getMinimumWidth();
                mBitmapHeight = drawable.getMinimumHeight();
            } else if (drawable instanceof NinePatchDrawable) {// placeholder 为 .9 图
                mBitmapWidth = drawable.getIntrinsicWidth();
                mBitmapHeight = drawable.getIntrinsicHeight();
            } else {
                mBitmapWidth = drawable.getIntrinsicWidth();
                mBitmapHeight = drawable.getIntrinsicHeight();
            }
            if (mBitmapWidth <= 0) {
                mBitmapWidth = 200;
            }
            if (mBitmapHeight <= 0) {
                mBitmapHeight = 200;
            }
        }

        // 防止mTransform重复的做同样的初始化
        if (mTransformData != null) {
            return;
        }
        mTransformData = new Transform();

        /* 下面为缩放的计算 */
        /* 计算初始的缩放值，初始值因为是 CENTER_CROP 效果，所以要保证图片的宽和高至少1个能匹配原始的宽和高，另1个大于 */
        float xSScale = mOriginalWidth / ((float) mBitmapWidth);
        float ySScale = mOriginalHeight / ((float) mBitmapHeight);
        mTransformData.startScale = Math.max(xSScale, ySScale);
        /* 计算结束时候的缩放值，结束值因为要达到 FIT_CENTER 效果，所以要保证图片的宽和高至少1个能匹配原始的宽和高，另1个小于 */
        float xEScale = getWidth() / ((float) mBitmapWidth);
        float yEScale = getHeight() / ((float) mBitmapHeight);
        mTransformData.endScale = Math.min(xEScale, yEScale);

        /*
         * 下面计算 Canvas Clip 的范围，也就是图片的显示的范围，因为图片是慢慢变大，并且是等比例的，所以这个效果还需要裁减图片显示的区域
         * ，而显示区域的变化范围是在原始 CENTER_CROP 效果的范围区域
         * ，到最终的 FIT_CENTER 的范围之间的，区域我用 LocationSizeF 更好计算
         * ，他就包括左上顶点坐标，和宽高，最后转为 Canvas 裁剪的 Rect.
         */
        /* 开始区域 */
        mTransformData.startRect = new LocationSizeF();
        mTransformData.startRect.left = mOriginalLocationX;
        mTransformData.startRect.top = mOriginalLocationY;
        mTransformData.startRect.width = mOriginalWidth;
        mTransformData.startRect.height = mOriginalHeight;
        /* 结束区域 */
        mTransformData.endRect = new LocationSizeF();
        float bitmapEndWidth = mBitmapWidth * mTransformData.endScale;// 图片最终的宽度
        float bitmapEndHeight = mBitmapHeight * mTransformData.endScale;// 图片最终的宽度
        mTransformData.endRect.left = (getWidth() - bitmapEndWidth) / 2;
        mTransformData.endRect.top = (getHeight() - bitmapEndHeight) / 2;
        mTransformData.endRect.width = bitmapEndWidth;
        mTransformData.endRect.height = bitmapEndHeight;

        mTransformData.rect = new LocationSizeF();
    }

    public void setState(int state) {
        mState = state;
    }

    @Override
    public void setImageDrawable(@Nullable Drawable drawable) {
        super.setImageDrawable(drawable);
        // todo 这里图片已经加载了,根据 Drawable 不同来处理页面的变幻数据
        // if (drawable instanceof BitmapDrawable) {
        mBitmapWidth = 0;
        mBitmapHeight = 0;
        mTransformData = null;
        mSmoothMatrix = new Matrix();
        this.initTransform();
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            drawLoading(canvas);
            return;
        } // couldn't resolve the URI
        if (mState == STATE_TRANSFORM_MOVE) { // 移动
            if (mTransformData == null) {
                super.onDraw(canvas);
                return;
            }

            mPaint.setAlpha(alpha);
            canvas.drawPaint(mPaint);

            int saveCount = canvas.getSaveCount();
            canvas.save();
            getBmpMatrix(); // 先得到图片在此刻的图像Matrix矩阵

            canvas.translate(mTransformData.rect.left, mTransformData.rect.top);
            canvas.clipRect(0, 0, mTransformData.rect.width, mTransformData.rect.height);
            canvas.concat(mSmoothMatrix);
            getDrawable().draw(canvas);
            canvas.restoreToCount(saveCount);
        } else if (mState == STATE_TRANSFORM_IN || mState == STATE_TRANSFORM_OUT || mState == STATE_TRANSFORM_RESTORE) {
            if (mTransformStart) {
                initTransform();
            }

            if (mTransformData == null) {
                super.onDraw(canvas);
                return;
            }

            if (mTransformStart) {
                if (mState == STATE_TRANSFORM_IN) {
                    Timber.d("STATE_TRANSFORM_IN");
                    mTransformData.initStartIn();
                } else if (mState == STATE_TRANSFORM_OUT) {
                    Timber.d("STATE_TRANSFORM_OUT");
                    // mTransform.initStartOut();
                }
            }

            mPaint.setAlpha(alpha);
            canvas.drawPaint(mPaint);

            int saveCount = canvas.getSaveCount();
            canvas.save();
            getBmpMatrix(); // 先得到图片在此刻的图像Matrix矩阵

            canvas.translate(mTransformData.rect.left, mTransformData.rect.top);
            canvas.clipRect(0, 0, mTransformData.rect.width, mTransformData.rect.height);
            canvas.concat(mSmoothMatrix);
            getDrawable().draw(canvas);
            canvas.restoreToCount(saveCount);

            if (mTransformStart) {
                mTransformStart = false;
                startTransform(mState);
            }
        } else {
            // 当Transform In变化完成后，把背景改为黑色，使得Activity不透明
            mPaint.setAlpha(255);
            canvas.drawPaint(mPaint);
            super.onDraw(canvas);
        }
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
    protected Paint mLoadingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 绘制加载进度条
     *
     * @param canvas
     */
    private void drawLoading(Canvas canvas) {
        if (!isLoading) {
            return;
        }

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
            mLoadingPaint.setAlpha((i + 5) * 0x11);
            canvas.rotate(30, (width) / 2f, (height) / 2f);
            canvas.drawPath(mPath, mLoadingPaint);
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

    private void getBmpMatrix() {
        /* 下面实现了CENTER_CROP的功能 */
        mSmoothMatrix.setScale(mTransformData.scale, mTransformData.scale);
        mSmoothMatrix.postTranslate(-(mTransformData.scale * mBitmapWidth / 2 - mTransformData.rect.width / 2),
                -(mTransformData.scale * mBitmapHeight / 2 - mTransformData.rect.height / 2));
    }

    /**
     * 执行动画
     *
     * @param state .
     */
    private void startTransform(final int state) {
        if (mTransformData == null) {
            return;
        }
        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(duration);
        // valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator()); // 加速减速
        valueAnimator.setInterpolator(new DecelerateInterpolator(2)); // 减速
        if (state == STATE_TRANSFORM_IN) {// 显示
            PropertyValuesHolder scaleHolder =
                    PropertyValuesHolder.ofFloat("scale", mTransformData.startScale, mTransformData.endScale);
            PropertyValuesHolder leftHolder =
                    PropertyValuesHolder.ofFloat("left", mTransformData.startRect.left, mTransformData.endRect.left);
            PropertyValuesHolder topHolder =
                    PropertyValuesHolder.ofFloat("top", mTransformData.startRect.top, mTransformData.endRect.top);
            PropertyValuesHolder widthHolder =
                    PropertyValuesHolder.ofFloat("width", mTransformData.startRect.width, mTransformData.endRect.width);
            PropertyValuesHolder heightHolder =
                    PropertyValuesHolder.ofFloat("height", mTransformData.startRect.height, mTransformData.endRect.height);
            PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("alpha", 0, 255);
            valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder, alphaHolder);
        } else if (state == STATE_TRANSFORM_OUT) {// 退出
            if (mTransformData.scale == 0) {
                PropertyValuesHolder scaleHolder =
                        PropertyValuesHolder.ofFloat("scale", mTransformData.endScale, mTransformData.startScale);
                PropertyValuesHolder leftHolder =
                        PropertyValuesHolder.ofFloat("left", mTransformData.endRect.left, mTransformData.startRect.left);
                PropertyValuesHolder topHolder =
                        PropertyValuesHolder.ofFloat("top", mTransformData.endRect.top, mTransformData.startRect.top);
                PropertyValuesHolder widthHolder =
                        PropertyValuesHolder.ofFloat("width", mTransformData.endRect.width, mTransformData.startRect.width);
                PropertyValuesHolder heightHolder =
                        PropertyValuesHolder.ofFloat("height", mTransformData.endRect.height, mTransformData.startRect.height);
                PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("alpha", mPaint.getAlpha(), 0);
                valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder, alphaHolder);
            } else {
                PropertyValuesHolder scaleHolder =
                        PropertyValuesHolder.ofFloat("scale", mTransformData.scale, mTransformData.startScale);
                PropertyValuesHolder leftHolder =
                        PropertyValuesHolder.ofFloat("left", mTransformData.rect.left, mTransformData.startRect.left);
                PropertyValuesHolder topHolder =
                        PropertyValuesHolder.ofFloat("top", mTransformData.rect.top, mTransformData.startRect.top);
                PropertyValuesHolder widthHolder =
                        PropertyValuesHolder.ofFloat("width", mTransformData.rect.width, mTransformData.startRect.width);
                PropertyValuesHolder heightHolder =
                        PropertyValuesHolder.ofFloat("height", mTransformData.rect.height, mTransformData.startRect.height);
                PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("alpha", mPaint.getAlpha(), 0);
                valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder, alphaHolder);
            }
        } else if (state == STATE_TRANSFORM_RESTORE) {// 复位
            PropertyValuesHolder scaleHolder =
                    PropertyValuesHolder.ofFloat("scale", mTransformData.scale, mTransformData.endScale);
            PropertyValuesHolder leftHolder =
                    PropertyValuesHolder.ofFloat("left", mTransformData.rect.left, mTransformData.endRect.left);
            PropertyValuesHolder topHolder =
                    PropertyValuesHolder.ofFloat("top", mTransformData.rect.top, mTransformData.endRect.top);
            PropertyValuesHolder widthHolder =
                    PropertyValuesHolder.ofFloat("width", mTransformData.rect.width, mTransformData.endRect.width);
            PropertyValuesHolder heightHolder =
                    PropertyValuesHolder.ofFloat("height", mTransformData.rect.height, mTransformData.endRect.height);
            PropertyValuesHolder alphaHolder = PropertyValuesHolder.ofInt("alpha", mPaint.getAlpha(), 255);
            valueAnimator.setValues(scaleHolder, leftHolder, topHolder, widthHolder, heightHolder, alphaHolder);
        } else {
            return;
        }

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public synchronized void onAnimationUpdate(ValueAnimator animation) {
                mTransformData.scale = (Float) animation.getAnimatedValue("scale");
                mTransformData.rect.left = (Float) animation.getAnimatedValue("left");
                mTransformData.rect.top = (Float) animation.getAnimatedValue("top");
                mTransformData.rect.width = (Float) animation.getAnimatedValue("width");
                mTransformData.rect.height = (Float) animation.getAnimatedValue("height");
                alpha = (Integer) animation.getAnimatedValue("alpha");
                invalidate(); // 重新绘制
            }
        });
        valueAnimator.addListener(new ValueAnimator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mTransformListener != null) {
                    mTransformListener.onTransformComplete(state);
                }
                duration = ANIMATION_DURATION;
                /*
                 * 如果是进入的话，当然是希望最后停留在center_crop的区域。但是如果是out的话，就不应该是center_crop的位置了
                 * ， 而应该是最后变化的位置，因为当out的时候结束时，不恢复视图是Normal，要不然会有一个突然闪动回去的bug
                 */
                // TODO 这个可以根据实际需求来修改
                if (state == STATE_TRANSFORM_IN) {
                    mState = STATE_NORMAL;
                }
                if (state == STATE_TRANSFORM_MOVE) {
                    mState = STATE_NORMAL;
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });
        valueAnimator.start();
    }

    /**
     * 设置动画完成监听
     *
     * @param listener .
     */
    public void setOnTransformListener(TransformListener listener) {
        mTransformListener = listener;
    }

    /**
     * 设置动画完成监听
     *
     * @param listener .
     */
    public void setOnBeforeTransformListener(TransformListener listener) {
        mBeforeTransformListener = listener;
    }

    public TransformListener getBeforeTransformListener() {
        return mBeforeTransformListener;
    }

    /**
     * 变幻
     */
    public static class Transform {
        float startScale;// 图片开始的缩放值
        float endScale;// 图片结束的缩放值
        float scale;// 属性ValueAnimator计算出来的值 (当前值)

        LocationSizeF startRect;// 开始的区域
        LocationSizeF endRect;// 结束的区域
        LocationSizeF rect;// 属性ValueAnimator计算出来的值 (当前值)

        void initStartIn() {
            scale = startScale;
            try {
                rect = (LocationSizeF) startRect.clone();
            } catch (CloneNotSupportedException e) {
                Timber.e(e);
            }
        }

        void initStartOut() {
            scale = endScale;
            try {
                rect = (LocationSizeF) endRect.clone();
            } catch (CloneNotSupportedException e) {
                Timber.e(e);
            }
        }
    }

    /**
     * 位置
     */
    public static class LocationSizeF implements Cloneable {
        float left;
        float top;
        float width;
        float height;

        @Override
        public @NotNull String toString() {
            return "[left:" + left + " top:" + top + " width:" + width + " height:" + height + "]";
        }

        @Override
        public @NotNull Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    /**
     * 动画完成监听接口
     */
    public interface TransformListener {
        /**
         * @param mode STATE_TRANSFORM_IN 1 ,STATE_TRANSFORM_OUT 2
         */
        void onTransformComplete(int mode);// mode 1
    }

    /**
     * 计算内容的宽高值,保持原始比例
     *
     * @param content   内容实际宽高
     * @param container 容器实际宽高
     * @param contain   true: 容器包含内容  false: 内容包含容器
     * @return 计算后的尺寸, 保持比例
     */
    public static Size calculateContentSize(Size content, Size container, boolean contain) {
        // 获取裁剪区域的新的宽高
        float containerScale = (float) container.getWidth() / (float) container.getHeight();// 1
        float contentScale = (float) content.getWidth() / (float) content.getHeight(); // 2

        float w;
        float h;
        // 如果 容器宽高比 > 内容宽高比 那么内容宽度取容器宽度, 内容高度需计算(contentScale = w / h)
        if (contain) { // 容器包含内容
            if (containerScale < contentScale) {
                w = container.getWidth();
                h = w / contentScale;
            } else {
                h = container.getHeight();
                w = h * contentScale;
            }
        } else { // 内容包含容器
            if (containerScale > contentScale) { // 高度超出容器, 宽度等于容器宽度
                w = container.getWidth();
                h = w / contentScale;
            } else { // 内容宽度超出容器,高度等于容器高度
                h = container.getHeight();
                w = h * contentScale;
            }
        }
        return new Size((int) w, (int) h);
    }
}
