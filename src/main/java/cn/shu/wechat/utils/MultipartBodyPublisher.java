package cn.shu.wechat.utils;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class MultipartBodyPublisher {
    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final String BOUNDARY = "12A3DA64D65";
    private static final String LINE_FEED = "\r\n";
    private final byte[] endBoundary = ("--" + BOUNDARY + "--" + LINE_FEED).getBytes(StandardCharsets.UTF_8);

    private final Map<String, byte[]> parts = new LinkedHashMap<>();
    public String getBoundary() {
        return BOUNDARY;
    }

    public static byte[] concatThree(byte[] a, byte[] b, byte[] c) {
        byte[] result = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        System.arraycopy(c, 0, result, a.length + b.length, c.length);
        return result;
    }

    public static byte[] concatBytes(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public MultipartBodyPublisher addText(String name, String value) {
        String part = "--" + BOUNDARY + LINE_FEED +
                "Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED +
                LINE_FEED +
                value + LINE_FEED;
        parts.put(name, part.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public MultipartBodyPublisher addFile( String name,Path file, String mimeType, String fileName) throws IOException {
        return addFile(name, Files.readAllBytes(file), mimeType, fileName);
    }

    public MultipartBodyPublisher addFile(String name, byte[] bytes, String mimeType, String fileName) throws IOException {
        String fileHeader = "--" + BOUNDARY + LINE_FEED +
                "Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + fileName + "\"" + LINE_FEED +
                "Content-Type: " + mimeType + LINE_FEED + LINE_FEED;

        parts.put("fileHeader", fileHeader.getBytes(StandardCharsets.UTF_8));
        parts.put(name, bytes);
        parts.put("end", LINE_FEED.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public HttpRequest.BodyPublisher build() {

        //不能用 HttpRequest.BodyPublishers.ofByteArrays(parts) //上传文件返回数据为空 状态码为412
        parts.put("endBoundary", endBoundary);
        return HttpRequest.BodyPublishers.ofByteArray(mergeParts(parts.values()));
    }

    private byte[] mergeParts(Collection<byte[]> parts) {
        // 1. 计算总长度
        int totalLength = 0;
        for (byte[] part : parts) {
            totalLength += part.length;
        }

        // 2. 分配结果数组
        byte[] result = new byte[totalLength];

        // 3. 拷贝内容
        int currentPos = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, currentPos, part.length);
            currentPos += part.length;
        }

        return result;
    }
    public static String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30;

        for(int i = 0; i < count; ++i) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }

        return buffer.toString();
    }

}
