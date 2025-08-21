package cn.shu.wechat.swing.adapter.message.system;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.panels.RightPanel;
import cn.shu.wechat.utils.FontUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-6-2.
 */
public class MessageSystemMessageViewHolder extends BaseMessageViewHolder {
    public SizeAutoAdjustTextArea text = new SizeAutoAdjustTextArea ((int)(RightPanel.getContext().getWidth()/1.5)) {

//        @Override
//        protected void paintComponent(Graphics g) {
//            FontMetrics fm = g.getFontMetrics(getFont());
//            int x = (getWidth() - fm.stringWidth(getText())) / 2;
//            int y = fm.getAscent() + (getHeight() - fm.getHeight()) / 2;
//
//            g.setFont(getFont());
//            g.setColor(getForeground());
//            g.drawString(getText(), x, y);
//        }
    };
    private JPanel textPanel;

    public MessageSystemMessageViewHolder() {
        avatar = null;
        initComponents();
        initView();
    }

    private void initComponents() {
        setBackground(Colors.WINDOW_BACKGROUND);
        //text.setLineWrap(true);          // 启用自动换行
       // text.setWrapStyleWord(true);     // 按单词边界换行
        text.setEditable(false);         // 不可编辑
        text.setOpaque(false);           // 背景透明
        text.setFocusable(false);        // 不可聚焦
       // text.setHorizontalTextPosition(SwingConstants.CENTER);
        text.setFont(FontUtil.getDefaultFont(12));
        text.setForeground(new Color(248, 248, 248));
       // text.setColumns(20);
        textPanel = new JPanel() {
            @Override
            public Insets getInsets() {
                return new Insets(-3, 0, -3, 0);
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(195, 195, 195));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.dispose();
            }
        };

        textPanel.setFont(FontUtil.getDefaultFont(12));
        textPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void initView() {
        this.setLayout(new GridBagLayout());
        textPanel.add(text);
        add(time, new GBC(0, 0).setWeight(1, 1)
                .setAnchor(GBC.NORTH).setInsets(0, 0, 0, 0)
                .setFill(GBC.HORIZONTAL));

        add(textPanel, new GBC(0, 1).setWeight(1, 1)
                .setAnchor(GBC.CENTER).setInsets(0, 0, 0, 0)
                .setFill(GBC.NONE));
    }
}
