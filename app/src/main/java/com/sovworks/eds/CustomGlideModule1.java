package com.sovworks.eds;

import static com.bumptech.glide.load.DataSource.LOCAL;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.MultiModelLoaderFactory;
import com.bumptech.glide.module.LibraryGlideModule;
import com.sovworks.eds.android.filemanager.activities.rar.RarWrap;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;

import timber.log.Timber;

@GlideModule
public class CustomGlideModule1 extends LibraryGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        // 指定Model类型为Picture的处理方式
        registry.append(RarWrap.class, InputStream.class, new MyModelLoader.LoaderFactory());
    }

    /**
     * 清单解析的开启
     * <p>
     * 这里不开启，避免添加相同的modules两次
     *
     * @return {@link Boolean}
     */
    /*@Override
    public boolean isManifestParsingEnabled() {
        return false;
    }*/

    public static class MyModelLoader implements ModelLoader<RarWrap, InputStream> {

        public MyModelLoader() {
        }

        @Nullable
        @Override
        public LoadData<InputStream> buildLoadData(@NonNull RarWrap model,
                                                   int width,
                                                   int height,
                                                   @NonNull Options options) {
            return new LoadData<>(new MyKey(model), new MyDataFetcher(model));
        }

        @Override
        public boolean handles(@NonNull RarWrap s) {
            return true;
        }

        public static class LoaderFactory implements ModelLoaderFactory<RarWrap, InputStream> {

            public LoaderFactory() {
            }

            @NonNull
            @Override
            public ModelLoader<RarWrap, InputStream> build(@NonNull MultiModelLoaderFactory multiFactory) {
                return new MyModelLoader();
            }

            @Override
            public void teardown() {

            }
        }

        public static class MyKey implements Key {
            RarWrap path;

            public MyKey(RarWrap path) {
                this.path = path;
            }

            @Override
            public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
                // 缓存使用的Key,唯一标识文件对应的缓存
                messageDigest.update((path.getFileHeader().getFileName()).getBytes(CHARSET));
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }

                MyKey myKey = (MyKey) o;
                return Objects.equals(path, myKey.path);
            }

            @Override
            public int hashCode() {
                return path != null ? path.hashCode() : 0;
            }
        }

        public static class MyDataFetcher implements DataFetcher<InputStream> {

            private final RarWrap file;
            private boolean isCanceled;
            InputStream mInputStream = null;

            public MyDataFetcher(RarWrap file) {
                this.file = file;
            }

            @Override
            public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super InputStream> callback) {
                if (!isCanceled) {
                    try {
                        // long start = System.currentTimeMillis();
                        mInputStream = file.getArchive().getInputStream(file.getFileHeader());
                        callback.onDataReady(mInputStream);
                        // Timber.e("耗时%s", System.currentTimeMillis() - start);
                    } catch (IOException e) {
                        Timber.e(e);
                        callback.onLoadFailed(e);
                    }
                } else {
                    callback.onDataReady(null);
                }
            }

            @Override
            public void cleanup() {
                if (mInputStream != null) {
                    try {
                        mInputStream.close();
                    } catch (IOException e) {
                        Timber.e(e);
                    }
                }
            }

            @Override
            public void cancel() {
                isCanceled = true;
            }

            @NonNull
            @Override
            public Class<InputStream> getDataClass() {
                return InputStream.class;
            }

            @NonNull
            @Override
            public DataSource getDataSource() {
                return LOCAL;
            }
        }
    }
}