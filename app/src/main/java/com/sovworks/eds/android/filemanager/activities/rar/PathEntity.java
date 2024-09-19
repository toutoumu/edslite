package com.sovworks.eds.android.filemanager.activities.rar;

import com.github.junrar.rarfile.FileHeader;
import com.sovworks.eds.android.filemanager.activities.zip.TreeData;

/**
 * rar文件目录
 */
public class PathEntity {

    // 目录名称
    private String name;
    // 目录对象
    private TreeData<FileHeader> node;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TreeData<FileHeader> getNode() {
        return node;
    }

    public void setNode(TreeData<FileHeader> node) {
        this.node = node;
    }
}
