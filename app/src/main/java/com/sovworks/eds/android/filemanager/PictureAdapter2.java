package com.sovworks.eds.android.filemanager;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView1;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.fragments.FileListViewFragment;
import com.sovworks.eds.android.helpers.CachedPathInfo;

import java.util.List;

public class PictureAdapter2 extends RecyclerView.Adapter<PictureAdapter2.HorizontalVpViewHolder> {
    private final List<CachedPathInfo> pictures;
    private final FileListViewFragment mActivity;
    private final RequestOptions mRequestOptions;

    public PictureAdapter2(FileListViewFragment activity, List<CachedPathInfo> pictures) {
        this.mActivity = activity;
        this.pictures = pictures;

        // Glide 4.x 加载数据
        mRequestOptions = RequestOptions.fitCenterTransform()
                // .placeholder(R.drawable.transparent_drawable)
                // .placeholder(R.drawable.ic_default_image)
                // .placeholder(new GlidePlaceholderDrawable(mActivity.getResources(), R.drawable.ic_default_image))
                .override(3968, 3968);
    }

    @NonNull
    @Override
    public HorizontalVpViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HorizontalVpViewHolder(LayoutInflater.from(mActivity.getActivity()).inflate((R.layout.item_photo), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HorizontalVpViewHolder holder, int position) {
        // holder.mDragFrameLayout.setContainer(mActivity.bind.pager);
        // holder.mDragFrameLayout.setImageView(mActivity.bind.smoothImageView);

        holder.mPhotoView.setMaximumScale(10);
        holder.mPhotoView.setMediumScale(4);
        // holder.mPhotoView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.mPhotoView.setOnViewTapListener((view, x, y) -> mActivity.toggleUI());

        // 缩略图, 必须和 smoothImageView 的图片一样的请求,才会使用同一个图片缓存
        // RequestBuilder<Drawable> thumbnailRequest = mActivity.mGlideRequests
        RequestBuilder<Drawable> thumbnailRequest = mActivity.mGlideRequests
                // .load(pictures.get(position).getFilePath())
                .load(pictures.get(position).getPath())
                .apply(mActivity.mThumbnailRequestOptions);

        // 加载图片
        holder.mPhotoView.setLoading(true);
        // mActivity.mGlideRequests.load(pictures.get(position).getFilePath())
        mActivity.mGlideRequests.load(pictures.get(position).getPath())
                .thumbnail(thumbnailRequest)
                .placeholder(R.drawable.transparent_drawable)
                .error(R.drawable.ic_default_image_list)
                .override(3968, 3968)
                .apply(mRequestOptions)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e,
                                                Object model,
                                                Target<Drawable> target,
                                                boolean isFirstResource) {
                        holder.mPhotoView.setLoading(false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource,
                                                   Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        holder.mPhotoView.setLoading(false);
                        return false;
                    }
                })
                .into(holder.mPhotoView);
    }

    @Override
    public int getItemCount() {
        return pictures.size();
    }

    static class HorizontalVpViewHolder extends RecyclerView.ViewHolder {
        // DragFrameLayout mDragFrameLayout;
        PhotoView1 mPhotoView;

        HorizontalVpViewHolder(@NonNull View itemView) {
            super(itemView);
            // mDragFrameLayout = itemView.findViewById(R.id.drag_layout);
            mPhotoView = itemView.findViewById(R.id.photo_view);
        }
    }
}