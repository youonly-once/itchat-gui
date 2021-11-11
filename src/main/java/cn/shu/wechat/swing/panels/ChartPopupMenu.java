package cn.shu.wechat.swing.panels;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.swing.frames.ImageViewerFrame;
import cn.shu.wechat.utils.ChartUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

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
    }

    /**
     * 创建属性分布图并展示
     *
     * @param attr 属性
     */
    private void createAndShowChart(String attr) {

        ChartUtil chartUtil = SpringContextHolder.getBean(ChartUtil.class);
        BufferedImage bufferedImage = chartUtil.makeContactsAttrPieChartAsBufferedImage(roomId, attr, Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        if (bufferedImage == null) {
            JOptionPane.showMessageDialog(this, "创建失败。");
            return;
        }

        ImageViewerFrame instance = ImageViewerFrame.getInstance();
        instance.setImage(bufferedImage);

        instance.toFront();
        instance.setVisible(true);

    }
}
