package com.jake.library.job;

import com.jake.library.DownloadState;
import com.jake.library.Downloader;
import com.jake.library.datafetch.DownloadDataFetch;
import com.jake.library.db.DownloadPart;
import com.jake.library.db.DownloadPartOperator;
import com.jake.library.utils.DownloadUtils;
import com.jake.library.utils.DLog;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URL;

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
        DLog.d("tag  DownloadPartTask   run() ");
        DownloadDataFetch dataFetcher = null;
        BufferedInputStream bis = null;
        try {
            dataFetcher = Downloader.getInstance().getConfiguration().getDataFetch();
            URL url = new URL(mDownloadPart.url);
            RandomAccessFile accessFile = new RandomAccessFile(mDownloadPart.path, "rwd");
            accessFile.seek(mDownloadPart.rangeStart);
            InputStream inputStream = dataFetcher.loadData(url, mDownloadPart.rangeStart, mDownloadPart.rangeEnd);
            if (inputStream != null) {
                // 更新数据库状态
                mDownloadPart.state = DownloadState.DOWNLOADING;
                DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
                // 获得文件流
                bis = new BufferedInputStream(inputStream);
                byte[] buffer = new byte[2 * 1024];
                int length = 0;
                int buffOffset = 0;
                final long updateSize = mDownloadPart.totalSize / 20;
                // 开始下载数据库中插入下载信息
                while (!isStop() && (length = bis.read(buffer)) > 0) {
                    accessFile.write(buffer, 0, length);
                    mDownloadPart.positionSize += length;
                    buffOffset += length;
                    // 以kb计算
                    if (buffOffset >= updateSize) {
                        buffOffset = 0;
                        // 更新数据库
                        DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
                    }
                }
                if (!isStop()) {
                    mDownloadPart.state = DownloadState.FINISH;
                    DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
                } else {
                    mDownloadPart.state = DownloadState.STOP;
                    DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dataFetcher != null) {
                dataFetcher.cleanup();
            }
            DownloadUtils.closeIo(bis);
        }
    }

    public DownloadPart getDownloadPart() {
        return mDownloadPart;
    }

    @Override
    public void stop() {
        super.stop();
        mDownloadPart.state = DownloadState.STOP;
        DownloadPartOperator.getInstance().update(mDownloadPart.id, mDownloadPart);
    }
}
