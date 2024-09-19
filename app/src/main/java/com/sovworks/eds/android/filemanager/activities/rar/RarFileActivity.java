package com.sovworks.eds.android.filemanager.activities.rar;

import static com.sovworks.eds.android.providers.MainContentProviderBase.getLocationFromProviderUri;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.activities.zip.RarFilePathAdapter;
import com.sovworks.eds.android.filemanager.activities.zip.TreeData;
import com.sovworks.eds.android.service.FileOpsService;
import com.sovworks.eds.fs.encfs.File;
import com.sovworks.eds.fs.util.StringPathUtil;
import com.sovworks.eds.fs.util.Util;
import com.sovworks.eds.locations.Location;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RarFileActivity extends AppCompatActivity {

    /**
     * 消息相关
     **/
    private static final int HANDLER_SHOW_UN_ALL_FILE_LIST = 100;
    /**
     * 控件相关
     **/
    private ImageView ivBack;
    private TextView tvPath;
    private RecyclerView rvFilePath;
    private RecyclerView rvFileList;
    private ProgressBar progressBar;

    /**
     * 当前节点
     */
    private TreeData<FileHeader> currentPath;

    /**
     * 路径列表
     **/
    private final List<PathEntity> pathList = new ArrayList<>();

    /**
     * 文件列表
     **/
    private final List<TreeData<FileHeader>> fileList = new ArrayList<>();

    /**
     * 路径列表适配器
     **/
    private RarFilePathAdapter pathListAdapter;

    /**
     * 文件列表适配器
     **/
    private RarFileListAdapter fileListAdapter;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == HANDLER_SHOW_UN_ALL_FILE_LIST) {
                progressBar.setVisibility(View.GONE);
                fileList.clear();
                fileList.addAll((List<TreeData<FileHeader>>) msg.obj);
                sortFileModelsList();
            }
        }
    };
    private Archive archive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Uri url = getIntent().getExtras().getParcelable("uri");
        setContentView(R.layout.activity_ziprar_file);
        initView();
        setAdapter();
        initListener();
        getZipOrRarFile(url);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (archive != null) {
                archive.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setAdapter() {
        fileListAdapter = new RarFileListAdapter(R.layout.file_list_item, fileList, archive);
        rvFileList.setAdapter(fileListAdapter);
        pathListAdapter = new RarFilePathAdapter(R.layout.file_path_item, pathList);
        rvFilePath.setAdapter(pathListAdapter);
    }

    /**
     * 获取zip/rar文件
     */
    @SuppressLint("CheckResult")
    private void getZipOrRarFile(Uri url) {
        progressBar.setVisibility(View.VISIBLE);
        Observable.just("url")
                // .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(uri -> {
                    try {
                        // 获取输入流
                        Location loc = getLocationFromProviderUri(RarFileActivity.this, url);
                        File.AccessMode am = Util.getAccessModeFromString("r");
                        if (!loc.getCurrentPath().isFile() && am == File.AccessMode.Read) {
                            throw new FileNotFoundException();
                        }
                        com.sovworks.eds.fs.File f = loc.getCurrentPath().getFile();
                        InputStream stream = f.getInputStream();

                        archive = new Archive(stream);
                        fileListAdapter.setArchive(archive);
                        List<FileHeader> fileHeaders = archive.getFileHeaders();
                        // 读取目录结构
                        TreeData<FileHeader> forest = new TreeData<>(null);
                        fileHeaders.forEach(FileHeader -> {
                            if (FileHeader != null) {
                                TreeData<FileHeader> temp = forest;
                                for (String data : FileHeader.getFileName().split("\\\\")) {
                                    temp = temp.addAndReturnChild(data);
                                }
                                temp.setData(FileHeader);
                            }
                        });
                        return forest;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(root -> {
                    // 顶部路径
                    PathEntity rootEntity = new PathEntity();
                    rootEntity.setName("根目录");
                    rootEntity.setNode(root);
                    pathList.clear();
                    pathList.add(rootEntity);

                    // 刷新列表
                    currentPath = root;
                    layeredShowByPath(root);
                });
    }

    /**
     * 根据路径打开的zip/rar文件预览
     *
     * @param node
     */
    private void layeredShowByPath(final TreeData<FileHeader> node) {
        // 显示路径
        runOnUiThread(this::showRecyclerViewPath);

        List<TreeData<FileHeader>> list = new ArrayList<>(node.getChildren());

        Message msg = mHandler.obtainMessage();
        msg.what = HANDLER_SHOW_UN_ALL_FILE_LIST;
        msg.obj = list;
        mHandler.sendMessage(msg);
    }


    /**
     * 显示路径
     */
    private void showRecyclerViewPath() {
        if (pathList.isEmpty()) {
            tvPath.setVisibility(View.VISIBLE);
            rvFilePath.setVisibility(View.GONE);
        } else {
            tvPath.setVisibility(View.GONE);
            rvFilePath.setVisibility(View.VISIBLE);
        }
    }

    private final OnBackPressedCallback _backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            goBack();
        }
    };

    private void initListener() {
        getOnBackPressedDispatcher().addCallback(_backPressedCallback);
        ivBack.setOnClickListener(v -> goBack());

        fileListAdapter.setOnItemClickListener((adapter, view, position) -> {
            final TreeData<FileHeader> fileModel = (TreeData<FileHeader>) adapter.getData().get(position);
            // 如果预览是文件
            if (fileModel.getChildren().isEmpty()) {
                // 获取目录下所有文件
                List<TreeData<FileHeader>> list = new ArrayList<>(fileModel.getParent().getChildren());
                if (list.isEmpty()) {
                    return;
                }
                // 获取所有图片
                List<TreeData<FileHeader>> images = new ArrayList<>();
                list.forEach(fileHeaderTreeData -> {
                    String mime1 = FileOpsService.getMimeTypeFromExtension(this,
                            new StringPathUtil(fileHeaderTreeData.getName()).getFileExtension());
                    if (mime1.startsWith("image/")) {
                        images.add(fileHeaderTreeData);
                    }
                });
                if (images.isEmpty()) {
                    return;
                }
                // 排序
                images.sort((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
                int index = images.indexOf(fileModel);

                new StfalconImageViewer.Builder<>(RarFileActivity.this, images, (imageView, image) ->
                        Glide.with(RarFileActivity.this)
                                .load(new RarWrap(archive, image.getData()))
                                .into(imageView))
                        .withStartPosition(index)
                        .show(true);
            }
            // 如果预览是文件夹
            else {
                currentPath = fileModel;

                // 顶部路径处理
                PathEntity entity = new PathEntity();
                entity.setName(currentPath.getName());
                entity.setNode(fileModel);
                pathList.add(entity);

                layeredShowByPath(fileModel);
            }
        });
        // 文件路径点击事件
        pathListAdapter.setOnItemClickListener((adapter, view, position) -> {
            if (pathList != null && !pathList.isEmpty() && pathList.size() > position) {
                if (position == pathList.size() - 1) {
                    return;
                }

                // 当前节点
                currentPath = pathList.get(position).getNode();

                // 当前路径列表
                int size = pathList.size();
                if (size > position + 1) {
                    // subList 返回仅仅只是一个视图, 对 subList 操作也会影响到原来的列表, 如这里的清空 subList 会将 原来列表的相同元素清空
                    pathList.subList(position + 1, size).clear();
                }
                layeredShowByPath(currentPath);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // goBack();
    }


    /**
     * 返回上一级
     */
    private void goBack() {
        TreeData<FileHeader> parent = currentPath.getParent();
        if (parent != null) {
            currentPath = parent;
            pathList.remove(pathList.size() - 1);
            layeredShowByPath(parent);
        } else {
            finish();
        }
    }

    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        tvPath = findViewById(R.id.tv_path);
        progressBar = findViewById(R.id.progress_bar);
        rvFileList = findViewById(R.id.rv_file_list);
        rvFileList.setLayoutManager(new LinearLayoutManager(this));
        rvFilePath = findViewById(R.id.rv_file_path);
        rvFilePath.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    /**
     * 排序
     */
    private void sortFileModelsList() {
        // 文件排序
        /*FileUtils.sortFileModelList(showZipRarFileList, "fileName", true);
        fileListAdapter.replaceData(showZipRarFileList);
        fileListAdapter.notifyDataSetChanged();
        // 路径刷新
        filePathAdapter.replaceData(filePathList);
        filePathAdapter.notifyDataSetChanged();*/
        fileListAdapter.notifyDataSetChanged();
        pathListAdapter.notifyDataSetChanged();
    }
}


