package com.sovworks.eds.android.filemanager.activities.zip;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sovworks.eds.android.R;

import java.util.List;

/**
 * 文件路径适配器
 *
 * @author Andy.R
 */
public class FilePathAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public FilePathAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String path) {
        helper.setText(R.id.tv_value, path);
    }
}
