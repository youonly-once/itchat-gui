package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.beans.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.beans.pojo.Message;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXSendMsgCodeEnum;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.swing.adapter.message.*;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.components.message.FileEditorThumbnail;
import cn.shu.wechat.swing.components.message.RemindUserPopup;
import cn.shu.wechat.swing.db.model.FileAttachment;
import cn.shu.wechat.swing.db.model.ImageAttachment;
import cn.shu.wechat.swing.entity.FileAttachmentItem;
import cn.shu.wechat.swing.entity.ImageAttachmentItem;
import cn.shu.wechat.swing.entity.MessageItem;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.helper.MessageViewHolderCacheHelper;
import cn.shu.wechat.swing.listener.ExpressionListener;
import cn.shu.wechat.swing.tasks.DownloadTask;
import cn.shu.wechat.swing.tasks.HttpResponseListener;
import cn.shu.wechat.swing.tasks.UploadTaskCallback;
import cn.shu.wechat.swing.utils.ClipboardUtil;
import cn.shu.wechat.swing.utils.FileCache;
import cn.shu.wechat.swing.utils.HttpUtil;
import cn.shu.wechat.swing.utils.MimeTypeUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * 右侧聊天面板
 * <p>
 * Created by 舒新胜 on 17-5-30.
 */
@Log4j2
public class ChatPanel extends ParentAvailablePanel {
    /**
     * 消息面板
     */
    private MessagePanel messagePanel;

    /**
     * 消息输入框
     */
    private MessageEditorPanel messageEditorPanel;

    /**
     * 消息列表
     */
    private final List<MessageItem> messageItems = new ArrayList<>();

    /**
     * 消息适配器
     */
    private MessageAdapter adapter;


    /**
     * 当前房间id
     */
    private final String roomId;
    /**
     * 如果是从消息搜索列表中进入房间的，这个属性不为0
     */
    private long firstMessageTimestamp = 0L;

    public void setRoomMembers(List<String> roomMembers) {
        this.roomMembers = roomMembers;
    }

    /**
     * 房间的用户 username列表
     */
    private List<String> roomMembers = new ArrayList<>();

    private  List<String> uploadingOrDownloadingFiles = new ArrayList<>();

    /**
     * 文件缓存
     */
    private final FileCache fileCache;


    /**
     * 每次加载的消息条数
     */
    private static final int PAGE_LENGTH = 10;

    /**
     * @" 用户列表
     */
    private final RemindUserPopup remindUserPopup = new RemindUserPopup();
    private final MessageViewHolderCacheHelper messageViewHolderCacheHelper;


    private static final int MAX_SHARE_ATTACHMENT_UPLOAD_COUNT = 1024;

    private final Queue<String> shareAttachmentUploadQueue = new ArrayDeque<>(MAX_SHARE_ATTACHMENT_UPLOAD_COUNT);


    public ChatPanel(JPanel parent,String roomId) {

        super(parent);
        this.roomId = roomId;
        messageViewHolderCacheHelper = new MessageViewHolderCacheHelper();

        initComponents();
        initView();
        setListeners();

        initData();

        fileCache = new FileCache();
    }

    private void initComponents() {
        messagePanel = new MessagePanel(this);
        messagePanel.setBorder(new RCBorder(RCBorder.BOTTOM, Colors.LIGHT_GRAY));
        adapter = new MessageAdapter(messageItems, messagePanel.getMessageListView(), messageViewHolderCacheHelper);
        messagePanel.getMessageListView().setAdapter(adapter);

        messageEditorPanel = new MessageEditorPanel(this,roomId);
        messageEditorPanel.setPreferredSize(new Dimension(MainFrame.DEFAULT_WIDTH, MainFrame.DEFAULT_WIDTH / 4));
    }


    private void initView() {
        this.setLayout(new GridBagLayout());
        add(messagePanel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 4));
        add(messageEditorPanel, new GBC(0, 1).setFill(GBC.BOTH).setWeight(1, 1));

        if (roomId == null) {
            messagePanel.setVisible(false);
            messageEditorPanel.setVisible(false);
        }
    }

    private void initData() {
        if (roomId != null) {
            // 如果是从搜索列表进入房间的
            if (firstMessageTimestamp != 0L) {
                loadMessageWithEarliestTime(firstMessageTimestamp);
            } else {
                // 加载本地消息
                loadLocalHistory(); // 初次打开房间时加载历史消息

                // TODO：从服务器获取本地最后一条消息之后的消，并追回到消息列表
            }
        }
    }

    private void setListeners() {
        messagePanel.getMessageListView().setScrollToTopListener(new RCListView.ScrollToTopListener() {
            @Override
            public void onScrollToTop() {
                // 当滚动到顶部时，继续拿前面的消息
                if (roomId != null) {
                    MessageMapper mapper = SpringContextHolder.getBean(MessageMapper.class);
                    List<cn.shu.wechat.beans.pojo.Message> messageList = mapper.selectByPage(messageItems.size(), messageItems.size() + PAGE_LENGTH, roomId);

                    //List<Message> messages = messageService.findOffset(roomId, messageItems.size(), PAGE_LENGTH);
                    //TODO
                    for (int i = 0 ; i < messageList.size(); i++) {
                        MessageItem item = new MessageItem(messageList.get(i), roomId);
                        messageItems.add(0, item);
                    }

                    messagePanel.getMessageListView().notifyItemRangeInserted(0, messageList.size());
                }
            }
        });

        JTextPane editor = messageEditorPanel.getEditor();
        Document document = editor.getDocument();

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // CTRL + 回车换行
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        document.insertString(editor.getCaretPosition(), "\n", null);
                    } catch (BadLocationException e1) {
                        e1.printStackTrace();
                    }
                }

                // 回车发送消息
                else if (!e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                    e.consume();
                }

                // 输入@，弹出选择用户菜单
                else if (e.getKeyChar() == '@') {
                    Point point = editor.getCaret().getMagicCaretPosition();
                    point = point == null ? new Point(10, 0) : point;
                    List<String> users = exceptSelfFromRoomMember();
                    users.add(0, "all");
                    remindUserPopup.setUsers(users);
                    remindUserPopup.show((Component) e.getSource(), point.x, point.y, roomId);
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
                            e1.printStackTrace();
                        }
                    }
                }
            }

        });

        remindUserPopup.setSelectedCallBack(new RemindUserPopup.UserSelectedCallBack() {
            @Override
            public void onSelected(String username) {
                JTextPane editor = messageEditorPanel.getEditor();
                editor.replaceSelection(username + " ");
            }
        });

        // 发送按钮
        messageEditorPanel.getSendButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // 上传文件按钮
        messageEditorPanel.getUploadFileLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("请选择上传文件或图片");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                fileChooser.showDialog(MainFrame.getContext(), "上传");
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    String path = selectedFile.getAbsolutePath();
                    prepareStartUploadFile(path, randomMessageId());
                    showSendingMessage();
                }

                super.mouseClicked(e);
            }
        });

        // 插入表情
        messageEditorPanel.setExpressionListener(new ExpressionListener() {
            @Override
            public void onSelected(String code) {
                editor.replaceSelection(code);
            }
        });
    }


    /**
     * 解析输入框中的内容并发送消息
     */
    private void sendMessage() {
        List<Object> inputDatas = parseEditorInput();
        boolean isImageOrFile = false;
        for (Object data : inputDatas) {
            //文本消息
            if (data instanceof String && !data.equals("\n")) {
                sendTextMessage(null, data.toString());
                RoomsPanel.getContext().updateRoomItem(roomId, 0,  data.toString(), System.currentTimeMillis());

            } else if (data instanceof JLabel) {
                //图片消息
                isImageOrFile = true;
                JLabel label = (JLabel) data;
                ImageIcon icon = (ImageIcon) label.getIcon();
                String path = icon.getDescription();
                if (path != null && !path.isEmpty()) {
                   /* sendFileMessage(path);
                    showSendingMessage();*/
                    //多个图片消息添加到队列中
                    shareAttachmentUploadQueue.add(path);
                }
                RoomsPanel.getContext().updateRoomItem(roomId, 0, "[图片]", System.currentTimeMillis());
            } else if (data instanceof FileEditorThumbnail) {
                //文件消息
                isImageOrFile = true;
                FileEditorThumbnail component = (FileEditorThumbnail) data;
                //多个文件消息添加到队列中
                shareAttachmentUploadQueue.add(component.getPath());
                RoomsPanel.getContext().updateRoomItem(roomId, 0, "[文件]", System.currentTimeMillis());

            }

        }
        //上传队列中的文件
        if (isImageOrFile) {
            // 先上传第一个图片/文件
            dequeueAndUpload();
        }

        messageEditorPanel.getEditor().setText("");
    }

    /**
     * 解析输入框中的输入数据
     *
     * @returnj
     */
    private List<Object> parseEditorInput() {
        List<Object> inputData = new ArrayList<>();

        Document doc = messageEditorPanel.getEditor().getDocument();
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
                            inputData.add(icon);
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
            inputData.add(messageEditorPanel.getEditor().getText());
        }

        return inputData;
    }

    /**
     * 从待上传附件队列中出队一个，并上传
     */
    public synchronized void dequeueAndUpload() {
        String path = shareAttachmentUploadQueue.poll();
        if (path != null) {
            prepareStartUploadFile(path, randomMessageId());
        }
    }

    /**
     * @return
     */
    private List<String> exceptSelfFromRoomMember() {
        List<String> users = new ArrayList<>(roomMembers);
        users.remove(Core.getUserSelf().getUsername());
        return users;
    }

    /**
     * 加载指定 firstMessageTimestamp 以后的消息
     *
     * @param firstMessageTimestamp
     */
    private void loadMessageWithEarliestTime(long firstMessageTimestamp) {
        List<cn.shu.wechat.swing.db.model.Message> messages = null;//messageService.findBetween(roomId, firstMessageTimestamp, System.currentTimeMillis());
        if (messages.size() > 0) {
            for (cn.shu.wechat.swing.db.model.Message message : messages) {
                if (!message.isDeleted()) {
                    //TODO
                    MessageItem item = new MessageItem(new cn.shu.wechat.beans.pojo.Message(), roomId);
                    this.messageItems.add(item);
                }
            }

            messagePanel.getMessageListView().notifyDataSetChanged(false);
            messagePanel.getMessageListView().setAutoScrollToTop();
        }

        // TODO: 从服务器获取本地最后一条消息之后的消息，并追回到消息列表
    }

    /**
     * 从数据库加载本地历史消息
     */
    private void loadLocalHistory() {
        ( (RoomChatPanelCard) this.getParentPanel()).getTitlePanel().showStatusLabel("加载中...");
        new SwingWorker<Object,Object>(){

            @Override
            protected Object doInBackground() throws Exception {
                MessageMapper mapper = SpringContextHolder.getBean(MessageMapper.class);
                List<cn.shu.wechat.beans.pojo.Message> messageList = mapper.selectByPage(messageItems.size(), messageItems.size() + PAGE_LENGTH, roomId);

                for (int i = messageList.size()-1 ; i >= 0; i--) {
                    Message message = messageList.get(i);
                    MessageItem item = new MessageItem(message, roomId);
                    item.setProgress(message.getIsSend()?100:0);
                    messageItems.add(item);
                }
                return null;
            }

            @Override
            protected void done() {
                messagePanel.getMessageListView().notifyDataSetChanged(false);

                messagePanel.getMessageListView().setAutoScrollToBottom();
                ( (RoomChatPanelCard) ChatPanel.this.getParentPanel()).getTitlePanel().hideStatusLabel();
            }
        }.execute();

    }

    /**
     * 更新数据库中的房间未读消息数，以及房间列表中的未读消息数
     *
     * @param count 消息数量
     */
    private void updateUnreadCount(int count) {
        //   room = roomService.findById(roomId);
        if (count < 0) {
            System.out.println(count);
        }
  /*      room.setUnreadCount(count);
        room.setTotalReadCount(room.getMsgSum());*/
        // roomService.update(room);

        // 通知UI更新未读消息数
        RoomsPanel.getContext().updateUnreadCount(roomId, count);
    }


    /**
     * 通知数据改变，需要重绘整个列表
     */
    public void notifyDataSetChanged() {
        messagePanel.getMessageListView().setVisible(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 重置ViewHolder缓存
                messageViewHolderCacheHelper.reset();

                messageItems.clear();
                initData();
                messagePanel.setVisible(true);
                messageEditorPanel.setVisible(true);
                messagePanel.getMessageListView().setVisible(true);

                TitlePanel.getContext().hideRoomMembersPanel();
            }
        }).start();
    }



    /**
     * 添加一条消息到消息列表最后
     *
     * @param lastMessage 消息
     */
    public void addMessageItemToEnd(Message lastMessage) {
        MessageItem messageItem = new MessageItem(lastMessage, roomId);
        addMessageItemToEnd(messageItem);
    }
    /**
     * 添加一条消息到消息列表最后
     *
     * @param messageItem 消息
     */
    public void addMessageItemToEnd(MessageItem messageItem) {
        this.messageItems.add(messageItem);
        messagePanel.getMessageListView().notifyItemInserted(messageItems.size() - 1, true);
        // 只有当滚动条在最底部最，新消到来后才自动滚动到底部
        JScrollBar scrollBar = messagePanel.getMessageListView().getVerticalScrollBar();
        if (scrollBar.getValue() == (scrollBar.getModel().getMaximum() - scrollBar.getModel().getExtent())) {
            messagePanel.getMessageListView().setAutoScrollToBottom();
        }
    }

    /**
     *  更新已有消息
     * @param lastMessage 消息
     */
    public void updateMessageItem(Message lastMessage) {
        // 已有消息更新状态
        int pos = findMessageItemPositionInViewReverse(lastMessage.getId());
        if (pos > -1) {
            MessageItem messageItem = messageItems.get(pos);
            messageItem.setNeedToResend(!lastMessage.getIsSend());
            messageItem.setProgress(lastMessage.getProcess());
            messagePanel.getMessageListView().notifyItemChanged(pos);
        }
    }

    /**
     * 发送文本消息
     * <p>
     * 如果messageId不为null, 则认为重发该消息，否则发送一条新的消息
     */
    public void sendTextMessage(String messageId, String content) {
        String msgId = randomMessageId();
        Message message = Message.builder().isSend(false)
                .id(msgId)
                .content(content)
                .plaintext(content)
                .createTime(new Date())
                .fromUsername(Core.getUserName())
                .toUsername(roomId)
                .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                .fromNickname(Core.getNickName())
                .process(50)
                .deleted(false)
                .isSend(true)
                .build();
        //消息列表添加消息块
        addMessageItemToEnd(message);
         new SwingWorker<WebWXSendMsgResponse, WebWXSendMsgResponse>() {
            private WebWXSendMsgResponse wxSendMsgResponse;

            @Override
            protected WebWXSendMsgResponse doInBackground() throws Exception {
                //后台发送消息
                wxSendMsgResponse = MessageTools.sendMsgByUserId(
                        MessageTools.Message.builder().content(content)
                                .messageId(msgId)
                                .plaintext(content)
                                .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT).build()
                        , roomId);
                return wxSendMsgResponse;
            }

            @Override
            protected void process(List<WebWXSendMsgResponse> chunks) {
                super.process(chunks);
            }

            @Override
            protected void done() {
                if (wxSendMsgResponse == null
                        || wxSendMsgResponse.getBaseResponse().getRet() != 0) {
                    message.setIsSend(false);
                    message.setProcess(100);
                } else {
                    message.setIsSend(true);
                    message.setProcess(100);
                }
                updateMessageItem(message);
            }
        }.execute();
        cn.shu.wechat.swing.db.model.Message dbMessage = null;
   /*     if (messageId == null)
        {
            MessageItem item = new MessageItem();
            if (content == null || content.equals(""))
            {
                return;
            }

            messageId = randomMessageId();
            item.setMessageContent(content);
            item.setTimestamp(System.currentTimeMillis());
            item.setSenderId(currentUser.getUserId());
            item.setSenderUsername(currentUser.getUsername());
            item.setId(messageId);
            item.setMessageType(MessageItem.RIGHT_TEXT);

            dbMessage = new Message();
            dbMessage.setId(messageId);
            dbMessage.setMessageContent(content);
            dbMessage.setRoomId(roomId);
            dbMessage.setSenderId(currentUser.getUserId());
            dbMessage.setSenderUsername(currentUser.getUsername());
            dbMessage.setTimestamp(item.getTimestamp());
            dbMessage.setNeedToResend(false);

            addMessageItemToEnd(item);

            messageService.insert(dbMessage);

        }*/
        // 已有消息重发
        // else
        // {
 /*           Message msg = messageService.findById(messageId);

            msg.setTimestamp(System.currentTimeMillis());
            msg.setUpdatedAt(0);
            msg.setNeedToResend(false);
            messageService.insertOrUpdate(msg);

            content = msg.getMessageContent();

            int pos = findMessageItemPositionInViewReverse(msg.getId());
            if (pos > -1)
            {
                messageItems.get(pos).setNeedToResend(false);
                messageItems.get(pos).setUpdatedAt(0);
                messageItems.get(pos).setTimestamp(System.currentTimeMillis());
                messagePanel.getMessageListView().notifyItemChanged(pos);
            }*/
        //  }

        // content = StringEscapeUtils.escapeJava(content);

        // TODO: 发送消息到服务器
        // 发送
        // sendToServer(content);

        // TODO：完善发送消息回调：
        boolean sentSuccess = true;
        if (sentSuccess) {
            // 如果发送成功，收到服务器响应后更新消息的updatedAt属性，该属性如果小于0，说明消息发送失败
/*            dbMessage.setUpdatedAt(System.currentTimeMillis());
            messageService.insertOrUpdate(dbMessage);*/
            // 更新消息列表


            // 更新房间信息以及房间列表
 /*           Room room = roomService.findById(dbMessage.getRoomId());
            room.setLastMessage(dbMessage.getMessageContent());
            room.setLastChatAt(dbMessage.getTimestamp());
            room.setMsgSum(room.getMsgSum() + 1);
            room.setUnreadCount(0);
            room.setTotalReadCount(room.getMsgSum());
            roomService.update(room);
            RoomsPanel.getContext().updateRoomsList(dbMessage.getRoomId());*/
        }


        // 10秒后如果发送不成功，则显示重发按钮
 /*       MessageResendTask task = new MessageResendTask();
        task.setListener(new ResendTaskCallback(10000)
        {
            @Override
            public void onNeedResend(String messageId)
            {
                Message msg = messageService.findById(messageId);
                if (msg.getUpdatedAt() == 0)
                {
                    // 更新消息列表
                    int pos = findMessageItemPositionInViewReverse(messageId);
                    if (pos > -1)
                    {
                        messageItems.get(pos).setNeedToResend(true);
                        msg.setNeedToResend(true);
                        messageService.update(msg);
                        messagePanel.getMessageListView().notifyItemChanged(pos);
                    }


                    // 更新房间列表
                    // 注意这里不能用类的成员room，因为可能已经离开了原来的房间
                    Room room = roomService.findById(msg.getRoomId());
                    room.setLastMessage("[有消息发送失败]");
                    room.setLastChatAt(msg.getTimestamp());
                    roomService.update(room);
                    RoomsPanel.getContext().updateRoomItem(msg.getRoomId());
                }
            }
        });
        task.execute(messageId);*/
    }

    private void showSendingMessage() {
        RoomsPanel.getContext().updateRoomItem(roomId, -1, "[发送中...]", System.currentTimeMillis());
    }

    /**
     * 倒序查找指定的消息在消息列表中的位置中的位置
     *
     * @param messageId
     * @return 查找成功，返回该消息在消息列表中的位置，否则返回-1
     */
    private int findMessageItemPositionInViewReverse(String messageId) {
        for (int i = messageItems.size() - 1; i >= 0; i--) {
            // 找到消息列表中对应的消息
            if (messageId.equals(messageItems.get(i).getId())) {
                return i;
            }
        }

        return -1;
    }

    /**
     * 随机生成MessageId
     *
     * @return
     */
    private String randomMessageId() {
        String raw = UUID.randomUUID().toString().replace("-", "");
        return raw;
    }

    /**
     * 发送文件消息
     *
     * @param path
     */
    private void sendFileMessage(String path) {

        prepareStartUploadFile(path, randomMessageId());

    }

    /**
     * 重发文件消息
     *
     * @param messageId
     * @param type
     */
    public void resendFileMessage(String messageId, String type) {
        cn.shu.wechat.swing.db.model.Message dbMessage = null;//= messageService.findById(messageId);
        String path = null;

        if (type.equals("file")) {
            if (dbMessage.getFileAttachmentId() != null) {
              //  path = fileAttachmentService.findById(dbMessage.getFileAttachmentId()).getLink();
            } else {
                path = null;
            }
        } else {
            if (dbMessage.getImageAttachmentId() != null) {
               // path = imageAttachmentService.findById(dbMessage.getImageAttachmentId()).getImagePath();
            } else {
                path = null;

            }
        }


        if (path != null) {
            int index = findMessageItemPositionInViewReverse(messageId);

            if (index > -1) {
                messageItems.remove(index);
                messagePanel.getMessageListView().notifyItemRemoved(index);
               // messageService.delete(dbMessage.getId());
            }

            prepareStartUploadFile(path, randomMessageId());
            showSendingMessage();
        }
    }

    /**
     * 通知开始上传文件
     *
     * @param uploadFilename
     * @param fileId
     */
    public void prepareStartUploadFile(String uploadFilename, String fileId) {
        uploadFile(uploadFilename, fileId);
        uploadingOrDownloadingFiles.add(fileId);
    }

    /**
     * 上传文件
     *
     * @param uploadFilename
     */
    private void uploadFile(String uploadFilename, String fileId) {
        File file = new File(uploadFilename);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "文件不存在", "上传失败", JOptionPane.ERROR_MESSAGE);
        }
        //新增消息项
        final MessageItem item = new MessageItem();
        String type = MimeTypeUtil.getMime(uploadFilename.substring(uploadFilename.lastIndexOf(".")));
        final boolean isImage = type.startsWith("image/");

        // 发送的是图片
        int[] bounds;
        String name = uploadFilename.substring(uploadFilename.lastIndexOf(File.separator) + 1); // 文件名

        FileAttachment fileAttachment = null;
        ImageAttachment imageAttachment = null;
        cn.shu.wechat.swing.db.model.Message dbMessage = new cn.shu.wechat.swing.db.model.Message();
        dbMessage.setProgress(-1);

        if (isImage) {
            bounds = getImageSize(uploadFilename);
            imageAttachment = new ImageAttachment();
            imageAttachment.setId(fileId);
            imageAttachment.setWidth(bounds[0]);
            imageAttachment.setHeight(bounds[1]);
            imageAttachment.setImagePath(uploadFilename);
            imageAttachment.setTitle(name);
            item.setImageAttachment(new ImageAttachmentItem(imageAttachment));
            dbMessage.setImageAttachmentId(imageAttachment.getId());
            // imageAttachmentService.insertOrUpdate(imageAttachment);

            item.setMessageType(MessageItem.RIGHT_IMAGE);
        } else {

            fileAttachment = new FileAttachment();
            fileAttachment.setId(fileId);
            System.out.println(File.separator);
            fileAttachment.setLink(uploadFilename);
            fileAttachment.setTitle(name);
            item.setFileAttachment(new FileAttachmentItem(fileAttachment));
            dbMessage.setFileAttachmentId(fileAttachment.getId());
            //  fileAttachmentService.insertOrUpdate(fileAttachment);

            item.setMessageType(MessageItem.RIGHT_ATTACHMENT);
        }

        final String messageId = randomMessageId();
        item.setMessageContent(name);
        item.setTimestamp(System.currentTimeMillis());
        item.setSenderId(Core.getUserSelf().getUsername());
        item.setSenderUsername(Core.getUserSelf().getNickname());
        item.setId(messageId);
        item.setProgress(0);
        //添加消息
        addMessageItemToEnd(item);


        new SwingWorker<Void, Integer>() {

            @Override
            protected Void doInBackground() throws Exception {

                //文件上传回调函数
                UploadTaskCallback callback = new UploadTaskCallback() {
                    @Override
                    public void onTaskSuccess(int curr, int size) {
                        int progress = (int) (((curr*1.0f) / size) * 100);
                        publish(progress);
                    }

                    @Override
                    public void onTaskError() {
                    }
                };

                //发送消息 等待回调
                MessageTools.sendMsgByUserId(MessageTools.Message
                        .builder()
                        .filePath(uploadFilename)
                        .slavePath(uploadFilename)
                        .plaintext(isImage?"[图片]":"[文件]")
                        .messageId(messageId)
                        .replyMsgTypeEnum(isImage ? WXSendMsgCodeEnum.PIC : WXSendMsgCodeEnum.APP)
                        .toUserName(roomId)
                        .build(), roomId, callback);

                return null;

            }

            @Override
            protected void process(List<Integer> chunks) {
                Integer progress = chunks.get(chunks.size() - 1);
                // 上传完成
                if (progress == 100) {
                    uploadingOrDownloadingFiles.remove(fileId);
                    RoomsPanel.getContext().updateRoomItem(roomId, -1, dbMessage.getMessageContent(), System.currentTimeMillis());

                    if (uploadFilename.startsWith(ClipboardUtil.CLIPBOARD_TEMP_DIR)) {
                        File file = new File(uploadFilename);
                        //file.delete();
                    }
                }
                for (int i = messageItems.size() - 1; i >= 0; i--) {
                    if (messageItems.get(i).getId().equals(item.getId())) {
                        messageItems.get(i).setProgress(progress);
                        // messageService.updateProgress(messageItems.get(i).getId(), progress);


                        BaseMessageViewHolder viewHolder = getViewHolderByPosition(i);
                        if (viewHolder != null) {
                            if (isImage) {
                                MessageRightImageViewHolder holder = (MessageRightImageViewHolder) viewHolder;
                                if (progress >= 100) {
                                    holder.sendingProgress.setVisible(false);
                                }else{
                                    holder.sendingProgress.setVisible(true);
                                }
                            } else {
                                MessageRightAttachmentViewHolder holder = (MessageRightAttachmentViewHolder) viewHolder;

                                // 隐藏"等待上传"，并显示进度条
                                holder.sizeLabel.setVisible(false);
                                holder.progressBar.setVisible(true);
                                holder.progressBar.setValue(progress);

                                if (progress >= 100) {
                                    holder.progressBar.setVisible(false);
                                    holder.sizeLabel.setVisible(true);
                                    holder.sizeLabel.setText(fileCache.fileSizeString(uploadFilename));
                                }
                            }

                        }
                        break;
                    }
                }
            }

            @Override
            protected void done() {
                super.done();
            }
        }.execute();


    }

    private void sendDataPart(int partIndex, List<byte[]> dataParts, UploadTaskCallback callback) {
        // TODO： 发送第 partIndex 个分块到服务器
        //send(dataParts(partIndex))

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("发送第" + partIndex + "个分块，共" + dataParts.size() + "个分块");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // callback.onTaskSuccess();
            }
        }).start();

    }

    /**
     * 获取图片的宽高
     *
     * @param file
     * @return
     */
    private int[] getImageSize(String file) {
        try {
            BufferedImage image = ImageIO.read(new File(file));
            int width = image.getWidth();
            int height = image.getHeight();
            return new int[]{width, height};
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new int[]{0, 0};
    }

    /**
     * 分割大文件，分块上传
     *
     * @param file
     * @return
     */
    private static List<byte[]> cuttingFile(File file) {
        long size = file.length();

        int partSize = 512000;
        //int partSize = 4140;
        int blockCount;
        blockCount = (int) (size % partSize == 0 ? size / partSize : size / partSize + 1);
        List<byte[]> dataParts = new ArrayList<>(blockCount);
        try {
            byte[] buffer = new byte[partSize];
            int len;
            FileInputStream inputStream = new FileInputStream(file);

            while ((len = inputStream.read(buffer)) > -1) {
                byte[] dataPart = Arrays.copyOf(buffer, len);
                dataParts.add(dataPart);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return dataParts;
    }

    private BaseMessageViewHolder getViewHolderByPosition(int position) {
        if (position < 0) {
            return null;
        }

        try {
            return (BaseMessageViewHolder) messagePanel.getMessageListView().getItem(position);
        } catch (Exception e) {
            return null;
        }
    }

    public static void openFile(String filePath){
        if (filePath == null) {
            JOptionPane.showMessageDialog(null, "文件不存在", "打开失败", JOptionPane.ERROR_MESSAGE);
        } else {
            openFileWithDefaultApplication(filePath);
        }
    }
    /**
     * 打开文件，如果文件不存在，则下载
     *
     * @param messageId 数据库主键 消息id
     */
    public static void downloadOrOpenFile(String messageId) {
        MessageMapper messageMapper = SpringContextHolder.getBean(MessageMapper.class);
        Message message = messageMapper.selectByPrimaryKey(messageId);
        FileAttachment fileAttachment;
/*        if (message == null) {
            // 如果没有messageId对应的message, 尝试寻找messageId对应的file attachment，因为在自己上传文件时，此时是以fileId作为临时的messageId
            fileAttachment = fileAttachmentService.findById(messageId);
        } else {
            fileAttachment = fileAttachmentService.findById(message.getFileAttachmentId());
        }*/

  /*      if (fileAttachment == null) {
            JOptionPane.showMessageDialog(null, "无效的附件消息", "消息无效", JOptionPane.ERROR_MESSAGE);
            return;
        }*/

        openFile(message.getFilePath());
   /*     String filepath = fileCache.tryGetFileCache(fileAttachment.getId(), fileAttachment.getTitle());
        if (filepath == null) {
            // 服务器上的文件
            if (fileAttachment.getLink().startsWith("/file-upload")) {
                downloadFile(fileAttachment, messageId);
            }
            // 本地的文件
            else {
                openFileWithDefaultApplication(fileAttachment.getLink());
            }
        } else {
            openFileWithDefaultApplication(filepath);
        }*/
    }

    /**
     * 下载文件
     *
     * @param fileAttachment
     * @param messageId
     */
    private void downloadFile(FileAttachment fileAttachment, String messageId) {
        final DownloadTask task = new DownloadTask(new HttpUtil.ProgressListener() {
            @Override
            public void onProgress(int progress) {
                int pos = findMessageItemPositionInViewReverse(messageId);
                MessageAttachmentViewHolder holder = (MessageAttachmentViewHolder) getViewHolderByPosition(pos);

                if (pos < 0 || holder == null) {
                    return;
                }

                if (progress >= 0 && progress < 100) {
                    if (holder.sizeLabel.isVisible()) {
                        holder.sizeLabel.setVisible(false);
                    }
                    if (!holder.progressBar.isVisible()) {
                        holder.progressBar.setVisible(true);
                    }

                    holder.progressBar.setValue(progress);
                } else if (progress >= 100) {
                    holder.progressBar.setVisible(false);
                    holder.sizeLabel.setVisible(true);
                }
            }
        });

        task.setListener(new HttpResponseListener<byte[]>() {
            @Override
            public void onSuccess(byte[] data) {
                //System.out.println(data);
                String path = fileCache.cacheFile(fileAttachment.getId(), fileAttachment.getTitle(), data);

                int pos = findMessageItemPositionInViewReverse(messageId);
                MessageAttachmentViewHolder holder = (MessageAttachmentViewHolder) getViewHolderByPosition(pos);

                if (pos < 0 || holder == null) {
                    return;
                }
                if (path == null) {
                    holder.sizeLabel.setVisible(true);
                    holder.sizeLabel.setText("文件获取失败");
                    holder.progressBar.setVisible(false);
                } else {
                    holder.sizeLabel.setVisible(true);
                    System.out.println("文件已缓存在 " + path);
                    holder.sizeLabel.setText(fileCache.fileSizeString(path));
                }
            }

            @Override
            public void onFailed() {
                int pos = findMessageItemPositionInViewReverse(messageId);
                MessageAttachmentViewHolder holder = (MessageAttachmentViewHolder) getViewHolderByPosition(pos);
                holder.sizeLabel.setVisible(true);
                holder.sizeLabel.setText("文件获取失败");
                holder.progressBar.setVisible(false);
            }
        });

        //String url = Launcher.HOSTNAME + fileAttachment.getLink() + "?rc_uid=" + Core.getUserSelf().getUsername() + "&rc_token=" + currentUser.getAuthToken();
        // task.execute(url);
    }

    /**
     * 使用默认程序打开文件
     *
     * @param path
     */
    public static void openFileWithDefaultApplication(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, "文件打开失败，没有找到关联的应用程序", "打开失败", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        } catch (IllegalArgumentException e2) {
            JOptionPane.showMessageDialog(null, "文件不存在，可能已被删除", "打开失败", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * 删除消息
     *
     * @param messageId
     */
    public void deleteMessage(String messageId) {
        int pos = findMessageItemPositionInViewReverse(messageId);
        if (pos > -1) {
            messageItems.remove(pos);
            messagePanel.getMessageListView().notifyItemRemoved(pos);
            //messageService.markDeleted(messageId);
        }
    }

    /**
     * 粘贴
     */
    public void paste() {
        messageEditorPanel.getEditor().paste();
        messageEditorPanel.getEditor().requestFocus();
    }

}