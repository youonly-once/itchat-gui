package cn.shu.wechat.utils;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 3/3/2021 10:57 AM
 */

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * MD5计算工具
 */
public class MD5Util {

    /**
     * 获取一个文件的md5值(可处理大文件)
     *
     * @return md5 value
     */
    public static String getMD5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[1024 * 1024];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            return null;
        }
    }


    public static String fastMD5(File file) throws IOException, NoSuchAlgorithmException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r");
             FileChannel channel = raf.getChannel()) {

            long size = channel.size();
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, size);

            byte[] chunk = new byte[1024 * 1024]; // 1MB临时缓冲
            while (buffer.hasRemaining()) {
                int remaining = Math.min(buffer.remaining(), chunk.length);
                buffer.get(chunk, 0, remaining);
                md5.update(chunk, 0, remaining);
            }

            return HexFormat.of().formatHex(md5.digest());
        }
    }


    /**
     * 求一个字符串的md5值
     *
     * @param target 字符串
     * @return md5 value
     */
    public static String MD5(String target) {
        return DigestUtils.md5Hex(target);
    }

}
