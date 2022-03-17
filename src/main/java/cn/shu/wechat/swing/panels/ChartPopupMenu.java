package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.swing.frames.ImageViewerFrame;
import cn.shu.wechat.utils.ChartUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

/**
 * @Description
 * @Author SXS
 * @Date 2021/11/11 11:55
 */
@Component
@Lazy
public class ChartPopupMenu extends JPopupMenu {
    public ChartPopupMenu() {
        init();
    }


    public static ChartPopupMenu getInstance(String roomId) {
        ChartPopupMenu.roomId = roomId;
        return SpringContextHolder.getBean(ChartPopupMenu.class);
    }

    private static String roomId;

    private void init() {
        setLayout(new GridLayout(12, 3));
        for (
                Field declaredField : Contacts.class.getDeclaredFields()) {
            declaredField.setAccessible(true);
            JMenuItem jMenuItem = new JMenuItem(declaredField.getName() + "图表");
            jMenuItem.addActionListener(e -> createAndShowChart(declaredField.getName().toLowerCase()));
            this.add(jMenuItem);
        }
        //群成员活跃度
        JMenuItem jMenuItem = new JMenuItem("gma10图表");
        jMenuItem.addActionListener(e -> createAndShowChart("gma10"));
        this.add(jMenuItem);
        //聊天消息关键词
        jMenuItem = new JMenuItem( "mf10图表");
        jMenuItem.addActionListener(e -> createAndShowChart("mf10"));
        this.add(jMenuItem);
        //聊天消息关键词
        jMenuItem = new JMenuItem( "mft10图表");
        jMenuItem.addActionListener(e -> createAndShowChart("mft10"));
        this.add(jMenuItem);
    }

    /**
     * 创建属性分布图并展示
     *
     * @param attr 属性
     */
    private void createAndShowChart(String attr) {

        ChartUtil chartUtil = SpringContextHolder.getBean(ChartUtil.class);
        Optional<BufferedImage> bufferedImageOptional;
        if ("gma10".equals(attr)){
            if (ContactsTools.isRoomContact(roomId)) {
                bufferedImageOptional = chartUtil.makeWXMemberOfGroupActivityBufferedImage(roomId);
            }else{
                bufferedImageOptional = chartUtil.makeWXUserActivityBufferedImage(roomId);
            }
        }else if("mf10".equals(attr)){
            bufferedImageOptional = chartUtil.makeWXGroupMessageTopBufferedImage(roomId);
        }else if("mft10".equals(attr)){
            bufferedImageOptional =chartUtil.makeWXGroupMessageTypeTopBufferedImage(roomId);
        }else{
            bufferedImageOptional = chartUtil.makeContactsAttrPieChartAsBufferedImage(roomId, attr, Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        }


        Optional<BufferedImage> finalBufferedImageOptional = bufferedImageOptional;
        bufferedImageOptional.ifPresent(a -> {
            ImageViewerFrame instance = ImageViewerFrame.getInstance();
            instance.setImage(finalBufferedImageOptional.get());

            instance.toFront();
            instance.setVisible(true);
        });


    }
}
