package cn.shu.wechat.swing.tasks;

import cn.shu.wechat.swing.utils.HttpUtil;

/**
 * Created by 舒新胜 on 16/06/2017.
 */
public class DownloadTask extends HttpTask {
    HttpUtil.ProgressListener progressListener;

    public DownloadTask(HttpUtil.ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public void execute(String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] data = HttpUtil.download(url, null, null, progressListener);
                    if (listener != null) {
                        listener.onSuccess(data);
                    }
                } catch (Exception e) {
                    if (listener != null) {
                        listener.onFailed();
                    }
                }
            }
        }).start();
    }
}
