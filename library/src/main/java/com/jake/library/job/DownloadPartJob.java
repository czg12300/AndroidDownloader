package com.jake.library.job;

import com.jake.library.DownloadState;
import com.jake.library.db.DownloadPart;
import com.jake.library.db.DownloadPartOperator;
import com.jake.library.utils.LogUtil;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by jakechen on 2016/10/26.
 */

public class DownloadPartJob extends BaseJob {
    private DownloadPart mDownloadPart;

    public DownloadPartJob(DownloadPart part) {
        mDownloadPart = part;
    }

    @Override
    protected void runInThread() {
        LogUtil.d("tag  DownloadPartTask   run() ");
        InputStream inputStream = ;
        BufferedInputStream bis = null;
        try {
            RandomAccessFile accessFile = new RandomAccessFile(mDownloadPart.path, "rwd");
            // 开始下载
            Request request = new Request.Builder().url(mDownloadPart.url)
                    .header("RANGE",
                            "bytes=" + (mDownloadPart.rangeStart + mDownloadPart.positionSize) + "-"
                                    + mDownloadPart.rangeEnd)
                    .build();
            // 文件跳转到指定位置开始写入
            accessFile.seek(mDownloadPart.rangeStart);
            Response response = OkHttpClientManager.getInstance().getOkHttpClient().newCall(request)
                    .execute();
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                mDownloadPart.state = DownloadState.DOWNLOADING;
                // 更新数据库状态
                DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
                // 获得文件流
                inputStream = responseBody.byteStream();
                bis = new BufferedInputStream(inputStream);
                byte[] buffer = new byte[2 * 1024];
                int length = 0;
                int buffOffset = 0;
                final long updateSize = mDownloadPart.totalSize / 20;
                // 开始下载数据库中插入下载信息
                while (!mIsPause && (length = bis.read(buffer)) > 0) {
                    accessFile.write(buffer, 0, length);
                    mDownloadPart.positionSize += length;
                    buffOffset += length;
                    // 以kb计算
                    if (buffOffset >= updateSize) {
                        buffOffset = 0;
                        // 更新数据库
                        DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
                        if (mIDownloadPartListener != null) {
                            mIDownloadPartListener.downloadChange(mDownloadPart);
                        }
                    }
                }
                if (!mIsPause) {
                    mDownloadPart.state = DownloadState.FINISH;
                    DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
                    if (mIDownloadPartListener != null) {
                        mIDownloadPartListener.downloadChange(mDownloadPart);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mIDownloadPartListener != null) {
                mIDownloadPartListener.fail(mDownloadPart);
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DownloadPart getDownloadPart() {
        return mDownloadPart;
    }

    @Override
    public void stop() {
        super.stop();
        DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
    }
}
