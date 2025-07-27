package cn.shu.wechat.swing.frames;

import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCButton;
import cn.shu.wechat.swing.components.RCTextField;
import cn.shu.wechat.swing.entity.SelectUserData;
import cn.shu.wechat.swing.panels.SelectUserPanel;
import cn.shu.wechat.swing.utils.FontUtil;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;


/**
 * Created by 舒新胜 on 07/06/2017.
 */
public class RemindUserDialog extends JDialog {
    public static final int DIALOG_WIDTH = 580;
    public static final int DIALOG_HEIGHT = 500;
    @Getter
    private static RemindUserDialog context;
    //初始列表
    private final List<SelectUserData> userList = new ArrayList<>();
    //待搜索列表
    private final Collection<Contacts> searchList = new ArrayList<>();
    private JPanel editorPanel;
    private RCTextField groupNameTextField;
    private SelectUserPanel selectUserPanel;
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton okButton;


    public RemindUserDialog(Frame owner, boolean modal) {
        super(owner, modal);
        context = this;
        initComponents();
        initView();
    }


    public void addData(Collection<String> defaultList, Collection<Contacts> contactsList) {
        userList.clear();
        searchList.clear();
        for (String con : defaultList) {
            SelectUserData selectUserData = new SelectUserData(con,
                    con,
                    false);
            userList.add(selectUserData);
        }
        searchList.addAll(contactsList);
        selectUserPanel.update();
    }

    private void initComponents() {
        int posX = MainFrame.getContext().getX();
        int posY = MainFrame.getContext().getY();

        posX = posX + (MainFrame.getContext().currentWindowWidth - DIALOG_WIDTH) / 2;
        posY = posY + (MainFrame.getContext().currentWindowHeight - DIALOG_HEIGHT) / 2;
        setBounds(posX, posY, DIALOG_WIDTH, DIALOG_HEIGHT);
        setUndecorated(true);
        getRootPane().setBorder(new LineBorder(Colors.DIALOG_BORDER));

        /*if (OSUtil.getOsType() != OSUtil.Mac_OS)
        {
            // 边框阴影，但是会导致字体失真
            AWTUtilities.setWindowOpaque(this, false);
            //getRootPane().setOpaque(false);
            getRootPane().setBorder(ShadowBorder.newInstance());
        }*/

        // 输入面板
        editorPanel = new JPanel();
        groupNameTextField = new RCTextField();
        groupNameTextField.setPlaceholder("群聊名称");
        groupNameTextField.setPreferredSize(new Dimension(DIALOG_WIDTH / 2, 35));
        groupNameTextField.setFont(FontUtil.getDefaultFont(14));
        groupNameTextField.setForeground(Colors.FONT_BLACK);
        groupNameTextField.setMargin(new Insets(0, 15, 0, 0));


        // 按钮组
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        cancelButton = new RCButton("取消");
        cancelButton.setForeground(Colors.FONT_BLACK);

        okButton = new RCButton("@", Colors.MAIN_COLOR, Colors.MAIN_COLOR_DARKER, Colors.MAIN_COLOR_DARKER);
        okButton.setBackground(Colors.PROGRESS_BAR_START);

        selectUserPanel = new SelectUserPanel(DIALOG_WIDTH, DIALOG_HEIGHT - 100, userList, searchList);
    }

    private void initView() {
        editorPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        //  editorPanel.add(groupNameTextField);

        buttonPanel.add(cancelButton, new GBC(0, 0).setWeight(1, 1).setInsets(15, 0, 0, 0));
        buttonPanel.add(okButton, new GBC(1, 0).setWeight(1, 1));


        add(editorPanel, BorderLayout.NORTH);
        add(selectUserPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }


    public void setListeners(Consumer<List<SelectUserData>> consumer) {
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setVisible(false);

                super.mouseClicked(e);
            }
        });

        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (okButton.isEnabled()) {
                    okButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                    consumer.accept(selectUserPanel.getSelectedUser());

                    okButton.setEnabled(true);
                    okButton.setText("@");
                    cancelButton.setEnabled(true);
                    RemindUserDialog.context.dispose();
                }
                super.mouseClicked(e);
            }
        });
    }


}
