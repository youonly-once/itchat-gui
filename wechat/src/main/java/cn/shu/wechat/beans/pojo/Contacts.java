package cn.shu.wechat.beans.pojo;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * @作者 舒新胜
 * @项目 AutoWechat
 * @创建时间 6/14/2021 7:41 PM
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contacts {
    private String username;

    private Double chatroomid;

    private Double sex;

    private Double attrstatus;

    private Double statues;

    private String pyquanpin;

    private String encrychatroomid;

    private String displayname;

    private Double verifyflag;

    private Double unifriend;

    private Double contactflag;

    private List<Contacts> memberlist;

    private Double starfriend;

    private String headimgurl;

    private Double appaccountflag;

    private Double membercount;

    private String remarkpyinitial;

    private String city;

    private String nickname;

    private String province;

    private Double snsflag;

    private String alias;

    private String keyword;

    private Double hideinputbarflag;

    private String signature;

    private String remarkname;

    private String remarkpyquanpin;

    private Double uin;

    private Double owneruin;

    private Double isowner;

    private String pyinitial;

    /**
     * 是否为联系人(false则为群成员)
     */
    @JSONField(serialize = false)
    private Boolean iscontacts;
    /**
     * 头像
     */
    @JSONField(serialize = false)
    private Image avatar;

    /**
     * 群id
     */
    @JSONField(serialize = false)
    private String groupName;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contacts contacts = (Contacts) o;
        return contacts.username.equals(username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}