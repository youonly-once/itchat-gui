package cn.shu.wechat.utils;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.AttrHistory;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import cn.shu.wechat.swing.components.Colors;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.task.DownloadTask;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 头像创建工具类
 * @author SXS
 */
@Log4j2
public final class AvatarUtil {
    private static final Color[] colorArr= new Color[]{
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
    };;
    private AvatarUtil() {}


    private static final String AVATAR_CACHE_ROOT;
    private static final String CUSTOM_AVATAR_CACHE_ROOT;
    private static final int DEFAULT_AVATAR = 0;
    private static final int CUSTOM_AVATAR = 1;
    public static final int NORMAL_AVATAR_SIZE = 40;
    public static final int BIG_AVATAR_SIZE = 200;

    /**
     * 小头像缓存 userName,ImageIcon
     */

    private static final Map<String, CompletableFuture<ImageIcon>> avatarCache = new ConcurrentHashMap<>();

    //只为消除重复加载的隐患 不做缓存 下载完成就清理
    private static final ConcurrentHashMap<String, CompletableFuture<Image>> bigAvatarCache = new ConcurrentHashMap<>();


    public static void invalidateAvatarCache(){
        avatarCache.clear();
        bigAvatarCache.clear();
    }

    static {
        AVATAR_CACHE_ROOT = initDirectory(WechatConfiguration.getInstance().getBasePath() + "/cache/avatar", "头像缓存目录");
        CUSTOM_AVATAR_CACHE_ROOT = initDirectory(AVATAR_CACHE_ROOT + "/custom", "用户自定义头像缓存目录");
    }

    /**
     * 初始化目录，如果不存在则创建，并输出日志
     *
     * @param path 路径字符串
     * @param description 日志描述
     * @return 最终路径
     */
    private static String initDirectory(String path, String description) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("创建{}：{}", description, dir.getAbsolutePath());
            } else {
                log.warn("创建{}失败：{}", description, dir.getAbsolutePath());
            }
        }
        return dir.getAbsolutePath();
    }


    /**
     * 获取 小 模糊头像
     *
     * @param user
     * @return
     */
    private static ImageIcon getFuzzUpAvatar(Contacts user) {
        Image avatar = createAvatar(ContactsTools.getContactDisplayNameByUserName(user),NORMAL_AVATAR_SIZE,NORMAL_AVATAR_SIZE);
        return new ImageIcon(avatar);
    }

    /**
     * 获取 大 模糊头像
     *
     * @param user
     * @return
     */
    private static Image getFuzzUpBigAvatar(Contacts user) {

        return createAvatar(ContactsTools.getContactDisplayNameByUserName(user),BIG_AVATAR_SIZE,BIG_AVATAR_SIZE);
    }

    /**
     * 获取用户头像
     *
     * @param user 用户名
     */
    private static ImageIcon getOrDownloadUserAvatar(Contacts user) {

        if (user == null) {
            return IconUtil.getIcon(MainFrame.getContext(), "/image/default_head.png",NORMAL_AVATAR_SIZE,NORMAL_AVATAR_SIZE);
        }
        //获取模糊头像
        //下载头像
        try{
            CompletableFuture<ImageIcon> future =  avatarCache.computeIfAbsent(user.getUsername(), userName -> {
                    return CompletableFuture.supplyAsync(() -> {

                        //获取模糊头像
                        if (WechatConfiguration.getInstance().getFuzzUpAvatar()) {
                            return getFuzzUpAvatar(user);
                        }
                        Pattern pattern = Pattern.compile("webwxgeticon\\?[^#]*?\\bseq=(\\d+)\\b");
                        Matcher matcher = pattern.matcher(user.getHeadimgurl());
                        if (matcher.find()) {
                            String seqValue = matcher.group(1); // 捕获组1是seq值
                            if (Integer.parseInt(seqValue) == 0) {
                                //seq为0 通常下载失败 节约资源
                                return null;
                            }
                        }
                        //下载头像
                        DownloadTask<Image> downloadTask = new DownloadTask<>();
                        if (StringUtils.isNotEmpty((user.getHeadimgurl()))) {
                            downloadTask.setRelativeUrl(user.getHeadimgurl());
                            downloadTask.setTaskId(user.getHeadimgurl());
                            downloadTask.setType(DownloadType.ByRelativeUrl);
                        } else {
                            downloadTask.setUserName(user.getUsername());
                            downloadTask.setTaskId(user.getUsername());
                            downloadTask.setType(DownloadType.RESOURCE_BY_USERNAME);
                        }
                        Image avatar = DownloadManager.submitAwait(downloadTask);
                        if (avatar == null) {
                            return null;
                        }
                        try {
                            return getIconByImage( avatar);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).whenComplete((result, ex) -> {
                        // 结果为null或发生异常时，从缓存移除，允许后续重试
                        if (ex != null) {
                            log.error("头像缓存获取失败：{}", user, ex);
                            avatarCache.remove(userName);
                        }else if (result == null) {
                            log.error("头像缓存获取失败，result is null");
                            avatarCache.remove(userName);
                        }
                    });
                });

            ImageIcon icon = future.get();
            return icon != null ? icon : getFuzzUpAvatar(user);

        } catch (Exception e) {
             log.error("头像缓存获取失败：{}", user, e);
            avatarCache.remove(user.getUsername());
            return getFuzzUpAvatar(user); // 兜底策略
        }

    }

    /**
     * 创建或读取群成员头像
     *
     * @param userName 用户名也是房间id
     * @return 头像
     */
    public static ImageIcon createOrLoadMemberAvatar(String groupName, String userName) {
        Contacts member = ContactsTools.getMemberOfGroup(groupName, userName);
        return getOrDownloadUserAvatar(member);
    }

    /**
     * 创建或读取群成员头像
     *
     * @param user 用户也是房间id
     * @return 头像
     */
    public static Image createOrLoadBigAvatar(Contacts user) {
        if (user == null) {
            log.error("user is null.");
            return IconUtil.getBufferedImage(MainFrame.getContext(), "/image/default_head.png");
        }

        String userName = user.getUsername();

        try {
            CompletableFuture<Image> future = bigAvatarCache.computeIfAbsent(userName, key ->
                    CompletableFuture.supplyAsync(() -> {

                            if (WechatConfiguration.getInstance().getFuzzUpAvatar()) {
                                return getFuzzUpBigAvatar(user);
                            }

                            String url = user.getHeadimgurl();
                            String filePath = Core.getContactHeadImgPath().get(userName);
                            BufferedImage bufferedImage = null;

                            // URL 下载尝试
                            if (url != null && url.startsWith("http")) {
                                bufferedImage = getBufferedImageByUrl(url);
                                if (bufferedImage != null) {
                                    return bufferedImage;
                                }
                            }

                            // 本地缓存文件尝试
                            if (StringUtils.isNotEmpty(filePath)) {
                                bufferedImage = getBufferedImageByPath(filePath);
                                if (bufferedImage != null) {
                                    return bufferedImage;
                                }
                            }

                            // 下载任务
                            DownloadTask<String> downloadTask = new DownloadTask<>(url, userName, null);
                            downloadTask.setTaskId("bigAvatar："+url);
                            downloadTask.setType(DownloadType.HEAD_IMAGE_BIG);
                            filePath = DownloadManager.submitAwait(downloadTask);

                            if (filePath != null) {
                                bufferedImage = getBufferedImageByPath(filePath);
                                if (bufferedImage != null) {
                                    Core.getContactHeadImgPath().put(userName, filePath);
                                    return bufferedImage;
                                }
                            }

                            // 兜底模糊头像
                            return getFuzzUpBigAvatar(user);
                    })
            );

            Image image = future.get();
            return image != null ? image : getFuzzUpBigAvatar(user);

        } catch (Exception e) {
            log.error("头像缓存获取失败：{}", userName, e);
            return getFuzzUpBigAvatar(user); // 兜底策略
        }finally {
            bigAvatarCache.remove(userName); // 正常或异常都清理
        }
    }

    private static BufferedImage getBufferedImageByUrl(String url) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(URI.create(url).toURL());
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return bufferedImage;
    }

    private static BufferedImage getBufferedImageByPath(String path) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(new File(path));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return bufferedImage;
    }

    /**
     * 获取用户头像
     *
     * @param userName 用户名
     * @return 头像
     */
    public static ImageIcon createOrLoadUserAvatar(String userName) {
        Contacts contacts = Core.getMemberMap().get(userName);
        return getOrDownloadUserAvatar(contacts);
    }

    /**
     * 判断头像是否加载
     * @param userName
     * @return
     */
    public static boolean avatarExists(String userName){
        if (userName == null) {
            return false;
        }
        return avatarCache.containsKey(userName);
    }

    public static ImageIcon getIconByImage(Image image) throws IOException {
        // 圆角处理（假设返回一定是 BufferedImage）
        BufferedImage rounded = IconUtil.setRadius(image, image.getWidth(null), image.getHeight(null), 35);

        // 创建最终缩放图像
        BufferedImage finalImage = new BufferedImage(NORMAL_AVATAR_SIZE, NORMAL_AVATAR_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = finalImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(rounded, 0, 0, NORMAL_AVATAR_SIZE, NORMAL_AVATAR_SIZE, null);
        g2.dispose();

        return new ImageIcon(finalImage);
    }
    /**
     * 添加用户头像
     *
     * @param username 用户名
     * @param image    头像
     */
    public static void putUserAvatarCache(String username, Image image) {
        try {
             avatarCache.put(username, CompletableFuture.supplyAsync(() -> {
                 try {
                     return getIconByImage(image);
                 } catch (IOException e) {
                     throw new RuntimeException(e);
                 }
             }).whenComplete((result, ex) -> {
                // 结果为null或发生异常时，从缓存移除，允许后续重试
                if (ex != null) {
                    log.error("更新头像缓存失败：{}", username, ex);
                    avatarCache.remove(username);
                }else if (result == null) {
                    log.error("更新头像缓存失败，result is null：{}", username);
                    avatarCache.remove(username);
                }
            }));
        } catch (Exception e) {
            log.error("更新头像缓存失败：{}", username, e);
            avatarCache.remove(username);
        }
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
                log.error(e.getMessage(),e);
            }
        }

    }

    /**
     * 创建头像
     *
     * @param displayName 显示名称
     * @return 头像
     */
    private static Image createAvatar(String displayName,int width, int height) {
        String drawString;
        //取前几位绘制头像
        if (displayName.length() > 1) {
            drawString = displayName.substring(0, 1).toUpperCase() + displayName.substring(1, 2).toLowerCase();
        } else {
            drawString = displayName;
        }

        try {

            // 创建BufferedImage对象
            Font font = FontUtil.getDefaultFont(width/2, Font.PLAIN);
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

            BufferedImage roundImage = IconUtil.setRadius(image, width, height, width / 3);

            g2d.dispose();


            return roundImage;
        } catch (Exception ex) {
            log.error(ex.getMessage());
        }

        Image scaledInstance = IconUtil.getBufferedImage(MainFrame.getContext(), "/image/default_head.png");
        if (scaledInstance != null) {
            IconUtil.getScaledImage(scaledInstance, NORMAL_AVATAR_SIZE, NORMAL_AVATAR_SIZE);
        }
        return scaledInstance;
    }


    private static Color getColor(String username) {
        int position = username.length() % colorArr.length;
        return colorArr[position];
    }




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
     * 删除下载的失效头像
     */
    public static void deleteLoseEfficacyHeadImg(String imgPath) {
        AttrHistoryMapper attrHistoryMapper = SpringContextHolder.getBean(AttrHistoryMapper.class);
        List<AttrHistory> headImageList = attrHistoryMapper
                .selectByAll(AttrHistory.builder()
                        .attr("头像更换")
                        .build());
        HashSet<String> headImages = new HashSet<>();
        for (AttrHistory attrHistory : headImageList) {
            headImages.add(attrHistory.getNewval());
            headImages.add(attrHistory.getOldval());
        }
        deleteFile(imgPath, headImages);
        log.info("头像删除成功");

    }

    /**
     * 遍历删除文件
     *
     * @param imgPath    目录
     * @param headImages 不删除列表
     */
    private static void deleteFile(String imgPath, HashSet<String> headImages) {
        File file = new File(imgPath);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                return;
            }
            for (File file1 : files) {
                if (file1.isFile()) {
                    if (!headImages.contains(file1.getAbsolutePath())) {
                        file1.delete();
                    }

                } else if (file1.isDirectory()) {
                    deleteFile(file1.getAbsolutePath(), headImages);
                }

            }
        }
    }
}
