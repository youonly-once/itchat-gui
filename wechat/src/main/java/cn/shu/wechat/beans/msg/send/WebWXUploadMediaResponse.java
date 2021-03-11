package cn.shu.wechat.beans.msg.send;

import lombok.Data;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 1:56 PM
 */
@Data
public class WebWXUploadMediaResponse {
    /**
     * Copyright 2021 json.cn
     */
        private BaseResponse BaseResponse;
        private String MediaId;
        private int StartPos;
        private int CDNThumbImgHeight;
        private int CDNThumbImgWidth;
        private String EncryFileName;
        @Data
        private static class BaseResponse {
            private int Ret;
            private String ErrMsg;

        }
}
