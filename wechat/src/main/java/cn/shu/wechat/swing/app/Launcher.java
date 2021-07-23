package cn.shu.wechat.swing.app;

import cn.shu.wechat.swing.frames.LoginFrame;
import cn.shu.wechat.utils.SpringContextHolder;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by 舒新胜 on 09/06/2017.
 */

@Component
public class Launcher {
    public static final String APP_VERSION = "1.0.0";
    public static String appFilesBasePath;


    public void launch() {
        config();

        if (!isApplicationRunning()) {
            openFrame();
        } else {
            System.exit(-1);
        }
    }


    private void openFrame() {
        LoginFrame loginFrame = SpringContextHolder.getBean(LoginFrame.class);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setVisible(true);
        loginFrame.login(true);
    }

    private void config() {
        String userHome = System.getProperty("user.home");

        appFilesBasePath = userHome + System.getProperty("file.separator") + "wechat";
    }


    /**
     * 通过文件锁来判断程序是否正在运行
     *
     * @return 如果正在运行返回true，否则返回false
     */
    private static boolean isApplicationRunning() {
        boolean rv = false;
        try {
            String path = appFilesBasePath + System.getProperty("file.separator") + "appLock";
            File dir = new File(path);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File lockFile = new File(path + System.getProperty("file.separator") + "appLaunch.lock");
            if (!lockFile.exists()) {
                lockFile.createNewFile();
            }

            //程序名称
            RandomAccessFile fis = new RandomAccessFile(lockFile.getAbsolutePath(), "rw");
            FileChannel fileChannel = fis.getChannel();
            FileLock fileLock = fileChannel.tryLock();
            if (fileLock == null) {
                System.out.println("程序已在运行.");
                rv = true;
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return rv;
    }


}
