package cn.shu.wechat.beans.msg;

import lombok.Data;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 3/3/2021 3:44 PM
 */
@Data
public class SendMsgResponse {


    private BaseResponse BaseResponse;
    private String MsgID;
    private String LocalID;

    @Data
    static
    class BaseResponse {

        private int Ret;
        private String ErrMsg;

    }
}
