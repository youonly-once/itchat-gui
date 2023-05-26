package cn.shu.wechat.swing.components.message;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.mapper.MessageMapper;
import cn.shu.wechat.dto.response.msg.send.WebWXSendMsgResponse;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.RCMenuItemUI;
import cn.shu.wechat.swing.components.SizeAutoAdjustTextArea;
import cn.shu.wechat.swing.utils.ChatUtil;
import cn.shu.wechat.swing.utils.ClipboardUtil;
import cn.shu.wechat.swing.utils.FileUtil;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by 舒新胜 on 2017/6/5.
 */
public class MessagePopupMenu extends JPopupMenu {
    private WxRespConstant.WXReceiveMsgCodeEnum messageType;
    private final JMenuItem showPathItem = new JMenuItem("文件夹");
    private final JMenuItem revokeItem = new JMenuItem("撤回");

    public MessagePopupMenu() {
        initMenuItem();
    }

    private void initMenuItem() {
        JMenuItem copy = new JMenuItem("复制");
        JMenuItem delItem = new JMenuItem("删除");
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
                    case MSGTYPE_IMAGE:
                    case MSGTYPE_EMOTICON:
                    case MSGTYPE_VIDEO:
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
                System.out.println("转发");
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
                            boolean b = MessageTools.sendRevokeMsgByUserId(message.getToUsername(), wxSendMsgResponse.getLocalID(), wxSendMsgResponse.getMsgID());
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
                    case MSGTYPE_VIDEO: {
                        TagJLayeredPane attachmentPanel = (TagJLayeredPane) getInvoker();
                        obj = attachmentPanel.getTag();
                        break;
                    }
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
        super.show(invoker, x, y);
    }
}
