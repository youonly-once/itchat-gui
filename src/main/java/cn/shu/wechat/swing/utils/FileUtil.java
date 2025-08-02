package cn.shu.wechat.swing.utils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public final class FileUtil {
    private FileUtil() {
    }

    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static boolean isDesktopSupported() {
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN);
    }

    /**
     * 在资源管理器中定位指定文件（当前仅支持 Windows）
     */
    public static void showAtExplorer(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "文件不存在：" + filePath, "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isWindows()) {
            JOptionPane.showMessageDialog(null, "当前操作系统不支持文件定位功能", "不支持", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // 使用 ProcessBuilder 更安全
            ProcessBuilder builder = new ProcessBuilder(
                    "explorer.exe", "/select,", file.getAbsolutePath()
            );
            builder.start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "打开资源管理器失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);

        }
    }

    /**
     * 使用默认程序打开文件（支持 Windows、macOS、部分 Linux 桌面环境）
     */
    public static void openFileWithDefaultApplication(String path) {
        File file = new File(path);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(null, "文件不存在：" + path, "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isDesktopSupported()) {
            JOptionPane.showMessageDialog(null, "当前系统不支持默认打开程序", "不支持", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Desktop.getDesktop().open(file);
        } catch (IOException | SecurityException e) {
            JOptionPane.showMessageDialog(null, "文件打开失败，系统未配置默认程序或权限受限", "打开失败", JOptionPane.ERROR_MESSAGE);
        }
    }
}
