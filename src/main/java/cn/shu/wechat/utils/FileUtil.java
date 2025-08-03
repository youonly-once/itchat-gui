package cn.shu.wechat.utils;

import cn.shu.wechat.constant.DownloadStatus;
import cn.shu.wechat.task.DownloadManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

@Log4j2
public final class FileUtil {

    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

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
     * 使用默认程序打开文件
     *
     * @param path
     */
    public static void openFileWithDefaultApplication(String path) {
        if (StringUtils.isEmpty(path)) {
            JOptionPane.showMessageDialog(null, "文件不存在", "打开失败", JOptionPane.ERROR_MESSAGE);
        }
        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
            try {
                if (DownloadManager.containsTask(path)) {
                    if (DownloadManager.getStatus(path) == DownloadStatus.SUCCESS) {
                        Desktop.getDesktop().open(new File(path));
                    } else if (DownloadManager.getStatus(path) == DownloadStatus.RUNNING || DownloadManager.getStatus(path) == DownloadStatus.WAITING) {
                        JOptionPane.showMessageDialog(null, "下载中", "打开失败", JOptionPane.ERROR_MESSAGE);
                    } else if (DownloadManager.getStatus(path) == DownloadStatus.FAIL) {
                        JOptionPane.showMessageDialog(null, "下载失败", "打开失败", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    Desktop.getDesktop().open(new File(path));
                }

            } catch (IOException e1) {
                JOptionPane.showMessageDialog(null, "文件打开失败，没有找到关联的应用程序", "打开失败", JOptionPane.ERROR_MESSAGE);
                log.error(e1.getMessage(), e1);
            } catch (IllegalArgumentException e2) {
                JOptionPane.showMessageDialog(null, "文件已被删除", "打开失败", JOptionPane.ERROR_MESSAGE);
                log.error(e2.getMessage(), e2);
            }


        });
    }

    public static String fileSizeString(String path) {
        return fileSizeString(new File(path).length());

    }

    /**
     * 将字节大小转换为 KB / MB 字符串表示（保留两位小数）
     * 例如：1024 → 1 KB，1048576 → 1 MB
     */
    public static String fileSizeString(Long size) {
        if (size == null || size < 0) {
            return "未知大小";
        }

        if (size < KB) {
            return size + " 字节";
        } else if (size < MB) {
            return DECIMAL_FORMAT.format(size / (double) KB) + " K";
        } else {
            return DECIMAL_FORMAT.format(size / (double) MB) + " M";
        }
    }

    public static long getDirectorySize(File dir) {
        if (dir == null || !dir.exists()) return 0L;
        if (dir.isFile()) return dir.length();

        long total = 0;
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                total += getDirectorySize(file);
            }
        }
        return total;
    }
}
