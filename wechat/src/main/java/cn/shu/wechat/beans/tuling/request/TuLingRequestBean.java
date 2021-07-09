/**
 * Copyright 2021 bejson.com
 */
package cn.shu.wechat.beans.tuling.request;

import lombok.Builder;

/**
 * Auto-generated: 2021-02-02 14:39:21
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Builder
public class TuLingRequestBean {

    private int reqType;
    private Perception perception;
    private UserInfo userInfo;

    public void setReqType(int reqType) {
        this.reqType = reqType;
    }

    public int getReqType() {
        return reqType;
    }

    public void setPerception(Perception perception) {
        this.perception = perception;
    }

    public Perception getPerception() {
        return perception;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

}