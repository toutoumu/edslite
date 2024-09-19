package com.sovworks.eds.android.filemanager.activities.rar;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;

public class RarWrap {
    private Archive archive;
    private FileHeader fileHeader;

    public RarWrap(Archive archive, FileHeader fileHeader) {
        this.archive = archive;
        this.fileHeader = fileHeader;
    }

    public Archive getArchive() {
        return archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    public FileHeader getFileHeader() {
        return fileHeader;
    }

    public void setFileHeader(FileHeader fileHeader) {
        this.fileHeader = fileHeader;
    }
}
