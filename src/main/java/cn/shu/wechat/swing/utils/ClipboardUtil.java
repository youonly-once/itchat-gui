package cn.shu.wechat.swing.utils;

import cn.shu.wechat.configuration.WechatConfiguration;
import lombok.extern.log4j.Log4j2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Created by 舒新胜 on 20/06/2017.
 */
@Log4j2
public final class ClipboardUtil {
    private ClipboardUtil(){

    }
    private static final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
    public static final String CLIPBOARD_TEMP_DIR;

    static {
        CLIPBOARD_TEMP_DIR = WechatConfiguration.getInstance().getBasePath() + System.getProperty("file.separator") + "clipboard_temp";
        File file = new File(CLIPBOARD_TEMP_DIR);
        log.info("创建剪切板临时文件缓存目录：{}" , file.getAbsolutePath());
        if (!file.mkdirs()) {
            log.warn("创建剪贴板目录失败");
        }
    }

    public static void copyString(String content) {
        if (content != null) {
            Transferable tText = new StringSelection(content);
            clipboard.setContents(tText, null);
        }
    }

    public static void copyImage(String path) {
        try {
            Image image = ImageIO.read(new File(path));
            clipboard.setContents(new ImageTransferable(image), null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void copyImage(Image image) {
        clipboard.setContents(new ImageTransferable(image), null);
    }

    /**
     * 复制文件
     * @param path 文件路径
     */
    public static void copyFile(String path) {
        try {
            File file = new File(path);
            //clipboard.setContents(new FileTransferable(file), null);
            Transferable contents = new Transferable() {
                final DataFlavor[] dataFlavors = new DataFlavor[]{DataFlavor.javaFileListFlavor};

                @Override
                public Object getTransferData(DataFlavor flavor) {
                    ArrayList<File> files = new ArrayList<>(1);
                    files.add(file);
                    return files;
                }

                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return dataFlavors;
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    for (DataFlavor dataFlavor : dataFlavors) {
                        if (dataFlavor.equals(flavor)) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            clipboard.setContents(contents, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 粘贴
     * @return 粘贴内容
     */
    public static Object paste() {
        Transferable transferable = clipboard.getContents(null);
        return paste(transferable);
    }

    /**
     * 粘贴
     * @param transferable 粘贴内容
     * @return 粘贴内容
     */
    public static Object paste(Transferable transferable) {

        if (transferable != null) {
            try {
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    List<File> files = (List<File>)transferable.getTransferData(DataFlavor.javaFileListFlavor);

                    return files.stream()
                            .map(file -> {
                                if (ImageUtil.isImage(file)) {
                                    return IconUtil.getIconFromFile(file);
                                }else if (file.isFile()){
                                    return file.getAbsolutePath();
                                }
                                //TODO
                                return null;
                            }).collect(Collectors.toList());
                } else if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    Object obj = transferable.getTransferData(DataFlavor.imageFlavor);

                    if (obj instanceof Image) {
                        Image image = (Image) obj;
                        File destFile = new File(CLIPBOARD_TEMP_DIR + File.separator + "clipboard_image_" + UUID.randomUUID() + ".png");
                        BufferedImage outImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
                        outImage.getGraphics().drawImage(image, 0, 0, null);
                        ImageIO.write(outImage, "png", destFile);

                        ImageIcon icon = new ImageIcon(image);
                        icon.setDescription(destFile.getAbsolutePath());
                        return icon;
                    }
                } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    return transferable.getTransferData(DataFlavor.stringFlavor);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }



    /**
     * 清除剪切板缓存文件
     */
    public static void clearCache() {
        log.info("清除剪切板缓存文件...");
        try {
            Files.list(Paths.get(CLIPBOARD_TEMP_DIR)).forEach(path1 -> {
                try {
                    Files.delete(path1);
                } catch (IOException e) {
                    log.warn(e.getMessage());
                }
            });
        }catch (IOException e){
            log.warn(e.getMessage());
        }

    }


}

class ImageTransferable implements Transferable {
    private final Image image;

    public ImageTransferable(Image image) {
        this.image = image;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DataFlavor.imageFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return DataFlavor.imageFlavor.equals(flavor);
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor))
            return image;
        throw new UnsupportedFlavorException(flavor);
    }
}

class FileTransferable implements Transferable {
    private final File file;

    public FileTransferable(File file) {
        this.file = file;
    }

    DataFlavor[] dataFlavors = new DataFlavor[]{DataFlavor.javaFileListFlavor};

    @Override
    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
        return new ArrayList<>().add(file);
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return dataFlavors;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        for (DataFlavor dataFlavor : dataFlavors) {
            if (dataFlavor.equals(flavor)) {
                return true;
            }
        }
        return false;
    }
}