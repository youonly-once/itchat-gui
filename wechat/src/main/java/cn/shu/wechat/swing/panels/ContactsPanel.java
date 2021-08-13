package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.swing.adapter.ContactsItemsAdapter;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.components.GBC;
import cn.shu.wechat.swing.components.RCListView;
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
import java.util.Optional;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public class ContactsPanel extends ParentAvailablePanel {
    private static ContactsPanel context;

    private RCListView contactsListView;
    private final List<ContactsItem> contactsItemList = new ArrayList<>();

    /**
     * 已更新头像的联系人数量 从上往下
     */
    private  int updatedCount ;
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
        contactsListView.setScrollListener(new RCListView.ScrollListener() {
            @Override
            public void onScroll(int curr,int max) {
                int size = contactsListView.getContentPanel().getComponentCount();
                ContactsItemsAdapter adapter = (ContactsItemsAdapter) contactsListView.getAdapter();
                Map<Integer, String> c = adapter.getPositionMap();
                //每个联系人大约占用的高度
                double height = (max * 1.0) / size;
                //当前应显示的联系人数量 多加载二个
                int count = (int)Math.ceil(curr / height)+20;
                boolean loadPre;
                //启动程序的时候这个顺序还不对，先返回
                Optional<Integer> max1 = adapter.getPositionMap().keySet().stream().max(Integer::compareTo);
                if (max1.isPresent() && adapter.getPositionMap().size() == max1.get() + 1){
                    return;
                }
                int i = updatedCount;
                updatedCount = count;
                for (; i <count; i ++) {

                    int fi = i;
                    new SwingWorker<Object,Object>(){
                        ImageIcon orLoadAvatar =null;
                        int pos;
                        @Override
                        protected Object doInBackground() throws Exception {
                            //i为视图holder的位置
                            Map<Integer, String> positionMap = adapter.getPositionMap();
                            int x =0;
                            for (Integer integer : positionMap.keySet()) {
                                if (integer <= fi){
                                    x++;
                                }
                            }
                            pos = fi - x;
                            if (pos<0){
                                return null;
                            }
                            ContactsItem contactsItem = contactsItemList.get(pos);
                            orLoadAvatar = AvatarUtil.createOrLoadUserAvatar(contactsItem.getId());
                            return null;
                        }

                        @Override
                        protected void done() {
                            //更新联系人头像
                            if (orLoadAvatar == null){
                                return;
                            }
                             updateAvatar(pos, orLoadAvatar);
                        }
                    }.execute();

                }


            }
        });
        add(contactsListView, new GBC(0, 0).setFill(GBC.BOTH).setWeight(1, 1));
    }

    /**
     * 初始化数据，加载所有联系人到List中
     */
    private void initData() {
        contactsItemList.clear();

        for (Map.Entry<String, Contacts> entry : Core.getMemberMap().entrySet()) {
            ContactsItem item = ContactsItem.builder()
                    .id(entry.getKey())
                    .displayName(ContactsTools.getContactDisplayNameByUserName(entry.getKey()))
                    //.avatar()
                    .type(entry.getValue().getType())
                    .build();
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
      //  getContactsUserAvatar();
    }



    /**
     * 更新联系人头像
     * @param contactId 联系人id
     * @param image 联系人头像
     */
    public void updateAvatar(String contactId, ImageIcon image) {
        for (int i = 0; i < contactsItemList.size(); i++) {
            ContactsItem contactsItem = contactsItemList.get(i);
            if (contactsItem.getId().equals(contactId)){
                updateAvatar(i,image);
            }
        }
    }

    /**
     * 更新联系人头像
     * @param pos 联系人位置
     * @param image 联系人头像
     */
    public void updateAvatar(int pos, ImageIcon image) {
        ContactsItem contactsItem = contactsItemList.get(pos);
        contactsItem.setAvatar(image);
        contactsListView.notifyItemChanged(pos);
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
                getUserAvatar(Core.getUserSelf().getUsername(), true);
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
            ImageIcon imageIcon ;
            if (StringUtils.isNotEmpty(head)) {
                BufferedImage image = ImageIO.read(new File(head));
                imageIcon =new ImageIcon();
                imageIcon.setImage(image);
            } else {
                imageIcon = AvatarUtil.createOrLoadUserAvatar(username);
            }

            processAvatarData( imageIcon, username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (hotRefresh) {
//            AvatarUtil.refreshUserAvatarCache(username);

            if (username.equals(Core.getUserSelf().getUsername())) {
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
            processAvatarData(new ImageIcon(image), username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (hotRefresh) {
            AvatarUtil.refreshUserAvatarCache(username);

            if (username.equals(Core.getUserSelf().getUsername())) {
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
    private void processAvatarData(ImageIcon image, String username) {
        if (image != null) {
            AvatarUtil.saveAvatar(image, username);
        } else {
            AvatarUtil.deleteCustomAvatar(username);
        }
    }

}
