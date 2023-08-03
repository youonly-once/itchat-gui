package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.listener.ExpressionListener;
import cn.shu.wechat.swing.panels.chat.MessageEditorExpressionItemPanel;
import cn.shu.wechat.swing.utils.EmojiUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import org.apache.commons.compress.utils.Lists;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 舒新胜 on 04/07/2017.
 */
public class MessageEditorEmojiPanel extends JPanel {
    private ExpressionListener expressionListener;
    private JPopupMenu parentPopup;

    public MessageEditorEmojiPanel() {
        initComponents();
        initView();
        initData();

    }

    private void initData() {
        MouseListener listener = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                JPanel panel = (JPanel) e.getSource();
                panel.setBackground(Colors.SCROLL_BAR_TRACK_LIGHT);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JPanel panel = (JPanel) e.getSource();
                panel.setBackground(Colors.WINDOW_BACKGROUND);
                super.mouseExited(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                MessageEditorExpressionItemPanel panel = (MessageEditorExpressionItemPanel) e.getSource();
                if (expressionListener != null) {
                    ImageIcon icon = new ImageIcon(panel.getIcon().getImage());
                    icon.setDescription(panel.getCode()+"&"+icon.getDescription());
                    expressionListener.onSelected(icon);
                    if (parentPopup != null) {
                        parentPopup.setVisible(false);
                    }

                }
               // super.mouseClicked(e);
            }
        };


        String iconPath = "/emoji/wechat_emoji/";
        for (int i = 0; i < EmojiUtil.wechatEmojiList.size(); i++) {
            String code = EmojiUtil.wechatEmojiList.get(i);
            ImageIcon icon = IconUtil.getIcon(this, iconPath + (2*i + 4) + ".png",22,22);
            JPanel panel = new MessageEditorExpressionItemPanel(code, icon, code);
            panel.addMouseListener(listener);
            add(panel);
        }

    }

    private void initComponents() {
        //setPreferredSize(new Dimension(400,300));
        this.setLayout(new GridLayout(8, 10, 3, 0));

    }

    private void initView() {

    }

    public void setExpressionListener(ExpressionListener expressionListener, JPopupMenu parentPopup) {
        this.expressionListener = expressionListener;
        this.parentPopup = parentPopup;
    }
}
