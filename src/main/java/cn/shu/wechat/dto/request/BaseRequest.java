package cn.shu.wechat.dto.request;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public  class BaseRequest{
            @JSONField(name ="Skey")
            private String sKey;

            @JSONField(name ="Sid")
            private String wxSid;

            @JSONField(name ="Uin")
            private String wxUin;

            @JSONField(name ="DeviceID")
            private String deviceId;

        }