package cn.yinxm.media.video.gesture.touch.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.ijk.R;

/**
 * 重置缩放
 */
public abstract class TouchScaleResetView implements View.OnClickListener {
    private final View mScaleResetContent;

    public TouchScaleResetView(Context context, ViewGroup container) {
        View view = LayoutInflater.from(context).inflate(R.layout.touch_scale_rest_view, container);
        mScaleResetContent = view.findViewById(R.id.view_scale_reset);
        View mScaleResetView = view.findViewById(R.id.tv_scale_reset);
        mScaleResetView.setOnClickListener(this);
    }

    public void setVisibility(int visibility) {
        mScaleResetContent.setVisibility(visibility);
    }

    public int getVisibility() {
        return mScaleResetContent.getVisibility();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tv_scale_reset) {
            clickResetScale();
        }
    }

    /**
     * 重置缩放值
     */
    public abstract void clickResetScale();
}
