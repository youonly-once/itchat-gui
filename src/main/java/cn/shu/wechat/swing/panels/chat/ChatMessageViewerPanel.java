package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.adapter.message.MessageAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class ChatMessageViewerPanel extends ParentAvailablePanel {
    private RCListView<BaseMessageViewHolder, MessageAdapter> listView;

    public ChatMessageViewerPanel(JPanel parent) {
        super(parent);

        initComponents();
        initView();
    }


    private void initComponents() {
        listView = new RCListView(0, 15);
        listView.setScrollBarColor(Colors.WINDOW_BACKGROUND, Colors.WINDOW_BACKGROUND);
        listView.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        listView.setScrollHiddenOnMouseLeave(listView);

    }



    private void initView() {
        this.setLayout(new BorderLayout());
        add(listView, BorderLayout.CENTER);
    }

    public RCListView<BaseMessageViewHolder, MessageAdapter> getMessageListView() {
        return listView;
    }
}
