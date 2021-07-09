package cn.shu.wechat.utils;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 3/6/2021 11:31 PM
 */
public class PropertyUtil {


    private static final Properties pps = new Properties();

    private static String msgFileName = DateFormatUtils.format(System.currentTimeMillis(),DateFormatConstant.yyyy_mm_dd) + "msg.property";

    static {
   /*     new Thread(new Runnable() {
            @Override
            public void run() {
                msgFileName = DateUtil.getCurrDate() + "msg.property";
                try {
                    Thread.sleep(1000 * 60 * 60 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }, "").start();*/
    }

    /**
     * 存储发送的消息
     *
     * @param msgId 消息id
     * @param msg   消息内容
     */
    public static void storeMsg(String msgId, String msg) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(msgFileName, true), StandardCharsets.UTF_8);
            pps.setProperty(msgId, msg);
            pps.store(outputStreamWriter, DateFormatUtils.format(System.currentTimeMillis(),DateFormatConstant.yyyy_mm_dd_HH_MM_SS));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    /**
     * 读取消息
     *
     * @param msgId
     */
    public static String loadMsg(String msgId) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(new FileInputStream(msgFileName), StandardCharsets.UTF_8);
            pps.load(inputStreamReader);
            return pps.getProperty(msgId);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return "";
    }

    public static void delMsg(String msgId) {
        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(msgFileName, true), StandardCharsets.UTF_8);
            pps.remove(msgId);
            pps.store(outputStreamWriter, DateFormatUtils.format(System.currentTimeMillis(),DateFormatConstant.yyyy_mm_dd_HH_MM_SS));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outputStreamWriter != null) {
                try {
                    outputStreamWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
