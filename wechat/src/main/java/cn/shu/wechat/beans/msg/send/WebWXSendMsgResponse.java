package cn.shu.wechat.beans.msg.send;

import lombok.Data;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 3/3/2021 3:44 PM
 */
@Data
public class WebWXSendMsgResponse {


    private BaseResponse BaseResponse;
    private String MsgID;
    private String LocalID;

    @Data
    public static class BaseResponse {

        private int Ret;
        private String ErrMsg;

    }
}
