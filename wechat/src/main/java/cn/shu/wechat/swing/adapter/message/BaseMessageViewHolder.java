package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.adapter.ViewHolder;

import javax.swing.*;

/**
 * @author 舒新胜
 * @date 13/06/2017
 */
public abstract class BaseMessageViewHolder extends ViewHolder {
    public JLabel avatar = new JLabel();
    public JLabel time = new JLabel();
}
