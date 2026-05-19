package cn.shu.wechat.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import javax.swing.*;
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
    @Excel(name="用户名",width = 50)
    private String username;

    @Excel(name="昵称",width = 20)
    private String nickname;

    @Excel(name="签名",width = 50)
    private String signature;

    @Excel(name="性别",replace ={"男_1","女_2"} )
    private Byte sex;

    @Excel(name="备注",width = 20)
    private String remarkname;

    @Excel(name="省份")
    private String province;

    @Excel(name="城市")
    private String city;

    @Excel(name="chatroomid")
    private Integer chatroomid;

    @Excel(name="attrstatus")
    private Long attrstatus;

    /**
     * 等于0消息免打扰 1正常
     */
    @Excel(name="statues")
    private Integer statues;

    @Excel(name="pyquanpin")
    private String pyquanpin;

    @Excel(name="encrychatroomid")
    private String encrychatroomid;

    @Excel(name="displayname")
    private String displayname;

    @Excel(name="verifyflag")
    private Integer verifyflag;

    @Excel(name="unifriend")
    private Integer unifriend;

    @Excel(name="contactflag")
    private Integer contactflag;


    private List<Contacts> memberlist;

    @Excel(name="starfriend")
    private Integer starfriend;

    @Excel(name="headimgurl")
    private String headimgurl;

    @Excel(name="appaccountflag")
    private Integer appaccountflag;

    @Excel(name="membercount")
    private Integer membercount;

    @Excel(name="remarkpyinitial")
    private String remarkpyinitial;


    @Excel(name="snsflag")
    private Integer snsflag;

    @Excel(name="alias")
    private String alias;

    @Excel(name="keyword")
    private String keyword;

    @Excel(name="hideinputbarflag")
    private Integer hideinputbarflag;

    @Excel(name="remarkpyquanpin")
    private String remarkpyquanpin;

    @Excel(name="uin")
    private Long uin;

    @Excel(name="owneruin")
    private Integer owneruin;

    @Excel(name="isowner")
    private Integer isowner;

    @Excel(name="pyinitial")
    private String pyinitial;

    @Excel(name="ticket")
    private String ticket;

    @Getter
    @Setter
    private boolean mutualCreate;


    /**
     * 联系人类型
     */
    public enum ContactsType{
        GROUP_USER((byte)1,"群组")
        ,PUBLIC_USER((byte)2,"公众号")
        ,SPECIAL_USER((byte)3,"特殊账号")
        ,ORDINARY_USER((byte)4,"普通用户");
        public final byte code;
        public final String desc ;

        ContactsType(byte code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        // 枚举内部方法：返回 byte[]
        public static byte[] codes() {
            ContactsType[] types = values();
            byte[] result = new byte[types.length];
            for (int i = 0; i < types.length; i++) {
                result[i] = types[i].code;
            }
            return result;
        }
    }
    private ContactsType type;
    /**
     * 是否为联系人(false则为群成员)
     */
    @JSONField(serialize = false)
    private Boolean iscontacts;
    /**
     * 头像
     */
    @JSONField(serialize = false)
    private ImageIcon avatarIcon;

    /**
     * 群id
     */
    @JSONField(serialize = false)
    private String groupName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Contacts contacts = (Contacts) o;
        return contacts.username.equals(username);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }
}
