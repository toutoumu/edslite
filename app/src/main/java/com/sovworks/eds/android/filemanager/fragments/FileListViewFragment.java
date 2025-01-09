package com.sovworks.eds.android.filemanager.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;
import com.library.widget.SmoothImageView;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.databinding.FileListViewFragmentBinding;
import com.sovworks.eds.android.filemanager.PictureAdapter2;
import com.sovworks.eds.android.filemanager.SimpleAnimationListener;
import com.sovworks.eds.android.helpers.CachedPathInfo;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FileListViewFragment extends FileListViewFragmentBase {
    public static FileListViewFragment newInstance() {
        return new FileListViewFragment();
    }

    /**
     * 动画时长
     */
    private static final int ANIMATION_DURATION = 300;
    public RequestOptions mThumbnailRequestOptions;
    public RequestManager mGlideRequests;

    private int viewType = 0; // 0: 网格 1: 图片浏览
    private int mPageIndex = 0; // 当前页索引(图片浏览器中图片索引)

    private final List<CachedPathInfo> mPictures = new ArrayList<>(); // 图片浏览器的图片列表
    private final List<CachedPathInfo> allFIle = new ArrayList<>(); // 所有文件列表

    public PictureAdapter2 mPictureAdapter;

    public FileListViewFragmentBinding bind;

    final OnBackPressedCallback _backPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            if (!onBackPressed()) {
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTransparentForWindow();

        mGlideRequests = Glide.with(this);
        mThumbnailRequestOptions = RequestOptions
                .fitCenterTransform()
                .override(500, 900)
                .error(R.drawable.ic_default_image_list)
                .placeholder(R.drawable.transparent_drawable)
                // .placeholder(R.drawable.ic_default_image)
                // https://www.jianshu.com/p/54bf089d0b04 解决Glide启用过渡时Placeholder变形问题
                // .placeholder(new GlidePlaceholderDrawable(this.getResources(), R.drawable.ic_default_image))
                .dontAnimate()
                .dontTransform()
                .encodeQuality(80);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        assert view != null;
        bind = FileListViewFragmentBinding.bind(view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 监听返回键
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), _backPressedCallback);

        initViewPage();
        initListeners();

        bind.dragLayout.setContainer(bind.pager);
        bind.dragLayout.setImageView(bind.smoothImageView);

        // bind.include.toolBar.setPadding(0, SizeUtils.getStatusBarHeight(requireActivity()), 0, 0);
        // 状态栏布局变化后, 更新列表位置
        bind.include.toolBar.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom != oldBottom) {
                // 设置文件列表区域
                bind.listContainer.setPadding(0,
                        bottom,
                        0,
                        ImmersionBar.getNavigationBarHeight(FileListViewFragment.this));
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 监听返回键
        _backPressedCallback.remove();
    }

    public RequestOptions getmThumbnailRequestOptions() {
        return mThumbnailRequestOptions;
    }

    public void showView(List<CachedPathInfo> allFIle, List<CachedPathInfo> pictures, View view, int position) {
        // 如果GridView不可点击, 防止多次点击
        if (!gridClickable) {
            return;
        }
        bind.browserContainer.setVisibility(View.VISIBLE);
        // 拦截返回按钮点击事件
        _backPressedCallback.setEnabled(true);
        // 所有数据
        this.allFIle.clear();
        this.allFIle.addAll(allFIle);
        // 图片数据
        this.mPictures.clear();
        this.mPictures.addAll(pictures);
        if (mPictureAdapter != null) {
            mPictureAdapter.notifyDataSetChanged();
        }

        bind.pager.setCurrentItem(position, false);

        // 打开图片浏览
        ImageView img = view.findViewById(android.R.id.icon);
        Rect rect = new Rect();
        img.getGlobalVisibleRect(rect);

        bind.smoothImageView.setImageDrawable(img.getDrawable()); // 必须在 setOriginalInfo 之前设置图片
        bind.smoothImageView.setOriginalInfo(rect.width(), rect.height(), rect.left, rect.top, 0, 0);
        bind.smoothImageView.transformIn();
    }

    /**
     * 初始化点击事件
     */
    private void initListeners() {
        // 动画开始之前的回调
        bind.smoothImageView.setOnBeforeTransformListener(mode -> {
            gridClickable = false;
            bind.pager.setVisibility(View.INVISIBLE);
            bind.smoothImageView.setVisibility(View.VISIBLE);
            switch (mode) {
                case SmoothImageView.STATE_TRANSFORM_IN: { // 显示图片浏览器
                    Timber.e("------------显示图片浏览器之前------------");
                    viewType = 1; // 0: 网格 1: 图片浏览
                    toolbarInAnimation();
                    footerInAnimation();
                    renderView();
                    break;
                }
                case SmoothImageView.STATE_TRANSFORM_OUT: { // 显示网格列表
                    Timber.e("------------显示网格列表之前------------");
                    viewType = 0; // 0: 网格 1: 图片浏览
                    toolbarInAnimation();
                    footerOutAnimation();
                    renderView();
                    break;
                }
                case SmoothImageView.STATE_TRANSFORM_RESTORE: { // 恢复到图片浏览器
                    Timber.e("------------恢复到图片浏览器之前------------");
                    viewType = 1; // 0: 网格 1: 图片浏览
                    toolbarInAnimation();
                    footerInAnimation();
                    renderView();
                    break;
                }
                case SmoothImageView.STATE_TRANSFORM_MOVE: {
                    Timber.e("------------开始拖动之前------------");
                    toolBarOutAnimation();
                    footerOutAnimation();
                    break;
                }
            }
        });

        // 动画结束之后的回调
        bind.smoothImageView.setOnTransformListener(mode -> {
            gridClickable = true;
            bind.smoothImageView.setVisibility(View.INVISIBLE);
            switch (mode) {
                case SmoothImageView.STATE_TRANSFORM_IN: { // 显示图片浏览器
                    Timber.e("------------显示图片浏览器------------");
                    bind.pager.setVisibility(View.VISIBLE);
                    break;
                }
                case SmoothImageView.STATE_TRANSFORM_OUT: { // 显示网格列表
                    Timber.e("------------显示网格列表------------");
                    bind.pager.setVisibility(View.INVISIBLE);
                    bind.browserContainer.setVisibility(View.INVISIBLE);
                    break;
                }
                case SmoothImageView.STATE_TRANSFORM_RESTORE: { // 恢复到原来昨天
                    Timber.e("------------恢复到图片浏览器------------");
                    bind.pager.setVisibility(View.VISIBLE);
                }
            }
        });

        // 左下角返回相册
        bind.showAlbum.setOnClickListener(view12 -> {
            if (viewType == 1) {
                this.onBackPressed();
            }
        });

        // 下一张
        bind.next.setOnClickListener(v -> {
            if (mPageIndex < mPictures.size() - 1) {// 如果不是最后一项
                mPageIndex = mPageIndex + 1;
                bind.pager.setCurrentItem(mPageIndex);
                renderView();
            } else {
                Toast.makeText(this.requireActivity(), "已经是最后一张了!", Toast.LENGTH_SHORT).show();
            }
        });

        // 上一张
        bind.preview.setOnClickListener(v -> {
            if (mPageIndex > 0) {// 如果不是第一项
                mPageIndex = mPageIndex - 1;
                bind.pager.setCurrentItem(mPageIndex);
                renderView();
            } else {
                Toast.makeText(this.requireActivity(), "已经是第一张了!", Toast.LENGTH_SHORT).show();
            }
        });

        // 右下角操作
        // bind.showAction.setOnClickListener(view1 -> showAction());
    }

    /**
     * 初始化图片浏览
     */
    private void initViewPage() {
        mPictureAdapter = new PictureAdapter2(this, mPictures);
        bind.pager.setAdapter(mPictureAdapter);
        bind.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                Timber.e("onPageSelected position: %s", position);
                if (position != 0 && position > mPictures.size() - 1) {
                    Timber.e("position 超出了 mPictrues.size");
                    return;
                }
                mPageIndex = position;

                int realPosition = allFIle.indexOf(mPictures.get(position));
                getListView().smoothScrollToPosition(realPosition);
                renderView();
                updateTransferInfo();
            }
        });
        bind.pager.setCurrentItem(mPageIndex, false);
    }

    /**
     * 更新拖拽图片的动画参数,并加载对应位置的图片
     */
    private void updateTransferInfo() {
        if (!mPictures.isEmpty()) {
            getListView().postDelayed(() -> {
                int realPosition = allFIle.indexOf(mPictures.get(mPageIndex));
                Rect rect = computeBounds(getListView(), realPosition, android.R.id.icon, 0);
                bind.smoothImageView.setOriginalInfo(rect.width(), rect.height(), rect.left, rect.top, 0, 0);
            }, 150);

            CachedPathInfo picture = mPictures.get(mPageIndex);
            if (picture != null) {
                // 加载数据
                bind.smoothImageView.setLoading(true);
                // mGlideRequests.load(picture.getFilePath())
                mGlideRequests.load(picture.getPath())
                        .apply(mThumbnailRequestOptions)
                        .error(R.drawable.ic_default_image_list)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e,
                                                        Object model,
                                                        Target<Drawable> target,
                                                        boolean isFirstResource) {
                                bind.smoothImageView.setLoading(false);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource,
                                                           Object model,
                                                           Target<Drawable> target,
                                                           DataSource dataSource,
                                                           boolean isFirstResource) {
                                bind.smoothImageView.setLoading(false);
                                return false;
                            }
                        })
                        // .transition(DrawableTransitionOptions.withCrossFade())
                        .into(bind.smoothImageView);
            }
        }
    }

    private boolean gridClickable = true;

    /**
     * 切换到网格视图
     *
     * @return 是否成功
     */
    public boolean onBackPressed() {
        if (viewType == 1) {// 切换到网格视图
            bind.smoothImageView.transformOut();
            return true;
        }
        _backPressedCallback.setEnabled(false);
        return super.onBackPressed();
    }

    /**
     * 底部操作按钮,标题栏 显示&隐藏
     */
    public void toggleUI() {
        if (bind.footer.getVisibility() != View.VISIBLE) {// 显示
            toolbarInAnimation();
            footerInAnimation();
        } else {// 隐藏
            toolBarOutAnimation();
            footerOutAnimation();
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderView() {
        if (viewType == 0) {// 网格|列表
            bind.include.sum.setVisibility(View.GONE);
        } else { // 图片浏览模式
            bind.include.sum.setVisibility(View.VISIBLE);
            bind.include.sum.setText((mPageIndex + 1) + "/" + mPictures.size());
        }
    }

    /**
     * 标题栏进入动画
     */
    private void toolbarInAnimation() {
        if (bind.include.toolBar.getVisibility() == View.VISIBLE) {
            return;
        }
        ImmersionBar.with(this).reset()
                .fullScreen(false)
                .hideBar(BarHide.FLAG_SHOW_BAR)
                .init();
        setTransparentForWindow();

        TranslateAnimation translateAnimation =
                new TranslateAnimation(0, 0, -bind.include.toolBar.getHeight() + ImmersionBar.getStatusBarHeight(this), 0);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setInterpolator(new DecelerateInterpolator());
        animationSet.setDuration(ANIMATION_DURATION);
        animationSet.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animationSet);
                bind.include.toolBar.clearAnimation();
            }
        });
        bind.include.toolBar.clearAnimation();
        bind.include.toolBar.setVisibility(View.VISIBLE);
        bind.include.toolBar.startAnimation(animationSet);
    }

    /**
     * 标题栏退出动画
     */
    private void toolBarOutAnimation() {
        if (bind.include.toolBar.getVisibility() != View.VISIBLE) {
            Timber.e("toolbar当前为不可见状态不执行动画");
            return;
        }
        ImmersionBar.with(this).reset()
                .fullScreen(true)
                .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                .init();

        TranslateAnimation translateAnimation =
                new TranslateAnimation(0, 0, 0, -bind.include.toolBar.getHeight() + ImmersionBar.getStatusBarHeight(this));
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.setDuration(ANIMATION_DURATION);
        animationSet.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animationSet);
                bind.include.toolBar.clearAnimation();
            }
        });
        bind.include.toolBar.clearAnimation();
        bind.include.toolBar.setVisibility(View.INVISIBLE);
        bind.include.toolBar.startAnimation(animationSet);
    }

    /**
     * 底部进入动画
     */
    private void footerInAnimation() {
        if (bind.footer.getVisibility() == View.VISIBLE) {
            Timber.e("footer当前为可见状态不执行动画");
            return;
        }
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, bind.footer.getHeight() / 3.0f, 0);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setInterpolator(new AccelerateInterpolator()); // 加速
        // animationSet.setInterpolator(new DecelerateInterpolator()); // 减速
        animationSet.setDuration(ANIMATION_DURATION);
        animationSet.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animationSet);
                bind.footer.clearAnimation();
            }
        });
        bind.footer.clearAnimation();
        bind.footer.setVisibility(View.VISIBLE);
        bind.footer.startAnimation(animationSet);
    }

    /**
     * 底部退出动画
     */
    private void footerOutAnimation() {
        if (bind.footer.getVisibility() != View.VISIBLE) {
            return;
        }
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, 0, bind.footer.getHeight() / 3.0f);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setInterpolator(new AccelerateInterpolator()); // 加速
        // animationSet.setInterpolator(new DecelerateInterpolator()); // 减速
        animationSet.setDuration(ANIMATION_DURATION);
        animationSet.setAnimationListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                super.onAnimationEnd(animationSet);
                bind.footer.clearAnimation();
            }
        });
        bind.footer.clearAnimation();
        bind.footer.setVisibility(View.INVISIBLE);
        bind.footer.startAnimation(animationSet);
    }

    /**
     * 计算图片边界
     *
     * @param gridView
     * @param position
     * @param imageViewId
     * @param offset
     * @return
     */
    public static Rect computeBounds(AbsListView gridView, int position, @IdRes int imageViewId, int offset) {
        int firstVisiblePosition = gridView.getFirstVisiblePosition();
        View itemView = gridView.getChildAt(position - firstVisiblePosition);
        Rect bounds = new Rect();
        if (itemView != null) {
            View thumbView = itemView.findViewById(imageViewId);
            thumbView.getGlobalVisibleRect(bounds);
            if (offset != 0) {
                bounds.set(bounds.left, bounds.top + offset, bounds.right, bounds.bottom + offset);
            }
        }
        return bounds;
    }

    /**
     * 这里不会隐藏底部导航栏
     * <p>
     * 设置内容显示到状态栏下层,并使状态栏透明
     * {@link Build.VERSION_CODES#KITKAT}以上系统调用此方法,可以是状态栏透明,
     * 并使得Activity内容显示在状态栏下层,内容被状态栏覆盖
     */
    public void setTransparentForWindow() {
        final Window window = requireActivity().getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住。
            // 如果需要隐藏底部导航栏加上这个 View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            window.getDecorView()
                    .setSystemUiVisibility(window.getDecorView().getSystemUiVisibility()
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN /*|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION*/);
            window.setStatusBarColor(Color.TRANSPARENT);
            // getWindow().setNavigationBarColor(Color.TRANSPARENT);

            // todo 白色状态栏图标用这个 并在 setContentView 之前调用 setStatusBarTransparent 方法
            // UiUtils.requestStatusBarLight(this, true);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住。
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }
}
