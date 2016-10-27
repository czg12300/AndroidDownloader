package com.jake.downloader;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.jake.library.Downloader;

/**
 * description：
 *
 * @author Administrator
 * @since 2016/10/26 21:29
 */


public class DownloadAdapter extends BaseListAdapter<FileInfo, DownloadAdapter.ViewHolder> {
    public DownloadAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FileInfo info = getItem(position);
        Glide.with(mContext).load(info.icon).into(holder.ivIcon);
        holder.tvName.setText(info.name);
        holder.btnOpt.setTag(info);
        holder.progressBar.setTag(info);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(View.inflate(mContext, R.layout.item_download, null));
    }

    static class ViewHolder extends BaseViewHolder {
        ImageView ivIcon;
        ProgressBar progressBar;
        TextView tvName;
        Button btnOpt;

        public ViewHolder(View itemView) {
            super(itemView);
            ivIcon = (ImageView) findViewById(R.id.iv_icon);
            progressBar = (ProgressBar) findViewById(R.id.pb);
            tvName = (TextView) findViewById(R.id.tv_name);
            btnOpt = (Button) findViewById(R.id.btn_opt);
            btnOpt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FileInfo info = (FileInfo) v.getTag();
                    if (info != null) {
                        if (!info.isDownloading) {
                            btnOpt.setText("暂停");
                            btnOpt.setTextColor(Color.parseColor("#FF4081"));
                            Downloader.getInstance().download(info.url, info.name + ".apk", null);
                            info.isDownloading = true;
                        } else {
                            btnOpt.setText("继续");
                            btnOpt.setTextColor(Color.parseColor("#333333"));
                            Downloader.getInstance().stop(info.url, info.name + ".apk", null);
                            info.isDownloading = false;
                        }
                    }
                }
            });

        }
    }
}
