package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.adapter.message.MessageAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Created by 舒新胜 on 17-5-30.
 */
@Log4j2
public class ChatMessageViewerPanel extends ParentAvailablePanel {

    @Getter
    private RCListView<BaseMessageViewHolder, MessageAdapter> messageListView;

    private final String roomId;

    private volatile boolean isLoadHis = false;

    private final List<Message> messageItems ;

    private final int PAGE_LENGTH = 10;

    public ChatMessageViewerPanel(JPanel parent,String roomId,List<Message> messageItems) {
        super(parent);
        this.roomId = roomId;
        this.messageItems = messageItems;
        initComponents();
        initView();
        setListeners();
    }

    private void setListeners() {
        messageListView.setScrollToTopListener(() -> {
            // 当滚动到顶部时，继续拿前面的消息
            if (isLoadHis) {
                return;
            }
            isLoadHis = true;
            ((ChatPanel)((ChatMessagePanel) ChatMessageViewerPanel.this.getParentPanel()).getParentPanel()).getTitlePanel().showStatusLabel("加载中...");

            ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
                try {
                    MessageMapper mapper = SpringContextHolder.getBean(MessageMapper.class);
                    Contacts contacts = Core.getMemberMap().get(roomId);
                    String remarkName = ContactsTools.getContactRemarkNameByUserName(contacts);
                    String nickName = ContactsTools.getContactNickNameByUserName(contacts);
                    java.util.List<Message> messageList = mapper.selectByPage(messageItems.size(), PAGE_LENGTH, roomId, remarkName, nickName);
                    int i =0;
                    for (Message message : messageList) {
                        if (message.getIsSend()) {
                            message.setFromUsername(Core.getUserName());
                            message.setToUsername(roomId);
                            if (ContactsTools.isRoomContact(roomId)) {
                                message.setFromMemberOfGroupUsername(Core.getUserName());
                            }
                        } else {
                            message.setFromUsername(roomId);
                            message.setToUsername(Core.getUserName());
                            if (ContactsTools.isRoomContact(roomId)) {
                                java.util.List<Contacts> members = Core.getMemberMap().get(roomId).getMemberlist();
                                ContactsTools.findGroupMember(members, message).ifPresent(member ->
                                        message.setFromMemberOfGroupUsername(member.getUsername())
                                );
                            }
                        }
                        ContactsTools.loadUserInfo(message.getFromUsername(), message.getToUsername(), message.getFromMemberOfGroupUsername(), message);
                        if (i+1<messageList.size()) {
                            message.setPreMessageTime(messageList.get(i+1).getMessageTime());
                        }

                        SwingUtilities.invokeLater(() -> {
                            try {

                                messageItems.addFirst(message);
                                messageListView.notifyItemRangeInsertedHead(0, 1);

                            } finally {
                                isLoadHis = false;
                                ((ChatPanel)((ChatMessagePanel) ChatMessageViewerPanel.this.getParentPanel()).getParentPanel()).getTitlePanel().hideStatusLabel();
                            }
                        });
                        i++;
                    }

                    //messageList = messageList.reversed();
//                    List<Message> finalMessageList = messageList;
//                    SwingUtilities.invokeLater(() -> {
//                        try {
//                            if (finalMessageList != null && !finalMessageList.isEmpty()) {
//                                messageItems.addAll(0, finalMessageList);
//                                messageListView.notifyItemRangeInsertedHead(0, finalMessageList.size());
//                            }
//
//                        } finally {
//                            isLoadHis = false;
//                            ((ChatPanel)((ChatMessagePanel) ChatMessageViewerPanel.this.getParentPanel()).getParentPanel()).getTitlePanel().hideStatusLabel();
//                        }
//                    });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });

        });
    }


    private void initComponents() {
        messageListView = new RCListView<>(0, 15);
        messageListView.setScrollBarColor(Colors.WINDOW_BACKGROUND, Colors.WINDOW_BACKGROUND);
        messageListView.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        messageListView.setScrollHiddenOnMouseLeave(messageListView);

    }



    private void initView() {
        this.setLayout(new BorderLayout());
        messageListView.getContentPanel().setBorder(new EmptyBorder(0, 5, 5, 10));
        add(messageListView, BorderLayout.CENTER);
    }

}
