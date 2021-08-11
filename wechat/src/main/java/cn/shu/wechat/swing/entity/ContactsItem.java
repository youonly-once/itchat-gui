package cn.shu.wechat.swing.entity;

import cn.shu.wechat.swing.utils.CharacterParser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.awt.*;

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

    @Override
    public int compareTo(ContactsItem o) {
        String tc = CharacterParser.getSelling(this.getDisplayName()).toUpperCase();
        String oc = CharacterParser.getSelling(o.getDisplayName()).toUpperCase();
        return tc.compareTo(oc);
    }
}
