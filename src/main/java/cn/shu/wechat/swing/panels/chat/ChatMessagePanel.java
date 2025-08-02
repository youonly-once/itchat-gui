package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.constant.DownloadStatus;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.response.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.adapter.message.MessageAdapter;
import cn.shu.wechat.swing.adapter.message.app.MessageRightAttachmentViewHolder;
import cn.shu.wechat.swing.adapter.message.image.MessageRightImageViewHolder;
import cn.shu.wechat.swing.adapter.message.video.MessageRightVideoViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.components.message.FileEditorThumbnail;
import cn.shu.wechat.swing.entity.SelectUserData;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.frames.RemindUserDialog;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.swing.tasks.UploadTaskCallback;
import cn.shu.wechat.swing.utils.EmojiUtil;
import cn.shu.wechat.swing.utils.FileCache;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.swing.utils.MimeTypeUtil;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.MediaUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * 右侧聊天面板
 * <p>
 * Created by 舒新胜 on 17-5-30.
 */
@Log4j2
public class ChatMessagePanel extends ParentAvailablePanel {
    /**
     * 消息面板
     */
    private ChatMessageViewerPanel chatMessageViewerPanel;

    /**
     *  用户列表
     */
    private final RemindUserDialog remindUserDialog = new RemindUserDialog(MainFrame.getContext(), true);


    /**
     * 消息列表
     */
    private final List<Message> messageItems = new ArrayList<>();

    /**
     * 消息适配器
     */
    private MessageAdapter adapter;

    private static String lastOpenDir;
    /**
     * 当前房间id
     */
    private final String roomId;
    /**
     * 保留最近发消息的5个人 @列表展示
     */
    private final Map<String, String> recentSenderUser = new LinkedHashMap<>(16, 0.75f, true) {

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 10;
        }
    };


    /**
     * 每次加载的消息条数
     */
    private final int PAGE_LENGTH = 10;
    /**
     * 消息输入框
     */
    @Getter
    private ChatMessageEditorPanel chatMessageEditorPanel;


    private final int MAX_SHARE_ATTACHMENT_UPLOAD_COUNT = 1024;

    private final Queue<String> shareAttachmentUploadQueue = new ArrayDeque<>(MAX_SHARE_ATTACHMENT_UPLOAD_COUNT);

    private volatile boolean isLoadHis = false;



    public ChatMessagePanel(JPanel parent, String roomId) {

        super(parent);
        this.roomId = roomId;
        if (org.apache.commons.lang3.StringUtils.isEmpty(roomId)) {
            throw new NullPointerException("RoomId can not be null.");
        }
        initComponents();
        initView();
        setListeners();
    }

    /**
     * 使用默认程序打开文件
     *
     * @param path
     */
    public static void openFileWithDefaultApplication(String path) {
        ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    if (DownloadManager.containsTask(path)) {
                        if (DownloadManager.getStatus(path) == DownloadStatus.SUCCESS) {
                            Desktop.getDesktop().open(new File(path));
                        } else if (DownloadManager.getStatus(path) == DownloadStatus.RUNNING || DownloadManager.getStatus(path) == DownloadStatus.WAITING) {
                            JOptionPane.showMessageDialog(null, "下载中", "打开失败", JOptionPane.ERROR_MESSAGE);
                        } else if (DownloadManager.getStatus(path) == DownloadStatus.FAIL) {
                            JOptionPane.showMessageDialog(null, "下载失败", "打开失败", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        Desktop.getDesktop().open(new File(path));
                    }

                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "文件打开失败，没有找到关联的应用程序", "打开失败", JOptionPane.ERROR_MESSAGE);
                    log.error(e1.getMessage(), e1);
                } catch (IllegalArgumentException e2) {
                    JOptionPane.showMessageDialog(null, "文件已被删除", "打开失败", JOptionPane.ERROR_MESSAGE);
                    log.error(e2.getMessage(), e2);
                }


            }
        });
    }


    private void initView() {
        this.setLayout(new GridBagLayout());
        add(chatMessageViewerPanel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 4));
        add(chatMessageEditorPanel, new GBC(0, 1).setFill(GBC.BOTH).setWeight(1, 1));

    }

    private void initComponents() {

        chatMessageViewerPanel = new ChatMessageViewerPanel(this);


        chatMessageViewerPanel.setBorder(new RCBorder(RCBorder.BOTTOM, Colors.LIGHT_GRAY));

        adapter = new MessageAdapter(this,messageItems, chatMessageViewerPanel.getMessageListView());


        chatMessageViewerPanel.getMessageListView().setAdapter(adapter);

        chatMessageEditorPanel = new ChatMessageEditorPanel(this, roomId);

        chatMessageEditorPanel.setPreferredSize(new Dimension(MainFrame.DEFAULT_WIDTH, MainFrame.DEFAULT_WIDTH / 4));


    }


    /**
     * 解析输入框中的内容并发送消息
     */
    private void sendMessage() {
        List<Object> inputDatas = parseEditorInput();
        boolean isImageOrFile = false;
        for (Object data : inputDatas) {
            if (data instanceof String && !"\n".equals(data)) {
                if (StringUtils.isEmpty(data)) {
                    continue;
                }
                    //文本消息
                    sendTextMessage(data.toString());

            } else if (data instanceof JLabel ) {
                //图片消息
                isImageOrFile = true;
                JLabel label = (JLabel) data;
                ImageIcon icon = (ImageIcon) label.getIcon();
                String path = icon.getDescription();
                if (path != null && !path.isEmpty()) {
                    //多个图片消息添加到队列中
                    shareAttachmentUploadQueue.add(path);
                }
                RoomsPanel.getContext().updateRoomItem(roomId, 0
                        , "[图片]发送中..."
                        , LocalDateTime.now(),ContactsTools.isMute(roomId),false);
            }else if (data instanceof Icon ) {
                //表情消息
                ImageIcon imageIcon = (ImageIcon) data;
                sendEmojiMessage(imageIcon);
            }  else if (data instanceof FileEditorThumbnail) {
                //文件消息
                isImageOrFile = true;
                FileEditorThumbnail component = (FileEditorThumbnail) data;
                //多个文件消息添加到队列中
                shareAttachmentUploadQueue.add(component.getPath());
                RoomsPanel.getContext().updateRoomItem(roomId, 0, "[文件]发送中...", LocalDateTime.now(),ContactsTools.isMute(roomId),false);

            }

        }
        //上传队列中的文件
        if (isImageOrFile) {
            // 先上传第一个图片/文件
            dequeueAndUpload();
        }
        chatMessageEditorPanel.getEditor().setText("");
        RoomsPanel.getContext().scrollToPosition(0);
    }

    /**
     * 解析输入框中的输入数据
     *
     * @returnj
     */
    private List<Object> parseEditorInput() {
        List<Object> inputData = new ArrayList<>();

        Document doc = chatMessageEditorPanel.getEditor().getDocument();
        int count = doc.getRootElements()[0].getElementCount();

        // 是否是纯文本，如果发现有图片或附件，则不是纯文本
        boolean pureText = true;

        for (int i = 0; i < count; i++) {
            Element root = doc.getRootElements()[0].getElement(i);

            int elemCount = root.getElementCount();

            for (int j = 0; j < elemCount; j++) {
                try {
                    Element elem = root.getElement(j);
                    String elemName = elem.getName();
                    switch (elemName) {
                        case "content": {
                            int start = elem.getStartOffset();
                            int end = elem.getEndOffset();
                            String text = doc.getText(elem.getStartOffset(), end - start);
                            inputData.add(text);
                            break;
                        }
                        case "component": {
                            pureText = false;
                            Component component = StyleConstants.getComponent(elem.getAttributes());
                            inputData.add(component);
                            break;
                        }
                        case "icon": {
                            pureText = false;

                            ImageIcon icon = (ImageIcon) StyleConstants.getIcon(elem.getAttributes());

                            String pathAndCode = icon.getDescription();
                            if (pathAndCode.contains("&")){
                                String code = pathAndCode.substring(0,pathAndCode.indexOf("&"));
                                if (EmojiUtil.isWeChatEmoji(this,code)){
                                    inputData.add(code);
                                }else{
                                    inputData.add(icon);
                                }
                            }else{
                                inputData.add(icon);
                            }


                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // 如果是纯文本，直接返回整个文本，否则如果出消息中有换行符\n出现，那么每一行都会被解析成一句话，会造成一条消息被分散成多个消息发送
        if (pureText) {
            inputData.clear();
            inputData.add(chatMessageEditorPanel.getEditor().getText());
        }else{
            inputData = mergeConsecutiveStrings(inputData);
        }

        return inputData;
    }
    private  List<Object> mergeConsecutiveStrings(List<Object> inputList) {
        List<Object> mergedList = new ArrayList<>();

        StringBuilder currentString = null;
        for (Object element : inputList) {
            if (element instanceof String) {
                String str = (String) element;
                if ("\n".equals(str)){
                    continue;
                }
                if (currentString == null) {
                    currentString = new StringBuilder(str);
                } else {
                    currentString.append(str);
                }
            } else {
                if (currentString != null) {
                    mergedList.add(currentString.toString());
                    currentString = null;
                }
                mergedList.add(element);
            }
        }

        // Add the last merged string if there is one
        if (currentString != null) {
            mergedList.add(currentString.toString());
        }

        return mergedList;
    }
    /**
     * 从待上传附件队列中出队一个，并上传
     */
    public synchronized void dequeueAndUpload() {
        String path = shareAttachmentUploadQueue.poll();
        if (path != null) {
            sendFileMessage(path);
        }
    }


    /**
     * 从数据库加载本地历史消息
     */
    private void loadLocalHistory() {
        ((ChatPanel) this.getParentPanel()).getTitlePanel().showStatusLabel("加载中...");
        //TODO 线程安全问题
        new SwingWorker<Object, Object>() {

            @Override
            protected Object doInBackground() throws Exception {
                MessageMapper mapper = SpringContextHolder.getBean(MessageMapper.class);
                Contacts contacts = Core.getMemberMap().get(roomId);
                String remarkName = ContactsTools.getContactRemarkNameByUserName(contacts);
                String nickName = ContactsTools.getContactNickNameByUserName(contacts);
                List<cn.shu.wechat.entity.Message> messageList = mapper.selectByPage(messageItems.size(), messageItems.size() + PAGE_LENGTH, roomId, remarkName, nickName);
                messageItems.addAll(messageList);
                return null;
            }

            @Override
            protected void done() {
                chatMessageViewerPanel.getMessageListView().notifyDataSetChanged(false);

                chatMessageViewerPanel.getMessageListView().setAutoScrollToBottom();
                ((ChatPanel) ChatMessagePanel.this.getParentPanel()).getTitlePanel().hideStatusLabel();
            }
        }.execute();

    }




    /**
     * 通知数据改变，需要重绘整个列表
     */
    public void notifyDataSetChanged() {
        chatMessageViewerPanel.getMessageListView().setVisible(false);
        messageItems.clear();
        //TODO 不应该在这个线程
        chatMessageViewerPanel.setVisible(true);
        chatMessageEditorPanel.setVisible(true);
        chatMessageViewerPanel.getMessageListView().setVisible(true);

    }

    private void setListeners() {
        //暂时不加载历史消息，无意义
        chatMessageViewerPanel.getMessageListView().setScrollToTopListener(() -> {
            // 当滚动到顶部时，继续拿前面的消息
            if (isLoadHis) {
                return;
            }
                isLoadHis = true;
                ((ChatPanel) ChatMessagePanel.this.getParentPanel()).getTitlePanel().showStatusLabel("加载中...");

            ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
                try {
                    MessageMapper mapper = SpringContextHolder.getBean(MessageMapper.class);
                    Contacts contacts = Core.getMemberMap().get(roomId);
                    String remarkName = ContactsTools.getContactRemarkNameByUserName(contacts);
                    String nickName = ContactsTools.getContactNickNameByUserName(contacts);
                    List<Message> messageList = mapper.selectByPage(messageItems.size(), PAGE_LENGTH, roomId, remarkName, nickName);
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
                                List<Contacts> members = Core.getMemberMap().get(roomId).getMemberlist();
                                ContactsTools.findGroupMember(members, message).ifPresent(member ->
                                        message.setFromMemberOfGroupUsername(member.getUsername())
                                );
                            }
                        }
                        ContactsTools.loadUserInfo(message.getFromUsername(), message.getToUsername(), message.getFromMemberOfGroupUsername(), message);
                    }

                    messageList = messageList.reversed();
                    List<Message> finalMessageList = messageList;
                    SwingUtilities.invokeLater(() -> {
                        try {
                            if (finalMessageList != null && !finalMessageList.isEmpty()) {
                                messageItems.addAll(0, finalMessageList);
                                chatMessageViewerPanel.getMessageListView().notifyItemRangeInsertedHead(0, finalMessageList.size());
                            }

                        } finally {
                            isLoadHis = false;
                            ((ChatPanel) ChatMessagePanel.this.getParentPanel()).getTitlePanel().hideStatusLabel();
                        }
                    });
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    }
            });

        });

        JTextPane editor = chatMessageEditorPanel.getEditor();
        Document document = editor.getDocument();

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // CTRL + 回车换行
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        document.insertString(editor.getCaretPosition(), "\n", null);
                    } catch (BadLocationException e1) {
                        log.error(e1.getMessage(), e);
                    }
                }

                // 回车发送消息
                else if (!e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                    e.consume();
                }

                // 输入@，弹出选择用户菜单
                else if (e.getKeyChar() == '@' && ContactsTools.isRoomContact(roomId)) {
                    Collection<String> values = recentSenderUser.values();
                    List<Contacts> memberlist = Core.getMemberMap().get(roomId)
                            .getMemberlist();
                    if (values.isEmpty() && !memberlist.isEmpty()) {
                        values = memberlist.subList(0, Math.min(5, memberlist.size()))
                                .stream().map(ContactsTools::getContactDisplayNameByUserName).toList();
                    }
                    remindUserDialog.addData(values, memberlist);
                    remindUserDialog.setVisible(true, editor);
                }

                // 输入退格键，删除最后一个@user
                else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    String str = editor.getText();
                    if (str.matches(".*@\\w+\\s")) {
                        try {
                            int startPos = str.lastIndexOf("@");
                            String rmStr = str.substring(startPos);
                            editor.getDocument().remove(startPos + 1, rmStr.length() - 1);
                        } catch (BadLocationException e1) {
                            log.error(e1.getMessage(), e);
                        }
                    }
                }
            }

        });

        remindUserDialog.setListeners((usernames) -> {
            ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
                StringBuilder sb = new StringBuilder();
                for (SelectUserData username : usernames) {
                    sb.append("@").append(username.getDisplayName()).append(" ");
                }
                String at = sb.substring(1);
                SwingUtilities.invokeLater(() -> editor.replaceSelection(at));
            });
        });

        // 发送按钮
        chatMessageEditorPanel.getSendButton().addActionListener(e -> sendMessage());

        // 上传文件按钮
        chatMessageEditorPanel.getUploadFileLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("请选择上传文件或图片");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                if (lastOpenDir != null) {
                    fileChooser.setCurrentDirectory(new File(lastOpenDir));
                }

                fileChooser.showDialog(MainFrame.getContext(), "上传");
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    String path = selectedFile.getAbsolutePath();
                    lastOpenDir = path;
                    sendFileMessage(path);
                    showSendingMessage();
                }

                super.mouseClicked(e);
            }
        });


    }


    /**
     * 更新已有消息
     *
     * @param lastMessage 消息
     */
    public void updateMessage(BaseMessageViewHolder viewHolder, Message lastMessage) {
        // 已有消息更新状态
        int pos = findMessagePositionInViewReverse(lastMessage.getId());
        if (pos > -1) {
        /*    Message messageItem = messageItems.get(pos);
            messageItem.setNeedToResend(!lastMessage.getIsSend());
            messageItem.setProgress(lastMessage.get);*/
            chatMessageViewerPanel.getMessageListView().notifyItemChanged(viewHolder, pos);
        }
    }

    public void setRevokeStatus(String id){
        int pos = findMessagePositionInViewReverse(id);
        if (pos > -1) {
            messageItems.get(pos).setRevoke(true);
            chatMessageViewerPanel.getMessageListView().notifyItemChanged(pos);
        }
    }

    /**
     * 发送文本消息
     *
     * @param content 消息内容
     */
    public void sendTextMessage(String content)  {
        //更新房间列表
        RoomsPanel.getContext().updateRoomItem(roomId, 0, content+"[发送中...]",LocalDateTime.now(),ContactsTools.isMute(roomId),false);
        String msgId = MessageTools.randomMessageId();
        Message message = Message.builder().isSend(false)
                .id(msgId)
                .content(content)
                .plaintext(content)
                .createTime(LocalDateTime.now())
                .fromUsername(Core.getUserName())
                .toUsername(roomId)
                .msgType(WxRespConstant.WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                .fromNickname(Core.getNickName())
                .progress(50)
                .messageTime(LocalDateTime.now())
                .deleted(false)
                .isSend(true)
                .isNeedToResend(false)
                .build();
        //绘制消息项
        BaseMessageViewHolder viewHolder = addMessageToEnd(message);
        new SwingWorker<WebWXSendMsgResponse, WebWXSendMsgResponse>() {
            private WebWXSendMsgResponse wxSendMsgResponse;

            @Override
            protected WebWXSendMsgResponse doInBackground() throws Exception {
                //后台发送消息
                wxSendMsgResponse = MessageTools.sendMsgByUserId(message);
                return null;
            }

            @Override
            protected void process(List<WebWXSendMsgResponse> chunks) {
                super.process(chunks);
            }

            @Override
            protected void done() {
                message.setProgress(100);
                if (wxSendMsgResponse == null
                        || wxSendMsgResponse.getBaseResponse().getRet() != 0) {
                    message.setNeedToResend(true);
                    RoomsPanel.getContext().updateRoomItem(roomId, 0, content+"[发送失败]", LocalDateTime.now(),ContactsTools.isMute(roomId),false);

                } else {
                    message.setNeedToResend(false);
                    RoomsPanel.getContext().updateRoomItem(roomId, 0, content, LocalDateTime.now(),ContactsTools.isMute(roomId),false);

                }
                updateMessage(viewHolder, message);
            }
        }.execute();
    }
    /**
     * 发送文本消息
     *
     * @param imageIcon 表情
     */
    public void sendEmojiMessage(ImageIcon imageIcon)  {
        //更新房间列表
        RoomsPanel.getContext().updateRoomItem(roomId, 0, "[表情][发送中...]",LocalDateTime.now(),ContactsTools.isMute(roomId),false);
        String msgId = MessageTools.randomMessageId();

        String pathAndCode = imageIcon.getDescription();
        String code = pathAndCode.substring(0,pathAndCode.indexOf("&"));
        String path = pathAndCode.substring(pathAndCode.indexOf("&file:/")+"&file:/".length());

        //绘制消息项
        Message messageView = Message.builder().isSend(false)
                .id(msgId)
                .content(code)
                .plaintext(code)
                .createTime(LocalDateTime.now())
                .fromUsername(Core.getUserName())
                .toUsername(roomId)
                .msgType(WxRespConstant.WXReceiveMsgCodeEnum.MSGTYPE_IMAGE.getCode())
                .fromNickname(Core.getNickName())
                .progress(50)
                .messageTime(LocalDateTime.now())
                .deleted(false)
                .isSend(true)
                .filePath(path)
                .slavePath(path)
                .imgWidth(imageIcon.getIconWidth())
                .imgHeight(imageIcon.getIconHeight())
                .isNeedToResend(false)
                .build();

        BaseMessageViewHolder viewHolder = addMessageToEnd(messageView);
        new SwingWorker<WebWXSendMsgResponse, WebWXSendMsgResponse>() {
            private WebWXSendMsgResponse wxSendMsgResponse;

            @Override
            protected WebWXSendMsgResponse doInBackground() throws Exception {
                //后台发送消息
                wxSendMsgResponse = MessageTools.sendMsgByUserId(Message.builder().isSend(false)
                        .id(msgId)
                        .content(code)
                        .plaintext(code)
                        .createTime(LocalDateTime.now())
                        .fromUsername(Core.getUserName())
                        .toUsername(roomId)
                        .msgType(WxRespConstant.WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                        .fromNickname(Core.getNickName())
                        .progress(50)
                        .messageTime(LocalDateTime.now())
                        .deleted(false)
                        .isSend(true)
                        .isNeedToResend(false)
                        .build());
                return null;
            }

            @Override
            protected void process(List<WebWXSendMsgResponse> chunks) {
                super.process(chunks);
            }

            @Override
            protected void done() {
                messageView.setProgress(100);
                if (wxSendMsgResponse == null
                        || wxSendMsgResponse.getBaseResponse().getRet() != 0) {
                    messageView.setNeedToResend(true);
                    RoomsPanel.getContext().updateRoomItem(roomId, 0, "[发送失败]", LocalDateTime.now(),ContactsTools.isMute(roomId),false);

                } else {
                    messageView.setNeedToResend(false);
                    RoomsPanel.getContext().updateRoomItem(roomId, 0, "[表情]", LocalDateTime.now(),ContactsTools.isMute(roomId),false);

                }
                updateMessage(viewHolder, messageView);
            }
        }.execute();
    }
    private void showSendingMessage() {
        RoomsPanel.getContext().updateRoomItem(roomId, 0, "[发送中...]", LocalDateTime.now(),ContactsTools.isMute(roomId),false);
    }

    /**
     * 倒序查找指定的消息在消息列表中的位置中的位置
     *
     * @param messageId
     * @return 查找成功，返回该消息在消息列表中的位置，否则返回-1
     */
    private int findMessagePositionInViewReverse(String messageId) {
        for (int i = messageItems.size() - 1; i >= 0; i--) {
            // 找到消息列表中对应的消息
            if (messageId.equals(messageItems.get(i).getId())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 上传文件
     *
     * @param uploadFilename
     */
    public void sendFileMessage(String uploadFilename) {
        String msgId = MessageTools.randomMessageId();
        File file = new File(uploadFilename);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "文件不存在", "上传失败", JOptionPane.ERROR_MESSAGE);
        }

        String mime = MimeTypeUtil.getMime(uploadFilename.substring(uploadFilename.lastIndexOf(".")));
        WxRespConstant.WXReceiveMsgCodeEnum msgType = WxRespConstant.WXReceiveMsgCodeEnum.MSGTYPE_APP;
        WxRespConstant.WXReceiveMsgCodeOfAppEnum fileOfAppType = WxRespConstant.WXReceiveMsgCodeOfAppEnum.FILE;
        if (mime == null) {
            msgType = WxRespConstant.WXReceiveMsgCodeEnum.MSGTYPE_APP;
            mime = "app";
        } else if (mime.startsWith("image/")) {
            msgType = WxRespConstant.WXReceiveMsgCodeEnum.MSGTYPE_IMAGE;
        } else if (mime.startsWith("video/")) {
            msgType = WxRespConstant.WXReceiveMsgCodeEnum.MSGTYPE_VIDEO;
        }
        //新增消息项
        Message message = null;
        // 发送的是图片
        Dimension imageSize;
        String fileName = uploadFilename.substring(uploadFilename.lastIndexOf(File.separator) + 1); // 文件名

        switch (msgType) {
            case MSGTYPE_IMAGE:
                imageSize = IconUtil.getImageSize(uploadFilename);
                message = Message.builder()
                        .desc(fileName)
                        .id(msgId)
                        .filePath(uploadFilename)
                        .slavePath(uploadFilename)
                        .title(fileName)
                        .imgWidth(imageSize.width)
                        .msgType(msgType.getCode())
                        .appMsgType(fileOfAppType.getType())
                        .imgHeight(imageSize.height).build();
                break;
            case MSGTYPE_VIDEO:
                BufferedImage videoPic = MediaUtil.getVideoPic(this, file);
                message = Message.builder()
                        .slavePath(uploadFilename)
                        .filePath(uploadFilename)
                        .fileSize(file.length())
                        .id(msgId)
                        .imgWidth(videoPic.getWidth())
                        .imgHeight(videoPic.getHeight())
                        .playLength(MediaUtil.getVideoDuration(file)/1000)
                        .videoPic(videoPic)
                        .msgType(msgType.getCode())
                        .appMsgType(fileOfAppType.getType())
                        .desc(fileName)
                        .fileName(fileName).build();
                break;
            case MSGTYPE_APP:
                message = Message.builder()
                        .slavePath(uploadFilename)
                        .filePath(uploadFilename)
                        .fileSize(file.length())
                        .id(msgId)
                        .msgType(msgType.getCode())
                        .appMsgType(fileOfAppType.getType())
                        .desc(fileName)
                        .fileName(fileName).build();
                break;
            default:
                log.error("不支持的消息类型");
                return;
        }


        message.setPlaintext("[文件]"+fileName);
        message.setPlainName(Core.getUserSelf().getNickname());
        message.setId(msgId);
        message.setProgress(0);
        message.setToUsername(roomId);
        message.setFromUsername(Core.getUserName());
        message.setMessageTime(LocalDateTime.now());
        //添加消息 到面板
        BaseMessageViewHolder viewHolder = addMessageToEnd(message);

        Message finalMessage = message;
        WxRespConstant.WXReceiveMsgCodeEnum finalMsgType = msgType;
        new SwingWorker<Void, Long>() {
            private WebWXSendMsgResponse wxSendMsgResponse;
            @Override
            protected Void doInBackground() throws Exception {

                //文件上传回调函数
                UploadTaskCallback callback = new UploadTaskCallback() {
                    @Override
                    public void onTaskSuccess(int curr, int size) {

                        publish((long)curr,(long)size);
                    }

                    @Override
                    public void onTaskError() {
                    }
                };
                //发送消息 等待回调
                    wxSendMsgResponse = MessageTools.sendMsgByUserId(finalMessage, callback);
                    if (wxSendMsgResponse.getBaseResponse().getRet()==-1) {
                        JOptionPane.showMessageDialog(null, wxSendMsgResponse.getBaseResponse().getErrMsg(), "上传失败", JOptionPane.ERROR_MESSAGE);
                    }
                return null;

            }

            @Override
            protected void process(List<Long> chunks) {
                Long curr = chunks.get(chunks.size() - 2);
                Long size = chunks.get(chunks.size() - 1);
                int progress = (int) (((curr * 1.0f) / size) * 100);
                // 上传完成
                if (progress == 100) {
                    RoomsPanel.getContext().updateRoomItem(roomId, 0, finalMessage.getPlaintext(), LocalDateTime.now(),ContactsTools.isMute(roomId),false);
                }
                if (viewHolder != null) {
                    finalMessage.setProgress(progress);
                    switch (finalMsgType) {
                        case MSGTYPE_VIDEO: {
                            MessageRightVideoViewHolder holder = (MessageRightVideoViewHolder) viewHolder;
                            holder.sendingProgress.setVisible(progress < 100);
                            break;
                        }
                        default:
                        case MSGTYPE_APP: {
                            MessageRightAttachmentViewHolder holder = (MessageRightAttachmentViewHolder) viewHolder;

                            // 隐藏"等待上传"，并显示进度条
                            holder.progressBar.setVisible(true);
                            holder.progressBar.setValue(progress);

                            if (progress >= 100) {
                                holder.progressBar.setVisible(false);
                                holder.sizeLabel.setText(FileCache.fileSizeString(uploadFilename));
                            }else{
                                float v = (curr * 1.0f) / size;
                                long length = file.length();
                                long v1 = (long)(v * length);
                                holder.sizeLabel.setText(FileCache.fileSizeString(v1)+"/"+FileCache.fileSizeString(uploadFilename));
                            }
                        }
                        break;
                        case MSGTYPE_IMAGE: {
                            MessageRightImageViewHolder holder = (MessageRightImageViewHolder) viewHolder;
                            holder.sendingProgress.setVisible(progress < 100);
                            break;
                        }

                    }

                }

            }

            @Override
            protected void done() {
                finalMessage.setProgress(100);
                if (wxSendMsgResponse == null
                        || wxSendMsgResponse.getBaseResponse().getRet() != 0) {
                    finalMessage.setNeedToResend(true);
                } else {
                    finalMessage.setNeedToResend(false);
                }
                updateMessage(viewHolder, finalMessage);
            }
        }.execute();


    }


    public static void openFile(String filePath) {
        if (filePath == null) {
            JOptionPane.showMessageDialog(null, "文件不存在", "打开失败", JOptionPane.ERROR_MESSAGE);
        } else {
            openFileWithDefaultApplication(filePath);
        }
    }

    /**
     * 添加一条消息到消息列表最后
     *
     * @param messageItem 消息
     */
    public BaseMessageViewHolder addMessageToEnd(Message messageItem) {
        if (messageItem.isGroup()) {
            recentSenderUser.put(messageItem.getFromMemberOfGroupUsername(), messageItem.getFromMemberOfGroupNickname());
        }
        if (messageItems.size() > PAGE_LENGTH) {
            this.messageItems.removeFirst();
            chatMessageViewerPanel.getMessageListView().getContentPanel().remove(0);
        }
        this.messageItems.add(messageItem);
        BaseMessageViewHolder holder = chatMessageViewerPanel.getMessageListView().notifyItemInserted(messageItems.size() - 1, true);
        // 只有当滚动条在最底部最，新消到来后才自动滚动到底部
        JScrollBar scrollBar = chatMessageViewerPanel.getMessageListView().getVerticalScrollBar();
        if (scrollBar.getValue() == (scrollBar.getModel().getMaximum() - scrollBar.getModel().getExtent())) {
            chatMessageViewerPanel.getMessageListView().setAutoScrollToBottom();
        }
        RoomsPanel.getContext().hasRead(roomId);
        return holder;
    }


    /**
     * 删除消息
     *
     */
    public void deleteMessage(String messageId) {
        int pos = findMessagePositionInViewReverse(messageId);
        if (pos > -1) {
            messageItems.remove(pos);
            chatMessageViewerPanel.getMessageListView().notifyItemRemoved(pos);
        }
    }

    /**
     * 粘贴
     */
    public void paste() {
        chatMessageEditorPanel.getEditor().paste();
        chatMessageEditorPanel.getEditor().requestFocus();
    }

    public void clearMsgItem() {
        SwingUtilities.invokeLater(() -> {

            if (!MainFrame.getContext().isActive() ||
                    (MainFrame.getContext().isActive() && !ChatPanelContainer.getCurrRoomId().equals(roomId))) {
                if (messageItems.size() > PAGE_LENGTH) {
                    int count = messageItems.size() - PAGE_LENGTH;
                        log.info("清理消息项：{} for {}", count,ContactsTools.getContactDisplayNameByUserName(roomId));
                        messageItems.subList(0, count).clear();
                        chatMessageViewerPanel.getMessageListView().notifyItemRemoved(0, count);

                }
                if (chatMessageViewerPanel.getMessageListView().getContentPanel().getComponentCount() > PAGE_LENGTH) {
                    int count = chatMessageViewerPanel.getMessageListView().getContentPanel().getComponentCount() - PAGE_LENGTH;
                    log.info("清理消息项(component)：{} for {}", count,ContactsTools.getContactDisplayNameByUserName(roomId));
                    chatMessageViewerPanel.getMessageListView().notifyItemRemoved(0, count);

                }
            }
        });
    }
}
