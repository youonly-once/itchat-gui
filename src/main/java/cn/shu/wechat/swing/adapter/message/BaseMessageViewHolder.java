package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.adapter.ViewHolder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * @author 舒新胜
 * @date 13/06/2017
 */
public abstract class BaseMessageViewHolder extends ViewHolder {
    public JLabel avatar = new JLabel();
    public JLabel time = new JLabel();
    public JLabel revoke = new JLabel("已撤回");

    public BaseMessageViewHolder() {
        revoke.setForeground(Color.GRAY);
        revoke.setVisible(false);
        revoke.setBorder(new EmptyBorder(0,5,0,5));
    }
}
