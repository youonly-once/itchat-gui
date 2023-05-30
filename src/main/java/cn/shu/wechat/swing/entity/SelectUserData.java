package cn.shu.wechat.swing.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Created by 舒新胜 on 20/06/2017.
 */
@Data
@AllArgsConstructor
public class SelectUserData {
    private String userName;
    private String displayName;
    private boolean selected;
}
