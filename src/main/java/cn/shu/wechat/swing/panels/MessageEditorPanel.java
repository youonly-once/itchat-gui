package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.mapper.StatusMapper;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.pojo.entity.Status;
import cn.shu.wechat.service.impl.IMsgHandlerFaceImpl;
import cn.shu.wechat.swing.components.*;
import cn.shu.wechat.swing.components.message.ChatEditorPopupMenu;
import cn.shu.wechat.swing.frames.BombFrame;
import cn.shu.wechat.swing.frames.ImageViewerFrame;
import cn.shu.wechat.swing.frames.ScreenShotFrame;
import cn.shu.wechat.swing.listener.ExpressionListener;
import cn.shu.wechat.swing.utils.ClipboardUtil;
import cn.shu.wechat.swing.utils.FontUtil;
import cn.shu.wechat.swing.utils.IconUtil;
import cn.shu.wechat.swing.utils.OSUtil;
import cn.shu.wechat.utils.ChartUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class MessageEditorPanel extends ParentAvailablePanel {
    private JPanel controlLabel;
    private JLabel fileLabel;
    private JLabel expressionLabel;
    private JLabel cutLabel;
    private JLabel preventUndoLabel;
    private JLabel autoReplyLabel;
    private JLabel bombMsgLabel;
    private JLabel chartMsgLabel;
    private JScrollPane textScrollPane;
    private RCTextEditor textEditor;
    private JPanel sendPanel;
    private RCButton sendButton;
    private ChatEditorPopupMenu chatEditorPopupMenu;

    private ImageIcon fileNormalIcon;
    private ImageIcon fileActiveIcon;

    private ImageIcon emotionNormalIcon;
    private ImageIcon emotionActiveIcon;

    private ImageIcon cutNormalIcon;
    private ImageIcon cutActiveIcon;

    private ImageIcon preventUndoNormalIcon;
    private ImageIcon preventUndoActiveIcon;

    private ImageIcon autoReplyNormalIcon;
    private ImageIcon autoReplyActiveIcon;

    private ImageIcon bombMsgNormalIcon;
    private ImageIcon bombMsgActiveIcon;

    private ImageIcon chartNormalIcon;
    private ImageIcon chartActiveIcon;

    private ExpressionPopup expressionPopup;
    private final String roomId;

    public MessageEditorPanel(JPanel parent, String roomId) {
        super(parent);
        this.roomId = roomId;
        initComponents();
        initView();
        setListeners();

        if (OSUtil.getOsType() == OSUtil.Windows) {
            registerHotKey();
        }
    }

    private void registerHotKey() {
        int SCREEN_SHOT_CODE = 10001;
        JIntellitype.getInstance().registerHotKey(SCREEN_SHOT_CODE, JIntellitype.MOD_ALT, 'S');

        JIntellitype.getInstance().addHotKeyListener(new HotkeyListener() {
            @Override
            public void onHotKey(int markCode) {
                if (markCode == SCREEN_SHOT_CODE) {
                    screenShot();
                }
            }
        });
    }

    private void initComponents() {
        Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
        controlLabel = new JPanel();
        controlLabel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 7));

        fileLabel = new JLabel();
        fileNormalIcon = IconUtil.getIcon(this, "/image/file.png");
        fileActiveIcon = IconUtil.getIcon(this, "/image/file_active.png");
        fileLabel.setIcon(fileNormalIcon);
        fileLabel.setCursor(handCursor);
        fileLabel.setToolTipText("发送文件/图片");

        expressionLabel = new JLabel();
        emotionNormalIcon = IconUtil.getIcon(this, "/image/emotion.png");
        emotionActiveIcon = IconUtil.getIcon(this, "/image/emotion_active.png");
        expressionLabel.setIcon(emotionNormalIcon);
        expressionLabel.setCursor(handCursor);
        expressionLabel.setToolTipText("表情");

        cutLabel = new JLabel();
        cutNormalIcon = IconUtil.getIcon(this, "/image/cut.png");
        cutActiveIcon = IconUtil.getIcon(this, "/image/cut_active.png");
        cutLabel.setIcon(cutNormalIcon);
        cutLabel.setCursor(handCursor);
        if (OSUtil.getOsType() == OSUtil.Windows) {
            cutLabel.setToolTipText("截图(Alt + S)");
        } else {
            cutLabel.setToolTipText("截图(当前系统下不支持全局热键)");
        }


        preventUndoLabel = new JLabel();
        preventUndoNormalIcon = IconUtil.getIcon(this, "/image/prevent.png");
        preventUndoActiveIcon = IconUtil.getIcon(this, "/image/prevent_active.png");
        preventUndoLabel.setIcon(preventUndoNormalIcon);

        autoReplyLabel = new JLabel();
        autoReplyNormalIcon = IconUtil.getIcon(this, "/image/robot.png");
        autoReplyActiveIcon = IconUtil.getIcon(this, "/image/robot_active.png");
        autoReplyLabel.setIcon(autoReplyNormalIcon);
        setUndoAndAutoLabel();

        bombMsgLabel = new JLabel();
        bombMsgNormalIcon = IconUtil.getIcon(this, "/image/bomb.png");
        bombMsgActiveIcon = IconUtil.getIcon(this, "/image/bomb_active.png");
        bombMsgLabel.setIcon(bombMsgNormalIcon);


        chartMsgLabel = new JLabel();
        chartNormalIcon = IconUtil.getIcon(this, "/image/chart.png");
        chartActiveIcon = IconUtil.getIcon(this, "/image/chart_active.png");
        chartMsgLabel.setIcon(chartNormalIcon);

        textEditor = new RCTextEditor();
        new DropTarget(textEditor, textEditor);
        textEditor.setDragEnabled(true);
        textEditor.setBackground(Colors.WINDOW_BACKGROUND);
        textEditor.setFont(FontUtil.getDefaultFont(14));
        textEditor.setMargin(new Insets(0, 15, 0, 0));
        textScrollPane = new JScrollPane(textEditor);
        textScrollPane.getVerticalScrollBar().setUI(new ScrollUI(Colors.SCROLL_BAR_THUMB, Colors.WINDOW_BACKGROUND));
        textScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        textScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        textScrollPane.setBorder(null);

        sendPanel = new JPanel();
        sendPanel.setLayout(new BorderLayout());

        sendButton = new RCButton("发 送");
        sendPanel.add(sendButton, BorderLayout.EAST);
        sendButton.setForeground(Colors.DARKER);
        sendButton.setFont(FontUtil.getDefaultFont(13));
        sendButton.setPreferredSize(new Dimension(75, 23));
        sendButton.setToolTipText("Enter发送消息，Ctrl+Enter换行");

        chatEditorPopupMenu = new ChatEditorPopupMenu();

        expressionPopup = new ExpressionPopup();
    }

    private void initView() {
        this.setLayout(new GridBagLayout());

        controlLabel.add(expressionLabel);
        controlLabel.add(preventUndoLabel);

        controlLabel.add(fileLabel);
        controlLabel.add(cutLabel);
        controlLabel.add(bombMsgLabel);
        controlLabel.add(autoReplyLabel);
        controlLabel.add(chartMsgLabel);


        add(controlLabel, new GBC(0, 0).setFill(GBC.HORIZONTAL).setWeight(1, 1));
        add(textScrollPane, new GBC(0, 1).setFill(GBC.BOTH).setWeight(1, 15));
        add(sendPanel, new GBC(0, 2).setFill(GBC.BOTH).setWeight(1, 1).setInsets(0, 0, 10, 10));
    }

    private void setListeners() {
        fileLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                fileLabel.setIcon(fileActiveIcon);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                fileLabel.setIcon(fileNormalIcon);
                super.mouseExited(e);
            }
        });

        expressionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                expressionLabel.setIcon(emotionActiveIcon);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                expressionLabel.setIcon(emotionNormalIcon);
                super.mouseExited(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                expressionPopup.show((Component) e.getSource(), e.getX() - 200, e.getY() - 320);
                super.mouseClicked(e);
            }
        });

        cutLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cutLabel.setIcon(cutActiveIcon);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                cutLabel.setIcon(cutNormalIcon);
                super.mouseExited(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                screenShot();
                super.mouseClicked(e);
            }
        });

        textEditor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    chatEditorPopupMenu.show((Component) e.getSource(), e.getX(), e.getY());
                }
                super.mouseReleased(e);
            }
        });

        autoReplyLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeAutoStatus();

                super.mouseClicked(e);
            }
        });

        JPopupMenu jPopupMenu = createChartPopup();
        chartMsgLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                chartMsgLabel.setIcon(chartActiveIcon);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chartMsgLabel.setIcon(chartNormalIcon);
                super.mouseExited(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                jPopupMenu.show((Component) e.getSource(), e.getX() - jPopupMenu.getWidth(), e.getY() - jPopupMenu.getHeight());
                super.mouseClicked(e);
            }
        });
        final BombFrame[] bombFrame = {null};

        bombMsgLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                bombMsgLabel.setIcon(bombMsgActiveIcon);
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                bombMsgLabel.setIcon(bombMsgNormalIcon);
                super.mouseExited(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (bombFrame[0] == null) {
                    bombFrame[0] = new BombFrame(roomId);
                    bombFrame[0].setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                } else {
                    bombFrame[0].setVisible(true);
                }

                super.mouseClicked(e);
            }
        });

        preventUndoLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeUndoStatus();
                super.mouseClicked(e);
            }
        });
    }

    /**
     * 创建图表菜单
     *
     * @return 菜单
     */
    private JPopupMenu createChartPopup() {
        JPopupMenu jPopupMenu = new JPopupMenu();
        jPopupMenu.setLayout(new GridLayout(12, 3));
        for (Field declaredField : Contacts.class.getDeclaredFields()) {
            declaredField.setAccessible(true);
            JMenuItem jMenuItem = new JMenuItem(declaredField.getName() + "图表");
            jMenuItem.addActionListener(e -> createAndShowChart(declaredField.getName().toLowerCase()));
            jPopupMenu.add(jMenuItem);
        }

        return jPopupMenu;
    }

    /**
     * 创建属性分布图并展示
     *
     * @param attr 属性
     */
    private void createAndShowChart(String attr) {
        String remarkNameByGroupUserName = ContactsTools.getContactDisplayNameByUserName(roomId);
        ChartUtil chartUtil = SpringContextHolder.getBean(ChartUtil.class);
        String path = chartUtil.makeGroupMemberAttrPieChart(roomId, remarkNameByGroupUserName, attr, java.awt.Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        if (path == null) {
            JOptionPane.showMessageDialog(this, "创建失败。");
            return;
        }
        BufferedImage read = null;
        try {
            read = ImageIO.read(new File(path));
            ImageViewerFrame instance = ImageViewerFrame.getInstance();
            instance.setImage(read);

            instance.toFront();
            instance.setVisible(true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void screenShot() {
        ScreenShotFrame ssw = new ScreenShotFrame();
        ssw.setRoomId(roomId);
        ssw.setVisible(true);
    }

    public void setExpressionListener(ExpressionListener listener) {
        expressionPopup.setExpressionListener(listener);
    }

    public RCTextEditor getEditor() {
        return textEditor;
    }

    public JButton getSendButton() {
        return sendButton;
    }

    public JLabel getUploadFileLabel() {
        return fileLabel;
    }

    /**
     * 修改联系人的自动回复状态
     */
    private void changeAutoStatus() {
        new SwingWorker<Object, Object>() {
            Short autoStatus = 0;
            int i = 0;

            @Override
            protected Object doInBackground() throws Exception {
                String to = ContactsTools.getContactDisplayNameByUserName(roomId);
                IMsgHandlerFaceImpl face = SpringContextHolder.getBean(IMsgHandlerFaceImpl.class);
                StatusMapper statusMapper = SpringContextHolder.getBean(StatusMapper.class);
                Status status = statusMapper.selectByPrimaryKey(to);
                if (status == null) {
                    //开启自动回复
                    status = new Status();
                    autoStatus = 1;
                } else {
                    autoStatus = status.getAutoStatus();
                    if (autoStatus == null) {
                        //开启自动回复
                        autoStatus = 1;
                    } else if (autoStatus == 1) {
                        //关闭自动回复
                        autoStatus = 2;

                    } else if (autoStatus == 2) {
                        //开启自动回复
                        autoStatus = 1;
                    }
                }
                if (autoStatus == 1) {
                    face.autoChatUserNameList.add(to);
                } else if (autoStatus == 2) {
                    face.autoChatUserNameList.remove(to);
                }
                status.setAutoStatus(autoStatus);
                status.setName(to);
                i = statusMapper.insertOrUpdateSelectiveForSqlite(status);
                return null;
            }

            @Override
            protected void done() {
                if (i == 0) return;
                if (autoStatus == 1) {
                    autoReplyLabel.setIcon(autoReplyActiveIcon);
                } else if (autoStatus == 2) {
                    autoReplyLabel.setIcon(autoReplyNormalIcon);
                }
            }
        }.execute();
    }

    /**
     * 修改联系人的防撤回状态
     */
    private void changeUndoStatus() {
        new SwingWorker<Object, Object>() {
            Short preventStatus = 0;
            int i = 0;

            @Override
            protected Object doInBackground() throws Exception {
                String to = ContactsTools.getContactDisplayNameByUserName(roomId);
                IMsgHandlerFaceImpl face = SpringContextHolder.getBean(IMsgHandlerFaceImpl.class);
                StatusMapper statusMapper = SpringContextHolder.getBean(StatusMapper.class);
                Status status = statusMapper.selectByPrimaryKey(to);
                if (status == null) {
                    //关闭防撤回
                    status = new Status();
                    preventStatus = 2;
                } else {
                    preventStatus = status.getUndoStatus();
                    if (preventStatus == null) {
                        //关闭防撤回
                        preventStatus = 2;
                    } else if (preventStatus == 2) {
                        //开启防撤回
                        preventStatus = 1;
                    } else if (preventStatus == 1) {
                        //关闭防撤回
                        preventStatus = 2;
                    }
                }
                if (preventStatus == 2) {
                    face.nonPreventUndoMsgUserName.add(to);
                } else if (preventStatus == 1) {
                    face.nonPreventUndoMsgUserName.remove(to);
                }
                status.setUndoStatus(preventStatus);
                status.setName(to);
                i = statusMapper.insertOrUpdateSelectiveForSqlite(status);
                return null;
            }

            @Override
            protected void done() {
                if (i == 0) return;
                if (preventStatus == 2) {
                    preventUndoLabel.setIcon(preventUndoNormalIcon);
                } else if (preventStatus == 1) {
                    preventUndoLabel.setIcon(preventUndoActiveIcon);
                }
            }
        }.execute();
    }


    /**
     * 获取当前用户的撤回状态、自动回复状态
     */
    public void setUndoAndAutoLabel() {
        new SwingWorker<Object, Object>() {
            Status status = null;

            @Override
            protected Object doInBackground() throws Exception {
                String to = ContactsTools.getContactDisplayNameByUserName(roomId);
                StatusMapper statusMapper = SpringContextHolder.getBean(StatusMapper.class);
                status = statusMapper.selectByPrimaryKey(to);

                return null;
            }

            @Override
            protected void done() {
                if (status == null || status.getUndoStatus() == null || status.getUndoStatus() == 1) {
                    preventUndoLabel.setIcon(preventUndoActiveIcon);
                } else {
                    preventUndoLabel.setIcon(preventUndoNormalIcon);
                }

                if (status == null || status.getAutoStatus() == null || status.getAutoStatus() == 2) {
                    autoReplyLabel.setIcon(autoReplyNormalIcon);
                } else {
                    autoReplyLabel.setIcon(autoReplyActiveIcon);
                }
            }
        }.execute();

    }

}
