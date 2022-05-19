package cn.shu.wechat.swing.panels;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.pojo.entity.Contacts;
import cn.shu.wechat.swing.frames.ImageViewerFrame;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.utils.ChartUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        jMenuItem = new JMenuItem("mf10图表");
        jMenuItem.addActionListener(e -> createAndShowChart("mf10"));
        this.add(jMenuItem);
        //聊天消息关键词
        jMenuItem = new JMenuItem("mft10图表");
        jMenuItem.addActionListener(e -> createAndShowChart("mft10"));
        this.add(jMenuItem);
        //导出好友属性
        jMenuItem = new JMenuItem("导出好友属性");
        jMenuItem.addActionListener(e -> createAndShowChart("export"));
        this.add(jMenuItem);
    }

    /**
     * 创建属性分布图并展示
     *
     * @param attr 属性
     */
    private void createAndShowChart(String attr) {

        ChartUtil chartUtil = SpringContextHolder.getBean(ChartUtil.class);
        Optional<BufferedImage> bufferedImageOptional = Optional.empty();
        if ("gma10".equals(attr)) {
            if (ContactsTools.isRoomContact(roomId)) {
                bufferedImageOptional = chartUtil.makeWXMemberOfGroupActivityBufferedImage(roomId);
            } else {
                bufferedImageOptional = chartUtil.makeWXUserActivityBufferedImage(roomId);
            }
        } else if ("mf10".equals(attr)) {
            bufferedImageOptional = chartUtil.makeWXGroupMessageTopBufferedImage(roomId);
        } else if ("mft10".equals(attr)) {
            bufferedImageOptional = chartUtil.makeWXGroupMessageTypeTopBufferedImage(roomId);
        } else if ("export".equals(attr)) {
            JFileChooser fileChooser = new JFileChooser(".");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(MainFrame.getContext());
            File selectedFile = fileChooser.getSelectedFile();
            ExportParams exportParams = new ExportParams();
            exportParams.setType(ExcelType.XSSF);
            Workbook sheets;
            String username = ContactsTools.getContactDisplayNameByUserName(roomId);
            username = DownloadTools.replace(username);
            if (ContactsTools.isRoomContact(roomId)) {
                sheets = ExcelExportUtil.exportExcel(exportParams, Contacts.class, Core.getGroupMap().get(roomId).getMemberlist());
            } else {
                sheets = ExcelExportUtil.exportExcel(exportParams, Contacts.class, Core.getMemberMap().values());
            }
            try(FileOutputStream fileOutputStream = new FileOutputStream(selectedFile + File.separator + username + "_attr.xlsx")) {
                sheets.write(fileOutputStream);
            } catch (IOException e) {

                JOptionPane.showMessageDialog(MainFrame.getContext(),"导出失败："+ e.getMessage(), "导出", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
            JOptionPane.showMessageDialog(MainFrame.getContext(), "导出完成", "导出", JOptionPane.INFORMATION_MESSAGE);

        } else {
            bufferedImageOptional = chartUtil.makeContactsAttrPieChartAsBufferedImage(roomId, attr, Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height);
        }

        if (bufferedImageOptional.isPresent()) {
            Optional<BufferedImage> finalBufferedImageOptional = bufferedImageOptional;
            bufferedImageOptional.ifPresent(a -> {
                ImageViewerFrame instance = ImageViewerFrame.getInstance();
                instance.setImage(finalBufferedImageOptional.get());

                instance.toFront();
                instance.setVisible(true);
            });
        }

    }
}
