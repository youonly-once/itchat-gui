package cn.shu.wechat.swing.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public final class FileUtil {
    private FileUtil() {
    }

    /**
     * 打开文件在资源管理器的位置
     * @param filePath 文件路径
     */
    public static void showAtExplorer(String filePath){
        File file = new File(filePath);
        if (file.exists()) {
            try {
                Runtime.getRuntime().exec(
                        "rundll32 SHELL32.DLL,ShellExec_RunDLL "
                                + "Explorer.exe /select," + file.getAbsolutePath());
                // Desktop.getDesktop().open(file.getParentFile());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 使用默认程序打开文件
     *
     * @param path 打开文件
     */
    public static void openFileWithDefaultApplication(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(null, "文件打开失败，没有找到关联的应用程序", "打开失败", JOptionPane.ERROR_MESSAGE);
            e1.printStackTrace();
        }
    }
}
