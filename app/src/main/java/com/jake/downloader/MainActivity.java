package com.jake.downloader;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.jake.library.Downloader;
import com.jake.library.IDownloadListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IDownloadListener {
    private ListView mListView;
    private DownloadAdapter mDownloadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Downloader.getInstance().addDownloadListener(this);
        setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.lv_list);
        mDownloadAdapter = new DownloadAdapter(this);
        ArrayList<FileInfo> list = new ArrayList<>();
        list.add(new FileInfo("淘宝", "http://up.apk8.com/small1/1439970241775.jpg", "http://download.apk8.com/soft/2015/%E6%B7%98%E5%AE%9D.apk"));
        list.add(new FileInfo("爱奇艺", "http://up.apk8.com/small1/1439969258974.jpg", "http://183.56.150.169/imtt.dd.qq.com/16891/4C17EF01EDAC64DC9171EBDA780D1FB6.apk?mkey=58109f81d13118cf&f=8f5d&c=0&fsname=com.qiyi.video_7.9.1_80795.apk&csr=2097&_track_d99957f7=c2d8302c-178c-4ae8-9f7a-4f40d7f26b8a&p=.apk"));
        list.add(new FileInfo("创世战纪", "http://up.apk8.com/small3/1473326895610.png", "http://119.147.33.13/imtt.dd.qq.com/16891/AD118566846E91FA3E883129027AB8A4.apk?mkey=58109f12d13118cf&f=8a5d&c=0&fsname=com.pp.assistant_5.0.0_1603.apk&csr=2097&_track_d99957f7=7211c244-c716-47c3-9fd8-3ec427ae651e&p=.apk"));
        list.add(new FileInfo("三国威力加强版", "http://up.apk8.com/small3/1473410785728.png", "http://183.61.13.174/imtt.dd.qq.com/16891/AD118566846E91FA3E883129027AB8A4.apk?mkey=58109cd0123218cf&f=d688&c=0&fsname=com.pp.assistant_5.0.0_1603.apk&csr=2097&_track_d99957f7=691005c8-6793-4891-ae8d-2d408c23a294&p=.apk"));
        list.add(new FileInfo("围攻大菠萝", "http://up.apk8.com/small3/1473151117450.png", "http://183.61.13.174/imtt.dd.qq.com/16891/AD118566846E91FA3E883129027AB8A4.apk?mkey=58109ca6d03118cf&f=d488&c=0&fsname=com.pp.assistant_5.0.0_1603.apk&csr=2097&_track_d99957f7=fbc15a92-f21e-474b-999e-7b8efc68a38e&p=.apk"));
        mDownloadAdapter.set(list);
        mListView.setAdapter(mDownloadAdapter);
    }

    @Override
    public void onSuccess(String url, String filePath) {
        int len = mListView.getChildCount();
        for (int i = 0; i < len; i++) {
            View item = mListView.getChildAt(i);
            Button btn = (Button) item.findViewById(R.id.btn_opt);
            FileInfo info = (FileInfo) btn.getTag();
            if (TextUtils.equals(info.url, url)) {
                btn.setTextColor(Color.parseColor("#eee"));
                btn.setText("完成");
                return;
            }
        }
    }

    @Override
    public void onFail(String url) {
        int len = mListView.getChildCount();
        for (int i = 0; i < len; i++) {
            View item = mListView.getChildAt(i);
            Button btn = (Button) item.findViewById(R.id.btn_opt);
            FileInfo info = (FileInfo) btn.getTag();
            if (TextUtils.equals(info.url, url)) {
                btn.setTextColor(Color.parseColor("#FF0000"));
                btn.setText("失败");
                return;
            }
        }
    }

    @Override
    public void onStop(String url) {
        int len = mListView.getChildCount();
        for (int i = 0; i < len; i++) {
            View item = mListView.getChildAt(i);
            Button btn = (Button) item.findViewById(R.id.btn_opt);
            FileInfo info = (FileInfo) btn.getTag();
            if (TextUtils.equals(info.url, url)) {
                btn.setTextColor(Color.parseColor("#eee"));
                btn.setText("停止");
                return;
            }
        }
    }

    @Override
    public void onProgress(String url, long positionSize, long totalSize) {
        int len = mListView.getChildCount();
        for (int i = 0; i < len; i++) {
            View item = mListView.getChildAt(i);
            ProgressBar progressBar = (ProgressBar) item.findViewById(R.id.pb);
            FileInfo info = (FileInfo) progressBar.getTag();
            if (TextUtils.equals(info.url, url)) {
                progressBar.setProgress((int) (positionSize * 100 / totalSize));
                return;
            }
        }
    }
}
