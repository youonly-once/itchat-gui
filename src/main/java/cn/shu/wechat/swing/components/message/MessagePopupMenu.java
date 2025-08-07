package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.dto.response.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCMenuItemUI;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.frames.ForwardMsgDialog;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.utils.*;
import com.alibaba.fastjson.JSON;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.ThreadUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.locks.LockSupport;

/**
 * Created by 舒新胜 on 2017/6/5.
 */
@Log4j2
public class MessagePopupMenu extends JPopupMenu {
    private WxRespConstant.WXReceiveMsgCodeEnum messageType;
    private final JMenuItem showPathItem = new JMenuItem("文件夹");
    private final JMenuItem revokeItem = new JMenuItem("撤回");
    private WeakReference<Component> lastInvokerRef = new WeakReference<>(null);
    public MessagePopupMenu() {
        initMenuItem();
    }

    private void initMenuItem() {
        JMenuItem copy = new JMenuItem("复制");
        JMenuItem delItem = new JMenuItem("删除");
        JMenuItem pause = new JMenuItem("启动/暂停");
        JMenuItem forwardItem = new JMenuItem("转发");

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
                    case MSGTYPE_VIDEO: {
                        TagJLayeredPane videoPanel = (TagJLayeredPane) getInvoker();
                        Object obj = videoPanel.getTag();
                        break;
                    }

                    case MSGTYPE_IMAGE:
                    case MSGTYPE_EMOTICON:
                    case MSGTYPE_VOICE:
                    case MSGTYPE_APP: {
                        MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                        Object obj = imageLabel.getTag();
                        if (obj != null) {
                            ExecutorServiceUtil.getGlobalExecutorService().submit(new Runnable() {
                                @Override
                                public void run() {
                                    Message msg = (Message) obj;
                                    String filePath = msg.getFilePath();
                                    ClipboardUtil.copyFile(filePath);
                                }
                            });


                        }
                        break;
                    }
                    default:
                        break;
                }

            }
        });

        pause.setUI(new RCMenuItemUI());
        pause.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object obj = null;
                switch (messageType) {
                    case MSGTYPE_APP: {
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        obj = attachmentPanel.getTag();
                        break;
                    }
                    default:
                }
                Message item = (Message) obj;
                if (item != null) {
                    if (MessageTools.getMapPasue().containsKey(item.getFilePath())){
                        if (MessageTools.getMapPasue().get(item.getFilePath())){
                            MessageTools.getMapPasue().put(item.getFilePath(), false);
                            Thread threadById = ThreadUtils.findThreadById(item.getThreadId());
                            if (threadById != null) {
                                LockSupport.unpark(threadById);
                            }
                            //pause.setText("暂停");
                        }else{
                            MessageTools.getMapPasue().put(item.getFilePath(), true);
                            //pause.setText("启动");
                        }
                    }else{
                        MessageTools.getMapPasue().put(item.getFilePath(), true);
                        // pause.setText("启动");
                    }
                }
            }
        });
        delItem.setUI(new RCMenuItemUI());
        delItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object obj = null;
                switch (messageType) {
                    case MSGTYPE_TEXT: {
                        SizeAutoAdjustTextArea textArea = (SizeAutoAdjustTextArea) getInvoker();
                        obj = textArea.getTag();
                        break;
                    }
                    case MSGTYPE_VIDEO: {
                        TagJLayeredPane videoPanel = (TagJLayeredPane) getInvoker();
                        obj = videoPanel.getTag();
                        break;
                    }
                    case MSGTYPE_EMOTICON:
                    case MSGTYPE_IMAGE: {
                        MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                        obj = imageLabel.getTag();
                        break;
                    }
                    case MSGTYPE_APP: {
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        obj = attachmentPanel.getTag();
                        break;
                    }
                    default:
                }
                Message item = (Message) obj;
                ChatUtil.deleteMessage(item);
            }
        });

        forwardItem.setUI(new RCMenuItemUI());
        forwardItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object obj = null;
                switch (messageType) {
                    case MSGTYPE_TEXT: {
                        SizeAutoAdjustTextArea textArea = (SizeAutoAdjustTextArea) getInvoker();
                        obj = textArea.getTag();
                        break;
                    }
                    case MSGTYPE_VIDEO: {
                        TagJLayeredPane videoPanel = (TagJLayeredPane) getInvoker();
                        obj = videoPanel.getTag();
                        break;
                    }

                    case MSGTYPE_EMOTICON:
                    case MSGTYPE_VOICE:
                    case MSGTYPE_IMAGE: {
                        MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                        obj = imageLabel.getTag();
                        break;
                    }
                    case MSGTYPE_APP: {
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        obj = attachmentPanel.getTag();
                        break;
                    }
                    default:
                }
                if (obj == null) return;
                Message item = (Message) obj;
                ForwardMsgDialog dialog = new ForwardMsgDialog(MainFrame.getContext(), true, item);
                dialog.setVisible(true);
            }
        });
        revokeItem.setUI(new RCMenuItemUI());
        revokeItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object obj = null;
                switch (messageType) {
                    case MSGTYPE_TEXT: {
                        SizeAutoAdjustTextArea textArea = (SizeAutoAdjustTextArea) getInvoker();
                        obj = textArea.getTag();
                        break;
                    }
                    case MSGTYPE_EMOTICON:
                    case MSGTYPE_IMAGE: {
                        MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                        obj = imageLabel.getTag();
                        break;
                    }
                    case MSGTYPE_APP:
                    case MSGTYPE_VOICE: {
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        obj = attachmentPanel.getTag();
                        break;
                    }
                    case MSGTYPE_VIDEO: {
                        TagJLayeredPane attachmentPanel = (TagJLayeredPane) getInvoker();
                        obj = attachmentPanel.getTag();
                        break;
                    }
                    default:
                }
                if (obj == null) {
                    return;

                }
                Message item = (Message) obj;
                final String messageId = item.getId();
                if (!StringUtils.isEmpty(messageId)) {
                    ExecutorServiceUtil.getGlobalExecutorService().execute(() -> {
                        MessageMapper bean = SpringContextHolder.getBean(MessageMapper.class);
                        Message message = bean.selectByPrimaryKey(messageId);
                        String response = message.getResponse();
                        WebWXSendMsgResponse wxSendMsgResponse = JSON.parseObject(response, WebWXSendMsgResponse.class);
                        if (wxSendMsgResponse != null && wxSendMsgResponse.getBaseResponse().getRet() == 0) {
                            try {
                                boolean b = MessageTools.sendRevokeMsgByUserId(message.getToUsername(), wxSendMsgResponse.getLocalID(), wxSendMsgResponse.getMsgID());
                            } catch (IOException | InterruptedException ex) {
                                log.error(ex.getMessage());
                            }
                        }
                    });

                }
            }
        });
        showPathItem.setUI(new RCMenuItemUI());
        showPathItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {

                Object obj = null;
                switch (messageType) {
                    case MSGTYPE_EMOTICON:
                    case MSGTYPE_IMAGE: {
                        MessageImageLabel imageLabel = (MessageImageLabel) getInvoker();
                        obj = imageLabel.getTag();
                        break;
                    }
                    case MSGTYPE_VIDEO:
                    case MSGTYPE_VOICE:
                    case MSGTYPE_APP: {
                        TagPanel attachmentPanel = (TagPanel) getInvoker();
                        obj = attachmentPanel.getTag();
                        break;
                    }
                    default:
                        break;
                }
                if (obj == null) {
                    return;
                }
                Message item = (Message) obj;
                if (StringUtils.isNotEmpty(item.getFilePath())) {
                    ExecutorServiceUtil.getGlobalExecutorService().submit(() -> FileUtil.showAtExplorer(item.getFilePath()));
                }


            }
        });

        this.add(copy);
        this.add(pause);
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
        this.messageType = WxRespConstant.WXReceiveMsgCodeEnum.getByCode(messageType);
        switch (this.messageType) {
            case MSGTYPE_TEXT:
                remove(showPathItem);
                break;
            default:
                add(showPathItem);

        }
        if (messageType<0){
            remove(revokeItem);
        }else{
            add(revokeItem);
        }
        lastInvokerRef = new WeakReference<>(invoker);
        super.show(invoker, x, y);
    }


    @Override
    public void setVisible(boolean b) {
        if (!b) {
            setInvoker(null);  // 手动解除引用
        }
        super.setVisible(b);
    }

    @Override
    public Component getInvoker() {
        Component invoker = super.getInvoker();
        if (invoker == null) {
            return lastInvokerRef.get();
        }
        return invoker;
    }
}
