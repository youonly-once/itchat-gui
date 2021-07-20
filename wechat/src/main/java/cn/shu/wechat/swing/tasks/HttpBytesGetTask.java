package cn.shu.wechat.swing.tasks;

import cn.shu.wechat.swing.utils.HttpUtil;

import java.io.IOException;

/**
 * Created by 舒新胜 on 2017/6/13.
 */
public class HttpBytesGetTask extends HttpTask {
    @Override
    public void execute(String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] ret = HttpUtil.getBytes(url, headers, requestParams);
                    if (listener != null) {
                        listener.onSuccess(ret);
                    }
                } catch (IOException e) {
                    if (listener != null) {
                        listener.onFailed();
                    }
                }


            }
        }).start();
    }
}
