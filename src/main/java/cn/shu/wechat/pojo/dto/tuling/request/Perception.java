/**
 * Copyright 2021 bejson.com
 */
package cn.shu.wechat.pojo.dto.tuling.request;

import lombok.Builder;

/**
 * Auto-generated: 2021-02-02 14:39:21
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Builder
public class Perception {

    private InputText inputText;
    private InputImage inputImage;
    private SelfInfo selfInfo;

    public void setInputText(InputText inputText) {
        this.inputText = inputText;
    }

    public InputText getInputText() {
        return inputText;
    }

    public void setInputImage(InputImage inputImage) {
        this.inputImage = inputImage;
    }

    public InputImage getInputImage() {
        return inputImage;
    }

    public void setSelfInfo(SelfInfo selfInfo) {
        this.selfInfo = selfInfo;
    }

    public SelfInfo getSelfInfo() {
        return selfInfo;
    }

}