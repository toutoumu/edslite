package com.sovworks.eds.android.filemanager.activities.zip;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sovworks.eds.android.R;

import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.util.Zip4jUtil;

import java.util.Date;
import java.util.List;

/**
 * 文件列表适配器
 *
 * @author Andy.R
 */
public class ZipFileListAdapter extends BaseQuickAdapter<TreeData<LocalFileHeader>, BaseViewHolder> {

    public ZipFileListAdapter(int layoutResId, @Nullable List<TreeData<LocalFileHeader>> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, TreeData<LocalFileHeader> itemFileModel) {
        viewHolderHelper(helper, itemFileModel);
    }

    /**
     * viewHolder绑定数据
     *
     * @param helper
     * @param itemFileModel
     */
    private void viewHolderHelper(BaseViewHolder helper, TreeData<LocalFileHeader> itemFileModel) {
        if (itemFileModel != null) {
            if (itemFileModel.getData() != null) {
                // 修改日期
                Date date = new Date(Zip4jUtil.dosToExtendedEpochTme(itemFileModel.getData().getLastModifiedTime()));
                String formatDate = DateUtils.getDateToString(helper.itemView.getContext(), date);
                helper.setText(R.id.tv_file_date, formatDate);

                // 文件大小
                long fileSize = itemFileModel.getData().getUncompressedSize();
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
            // 文件图标
            helper.setImageResource(R.id.iv_file_icon, FileUtils.getFileIcon(false, itemFileModel.getName()));
        }
    }
}
