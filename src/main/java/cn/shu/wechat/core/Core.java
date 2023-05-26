package cn.shu.wechat.core;

import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.entity.Contacts;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


/**
 * 核心存储类，全局只保存一份，单例模式
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年4月23日 下午2:33:56
 */
@Getter
@Setter
public class Core {

    /**
     * 在线状态
     */
    @Getter @Setter static private volatile boolean alive = false;

    /**
     * 本次登录用户唯一标识
     */
    @Getter @Setter static private String userName;

    /**
     * 当前登录用户昵称
     */
    @Getter @Setter static private String nickName;

    /**
     * 登陆账号自身信息
     */
    @Getter @Setter static private Contacts userSelf;

    /**
     * 好友+群聊+公众号+特殊账号
     */

    @Getter @Setter static private Map<String, Contacts> memberMap = new ConcurrentHashMap<>(1024);


    /**
     * 好友
     */
    @Getter @Setter
    static private Map<String, Contacts> contactMap = new ConcurrentHashMap<>(1024);

    /**
     * 群
     */
    @Getter @Setter
    static private Map<String, Contacts> groupMap = new ConcurrentHashMap<>(32);

    /**
     * 公众号／服务号
     */
    @Getter @Setter
    static private Map<String, Contacts> publicUsersMap = new ConcurrentHashMap<>(64);

    /**
     * 特殊账号
     */
    @Getter @Setter
    static private Map<String, Contacts> specialUsersMap = new ConcurrentHashMap<>(0);


    /**
     * 用户头像
     */
    @Getter @Setter
    static private Map<String, Image> userHeadImage = new ConcurrentHashMap<>(1024);
    /**
     * 群ID列表
     */
    @Getter @Setter
    static private Set<String> groupIdSet = new CopyOnWriteArraySet<>();


    /**
     * 登录信息
     *
     */
    @Getter @Setter
    static private LoginResultData loginResultData = new LoginResultData();



    /**
     * 所有好友最新头像路径
     * 多线程下载 用安全ConcurrentHashMap
     */
    @Getter @Setter
    static private Map<String, String> contactHeadImgPath = new ConcurrentHashMap<>();


    /**
     * 所有好友最新头像路径
     * 多线程下载 用安全ConcurrentHashMap
     */
    @Getter @Setter
    static private Set<String> recentContacts = new CopyOnWriteArraySet<>();



    /**
     * 扫描登录前需获取的UUID
     */
    @Getter @Setter
    static private String uuid = null;

    /**
     * 消息同步失败重试次数
     */
    @Getter @Setter
    static private int receivingRetryCount = 5;

    /**
     * 最后一次收到正常retcode的时间，秒为单位
     */
    @Getter @Setter
    static private long lastNormalRetCodeTime;


}
