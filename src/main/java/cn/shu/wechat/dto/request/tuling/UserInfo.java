/**
 * Copyright 2021 bejson.com
 */
package cn.shu.wechat.dto.request.tuling;

import lombok.Builder;

/**
 * Auto-generated: 2021-02-02 14:39:21
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Builder
public class UserInfo {

    private String apiKey;
    private String userId;

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

}