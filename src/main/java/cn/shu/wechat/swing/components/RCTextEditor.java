package cn.shu.wechat.swing.components;

import cn.shu.wechat.swing.components.message.FileEditorThumbnail;
import cn.shu.wechat.swing.frames.ImageViewerFrame;
import cn.shu.wechat.swing.utils.ClipboardUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/**
 * Created by 舒新胜 on 03/07/2017.
 */
public class RCTextEditor extends JTextPane implements DropTargetListener {
    public void paste(Object data) {
        if (data instanceof String) {
            this.replaceSelection((String) data);
        } else if (data instanceof ImageIcon) {
            ImageIcon icon = (ImageIcon) data;
            adjustAndInsertIcon(icon);
        } else if (data instanceof java.util.List) {
            java.util.List<Object> list = (java.util.List<Object>) data;
            for (Object obj : list) {
                // 图像
                if (obj instanceof ImageIcon) {
                    adjustAndInsertIcon((ImageIcon) obj);
                }
                // 文件
                else if (obj instanceof String) {
                    FileEditorThumbnail thumbnail = new FileEditorThumbnail((String) obj);
                    this.insertComponent(thumbnail);
                }
            }
        }
    }
    @Override
    public void paste() {
        Object paste = ClipboardUtil.paste();
        paste(paste);
    }
    /**
     * 插入图片到编辑框，并自动调整图片大小
     *
     * @param icon
     */
    private void adjustAndInsertIcon(ImageIcon icon) {
        String path = icon.getDescription();
        int iconWidth = icon.getIconWidth();
        int iconHeight = icon.getIconHeight();
        float scale = iconWidth * 1.0F / iconHeight;
        boolean needToScale = false;
        int max = 100;
        if (iconWidth >= iconHeight && iconWidth > max) {
            iconWidth = max;
            iconHeight = (int) (iconWidth / scale);
            needToScale = true;
        } else if (iconHeight >= iconWidth && iconHeight > max) {
            iconHeight = max;
            iconWidth = (int) (iconHeight * scale);
            needToScale = true;
        }

        JLabel label = new JLabel();
        if (needToScale) {
            ImageIcon scaledIcon = new ImageIcon(icon.getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH));
            scaledIcon.setDescription(icon.getDescription());
            //this.insertIcon(scaledIcon);
            label.setIcon(scaledIcon);
        } else {
            //this.insertIcon(icon);
            label.setIcon(icon);
        }

        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 双击预览选中的图片
                if (e.getClickCount() == 2) {
                    ImageViewerFrame frame = ImageViewerFrame.getInstance();
                    try {
                        frame.setImage(ImageIO.read(new File(path)));
                        frame.setVisible(true);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                }
                super.mouseClicked(e);
            }
        });

        insertComponent(label);

    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        if (dropListener != null){
            dropListener.drop(dtde);
        }else{
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            Transferable transferable = dtde.getTransferable();
            Object data = ClipboardUtil.paste(transferable);
            paste(data);
        }
    }
    public interface DropListener{
        void drop(DropTargetDropEvent dtde);
    }
    private DropListener dropListener;
    public void addDropListener(DropListener dropListener){
        this.dropListener = dropListener;
        this.setDragEnabled(true);
    }
}