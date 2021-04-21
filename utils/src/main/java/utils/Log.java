package utils;

import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.ParseException;
import java.util.Timer;
import java.util.TimerTask;

@Log4j2
public class Log {

    private static RandomAccessFile randomAccessFile;// 写日志
    private String logDir = null;
    private final static Log log = new Log();

    private Log() {
    }

    public static Log getInstance() {
        return log;
    }

    private void setDir(String logDir) throws IOException, Exception {

        this.logDir = logDir;
        // 创建日志文件
        createLogFile();

        // 删除七天前的日志文件
        deleteFileBefore7();

    }

    public void initLog(String logDir) throws IOException, Exception {

        setDir(logDir);
        // 定时任务，定时创建文件
        createFileSchedule();
    }

    /**
     * 写入日志
     *
     * @param s
     */
    public static void logChange(String str) {

        str = DateUtil.getCurrDateAndTimeMil() + " Thread :" + Thread.currentThread().getName() + " INFO  =>  " + str;
        System.out.println(str);

        try {
            if (randomAccessFile == null) {
                return;
            }
            randomAccessFile.seek(randomAccessFile.length());
            randomAccessFile.write(("\t" + str + "\r\n").getBytes());
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            System.out.println(e.getMessage());
        }

    }

    /**
     * 创建日志文件 每天一个文件
     */
    private void createLogFile() throws IOException, Exception {
        if (!createLogDir()) {
            throw new Exception("文件目录创建失败");
        }
        File file = new File(logDir + File.separator + DateUtil.getCurrDate() + ".log");

        if (!file.exists() || randomAccessFile == null) {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
            randomAccessFile = new RandomAccessFile(file, "rw");
        }
    }

    /**
     * 创建目录
     *
     * @throws Exception
     */
    private boolean createLogDir() {
        File logPath = new File(logDir);
        if (!logPath.exists()) {
            return logPath.mkdirs();
        }
        System.out.println("日志目录：" + logPath.getAbsolutePath());
        return true;
    }

    /**
     * 删除七天前日志文件
     *
     * @throws Exception
     */
    private void deleteFileBefore7() {
        // System.out.println("log/"+GetDate.getInstance().getDateBefore7()+".log");
        File logFile = new File(logDir);// + File.separator +
        // DateUtil.getCurrDateBeforeDay(-7) +
        // ".log");
        final String befer7 = DateUtil.getCurrDateBeforeDay(-7);
        if (logFile.isDirectory()) {
            File[] files = logFile.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    // TODO Auto-generated method stub
                    if (pathname.isDirectory()) {
                        return false;
                    }
                    if (!pathname.getName().endsWith(".log")) {

                        return false;
                    }
                    try {
                        int pos = pathname.getName().lastIndexOf('.');
                        long diff = DateUtil.getDateDiff(pathname.getName().substring(0, pos), befer7, "yyyy-MM-dd");

                        if (diff > 0)
                            return true;
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        return false;
                    }
                    return false;
                }
            });
            for (File file : files) {
                file.delete();
                logChange(DateUtil.getCurrDateAndTime() + "===删除文件:" + file.getAbsolutePath());
            }
        }
    }

    /**
     * 定时创建日志文件
     */
    private void createFileSchedule() {// 开始定时任务 发送工程不良


        TimerTask task = new TimerTask() {
            public void run() {
                // 删除七天前的日志文件

                try {
                    deleteFileBefore7();
                    createLogFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (Exception e2) {
                    // TODO: handle exception
                    e2.printStackTrace();
                    System.out.println(e2.getMessage());
                }

            }
        };
        Timer timerEngineerTimer = new Timer();
        // 24小时执行一次
        timerEngineerTimer.scheduleAtFixedRate(task, 24 * 60 * 60 * 1000, 24 * 60 * 60 * 1000);
        // scheduleAtFixedRate从第一个任务开始执行 然后开始计算间隔(不是执行完成，是开始执行)
    }

}
