/**
 * Copyright 2021 bejson.com
 */
package cn.shu.wechat.beans.tuling.response;

import java.util.List;

/**
 * Auto-generated: 2021-02-02 15:2:56
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
public class AtoolBean {

    private Emotion emotion;
    private Intent intent;
    private List<Results> results;

    public void setEmotion(Emotion emotion) {
        this.emotion = emotion;
    }

    public Emotion getEmotion() {
        return emotion;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setResults(List<Results> results) {
        this.results = results;
    }

    public List<Results> getResults() {
        return results;
    }

}