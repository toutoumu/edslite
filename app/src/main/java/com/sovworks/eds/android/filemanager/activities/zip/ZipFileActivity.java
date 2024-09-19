package com.sovworks.eds.android.filemanager.activities.zip;

import static com.sovworks.eds.android.providers.MainContentProviderBase.getLocationFromProviderUri;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sovworks.eds.android.R;
import com.sovworks.eds.fs.File;
import com.sovworks.eds.fs.util.Util;
import com.sovworks.eds.locations.Location;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.model.LocalFileHeader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class ZipFileActivity extends AppCompatActivity {

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
     * 当前路径
     */
    private String strCurrectPath = "";
    private TreeData<LocalFileHeader> currentPath;
    /**
     * 用来显示的数据集合
     **/
    private final List<TreeData<LocalFileHeader>> fileList = new ArrayList<>();
    /**
     * 路径集合
     **/
    private final List<String> filePathList = new ArrayList<>();
    /**
     * 文件列表适配器
     **/
    private ZipFileListAdapter fileListAdapter;
    /**
     * 文件路径适配器
     **/
    private FilePathAdapter filePathAdapter;


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case HANDLER_SHOW_UN_ALL_FILE_LIST:
                    progressBar.setVisibility(View.GONE);
                    fileList.clear();
                    fileList.addAll((List<TreeData<LocalFileHeader>>) msg.obj);
                    sortFileModelsList();
                    break;
            }
        }
    };
    private ZipInputStream inputStream;

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

    private void setAdapter() {
        fileListAdapter = new ZipFileListAdapter(R.layout.file_list_item, fileList);
        rvFileList.setAdapter(fileListAdapter);
        filePathAdapter = new FilePathAdapter(R.layout.file_path_item, filePathList);
        rvFilePath.setAdapter(filePathAdapter);
    }

    /**
     * 获取zip/rar文件
     */
    @SuppressLint("CheckResult")
    private void getZipOrRarFile(Uri url) {
        progressBar.setVisibility(View.VISIBLE);
        Observable.just(url)
                // .observeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .map(uri -> {
                    try {
                        // 获取输入流
                        Location loc = getLocationFromProviderUri(ZipFileActivity.this, url);
                        File.AccessMode am = Util.getAccessModeFromString("r");
                        if (!loc.getCurrentPath().isFile() && am == File.AccessMode.Read) {
                            throw new FileNotFoundException();
                        }
                        File f = loc.getCurrentPath().getFile();
                        InputStream stream = f.getInputStream();

                        inputStream = new ZipInputStream(stream, Charset.forName("gb2312"));
                        // 读取目录结构
                        TreeData<LocalFileHeader> forest = new TreeData<>(null);
                        LocalFileHeader entry = inputStream.getNextEntry();
                        while (entry != null) {
                            entry = inputStream.getNextEntry();
                            if (entry != null) {
                                TreeData<LocalFileHeader> temp = forest;
                                for (String data : entry.getFileName().split("/")) {
                                    temp = temp.addAndReturnChild(data);
                                }
                                temp.setData(entry);
                            }
                        }
                        inputStream.close();
                        return forest;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<TreeData<LocalFileHeader>>() {
                    @Override
                    public void accept(TreeData<LocalFileHeader> data) {
                        readZipOrRarFileContentList(data);
                    }
                });
    }

    /**
     * 读取zip/rar文件内容列表
     *
     * @param root 文件跟节点
     */
    private void readZipOrRarFileContentList(final TreeData<LocalFileHeader> root) {
        currentPath = root;
        strCurrectPath = "";
        layeredShowByPath(root, "");
    }

    /**
     * 根据路径打开的zip/rar文件预览
     *
     * @param zipRarFileList
     * @param zipRarFileInnerPath
     */
    private void layeredShowByPath(final TreeData<LocalFileHeader> zipRarFileList, final String zipRarFileInnerPath) {
        // 显示路径
        runOnUiThread(() -> showRecyclerViewPath(zipRarFileInnerPath));

        List<TreeData<LocalFileHeader>> list = new ArrayList<>(zipRarFileList.getChildren());

        Message msg = mHandler.obtainMessage();
        msg.what = HANDLER_SHOW_UN_ALL_FILE_LIST;
        msg.obj = list;
        mHandler.sendMessage(msg);
    }


    /**
     * 显示路径
     *
     * @param rarFileInnerPath rar文件内部路径
     */
    private void showRecyclerViewPath(String rarFileInnerPath) {
        if (TextUtils.isEmpty(rarFileInnerPath)) {
            tvPath.setVisibility(View.VISIBLE);
            rvFilePath.setVisibility(View.GONE);
        } else {
            tvPath.setVisibility(View.GONE);
            rvFilePath.setVisibility(View.VISIBLE);
            filePathList.clear();
            if (rarFileInnerPath.contains("\\")) {
                String[] splitPath = rarFileInnerPath.split("\\\\");
                filePathList.addAll(Arrays.asList(splitPath));
            } else {
                filePathList.add(rarFileInnerPath);
            }
        }
    }

    private final OnBackPressedCallback _backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // 非选择模式处理
            goBack();
        }
    };

    private void initListener() {
        getOnBackPressedDispatcher().addCallback(_backPressedCallback);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        /*btnUnRar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ZipRarFileActivity.this, PathSelectedActivity.class);
                startActivityForResult(intent, 100);
            }
        });*/
        fileListAdapter.setOnItemClickListener((adapter, view, position) -> {
            final TreeData<LocalFileHeader> fileModel = (TreeData<LocalFileHeader>) adapter.getData().get(position);
            // 如果预览是文件夹
            if (fileModel.getChildren().isEmpty()) {
                Toast.makeText(ZipFileActivity.this, fileModel.getName(), Toast.LENGTH_LONG).show();
                if (fileModel.getData() != null) {
                    // inputStream.getNextEntry(fileModel.getData(), true);
                }
            }
            // 如果预览是文件
            else {
                currentPath = fileModel;
                strCurrectPath = strCurrectPath + "/" + fileModel.getName();
                layeredShowByPath(fileModel, strCurrectPath);
            }
        });
        /*filePathAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                if (filePathList != null && filePathList.size() > 0 && filePathList.size() > position) {
                    if (position == filePathList.size() - 1) {
                        return;
                    }
                    String rarFileInnerPath;
                    // 修改路径判断多层路径
                    if (position > 0) {
                        StringBuilder strBuilderPath = new StringBuilder();
                        for (int i = 0; i < position + 1; i++) {
                            strBuilderPath.append(filePathList.get(i)).append("\\");
                        }
                        rarFileInnerPath = strBuilderPath.substring(0, strBuilderPath.length() - 1);
                    } else {
                        rarFileInnerPath = filePathList.get(position);
                    }
                    // 用于区分选择的层级
                    splitIndex = position + 3;
                    layeredShowByPath(zipOrRarFileModelList, rarFileInnerPath);
                }
            }
        });*/
    }

    @Override
    public void onBackPressed() {
        goBack();
    }


    /**
     * 返回上一级
     */
    private void goBack() {
        TreeData<LocalFileHeader> parent = currentPath.getParent();
        if (parent != null) {
            currentPath = parent;
            strCurrectPath = strCurrectPath.substring(0, strCurrectPath.lastIndexOf("/"));
            layeredShowByPath(parent, strCurrectPath);
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
        filePathAdapter.notifyDataSetChanged();
    }

    /**
     * 解压全部文件
     *
     * @param selectedPath
     * @param password
     */
    /* public void unZipRarAllFile(final String selectedPath, final String password) {
        progressBar.setVisibility(View.VISIBLE);
        ThreadManager.getInstance().start(new Runnable() {
            @Override
            public void run() {
                String unAllFilePath;
                if (zipRarFilePath.endsWith("rar")) {
                    unAllFilePath = UnRarManager.getInstance().unRarAllFile(zipRarFilePath,
                            selectedPath, password);
                } else {
                    unAllFilePath = UnZipManager.getInstance().unZipAllFile(zipRarFilePath, selectedPath, password);
                }
                Message msg = mHandler.obtainMessage();
                msg.what = HANDLER_UN_ALL_FILE;
                msg.obj = unAllFilePath;
                mHandler.sendMessage(msg);
            }
        });
    } */


    /**
     * 弹出Dialog 让用户输入密码
     *
     * @param outFolerPath
     * @param fileModelPath
     */
    private void getUnZipPassword(final String outFolerPath, final String fileModelPath) {
        // new PasswordDialog(this, R.style.DialogTheme, zipRarFilePath, outFolerPath, fileModelPath).show();
    }

    /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String selectedPath = data.getStringExtra("selectedPath");
            selectedPath = selectedPath + "/" + FileUtils.getFileNameNoExtension(zipRarFilePath);
            // 判断是否有密码
            boolean hasPassword;
            if (zipRarFilePath.endsWith(".zip")) {
                hasPassword = UnZipManager.getInstance().checkZipFileHasPassword(zipRarFilePath);
            } else {
                hasPassword = UnRarManager.getInstance().checkRarFileHasPassword(new File(zipRarFilePath));
            }
            if (hasPassword) {
                getUnZipPassword(selectedPath, "");
            } else {
                unZipRarAllFile(selectedPath, "");
            }
        }
    } */
}
