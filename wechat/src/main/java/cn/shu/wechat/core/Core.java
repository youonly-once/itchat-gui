package cn.shu.wechat.core;

import cn.shu.wechat.beans.pojo.Contacts;
import cn.shu.wechat.enums.parameters.BaseParaEnum;

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

public class Core {

    /**
     * 在线状态
     */
    static private volatile boolean alive = false;

    public static boolean isCancelPreLogin() {
        return cancelPreLogin;
    }

    public static void setCancelPreLogin(boolean cancelPreLogin) {
        Core.cancelPreLogin = cancelPreLogin;
    }

    /**
     * 取消登录
     */
    static private boolean cancelPreLogin = false;
    /**
     * 本次登录用户唯一标识
     */
    static private String userName;

    /**
     * 当前登录用户昵称
     */
    static private String nickName;

    /**
     * 登陆账号自身信息
     */
    static private Contacts userSelf;

    /**
     * 好友+群聊+公众号+特殊账号
     */

    static private Map<String, Contacts> memberMap = new ConcurrentHashMap<>(1024);


    /**
     * 好友
     */
    static private Map<String, Contacts> contactMap = new ConcurrentHashMap<>(1024);

    /**
     * 群
     */
    static private Map<String, Contacts> groupMap = new ConcurrentHashMap<>(32);

    /**
     * 群聊成员字典
     */
/*
    static private Map<String, JSONArray> groupMemberMap = new HashMap<String, JSONArray>();
*/

    /**
     * 公众号／服务号
     */
    static private Map<String, Contacts> publicUsersMap = new ConcurrentHashMap<>(64);

    /**
     * 特殊账号
     */
    static private Map<String, Contacts> specialUsersMap = new ConcurrentHashMap<>(0);


    /**
     * 用户头像
     */
    static private Map<String, Image> userHeadImage = new ConcurrentHashMap<>(1024);
    /**
     * 群ID列表
     */
    static private Set<String> groupIdSet = new CopyOnWriteArraySet<>();

    /**
     * 用户信息
     */
/*
    static private Map<String, JSONObject> userInfoMap = new HashMap<>();
*/

    /**
     * 登录信息
     */
    static private Map<String, Object> loginInfoMap = new ConcurrentHashMap<String, Object>(12);

    /**
     * 所有好友最新头像路径
     * 多线程下载 用安全ConcurrentHashMap
     */
    static private Map<String, String> contactHeadImgPath = new ConcurrentHashMap<>();


    /**
     * 所有好友最新头像路径
     * 多线程下载 用安全ConcurrentHashMap
     */
    static private Set<String> recentContacts = new CopyOnWriteArraySet<>();

    public static Set<String> getRecentContacts() {
        return recentContacts;
    }

    public static void setRecentContacts(Set<String> recentContacts) {
        Core.recentContacts = recentContacts;
    }

    /**
     * 扫描登录前需获取的UUID
     */
    static private String uuid = null;

    /**
     * 消息同步失败重试次数
     */
    static private int receivingRetryCount = 5;

    /**
     * 最后一次收到正常retcode的时间，秒为单位
     */
    static private long lastNormalRetCodeTime;

    /**
     * 请求参数
     */
    public static Map<String, Object> getParamMap() {

        return new ConcurrentHashMap<String, Object>(1) {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                Map<String, String> map = new HashMap<String, String>();
                for (BaseParaEnum baseRequest : BaseParaEnum.values()) {
                    map.put(baseRequest.para(), Core.getLoginInfoMap().get(baseRequest.value()).toString());
                }
                put("BaseRequest", map);
            }
        };
    }


    public static boolean isAlive() {
        return alive;
    }

    public static void setAlive(boolean alive) {
        Core.alive = alive;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        Core.userName = userName;
    }

    public static String getNickName() {
        return nickName;
    }

    public static void setNickName(String nickName) {
        Core.nickName = nickName;
    }

    public static Contacts getUserSelf() {
        return userSelf;
    }

    public static void setUserSelf(Contacts userSelf) {
        Core.userSelf = userSelf;
    }

    public static Map<String, Contacts> getMemberMap() {
        return memberMap;
    }

    public static void setMemberMap(Map<String, Contacts> memberMap) {
        Core.memberMap = memberMap;
    }

    public static Map<String, Contacts> getContactMap() {
        return contactMap;
    }

    public static void setContactMap(Map<String, Contacts> contactMap) {
        Core.contactMap = contactMap;
    }

    public static Map<String, Contacts> getGroupMap() {
        return groupMap;
    }

    public static void setGroupMap(Map<String, Contacts> groupMap) {
        Core.groupMap = groupMap;
    }

 /*   public static Map<String, JSONArray> getGroupMemberMap() {
        return groupMemberMap;
    }

    public static void setGroupMemberMap(Map<String, JSONArray> groupMemberMap) {
        Core.groupMemberMap = groupMemberMap;
    }*/

    public static Map<String, Contacts> getPublicUsersMap() {
        return publicUsersMap;
    }

    public static void setPublicUsersMap(Map<String, Contacts> publicUsersMap) {
        Core.publicUsersMap = publicUsersMap;
    }

    public static Map<String, Contacts> getSpecialUsersMap() {
        return specialUsersMap;
    }

    public static void setSpecialUsersMap(Map<String, Contacts> specialUsersMap) {
        Core.specialUsersMap = specialUsersMap;
    }

    public static Set<String> getGroupIdSet() {
        return groupIdSet;
    }

    public static void setGroupIdSet(Set<String> groupIdSet) {
        Core.groupIdSet = groupIdSet;
    }



/*    public static Map<String, JSONObject> getUserInfoMap() {
        return userInfoMap;
    }*/

/*    public static void setUserInfoMap(Map<String, JSONObject> userInfoMap) {
        Core.userInfoMap = userInfoMap;
    }*/

    public static Map<String, Object> getLoginInfoMap() {
        return loginInfoMap;
    }

    public static void setLoginInfoMap(Map<String, Object> loginInfoMap) {
        Core.loginInfoMap = loginInfoMap;
    }

    public static Map<String, String> getContactHeadImgPath() {
        return contactHeadImgPath;
    }

    public static void setContactHeadImgPath(Map<String, String> contactHeadImgPath) {
        Core.contactHeadImgPath = contactHeadImgPath;
    }

    public static String getUuid() {
        return uuid;
    }

    public static void setUuid(String uuid) {
        Core.uuid = uuid;
    }

    public static int getReceivingRetryCount() {
        return receivingRetryCount;
    }

    public static void setReceivingRetryCount(int receivingRetryCount) {
        Core.receivingRetryCount = receivingRetryCount;
    }

    public static long getLastNormalRetCodeTime() {
        return lastNormalRetCodeTime;
    }

    public static void setLastNormalRetCodeTime(long lastNormalRetCodeTime) {
        Core.lastNormalRetCodeTime = lastNormalRetCodeTime;
    }

    public static Map<String, Image> getUserHeadImage() {
        return userHeadImage;
    }
}
