package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.beans.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.beans.pojo.Message;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCMenuItemUI;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.ClipboardUtil;
import cn.shu.wechat.swing.utils.FileCache;
import cn.shu.wechat.swing.utils.ImageCache;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by 舒新胜 on 2017/6/5.
 */
public class MessagePopupMenu extends JPopupMenu {
    private WXReceiveMsgCodeEnum messageType;
    private final ImageCache imageCache = new ImageCache();
    private final FileCache fileCache = new FileCache();

    public MessagePopupMenu() {
        initMenuItem();
    }

    private void initMenuItem() {
        JMenuItem copy = new JMenuItem("复制");
        JMenuItem delItem = new JMenuItem("删除");
        JMenuItem forwardItem = new JMenuItem("转发");
        JMenuItem revokeItem = new JMenuItem("撤回");
        JMenuItem showPathItem = new JMenuItem("文件夹");
        copy.setUI(new RCMenuItemUI());
        copy.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch (messageType) {
                    case MSGTYPE_TEXT: {
                        SizeAutoAdjustTextArea textArea = (SizeAutoAdjustTextArea) getInvoker();
                        String text = textArea.getSelectedText() == null ? textArea.getText() : textArea.getSelectedText();
                        if (text != null) {
                            ClipboardUtil.copyString(text);
                        }
                        break;
                    }
                    case MSGTYPE_IMAGE:
                    case MSGTYPE_EMOTICON: {
                        MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                        Object obj = imageLabel.getTag();
                        if (obj != null) {
                            Map map = (Map) obj;
                            String id = (String) map.get("attachmentId");
                            String url = (String) map.get("url");
                            imageCache.requestOriginalAsynchronously(id, url, new ImageCache.ImageCacheRequestListener() {
                                @Override
                                public void onSuccess(ImageIcon icon, String path) {
                                    if (path != null && !path.isEmpty()) {
                                        ClipboardUtil.copyImage(path);
                                    } else {
                                        System.out.println("图片不存在，复制失败");
                                    }
                                }

                                @Override
                                public void onFailed(String why) {
                                    System.out.println("图片不存在，复制失败");
                                }
                            });
                        }
                        break;
                    }
                    case MSGTYPE_VIDEO: {
                        TagJLayeredPane attachmentPanel = (TagJLayeredPane) getInvoker();
                        Object obj = attachmentPanel.getTag();
                        if (obj != null) {
                            Message item = (Message)obj;
                            String videoPath = item.getFilePath();
                            if (videoPath != null && !videoPath.isEmpty()) {
                                ClipboardUtil.copyFile(videoPath);
                            }else{
                                JOptionPane.showMessageDialog(MainFrame.getContext(), "文件不存在", "文件不存在", JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                        }
                        break;
                    }
                    case MSGTYPE_VOICE:{
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        Object obj = attachmentPanel.getTag();
                        if (obj != null) {
                            Message item = (Message)obj;
                            String voicePath = item.getFilePath();
                            if (voicePath != null && !voicePath.isEmpty()) {
                                ClipboardUtil.copyFile(voicePath);
                            }else{
                                JOptionPane.showMessageDialog(MainFrame.getContext(), "文件不存在", "文件不存在", JOptionPane.WARNING_MESSAGE);
                                return;
                            }

                        }
                        break;
                    }
                    case MSGTYPE_APP: {
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        Object obj = attachmentPanel.getTag();
                        if (obj != null) {
                            Map map = (Map) obj;
                            String id = (String) map.get("attachmentId");
                            String name = (String) map.get("name");

                            String path = fileCache.tryGetFileCache(id, name);
                            if (path != null && !path.isEmpty()) {
                                ClipboardUtil.copyFile(path);
                            } else {
                                Object filepath = map.get("filepath");

                                if (filepath == null) {
                                    JOptionPane.showMessageDialog(MainFrame.getContext(), "文件不存在", "文件不存在", JOptionPane.WARNING_MESSAGE);
                                    return;
                                }

                                String link = filepath.toString();
                                if (link.startsWith("/file-upload")) {
                                    JOptionPane.showMessageDialog(MainFrame.getContext(), "请先下载文件", "请先下载文件", JOptionPane.WARNING_MESSAGE);
                                    return;
                                } else {
                                    File file = new File(link);
                                    if (!file.exists()) {
                                        JOptionPane.showMessageDialog(MainFrame.getContext(), "文件不存在，可能已被删除", "文件不存在", JOptionPane.WARNING_MESSAGE);
                                        return;
                                    }
                                    ClipboardUtil.copyFile(link);
                                }
                            }
                        }
                        break;

                    }
                    default:
                        break;
                }

            }
        });


        delItem.setUI(new RCMenuItemUI());
        delItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageId = null;
                switch (messageType) {
                    case MSGTYPE_TEXT: {
                        SizeAutoAdjustTextArea textArea = (SizeAutoAdjustTextArea) getInvoker();
                        messageId = textArea.getTag().toString();
                        break;
                    }
                    case MSGTYPE_IMAGE: {
                        MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                        Object obj = imageLabel.getTag();
                        if (obj != null) {
                            Map map = (Map) obj;
                            messageId = (String) map.get("messageId");
                        }
                        break;
                    }
                    case MSGTYPE_APP:{
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        Object obj = attachmentPanel.getTag();
                        if (obj != null) {
                            Map map = (Map) obj;
                            messageId = (String) map.get("messageId");
                        }
                        break;
                    }
                    default:
                }

                if (messageId != null && !messageId.isEmpty()) {
                    //ChatPanel.getContext().deleteMessage(messageId);
                }
            }
        });

        forwardItem.setUI(new RCMenuItemUI());
        forwardItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("转发");
            }
        });
        revokeItem.setUI(new RCMenuItemUI());
        revokeItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String messageId = null;
                switch (messageType) {
                    case MSGTYPE_TEXT:{
                        SizeAutoAdjustTextArea textArea = (SizeAutoAdjustTextArea) getInvoker();
                        messageId = textArea.getTag().toString();
                        break;
                    }
                    case MSGTYPE_IMAGE:{
                        MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                        Object obj = imageLabel.getTag();
                        if (obj != null) {
                            Map map = (Map) obj;
                            messageId = (String) map.get("messageId");
                        }
                        break;
                    }
                    case MSGTYPE_APP:{
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        Object obj = attachmentPanel.getTag();
                        if (obj != null) {
                            Map map = (Map) obj;
                            messageId = (String) map.get("messageId");
                        }
                        break;
                    }
                    case MSGTYPE_VIDEO:{
                        TagJLayeredPane attachmentPanel = (TagJLayeredPane) getInvoker();
                        Object obj = attachmentPanel.getTag();
                        if (obj != null) {
                            Message item = (Message)obj;
                            messageId = item.getId();
                        }
                        break;
                    }
                    case MSGTYPE_VOICE:{
                    TagPanel attachmentPanel = (TagPanel) getInvoker();
                    Object obj = attachmentPanel.getTag();
                    if (obj != null) {
                        Message item = (Message)obj;
                        messageId = item.getId();
                    }
                    break;
                }
                    default:
                }
                final String id = messageId;
                if (!StringUtils.isEmpty(messageId)) {
                    ExecutorServiceUtil.getGlobalExecutorService().execute(() -> {
                        MessageMapper bean = SpringContextHolder.getBean(MessageMapper.class);
                        Message message = bean.selectByPrimaryKey(id);
                        String response = message.getResponse();
                        WebWXSendMsgResponse webWXSendMsgResponse = JSON.parseObject(response, WebWXSendMsgResponse.class);
                        if (webWXSendMsgResponse != null && webWXSendMsgResponse.getBaseResponse().getRet() == 0){
                            boolean b = MessageTools.sendRevokeMsgByUserId(message.getToUsername(), webWXSendMsgResponse.getLocalID(), webWXSendMsgResponse.getMsgID());
                        }
                    });

                }
            }
        });
        showPathItem.setUI(new RCMenuItemUI());
        showPathItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                        String path = null;
                        switch (messageType) {
                            case MSGTYPE_IMAGE: {
                                MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                                Object obj = imageLabel.getTag();
                                if (obj != null) {
                                    Map map = (Map) obj;
                                    path = (String) map.get("url");
                                }
                                break;
                            }
                            case MSGTYPE_VIDEO: {
                                TagJLayeredPane attachmentPanel = (TagJLayeredPane) getInvoker();
                                Object obj = attachmentPanel.getTag();
                                if (obj != null) {
                                    Message item = (Message) obj;
                                    path = item.getFilePath();
                                }
                                break;
                            }
                            case MSGTYPE_VOICE: {
                                TagPanel attachmentPanel = (TagPanel) getInvoker();
                                Object obj = attachmentPanel.getTag();
                                if (obj != null) {
                                    Message item = (Message) obj;
                                    path = item.getFilePath();
                                }
                                break;
                            }
                            case MSGTYPE_APP: {
                                TagPanel attachmentPanel = (TagPanel) getInvoker();
                                Object obj = attachmentPanel.getTag();
                                if (obj != null) {
                                    Map map = (Map) obj;
                                    String id = (String) map.get("attachmentId");
                                    String name = (String) map.get("name");
                                    path = fileCache.tryGetFileCache(id, name);
                                    if (path != null && !path.isEmpty()) {
                                        ClipboardUtil.copyFile(path);
                                    } else {
                                        path = map.get("filepath").toString();
                                    }
                                }
                                break;
                            }
                            default:
                                break;
                        }
                        if (StringUtils.isNotEmpty(path)) {
                            String finalPath = path;
                            ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
                                @Override
                                public void run() {

                            File file = new File(finalPath);
                            if (file.exists()) {
                                try {
                                    Desktop.getDesktop().open(file.getParentFile());
                                } catch (IOException ioException) {
                                    ioException.printStackTrace();
                                }
                            }
                           }});
                        }


            }
        });
        this.add(copy);
        this.add(delItem);
        this.add(revokeItem);
        this.add(forwardItem);
        this.add(showPathItem);
        setBorder(new LineBorder(Colors.SCROLL_BAR_TRACK_LIGHT));
        setBackground(Colors.FONT_WHITE);
    }

    @Override
    public void show(Component invoker, int x, int y) {
        throw new RuntimeException("此方法不会弹出菜单，请调用 show(Component invoker, int x, int y, int messageType) ");
        //super.show(invoker, x, y);
    }

    public void show(Component invoker, int x, int y, int messageType) {
        this.messageType = WXReceiveMsgCodeEnum.getByCode(messageType);
        super.show(invoker, x, y);
    }
}
