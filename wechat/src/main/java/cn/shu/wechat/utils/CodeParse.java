package cn.shu.wechat.utils;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CodeParse {
    /**
     * 解析指定路径下的二维码图片
     *
     * @param imgUrl 用户发送的二维码图片Url
     * @return
     */
    public static String parseQRCode(String imgUrl) {
        log.info("正在解析二维码");
        String content = "";
        BufferedImage image=null;
        Binarizer binarizer=null;
        LuminanceSource source=null;
        BinaryBitmap binaryBitmap=null;
        MultiFormatReader formatReader=null;
        InputStream inputStream=null;
        InputStream inputStream2=null;
        try {
            inputStream2=new URL(imgUrl).openStream();
            inputStream=new DataInputStream(inputStream2);
            image = ImageIO.read(inputStream);

            source = new BufferedImageLuminanceSource(image);
            binarizer = new HybridBinarizer(source);
            binaryBitmap = new BinaryBitmap(binarizer);
            Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
            formatReader = new MultiFormatReader();
            Result result = formatReader.decode(binaryBitmap, hints);

            //System.out.println("result 为：" + result.toString());
            //System.out.println("resultFormat 为：" + result.getBarcodeFormat());
            //System.out.println("resultText 为：" + result.getText());
            //设置返回值
            content = result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            try {
                if(inputStream!=null){
                    inputStream.close();
                }
                if(inputStream2!=null){
                    inputStream2.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            image=null;
            binarizer=null;
            source=null;
            binaryBitmap=null;
            formatReader=null;

            System.gc();
        }

        if(content==null || content.isEmpty()){
            log.info("二维码解析失败");
            //return decode(image);
        }
        return content;
    }
    /**
     * 解析条形码
     * @param imgPath
     * @return
     */
    public static String decode(BufferedImage image) {
        log.info("解析条形码");
        String content = "";
        try {
            //BufferedImage image = ImageIO.read(new DataInputStream(new URL(imgUrl).openStream()));
            if (image == null) {
                log.info("the decode image may be not exit.");
                return content;
            }
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap, null);
            content=result.getText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
    /**
     * 条形码编码
     * @param contents
     * @param width
     * @param height
     * @param imgPath
     */
    public static void encode(String contents, int width, int height, String imgPath) {
        log.info("生成一维码："+contents);
        //保证最小为70*25的大小
        int codeWidth = Math.max(100, width);
        int codeHeight = Math.max(25, height);
        try {
            //使用EAN_13编码格式进行编码
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents,BarcodeFormat.CODE_128, codeWidth, codeHeight, null);
            //生成png格式的图片保存到imgPath路径
            MatrixToImageWriter.writeToStream(bitMatrix, "png",new FileOutputStream(imgPath));
            log.info("encode success! the img's path is "+imgPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 生成二维码
     * @param contents
     * @param width
     * @param height
     * @param imgPath
     */
    public static void enQrcode(String contents, int width, int height, String imgPath) {
        log.info("生成二维码："+contents);
        Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
        // 指定纠错等级
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
        // 指定显示格式为GBK
        hints.put(EncodeHintType.CHARACTER_SET, "GBK");
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(contents,
                    BarcodeFormat.QR_CODE, width, height, hints);
            //生成png格式的图片保存到imgPath路径位置
            MatrixToImageWriter.writeToStream(bitMatrix, "png",
                    new FileOutputStream(imgPath));
            log.info("QR Code encode sucsess! the img's path is "+imgPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
