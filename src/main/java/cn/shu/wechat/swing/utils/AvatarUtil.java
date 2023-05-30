package cn.shu.wechat.swing.utils;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.frames.MainFrame;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 头像创建工具类
 * @author SXS
 */
@Log4j2
public final class AvatarUtil {
    private static final Color[] colorArr;
    private AvatarUtil() {
    }
    static {
        colorArr = new Color[]{
                new Color(244, 67, 54),
                new Color(233, 30, 99),
                new Color(156, 39, 176),
                new Color(103, 58, 183),
                new Color(63, 81, 181),
                new Color(33, 150, 243),
                new Color(3, 169, 244),
                new Color(0, 188, 212),
                new Color(0, 150, 136),
                new Color(76, 175, 80),
                new Color(139, 195, 74),
                new Color(205, 220, 57),
                new Color(255, 193, 7),
                new Color(255, 152, 0),
                new Color(255, 87, 34),
                new Color(121, 85, 72),
                new Color(158, 158, 158),
                new Color(96, 125, 139)
        };
    }

    private static final String AVATAR_CACHE_ROOT;
    private static final String CUSTOM_AVATAR_CACHE_ROOT;
    private static final int DEFAULT_AVATAR = 0;
    private static final int CUSTOM_AVATAR = 1;

    /**
     * 图片缓存
     */
    private static final Map<String, ImageIcon> avatarCache = new ConcurrentHashMap<>();

    public static void invalidateAvatarCache(){
        avatarCache.clear();
        avatarCacheBig.clear();
    }
    /**
     * 大头像缓存
     */
    private static final Map<String, Image> avatarCacheBig = new ConcurrentHashMap<>();

    static {
        AVATAR_CACHE_ROOT = WechatConfiguration.getInstance().getBasePath() + "/cache/avatar";

        File file = new File(AVATAR_CACHE_ROOT);
        if (!file.exists()) {
            file.mkdirs();
            log.info("创建头像缓存目录：{}", file.getAbsolutePath());
        }

        CUSTOM_AVATAR_CACHE_ROOT = AVATAR_CACHE_ROOT + "/custom";
        file = new File(CUSTOM_AVATAR_CACHE_ROOT);
        if (!file.exists()) {
            file.mkdirs();
            log.info("创建用户自定义头像缓存目录：{}", file.getAbsolutePath());
        }
    }

    /**
     * 创建群头像
     *
     * @param userName 房间id
     * @return 头像
     */
    public static ImageIcon createOrLoadGroupAvatar(String userName) {   //获取群成员

        //获取内存中的群头像
        ImageIcon avatarIcon = avatarCache.get(userName);
        Image avatar;
        // 如果在内存中的缓存不存在
        if (avatarIcon == null) {
            //获取网络图片
            Contacts contacts = Core.getMemberMap().get(userName);
            avatar = DownloadTools.downloadHeadImgByRelativeUrl(contacts.getHeadimgurl());
            if (avatar == null) {
                //获取缓存在磁盘的头像
                avatar = getCachedImageAvatar(userName);

                // 硬盘中无缓存
                if (avatar == null) {
                    // 如果尚未从服务器获取群成员，则获取默认群组头像
                    if (contacts.getMemberlist() == null || contacts.getMemberlist().isEmpty()) {
                        //获取 ##.png头像
                        String sign = "##";
                        avatar = getCachedImageAvatar(sign);
                        // 默认群组头像不存在，则生成
                        if (avatar == null) {
                            log.info("创建群组默认头像 : {}", userName);
                            avatar = createAvatar(ContactsTools.getContactDisplayNameByUserName(userName));
                        }
                    } else {
                        List<Contacts> memberList = contacts.getMemberlist();
                        // 有群成员，根据群成员的头像合成群头像
                        log.info("创建群组个性头像 : {}", userName);
                        avatar = createGroupAvatar(userName, memberList);
                    }
                }
            }

            avatarIcon = AvatarUtil.putUserAvatarCache(userName, avatar);
        }
        return avatarIcon;
    }


    /**
     * 获取用户头像
     *
     * @param userName 用户名
     */
    private static ImageIcon getOrDownloadUserAvatar(String userName, Contacts user) {
        if (WechatConfiguration.getInstance().getFuzzUpAvatar()){
            Image avatar = createAvatar(ContactsTools.getContactDisplayNameByUserName(userName));
            if (avatar == null){
                return IconUtil.getIcon(MainFrame.getContext(),"/image/smile.png");
            }
           return new ImageIcon(avatar.getScaledInstance(40, 40, Image.SCALE_SMOOTH));
        }
        //获取内存中的头像
        ImageIcon avatarIcon = avatarCache.get(userName);
        if (avatarIcon != null) {
            return avatarIcon;
        }
        String s = userName + "_avatar";
        synchronized (s.intern()){
           avatarIcon = avatarCache.get(userName);
            if (avatarIcon != null) {
                return avatarIcon;
            }
            Image avatar = null;
            if (user != null) {
                //下载头像
                if (StringUtils.isNotEmpty((user.getHeadimgurl()))) {
                    avatar = DownloadTools.downloadHeadImgByRelativeUrl(user.getHeadimgurl());
                }else{
                    avatar = DownloadTools.downloadHeadImgByUserName(user.getUsername());
                }
            }
            if (avatar != null) {
                avatarIcon = putUserAvatarCache(userName, avatar);
            }
        }

        return avatarIcon;
    }

    /**
     * 创建或读取群成员头像
     *
     * @param userName 用户名也是房间id
     * @return 头像
     */
    public static ImageIcon createOrLoadMemberAvatar(String groupName, String userName) {
        Contacts member = ContactsTools.getMemberOfGroup(groupName, userName);
        return getOrDownloadUserAvatar(userName, member);
    }

    /**
     * 创建或读取群成员头像
     *
     * @param userName 用户名也是房间id
     * @return 头像
     */
    public static BufferedImage createOrLoadBigAvatar(String userName, String url) {
        String filePath = Core.getContactHeadImgPath().get(userName);
        if (url.startsWith("http")){
            try {
                return  ImageIO.read(new URL(url));
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }else{

            if (filePath == null) {

                filePath = DownloadTools.downloadBigHeadImg(url, userName);
            }

        }
        Core.getContactHeadImgPath().put(userName, filePath);
        try {
            BufferedImage read = ImageIO.read(new File(filePath));
            return read;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 获取用户头像
     *
     * @param userName 用户名
     * @return 头像
     */
    public static ImageIcon createOrLoadUserAvatar(String userName) {

        Contacts contacts = Core.getMemberMap().get(userName);
        return getOrDownloadUserAvatar(userName, contacts);
    }
    /**
     * 刷新用户头像缓存
     *
     * @param username
     */
    public static void refreshUserAvatarCache(String username) {
        avatarCache.put(username, null);
    }

    /**
     * 判断头像是否加载
     * @param userName
     * @return
     */
    public static boolean avatarExists(String userName){
        if (!avatarCache.containsKey(userName)){
            return false;
        }
        return avatarCache.get(userName) != null;
    }
    /**
     * 添加用户头像
     *
     * @param username 用户名
     * @param image    头像
     */
    public static ImageIcon putUserAvatarCache(String username, Image image) {
        ImageIcon imageIcon = null;
        if (image != null) {
            try {
                image = ImageUtil.setRadius(image, ((BufferedImage) image).getWidth(), ((BufferedImage) image).getHeight(), 35)
                        .getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (avatarCache.containsKey(username)) {
                imageIcon = avatarCache.get(username);
                if (imageIcon != null) {
                    imageIcon.setImage(image);
                    return imageIcon;
                }
            }
            imageIcon = new ImageIcon();
            imageIcon.setImage(image);
            avatarCache.put(username, imageIcon);
        }
        return imageIcon;
    }

    /**
     * 更新头像
     *
     * @param username  用户名
     * @param imagePath 头像路径
     */
    public static void putUserAvatarCache(String username, String imagePath) {
        if (StringUtils.isNotEmpty(imagePath)) {
            try {
                BufferedImage read = ImageIO.read(new File(imagePath));
                putUserAvatarCache(username, read);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 创建头像
     *
     * @param displayName 显示名称
     * @return 头像
     */
    private static Image createAvatar(String displayName) {
        String drawString;
        //取前几位绘制头像
        if (displayName.length() > 1) {
            drawString = displayName.substring(0, 1).toUpperCase() + displayName.substring(1, 2).toLowerCase();
        } else {
            drawString = displayName;
        }

        try {
            int width = 200;
            int height = 200;

            // 创建BufferedImage对象
            Font font = FontUtil.getDefaultFont(96, Font.PLAIN);
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            // 获取Graphics2D
            Graphics2D g2d = image.createGraphics();

            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // 画图
            g2d.setBackground(getColor(displayName));
            g2d.clearRect(0, 0, width, height);

            // 文字
            g2d.setFont(font);
            g2d.setPaint(new Color(255, 255, 255));
            FontMetrics fm = g2d.getFontMetrics(font);
            int strWidth = fm.stringWidth(drawString);
            int strHeight = fm.getHeight();
            int x = (width - strWidth) / 2;

            g2d.drawString(drawString, x, strHeight);

            BufferedImage roundImage = ImageUtil.setRadius(image, width, height, 35);

            g2d.dispose();
            File file = new File(AVATAR_CACHE_ROOT + "/" + displayName + ".png");
            ImageIO.write(roundImage, "png", file);

            return roundImage;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }


    private static Color getColor(String username) {
        int position = username.length() % colorArr.length;
        return colorArr[position];
    }

    public static void saveAvatar(ImageIcon image, String username) {
        saveAvatar(image, username, CUSTOM_AVATAR);
    }

    private static void saveAvatar(ImageIcon image, String username, int type) {
        String path = "";
        if (type == DEFAULT_AVATAR) {
            path = AVATAR_CACHE_ROOT + "/" + username + ".png";
        } else if (type == CUSTOM_AVATAR) {
            path = CUSTOM_AVATAR_CACHE_ROOT + "/" + username + ".png";
        } else {
            throw new RuntimeException("类型不存在");
        }

        File avatarPath = new File(path);

        try {
            if (image != null) {
                BufferedImage bufferedImage = ImageUtil.setRadius(image.getImage(), image.getIconWidth(), image.getIconHeight(), 35);
                ImageIO.write(bufferedImage, "png", avatarPath);
            } else {
                throw new RuntimeException("头像保存失败，数据为空");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取缓存在磁盘的用户头像
     *
     * @param username 用户名
     * @return 头像
     */
    private static Image getCachedImageAvatar(String username) {

        if (customAvatarExist(username)) {
            //缓存在磁盘的自定义的用户头像
            String path = CUSTOM_AVATAR_CACHE_ROOT + "/" + username + ".png";

            return readImage(path);
        } else if (defaultAvatarExist(username)) {
            //缓存在磁盘的默认用户头像
            String path = AVATAR_CACHE_ROOT + "/" + username + ".png";
            return readImage(path);
        } else {
            return null;
        }
    }

    private static BufferedImage readImage(String path) {
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static boolean customAvatarExist(String username) {
        String path = CUSTOM_AVATAR_CACHE_ROOT + "/" + username + ".png";
        File file = new File(path);
        return file.exists();
    }

    public static boolean defaultAvatarExist(String username) {
        String path = AVATAR_CACHE_ROOT + "/" + username + ".png";
        File file = new File(path);
        return file.exists();
    }

    public static void deleteCustomAvatar(String username) {
        String path = CUSTOM_AVATAR_CACHE_ROOT + "/" + username + ".png";

        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void deleteGroupAvatar(String groupName) {
        String path = AVATAR_CACHE_ROOT + "/" + groupName + ".png";
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     *
     */
    /**
     * 创建群头像
     *
     * @param userName   群id
     * @param memberList 成员列表
     * @return 头像
     */
    public static Image createGroupAvatar(String userName, List<Contacts> memberList) {

        try {
            int width = 200;
            int height = 200;

            // 创建BufferedImage对象
            // 选择TYPE_INT_ARGB目的在于可创建透明背景的图，否则圆角外的地方会变成黑色
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            // 获取Graphics2D
            Graphics2D g2d = image.createGraphics();

            // 绘制一个圆角的灰色背景
            g2d.setComposite(AlphaComposite.Src);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Colors.GROUP_AVATAR_BACKGROUND);
            g2d.fill(new RoundRectangle2D.Float(0, 0, width, height, 35, 35));
            g2d.setComposite(AlphaComposite.SrcAtop);

            Rectangle[] rectangles = getSubAvatarPoints(memberList.size());
            int max = Math.min(memberList.size(), 9);
            for (int i = 0; i < max; i++) {
                Contacts member = memberList.get(i);
                String memUserName = member.getUsername();
                ImageIcon orLoadUserAvatar = AvatarUtil.createOrLoadUserAvatar(memUserName);
                g2d.drawImage(orLoadUserAvatar.getImage(), rectangles[i].x, rectangles[i].y, rectangles[i].width, rectangles[i].height, null);
            }

            g2d.dispose();

            // 缓存到磁盘
            File file = new File(AVATAR_CACHE_ROOT + "/" + userName + ".png");
            ImageIO.write(image, "png", file);

            return image;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private static Rectangle[] getSubAvatarPoints(int memberCount) {
        int gap = 8;
        int parentWidth = 200;

        Rectangle[] rectangles = new Rectangle[memberCount];

        int x;
        int y;

        if (memberCount == 1) {
            int childWidth = parentWidth / 2;
            x = (parentWidth - childWidth) / 2;
            rectangles[0] = new Rectangle(x, x, childWidth, childWidth);
        } else if (memberCount == 2) {
            int childWidth = (parentWidth - gap * 3) / 2;

            // 第一个
            y = (parentWidth - childWidth) / 2;
            Rectangle r1 = new Rectangle(gap, y, childWidth, childWidth);

            // 第二个
            x = gap * 2 + childWidth;
            Rectangle r2 = new Rectangle(x, y, childWidth, childWidth);

            rectangles[0] = r1;
            rectangles[1] = r2;
        } else if (memberCount == 3) {
            int childWidth = (parentWidth - gap * 3) / 2;


            // 第一个
            x = (parentWidth - childWidth) / 2;
            y = gap;
            Rectangle r1 = new Rectangle(x, y, childWidth, childWidth);

            // 第二个
            x = gap;
            y = childWidth + gap * 2;
            Rectangle r2 = new Rectangle(x, y, childWidth, childWidth);

            // 第三个
            x = childWidth + gap * 2;
            Rectangle r3 = new Rectangle(x, y, childWidth, childWidth);


            rectangles[0] = r1;
            rectangles[1] = r2;
            rectangles[2] = r3;
        } else if (memberCount == 4) {
            int childWidth = (parentWidth - gap * 3) / 2;


            // 第一个
            Rectangle r1 = new Rectangle(gap, gap, childWidth, childWidth);

            // 第二个
            x = childWidth + gap * 2;
            Rectangle r2 = new Rectangle(x, gap, childWidth, childWidth);

            // 第三个
            x = gap;
            y = childWidth + gap * 2;
            Rectangle r3 = new Rectangle(x, y, childWidth, childWidth);

            // 第四个
            x = childWidth + gap * 2;
            Rectangle r4 = new Rectangle(x, y, childWidth, childWidth);


            rectangles[0] = r1;
            rectangles[1] = r2;
            rectangles[2] = r3;
            rectangles[3] = r4;
        } else if (memberCount == 5) {
            int childWidth = (parentWidth - gap * 4) / 3;

            // 第一个
            x = (parentWidth - childWidth * 2 - gap) / 2;
            Rectangle r1 = new Rectangle(x, x, childWidth, childWidth);

            // 第二个
            y = x;
            x = x + gap + childWidth;
            Rectangle r2 = new Rectangle(x, y, childWidth, childWidth);

            // 第三个
            y = r1.y + gap + childWidth;
            Rectangle r3 = new Rectangle(gap, y, childWidth, childWidth);

            // 第四个
            x = gap * 2 + childWidth;
            Rectangle r4 = new Rectangle(x, y, childWidth, childWidth);

            // 第五个
            x = gap * 3 + childWidth * 2;
            Rectangle r5 = new Rectangle(x, y, childWidth, childWidth);

            rectangles[0] = r1;
            rectangles[1] = r2;
            rectangles[2] = r3;
            rectangles[3] = r4;
            rectangles[4] = r5;
        } else if (memberCount == 6) {
            int childWidth = (parentWidth - gap * 4) / 3;

            // 第一个
            y = (parentWidth - childWidth * 2 - gap) / 2;
            Rectangle r1 = new Rectangle(gap, y, childWidth, childWidth);

            // 第二个
            x = gap * 2 + childWidth;
            Rectangle r2 = new Rectangle(x, y, childWidth, childWidth);

            // 第三个
            x = gap * 3 + childWidth * 2;
            Rectangle r3 = new Rectangle(x, y, childWidth, childWidth);


            // 第四个
            y = r1.y + gap + childWidth;
            Rectangle r4 = new Rectangle(gap, y, childWidth, childWidth);

            // 第五个
            x = gap * 2 + childWidth;
            Rectangle r5 = new Rectangle(x, y, childWidth, childWidth);

            // 第六个
            x = gap * 3 + childWidth * 2;
            Rectangle r6 = new Rectangle(x, y, childWidth, childWidth);

            rectangles[0] = r1;
            rectangles[1] = r2;
            rectangles[2] = r3;
            rectangles[3] = r4;
            rectangles[4] = r5;
            rectangles[5] = r6;
        } else if (memberCount == 7) {
            int childWidth = (parentWidth - gap * 4) / 3;

            // 第一个
            x = (parentWidth - childWidth) / 2;
            Rectangle r1 = new Rectangle(x, gap, childWidth, childWidth);

            // 第二个
            y = gap * 2 + childWidth;
            Rectangle r2 = new Rectangle(gap, y, childWidth, childWidth);

            // 第三个
            x = gap * 2 + childWidth;
            Rectangle r3 = new Rectangle(x, y, childWidth, childWidth);

            // 第四个
            x = gap * 3 + childWidth * 2;
            Rectangle r4 = new Rectangle(x, y, childWidth, childWidth);

            // 第五个
            y = r2.y + childWidth + gap;
            Rectangle r5 = new Rectangle(gap, y, childWidth, childWidth);

            // 第六个
            x = gap * 2 + childWidth;
            Rectangle r6 = new Rectangle(x, y, childWidth, childWidth);

            // 第七个
            x = gap * 3 + childWidth * 2;
            Rectangle r7 = new Rectangle(x, y, childWidth, childWidth);

            rectangles[0] = r1;
            rectangles[1] = r2;
            rectangles[2] = r3;
            rectangles[3] = r4;
            rectangles[4] = r5;
            rectangles[5] = r6;
            rectangles[6] = r7;
        } else if (memberCount == 8) {
            int childWidth = (parentWidth - gap * 4) / 3;

            // 第一个
            x = (parentWidth - childWidth * 2 - gap) / 2;
            Rectangle r1 = new Rectangle(x, gap, childWidth, childWidth);

            // 第二个
            x = x + gap + childWidth;
            Rectangle r2 = new Rectangle(x, gap, childWidth, childWidth);

            // 第三个
            y = gap * 2 + childWidth;
            Rectangle r3 = new Rectangle(gap, y, childWidth, childWidth);

            // 第四个
            x = gap * 2 + childWidth;
            Rectangle r4 = new Rectangle(x, y, childWidth, childWidth);

            // 第五个
            x = gap * 3 + childWidth * 2;
            Rectangle r5 = new Rectangle(x, y, childWidth, childWidth);

            // 第六个
            y = r3.y + childWidth + gap;
            Rectangle r6 = new Rectangle(gap, y, childWidth, childWidth);

            // 第七个
            x = gap * 2 + childWidth;
            Rectangle r7 = new Rectangle(x, y, childWidth, childWidth);

            // 第八个
            x = gap * 3 + childWidth * 2;
            Rectangle r8 = new Rectangle(x, y, childWidth, childWidth);


            rectangles[0] = r1;
            rectangles[1] = r2;
            rectangles[2] = r3;
            rectangles[3] = r4;
            rectangles[4] = r5;
            rectangles[5] = r6;
            rectangles[6] = r7;
            rectangles[7] = r8;
        } else if (memberCount >= 9) {
            int childWidth = (parentWidth - gap * 4) / 3;

            int index = 0;
            for (int i = 1; i <= 3; i++) {
                y = gap * i + (i - 1) * childWidth;

                for (int j = 1; j <= 3; j++) {
                    x = gap * j + (j - 1) * childWidth;
                    Rectangle r = new Rectangle(x, y, childWidth, childWidth);

                    rectangles[index++] = r;
                }
            }
        }

        return rectangles;
    }

    /**
     * 后台加载头像
     * @param userName 用户名
     * @param avatar 头像标签
     */
    public static void loadAvatar(String userName,JLabel avatar){
        // 头像
        new SwingWorker<Object,Object>(){
            ImageIcon orLoadAvatar = null;
            @Override
            protected Object doInBackground() throws Exception {
                orLoadAvatar = AvatarUtil.createOrLoadUserAvatar(userName);
                return null;
            }

            @Override
            protected void done() {
                if (orLoadAvatar != null){
                    avatar.setIcon(orLoadAvatar);
                }
            }
        }.execute();
    }
}
