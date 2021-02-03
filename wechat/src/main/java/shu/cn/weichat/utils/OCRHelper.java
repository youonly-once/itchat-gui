package shu.cn.weichat.utils;



import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 图文识别帮助类
 *
 * @author Felix Li
 * @create 2017-12-19-9:12
 */
@Log4j2
public class OCRHelper {

    private static final String LANG_OPTION = "-l";
    private static final String EOL = System.getProperty("line.separator");

    /**
     *  Tesseract-OCR的安装路径
     */
    private static String tessPath = "D:/Tesseract-OCR";
    //private String tessPath = new File("tesseract").getAbsolutePath();

    /**
     * @param imageFile   传入的图像文件
     * @param imageFormat 传入的图像格式
     * @return 识别后的字符串
     */
    public static String recognizeText(String imagePath,String filename,String lanuge) throws Exception {
        /**
         * 设置输出文件的保存的文件目录
         */
        File outputFile = new File(imagePath, filename+"_output");

        StringBuffer strB = new StringBuffer();
        List<String> cmd = new ArrayList<String>();

        cmd.add(tessPath + "/tesseract");

        cmd.add(imagePath+filename);
        cmd.add(outputFile.getName());
        cmd.add(LANG_OPTION);

        cmd.add(lanuge);

        ProcessBuilder pb = new ProcessBuilder();
        /**
         *Sets this process builder's working directory.
         */
        pb.directory(new File(imagePath));

        pb.command(cmd);
        pb.redirectErrorStream(true);
        long startTime = System.currentTimeMillis();
        System.out.println("开始时间：" + startTime);
        Process process = pb.start();
        // tesseract.exe 1.jpg 1 -l chi_sim
        //不习惯使用ProcessBuilder的，也可以使用Runtime，效果一致
        // Runtime.getRuntime().exec("tesseract.exe 1.jpg 1 -l chi_sim");
        /**
         * the exit value of the process. By convention, 0 indicates normal
         * termination.
         */
//      System.out.println(cmd.toString());
        int w = process.waitFor();
        if (w == 0)// 0代表正常退出
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(outputFile.getAbsolutePath() + ".txt"),
                    "UTF-8"));
            String str;

            while ((str = in.readLine()) != null) {
                strB.append(str).append(EOL);
            }
            in.close();

            long endTime = System.currentTimeMillis();
            System.out.println("结束时间：" + endTime);
            System.out.println("耗时：" + (endTime - startTime) + "毫秒");
        } else {
            String msg;
            switch (w) {
                case 1:
                    msg = "Errors accessing files. There may be spaces in your image's filename.";
                    break;
                case 29:
                    msg = "Cannot recognize the image or its selected region.";
                    break;
                case 31:
                    msg = "Unsupported image format.";
                    break;
                default:
                    msg = "Errors occurred.";
            }
            throw new RuntimeException(msg);
        }
        new File(outputFile.getAbsolutePath() + ".txt").delete();
        String result=strB.toString().replaceAll("\\s*", "");
        if (result.isEmpty()) {
            return recognizeText(imagePath, filename,"end");
        }
        return result;

    }
}
