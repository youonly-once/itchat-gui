package cn.shu.wechat.swing.tasks;

import cn.shu.wechat.beans.msg.send.WebWXUploadMediaResponse;

/**
 * Created by song on 15/06/2017.
 */
public interface UploadTaskCallback
{


    void onTaskSuccess(int curr, int size, WebWXUploadMediaResponse webWXUploadMediaResponse);

    void onTaskError();
}