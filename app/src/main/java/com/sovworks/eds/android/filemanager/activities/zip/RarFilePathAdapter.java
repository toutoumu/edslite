package com.sovworks.eds.android.filemanager.activities.zip;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sovworks.eds.android.R;
import com.sovworks.eds.android.filemanager.activities.rar.PathEntity;

import java.util.List;

public class RarFilePathAdapter extends BaseQuickAdapter<PathEntity, BaseViewHolder> {

    public RarFilePathAdapter(int layoutResId, @Nullable List<PathEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, PathEntity path) {
        helper.setText(R.id.tv_value, path.getName());
    }
}
