package cn.shu.wechat.swing.frames;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import cn.shu.wechat.pojo.entity.Message;
import cn.shu.wechat.swing.panels.RoomChatContainer;
import cn.shu.wechat.utils.DateUtils;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.SleepUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutorService;

public class BombFrame extends JFrame {

    private final String roomId;
    /**
     * 使窗口在屏幕中央显示
     */
    private void centerScreen() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        this.setLocation((tk.getScreenSize().width - 400) / 2,
                (tk.getScreenSize().height - 300) / 2);
    }
    public BombFrame(String roomId) {
        super();
        this.roomId = roomId;
        JLabel contentLabel = new JLabel("发送内容：");
        JLabel countLabel = new JLabel("发送数量：");
        JLabel intervalLabel = new JLabel("发送间隔（毫秒）：");
        JTextField count = new JTextField("50");
        JTextField content = new JTextField("[Bomb]");
        JTextField interval = new JTextField("1000");
        JPanel inputPanel = new JPanel(new GridLayout(3, 2));
        inputPanel.add(contentLabel);
        inputPanel.add(content);
        inputPanel.add(countLabel);
        inputPanel.add(count);
        inputPanel.add(intervalLabel);
        inputPanel.add(interval);
        this.setSize(400, 300);
        JButton send = new JButton("发送");
        send.setBorder(new EmptyBorder(10,5,10,5));
        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(inputPanel,BorderLayout.CENTER);
        this.getContentPane().add(send,BorderLayout.SOUTH);
        send.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtils.isEmpty(content.getText())) {
                    JOptionPane.showMessageDialog(BombFrame.this, "消息不能为空");
                    return;
                }
                try {
                    ExecutorServiceUtil.getGlobalExecutorService().execute(new Runnable() {
                        @Override
                        public void run() {
                            BombFrame.this.setVisible(false);
                            Message message = Message.builder().isSend(false)
                                    .content(content.getText())
                                    .plaintext(content.getText())
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
                            for (int i = 0; i < Integer.parseInt(count.getText()); i++) {
                                SleepUtils.sleep(Long.parseLong(interval.getText()));
                                message.setId(MessageTools.randomMessageId());
                                MessageTools.sendMsgByUserId(message);
                                /*new SwingWorker<Object,Object>(){

                                    @Override
                                    protected Object doInBackground() throws Exception {
                                        SleepUtils.sleep(Long.parseLong(interval.getText()));
                                        return null;
                                    }

                                    @Override
                                    protected void done() {
                                        RoomChatContainer.get(roomId).getChatPanel().sendTextMessage(content.getText());
                                    }
                                }.execute();*/

                            }
                        }
                    });

                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(BombFrame.this, exception.getMessage());
                }
            }
        });
    }


}
