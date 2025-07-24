package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.listener.ExpressionListener;
import cn.shu.wechat.swing.utils.EmojiUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.stream.IntStream;

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
                JLabel label = (JLabel) e.getSource();
                label.setBackground(Colors.SCROLL_BAR_TRACK_LIGHT);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                JLabel panel = (JLabel) e.getSource();
                panel.setBackground(Colors.WINDOW_BACKGROUND);
                super.mouseExited(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                MessageEditorExpressionItemLabel panel = (MessageEditorExpressionItemLabel) e.getSource();
                if (expressionListener != null) {
                    ImageIcon icon = new ImageIcon(panel.getIcon().getImage());
                    icon.setDescription(panel.getCode()+"&"+icon.getDescription());
                    expressionListener.onSelected(icon);

                }
                super.mouseClicked(e);
            }
        };


//        String iconPath = "/emoji/wechat_emoji/";
//        for (int i = 0; i < EmojiUtil.wechatEmojiList.size(); i++) {
//            String code = EmojiUtil.wechatEmojiList.get(i);
//            ImageIcon icon = IconUtil.getIcon(this, iconPath + (2*i + 4) + ".png",22,22);
//            JLabel panel = new MessageEditorExpressionItemLabel(code, icon, code);
//            panel.addMouseListener(listener);
//            add(panel);
//        }

        SwingWorker<Void, JLabel> worker = new SwingWorker<Void, JLabel>() {
            @Override
            protected Void doInBackground() {
                String iconPath = "/emoji/wechat_emoji/";

                IntStream.range(0, EmojiUtil.wechatEmojiList.size()).parallel().forEach(i -> {
                    String code = EmojiUtil.wechatEmojiList.get(i);
                    ImageIcon icon = IconUtil.getIcon(MessageEditorEmojiPanel.this, iconPath + (2 * i + 4) + ".png", 22, 22);
                    JLabel label = new MessageEditorExpressionItemLabel(code, icon, code);
                    label.addMouseListener(listener);
                    publish(label);
                });

                return null;
            }

            @Override
            protected void process(java.util.List<JLabel> chunks) {
                for (JLabel label : chunks) {
                    add(label);
                }
                revalidate();
                repaint();
            }
        };
        worker.execute();

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
