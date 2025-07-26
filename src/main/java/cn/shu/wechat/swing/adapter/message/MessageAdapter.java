package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.constant.DownloadStatus;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.dto.request.msg.url.WXMsgUrl;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.swing.adapter.BaseAdapter;
import cn.shu.wechat.swing.adapter.message.app.*;
import cn.shu.wechat.swing.adapter.message.image.MessageLeftImageViewHolder;
import cn.shu.wechat.swing.adapter.message.image.MessageRightImageViewHolder;
import cn.shu.wechat.swing.adapter.message.system.MessageSystemMessageViewHolder;
import cn.shu.wechat.swing.adapter.message.text.MessageLeftTextViewHolder;
import cn.shu.wechat.swing.adapter.message.text.MessageRightTextViewHolder;
import cn.shu.wechat.swing.adapter.message.video.MessageLeftVideoViewHolder;
import cn.shu.wechat.swing.adapter.message.video.MessageRightVideoViewHolder;
import cn.shu.wechat.swing.adapter.message.voice.MessageLeftVoiceViewHolder;
import cn.shu.wechat.swing.adapter.message.voice.MessageRightVoiceViewHolder;
import cn.shu.wechat.swing.adapter.message.voice.MessageVoiceViewHolder;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.components.RCProgressBar;
import cn.shu.wechat.swing.components.UserInfoPopup;
import cn.shu.wechat.swing.components.message.MessageImageLabel;
import cn.shu.wechat.swing.components.message.MessagePopupMenu;
import cn.shu.wechat.swing.components.message.RCMessageBubble;
import cn.shu.wechat.swing.frames.ImageViewerFrame;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.helper.AttachmentIconHelper;
import cn.shu.wechat.swing.helper.MessageViewHolderCacheHelper;
import cn.shu.wechat.swing.media.Mp3Player;
import cn.shu.wechat.swing.media.VoicePlaybackListener;
import cn.shu.wechat.swing.panels.chat.ChatMessagePanel;
import cn.shu.wechat.swing.utils.*;
import cn.shu.wechat.swing.worker.HeadLoadingSwingWorker;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.task.DownloadTask;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import javazoom.jl.decoder.JavaLayerException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by 舒新胜 on 17-6-2.
 */
@Log4j2
public class MessageAdapter extends BaseAdapter<BaseMessageViewHolder> {
    private final List<Message> messageItems;
    private final RCListView<BaseMessageViewHolder> listView;
    private final AttachmentIconHelper attachmentIconHelper = new AttachmentIconHelper();
    private final ImageCache imageCache;
    private final Mp3Player player = new Mp3Player();

    private final MessagePopupMenu popupMenu = new MessagePopupMenu();
    private final ChatMessagePanel parent;

    MessageViewHolderCacheHelper messageViewHolderCacheHelper;

    public MessageAdapter(ChatMessagePanel parent, List<Message> messageItems, RCListView<BaseMessageViewHolder> listView, MessageViewHolderCacheHelper messageViewHolderCacheHelper) {
        this.messageItems = messageItems;
        this.listView = listView;
        this.parent = parent;
        imageCache = new ImageCache();
        this.messageViewHolderCacheHelper = messageViewHolderCacheHelper;
    }

    @Override
    public int getItemViewType(int position) {
        return messageItems.get(position).getMsgType();
    }

    @Override
    public int getItemSubViewType(int position) {
        Integer appMsgType = messageItems.get(position).getAppMsgType();
        return appMsgType == null?0:appMsgType;
    }

    @Override
    public boolean isGroup(int position) {
        return messageItems.get(position).getFromUsername().startsWith("@@");
    }

    @Override
    public BaseMessageViewHolder onCreateViewHolder(int viewType,int subViewType, int position) {
        Message messageItem = messageItems.get(position);
        boolean isSelf = Core.getUserName().equals(messageItem.getFromUsername());
        switch (WxRespConstant.WXReceiveMsgCodeEnum.getByCode(viewType)) {
            case MSGTYPE_VERIFYMSG:
            case MSGTYPE_SHARECARD:{
                if (isSelf) {
                    return new MessageRightContactsCardOfAppViewHolder();
                } else {
                    return new MessageLeftContactsCardOfAppViewHolder(messageItem.isGroup());
                }
            }

            case MSGTYPE_RECALLED:
            case MSGTYPE_SYS:
            case MSGTYPE_STATUSNOTIFY: {

                return messageViewHolderCacheHelper.tryGetSystemMessageViewHolder();
            }
            default:
            case MSGTYPE_TEXT: {
                if (isSelf) {
                    return messageViewHolderCacheHelper.tryGetRightTextViewHolder();
                } else {

                    return messageViewHolderCacheHelper.tryGetLeftTextViewHolder(messageItem);
                }
            }
            case MSGTYPE_IMAGE:
            case MSGTYPE_EMOTICON: {
                if (isSelf) {
                    return messageViewHolderCacheHelper.tryGetRightImageViewHolder();
                } else {

                    return messageViewHolderCacheHelper.tryGetLeftImageViewHolder(messageItem);
                }
            }
            case MSGTYPE_VIDEO:{
                if (isSelf) {

                    return messageViewHolderCacheHelper.tryGetRightVideoViewHolder(messageItem);
                } else {

                    return messageViewHolderCacheHelper.tryGetLeftVideoViewHolder(messageItem);
                }
            }
            case MSGTYPE_APP:{
               switch (WxRespConstant.WXReceiveMsgCodeOfAppEnum.getByCode(subViewType)){
                   case FILE:{
                       if (isSelf){

                           return messageViewHolderCacheHelper.tryGetRightAttachmentViewHolder();
                       }else {

                           return messageViewHolderCacheHelper.tryGetLeftAttachmentViewHolder(messageItem);
                       }
                   }

                   case MUSIC:
                   case LINK:{
                       if (isSelf){
                           return new MessageRightLinkOfAppViewHolder();

                       }else {
                           return new MessageLeftLinkOfAppViewHolder(messageItem.isGroup());
                       }
                   }
                   default:
                   case PICTURE:
                   case PROGRAM:{
                      if (isSelf){

                          return messageViewHolderCacheHelper.tryGetRightProgramOfAppViewHolder();
                       }else {

                          return messageViewHolderCacheHelper.tryGetLeftProgramOfAppViewHolder(messageItem);
                       }
                   }

               }
            }

            case MSGTYPE_VOICE: {
                if (isSelf) {

                    return messageViewHolderCacheHelper.tryGetRightVoiceViewHolder();
                } else {

                    return messageViewHolderCacheHelper.tryGetLeftVoiceViewHolder(messageItem);
                }
            }

        }
    }

    @Override
    public void onBindViewHolder(BaseMessageViewHolder viewHolder, int position) {

        final Message item = messageItems.get(position);
        Message preItem = position == 0 ? null : messageItems.get(position - 1);

       processTimeAndAvatar(item, preItem, viewHolder);
        if (item.isRevoke()){
            viewHolder.revoke.setVisible(true);
        }
        switch (viewHolder) {
            case MessageSystemMessageViewHolder messageSystemMessageViewHolder ->
                    processSystemMessage(messageSystemMessageViewHolder, item);
            case MessageRightTextViewHolder messageRightTextViewHolder ->
                    processRightTextMessage(messageRightTextViewHolder, item);
            case MessageLeftTextViewHolder messageLeftTextViewHolder ->
                    processLeftTextMessage(messageLeftTextViewHolder, item);
            case MessageRightImageViewHolder messageRightImageViewHolder ->
                    processRightImageMessage(messageRightImageViewHolder, item);
            case MessageLeftVideoViewHolder messageLeftVideoViewHolder ->
                    processLeftVideoMessage(messageLeftVideoViewHolder, item);
            case MessageRightVideoViewHolder messageRightVideoViewHolder ->
                    processRightVideoMessage(messageRightVideoViewHolder, item);
            case MessageLeftVoiceViewHolder messageLeftVoiceViewHolder ->
                    processLeftVoiceMessage(messageLeftVoiceViewHolder, item);
            case MessageRightVoiceViewHolder messageRightVoiceViewHolder ->
                    processRightVoiceMessage(messageRightVoiceViewHolder, item);
            case MessageLeftImageViewHolder messageLeftImageViewHolder ->
                    processLeftImageMessage(messageLeftImageViewHolder, item);
            case MessageRightAttachmentViewHolder messageRightAttachmentViewHolder ->
                    processRightAttachmentMessage(messageRightAttachmentViewHolder, item);
            case MessageLeftAttachmentViewHolder messageLeftAttachmentViewHolder ->
                    processLeftAttachmentMessage(messageLeftAttachmentViewHolder, item);
            case MessageRightLinkOfAppViewHolder messageRightLinkOfAppViewHolder ->
                    processRightLinkMessage(messageRightLinkOfAppViewHolder, item);
            case MessageLeftLinkOfAppViewHolder messageLeftLinkOfAppViewHolder ->
                    processLeftLinkMessage(messageLeftLinkOfAppViewHolder, item);
            case MessageRightProgramOfAppViewHolder messageRightProgramOfAppViewHolder ->
                    processRightProgramOfAppMessage(messageRightProgramOfAppViewHolder, item);
            case MessageLeftProgramOfAppViewHolder messageLeftProgramOfAppViewHolder ->
                    processLeftProgramOfAppMessage(messageLeftProgramOfAppViewHolder, item);
            case MessageRightContactsCardOfAppViewHolder messageRightContactsCardOfAppViewHolder ->
                    processRightContactsCardOfAppMessage(messageRightContactsCardOfAppViewHolder, item);
            case MessageLeftContactsCardOfAppViewHolder messageLeftContactsCardOfAppViewHolder ->
                    processLeftContactsCardOfAppMessage(messageLeftContactsCardOfAppViewHolder, item);
            default -> {
            }
        }
    }

    private void processLeftProgramOfAppMessage(BaseMessageViewHolder viewHolder, Message item) {
        MessageLeftProgramOfAppViewHolder appViewHolder = (MessageLeftProgramOfAppViewHolder) viewHolder;
        appViewHolder.sender.setText(item.getPlainName());
        processProgramOfAppMessage(viewHolder,item);
    }

    private void processRightProgramOfAppMessage(BaseMessageViewHolder viewHolder, Message item) {

        processProgramOfAppMessage(viewHolder,item);
    }
    private void processProgramOfAppMessage(BaseMessageViewHolder viewHolder, Message item){
        MessageProgramOfAppViewHolder appViewHolder = (MessageProgramOfAppViewHolder) viewHolder;
        appViewHolder.title.setText(item.getTitle());
        appViewHolder.contentTitlePanel.setTag(item);
        appViewHolder.sourceName.setText(item.getSourceName());
        if (StringUtils.isNotEmpty(item.getSourceIconUrl())){
            try {
                ImageIcon imageIcon = new ImageIcon(URI.create(item.getSourceIconUrl()).toURL());
                ImageUtil.preferredImageSize(imageIcon, 16);
                appViewHolder.sourceIcon.setIcon(imageIcon);
            } catch (MalformedURLException e) {
                log.error(e.getMessage(), e);
            }
        }
        //加载缩略图
        if (StringUtils.isEmpty(item.getThumbUrl())){
            appViewHolder.imageLabel.setIcon(IconUtil.getIcon(this, "/image/image_loading.gif"));

            DownloadTask<byte[]> downloadTask = new DownloadTask<>();
            downloadTask.setMsgId(item.getMsgId());
            downloadTask.setTaskId(item.getMsgId() + WXMsgUrl.BIG_TYPE);
            downloadTask.setType(DownloadType.ImgByteByMsgID);
            downloadTask.setResourceType(WXMsgUrl.BIG_TYPE);

            downloadTask.setCallback(task -> {
                byte[] bytes = task.getResult();
                if (bytes != null && bytes.length > 0) {
                    process(item, appViewHolder, bytes);
                } else {
                    downloadTask.setResourceType(WXMsgUrl.SLAVE_TYPE);
                    downloadTask.setTaskId(item.getMsgId() + WXMsgUrl.SLAVE_TYPE);
                    downloadTask.setCallback(secondTask -> {
                        byte[] secondBytes = secondTask.getResult();
                        if (secondBytes != null && secondBytes.length > 0) {
                            process(item, appViewHolder, secondBytes);
                        }
                    });
                    DownloadManager.submit(downloadTask);
                }

            });
            DownloadManager.submit(downloadTask);

        } else if (StringUtils.isEmpty(item.getFilePath())) {
            try {
                appViewHolder.imageLabel.setIcon(new ImageIcon(URI.create((item.getThumbUrl())).toURL()));
            } catch (MalformedURLException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
                DownloadManager.awaitDownloadTimeOut(item.getFilePath());
                if (Files.exists(Path.of(item.getFilePath()))) {
                    ImageIcon imageIcon = new ImageIcon(item.getFilePath());
                    ImageUtil.preferredImageSize(imageIcon, 200);
                    SwingUtilities.invokeLater(() -> appViewHolder.imageLabel.setIcon(imageIcon));
                }
            });
        }
        if (StringUtils.isNotEmpty(item.getUrl())) {
            //点击打开链接
            MessageMouseListener messageMouseListener = new MessageMouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON1) {
                        if (StringUtils.isNotEmpty(item.getUrl())) {
                            try {
                                Desktop.getDesktop().browse(new URI(item.getUrl()));
                            } catch (IOException | URISyntaxException ioException) {
                                log.error(e);
                            }
                        }
                    }
                }
            };
            appViewHolder.contentTitlePanel.addMouseListener(messageMouseListener);
        }
        // 绑定右键菜单
        attachPopupMenu(viewHolder, item);
    }

    private void process(Message item, MessageProgramOfAppViewHolder appViewHolder, byte[] secondBytes) {
        ImageIcon imageIcon = null;
        if (ImageUtil.isGIF(secondBytes)) {
            imageIcon = ImageUtil.preferredGifSize(secondBytes, item.getImgWidth(), item.getImgHeight());
        } else {
            imageIcon = new ImageIcon(secondBytes);
            ImageUtil.preferredImageSize(imageIcon, 200);
        }
        if (imageIcon != null) {
            ImageIcon finalImageIcon = imageIcon;
            SwingUtilities.invokeLater(() -> appViewHolder.imageLabel.setIcon(finalImageIcon));
        }
    }

    /**
     * 处理系统消息
     *
     * @param holder
     * @param item
     */
    private void processSystemMessage(MessageSystemMessageViewHolder holder, Message item) {
        holder.text.setText(item.getPlaintext());
    }

    /**
     * 其它用户的附件消息
     *
     * @param holder
     * @param item
     */
    private void processLeftAttachmentMessage(MessageLeftAttachmentViewHolder holder, Message item) {


        holder.sender.setText(item.getPlainName());

        updateFileDownloadProgress(holder, item);

        setAttachmentClickListener(holder, item);

        listView.setScrollHiddenOnMouseLeave(holder.attachmentPanel);
        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.attachmentTitle);

        // 绑定右键菜单
        attachPopupMenu(holder, item);
    }

    /**
     * 自己发送的附件消息
     *
     * @param holder
     * @param item
     */
    private void processRightAttachmentMessage(MessageRightAttachmentViewHolder holder, Message item) {

        if (item.getProgress() == 0 || item.getProgress() == 100) {
            holder.progressBar.setVisible(false);
        }

        updateFileDownloadProgress(holder, item);



        // 判断是否显示重发按钮
        if (item.isNeedToResend()) {
            holder.sizeLabel.setVisible(false);
            holder.progressBar.setVisible(false);
            holder.resend.setVisible(true);
        } else {
            holder.resend.setVisible(false);
        }

        if (holder.resend.getMouseListeners().length<=1) {
            holder.resend.addMouseListener(new MessageMouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ChatUtil.deleteMessage(item);
                    parent.sendFileMessage(item.getFilePath());

                    super.mouseClicked(e);
                }
            });
        }

        setAttachmentClickListener(holder, item);
        if (item.getProgress() >= 100 ) {
            if (DownloadManager.containsTask(item.getFilePath())) {
                holder.sizeLabel.setText("0/" + FileCache.fileSizeString(item.getFileSize()));
            } else {
                holder.sizeLabel.setText(FileCache.fileSizeString(item.getFileSize()));
            }
        }else if (item.getProgress() > 0) {
            holder.sizeLabel.setText("0/"+FileCache.fileSizeString(item.getFileSize()));
        } else {
            holder.sizeLabel.setText("等待上传...");
        }

        // 绑定右键菜单
        attachPopupMenu(holder, item);

        listView.setScrollHiddenOnMouseLeave(holder.attachmentPanel);
        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.attachmentTitle);
    }

    /**
     * 更新下载文件进度
     * @param holder
     * @param item
     */
    private void updateFileDownloadProgress(MessageAttachmentViewHolder holder, Message item) {
        holder.sizeLabel.setText("0/" + FileCache.fileSizeString(item.getFileSize()));
        String filePath = item.getFilePath();
        holder.attachmentPanel.setTag(item);

        ImageIcon attachmentTypeIcon = attachmentIconHelper.getImageIcon(filePath);
        holder.attachmentIcon.setIcon(attachmentTypeIcon);
        holder.attachmentTitle.setText(item.getFileName());

        if (new File(item.getFilePath()).length() == item.getFileSize()) {
            //已经下载成功
            holder.sizeLabel.setText(FileCache.fileSizeString(item.getFileSize()));
            return;
        }

        if (!DownloadManager.containsTask(item.getFilePath())) {
            return;
        }

        holder.progressBar.setValue(1);
        holder.progressBar.setVisible(true);
        holder.sizeLabel.setText("0/" + FileCache.fileSizeString(item.getFileSize()));

        BlockingQueue<Long> progress = DownloadManager.getProcessLinkedBlockingDeque(item.getFilePath());
        new SwingWorker<Object, Long>() {
            long p;
            @Override
            protected Object doInBackground() throws Exception {
                //已下载字节数
                while (true) {
                    List<Long> batch = new ArrayList<>();
                    // 先阻塞式获取一个，确保有数据
                    batch.add(progress.take());
                    // 总共最多获取10个
                    progress.drainTo(batch, 9);
                    p = batch.getLast();
                    if (p == 0) {
                        //完成
                        break;
                    }
                    if (p == -1) {
                        //失败
                        break;
                    }
                    publish(p);
                }
                return null;
            }

            @Override
            protected void process(List<Long> chunks) {
                Long loadedSize = chunks.getFirst();
                int progress = (int) (((loadedSize * 1.0f) / item.getFileSize()) * 100);
                if (progress>=100){
                    holder.progressBar.setValue(100);
                    holder.progressBar.setVisible(false);
                }
                holder.sizeLabel.setText(FileCache.fileSizeString(loadedSize)+"/"+FileCache.fileSizeString(item.getFileSize()));
                holder.progressBar.setValue(progress);
                super.process(chunks);
            }

            @Override
            protected void done() {
                super.done();
                if (p == -1) {
                    holder.progressBar.setVisible(false);
                    holder.sizeLabel.setText("下载失败");
                } else {
                    holder.progressBar.setValue(100);
                    holder.progressBar.setVisible(false);
                    holder.sizeLabel.setText(FileCache.fileSizeString(item.getFileSize()));
                }

            }
        }.execute();
    }
    /**
     * 设置附件点击监听
     *
     * @param viewHolder
     * @param item
     */
    private void setAttachmentClickListener(MessageAttachmentViewHolder viewHolder, Message item) {
        MessageMouseListener listener = new MessageMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ChatMessagePanel.openFile(item.getFilePath());
                }
            }
        };


        viewHolder.attachmentPanel.addMouseListener(listener);
        viewHolder.attachmentTitle.addMouseListener(listener);
    }


    /**
     * 对方发送的图片
     *
     * @param holder
     * @param item
     */
    private void processLeftImageMessage(MessageLeftImageViewHolder holder, Message item) {
        holder.sender.setText(item.getPlainName());

        processImage(item, holder.image);

        listView.setScrollHiddenOnMouseLeave(holder.image);
        listView.setScrollHiddenOnMouseLeave(holder.imageBubble);

        // 绑定右键菜单
        attachPopupMenu(holder, item);
    }

    /**
     * 处理 对方 发送的语音消息
     *
     * @param holder
     * @param item
     */
    private void processLeftVoiceMessage(MessageLeftVoiceViewHolder holder, Message item) {
        processVoice(item, holder);
        holder.sender.setText(item.getPlainName());
        attachPopupMenu(holder, item);

    }

    /**
     * 自己发送的语音消息
     *
     * @param holder
     * @param item
     */
    private void processRightVoiceMessage(MessageRightVoiceViewHolder holder, Message item) {
        processVoice(item, holder);
        attachPopupMenu(holder, item);

    }

    /**
     * 处理语音消息
     *
     * @param item
     * @param holder
     */
    private void processVoice(Message item, MessageVoiceViewHolder holder) {

        holder.contentTagPanel.setTag(item);
// 设置语音时长（单位为秒，四舍五入）
        long voiceDurationSec = Math.max(1, Math.round(item.getVoiceLength() / 1000.0)); // 最少显示1秒
        holder.durationText.setText(voiceDurationSec + "");

// 根据语音长度设置 padding（撑出视觉宽度）
        int minPadding = 5;   // px
        int maxPadding = 80;   // px
        int padding = (int) Math.min(maxPadding, Math.max(minPadding, voiceDurationSec * 5));
        holder.gapText.setText(""); // 清空文字内容
        holder.gapText.setPreferredSize(new Dimension(padding, 1));


        //播放语音
        holder.messageBubble.addMouseListener(new MessageMouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON1) {

                    String voicePath = item.getFilePath();
                    File file = new File(voicePath);
                    if (!file.exists()) {
                        DownloadStatus status = DownloadManager.getStatus(voicePath);
                        if (status == DownloadStatus.RUNNING) {
                            JOptionPane.showMessageDialog(null, "下载中...", "打开失败", JOptionPane.ERROR_MESSAGE);
                        } else if (status != DownloadStatus.SUCCESS) {
                            JOptionPane.showMessageDialog(null, "下载失败", "打开失败", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        try {
                            player.play(voicePath, new VoicePlaybackListener() {
                                @Override
                                public void playbackPosition(int position) {
                                    SwingUtilities.invokeLater(() -> {
                                        System.out.println(holder.progressBar);
                                        holder.progressBar.setValue(position);
                                    });
                                }

                                @Override
                                public void playbackStarted() {
                                    SwingUtilities.invokeLater(() -> {
                                        holder.removeUnreadPoint();
                                        RCProgressBar progressBar = holder.progressBar;
                                        progressBar.setVisible(true);
                                        progressBar.setMaximum(Integer.parseInt( String.valueOf(item.getVoiceLength())));
                                        holder.durationText.start();
                                    });

                                }

                                @Override
                                public void playbackFinished() {
                                    SwingUtilities.invokeLater(() -> {
                                        holder.progressBar.setValue(Math.toIntExact(item.getVoiceLength()));
                                        holder.durationText.stop();
                                        holder.progressBar.setValue(0);
                                    });
                                }
                            });
                        } catch (JavaLayerException | FileNotFoundException ex) {
                            log.error(ex.getMessage(),e);
                            JOptionPane.showMessageDialog(null, ex.getMessage(), "播放失败", JOptionPane.ERROR_MESSAGE);
                        }

                    }

                }
                super.mouseReleased(e);
            }
        });
    }

    /**
     * 对方发送的图片
     *
     * @param holder
     * @param item
     */
    private void processLeftVideoMessage(MessageLeftVideoViewHolder holder, Message item) {
        holder.sender.setText(item.getPlainName());

        try {
            processVideo(item
                    , holder.timeLabel
                    , holder.playImgLabel
                    , holder.slaveImgLabel
                    , holder.videoComponent);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        holder.videoComponent.setTag(item);
        listView.setScrollHiddenOnMouseLeave(holder.videoComponent);
        listView.setScrollHiddenOnMouseLeave(holder.imageBubble);

        // 绑定右键菜单
        attachPopupMenu(holder, item);
    }

    /**
     * 对方发送的图片
     *
     * @param holder
     * @param item
     */
    private void processRightVideoMessage(MessageRightVideoViewHolder holder, Message item) {
        try {
            processVideo(item
                    , holder.timeLabel
                    , holder.playImgLabel
                    , holder.slaveImgLabel
                    , holder.videoComponent);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        holder.videoComponent.setTag(item);
        listView.setScrollHiddenOnMouseLeave(holder.videoComponent);
        listView.setScrollHiddenOnMouseLeave(holder.imageBubble);
        // 判断是否显示重发按钮
        holder.resend.setVisible(item.isNeedToResend());


        if (holder.resend.getMouseListeners().length<=1) {
            holder.resend.addMouseListener(new MessageMouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ChatUtil.deleteMessage(item);
                    parent.sendFileMessage(item.getFilePath());

                    super.mouseClicked(e);
                }
            });
        }
        // 绑定右键菜单
        attachPopupMenu(holder, item);
    }

    /**
     * 我发送的图片
     *
     * @param holder
     * @param item
     */
    private void processRightImageMessage(MessageRightImageViewHolder holder, Message item) {

        processImage(item, holder.image);
        holder.sendingProgress.setVisible(item.getProgress() != 100);


        // 判断是否显示重发按钮
        holder.resend.setVisible(item.isNeedToResend());
        if (holder.resend.getMouseListeners().length<=1) {
            holder.resend.addMouseListener(new MessageMouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ChatUtil.deleteMessage(item);
                    parent.sendFileMessage(item.getFilePath());

                    super.mouseClicked(e);
                }
            });
        }

        // 绑定右键菜单
        attachPopupMenu(holder, item);

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
    private void processVideo(Message item, JLabel timeLabel, JLabel playImgLabel, JLabel slaveImgLabel, JComponent videoComponent) throws IOException {
        //#############判断缩略图是否下载完成#########################

        timeLabel.setText(getSecString(item.getPlayLength()));
        if (item.getVideoPic()!=null){
            //存在视频缩略图
            slaveImgLabel.setIcon(new ImageIcon(item.getVideoPic()));
            playImgLabel.setIcon(IconUtil.getIcon(this, "/image/play48.png"));
        }else {
            slaveImgLabel.setIcon(IconUtil.getIcon(this, "/image/image_loading.gif"));
            ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
                @Override
                public void run() {
                    //等待下载完成
                    final String slaveImgPath = item.getSlavePath();
                    DownloadManager.awaitDownload(slaveImgPath,1000*60*5);
                    File file = new File(slaveImgPath);
                    try {
                        ImageIcon imageIcon = new ImageIcon(ImageIO.read(file));
                        ImageUtil.preferredImageSize(imageIcon);
                        SwingUtilities.invokeLater(() -> {
                            slaveImgLabel.setIcon(imageIcon);
                        });
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    ImageIcon playImg = IconUtil.getIcon(this, "/image/play48.png");
                    SwingUtilities.invokeLater(() -> {
                        if (playImg != null) {
                            playImgLabel.setIcon(playImg);
                        }
                    });


                }
            });
        }

        // 当点击视频时，使用默认程序打开图片
        videoComponent.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {

                    DownloadStatus status = DownloadManager.getStatus(item.getFilePath());
                    if (status == DownloadStatus.RUNNING) {
                        JOptionPane.showMessageDialog(MainFrame.getContext(), "下载中...", "文件不存在", JOptionPane.WARNING_MESSAGE);
                        super.mouseReleased(e);
                        return;
                    }
                    ChatMessagePanel.openFile(item.getFilePath());
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
    private void processImage(Message item, MessageImageLabel imageLabel) {
        //显示加载中
        ImageIcon imageIcon = IconUtil.getIcon(this, "/image/image_loading.gif");
        imageLabel.setIcon(imageIcon);
        String filePath = item.getSlavePath();
        if (StringUtils.isEmpty(filePath)) {
            filePath = item.getFilePath();
        }
        final String finalPath = filePath;
        imageLabel.setTag(item);

        ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                //阻塞
                DownloadManager.awaitDownload(finalPath,1000*60*5);
                File file = new File(finalPath);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {


                        if (file.length() == 0) {
                            imageLabel.setIcon(null);
                            imageLabel.setText("[不支持的表情消息，请在手机上查看]");
                            return;
                        }
                        ImageIcon imageIcon = null;
                        if (ImageUtil.isGIF(finalPath)) {
                            imageIcon = ImageUtil.preferredGifSize(finalPath, item.getImgWidth(), item.getImgHeight());
                        } else {
                            imageIcon = imageCache.tryGetThumbCache(file);
                            ImageUtil.preferredImageSize(imageIcon);
                        }
                        imageLabel.setIcon(imageIcon);
                        // 当点击图片时，使用默认程序打开图片
                        imageLabel.addMouseListener(new MessageMouseListener() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                File file = new File(item.getFilePath());
                                new SwingWorker<Object, BufferedImage>() {
                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        //阻塞
                                        DownloadManager.awaitDownloadTimeOut(item.getFilePath());
                                        if (ImageUtil.isGIF(item.getFilePath())) {
                                            ChatMessagePanel.openFile(item.getFilePath());
                                        } else {
                                            if (file.exists() && file.length() <= 1024 * 1024) {
                                                BufferedImage read = ImageIO.read(new File(item.getFilePath()));
                                                publish(read);
                                            } else {
                                                ChatMessagePanel.openFile(item.getFilePath());
                                            }
                                        }

                                        return null;
                                    }

                                    @Override
                                    protected void process(List<BufferedImage> chunks) {
                                        BufferedImage read = chunks.get(chunks.size() - 1);
                                        if (read == null) {
                                            JOptionPane.showMessageDialog(MainFrame.getContext(), "图片下载中...", "文件不存在", JOptionPane.WARNING_MESSAGE);
                                            return;
                                        }
                                        ImageViewerFrame instance = ImageViewerFrame.getInstance();
                                        instance.setImage(read);

                                        instance.toFront();
                                        instance.setVisible(true);
                                    }

                                }.execute();
                                super.mouseClicked(e);
                            }
                        });
                    }
                });

            }
        });


    }


    /**
     * 处理 我发送的文本消息
     *
     * @param holder
     * @param item
     */
    private void processRightTextMessage(MessageRightTextViewHolder holder, final Message item) {

        holder.text.setText(item.getPlaintext());

        holder.text.setTag(item);

        //holder.text.setCaretPosition(holder.text.getDocument().getLength());
        //holder.text.insertIcon(IconUtil.getIcon(this, "/image/smile.png", 18,18));

        //processMessageContent(holder.messageText, item);
        //registerMessageTextListener(holder.messageText, item);


        if (item.isNeedToResend()) {
            holder.sendingProgress.setVisible(false);
            holder.resend.setVisible(true);
        } else {
            holder.resend.setVisible(false);
            // 如果是刚发送的消息，显示正在发送进度条
            holder.sendingProgress.setVisible(item.getProgress() != 100);
        }
        //TODO 通过数量来看后期可能会有BUG
        //TODO 例如其它地方多增加了一个mouseListener
        if (holder.resend.getMouseListeners().length<=1){
            holder.resend.addMouseListener(new MessageMouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    ChatUtil.deleteMessage(item);
                    parent.sendTextMessage(item.getContent());
                    super.mouseClicked(e);
                }
            });
        }


        // 绑定右键菜单
        attachPopupMenu(holder, item);

        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.text);
    }

    /**
     * 处理 对方 发送的文本消息
     *
     * @param holder
     * @param item
     */
    private void processLeftTextMessage(MessageLeftTextViewHolder holder, final Message item) {


        holder.text.setText(item.getPlaintext() == null ? "[空消息]" : item.getPlaintext());
        holder.text.setTag(item);

        holder.sender.setText(item.getPlainName());

        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.text);
        attachPopupMenu(holder, item);
    }

    private void processLeftLinkMessage(MessageLeftLinkOfAppViewHolder viewHolder, Message item) {
        processLinkMessage(viewHolder, item);
        viewHolder.sender.setText(item.getPlainName());
        attachPopupMenu(viewHolder, item);
    }

    private void processRightLinkMessage(MessageLinkOfAppViewHolder viewHolder, Message item) {
        processLinkMessage(viewHolder, item);
        attachPopupMenu(viewHolder, item);
    }

    private void processLeftContactsCardOfAppMessage(MessageLeftContactsCardOfAppViewHolder viewHolder, Message item) {
        processContactsCardMessage(viewHolder, item);
        viewHolder.sender.setText(item.getPlainName());
        attachPopupMenu(viewHolder, item);
    }

    private void processRightContactsCardOfAppMessage(MessageRightContactsCardOfAppViewHolder viewHolder, Message item) {
        processContactsCardMessage(viewHolder, item);
        attachPopupMenu(viewHolder, item);
    }

    private void processContactsCardMessage(MessageContactsCardOfAppViewHolder cardOfAppViewHolder, Message item) {
        cardOfAppViewHolder.contentTitlePanel.setTag(item);
        cardOfAppViewHolder.desc.setText("WechatId："+item.getContactsId()
                +"\n地区："+item.getContactsProvince()
        +" "+item.getContactsCity());
        cardOfAppViewHolder.title.setText(item.getContactsNickName());
        cardOfAppViewHolder.sourcePanel.setVisible(true);
        cardOfAppViewHolder.sourceName.setText("联系人卡片");

        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
            if (StringUtils.isNotEmpty(item.getThumbUrl())) {
                try {
                    BufferedImage image = ImageIO.read(URI.create(item.getThumbUrl()).toURL());
                    if (image != null) {
                        SwingUtilities.invokeLater(() ->
                                cardOfAppViewHolder.icon.setIcon(new ImageIcon(ImageUtil.preferredImageSize(image, MessageLinkOfAppViewHolder.THUMB_WIDTH))));
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }

            } else {
                DownloadTask<BufferedImage> downloadTask = new DownloadTask<>();
                downloadTask.setMsgId(item.getMsgId());
                downloadTask.setResourceType(WXMsgUrl.SLAVE_TYPE);
                downloadTask.setTaskId(item.getMsgId() + WXMsgUrl.SLAVE_TYPE);
                downloadTask.setType(DownloadType.ImgByMsgID);
                BufferedImage image = DownloadManager.submitAwait(downloadTask);
                if (image!=null){
                    SwingUtilities.invokeLater(() ->
                            cardOfAppViewHolder.icon.setIcon(new ImageIcon(ImageUtil.preferredImageSize(image, MessageLinkOfAppViewHolder.THUMB_WIDTH))));
                }
            }

        });

       final Contacts contacts = Contacts.builder()
                .sex(item.getContactsSex())
                .province(item.getContactsProvince())
                .city(item.getContactsCity())
                .signature("")
                .remarkname("")
                .username(item.getContactsUserName())
                .headimgurl(item.getContactsHeadImgUrl())
               .ticket(item.getContactsTicket())
                .nickname(item.getContactsNickName()).build();
        //点击打开链接
        MessageMouseListener messageMouseListener = new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

                UserInfoPopup instance = UserInfoPopup.getInstance();
                instance.setContacts(contacts);
                instance.show(e.getComponent(), e.getX(), e.getY());
                super.mouseClicked(e);
            }
        };
        cardOfAppViewHolder.desc.addMouseListener(messageMouseListener);
        cardOfAppViewHolder.title.addMouseListener(messageMouseListener);
        cardOfAppViewHolder.icon.addMouseListener(messageMouseListener);
        cardOfAppViewHolder.contentTitlePanel.addMouseListener(messageMouseListener);
        cardOfAppViewHolder.messageBubble.addMouseListener(messageMouseListener);
        listView.setScrollHiddenOnMouseLeave(cardOfAppViewHolder.desc);
        listView.setScrollHiddenOnMouseLeave(cardOfAppViewHolder.title);
        listView.setScrollHiddenOnMouseLeave(cardOfAppViewHolder.icon);
        listView.setScrollHiddenOnMouseLeave(cardOfAppViewHolder.contentTitlePanel);
        listView.setScrollHiddenOnMouseLeave(cardOfAppViewHolder.messageBubble);
    }

    private void processLinkMessage(MessageLinkOfAppViewHolder linkViewHolder, Message item) {
        linkViewHolder.contentTitlePanel.setTag(item);
        linkViewHolder.desc.setText(StringEscapeUtils.unescapeHtml4(item.getDesc()));
        linkViewHolder.title.setText(item.getTitle());
        if (StringUtils.isEmpty(item.getSourceName())) {
            linkViewHolder.sourcePanel.setVisible(false);
        } else {
            linkViewHolder.sourceName.setText(item.getSourceName());
        }

        ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.isNotEmpty(item.getThumbUrl())) {
                    try {
                        BufferedImage image = ImageIO.read(URI.create(item.getThumbUrl()).toURL());
                        if (image != null) {
                            SwingUtilities.invokeLater(() -> {
                                linkViewHolder.icon.setIcon(new ImageIcon(ImageUtil.preferredImageSize(image, MessageLinkOfAppViewHolder.THUMB_WIDTH)));
                                //有图片时缩短宽度，让其与无图的Panel尽量一致
                                linkViewHolder.desc.setColumns(16);
                            });
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                }else{
                    DownloadTask<BufferedImage> downloadTask = new DownloadTask<>();
                    downloadTask.setMsgId(item.getMsgId());
                    downloadTask.setResourceType(WXMsgUrl.SLAVE_TYPE);
                    downloadTask.setTaskId(item.getMsgId()+WXMsgUrl.SLAVE_TYPE);
                    downloadTask.setType(DownloadType.ImgByteByMsgID);
                    BufferedImage image = DownloadManager.submitAwait(downloadTask);
                    if (image != null) {
                        SwingUtilities.invokeLater(() -> {
                            linkViewHolder.icon.setIcon(new ImageIcon(ImageUtil.preferredImageSize(image, MessageLinkOfAppViewHolder.THUMB_WIDTH)));
                            //有图片时缩短宽度，让其与无图的Panel尽量一致
                            linkViewHolder.desc.setColumns(16);
                        });
                    }
                }

            }
        });


        //点击打开链接
        MessageMouseListener messageMouseListener = new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (StringUtils.isNotEmpty(item.getUrl())) {
                        try {
                            Desktop.getDesktop().browse(new URI(item.getUrl()));
                        } catch (IOException | URISyntaxException ioException) {
                            log.error(ioException.getMessage(), ioException);
                        }
                    }
                }
            }
        };
        linkViewHolder.desc.addMouseListener(messageMouseListener);
        linkViewHolder.title.addMouseListener(messageMouseListener);
        linkViewHolder.icon.addMouseListener(messageMouseListener);
        linkViewHolder.contentTitlePanel.addMouseListener(messageMouseListener);
        linkViewHolder.messageBubble.addMouseListener(messageMouseListener);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.desc);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.title);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.icon);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.contentTitlePanel);
        listView.setScrollHiddenOnMouseLeave(linkViewHolder.messageBubble);
        Dimension preferredSize = linkViewHolder.contentTitlePanel.getPreferredSize();
    }

    /**
     * 处理消息发送时间 以及 消息发送者头像
     *
     * @param item
     * @param preItem
     * @param holder
     */
    private void processTimeAndAvatar(Message item, Message preItem, BaseMessageViewHolder holder) {
        LocalDateTime messageTime = item.getMessageTime();
        if (messageTime == null) {
            messageTime = item.getCreateTime();
            item.setMessageTime(messageTime);
        }

        // 如果当前消息的时间与上条消息时间相差大于1分钟，则显示当前消息的时间
        if (preItem != null) {

            if (preItem.getMessageTime() == null) {
                preItem.setMessageTime(item.getCreateTime());
            }

            if (TimeUtil.inTheSameMinute(messageTime
                    , preItem.getMessageTime())) {
                holder.time.setVisible(false);
            } else {
                holder.time.setVisible(true);
                holder.time.setText(TimeUtil.diff(messageTime, true));
            }
        } else {
            holder.time.setVisible(true);
            holder.time.setText(TimeUtil.diff(messageTime, true));
        }

        String senderId = ContactsTools.isRoomContact(item.getFromUsername()) && !item.getFromUsername().equals(Core.getUserName()) ? item.getFromMemberOfGroupUsername()
                : item.getFromUsername();

        String roomId = item.getFromUsername();
        if (roomId.equals(Core.getUserName())) {
            roomId = item.getToUsername();
        }

        if (holder.avatar!=null){
            if (roomId.equals(senderId)) {
                new HeadLoadingSwingWorker(holder.avatar,roomId).loadAvatar();
            }else{
                new HeadLoadingSwingWorker(holder.avatar,roomId,senderId).loadAvatar();

            }
            bindAvatarAction(holder.avatar, item,senderId,roomId);
        }

    }


    private void bindAvatarAction(JLabel avatarLabel, Message item,String senderId,String roomId) {

        avatarLabel.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Contacts contacts = null;
                if (item.isGroup()) {
                    contacts = ContactsTools.getMemberOfGroup(roomId, senderId);
                } else {
                    contacts = Core.getMemberMap().get(senderId);
                }
                if (contacts == null) {
                    return;
                }
                contacts.setGroupName(roomId);
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

    private void attachPopupMenu(BaseMessageViewHolder viewHolder, Message item) {
        JComponent contentComponent = null;
        RCMessageBubble messageBubble = null;
        WxRespConstant.WXReceiveMsgCodeEnum typeEnum = WxRespConstant.WXReceiveMsgCodeEnum.getByCode(item.getMsgType());
        boolean isSelf = Core.getUserName().equals(item.getFromUsername());
        switch (typeEnum) {
            case MSGTYPE_TEXT: {
                if (isSelf) {
                    MessageRightTextViewHolder holder = (MessageRightTextViewHolder) viewHolder;
                    contentComponent = holder.text;
                    messageBubble = holder.messageBubble;

                } else {
                    MessageLeftTextViewHolder holder = (MessageLeftTextViewHolder) viewHolder;
                    contentComponent = holder.text;
                    messageBubble = holder.messageBubble;

                }
                break;
            }
            case MSGTYPE_IMAGE:
            case MSGTYPE_EMOTICON:{
                if (isSelf){
                    MessageRightImageViewHolder holder = (MessageRightImageViewHolder) viewHolder;
                    contentComponent = holder.image;
                    messageBubble = holder.imageBubble;
                }else{
                    MessageLeftImageViewHolder holder = (MessageLeftImageViewHolder) viewHolder;
                    contentComponent = holder.image;
                    messageBubble = holder.imageBubble;

                }
                break;
            }
            case MSGTYPE_VIDEO:{
                if (isSelf){
                    MessageRightVideoViewHolder holder = (MessageRightVideoViewHolder) viewHolder;
                    contentComponent = holder.videoComponent;
                    messageBubble = holder.imageBubble;
                }else {
                    MessageLeftVideoViewHolder holder = (MessageLeftVideoViewHolder) viewHolder;
                    contentComponent = holder.videoComponent;
                    messageBubble = holder.imageBubble;


                }
                break;
            }
            case MSGTYPE_VOICE:{
               if (isSelf){
                   MessageRightVoiceViewHolder holder = (MessageRightVoiceViewHolder) viewHolder;
                   contentComponent = holder.contentTagPanel;
                   messageBubble = holder.messageBubble;
                }else {
                   MessageLeftVoiceViewHolder holder = (MessageLeftVoiceViewHolder) viewHolder;
                   contentComponent = holder.contentTagPanel;
                   messageBubble = holder.messageBubble;

                }
                break;
            }
            case MSGTYPE_APP:{
                switch (WxRespConstant.WXReceiveMsgCodeOfAppEnum.getByCode(item.getAppMsgType())){

                    case FILE:{
                        if (isSelf){
                            MessageRightAttachmentViewHolder holder = (MessageRightAttachmentViewHolder) viewHolder;
                            contentComponent = holder.attachmentPanel;
                            messageBubble = holder.messageBubble;

                            holder.attachmentTitle.addMouseListener(new MessageMouseListener() {
                                @Override
                                public void mouseReleased(MouseEvent e) {
                                    if (e.getButton() == MouseEvent.BUTTON3) {
                                        // 通过holder.attachmentPane.getTag()可以获取文件附件信息
                                        popupMenu.show(holder.attachmentPanel, e.getX(), e.getY(), item.getMsgType());
                                    }
                                }
                            });
                        }else {
                            MessageLeftAttachmentViewHolder holder = (MessageLeftAttachmentViewHolder) viewHolder;
                            contentComponent = holder.attachmentPanel;
                            messageBubble = holder.messageBubble;

                            holder.attachmentTitle.addMouseListener(new MessageMouseListener() {
                                @Override
                                public void mouseReleased(MouseEvent e) {
                                    if (e.getButton() == MouseEvent.BUTTON3) {
                                        popupMenu.show(holder.attachmentPanel, e.getX(), e.getY(), item.getMsgType());
                                    }
                                }
                            });

                        }
                        break;
                    }
                    default:
                    case PROGRAM:
                    case PICTURE:
                    case LINK:{
                            MessageAppViewHolder holder = (MessageAppViewHolder) viewHolder;
                            contentComponent = holder.contentTitlePanel;
                            messageBubble = holder.messageBubble;
                        break;
                    }

                }
                break;
            }
            case MSGTYPE_VERIFYMSG:
            case MSGTYPE_SHARECARD:{
                MessageAppViewHolder holder = (MessageAppViewHolder) viewHolder;
                contentComponent = holder.contentTitlePanel;
                messageBubble = holder.messageBubble;
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
                    popupMenu.show((Component) e.getSource(), e.getX(), e.getY(), item.getMsgType());
                }

                super.mouseReleased(e);
            }
        });

        messageBubble.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    popupMenu.show(finalContentComponent, e.getX(), e.getY(), item.getMsgType());
                }
            }
        });
    }


}
