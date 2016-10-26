package com.jake.downloader;

import android.view.View;

public class BaseViewHolder {
    public View itemView;

    public BaseViewHolder(View itemView) {
        this.itemView = itemView;
    }

    protected View findViewById(int id) {
        return itemView != null ? itemView.findViewById(id) : null;
    }
}