package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.ImageViewer.ImageViewerFrame;
import cn.shu.wechat.swing.adapter.BaseAdapter;
import cn.shu.wechat.swing.adapter.ViewHolder;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.components.RCProgressBar;
import cn.shu.wechat.swing.components.UserInfoPopup;
import cn.shu.wechat.swing.components.message.*;
import cn.shu.wechat.swing.db.model.Message;
import cn.shu.wechat.swing.entity.*;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.helper.AttachmentIconHelper;
import cn.shu.wechat.swing.helper.MessageViewHolderCacheHelper;
import cn.shu.wechat.swing.panels.ChatPanel;
import cn.shu.wechat.swing.panels.RoomChatPanel;
import cn.shu.wechat.swing.utils.*;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SleepUtils;
import javazoom.jl.player.Player;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by 舒新胜 on 17-6-2.
 */
public class MessageAdapter extends BaseAdapter<BaseMessageViewHolder> {
    private final List<MessageItem> messageItems;
    private final RCListView listView;
    private final AttachmentIconHelper attachmentIconHelper = new AttachmentIconHelper();
    private final ImageCache imageCache;

    private final FileCache fileCache;
    private final MessagePopupMenu popupMenu = new MessagePopupMenu();


    MessageViewHolderCacheHelper messageViewHolderCacheHelper;

    public MessageAdapter(List<MessageItem> messageItems, RCListView listView, MessageViewHolderCacheHelper messageViewHolderCacheHelper) {
        this.messageItems = messageItems;
        this.listView = listView;

        // currentUser = currentUserService.findAll().get(0);
        imageCache = new ImageCache();
        fileCache = new FileCache();
        this.messageViewHolderCacheHelper = messageViewHolderCacheHelper;
    }

    @Override
    public int getItemViewType(int position) {
        return messageItems.get(position).getMessageType();
    }

    @Override
    public boolean isGroup(int position) {
        return messageItems.get(position).getSenderId().startsWith("@@");
    }

    @Override
    public BaseMessageViewHolder onCreateViewHolder(int viewType, int position) {
        MessageItem messageItem = messageItems.get(position);
        switch (viewType) {
            case MessageItem.SYSTEM_MESSAGE: {
                MessageSystemMessageViewHolder holder = messageViewHolderCacheHelper.tryGetSystemMessageViewHolder();
                if (holder == null) {
                    holder = new MessageSystemMessageViewHolder();
                }

                return holder;
            }
            case MessageItem.RIGHT_TEXT: {
                MessageRightTextViewHolder holder = messageViewHolderCacheHelper.tryGetRightTextViewHolder();
                if (holder == null) {
                    holder = new MessageRightTextViewHolder();
                }

                return holder;
            }
            case MessageItem.LEFT_TEXT: {
                MessageLeftTextViewHolder holder = messageViewHolderCacheHelper.tryGetLeftTextViewHolder();
                if (holder == null) {
                    holder = new MessageLeftTextViewHolder(messageItem.isGroupable());
                }

                return holder;
            }
            case MessageItem.RIGHT_IMAGE: {
                MessageRightImageViewHolder holder = messageViewHolderCacheHelper.tryGetRightImageViewHolder();
                if (holder == null) {
                    holder = new MessageRightImageViewHolder();
                }

                return holder;
            }
            case MessageItem.LEFT_IMAGE: {
                MessageLeftImageViewHolder holder = messageViewHolderCacheHelper.tryGetLeftImageViewHolder();
                if (holder == null) {
                    holder = new MessageLeftImageViewHolder(messageItem.isGroupable());
                }

                return holder;
            }
            case MessageItem.LEFT_VIDEO: {
                MessageLeftVideoViewHolder holder = messageViewHolderCacheHelper.tryGetLeftVideoViewHolder();
                if (holder == null) {
                    holder = new MessageLeftVideoViewHolder(messageItem.isGroupable(),
                            ImageUtil.getScaleDimen(messageItem.getVideoAttachmentItem().getSalveImgWidth()
                                    , messageItem.getVideoAttachmentItem().getSalveImgHeight()));
                }

                return holder;
            }
            case MessageItem.RIGHT_VIDEO: {
                MessageRightVideoViewHolder holder = messageViewHolderCacheHelper.tryGetRightVideoViewHolder();
                if (holder == null) {
                    holder = new MessageRightVideoViewHolder(
                            ImageUtil.getScaleDimen(messageItem.getVideoAttachmentItem().getSalveImgWidth()
                                    , messageItem.getVideoAttachmentItem().getSalveImgHeight()));
                }

                return holder;
            }
            case MessageItem.LEFT_LINK: {
                return new MessageLeftLinkViewHolder(messageItem.isGroupable());
            }
            case MessageItem.RIGHT_LINK: {
                return new MessageRightLinkViewHolder();
            }
            case MessageItem.LEFT_VOICE: {
                MessageLeftVoiceViewHolder holder = messageViewHolderCacheHelper.tryGetLeftVoiceViewHolder();
                if (holder == null) {
                    holder = new MessageLeftVoiceViewHolder(messageItem.isGroupable());
                }

                return holder;
            }
            case MessageItem.RIGHT_VOICE: {
                MessageRightVoiceViewHolder holder = messageViewHolderCacheHelper.tryGetRightVoiceViewHolder();
                if (holder == null) {
                    holder = new MessageRightVoiceViewHolder();
                }

                return holder;
            }
            case MessageItem.RIGHT_ATTACHMENT: {
                MessageRightAttachmentViewHolder holder = messageViewHolderCacheHelper.tryGetRightAttachmentViewHolder();
                if (holder == null) {
                    holder = new MessageRightAttachmentViewHolder();
                }

                return holder;
            }
            case MessageItem.LEFT_ATTACHMENT: {
                MessageLeftAttachmentViewHolder holder = messageViewHolderCacheHelper.tryGetLeftAttachmentViewHolder();
                if (holder == null) {
                    holder = new MessageLeftAttachmentViewHolder(messageItem.isGroupable());
                }

                return holder;
            }
            default:
        }

        return null;
    }

    @Override
    public void onBindViewHolder(BaseMessageViewHolder viewHolder, int position) {
        if (viewHolder == null) {
            return;
        }

        final MessageItem item = messageItems.get(position);
        MessageItem preItem = position == 0 ? null : messageItems.get(position - 1);

        processTimeAndAvatar(item, preItem, viewHolder);

        if (viewHolder instanceof MessageSystemMessageViewHolder) {
            processSystemMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageRightTextViewHolder) {
            processRightTextMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftTextViewHolder) {
            processLeftTextMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageRightImageViewHolder) {
            processRightImageMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftVideoViewHolder) {
            processLeftVideoMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageRightVideoViewHolder) {
            processRightVideoMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftVoiceViewHolder) {
            processLeftVoiceMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageRightVoiceViewHolder) {
            processRightVoiceMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftImageViewHolder) {
            processLeftImageMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageRightAttachmentViewHolder) {
            processRightAttachmentMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftAttachmentViewHolder) {
            processLeftAttachmentMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageRightLinkViewHolder) {
            processRightLinkMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftLinkViewHolder) {
            processLeftLinkMessage(viewHolder, item);
        }
    }

    /**
     * 处理系统消息
     *
     * @param viewHolder
     * @param item
     */
    private void processSystemMessage(ViewHolder viewHolder, MessageItem item) {
        MessageSystemMessageViewHolder holder = (MessageSystemMessageViewHolder) viewHolder;
        holder.text.setText(item.getMessageContent());
    }

    /**
     * 其它用户的附件消息
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftAttachmentMessage(ViewHolder viewHolder, MessageItem item) {
        MessageLeftAttachmentViewHolder holder = (MessageLeftAttachmentViewHolder) viewHolder;
        FileAttachmentItem fileAttachment = item.getFileAttachment();

        String filePath = fileAttachment.getFilePath();

        Map<String, Object> map = new HashMap<>();
        map.put("attachmentId", fileAttachment.getId());
        map.put("name", filePath);
        map.put("messageId", item.getId());
        map.put("filepath", fileAttachment.getFilePath());
        holder.attachmentPanel.setTag(map);

        ImageIcon attachmentTypeIcon = attachmentIconHelper.getImageIcon(filePath);
        holder.attachmentIcon.setIcon(attachmentTypeIcon);
        holder.attachmentTitle.setText(fileAttachment.getFileName());
        holder.sender.setText(item.getSenderUsername());
        holder.sizeLabel.setText(fileCache.fileSizeString(fileAttachment.getFileSize()));

        setAttachmentClickListener(holder, item);

        listView.setScrollHiddenOnMouseLeave(holder.attachmentPanel);
        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.attachmentTitle);

        // 绑定右键菜单
        attachPopupMenu(viewHolder, MessageItem.LEFT_ATTACHMENT);
    }

    /**
     * 自己发送的附件消息
     *
     * @param viewHolder
     * @param item
     */
    private void processRightAttachmentMessage(ViewHolder viewHolder, MessageItem item) {
        MessageRightAttachmentViewHolder holder = (MessageRightAttachmentViewHolder) viewHolder;
        FileAttachmentItem fileAttachment = item.getFileAttachment();

        Map<String, Object> map = new HashMap<>();
        map.put("attachmentId", fileAttachment.getId());
        String filename = fileAttachment.getFileName();
        map.put("name", filename);
        map.put("messageId", item.getId());
        map.put("filepath", fileAttachment.getFilePath());
        holder.attachmentPanel.setTag(map);
        ImageIcon attachmentTypeIcon = attachmentIconHelper.getImageIcon(filename);
        holder.attachmentIcon.setIcon(attachmentTypeIcon);
        holder.attachmentTitle.setText(fileAttachment.getFileName());

        if (item.getProgress() != 0 && item.getProgress() != 100) {
            Message msg = null;//= messageService.findById(item.getId());
            if (msg != null) {
                item.setProgress(msg.getProgress());

                holder.progressBar.setVisible(true);
                holder.progressBar.setValue(item.getProgress());

                if (item.getProgress() == 100) {
                    holder.progressBar.setVisible(false);
                } else {
       /*             if (!ChatPanel.getContext().uploadingOrDownloadingFiles.contains(item.getFileAttachment().getId())) {
                        item.setNeedToResend(true);
                    }*/
                }
            }
        } else {
            holder.progressBar.setVisible(false);
        }


        // 判断是否显示重发按钮
        if (item.isNeedToResend()) {
            holder.sizeLabel.setVisible(false);
            holder.progressBar.setVisible(false);
            holder.resend.setVisible(true);
        } else {
            holder.resend.setVisible(false);
        }

        holder.resend.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
               /* if (item.getUpdatedAt() > 0) {
                    holder.resend.setVisible(false);
                    System.out.println("这条消息其实已经发送出去了");
                    return;
                }*/

                //ChatPanel.getContext().resendFileMessage(item.getId(), "file");

                super.mouseClicked(e);
            }
        });

        setAttachmentClickListener(holder, item);

        if (item.getProgress() > 0) {
            holder.sizeLabel.setText(fileCache.fileSizeString(fileAttachment.getFileSize()));
        } else {
            holder.sizeLabel.setText("等待上传...");
        }

        // 绑定右键菜单
        attachPopupMenu(viewHolder, MessageItem.RIGHT_ATTACHMENT);

        listView.setScrollHiddenOnMouseLeave(holder.attachmentPanel);
        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.attachmentTitle);
    }

    /**
     * 设置附件点击监听
     *
     * @param viewHolder
     * @param item
     */
    private void setAttachmentClickListener(MessageAttachmentViewHolder viewHolder, MessageItem item) {
        MessageMouseListener listener = new MessageMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ChatPanel.downloadOrOpenFile(item.getId());
                }
            }
        };


        viewHolder.attachmentPanel.addMouseListener(listener);
        viewHolder.attachmentTitle.addMouseListener(listener);
    }

    /**
     * 附件大小
     *
     * @param viewHolder
     * @param item
     */
    private void processAttachmentSize(MessageAttachmentViewHolder viewHolder, MessageItem item) {
        FileAttachmentItem attachment = item.getFileAttachment();
        String path;
        // 远程服务器文件
        if (attachment.getFilePath().startsWith("/file-upload")) {
            path = fileCache.tryGetFileCache(item.getFileAttachment().getId(), item.getFileAttachment().getFileName());
        }
        // 我自己上传的文件
        else {
            path = attachment.getFilePath();
        }

        if (path != null) {
            viewHolder.sizeLabel.setVisible(true);
            viewHolder.sizeLabel.setText(fileCache.fileSizeString(path));
        }
    }

    /**
     * 对方发送的图片
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftImageMessage(ViewHolder viewHolder, MessageItem item) {
        MessageLeftImageViewHolder holder = (MessageLeftImageViewHolder) viewHolder;
        holder.sender.setText(item.getSenderUsername());

        processImage(item, holder.image);

        listView.setScrollHiddenOnMouseLeave(holder.image);
        listView.setScrollHiddenOnMouseLeave(holder.imageBubble);

        // 绑定右键菜单
        attachPopupMenu(viewHolder, MessageItem.LEFT_IMAGE);
    }

    /**
     * 处理 对方 发送的语音消息
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftVoiceMessage(ViewHolder viewHolder, MessageItem item) {
        MessageLeftVoiceViewHolder holder = (MessageLeftVoiceViewHolder) viewHolder;
        processVoice(item, holder);
        holder.getSender().setText(item.getSenderUsername());
        attachPopupMenu(viewHolder, MessageItem.LEFT_VOICE);

    }

    /**
     * 自己发送的语音消息
     *
     * @param viewHolder
     * @param item
     */
    private void processRightVoiceMessage(ViewHolder viewHolder, MessageItem item) {
        MessageRightVoiceViewHolder holder = (MessageRightVoiceViewHolder) viewHolder;
        processVoice(item, holder);
        attachPopupMenu(viewHolder, MessageItem.RIGHT_VOICE);

    }

    /**
     * 处理语音消息
     *
     * @param item
     * @param holder
     */
    private void processVoice(MessageItem item, MessageVoiceViewHolder holder) {

        holder.getContentTagPanel().setTag(item.getVoiceAttachmentItem());
        double len = item.getVoiceAttachmentItem().getVoiceLength() * 1.0;

        len = len / 1000;
        long round = Math.round(len);
        holder.getDurationText().setText(String.valueOf(round));
        StringBuilder t = new StringBuilder();
        for (long i = 0; i < round / 2; i++) {
            t.append(" ");
        }
        holder.getGapText().setText(t.toString());

        //holder.getDurationText().setTag(item.getId());


        //播放语音
        holder.getMessageBubble().addMouseListener(new MessageMouseListener() {
            private void closePlayer() {
                if (player != null) {
                    player.close();
                    player = null;
                    holder.durationText.stop();
                }
            }

            private Player player = null;

            @Override
            public void mouseReleased(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON1) {
                    closePlayer();
                    String voicePath = item.getVoiceAttachmentItem().getVoicePath();
                    File file = new File(voicePath);
                    if (!file.exists()) {
                        Boolean aBoolean = DownloadTools.FILE_DOWNLOAD_STATUS.get(voicePath);
                        if (aBoolean == null) {
                            JOptionPane.showMessageDialog(null, "下载失败", "打开失败", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "下载中...", "打开失败", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        holder.removeUnreadPoint();
                        RCProgressBar progressBar = holder.getProgressBar();
                        progressBar.setVisible(true);
                        progressBar.setMaximum((int) item.getVoiceAttachmentItem().getVoiceLength());
                        //刷新进度条
                        new SwingWorker<Object, Integer>() {
                            @Override
                            protected Object doInBackground() throws Exception {
                                player = new Player(new BufferedInputStream(new FileInputStream(file)));
                                //新线程更新进度条
                                ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (player != null) {
                                            if (player.isComplete()) {
                                                publish((int) item.getVoiceAttachmentItem().getVoiceLength());
                                                break;
                                            } else {
                                                publish(player.getPosition());
                                            }
                                        }
                                    }
                                });
                                player.play();
                                return null;
                            }

                            @Override
                            protected void process(List<Integer> chunks) {
                                Integer integer = chunks.get(chunks.size() - 1);
                                progressBar.setValue(integer);
                                super.process(chunks);
                            }

                            @Override
                            protected void done() {
                                closePlayer();
                                progressBar.setValue(0);
                                progressBar.setVisible(false);
                            }
                        }.execute();
                        //倒计时
                        holder.durationText.start();

                    }

                }
                super.mouseReleased(e);
            }
        });
    }

    /**
     * 对方发送的图片
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftVideoMessage(ViewHolder viewHolder, MessageItem item) {
        MessageLeftVideoViewHolder holder = (MessageLeftVideoViewHolder) viewHolder;
        holder.getSender().setText(item.getSenderUsername());

        try {
            processVideo(item
                    , holder.getTimeLabel()
                    , holder.getPlayImgLabel()
                    , holder.getSlaveImgLabel()
                    , holder.getVideoComponent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.getVideoComponent().setTag(item.getVideoAttachmentItem());
        listView.setScrollHiddenOnMouseLeave(holder.getVideoComponent());
        listView.setScrollHiddenOnMouseLeave(holder.getImageBubble());

        // 绑定右键菜单
        attachPopupMenu(viewHolder, MessageItem.LEFT_VIDEO);
    }

    /**
     * 对方发送的图片
     *
     * @param viewHolder
     * @param item
     */
    private void processRightVideoMessage(ViewHolder viewHolder, MessageItem item) {
        MessageRightVideoViewHolder holder = (MessageRightVideoViewHolder) viewHolder;
        try {
            processVideo(item
                    , holder.getTimeLabel()
                    , holder.getPlayImgLabel()
                    , holder.getSlaveImgLabel()
                    , holder.getVideoComponent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.getVideoComponent().setTag(item.getVideoAttachmentItem());
        listView.setScrollHiddenOnMouseLeave(holder.getVideoComponent());
        listView.setScrollHiddenOnMouseLeave(holder.getImageBubble());

        // 绑定右键菜单
        attachPopupMenu(viewHolder, MessageItem.RIGHT_VIDEO);
    }

    /**
     * 我发送的图片
     *
     * @param viewHolder
     * @param item
     */
    private void processRightImageMessage(ViewHolder viewHolder, MessageItem item) {
        MessageRightImageViewHolder holder = (MessageRightImageViewHolder) viewHolder;

        processImage(item, holder.image);
        holder.sendingProgress.setVisible(item.getProgress() != 100);


        // 判断是否显示重发按钮
        holder.resend.setVisible(item.isNeedToResend());

        //TODO 重发消息
        holder.resend.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
             /*   if (item.getUpdatedAt() > 0) {
                    holder.resend.setVisible(false);
                    System.out.println("这条消息其实已经发送出去了");
                    return;
                }*/

                //ChatPanel.getContext().resendFileMessage(item.getId(), "image");

                super.mouseClicked(e);
            }
        });

        // 绑定右键菜单
        attachPopupMenu(viewHolder, MessageItem.RIGHT_IMAGE);

        listView.setScrollHiddenOnMouseLeave(holder.image);
        listView.setScrollHiddenOnMouseLeave(holder.imageBubble);
    }

    /**
     * 返回时间格式化后的表示
     *
     * @param lengthSec 秒
     */
    private static String getSecString(long lengthSec) {
        long hour, minute;
        hour = lengthSec / 3600;
        minute = (lengthSec - hour * 3600) / 60;
        lengthSec = lengthSec - hour * 300 - minute * 60;

        return (hour < 10 && hour > 0 ? "0" + hour : hour) + ":"
                + (minute < 10 && minute > 0 ? "0" + minute : minute) + ":"
                + (lengthSec < 10 && lengthSec > 0 ? "0" + lengthSec : lengthSec);
    }

    /**
     * 处理视频
     *
     * @param item 消息项
     * @throws IOException 视频缩略图读取异常
     */
    private void processVideo(MessageItem item, JLabel timeLabel, JLabel playImgLabel, JLabel slaveImgLabel, JComponent videoComponent) throws IOException {
        VideoAttachmentItem videoItem = item.getVideoAttachmentItem();
        //#############判断缩略图是否下载完成#########################
        String slaveImgPath = videoItem.getSlaveImgPath();
        Boolean aBoolean = DownloadTools.FILE_DOWNLOAD_STATUS.get(slaveImgPath);
        timeLabel.setText(getSecString(videoItem.getVideoLength()));
        if (aBoolean == null) {
            //缩略图下载失败
            //  holder.getSlaveImgLabel().setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/image/image_error.png"))));
            playImgLabel.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/image/play48.png"))));
        } else if (aBoolean) {
            File file = new File(slaveImgPath);
            slaveImgLabel.setIcon(ImageUtil.preferredImageSize(new ImageIcon(ImageIO.read(file))));
            playImgLabel.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/image/play48.png"))));
        } else {
            // holder.getPlayImgLabel().setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/image/image_loading.gif"))));
            // holder.getPlayImgLabel().repaint();

            new SwingWorker<Object, Object>() {
                private final long startTime = System.currentTimeMillis();
                private boolean status = false;
                private ImageIcon imageIcon = null;
                private ImageIcon playImg = null;

                @Override
                protected Object doInBackground() throws Exception {
                    //等待下载完成
                    while (!status) {
                        SleepUtils.sleep(100);
                        status = DownloadTools.FILE_DOWNLOAD_STATUS.get(slaveImgPath);
                        //三分钟还未下载完成
                        if (System.currentTimeMillis() - startTime >= 1000 * 60 * 3) {
                            //下载超时
                            break;
                        }
                    }
                    File file = new File(slaveImgPath);
                    imageIcon = new ImageIcon(ImageIO.read(file));
                    ImageUtil.preferredImageSize(imageIcon);
                    playImg = new ImageIcon(ImageIO.read(getClass().getResource("/image/play48.png")));
                    return null;
                }

                @Override
                protected void done() {
                    if (status) {
                        slaveImgLabel.setIcon(imageIcon);
                    }
                    playImgLabel.setIcon(playImg);
                }
            }.execute();
        }

        // 当点击视频时，使用默认程序打开图片
        videoComponent.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ChatPanel.openFile(videoItem.getVideoPath());
                }
                super.mouseReleased(e);
            }
        });
    }

    /**
     * 处理图片消息
     *
     * @param item
     * @param imageLabel
     */
    private void processImage(MessageItem item, MessageImageLabel imageLabel) {
        final ImageAttachmentItem imageAttachment = item.getImageAttachment();
        //显示加载中
        ImageIcon imageIcon = IconUtil.getIcon(this,"/image/image_loading.gif");
        imageLabel.setIcon(imageIcon);
        String slavePath = imageAttachment.getSlavePath();

        if (StringUtils.isEmpty(slavePath)) {
            slavePath = imageAttachment.getImagePath();
        }
        final String finalPath = slavePath;
        //标志
        Map<String, Object> map = new HashMap<>();
        map.put("attachmentId", imageAttachment.getId());
        map.put("url", finalPath);
        map.put("messageId", item.getId());
        imageLabel.setTag(map);

        new SwingWorker<Object, Object>() {
            private ImageIcon imageIcon;

            @Override
            protected Object doInBackground() {
                //循环等待下载完成
                if (finalPath == null) {
                    return null;
                }
                //阻塞
                DownloadTools.awaitDownload(finalPath);
                File file = new File(finalPath);
                if (file.length()>0){
                    if (ImageUtil.isGIF(finalPath)){
                        imageIcon = ImageUtil.preferredImageSize(finalPath, imageAttachment.getWidth(), imageAttachment.getHeight());
                    }else{
                        imageIcon = imageCache.tryGetThumbCache(file);
                        ImageUtil.preferredImageSize(imageIcon);
                    }


                }

                return null;
            }

            @Override
            protected void done() {
                if (imageIcon != null) {
                    imageLabel.setIcon(imageIcon);
                    // 当点击图片时，使用默认程序打开图片
                    imageLabel.addMouseListener(new MessageMouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (finalPath == null) {
                                super.mouseClicked(e);
                                return;
                            }
                            ImageViewerFrame frame = new ImageViewerFrame( new ImageIcon( getClass().getResource("/image/image_loading.gif")));
                            frame.setVisible(true);
                            File file = new File(imageAttachment.getImagePath());
                            new SwingWorker<Object,ImageIcon>(){
                                @Override
                                protected Object doInBackground() throws Exception {
                                    //阻塞
                                    DownloadTools.awaitDownload(imageAttachment.getImagePath());
                                    if (file.exists() &&  file.length() <= 1024 * 1024) {
                                        ImageIcon imageIcon = new ImageIcon(imageAttachment.getImagePath());
                                        publish(imageIcon);
                                    }else {
                                      ChatPanel.openFile(imageAttachment.getImagePath());
                                    }
                                    return null;
                                }

                                @Override
                                protected void process(List<ImageIcon> chunks) {
                                    ImageIcon imageIcon = chunks.get(chunks.size() - 1);
                                    if (imageIcon == null ){
                                        JOptionPane.showMessageDialog(MainFrame.getContext(), "图片下载中...", "文件不存在", JOptionPane.WARNING_MESSAGE);
                                        return;
                                    }
                                    frame.setImageIcon(imageIcon);
                                }

                            }.execute();
                            super.mouseClicked(e);
                        }
                    });
                }else{
                    imageLabel.setIcon(null);
                    imageLabel.setText("[不支持的表情消息，请在手机上查看]");
                }

            }
        }.execute();

    }


    /**
     * 处理 我发送的文本消息
     *
     * @param viewHolder
     * @param item
     */
    private void processRightTextMessage(ViewHolder viewHolder, final MessageItem item) {
        MessageRightTextViewHolder holder = (MessageRightTextViewHolder) viewHolder;

        holder.text.setText(item.getMessageContent());

        holder.text.setTag(item.getId());

        //holder.text.setCaretPosition(holder.text.getDocument().getLength());
        //holder.text.insertIcon(IconUtil.getIcon(this, "/image/smile.png", 18,18));

        //processMessageContent(holder.messageText, item);
        //registerMessageTextListener(holder.messageText, item);

        // 判断是否显示重发按钮
        boolean needToUpdateResendStatus = !item.isNeedToResend() && System.currentTimeMillis() - item.getTimestamp() > 10 * 1000;

        if (item.isNeedToResend()) {
            if (needToUpdateResendStatus) {
                //messageService.updateNeedToResend(item.getId(), true);
            }


            holder.sendingProgress.setVisible(false);
            holder.resend.setVisible(true);
        } else {
            holder.resend.setVisible(false);
            // 如果是刚发送的消息，显示正在发送进度条
            if (item.getProgress() != 100) {
                holder.sendingProgress.setVisible(true);
            } else {
                holder.sendingProgress.setVisible(false);
            }
        }


        holder.resend.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            /*    if (item.getUpdatedAt() > 0) {
                    holder.resend.setVisible(false);
                    return;
                }*/

                System.out.println("重发消消息：" + item.getMessageContent());

                // TODO: 向服务器重新发送消息
                Message message = null;//= messageService.findById(item.getId());
                message.setUpdatedAt(System.currentTimeMillis());
                message.setNeedToResend(false);
                //messageService.update(message);

                super.mouseClicked(e);
            }
        });

        // 绑定右键菜单
        attachPopupMenu(viewHolder, MessageItem.RIGHT_TEXT);

        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.text);
    }

    /**
     * 处理 对方 发送的文本消息
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftTextMessage(ViewHolder viewHolder, final MessageItem item) {
        MessageLeftTextViewHolder holder = (MessageLeftTextViewHolder) viewHolder;

        holder.text.setText(item.getMessageContent() == null ? "[空消息]" : item.getMessageContent());
        holder.text.setTag(item.getId());

        holder.sender.setText(item.getSenderUsername());

        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.text);
        attachPopupMenu(viewHolder, MessageItem.LEFT_TEXT);
    }

    private void processLeftLinkMessage(ViewHolder viewHolder, MessageItem item) {
        processLinkMessage(viewHolder, item);
        ((MessageLeftLinkViewHolder) viewHolder).getSender().setText(item.getSenderUsername());
        attachPopupMenu(viewHolder, MessageItem.LEFT_LINK);
    }

    private void processRightLinkMessage(ViewHolder viewHolder, MessageItem item) {
        processLinkMessage(viewHolder, item);
        attachPopupMenu(viewHolder, MessageItem.RIGHT_LINK);
    }

    private void processLinkMessage(ViewHolder viewHolder, MessageItem item) {
        LinkAttachmentItem linkItem = item.getLinkAttachmentItem();
        MessageLinkViewHolder linkViewHolder = (MessageLinkViewHolder) viewHolder;


        linkViewHolder.desc.setText(StringEscapeUtils.unescapeHtml4(linkItem.getDesc()));
        linkViewHolder.title.setText(linkItem.getTitle());
        if (StringUtils.isEmpty(linkItem.getSourceName())) {
            linkViewHolder.sourcePanel.setVisible(false);
        } else {
            linkViewHolder.sourceName.setText(linkItem.getSourceName());
        }
        BufferedImage image = linkItem.getImage();
        if (image == null && StringUtils.isNotEmpty(linkItem.getThumbUrl())) {
            try {
                image = ImageIO.read(new URL(linkItem.getThumbUrl()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (image!=null){
            linkViewHolder.icon.setIcon(new ImageIcon(ImageUtil.preferredImageSize(image, MessageLinkViewHolder.THUMB_WIDTH)));
            //有图片时缩短宽度，让其与无图的Panel尽量一致
            linkViewHolder.desc.setColumns(16);
        }

        //点击打开链接
        MessageMouseListener messageMouseListener = new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (StringUtils.isNotEmpty(linkItem.getUrl())) {
                        try {
                            Desktop.getDesktop().browse(new URI(linkItem.getUrl()));
                        } catch (IOException | URISyntaxException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        };
        linkViewHolder.desc.addMouseListener(messageMouseListener);
        linkViewHolder.title.addMouseListener(messageMouseListener);
        linkViewHolder.icon.addMouseListener(messageMouseListener);
        linkViewHolder.contentTagPanel.addMouseListener(messageMouseListener);
        linkViewHolder.messageBubble.addMouseListener(messageMouseListener);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.desc);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.title);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.icon);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.contentTagPanel);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.messageBubble);
        Dimension preferredSize = linkViewHolder.contentTagPanel.getPreferredSize();
        if (preferredSize.height < 120) {
        //    preferredSize.height = 120;
        }
       // preferredSize.width = 250;
        //固定宽度
       // linkViewHolder.contentTagPanel.setPreferredSize(preferredSize);
       // linkViewHolder.messageBubble.repaint();
    }

    /**
     * 处理消息发送时间 以及 消息发送者头像
     *
     * @param item
     * @param preItem
     * @param holder
     */
    private void processTimeAndAvatar(MessageItem item, MessageItem preItem, BaseMessageViewHolder holder) {
        // 如果当前消息的时间与上条消息时间相差大于1分钟，则显示当前消息的时间
        if (preItem != null) {
            if (TimeUtil.inTheSameMinute(item.getTimestamp(), preItem.getTimestamp())) {
                holder.time.setVisible(false);
            } else {
                holder.time.setVisible(true);
                holder.time.setText(TimeUtil.diff(item.getTimestamp(), true));
            }
        } else {
            holder.time.setVisible(true);
            holder.time.setText(TimeUtil.diff(item.getTimestamp(), true));
        }

        if (holder.avatar != null) {
            ImageIcon icon = null;
            if (AvatarUtil.avatarExists(item.getSenderId())){
                //已存在图片缓存
                if (item.getRoomId().startsWith("@@")){
                    icon = AvatarUtil.createOrLoadMemberAvatar(item.getRoomId(),item.getSenderId());

                }else {
                    icon = AvatarUtil.createOrLoadUserAvatar(item.getSenderId());
                }
                holder.avatar.setIcon(icon);
            }else {
                //异步从网络加载
                new SwingWorker<Object,Object>(){
                    ImageIcon icon = null;
                    @Override
                    protected Object doInBackground() throws Exception {
                        if (item.getRoomId().startsWith("@@")){
                            icon = AvatarUtil.createOrLoadMemberAvatar(item.getRoomId(),item.getSenderId());

                        }else {
                            icon = AvatarUtil.createOrLoadUserAvatar(item.getRoomId());
                        }
                        return null;
                    }
                    @Override
                    protected void done() {
                        holder.avatar.setIcon(icon);
                    }
                }.execute();
            }


            //弹窗
            if (item.getMessageType() == MessageItem.LEFT_ATTACHMENT
                    || item.getMessageType() == MessageItem.LEFT_IMAGE
                    || item.getMessageType() == MessageItem.LEFT_TEXT) {

                bindAvatarAction(holder.avatar, item);
            }
        }

    }


    private void bindAvatarAction(JLabel avatarLabel, MessageItem item) {

        avatarLabel.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Contacts contacts = null;
                if (item.isGroupable()) {
                    contacts = ContactsTools.getMemberOfGroup(item.getRoomId(), item.getSenderId());
                } else {
                    contacts = Core.getMemberMap().get(item.getSenderId());
                }
                if (contacts == null) {
                    RoomChatPanel.getContext().get(RoomChatPanel.getContext().getCurrRoomId())
                            .getTipPanel().setText("成员信息加载中...");
                    return;
                }
                contacts.setGroupName(item.getRoomId());
                UserInfoPopup instance = UserInfoPopup.getInstance();
                instance.setContacts(contacts);
                instance.show(e.getComponent(), e.getX(), e.getY());

                super.mouseClicked(e);
            }
        });
    }

    @Override
    public int getCount() {
        return messageItems.size();
    }

    private void attachPopupMenu(ViewHolder viewHolder, int messageType) {
        JComponent contentComponent = null;
        RCMessageBubble messageBubble = null;

        switch (messageType) {
            case MessageItem.RIGHT_TEXT: {
                MessageRightTextViewHolder holder = (MessageRightTextViewHolder) viewHolder;
                contentComponent = holder.text;
                messageBubble = holder.messageBubble;

                break;
            }
            case MessageItem.LEFT_TEXT: {
                MessageLeftTextViewHolder holder = (MessageLeftTextViewHolder) viewHolder;
                contentComponent = holder.text;
                messageBubble = holder.messageBubble;
                break;
            }
            case MessageItem.RIGHT_IMAGE: {
                MessageRightImageViewHolder holder = (MessageRightImageViewHolder) viewHolder;
                contentComponent = holder.image;
                messageBubble = holder.imageBubble;
                break;
            }
            case MessageItem.LEFT_IMAGE: {
                MessageLeftImageViewHolder holder = (MessageLeftImageViewHolder) viewHolder;
                contentComponent = holder.image;
                messageBubble = holder.imageBubble;
                break;
            }
            case MessageItem.LEFT_VIDEO: {
                MessageLeftVideoViewHolder holder = (MessageLeftVideoViewHolder) viewHolder;
                contentComponent = holder.getVideoComponent();
                messageBubble = holder.getImageBubble();
                break;
            }
            case MessageItem.RIGHT_VIDEO: {
                MessageRightVideoViewHolder holder = (MessageRightVideoViewHolder) viewHolder;
                contentComponent = holder.getVideoComponent();
                messageBubble = holder.getImageBubble();
                break;
            }
            case MessageItem.LEFT_VOICE: {
                MessageLeftVoiceViewHolder holder = (MessageLeftVoiceViewHolder) viewHolder;
                contentComponent = holder.getMessageBubble();
                messageBubble = holder.getMessageBubble();
                break;
            }
            case MessageItem.RIGHT_VOICE: {
                MessageRightVoiceViewHolder holder = (MessageRightVoiceViewHolder) viewHolder;
                contentComponent = holder.getMessageBubble();
                messageBubble = holder.getMessageBubble();
                break;
            }
            case MessageItem.LEFT_LINK: {
                MessageLeftLinkViewHolder holder = (MessageLeftLinkViewHolder) viewHolder;
                contentComponent = holder.title;
                messageBubble = holder.messageBubble;
                break;
            }
            case MessageItem.RIGHT_LINK: {
                MessageRightLinkViewHolder holder = (MessageRightLinkViewHolder) viewHolder;
                contentComponent = holder.title;
                messageBubble = holder.messageBubble;
                break;
            }
            case MessageItem.RIGHT_ATTACHMENT: {
                MessageRightAttachmentViewHolder holder = (MessageRightAttachmentViewHolder) viewHolder;
                contentComponent = holder.attachmentPanel;
                messageBubble = holder.messageBubble;

                holder.attachmentTitle.addMouseListener(new MessageMouseListener() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            // 通过holder.attachmentPane.getTag()可以获取文件附件信息
                            popupMenu.show(holder.attachmentPanel, e.getX(), e.getY(), MessageItem.RIGHT_ATTACHMENT);
                        }
                    }
                });
                break;
            }
            case MessageItem.LEFT_ATTACHMENT: {
                MessageLeftAttachmentViewHolder holder = (MessageLeftAttachmentViewHolder) viewHolder;
                contentComponent = holder.attachmentPanel;
                messageBubble = holder.messageBubble;

                holder.attachmentTitle.addMouseListener(new MessageMouseListener() {
                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.getButton() == MouseEvent.BUTTON3) {
                            popupMenu.show(holder.attachmentPanel, e.getX(), e.getY(), MessageItem.LEFT_ATTACHMENT);
                        }
                    }
                });
                break;
            }
        }

        JComponent finalContentComponent = contentComponent;
        RCMessageBubble finalMessageBubble = messageBubble;

        contentComponent.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (e.getX() > finalContentComponent.getWidth() || e.getY() > finalContentComponent.getHeight()) {
                    finalMessageBubble.setBackgroundIcon(finalMessageBubble.getBackgroundNormalIcon());
                }
                super.mouseExited(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                finalMessageBubble.setBackgroundIcon(finalMessageBubble.getBackgroundActiveIcon());
                super.mouseEntered(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupMenu.show((Component) e.getSource(), e.getX(), e.getY(), messageType);
                }

                super.mouseReleased(e);
            }
        });

        messageBubble.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupMenu.show(finalContentComponent, e.getX(), e.getY(), messageType);
                }
            }
        });
    }


}
