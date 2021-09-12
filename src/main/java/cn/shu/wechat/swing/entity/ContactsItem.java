package cn.shu.wechat.swing.entity;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.pojo.entity.Contacts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContactsItem implements Comparable<ContactsItem> {
    /**
     * 联系人id
     */
    private String id;
    /**
     * 姓名 可能是备注可能是昵称
     */
    private String displayName;

    /**
     * 头像
     */
    private ImageIcon avatar;

    /**
     * 联系人类型
     */
    private Contacts.ContactsType type;

    @Override

    public int compareTo(ContactsItem o) {
        String tc = ContactsTools.getContactDisplayNameInitialByUserName(this.id);
        String oc = ContactsTools.getContactDisplayNameInitialByUserName(o.id);
        return type.equals(o.type) ? tc.compareTo(oc) : type.code -o.type.code;
    }
}
