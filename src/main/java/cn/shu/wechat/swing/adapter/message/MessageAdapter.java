package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.enums.WXReceiveMsgCodeOfAppEnum;
import cn.shu.wechat.pojo.dto.msg.url.WXMsgUrl;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.swing.adapter.BaseAdapter;
import cn.shu.wechat.swing.adapter.ViewHolder;
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
import cn.shu.wechat.swing.panels.ChatPanel;
import cn.shu.wechat.swing.panels.RoomChatContainer;
import cn.shu.wechat.swing.utils.*;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import javazoom.jl.player.Player;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Created by 舒新胜 on 17-6-2.
 */
@Log4j2
public class MessageAdapter extends BaseAdapter<BaseMessageViewHolder> {
    private final List<Message> messageItems;
    private final RCListView listView;
    private final AttachmentIconHelper attachmentIconHelper = new AttachmentIconHelper();
    private final ImageCache imageCache;

    private final FileCache fileCache;
    private final MessagePopupMenu popupMenu = new MessagePopupMenu();
    private ChatPanel parent;

    MessageViewHolderCacheHelper messageViewHolderCacheHelper;

    public MessageAdapter(ChatPanel parent,List<Message> messageItems, RCListView listView, MessageViewHolderCacheHelper messageViewHolderCacheHelper) {
        this.messageItems = messageItems;
        this.listView = listView;
        this.parent = parent;
        // currentUser = currentUserService.findAll().get(0);
        imageCache = new ImageCache();
        fileCache = new FileCache();
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
        switch (WXReceiveMsgCodeEnum.getByCode(viewType)) {
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
                MessageSystemMessageViewHolder holder = messageViewHolderCacheHelper.tryGetSystemMessageViewHolder();
                if (holder == null) {
                    holder = new MessageSystemMessageViewHolder();
                }
                return holder;
            }
            default:
            case MSGTYPE_TEXT: {
                if (isSelf) {
                    MessageRightTextViewHolder holder = messageViewHolderCacheHelper.tryGetRightTextViewHolder();
                    if (holder == null) {
                        holder = new MessageRightTextViewHolder();
                    }

                    return holder;
                } else {
                    MessageLeftTextViewHolder holder = messageViewHolderCacheHelper.tryGetLeftTextViewHolder();
                    if (holder == null) {
                        holder = new MessageLeftTextViewHolder(messageItem.isGroup());
                    }

                    return holder;
                }
            }
            case MSGTYPE_IMAGE:
            case MSGTYPE_EMOTICON: {
                if (isSelf) {
                    MessageRightImageViewHolder holder = messageViewHolderCacheHelper.tryGetRightImageViewHolder();
                    if (holder == null) {
                        holder = new MessageRightImageViewHolder();
                    }

                    return holder;
                } else {
                    MessageLeftImageViewHolder holder = messageViewHolderCacheHelper.tryGetLeftImageViewHolder();
                    if (holder == null) {
                        holder = new MessageLeftImageViewHolder(messageItem.isGroup());
                    }

                    return holder;
                }
            }
            case MSGTYPE_VIDEO:{
                if (isSelf) {
                    MessageRightVideoViewHolder holder = messageViewHolderCacheHelper.tryGetRightVideoViewHolder();
                    if (holder == null) {
                        holder = new MessageRightVideoViewHolder(
                                ImageUtil.getScaleDimen(messageItem.getImgWidth()
                                        , messageItem.getImgHeight()));
                    }

                    return holder;
                } else {
                    MessageLeftVideoViewHolder holder = messageViewHolderCacheHelper.tryGetLeftVideoViewHolder();
                    if (holder == null) {
                        holder = new MessageLeftVideoViewHolder(messageItem.isGroup(),
                                ImageUtil.getScaleDimen(messageItem.getImgWidth()
                                        , messageItem.getImgHeight()));
                    }

                    return holder;
                }
            }
            case MSGTYPE_APP:{
               switch (WXReceiveMsgCodeOfAppEnum.getByCode(subViewType)){
                   case FILE:{
                       if (isSelf){
                           MessageRightAttachmentViewHolder holder = messageViewHolderCacheHelper.tryGetRightAttachmentViewHolder();
                           if (holder == null) {
                               holder = new MessageRightAttachmentViewHolder();
                           }

                           return holder;
                       }else {
                           MessageLeftAttachmentViewHolder holder = messageViewHolderCacheHelper.tryGetLeftAttachmentViewHolder();
                           if (holder == null) {
                               holder = new MessageLeftAttachmentViewHolder(messageItem.isGroup());
                           }

                           return holder;
                       }
                   }
                   default:
                   case MUSIC:
                   case LINK:{
                       if (isSelf){
                           return new MessageRightLinkOfAppViewHolder();

                       }else {
                           return new MessageLeftLinkOfAppViewHolder(messageItem.isGroup());
                       }
                   }
                   case PICTURE:
                   case PROGRAM:{
                      if (isSelf){
                          MessageRightProgramOfAppViewHolder holder = messageViewHolderCacheHelper.tryGetRightProgramOfAppViewHolder();
                          if (holder == null) {
                              holder = new MessageRightProgramOfAppViewHolder();
                          }

                          return holder;
                       }else {
                          MessageLeftProgramOfAppViewHolder holder = messageViewHolderCacheHelper.tryGetLeftProgramOfAppViewHolder();
                          if (holder == null) {
                              holder = new MessageLeftProgramOfAppViewHolder(messageItem.isGroup());
                          }

                          return holder;
                       }
                   }

               }
            }

            case MSGTYPE_VOICE: {
                if (isSelf) {
                    MessageRightVoiceViewHolder holder = messageViewHolderCacheHelper.tryGetRightVoiceViewHolder();
                    if (holder == null) {
                        holder = new MessageRightVoiceViewHolder();
                    }

                    return holder;
                } else {
                    MessageLeftVoiceViewHolder holder = messageViewHolderCacheHelper.tryGetLeftVoiceViewHolder();
                    if (holder == null) {
                        holder = new MessageLeftVoiceViewHolder(messageItem.isGroup());
                    }

                    return holder;
                }
            }

        }
    }

    @Override
    public void onBindViewHolder(BaseMessageViewHolder viewHolder, int position) {
        if (viewHolder == null) {
            return;
        }

        final Message item = messageItems.get(position);
        Message preItem = position == 0 ? null : messageItems.get(position - 1);

       processTimeAndAvatar(item, preItem, viewHolder);
        if (item.isRevoke()){
            viewHolder.revoke.setVisible(true);
        }
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
        } else if (viewHolder instanceof MessageRightLinkOfAppViewHolder) {
            processRightLinkMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftLinkOfAppViewHolder) {
            processLeftLinkMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageRightProgramOfAppViewHolder) {
            processRightProgramOfAppMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftProgramOfAppViewHolder) {
            processLeftProgramOfAppMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageRightContactsCardOfAppViewHolder) {
            processRightContactsCardOfAppMessage(viewHolder, item);
        } else if (viewHolder instanceof MessageLeftContactsCardOfAppViewHolder) {
            processLeftContactsCardOfAppMessage(viewHolder, item);
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
                ImageIcon imageIcon = new ImageIcon(new URL(item.getSourceIconUrl()));
                ImageUtil.preferredImageSize(imageIcon, 16);
                appViewHolder.sourceIcon.setIcon(imageIcon);

            } catch (MalformedURLException e) {
                log.error(e.getMessage());
            }
        }

        if (StringUtils.isEmpty(item.getThumbUrl())){
            appViewHolder.imageLabel.setIcon(IconUtil.getIcon(this,"/image/image_loading.gif"));
            new SwingWorker<Object,Object>(){
                ImageIcon imageIcon = null;
                @Override
                protected Object doInBackground() throws Exception {
                    byte[] bytes = DownloadTools.downloadImgByteByMsgID(item.getMsgId(), WXMsgUrl.BIG_TYPE);
                    if (bytes == null || bytes.length<=0){
                        bytes = DownloadTools.downloadImgByteByMsgID(item.getMsgId(), WXMsgUrl.SLAVE_TYPE);
                    }
                    if (bytes != null && bytes.length>0) {
                        if (ImageUtil.isGIF(bytes)) {
                            imageIcon = ImageUtil.preferredGifSize(bytes, item.getImgWidth(), item.getImgHeight());
                        }else{
                           imageIcon = new ImageIcon(bytes);
                           ImageUtil.preferredImageSize(imageIcon, 200);
                        }

                    }
                    return null;
                }

                @Override
                protected void done() {
                    if (imageIcon != null) {
                        appViewHolder.imageLabel.setIcon(imageIcon);
                    }
                }
            }.execute();
        } else if (StringUtils.isEmpty(item.getFilePath())) {
            try {
                appViewHolder.imageLabel.setIcon(new ImageIcon(new URL(item.getThumbUrl())));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            new SwingWorker<Object, Object>() {
                ImageIcon imageIcon = null;

                @Override
                protected Object doInBackground() throws Exception {
                    DownloadTools.awaitDownload(item.getFilePath());
                    imageIcon = new ImageIcon(item.getFilePath());
                    ImageUtil.preferredImageSize(imageIcon, 200);
                    return null;
                }

                @Override
                protected void done() {
                    if (imageIcon != null) {
                        appViewHolder.imageLabel.setIcon(imageIcon);
                    }
                }
            }.execute();
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
                                ioException.printStackTrace();
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

    /**
     * 处理系统消息
     *
     * @param viewHolder
     * @param item
     */
    private void processSystemMessage(ViewHolder viewHolder, Message item) {
        MessageSystemMessageViewHolder holder = (MessageSystemMessageViewHolder) viewHolder;
        holder.text.setText(item.getPlaintext());
    }

    /**
     * 其它用户的附件消息
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftAttachmentMessage(ViewHolder viewHolder, Message item) {
        MessageLeftAttachmentViewHolder holder = (MessageLeftAttachmentViewHolder) viewHolder;

        String filePath = item.getFilePath();
        holder.attachmentPanel.setTag(item);

        ImageIcon attachmentTypeIcon = attachmentIconHelper.getImageIcon(filePath);
        holder.attachmentIcon.setIcon(attachmentTypeIcon);
        holder.attachmentTitle.setText(item.getFileName());
        holder.sender.setText(item.getPlainName());
        holder.sizeLabel.setText(fileCache.fileSizeString(item.getFileSize()));

        setAttachmentClickListener(holder, item);

        listView.setScrollHiddenOnMouseLeave(holder.attachmentPanel);
        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.attachmentTitle);

        // 绑定右键菜单
        attachPopupMenu(viewHolder,item);
    }

    /**
     * 自己发送的附件消息
     *
     * @param viewHolder
     * @param item
     */
    private void processRightAttachmentMessage(ViewHolder viewHolder, Message item) {
        MessageRightAttachmentViewHolder holder = (MessageRightAttachmentViewHolder) viewHolder;
        String filename = item.getFileName();
        holder.attachmentPanel.setTag(item);
        ImageIcon attachmentTypeIcon = attachmentIconHelper.getImageIcon(filename);
        holder.attachmentIcon.setIcon(attachmentTypeIcon);
        holder.attachmentTitle.setText(item.getFileName());
        if (item.getProgress()==0 || item.getProgress() == 100){
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

        if (item.getProgress() > 0) {
            holder.sizeLabel.setText(fileCache.fileSizeString(item.getFileSize()));
        } else {
            holder.sizeLabel.setText("等待上传...");
        }

        // 绑定右键菜单
        attachPopupMenu(viewHolder, item);

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
    private void setAttachmentClickListener(MessageAttachmentViewHolder viewHolder, Message item) {
        MessageMouseListener listener = new MessageMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ChatPanel.openFile(item.getFilePath());
                }
            }
        };


        viewHolder.attachmentPanel.addMouseListener(listener);
        viewHolder.attachmentTitle.addMouseListener(listener);
    }


    /**
     * 对方发送的图片
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftImageMessage(ViewHolder viewHolder, Message item) {
        MessageLeftImageViewHolder holder = (MessageLeftImageViewHolder) viewHolder;
        holder.sender.setText(item.getPlainName());

        processImage(item, holder.image);

        listView.setScrollHiddenOnMouseLeave(holder.image);
        listView.setScrollHiddenOnMouseLeave(holder.imageBubble);

        // 绑定右键菜单
        attachPopupMenu(viewHolder, item);
    }

    /**
     * 处理 对方 发送的语音消息
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftVoiceMessage(ViewHolder viewHolder, Message item) {
        MessageLeftVoiceViewHolder holder = (MessageLeftVoiceViewHolder) viewHolder;
        processVoice(item, holder);
        holder.sender.setText(item.getPlainName());
        attachPopupMenu(viewHolder, item);

    }

    /**
     * 自己发送的语音消息
     *
     * @param viewHolder
     * @param item
     */
    private void processRightVoiceMessage(ViewHolder viewHolder, Message item) {
        MessageRightVoiceViewHolder holder = (MessageRightVoiceViewHolder) viewHolder;
        processVoice(item, holder);
        attachPopupMenu(viewHolder, item);

    }

    /**
     * 处理语音消息
     *
     * @param item
     * @param holder
     */
    private void processVoice(Message item, MessageVoiceViewHolder holder) {

        holder.contentTagPanel.setTag(item);
        double len = item.getVoiceLength() * 1.0;

        len = len / 1000;
        long round = Math.round(len);
        holder.durationText.setText(String.valueOf(round));
        StringBuilder t = new StringBuilder();
        for (long i = 0; i < round / 2; i++) {
            t.append(" ");
        }
        holder.gapText.setText(t.toString());


        //播放语音
        holder.messageBubble.addMouseListener(new MessageMouseListener() {
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
                    String voicePath = item.getFilePath();
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
                        RCProgressBar progressBar = holder.progressBar;
                        progressBar.setVisible(true);
                        progressBar.setMaximum(Integer.parseInt( String.valueOf(item.getVoiceLength())));
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
                                                publish(Integer.parseInt( String.valueOf(item.getVoiceLength())));
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
    private void processLeftVideoMessage(ViewHolder viewHolder, Message item) {
        MessageLeftVideoViewHolder holder = (MessageLeftVideoViewHolder) viewHolder;
        holder.sender.setText(item.getPlainName());

        try {
            processVideo(item
                    , holder.timeLabel
                    , holder.playImgLabel
                    , holder.slaveImgLabel
                    , holder.videoComponent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.videoComponent.setTag(item);
        listView.setScrollHiddenOnMouseLeave(holder.videoComponent);
        listView.setScrollHiddenOnMouseLeave(holder.imageBubble);

        // 绑定右键菜单
        attachPopupMenu(viewHolder, item);
    }

    /**
     * 对方发送的图片
     *
     * @param viewHolder
     * @param item
     */
    private void processRightVideoMessage(ViewHolder viewHolder, Message item) {
        MessageRightVideoViewHolder holder = (MessageRightVideoViewHolder) viewHolder;
        try {
            processVideo(item
                    , holder.timeLabel
                    , holder.playImgLabel
                    , holder.slaveImgLabel
                    , holder.videoComponent);
        } catch (IOException e) {
            e.printStackTrace();
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
        attachPopupMenu(viewHolder, item);
    }

    /**
     * 我发送的图片
     *
     * @param viewHolder
     * @param item
     */
    private void processRightImageMessage(ViewHolder viewHolder, Message item) {
        MessageRightImageViewHolder holder = (MessageRightImageViewHolder) viewHolder;

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
        attachPopupMenu(viewHolder, item);

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
        String slaveImgPath = item.getSlavePath();
        timeLabel.setText(getSecString(item.getPlayLength()));
        if (item.getVideoPic()!=null){
            slaveImgLabel.setIcon(new ImageIcon(item.getVideoPic()));
            playImgLabel.setIcon(IconUtil.getIcon(this, "/image/play48.png"));
        }else {
            new SwingWorker<Object, Object>() {
                private ImageIcon imageIcon = null;
                private ImageIcon playImg = null;

                @Override
                protected Object doInBackground() {
                    //等待下载完成
                    DownloadTools.awaitDownload(slaveImgPath);
                    File file = new File(slaveImgPath);
                    try {
                        imageIcon = new ImageIcon(ImageIO.read(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ImageUtil.preferredImageSize(imageIcon);
                    playImg = IconUtil.getIcon(this, "/image/play48.png");
                    return null;
                }

                @Override
                protected void done() {
                    if (imageIcon != null) {
                        slaveImgLabel.setIcon(imageIcon);
                    }
                    if (playImg != null) {
                        playImgLabel.setIcon(playImg);
                    }

                }
            }.execute();
        }

        // 当点击视频时，使用默认程序打开图片
        videoComponent.addMouseListener(new MessageMouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    Boolean aBoolean1 = DownloadTools.FILE_DOWNLOAD_STATUS.get(item.getFilePath());
                    if (aBoolean1!=null && !aBoolean1){
                        JOptionPane.showMessageDialog(MainFrame.getContext(), "下载中...", "文件不存在", JOptionPane.WARNING_MESSAGE);
                        super.mouseReleased(e);
                        return;
                    }
                    ChatPanel.openFile(item.getFilePath());
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
        ImageIcon imageIcon = IconUtil.getIcon(this,"/image/image_loading.gif");
        imageLabel.setIcon(imageIcon);
        String filePath = item.getSlavePath();
        if (StringUtils.isEmpty(filePath)) {
            filePath = item.getFilePath();
        }
        final String finalPath = filePath;
        imageLabel.setTag(item);

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
                        imageIcon = ImageUtil.preferredGifSize(finalPath, item.getImgWidth(), item.getImgHeight());
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
                            File file = new File(item.getFilePath());
                            new SwingWorker<Object, BufferedImage>() {
                                @Override
                                protected Object doInBackground() throws Exception {
                                    //阻塞
                                    DownloadTools.awaitDownload(item.getFilePath());
                                    if (ImageUtil.isGIF(item.getFilePath())){
                                        ChatPanel.openFile(item.getFilePath());
                                    }else{
                                        if (file.exists() && file.length() <= 1024 * 1024) {
                                            BufferedImage read = ImageIO.read(new File(item.getFilePath()));
                                            publish(read);
                                        } else {
                                            ChatPanel.openFile(item.getFilePath());
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
    private void processRightTextMessage(ViewHolder viewHolder, final Message item) {
        MessageRightTextViewHolder holder = (MessageRightTextViewHolder) viewHolder;

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
            if (item.getProgress() != 100) {
                holder.sendingProgress.setVisible(true);
            } else {
                holder.sendingProgress.setVisible(false);
            }
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
        attachPopupMenu(viewHolder, item);

        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.text);
    }

    /**
     * 处理 对方 发送的文本消息
     *
     * @param viewHolder
     * @param item
     */
    private void processLeftTextMessage(ViewHolder viewHolder, final Message item) {
        MessageLeftTextViewHolder holder = (MessageLeftTextViewHolder) viewHolder;

        holder.text.setText(item.getPlaintext() == null ? "[空消息]" : item.getPlaintext());
        holder.text.setTag(item);

        holder.sender.setText(item.getPlainName());

        listView.setScrollHiddenOnMouseLeave(holder.messageBubble);
        listView.setScrollHiddenOnMouseLeave(holder.text);
        attachPopupMenu(viewHolder, item);
    }

    private void processLeftLinkMessage(ViewHolder viewHolder, Message item) {
        processLinkMessage(viewHolder, item);
        ((MessageLeftLinkOfAppViewHolder) viewHolder).sender.setText(item.getPlainName());
        attachPopupMenu(viewHolder, item);
    }

    private void processRightLinkMessage(ViewHolder viewHolder, Message item) {
        processLinkMessage(viewHolder, item);
        attachPopupMenu(viewHolder, item);
    }

    private void processLeftContactsCardOfAppMessage(ViewHolder viewHolder, Message item) {
        processContactsCardMessage(viewHolder, item);
       ((MessageLeftContactsCardOfAppViewHolder) viewHolder).sender.setText(item.getPlainName());
        attachPopupMenu(viewHolder, item);
    }

    private void processRightContactsCardOfAppMessage(ViewHolder viewHolder, Message item) {
        processContactsCardMessage(viewHolder, item);
        attachPopupMenu(viewHolder, item);
    }

    private void processContactsCardMessage(ViewHolder viewHolder, Message item) {
        MessageContactsCardOfAppViewHolder cardOfAppViewHolder = (MessageContactsCardOfAppViewHolder) viewHolder;
        cardOfAppViewHolder.contentTitlePanel.setTag(item);
        cardOfAppViewHolder.desc.setText("WechatId："+item.getContactsId()
                +"\n地区："+item.getContactsProvince()
        +" "+item.getContactsCity());
        cardOfAppViewHolder.title.setText(item.getContactsNickName());
        cardOfAppViewHolder.sourcePanel.setVisible(true);
        cardOfAppViewHolder.sourceName.setText("联系人卡片");
        new SwingWorker<Object,Object>(){
            BufferedImage image = null;
            @Override
            protected Object doInBackground() throws Exception {
                if (StringUtils.isNotEmpty(item.getThumbUrl())) {
                    image = ImageIO.read(new URL(item.getThumbUrl()));
                }else{
                    image = DownloadTools.downloadImgByMsgID(item.getMsgId(),WXMsgUrl.SLAVE_TYPE);
                }
                return null;
            }

            @Override
            protected void done() {
                if (image!=null){
                    cardOfAppViewHolder.icon.setIcon(new ImageIcon(ImageUtil.preferredImageSize(image, MessageLinkOfAppViewHolder.THUMB_WIDTH)));
                    //有图片时缩短宽度，让其与无图的Panel尽量一致
                   // cardOfAppViewHolder.desc.setColumns(16);
                }
            }
        }.execute();
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
    private void processLinkMessage(ViewHolder viewHolder, Message item) {
        MessageLinkOfAppViewHolder linkViewHolder = (MessageLinkOfAppViewHolder) viewHolder;
        linkViewHolder.contentTitlePanel.setTag(item);
        linkViewHolder.desc.setText(StringEscapeUtils.unescapeHtml4(item.getDesc()));
        linkViewHolder.title.setText(item.getTitle());
        if (StringUtils.isEmpty(item.getSourceName())) {
            linkViewHolder.sourcePanel.setVisible(false);
        } else {
            linkViewHolder.sourceName.setText(item.getSourceName());
        }
        new SwingWorker<Object,Object>(){
            BufferedImage image = null;
            @Override
            protected Object doInBackground() throws Exception {
                if (StringUtils.isNotEmpty(item.getThumbUrl())) {
                    image = ImageIO.read(new URL(item.getThumbUrl()));
                }else{
                    image = DownloadTools.downloadImgByMsgID(item.getMsgId(),WXMsgUrl.SLAVE_TYPE);
                }
                return null;
            }

            @Override
            protected void done() {
                if (image!=null){
                    linkViewHolder.icon.setIcon(new ImageIcon(ImageUtil.preferredImageSize(image, MessageLinkOfAppViewHolder.THUMB_WIDTH)));
                    //有图片时缩短宽度，让其与无图的Panel尽量一致
                    linkViewHolder.desc.setColumns(16);
                }
            }
        }.execute();


        //点击打开链接
        MessageMouseListener messageMouseListener = new MessageMouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (StringUtils.isNotEmpty(item.getUrl())) {
                        try {
                            Desktop.getDesktop().browse(new URI(item.getUrl()));
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
        long parse = item.getTimestamp();
        // 如果当前消息的时间与上条消息时间相差大于1分钟，则显示当前消息的时间
        if (preItem != null) {
            if (TimeUtil.inTheSameMinute(parse
                    , preItem.getTimestamp())) {
                holder.time.setVisible(false);
            } else {
                holder.time.setVisible(true);
                holder.time.setText(TimeUtil.diff(parse, true));
            }
        } else {
            holder.time.setVisible(true);
            holder.time.setText(TimeUtil.diff(parse, true));
        }

        String senderId = item.isGroup()&&! item.getFromUsername().equals(Core.getUserName())? item.getFromMemberOfGroupUsername()
                : item.getFromUsername();

        String roomId = item.getFromUsername();
        if (roomId.equals(Core.getUserName())) {
            roomId = item.getToUsername();
        }

        if (holder.avatar != null) {
            ImageIcon icon = null;
            if (AvatarUtil.avatarExists(senderId)){
                //已存在图片缓存
                if (roomId.startsWith("@@")){
                    icon = AvatarUtil.createOrLoadMemberAvatar(roomId,senderId);

                }else {
                    icon = AvatarUtil.createOrLoadUserAvatar(senderId);
                }
                holder.avatar.setIcon(icon);
            }else {
                //异步从网络加载
                String finalRoomId = roomId;
                new SwingWorker<Object,Object>(){
                    ImageIcon icon = null;
                    @Override
                    protected Object doInBackground() throws Exception {
                        if (finalRoomId.startsWith("@@")){
                            icon = AvatarUtil.createOrLoadMemberAvatar(finalRoomId,senderId);

                        }else {
                            icon = AvatarUtil.createOrLoadUserAvatar(senderId);
                        }
                        return null;
                    }
                    @Override
                    protected void done() {
                        holder.avatar.setIcon(icon);
                    }
                }.execute();
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
                    RoomChatContainer.get(RoomChatContainer.getCurrRoomId())
                            .getTipPanel().setText("成员信息加载中...");
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

    private void attachPopupMenu(ViewHolder viewHolder, Message item) {
        JComponent contentComponent = null;
        RCMessageBubble messageBubble = null;
        WXReceiveMsgCodeEnum typeEnum = WXReceiveMsgCodeEnum.getByCode(item.getMsgType());
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
                switch (WXReceiveMsgCodeOfAppEnum.getByCode(item.getAppMsgType())){

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
