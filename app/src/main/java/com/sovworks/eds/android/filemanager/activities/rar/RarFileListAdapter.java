package com.sovworks.eds.android.filemanager.activities.rar;

import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.activities.zip.DateUtils;
import com.sovworks.eds.android.filemanager.activities.zip.FileUtils;
import com.sovworks.eds.android.filemanager.activities.zip.TreeData;
import com.sovworks.eds.android.service.FileOpsService;
import com.sovworks.eds.fs.util.StringPathUtil;

import java.util.Date;
import java.util.List;

/**
 * 文件列表适配器
 *
 * @author Andy.R
 */
public class RarFileListAdapter extends BaseQuickAdapter<TreeData<FileHeader>, BaseViewHolder> {

    private Archive archive;

    public RarFileListAdapter(int layoutResId, @Nullable List<TreeData<FileHeader>> data, Archive archive) {
        super(layoutResId, data);
        this.archive = archive;
    }

    public void setArchive(Archive archive) {
        this.archive = archive;
    }

    @Override
    protected void convert(BaseViewHolder helper, TreeData<FileHeader> itemFileModel) {
        viewHolderHelper(helper, itemFileModel);
    }

    /**
     * viewHolder绑定数据
     *
     * @param helper
     * @param itemFileModel
     */
    private void viewHolderHelper(BaseViewHolder helper, TreeData<FileHeader> itemFileModel) {
        if (itemFileModel != null) {
            if (itemFileModel.getData() != null) {
                // 修改日期
                Date date = itemFileModel.getData().getMTime();
                String formatDate = DateUtils.getDateToString(helper.itemView.getContext(), date);
                helper.setText(R.id.tv_file_date, formatDate);

                // 文件大小
                long fileSize = itemFileModel.getData().getFullUnpackSize();
                String formatSize = FileUtils.formatFileSize(fileSize);
                helper.setText(R.id.tv_file_size, formatSize);

                // 是否文件
                if (itemFileModel.getData().isDirectory()) {
                    helper.setVisible(R.id.tv_file_size, false);
                } else {
                    helper.setVisible(R.id.tv_file_size, true);
                }
            }

            // 文件名
            helper.setText(R.id.tv_file_name, itemFileModel.getName());
            String mime1 = FileOpsService.getMimeTypeFromExtension(mContext,
                    new StringPathUtil(itemFileModel.getName()).getFileExtension());
            // icon
            if (mime1.startsWith("image/")) {
                ImageView imageView = helper.getView(R.id.iv_file_icon);
                Glide.with(helper.itemView.getContext())
                        .load(new RarWrap(archive, itemFileModel.getData()))
                        .into(imageView);
            } else {
                // 文件图标
                helper.setImageResource(R.id.iv_file_icon, FileUtils.getFileIcon(false, itemFileModel.getName()));
            }
        }
    }
}
