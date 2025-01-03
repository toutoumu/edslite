package cn.yinxm.media.video.gesture.touch.handler;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import cn.yinxm.media.video.gesture.touch.adapter.IVideoTouchAdapter;
import cn.yinxm.media.video.gesture.touch.anim.VideoScaleEndAnimator;
import cn.yinxm.media.video.gesture.touch.ui.TouchScaleResetView;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


/**
 * 播放器画面双指手势缩放处理：
 * <p>
 * 1. 双指缩放
 * 2. 双指平移
 * 3. 缩放结束后，若为缩小画面，居中动效
 * 4. 缩放结束后，若为放大画面，自动吸附屏幕边缘动效
 * 5. 暂停播放下，实时更新缩放画面
 *
 * @author yinxuming
 * @date 2020/12/2
 */
public class VideoTouchScaleHandler implements ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = "VideoTouchScaleHandler";
    /**
     * 最小缩放比例
     */
    private static final float MIN_SCALE = 0.4F;
    /**
     * 最大缩放比例
     */
    private static final float MAX_SCALE = 5F;

    /**
     * 获取初始的矩阵，使得视频等比例放大到左右两边或上下两边贴合容器居中显示
     *
     * @param textureViewWidth  视频容器(textureView)宽度
     * @param textureViewHeight 视频容器(textureView)高度
     * @param videoWidth        视频宽度
     * @param videoHeight       视频高度
     * @return 矩阵
     */
    public static @NonNull Matrix getMatrix(float textureViewWidth, float textureViewHeight, float videoWidth, float videoHeight) {
        float sx = textureViewWidth / videoWidth;
        float sy = textureViewHeight / videoHeight;
        Matrix matrix = new Matrix();
        // 第1步:把视频区移动到View区,使两者中心点重合.
        matrix.preTranslate((textureViewWidth - videoWidth) / 2, (textureViewHeight - videoHeight) / 2);
        // 第2步:因为默认视频是fitXY的形式显示的,所以首先要缩放还原回来.
        matrix.preScale(videoWidth / textureViewWidth, videoHeight / textureViewHeight);
        // 第3步,等比例放大或缩小,直到视频区的一边和View一边相等.如果另一边和view的一边不相等，则留下空隙
        if (sx >= sy) {
            matrix.postScale(sy, sy, textureViewWidth / 2, textureViewHeight / 2);
        } else {
            matrix.postScale(sx, sx, textureViewWidth / 2, textureViewHeight / 2);
        }

        // 注意XY方向的初始值不一样
        /*final float[] mMatrixValue = new float[9];
        matrix.getValues(mMatrixValue);
        Log.e(TAG, "初始缩放值:" + mMatrixValue[Matrix.MSCALE_X] + "----" + mMatrixValue[Matrix.MSCALE_Y]);*/
        return matrix;
    }

    private final Context mContext;
    public final FrameLayout mContainer;
    private final IVideoTouchAdapter mTouchAdapter;
    /**
     * 缩放是否可用
     */
    private boolean scaleEnable = true;
    /**
     * 是否正在缩放
     */
    private boolean isScaling = false; // 是否正在缩放
    /**
     * 是否正在单指拖动
     */
    private boolean isScrolling = false; // 是否正在单指拖动
    /**
     * 缩放开始前的矩阵值用于恢复
     */
    private Matrix mStartMatrix;
    /**
     * 缓存了上次的矩阵值，所以需要计算每次变化量
     */
    private Matrix mScaleTransMatrix;
    /**
     * 缩放中心点
     */
    private float mStartCenterX, mStartCenterY, mLastCenterX, mLastCenterY, mCurrentCenterX, mCurrentCenterY;
    /**
     * 两指之间距离
     */
    private float mStartSpan, mLastSpan, mCurrentSpan;
    /**
     * 缩放比例
     */
    private float mScale;
    private final float[] mMatrixValue = new float[9];
    private VideoScaleEndAnimator mScaleAnimator;
    private TouchScaleResetView mScaleRestView; // 恢复缩放

    /**
     * @param context
     * @param container         这个容器用于存放 恢复缩放的控件
     * @param videoTouchAdapter
     */
    public VideoTouchScaleHandler(Context context, FrameLayout container, IVideoTouchAdapter videoTouchAdapter) {
        mContext = context;
        mContainer = container;
        mTouchAdapter = videoTouchAdapter;
        initView();
    }

    private void initView() {
        // 初始化 恢复缩放 添加回调
        mScaleRestView = new TouchScaleResetView(mContext, mContainer) {
            @Override
            public void clickResetScale() {
                mScaleRestView.setVisibility(View.GONE);
                if (isScaled()) {
                    cancelScale();
                }
            }
        };
    }

    @Override
    public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
        final TextureView textureView = mTouchAdapter.getTextureView();
        if (textureView != null) {
            // 标记缩放状态
            isScaling = true;
            // 第一次缩放的时候缓存当前矩阵
            if (mStartMatrix == null) {
                mStartMatrix = new Matrix(textureView.getTransform(null));
            }
            if (mScaleTransMatrix == null) {
                mScaleTransMatrix = new Matrix(textureView.getTransform(null));
                onScaleMatrixUpdate(mScaleTransMatrix);
            }
        }
        // 开始缩放时的中心点,和两指之间的距离
        mStartCenterX = detector.getFocusX();
        mStartCenterY = detector.getFocusY();
        mStartSpan = detector.getCurrentSpan();

        // 初始化上一次两指中心点，和上一次的两指距离
        mLastCenterX = mStartCenterX;
        mLastCenterY = mStartCenterY;
        mLastSpan = mStartSpan;
        return true;
    }

    @Override
    public boolean onScale(@NonNull ScaleGestureDetector detector) {
        // 如果开启缩放且触发了缩放事件，则处理缩放事件
        if (isScaling && scaleEnable) {
            // 缩放滑动时, 更新当前缩放中心点，和两指之间的距离
            mCurrentCenterX = detector.getFocusX();
            mCurrentCenterY = detector.getFocusY();
            mCurrentSpan = detector.getCurrentSpan();
            if (processOnScale(detector)) {
                // 更新上一次两指中心点，和上一次的两指距离
                mLastCenterX = mCurrentCenterX;
                mLastCenterY = mCurrentCenterY;
                mLastSpan = mCurrentSpan;
            }
        }
        return false;
    }

    /**
     * 处理缩放操作
     */
    private boolean processOnScale(ScaleGestureDetector detector) {
        if (mScaleTransMatrix != null) {
            // 两次缩放回调之间放大或缩小了多少
            float diffScale = mCurrentSpan / mLastSpan;
            // 这里传mStartCenterX, mStartCenterY表示不移动位置,仅缩放操作
            postScale(mScaleTransMatrix, diffScale, mStartCenterX, mStartCenterY);
            // 这个方法仅移动位置,不缩放
            mScaleTransMatrix.postTranslate(detector.getFocusX() - mLastCenterX, detector.getFocusY() - mLastCenterY);
            onScaleMatrixUpdate(mScaleTransMatrix);
            return true;
        }
        return false;
    }

    /**
     * @param matrix
     * @param scale  相对于前一次放大缩小了多少 >1 表示放大, <1 表示缩小
     * @param x
     * @param y
     */
    private void postScale(Matrix matrix, float scale, float x, float y) {
        matrix.getValues(mMatrixValue);
        // final float curScale = mMatrixValue[Matrix.MSCALE_X];
        final float curScale = Math.max(mMatrixValue[Matrix.MSCALE_X], mMatrixValue[Matrix.MSCALE_Y]);
        if (scale < 1 && Math.abs(curScale - MIN_SCALE) < 0.001F) {
            scale = 1;
        } else if (scale > 1 && Math.abs(curScale - MAX_SCALE) < 0.001F) {
            scale = 1;
        }
        // 缩放之后的缩放倍数
        float newScale = curScale * scale;
        if (scale < 1 && newScale < MIN_SCALE) {
            scale = MIN_SCALE / curScale;
            newScale = curScale * scale;
        } else if (scale > 1 && newScale > MAX_SCALE) {
            scale = MAX_SCALE / curScale;
            newScale = curScale * scale;
        }

        // Log.e(TAG, "当前缩放: " + curScale + " 新的缩放: " + newScale);
        matrix.postScale(scale, scale, x, y);
    }


    @Override
    public void onScaleEnd(@NonNull ScaleGestureDetector detector) {
        if (isScaling) { // 取消多手势操作
            isScaling = false;
            doScaleEndAnim();
        }
    }

    /**
     * 取消缩放操作, 恢复到默认值, 这里的重置初始值很关键
     */
    public void cancelScale() {
        // 恢复到初始值
        if (mStartMatrix != null) {
            onScaleMatrixUpdate(mStartMatrix);
        }

        // 重置初始值
        isScaling = false;
        isScrolling = false;
        mStartMatrix = null;
        mScaleTransMatrix = null;

        mScale = 0;
    }

    /**
     * 计算缩放结束后动画位置：scaleEndAnimMatrix
     */
    private void doScaleEndAnim() {
        final TextureView textureView = mTouchAdapter.getTextureView();
        if (textureView == null) {
            return;
        }
        final IjkMediaPlayer mediaPlayer = mTouchAdapter.getMediaPlayer();
        if (mediaPlayer == null) {
            return;
        }
        Matrix scaleEndAnimMatrix = new Matrix();
        RectF videoRectF = new RectF(0, 0, textureView.getWidth(), textureView.getHeight());
        if (mScale > 0 && mScale <= 1.0f) { // 缩小居中
            float textureViewWidth = textureView.getWidth();
            float textureViewHeight = textureView.getHeight();
            float videoWidth = mediaPlayer.getVideoWidth();
            float videoHeight = mediaPlayer.getVideoHeight();
            scaleEndAnimMatrix = getMatrix(textureViewWidth, textureViewHeight, videoWidth, videoHeight);
            scaleEndAnimMatrix.postScale(mScale, mScale, videoRectF.right / 2, videoRectF.bottom / 2);

            startTransToAnimEnd(mScaleTransMatrix, scaleEndAnimMatrix);

            Log.e(TAG, "缩放比例: " + mScale);
            Log.e(TAG, "videoRef 左上右下 " + videoRectF);
            Log.e(TAG, "TextureWidth: " + textureViewWidth + " TextureHeight: " + textureViewHeight + " VideoWidth: " + videoWidth + " VideoHeight: " + videoHeight);
        }
        // 放大，检测4边是否有在屏幕内部，有的话自动吸附到屏幕边缘
        else if (mScale > 1.0F) {
            Log.e(TAG, "缩放比例: " + mScale);
            RectF rectF = new RectF(0, 0, textureView.getWidth(), textureView.getHeight());
            // 测量经过缩放位移变换后的播放画面位置
            mScaleTransMatrix.mapRect(rectF);
            float transAnimX = 0f;
            float transAnimY = 0f;
            scaleEndAnimMatrix.set(mScaleTransMatrix);
            if (rectF.left > videoRectF.left || rectF.right < videoRectF.right || rectF.top > videoRectF.top || rectF.bottom < videoRectF.bottom) {
                // 如果视频宽度小于容器宽度, 那么居中
                if (rectF.width() < videoRectF.width()) {
                    transAnimX = ((videoRectF.left - rectF.left) + (videoRectF.right - rectF.right)) / 2;
                    Log.e(TAG, "水平居中");
                } else {
                    if (rectF.left > videoRectF.left) { // 左移吸边
                        transAnimX = videoRectF.left - rectF.left;
                        Log.e(TAG, "左移吸边");
                    } else if (rectF.right < videoRectF.right) {  // 右移吸边
                        transAnimX = videoRectF.right - rectF.right;
                        Log.e(TAG, "右移吸边");
                    }
                }
                // 如果视频高度小于容器高度, 那么居中
                if (rectF.height() < videoRectF.height()) {
                    transAnimY = ((videoRectF.top - rectF.top) + (videoRectF.bottom - rectF.bottom)) / 2;
                    Log.e(TAG, "垂直居中");
                } else {
                    if (rectF.top > videoRectF.top) {  // 上移吸边
                        transAnimY = videoRectF.top - rectF.top;
                        Log.e(TAG, "上移吸边");
                    } else if (rectF.bottom < videoRectF.bottom) { // 下移吸边
                        transAnimY = videoRectF.bottom - rectF.bottom;
                        Log.e(TAG, "下移吸边");
                    }
                }

                scaleEndAnimMatrix.postTranslate(transAnimX, transAnimY);
                startTransToAnimEnd(mScaleTransMatrix, scaleEndAnimMatrix);
            }
        }
    }

    /**
     * 变幻动画
     *
     * @param startMatrix 开始矩阵
     * @param endMatrix   结束矩阵
     */
    private void startTransToAnimEnd(Matrix startMatrix, Matrix endMatrix) {
        Log.d(TAG, "startTransToAnimEnd \nstart=" + startMatrix + "\nend=" + endMatrix);
        if (mScaleAnimator != null) {
            mScaleAnimator.cancel();
            mScaleAnimator = null;
        }
        mScaleAnimator = new VideoScaleEndAnimator(startMatrix, endMatrix) {
            @Override
            protected void updateMatrixToView(Matrix transMatrix) {
                onScaleMatrixUpdate(transMatrix);
            }
        };
        mScaleAnimator.start();
        mScaleTransMatrix = endMatrix;
    }

    /**
     * 显示缩放重置按钮
     */
    public void showScaleReset() {
        if (isScaled() && mTouchAdapter != null) {
            if (mScaleRestView != null && mScaleRestView.getVisibility() != View.VISIBLE) {
                mScaleRestView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 隐藏缩放
     */
    public void hideScaleReset() {
        if (mScaleRestView != null && mScaleRestView.getVisibility() == View.VISIBLE) {
            mScaleRestView.setVisibility(View.GONE);
        }
    }


    /**
     * 手势滑动
     *
     * @param distanceX 横向滑动距离
     * @param distanceY 纵向滑动距离
     * @return 是否滑动
     */
    public boolean onScroll(float distanceX, float distanceY) {
        // 缩放模式下，是否需要单手滚动
        if (!isScaling && isScaled() && mScaleTransMatrix != null) {
            TextureView textureView = mTouchAdapter.getTextureView();
            if (textureView != null) {
                isScrolling = true;
                mScaleTransMatrix.postTranslate(-distanceX, -distanceY);
                onScaleMatrixUpdate(mScaleTransMatrix);
                return true;
            }
        }
        return false;
    }

    /**
     * 手势滑动结束
     */
    public void onScrollEnd() {
        if (isScrolling) {
            isScrolling = false;
            doScaleEndAnim();
        }
    }

    /**
     * 矩阵更新后的操作
     *
     * @param matrix .
     */
    private void onScaleMatrixUpdate(Matrix matrix) {
        matrix.getValues(mMatrixValue);
        // 获取当前缩放值
        mScale = Math.max(mMatrixValue[Matrix.MSCALE_X], mMatrixValue[Matrix.MSCALE_Y]);
        // 更新视频缩放
        TextureView textureView = mTouchAdapter.getTextureView();
        if (textureView != null) {
            textureView.setTransform(matrix);
            // 暂停下，实时更新缩放画面
            if (!mTouchAdapter.isPlaying()) {
                textureView.invalidate();
            }
        }
    }

    /**
     * 是否处于已缩放 or 缩放中
     */
    public boolean isInScaleStatus() {
        return isScaled() || isScaling;
    }

    /**
     * 是否处于已缩放
     */
    public boolean isScaled() {
        return mScale > 0 && mScale <= 0.99F || mScale >= 1.01F;
    }

    /**
     * 是否开启缩放手势
     *
     * @return .
     */
    public boolean getScaleEnable() {
        return scaleEnable;
    }

    /**
     * 设置是否开启缩放手势
     *
     * @param enable .
     */
    public void setScaleEnable(boolean enable) {
        this.scaleEnable = enable;
    }

    /**
     * 是否处于滑动中
     */
    public boolean isScrolling() {
        return isScrolling;
    }
}
