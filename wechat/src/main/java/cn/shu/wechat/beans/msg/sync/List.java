/**
 * Copyright 2021 bejson.com
 */
package cn.shu.wechat.beans.msg.sync;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * Auto-generated: 2021-02-22 13:35:59
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */

public class List {

    private int Key;

    private long Val;

    @JSONField(name = "Key")
    public int getKey() {
        return Key;
    }
    @JSONField(name = "Val")
    public long getVal() {
        return Val;
    }
    @JSONField(name = "Key")
    public void setKey(int key) {
        Key = key;
    }
    @JSONField(name = "Val")
    public void setVal(long val) {
        Val = val;
    }
}