package cn.shu.wechat.pojo.dto.msg.send;

import cn.shu.wechat.pojo.entity.Message;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 3/3/2021 3:44 PM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebWXSendMsgResponse {


    private BaseResponse BaseResponse;
    private String MsgID;
    private String LocalID;
    private List<Message> messageList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BaseResponse {

        private int Ret;
        private String ErrMsg;

    }

    public static WebWXSendMsgResponse error(String errMsg){
        WebWXSendMsgResponse.BaseResponse baseResponse = new BaseResponse();
        baseResponse.setErrMsg(errMsg);
        baseResponse.setRet(-1);
        return WebWXSendMsgResponse.builder().BaseResponse(baseResponse).build();
    }
}
