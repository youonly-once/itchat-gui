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
public class InputText {

    private String text;

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

}