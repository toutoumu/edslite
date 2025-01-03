package androidx.ijk.listener;

import androidx.ijk.helper.Orientation;

public interface OnIJKVideoSwitchScreenListener {

    /**
     * IJK屏幕旋转方向监听
     *
     * @param orientation
     */
    void onIJKVideoSwitchScreen(Orientation orientation);

}
