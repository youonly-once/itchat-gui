//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.shu.wechat.utils.xxx;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import me.xuxiaoxiao.xtools.common.http.executor.impl.XRequest.MultipartContent.Part;

final public class WeChatToolXXX {
    private static final char[] HEX = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    WeChatToolXXX() {
    }

    public static String fileType(File file) {
        String var1 = fileSuffix(file);
        byte var2 = -1;
        switch(var1.hashCode()) {
            case 97669:
                if (var1.equals("bmp")) {
                    var2 = 0;
                }
                break;
            case 105441:
                if (var1.equals("jpg")) {
                    var2 = 3;
                }
                break;
            case 108273:
                if (var1.equals("mp4")) {
                    var2 = 4;
                }
                break;
            case 111145:
                if (var1.equals("png")) {
                    var2 = 1;
                }
                break;
            case 3268712:
                if (var1.equals("jpeg")) {
                    var2 = 2;
                }
        }

        switch(var2) {
            case 0:
            case 1:
            case 2:
            case 3:
                return "pic";
            case 4:
                return "video";
            default:
                return "doc";
        }
    }

    public static String fileSuffix(File file) {
        try {
            FileInputStream is = new FileInputStream(file);
            Throwable var2 = null;

            String var7;
            try {
                byte[] b = new byte[3];
                is.read(b, 0, b.length);
                String fileCode = bytesToHex(b);
                byte var6 = -1;
                switch(fileCode.hashCode()) {
                    case -1277558572:
                        if (fileCode.equals("ffd8ff")) {
                            var6 = 0;
                        }
                        break;
                    case 1541115082:
                        if (fileCode.equals("474946")) {
                            var6 = 2;
                        }
                        break;
                    case 1657499917:
                        if (fileCode.equals("89504e")) {
                            var6 = 1;
                        }
                }

                switch(var6) {
                    case 0:
                        var7 = "jpg";
                        return var7;
                    case 1:
                        var7 = "png";
                        return var7;
                    case 2:
                        var7 = "gif";
                        return var7;
                    default:
                        if (fileCode.startsWith("424d")) {
                            var7 = "bmp";
                            return var7;
                        }

                        if (file.getName().lastIndexOf(46) <= 0) {
                            var7 = "";
                            return var7;
                        }

                        var7 = file.getName().substring(file.getName().lastIndexOf(46) + 1);
                }
            } catch (Throwable var22) {
                var2 = var22;
                throw var22;
            } finally {
                if (is != null) {
                    if (var2 != null) {
                        try {
                            is.close();
                        } catch (Throwable var21) {
                            var2.addSuppressed(var21);
                        }
                    } else {
                        is.close();
                    }
                }

            }

            return var7;
        } catch (IOException var24) {
            var24.printStackTrace();
            return "";
        }
    }

    private static String bytesToHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];

        for(int i = 0; i < bytes.length; ++i) {
            byte b = bytes[i];
            chars[i << 1] = HEX[b >>> 4 & 15];
            chars[(i << 1) + 1] = HEX[b & 15];
        }

        return new String(chars);
    }

    public static final class Slice extends Part {
        public String fileName;
        public String fileMime;
        public int count;

        public Slice(String name, String fileName, String fileMime, byte[] slice, int count) {
            super(name, slice);
            this.fileName = fileName;
            this.fileMime = fileMime;
            this.count = count;
        }

        public String[] headers() throws IOException {
            String disposition = String.format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", this.name, URLEncoder.encode(this.fileName, "utf-8"));
            String type = String.format("Content-Type: %s", this.fileMime);
            return new String[]{disposition, type};
        }

        public long partLength() {
            return (long)this.count;
        }

        public void partWrite(OutputStream doStream) throws IOException {
            doStream.write((byte[])((byte[])this.value), 0, this.count);
        }
    }
}
