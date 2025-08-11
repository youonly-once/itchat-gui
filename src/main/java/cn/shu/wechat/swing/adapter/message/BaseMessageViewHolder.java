package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.adapter.ViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.utils.FontUtil;

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
    // public JLabel sender = new JLabel();
    public final SizeAutoAdjustTextArea sender = new SizeAutoAdjustTextArea((int) (MainFrame.getContext().currentWindowWidth * 0.5));
    public BaseMessageViewHolder() {
        revoke.setForeground(Color.GRAY);
        revoke.setVisible(false);
        revoke.setBorder(new EmptyBorder(0,5,0,5));

        time.setForeground(Colors.FONT_GRAY);
        time.setFont(FontUtil.getDefaultFont(12));
        time.setHorizontalAlignment(SwingConstants.CENTER);
        time.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        sender.setFont(FontUtil.getDefaultFont(12));
        sender.setForeground(Colors.FONT_GRAY);

    }
}
