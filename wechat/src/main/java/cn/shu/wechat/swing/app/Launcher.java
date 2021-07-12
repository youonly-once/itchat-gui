package cn.shu.wechat.swing.app;

import cn.shu.wechat.controller.LoginController;
import cn.shu.wechat.swing.db.service.*;
import cn.shu.wechat.swing.frames.LoginFrame;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.tasks.HttpGetTask;
import cn.shu.wechat.swing.tasks.HttpResponseListener;
import cn.shu.wechat.swing.utils.DbUtils;
import cn.shu.wechat.utils.SpringApplicationContextUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import org.apache.ibatis.session.SqlSession;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Created by song on 09/06/2017.
 */

@Component
public class Launcher
{
    private static Launcher context;

    public static SqlSession sqlSession;
    public static RoomService roomService;
    public static CurrentUserService currentUserService;
    public static MessageService messageService;
    public static ContactsUserService contactsUserService;
    public static ImageAttachmentService imageAttachmentService;
    public static FileAttachmentService fileAttachmentService;

    public static final String HOSTNAME = "http://www.baidu.com";

    public static final String APP_VERSION = "1.0.0";

    public static String userHome;
    public static String appFilesBasePath;


    static
    {
        //sqlSession = DbUtils.getSqlSession();
        roomService = new RoomService(sqlSession);
        currentUserService = new CurrentUserService(sqlSession);
        messageService = new MessageService(sqlSession);
        contactsUserService = new ContactsUserService(sqlSession);
        imageAttachmentService = new ImageAttachmentService(sqlSession);
        fileAttachmentService = new FileAttachmentService(sqlSession);
    }

    private LoginFrame currentFrame;


    public Launcher()
    {
        context = this;
    }

    public void launch()
    {
        config();

        if (!isApplicationRunning())
        {
            openFrame();
        }
        else
        {
            System.exit(-1);
        }
    }


    private void openFrame()
    {
        LoginFrame currentFrame = SpringContextHolder.getBean(LoginFrame.class);
        //currentFrame = new LoginFrame();
            currentFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            currentFrame.setVisible(true);
            currentFrame.login(true);
    }

    private void config()
    {
        userHome = System.getProperty("user.home");

        appFilesBasePath = userHome + System.getProperty("file.separator") + "wechat";
    }


    /**
     * 通过文件锁来判断程序是否正在运行
     *
     * @return 如果正在运行返回true，否则返回false
     */
    private static boolean isApplicationRunning()
    {
        boolean rv = false;
        try
        {
            String path = appFilesBasePath + System.getProperty("file.separator") + "appLock";
            File dir = new File(path);
            if (!dir.exists())
            {
                dir.mkdirs();
            }

            File lockFile = new File(path + System.getProperty("file.separator") + "appLaunch.lock");
            if (!lockFile.exists())
            {
                lockFile.createNewFile();
            }

            //程序名称
            RandomAccessFile fis = new RandomAccessFile(lockFile.getAbsolutePath(), "rw");
            FileChannel fileChannel = fis.getChannel();
            FileLock fileLock = fileChannel.tryLock();
            if (fileLock == null)
            {
                System.out.println("程序已在运行.");
                rv = true;
            }
        }
        catch (FileNotFoundException e1)
        {
            e1.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return rv;
    }


    public static Launcher getContext()
    {
        return context;
    }


}
