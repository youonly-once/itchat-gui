package cn.shu.wechat.swing.utils;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultipartBodyPublisher {
    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private final String BOUNDARY = "12A3DA64D65";
    private static final String LINE_FEED = "\r\n";
    private final List<byte[]> parts = new ArrayList<>();

    public String getBoundary() {
        return BOUNDARY;
    }

    public MultipartBodyPublisher addText(String name, String value) {
        String part = "--" + BOUNDARY + LINE_FEED +
                "Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED +
                //"Content-Type: text/plain; charset=UTF-8" + LINE_FEED +
                LINE_FEED +
                value + LINE_FEED;
        parts.add(part.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public MultipartBodyPublisher addFile( String name,Path file, String mimeType, String fileName) throws IOException {
        String fileHeader = "--" + BOUNDARY + LINE_FEED +
                "Content-Disposition: form-data; name=\""+name+"\"; filename=\"" + fileName + "\"" + LINE_FEED +
                "Content-Type: " + mimeType + LINE_FEED + LINE_FEED;

        parts.add(fileHeader.getBytes(StandardCharsets.UTF_8));
        parts.add(Files.readAllBytes(file));
        parts.add(LINE_FEED.getBytes(StandardCharsets.UTF_8));
        return this;
    }

    public HttpRequest.BodyPublisher build() {
        String endBoundary = "--" + BOUNDARY + "--" + LINE_FEED;
        parts.add(endBoundary.getBytes(StandardCharsets.UTF_8));

        //不能用 HttpRequest.BodyPublishers.ofByteArrays(parts) //上传文件返回数据为空 状态码为412

        return HttpRequest.BodyPublishers.ofByteArray(mergeParts(parts));
    }

    private byte[] mergeParts(List<byte[]> parts) {
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
