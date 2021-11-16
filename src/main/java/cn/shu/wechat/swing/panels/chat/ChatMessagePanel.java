package cn.shu.wechat.swing.panels.chat;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.pojo.dto.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.swing.adapter.ViewHolder;
import cn.shu.wechat.swing.adapter.message.BaseMessageViewHolder;
import cn.shu.wechat.swing.adapter.message.MessageAdapter;
import cn.shu.wechat.swing.adapter.message.app.MessageRightAttachmentViewHolder;
import cn.shu.wechat.swing.adapter.message.image.MessageRightImageViewHolder;
import cn.shu.wechat.swing.adapter.message.video.MessageRightVideoViewHolder;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCBorder;
import cn.shu.wechat.swing.components.message.FileEditorThumbnail;
import cn.shu.wechat.swing.components.message.RemindUserPopup;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.helper.MessageViewHolderCacheHelper;
import cn.shu.wechat.swing.listener.ExpressionListener;
import cn.shu.wechat.swing.panels.ParentAvailablePanel;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;
import cn.shu.wechat.swing.panels.TitlePanel;
import cn.shu.wechat.swing.tasks.UploadTaskCallback;
import cn.shu.wechat.swing.utils.FileCache;
import cn.shu.wechat.swing.utils.ImageUtil;
import cn.shu.wechat.swing.utils.MimeTypeUtil;
import cn.shu.wechat.utils.DateUtils;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.MediaUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
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
public class ChatMessagePanel extends ParentAvailablePanel {
    /**
     * 消息面板
     */
    private ChatMessageViewerPanel chatMessageViewerPanel;

    public ChatMessageEditorPanel getMessageEditorPanel() {
        return chatMessageEditorPanel;
    }

    /**
     * 消息输入框
     */
    private ChatMessageEditorPanel chatMessageEditorPanel;

    /**
     * 消息列表
     */
    private final List<Message> messageItems = new ArrayList<>();

    /**
     * 消息适配器
     */
    private MessageAdapter adapter;


    /**
     * 当前房间id
     */
    private final String roomId;


    public void setRoomMembers(List<String> roomMembers) {
        this.roomMembers = roomMembers;
    }

    /**
     * 房间的用户 username列表
     */
    private List<String> roomMembers = new ArrayList<>();


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

    private volatile boolean isLoadHis = false;

    public ChatMessagePanel(JPanel parent, String roomId) {

        super(parent);
        this.roomId = roomId;
        if (StringUtils.isEmpty(roomId)) {
            throw new NullPointerException("roomid can not be null.");
        }
        messageViewHolderCacheHelper = new MessageViewHolderCacheHelper();

        initComponents();
        initView();
        setListeners();
    }

    private void initComponents() {

        chatMessageViewerPanel = new ChatMessageViewerPanel(this);
        chatMessageViewerPanel.setBorder(new RCBorder(RCBorder.BOTTOM, Colors.LIGHT_GRAY));
        adapter = new MessageAdapter(this,messageItems, chatMessageViewerPanel.getMessageListView(), messageViewHolderCacheHelper);
        chatMessageViewerPanel.getMessageListView().setAdapter(adapter);

        chatMessageEditorPanel = new ChatMessageEditorPanel(this, roomId);
        chatMessageEditorPanel.setPreferredSize(new Dimension(MainFrame.DEFAULT_WIDTH, MainFrame.DEFAULT_WIDTH / 4));
    }


    private void initView() {
        this.setLayout(new GridBagLayout());
        add(chatMessageViewerPanel, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 4));
        add(chatMessageEditorPanel, new GBC(0, 1).setFill(GBC.BOTH).setWeight(1, 1));

    }


    private void setListeners() {
        //暂时不加载历史消息，无意义
        /*chatMessageViewerPanel.getMessageListView().setScrollToTopListener(new RCListView.ScrollToTopListener() {
            @Override
            public void onScrollToTop() {
                // 当滚动到顶部时，继续拿前面的消息
                if (isLoadHis) {
                    return;
                }
                if (roomId != null) {
                    isLoadHis = true;
                    ((ChatPanel) ChatMessagePanel.this.getParentPanel()).getTitlePanel().showStatusLabel("加载中...");

                    new SwingWorker<Object, Object>() {
                        List<Message> messageList = null;

                        @Override
                        protected Object doInBackground() {
                            MessageMapper mapper = SpringContextHolder.getBean(MessageMapper.class);
                            Contacts contacts = Core.getMemberMap().get(roomId);
                            String remarkName = ContactsTools.getContactRemarkNameByUserName(contacts);
                            String nickName = ContactsTools.getContactNickNameByUserName(contacts);
                            messageList = mapper.selectByPage(messageItems.size(), messageItems.size() + PAGE_LENGTH, roomId, remarkName, nickName);
                            messageItems.addAll(messageList);
                            return null;
                        }

                        @Override
                        protected void done() {
                            try {

                                if (messageList != null && !messageList.isEmpty()) {
                                    //TODO 顺序有问题
                                    chatMessageViewerPanel.getMessageListView().notifyItemRangeInserted(0, messageList.size());
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                isLoadHis = false;
                                ((ChatPanel) ChatMessagePanel.this.getParentPanel()).getTitlePanel().hideStatusLabel();
                            }

                        }
                    }.execute();
                }
            }
        });
*/
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
                JTextPane editor = chatMessageEditorPanel.getEditor();
                editor.replaceSelection(username + " ");
            }
        });

        // 发送按钮
        chatMessageEditorPanel.getSendButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // 上传文件按钮
        chatMessageEditorPanel.getUploadFileLabel().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("请选择上传文件或图片");
                fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

                fileChooser.showDialog(MainFrame.getContext(), "上传");
                File selectedFile = fileChooser.getSelectedFile();
                if (selectedFile != null) {
                    String path = selectedFile.getAbsolutePath();
                    if (new File(path).length() > MessageTools.maxFileSize) {
                        JOptionPane.showMessageDialog(MainFrame.getContext(), "只能上传20M以内文件", "文件太大", JOptionPane.ERROR_MESSAGE);
                    } else {
                        sendFileMessage(path);
                        showSendingMessage();
                    }

                }

                super.mouseClicked(e);
            }
        });

        // 插入表情
        chatMessageEditorPanel.setExpressionListener(new ExpressionListener() {
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
            if (data instanceof String && !"\n".equals(data)) {
                if (StringUtils.isEmpty(data)) {
                    continue;
                }
                    //文本消息
                    sendTextMessage(data.toString());

            } else if (data instanceof JLabel) {
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
                        , System.currentTimeMillis(),ContactsTools.isMute(roomId),false);
            } else if (data instanceof FileEditorThumbnail) {
                //文件消息
                isImageOrFile = true;
                FileEditorThumbnail component = (FileEditorThumbnail) data;
                //多个文件消息添加到队列中
                shareAttachmentUploadQueue.add(component.getPath());
                RoomsPanel.getContext().updateRoomItem(roomId, 0, "[文件]发送中...", System.currentTimeMillis(),ContactsTools.isMute(roomId),false);

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
            inputData.add(chatMessageEditorPanel.getEditor().getText());
        }

        return inputData;
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
     * @return
     */
    private List<String> exceptSelfFromRoomMember() {
        List<String> users = new ArrayList<>(roomMembers);
        users.remove(Core.getUserSelf().getUsername());
        return users;
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
                List<cn.shu.wechat.pojo.entity.Message> messageList = mapper.selectByPage(messageItems.size(), messageItems.size() + PAGE_LENGTH, roomId, remarkName, nickName);
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
        chatMessageViewerPanel.getMessageListView().setVisible(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 重置ViewHolder缓存
                messageViewHolderCacheHelper.reset();

                messageItems.clear();
                chatMessageViewerPanel.setVisible(true);
                chatMessageEditorPanel.setVisible(true);
                chatMessageViewerPanel.getMessageListView().setVisible(true);

                TitlePanel.getContext().hideRoomMembersPanel();
            }
        }).start();
    }


    /**
     * 添加一条消息到消息列表最后
     *
     * @param messageItem 消息
     */
    public ViewHolder addMessageToEnd(Message messageItem) {
        this.messageItems.add(messageItem);
        ViewHolder holder = chatMessageViewerPanel.getMessageListView().notifyItemInserted(messageItems.size() - 1, true);
        // 只有当滚动条在最底部最，新消到来后才自动滚动到底部
        JScrollBar scrollBar = chatMessageViewerPanel.getMessageListView().getVerticalScrollBar();
        if (scrollBar.getValue() == (scrollBar.getModel().getMaximum() - scrollBar.getModel().getExtent())) {
            chatMessageViewerPanel.getMessageListView().setAutoScrollToBottom();
        }
        return holder;
    }


    /**
     * 更新已有消息
     *
     * @param lastMessage 消息
     */
    public void updateMessage(ViewHolder viewHolder, Message lastMessage) {
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
        RoomsPanel.getContext().updateRoomItem(roomId, 0, content+"[发送中...]", System.currentTimeMillis(),ContactsTools.isMute(roomId),false);
        String msgId = MessageTools.randomMessageId();
        Message message = Message.builder().isSend(false)
                .id(msgId)
                .content(content)
                .plaintext(content)
                .createTime(DateUtils.getCurrDateString(DateUtils.YYYY_MM_DD_HH_MM_SS))
                .fromUsername(Core.getUserName())
                .toUsername(roomId)
                .msgType(WXReceiveMsgCodeEnum.MSGTYPE_TEXT.getCode())
                .fromNickname(Core.getNickName())
                .progress(50)
                .timestamp(System.currentTimeMillis())
                .deleted(false)
                .isSend(true)
                .isNeedToResend(false)
                .build();
        //绘制消息项
        ViewHolder viewHolder = addMessageToEnd(message);
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
                    RoomsPanel.getContext().updateRoomItem(roomId, 0, content+"[发送失败]", System.currentTimeMillis(),ContactsTools.isMute(roomId),false);

                } else {
                    message.setNeedToResend(false);
                    RoomsPanel.getContext().updateRoomItem(roomId, 0, content, System.currentTimeMillis(),ContactsTools.isMute(roomId),false);

                }
                updateMessage(viewHolder, message);
            }
        }.execute();
    }

    private void showSendingMessage() {
        RoomsPanel.getContext().updateRoomItem(roomId, 0, "[发送中...]", System.currentTimeMillis(),ContactsTools.isMute(roomId),false);
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
        String msgId =  MessageTools.randomMessageId();
        File file = new File(uploadFilename);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "文件不存在", "上传失败", JOptionPane.ERROR_MESSAGE);
        }

        String mime = MimeTypeUtil.getMime(uploadFilename.substring(uploadFilename.lastIndexOf(".")));
        WXReceiveMsgCodeEnum msgType = WXReceiveMsgCodeEnum.MSGTYPE_APP;
        WXReceiveMsgCodeOfAppEnum fileOfAppType = WXReceiveMsgCodeOfAppEnum.FILE;
        if (mime == null) {
            msgType = WXReceiveMsgCodeEnum.MSGTYPE_APP;
            mime = "app";
        } else if (mime.startsWith("image/")) {
            msgType = WXReceiveMsgCodeEnum.MSGTYPE_IMAGE;
        } else if (mime.startsWith("video/")) {
            msgType = WXReceiveMsgCodeEnum.MSGTYPE_VIDEO;
        }
        //新增消息项
        Message message = null;
        // 发送的是图片
        Dimension imageSize;
        String fileName = uploadFilename.substring(uploadFilename.lastIndexOf(File.separator) + 1); // 文件名

        switch (msgType) {
            case MSGTYPE_IMAGE:
                imageSize = ImageUtil.getImageSize(uploadFilename);
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
        message.setTimestamp(System.currentTimeMillis());
        //添加消息 到面板
        ViewHolder viewHolder = addMessageToEnd(message);

        Message finalMessage = message;
        WXReceiveMsgCodeEnum finalMsgType = msgType;
        new SwingWorker<Void, Integer>() {
            private WebWXSendMsgResponse wxSendMsgResponse;
            @Override
            protected Void doInBackground() throws Exception {

                //文件上传回调函数
                UploadTaskCallback callback = new UploadTaskCallback() {
                    @Override
                    public void onTaskSuccess(int curr, int size) {
                        int progress = (int) (((curr * 1.0f) / size) * 100);
                        publish(progress);
                    }

                    @Override
                    public void onTaskError() {
                    }
                };
                //发送消息 等待回调
                wxSendMsgResponse = MessageTools.sendMsgByUserId(finalMessage, callback);

                return null;

            }

            @Override
            protected void process(List<Integer> chunks) {
                Integer progress = chunks.get(chunks.size() - 1);
                // 上传完成
                if (progress == 100) {
                    RoomsPanel.getContext().updateRoomItem(roomId, 0, finalMessage.getPlaintext(), System.currentTimeMillis(),ContactsTools.isMute(roomId),false);
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
                            holder.sizeLabel.setVisible(false);
                            holder.progressBar.setVisible(true);
                            holder.progressBar.setValue(progress);

                            if (progress >= 100) {
                                holder.progressBar.setVisible(false);
                                holder.sizeLabel.setVisible(true);
                                holder.sizeLabel.setText(FileCache.fileSizeString(uploadFilename));
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

    private BaseMessageViewHolder getViewHolderByPosition(int position) {
        if (position < 0) {
            return null;
        }

        try {
            return (BaseMessageViewHolder) chatMessageViewerPanel.getMessageListView().getItem(position);
        } catch (Exception e) {
            return null;
        }
    }

    public static void openFile(String filePath) {
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

        openFile(message.getFilePath());
    }

    /*  *//**
     * 下载文件
     *
     * @param fileAttachment
     * @param messageId
     *//*
    private void downloadFile(FileAttachment fileAttachment, String messageId) {
        final DownloadTask task = new DownloadTask(new HttpUtil.ProgressListener() {
            @Override
            public void onProgress(int progress) {
                int pos = findMessagePositionInViewReverse(messageId);
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

                int pos = findMessagePositionInViewReverse(messageId);
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
                int pos = findMessagePositionInViewReverse(messageId);
                MessageAttachmentViewHolder holder = (MessageAttachmentViewHolder) getViewHolderByPosition(pos);
                holder.sizeLabel.setVisible(true);
                holder.sizeLabel.setText("文件获取失败");
                holder.progressBar.setVisible(false);
            }
        });

        //String url = Launcher.HOSTNAME + fileAttachment.getLink() + "?rc_uid=" + Core.getUserSelf().getUsername() + "&rc_token=" + currentUser.getAuthToken();
        // task.execute(url);
    }*/

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
                    Desktop.getDesktop().open(new File(path));
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog(null, "文件打开失败，没有找到关联的应用程序", "打开失败", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                } catch (IllegalArgumentException e2) {
                    JOptionPane.showMessageDialog(null, "文件不存在，可能已被删除", "打开失败", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }


    /**
     * 删除消息
     *
     * @param messageId
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

}