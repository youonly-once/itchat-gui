package shu.cn.weichat.beans;

import com.alibaba.fastjson.JSONObject;

import java.util.Objects;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/3/2021 11:16 AM
 */
public class DistinctJSONObject extends JSONObject {
    @Override
    public boolean equals(Object obj) {
        if (this == obj)return true;
        if (obj == null) return false;
        if (this.getString("UserName").equals(((JSONObject)obj).getString("UserName"))){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getString("UserName"));
    }
}
