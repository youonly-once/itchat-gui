package cn.shu.wechat.dto.response.msg.send;

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
    private long StartPos;
    private int CDNThumbImgHeight;
    private int CDNThumbImgWidth;
    private String Signature;
    private String AESKey;
    private String EncryFileName;

    @Data
    public static class BaseResponse {
        private int Ret;
        private String ErrMsg;

    }
    public boolean isSuccess() {
        return BaseResponse != null && BaseResponse.Ret==0;
    }
}
