package cn.shu.wechat.swing.worker;

import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.swing.frames.MainFrame;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.swing.utils.IconUtil;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/**
 * 用于异步加载头像并设置至指定 JLabel 的 SwingWorker 实现。
 * 支持用户、群成员头像，支持默认图占位，支持完成后回调。
 *
 * 使用方式示例：
 * new HeadLoadingSwingWorker(label, user).onAvatarReady(icon -> { ... }).big().executeEx();
 */
public class HeadLoadingSwingWorker extends SwingWorker<Object, Object> {

    // UI 组件引用，用于设置头像
    private final JLabel avatarLabel;

    // 用户名（普通用户或群）
    private final String userName;

    // 群成员名（可为 null）
    private final String memberName;

    // 下载或加载成功的头像
    private ImageIcon avatar;

    // 是否加载大尺寸头像（影响默认图）
    private volatile boolean big;
    //加载大头像必穿
    private Contacts bigContacts;
    private int bigAvatarHeight;
    private int bigAvatarWidth;

    // 加载完成时的回调（可选）
    private Consumer<ImageIcon> onAvatarReady;



    /**
     * 构造方法 - 群成员头像加载
     * @param avatarLabel Swing UI 标签组件
     * @param userName 群名（或群 ID）
     * @param memberName 群成员用户名
     */
    public HeadLoadingSwingWorker(JLabel avatarLabel, String userName, String memberName) {
        this.avatarLabel = avatarLabel;
        this.userName = userName;
        this.memberName = memberName;
    }

    /**
     * 构造方法 - 普通用户或群组头像加载
     * @param avatarLabel Swing UI 标签组件
     * @param userName 用户名或群名
     */
    public HeadLoadingSwingWorker(JLabel avatarLabel, String userName) {
        this(avatarLabel, userName, null);
    }

    /**
     * 构造方法 - 普通用户或群组或群成员大头像加载
     * @param avatarLabel Swing UI 标签组件
     * @param contacts 用户或群或群成员
     */
    public HeadLoadingSwingWorker(JLabel avatarLabel, Contacts contacts) {
        this(avatarLabel, null, null);
        this.bigContacts = contacts;
    }

    /**
     * 设置是否使用大尺寸默认头像（默认 false，小图）
     * 支持链式调用
     */
    public HeadLoadingSwingWorker big(int bigAvatarHeight, int bigAvatarWidth) {
        big = true;
        if (bigContacts == null) {
            throw new RuntimeException("参数缺失：bigContacts");
        }
        this.bigAvatarHeight = bigAvatarHeight;
        this.bigAvatarWidth = bigAvatarWidth;
        return this;
    }

    /**
     * 设置头像加载完成时的回调处理（如缓存、日志、UI 刷新等）
     * 支持链式调用
     */
    public HeadLoadingSwingWorker onAvatarReady(Consumer<ImageIcon> callback) {
        this.onAvatarReady = callback;
        return this;
    }

    /**
     * 后台线程执行：加载或下载头像（可能从本地或远程）
     */
    @Override
    protected Object doInBackground() throws Exception {
        if (big){
                //加载用户大大头像
            avatar =
                    new ImageIcon(AvatarUtil.createOrLoadBigAvatar(bigContacts)
                            .getScaledInstance(bigAvatarWidth,bigAvatarHeight, Image.SCALE_SMOOTH));

        }
        else if (memberName == null) {
            avatar = AvatarUtil.createOrLoadUserAvatar(userName);
        } else {
            avatar = AvatarUtil.createOrLoadMemberAvatar(userName, memberName);
        }
        return null;
    }

    /**
     * 任务完成后回到 EDT（事件派发线程）执行
     * 安全地更新 UI 并触发回调
     */
    @Override
    protected void done() {
        if (avatar != null) {
            if (avatarLabel != null) {
                avatarLabel.setIcon(avatar);
            }
            if (onAvatarReady != null) {
                onAvatarReady.accept(avatar);
            }
        }
    }

    /**
     * 启动头像加载逻辑（包含本地缓存检查）
     * 若缓存存在则直接加载，否则使用默认图并异步加载
     */
    public void loadAvatar() {
        if (avatarLabel != null) {
            if (tryLoadFromCache()) return; // 本地存在头像，立即设置
            avatarLabel.setIcon(getDefaultIcon()); // 设置默认图占位
        }
        this.execute(); // 启动 SwingWorker 异步加载

    }

    /**
     * 判断头像是否已存在于本地缓存并加载
     * @return true 表示缓存命中，false 表示需异步加载
     */
    private boolean tryLoadFromCache() {
        if (memberName == null) {
            if (AvatarUtil.avatarExists(userName)) {
                avatarLabel.setIcon(AvatarUtil.createOrLoadUserAvatar(userName));
                return true;
            }
        } else {
            if (AvatarUtil.avatarExists(memberName)) {
                avatarLabel.setIcon(AvatarUtil.createOrLoadMemberAvatar(userName, memberName));
                return true;
            }
        }
        return false;
    }

    /**
     * 获取默认头像图标（根据 big 字段确定尺寸）
     * @return 默认头像图标
     */
    private ImageIcon getDefaultIcon() {
        if (big) {
            return IconUtil.getIcon(MainFrame.getContext(), "/image/default_head.png",bigAvatarWidth,bigAvatarHeight);
        } else {
            return IconUtil.getIcon(MainFrame.getContext(), "/image/default_head.png",
                    AvatarUtil.NORMAL_AVATAR_SIZE, AvatarUtil.NORMAL_AVATAR_SIZE);
        }
    }
}
