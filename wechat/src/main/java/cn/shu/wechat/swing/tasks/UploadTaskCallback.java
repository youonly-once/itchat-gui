package cn.shu.wechat.swing.tasks;

import cn.shu.wechat.beans.msg.send.WebWXUploadMediaResponse;

/**
 * Created by 舒新胜 on 15/06/2017.
 */
public interface UploadTaskCallback {


    void onTaskSuccess(int curr, int size);

    void onTaskError();
}