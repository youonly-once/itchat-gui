package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.adapter.ContactsItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCListView;
import cn.shu.wechat.swing.db.model.ContactsUser;
import cn.shu.wechat.swing.entity.ContactsItem;
import cn.shu.wechat.swing.utils.AvatarUtil;
import org.apache.commons.lang.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by song on 17-5-30.
 */
public class ContactsPanel extends ParentAvailablePanel {
    private static ContactsPanel context;

    private RCListView contactsListView;
    private final List<ContactsItem> contactsItemList = new ArrayList<>();

    private String currentUsername;

    public ContactsPanel(JPanel parent) {
        super(parent);
        context = this;

        initComponents();
        initView();
        initData();
        //绑定list到Adapter
        contactsListView.setAdapter(new ContactsItemsAdapter(contactsItemList));

        // TODO: 从服务器获取通讯录后，调用下面方法更新UI
        //notifyDataSetChanged();
    }


    private void initComponents() {
        contactsListView = new RCListView();
    }

    private void initView() {
        setLayout(new GridBagLayout());
        contactsListView.setContentPanelBackground(Colors.DARK);
        add(contactsListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
    }

    /**
     * 初始化数据，加载所有联系人到List中
     */
    private void initData() {
        contactsItemList.clear();

        //List<ContactsUser> contactsUsers = contactsUserService.findAll();
        List<ContactsUser> contactsUsers = new ArrayList<>();
        Map<String, Contacts> memberMap = Core.getMemberMap();
        for (String userName : memberMap.keySet()) {
            String head = Core.getContactHeadImgPath().get(userName);
            ContactsItem item = new ContactsItem(userName, ContactsTools.getContactDisplayNameByUserName(userName), head);
            contactsItemList.add(item);
        }

    }

    /**
     * 联系人数据刷新
     */
    public void notifyDataSetChanged() {
        initData();
        ((ContactsItemsAdapter) contactsListView.getAdapter()).processData();
        contactsListView.notifyDataSetChanged(false);

        // 通讯录更新后，获取头像
        getContactsUserAvatar();
    }

    public static ContactsPanel getContext() {
        return context;
    }

    /**
     * 获取通讯录中用户的头像
     */
    private void getContactsUserAvatar() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (ContactsItem user : contactsItemList) {
                    if (!AvatarUtil.customAvatarExist(user.getDisplayName())) {
                        final String username = user.getDisplayName();
                        //logger.debug("获取头像:" + username);
                        getUserAvatar(username, true);
                    }
                }

                // 自己的头像每次启动都去获取
                //  currentUsername = currentUserService.findAll().get(0).getUsername();
                getUserAvatar(currentUsername, true);
            }
        }).start();

    }

    /**
     * 更新指定用户头像
     *
     * @param username   用户名
     * @param hotRefresh 是否热更新，hotRefresh = true， 将刷新该用户的头像缓存
     */
    public void getUserAvatar(String username, boolean hotRefresh) {

        // TODO: 服务器获取头像，这里从资源文件夹中获取
        try {
            //  URL url = getClass().getResource("/avatar/" + username + ".png");
            String head = Core.getContactHeadImgPath().get(username);
            Image image;
            if (StringUtils.isNotEmpty(head)) {
                image = ImageIO.read(new File(head));
            } else {
                image = AvatarUtil.createOrLoadUserAvatar(username);
            }

            processAvatarData((BufferedImage) image, username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (hotRefresh) {
            AvatarUtil.refreshUserAvatarCache(username);

            if (username.equals(currentUsername)) {
                MyInfoPanel.getContext().reloadAvatar();
            }
        }
    }

    /**
     * 更新指定用户头像
     *
     * @param username   用户名
     * @param hotRefresh 是否热更新，hotRefresh = true， 将刷新该用户的头像缓存
     */
    public void getUserAvatar(String username, boolean hotRefresh, String headPath) {

        // TODO: 服务器获取头像，这里从资源文件夹中获取
        try {
            BufferedImage image = ImageIO.read(new File(headPath));

            processAvatarData(image, username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (hotRefresh) {
            AvatarUtil.refreshUserAvatarCache(username);

            if (username.equals(currentUsername)) {
                MyInfoPanel.getContext().reloadAvatar();
            }
        }
    }

    /**
     * 处理头像数据
     *
     * @param image
     * @param username
     */
    private void processAvatarData(BufferedImage image, String username) {
        if (image != null) {
            AvatarUtil.saveAvatar(image, username);
        } else {
            AvatarUtil.deleteCustomAvatar(username);
        }
    }

}
