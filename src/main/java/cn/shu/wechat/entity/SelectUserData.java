package cn.shu.wechat.entity;

import cn.shu.wechat.utils.CharacterParser;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by 舒新胜 on 20/06/2017.
 */
@Data
@AllArgsConstructor
public class SelectUserData implements Comparable<SelectUserData> {
    private String userName;
    private String displayName;
    private boolean selected;

    @Override
    public int compareTo(SelectUserData o2) {
        String tc = CharacterParser.getSelling(this.getDisplayName().toUpperCase());
        String oc = CharacterParser.getSelling(o2.getDisplayName().toUpperCase());
        return tc.compareTo(oc);
    }
}
