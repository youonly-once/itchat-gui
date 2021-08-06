package cn.shu.wechat.utils;
import lombok.extern.log4j.Log4j2;
import java.io.*;
import java.util.*;
/**
 * 文件分割工具类
 *
 * @author 舒新胜
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 11:46 AM
 */
@Log4j2
public class FileSplitAndMergeUtil {

    /**
     * 大文件分片方法1：普通IO方式
     *
     * @param filePath 文件路径
     */
    public static ArrayList<String> splitFile1(String filePath) {
        InputStream bis = null;
        OutputStream bos = null;
        ArrayList<String> partFilePaths = new ArrayList<>();
        try {
            File file = new File(filePath);
            bis = new BufferedInputStream(new FileInputStream(file));
            //单片文件大小,5M
            long splitSize = 512 * 1024;
            //已读取的字节数
            long writeByte = 0;
            int len = 0;
            byte[] bt = new byte[1024];
            while (-1 != (len = bis.read(bt))) {
                if (writeByte % splitSize == 0) {
                    if (bos != null) {
                        bos.flush();
                        bos.close();
                    }
                    String partFilePath = filePath + "." + (writeByte / splitSize + 1) + ".part";
                    bos = new BufferedOutputStream(new FileOutputStream(partFilePath));
                    partFilePaths.add(partFilePath);
                }
                writeByte += len;
                bos.write(bt, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (bos != null) {
                try {
                    bos.flush();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return partFilePaths;
    }

    /**
     * 删除分片文件
     *
     * @param filePathList 分片列表
     */
    public static void deletePartFile(List<String> filePathList) {
        for (String s : filePathList) {
            try {
                boolean delete = new File(s).delete();
                if (!delete){
                    log.error("分片文件删除失败！");
                }
            } catch (Exception e) {
                log.error("分片文件删除失败！");
            }

        }
    }
}