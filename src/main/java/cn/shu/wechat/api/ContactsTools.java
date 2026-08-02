package cn.shu.wechat.api;


import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.constant.WxConstant;
import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.AttrHistory;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.entity.Status;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import cn.shu.wechat.mapper.StatusMapper;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.task.DownloadTask;
import cn.shu.wechat.utils.AvatarUtil;
import cn.shu.wechat.utils.EmojiUtil;
import cn.shu.wechat.utils.IconUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 微信联系人工具，如获好友昵称、备注等
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年5月4日 下午10:49:16
 */
@Log4j2
public class ContactsTools {


    public static final Map<String, String> attributeMap = new HashMap<>();
    public static final Map<String, String> attributeReverseMap;
    private static final Pattern pattern = Pattern.compile(".+\\?seq=(\\d+).+");
    private static final AttrHistoryMapper attrHistoryMapper = SpringContextHolder.getBean(AttrHistoryMapper.class);

    private static final WechatConfiguration configuration = SpringContextHolder.getBean(WechatConfiguration.class);
    private static final StatusMapper statusMapper = SpringContextHolder.getBean(StatusMapper.class);

    static {
        attributeMap.put("性别", "sex");
        attributeMap.put("城市", "city");
        attributeMap.put("省份", "province");
        attributeMap.put("用户名", "username");
        attributeMap.put("昵称", "nickname");
        attributeMap.put("签名", "signature");
        attributeMap.put("备注名", "remarkname");
        attributeMap.put("群ID", "chatroomid");
        attributeMap.put("状态", "status");
        attributeMap.put("拼音全拼", "pyquanpin");
        attributeMap.put("加密群ID", "encrychatroomid");
        attributeMap.put("显示名", "displayname");
        attributeMap.put("验证标志", "verifyflag");
        attributeMap.put("统一好友", "unifriend");
        attributeMap.put("联系人标志", "contactflag");
        attributeMap.put("成员列表", "memberlist");
        attributeMap.put("星标好友", "starfriend");
        attributeMap.put("头像URL", "headimgurl");
        attributeMap.put("应用账号标志", "appaccountflag");
        attributeMap.put("成员数", "membercount");
        attributeMap.put("备注首字母", "remarkpyinitial");
        attributeMap.put("社交标志", "snsflag");
        attributeMap.put("别名", "alias");
        attributeMap.put("关键词", "keyword");
        attributeMap.put("隐藏输入栏标志", "hideinputbarflag");
        attributeMap.put("备注拼音全拼", "remarkpyquanpin");
        attributeMap.put("用户ID", "uin");
        attributeMap.put("群主ID", "owneruin");
        attributeMap.put("是否群主", "isowner");
        attributeMap.put("拼音首字母", "pyinitial");
        attributeMap.put("票据", "ticket");
        attributeMap.put("是否互为好友", "mutualCreate");
        attributeMap.put("类型", "type");
        attributeMap.put("是否联系人", "iscontacts");
        attributeMap.put("头像图标", "avatarIcon");
        attributeMap.put("群名称", "groupName");
        attributeReverseMap = attributeMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getValue,
                        Map.Entry::getKey
                ));
    }
    /**
     * 根据用户名获取用户信息
     *
     * @param userName 用户UserName
     * @return 用户信息
     */
    public static Contacts getContactByUserName(String userName) {
        Map<String, Contacts> contactMap = Core.getMemberMap();
        return contactMap.getOrDefault(userName, null);

    }

    /**
     * 根据用户名获取用户显示名称
     * 有备注显示备注，无备注显示昵称
     * 群则直接显示昵称
     *
     * @param userName 用户UserName
     * @return 备注
     */
    public static String getContactDisplayNameByUserName(String userName) {
        String remarkNameByPersonUserName = getContactRemarkNameByUserName(userName);
        if (StringUtils.isNotEmpty(remarkNameByPersonUserName)) {
            return remarkNameByPersonUserName;
        }
        String nickNameByPersonUserName = getContactNickNameByUserName(userName);
        if (StringUtils.isNotEmpty(nickNameByPersonUserName)) {
            return nickNameByPersonUserName;
        }
        String groupDefaultName = getGroupDefaultName(userName);
        if (StringUtils.isNotEmpty(groupDefaultName)) {
            return groupDefaultName;
        }
        return userName;
    }

    /**
     * 根据用户名获取用户显示名称
     * 有备注显示备注，无备注显示昵称
     * 群则直接显示昵称
     *
     * @param contacts 用户U
     * @return 备注
     */
    public static String getContactDisplayNameByUserName(Contacts contacts) {
        String name = getContactRemarkNameByUserName(contacts);
        if (StringUtils.isNotEmpty(name)) {
            return name;
        }
        if (StringUtils.isNotEmpty(contacts.getDisplayname())) {
            return contacts.getDisplayname();
        }
        String nickNameByPersonUserName = getContactNickNameByUserName(contacts);
        if (StringUtils.isNotEmpty(nickNameByPersonUserName)) {
            return nickNameByPersonUserName;
        }
        String groupDefaultName = getGroupDefaultName(contacts);
        if (StringUtils.isNotEmpty(groupDefaultName)) {
            return groupDefaultName;
        }
        return contacts.getUsername();
    }

    /**
     * 获取群聊的默认名称
     *
     * @param userName 用户UserName
     * @return 默认名称，如果是群，则以群成员的名称开始
     */
    public static String getGroupDefaultName(String userName) {

        if (userName != null && userName.startsWith("@@")) {

            return Optional.ofNullable(getContactByUserName(userName))
                    .map(Contacts::getMemberlist)
                    .map(memberList -> memberList.stream()
                            .map(Contacts::getNickname)
                            .limit(2)
                            .collect(Collectors.joining(",")))
                    .orElse(null);
        }
        return null;
    }

    /**
     * 获取群聊的默认名称
     *
     * @param contacts 用户UserName
     * @return 默认名称，如果是群，则以群成员的名称开始
     */
    public static String getGroupDefaultName(Contacts contacts) {

        if (contacts != null && contacts.getUsername().startsWith("@@")) {

            return Optional.of(contacts)
                    .map(Contacts::getMemberlist)
                    .map(memberList -> memberList.stream()
                            .map(Contacts::getNickname)
                            .limit(2)
                            .collect(Collectors.joining(",")))
                    .orElse(null);
        }
        return null;
    }

    /**
     * 根据用户名获取用户备注
     *
     * @param userName 用户UserName
     * @return 备注
     */
    public static String getContactRemarkNameByUserName(String userName) {
        if (userName == null) {
            return "";
        }
        //群只有备注 没有昵称
        if (userName.startsWith("@@")) {
            return getContactNickNameByUserName(userName);
        }
        Contacts contactByUserName = getContactByUserName(userName);
        if (contactByUserName == null) {
            return null;
        }
        return EmojiUtil.emojiFormatter(contactByUserName.getRemarkname());
    }

    /**
     * 根据用户名获取用户备注
     *
     * @param contacts 用户UserName
     * @return 备注
     */
    public static String getContactRemarkNameByUserName(Contacts contacts) {
        if (contacts == null) {
            return null;
        }
        //群只有备注 没有昵称
        if (contacts.getUsername().startsWith("@@")) {
            return getContactNickNameByUserName(contacts);
        }
        return EmojiUtil.emojiFormatter(contacts.getRemarkname());
    }
    /**
     * 根据用户名获取普通用户昵称
     *
     * @param userName 用户UserName
     * @return 备注
     */
    public static String getContactNickNameByUserName(String userName) {
        if (userName == null){
            return null;
        }
        Contacts contactByUserName = getContactByUserName(userName);
        if (contactByUserName == null) {
            return null;
        }
        return EmojiUtil.emojiFormatter(contactByUserName.getNickname());
    }
    /**
     * 根据用户名获取普通用户昵称
     *
     * @param contacts 用户UserName
     * @return 备注
     */
    public static String getContactNickNameByUserName(Contacts contacts) {
        if (contacts == null){
            return null;
        }
        return EmojiUtil.emojiFormatter(contacts.getNickname());
    }

    /**
     * 获取群成员
     *
     * @param groupName 群UserName
     * @param userName  成员UserName
     * @return 成员
     */
    public static Contacts getMemberOfGroup(String groupName, String userName) {
        if (StringUtils.isEmpty(userName)){
           return null;
        }
        if (Core.getUserName().equals(userName)) {
            return Core.getUserSelf();
        }
        Optional<Contacts> contacts1 = Optional.ofNullable(groupName)
                .map(Core.getMemberMap()::get)
                .map(Contacts::getMemberlist)
                .flatMap(memberList -> memberList.stream()
                        .filter(contacts -> userName.equals(contacts.getUsername()))
                        .findAny());
        return contacts1.orElse(null);
    }

    /**
     * 查找用户属于哪个群
     *
     * @param userName
     * @return
     */

    public static Contacts getGroupOfMember(String userName) {
        if (StringUtils.isEmpty(userName)) {
            return null;
        }
        if (Core.getUserName().equals(userName)) {
            return Core.getUserSelf();
        }
        return Core.getMemberMap().values().stream()
                .filter(e -> e.getType() == Contacts.ContactsType.GROUP_USER)
                .filter(e ->
                        e.getMemberlist().stream().anyMatch(e1 -> e1.getUsername().equals(userName))
                ).limit(1).findAny().orElse(null);

    }

    /**
     * 获取群成员
     *
     * @param group    群
     * @param userName 成员UserName
     * @return 成员
     */
    public static Contacts getMemberOfGroup(Contacts group, String userName) {
        if (StringUtils.isEmpty(userName)) {
            return null;
        }

        Optional<Contacts> contacts1 = Optional.of(group)
                .map(Contacts::getMemberlist)
                .flatMap(memberList -> memberList.stream()
                        .filter(contacts -> userName.equals(contacts.getUsername()))
                        .findAny());
        return contacts1.orElse(null);
    }

    /**
     * 获取群成员昵称
     *
     * @param groupName 群UserName
     * @param userName  成员UserName
     * @return 成员昵称
     */
    public static String getMemberNickNameOfGroup(String groupName, String userName) {
        Contacts memberOfGroup = getMemberOfGroup(groupName, userName);
        return memberOfGroup != null
                ? EmojiUtil.emojiFormatter(memberOfGroup.getNickname())
                : null;


    }

    /**
     * 获取群成员显示名称
     *
     * @param groupName 群UserName
     * @param userName  成员UserName
     * @return 群成员显示名称
     */
    public static String getMemberDisplayNameOfGroup(String groupName, String userName) {
        Contacts memberOfGroup = getMemberOfGroup(groupName, userName);
        return getMemberDisplayNameOfGroup(memberOfGroup, userName);
    }

    /**
     * 获取群成员显示名称
     *
     * @param group    群
     * @param userName 成员UserName
     * @return 群成员显示名称
     */
    public static String getMemberDisplayNameOfGroupObj(Contacts group, String userName) {
        Contacts memberOfGroup = getMemberOfGroup(group, userName);
        return getMemberDisplayNameOfGroup(memberOfGroup, userName);
    }

    /**
     * 获取群成员显示名称
     *
     * @param memberOfGroup 群
     * @param userName      成员UserName
     * @return 群成员显示名称
     */
    public static String getMemberDisplayNameOfGroup(Contacts memberOfGroup, String userName) {
        if (memberOfGroup == null || userName == null) {
            return "";
        }
            String displayName = memberOfGroup.getRemarkname();
            if (!StringUtils.isEmpty(displayName)) {
                return EmojiUtil.emojiFormatter(displayName);
            }
            displayName = memberOfGroup.getDisplayname();
            if (!StringUtils.isEmpty(displayName)) {
                return EmojiUtil.emojiFormatter(displayName);
            }
            displayName = memberOfGroup.getNickname();
            if (!StringUtils.isEmpty(displayName)) {
                return EmojiUtil.emojiFormatter(displayName);
            }
        return userName;
    }


    public static String getSignatureNameOfGroup(String userName) {
        Contacts contacts = Core.getMemberMap().get(userName);
        if (contacts == null){
            return null;
        }
        return EmojiUtil.emojiFormatter(contacts.getSignature());
    }


    public static String getSignatureNameOfGroup(Contacts contacts) {
        if (contacts == null){
            return null;
        }
        return EmojiUtil.emojiFormatter(contacts.getSignature());
    }

    /**
     * 根据用户名获取用户显示名称对应的拼音
     * 有备注显示备注，无备注显示昵称
     * 群则直接显示昵称
     *
     * @param userName 用户UserName
     * @return 备注
     */
    public static String getContactDisplayNameInitialByUserName(String userName) {
        Contacts contacts = Core.getMemberMap().get(userName);

        if (StringUtils.isNotEmpty(contacts.getRemarkpyinitial())) {
            return contacts.getRemarkpyinitial();
        }
        if (StringUtils.isNotEmpty(contacts.getPyinitial() )) {
            return contacts.getPyinitial();
        }else{
            return "#";
        }
    }

    /**
     * 根据用户名获取用户显示名称对应的拼音
     * 有备注显示备注，无备注显示昵称
     * 群则直接显示昵称
     *
     * @return 备注
     */
    public static String getContactDisplayNameInitialByUserName(Contacts contacts) {


        if (StringUtils.isNotEmpty(contacts.getRemarkpyinitial())) {
            return contacts.getRemarkpyinitial();
        }
        if (StringUtils.isNotEmpty(contacts.getPyinitial())) {
            return contacts.getPyinitial();
        } else {
            return "#";
        }
    }

    /**
     * 是否消息免打扰
     * @param contacts 联系人
     * @return {@code false} 免打扰
     */
    public static boolean isMute(Contacts contacts){
        if (contacts.getType().equals(Contacts.ContactsType.PUBLIC_USER)
                || contacts.getType().equals(Contacts.ContactsType.SPECIAL_USER)) {
            return true;
        } else if (isRoomContact(contacts.getUsername())) {
            return (contacts.getStatus() == null ||
                    contacts.getStatus() == WxConstant.ChatRoomMute.CHATROOM_NOTIFY_CLOSE.CODE
            );
        }else{
            return ((contacts.getContactflag() & WxConstant.ContactFlag.CONTACTFLAG_NOTIFYCLOSECONTACT.CODE) > 0);
        }


    }
    /**
     * 是否消息免打扰
     * @param userName 联系人
     * @return {@code false} 免打扰
     */
    public static boolean isMute(String  userName){

        return isMute(Core.getMemberMap().get(userName));


    }

    /**
     * 是否为群
     * @param userName 用户名
     * @return 是否为群
     */
    public static boolean isRoomContact(String userName){
        return userName.startsWith("@@");
    }

    /**
     * 是否为群
     *
     * @param contacts 用户
     * @return 是否为群
     */
    public static boolean isRoomContact(Contacts contacts) {
        return isRoomContact(contacts.getUsername());
    }

    /**
     * 联系人相关map的put操作
     * put前统计哪些信息变了
     *
     * @param oldGroup 旧值
     * @param newGroup 新值
     */
    public static void compareGroup(Contacts oldGroup, Contacts newGroup) {
        try {
            if (oldGroup == null) {
                return;
            }

            compareContacts(oldGroup, newGroup);
            List<Contacts> oldMemberList = oldGroup.getMemberlist();
            List<Contacts> newMemberList = newGroup.getMemberlist();
            if (oldMemberList.isEmpty() || newMemberList.isEmpty()) {
                log.warn(oldMemberList.isEmpty() ? "oldMemberList is empty." : "newMemberList is empty.");
                return;
            }
            String groupName = ContactsTools.getContactDisplayNameByUserName(oldGroup.getUsername());

            //新旧集合交集 判断更新的内容
            Map<String, Contacts> oldMap = oldMemberList.stream()
                    .collect(Collectors.toMap(Contacts::getUsername, Function.identity(), (a, b) -> b));

            Map<String, Contacts> newMap = newMemberList.stream()
                    .collect(Collectors.toMap(Contacts::getUsername, Function.identity(), (a, b) -> b));


            Set<String> oldKeys = oldMap.keySet();

            Set<String> newKeys = newMap.keySet();

            // 新增的用户
            Set<String> addedIds = new HashSet<>(newKeys);
            addedIds.removeAll(oldKeys);
            if (addedIds.size() > 10) {
                log.error("入群用户{}个，误报！", addedIds);
                return;
            }
            for (String addedId : addedIds) {
                String name = ContactsTools.getMemberDisplayNameOfGroup(newMap.get(addedId), addedId);
                ArrayList<Message> messages = new ArrayList<>();
                messages.add(Message.builder().content("【" + groupName + "】（" + name + "）:加入群聊!")
                        .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.TEXT.getCode())
                        .toUsername("filehelper")
                        .build());
                MessageTools.sendMsgByUserId(messages);
            }
            // 删除的用户
            Set<String> removedIds = new HashSet<>(oldKeys);
            removedIds.removeAll(newKeys);
            if (removedIds.size() > 10) {
                log.error("退群用户{}个，误报！", removedIds);
                return;
            }
            for (String removeId : removedIds) {
                String name = ContactsTools.getMemberDisplayNameOfGroup(oldMap.get(removeId), removeId);
                ArrayList<Message> messages = new ArrayList<>();
                messages.add(Message.builder().content("【" + groupName + "】（" + name + "）:退出群聊!")
                        .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.TEXT.getCode())
                        .toUsername("filehelper")
                        .build());
                MessageTools.sendMsgByUserId(messages);
            }
            //交集
            //oldKeys.removeAll(removedIds);
//            List<Message> messageList = oldKeys.stream()
//                    .map(commonKey -> compareGroupMember(oldMap.get(commonKey), newMap.get(commonKey), oldGroup))
//                    .filter(Objects::nonNull)
//                    .toList();
//
//            if (messageList.size() > 10) {
//                //群成员信息变化
//                log.error("群成员信息变化数量：{}", messageList.size());
//                return;
//            }
//            MessageTools.sendMsgByUserId(messageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }

    /**
     * 联系人相关map的put操作
     * put前统计哪些信息变了
     *
     * @param oldMember 旧值
     * @param newMember 新值
     * @param oldGroup  旧群
     */
    private static Message compareGroupMember(Contacts oldMember, Contacts newMember, Contacts oldGroup) {
        if (oldMember == null) {
            return null;
        }
        //TODO 更改一次头像  有二次seq都不一样，导致重发
        Map<String, Map<String, String>> differenceMap = getDifferenceMap(oldMember, newMember);
        if (differenceMap.isEmpty()) {
            return null;
        }
        //待发消息列表
        String differenceStr = differenceMapToString(differenceMap);
        //获取群成员姓名
        String memberDisplayNameOfGroup = ContactsTools.getMemberDisplayNameOfGroupObj(oldGroup, oldMember.getUsername());
        //获取群昵称
        String groupName = ContactsTools.getContactDisplayNameByUserName(oldGroup);
        String notifyTo = "filehelper";

        Message message = Message.builder().content("群成员信息更改" + "：【" + groupName + "】" + "（" + memberDisplayNameOfGroup + "）属性更新：" + differenceStr)
                .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.TEXT.getCode())
                .toUsername(notifyTo)
                .build();
        log.info("群成员信息更改" + "：【" + groupName + "】" + "（" + memberDisplayNameOfGroup + "）属性更新：" + differenceStr);
        //问题14：差异存到数据库（拆分为三步骤）
        List<Message> messageList = new ArrayList<>(List.of(message));
        List<AttrHistory> attrHistories = buildAttrHistories(differenceMap, oldMember);
        handleAvatarIfNeeded(differenceMap, oldMember, attrHistories, messageList, notifyTo);
        batchSaveAttrHistories(attrHistories);
        // message 本身已在 messageList 首部返回；若 handleAvatar 追加了图片消息，仅用于发送，但 compareGroupMember 返回只返回文本消息
        return message;
    }

    /**
     * 联系人相关map的put操作
     * put前统计哪些信息变了
     *
     * @param oldV 旧值
     * @param newV 新值
     */
    public static void compareContacts(Contacts oldV, Contacts newV) {
        try {
            if (oldV == null) {
                return;
            }
            //TODO 更改一次头像  有二次seq都不一样，导致重发
            Map<String, Map<String, String>> differenceMap = getDifferenceMap(oldV, newV);
            if (differenceMap.isEmpty()) {
                return;
            }
            String s = differenceMapToString(differenceMap);

            String name = ContactsTools.getContactDisplayNameByUserName(newV.getUsername());

            String toUserName  = newV.getUsername();
            Status welcome = statusMapper.selectOne(Wrappers.<Status>lambdaQuery().eq(Status::getKey, "attr_change_notify_to"));
            if (welcome != null && StringUtils.isNotEmpty(welcome.getValue())) {
                toUserName = welcome.getValue();
            }

            ArrayList<Message> messages = new ArrayList<>();
             messages.add(Message.builder().content("普通联系人" + "（" + name + "）属性更新：" + s)
                    .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.TEXT.getCode())
                    .toUsername(toUserName)
                    .build());
            log.info("普通联系人" + "（" + name + "）属性更新：" + s);

            //问题14：差异存到数据库（拆分为三步骤，职责分离；toUsername 统一使用文本消息目标，不再硬编码 filehelper）
            List<AttrHistory> attrHistories = buildAttrHistories(differenceMap, oldV);
            handleAvatarIfNeeded(differenceMap, oldV, attrHistories, messages, toUserName);
            batchSaveAttrHistories(attrHistories);

            MessageTools.sendMsgByUserId(messages);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    /**
     * 问题14拆分1：仅构建 AttrHistory 列表，不做任何 IO
     * 头像分支的 oldval/newval 先填 URL 字符串，真正下载成功后会在 handleAvatarIfNeeded 中替换为本地路径
     *
     * @return AttrHistory 列表
     */
    private static List<AttrHistory> buildAttrHistories(Map<String, Map<String, String>> differenceMap, Contacts oldV) {
        ArrayList<AttrHistory> attrHistories = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            String attrKey = stringMapEntry.getKey();
            for (Map.Entry<String, String> stringStringEntry : stringMapEntry.getValue().entrySet()) {
                // 问题8/8+：统一用"头像URL"或兼容历史"headimgurl"判断；移除不存在的"头像更换"
                boolean isHeadAttr = "头像URL".equalsIgnoreCase(attrKey) || "headimgurl".equalsIgnoreCase(attrKey);
                if (isHeadAttr) {
                    String oldHeadPath = Core.getContactHeadImgPath().get(oldV.getUsername());
                    // 问题9：头像分支 remarkname 使用 oldV.getRemarkname()，不再错填 nickname
                    AttrHistory build = AttrHistory.builder()
                            .attr(attrKey)
                            .oldval(oldHeadPath != null ? oldHeadPath : stringStringEntry.getKey())
                            .newval(stringStringEntry.getValue()) // 先填 URL，后续下载成功再替换为本地路径
                            .id(0)
                            .nickname(oldV.getNickname())
                            .remarkname(oldV.getRemarkname())
                            .username(oldV.getUsername())
                            .createtime(new Date())
                            .build();
                    attrHistories.add(build);
                } else {
                    AttrHistory build = AttrHistory.builder()
                            .attr(attrKey)
                            .oldval(stringStringEntry.getKey())
                            .newval(stringStringEntry.getValue())
                            .id(0)
                            .nickname(oldV.getNickname())
                            .remarkname(oldV.getRemarkname())
                            .username(oldV.getUsername())
                            .createtime(new Date())
                            .build();
                    attrHistories.add(build);
                }
            }
        }
        return attrHistories;
    }

    /**
     * 问题14拆分2：仅处理头像相关逻辑（下载、追加图片消息、更新缓存、回填 AttrHistory 本地路径）
     *
     * @param toUserName 消息接收方，由 compareContacts/compareGroupMember 决定；不再硬编码 filehelper
     */
    private static void handleAvatarIfNeeded(Map<String, Map<String, String>> differenceMap,
                                             Contacts oldV,
                                             List<AttrHistory> attrHistories,
                                             List<Message> messages,
                                             String toUserName) {
        for (Map.Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            String attrKey = stringMapEntry.getKey();
            boolean isHeadAttr = "头像URL".equalsIgnoreCase(attrKey) || "headimgurl".equalsIgnoreCase(attrKey);
            if (!isHeadAttr) {
                continue;
            }
            for (Map.Entry<String, String> stringStringEntry : stringMapEntry.getValue().entrySet()) {
                // 问题13：oldHeadPath 可能 null，IconUtil.getImageSize 前保护
                String oldHeadPath = Core.getContactHeadImgPath().get(oldV.getUsername());
                if (StringUtils.isNotEmpty(oldHeadPath)) {
                    Dimension imageSize = IconUtil.getImageSize(oldHeadPath);
                    if (imageSize != null) {
                        // 问题14：toUsername 使用传入值，不再硬编码 filehelper
                        messages.add(Message.builder()
                                .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.PIC.getCode())
                                .toUsername(toUserName)
                                .imgHeight(imageSize.height)
                                .imgWidth(imageSize.width)
                                .filePath(oldHeadPath).build());
                    }
                } else {
                    log.warn("旧头像路径为空，用户：{}，跳过发送旧头像图片", oldV.getUsername());
                }

                String newHeadPath = null;
                try {
                    DownloadTask<String> downloadTask = new DownloadTask<>();
                    downloadTask.setRelativeUrl(stringStringEntry.getValue());
                    downloadTask.setType(DownloadType.HEAD_IMAGE_BIG);
                    downloadTask.setUserName(oldV.getUsername());
                    downloadTask.setTaskId(stringStringEntry.getValue() + oldV.getUsername());
                    newHeadPath = DownloadManager.submitAwait(downloadTask, 1000 * 60 * 5, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    log.warn("新头像下载失败，用户：{}，URL：{}，原因：{}", oldV.getUsername(), stringStringEntry.getValue(), e.getMessage());
                }

                if (newHeadPath != null) {
                    // 更新缓存
                    Core.getContactHeadImgPath().put(oldV.getUsername(), newHeadPath);
                    AvatarUtil.putUserAvatarCache(oldV.getUsername(), newHeadPath);

                    // 问题13：newHeadPath 下载成功仍需保护 IconUtil
                    Dimension imageSize = IconUtil.getImageSize(newHeadPath);
                    if (imageSize != null) {
                        messages.add(Message.builder()
                                .toUsername(toUserName)
                                .imgHeight(imageSize.height)
                                .imgWidth(imageSize.width)
                                .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.PIC.getCode())
                                .filePath(newHeadPath).build());
                    }

                    // 回填 AttrHistory：newval → 本地路径
                    String finalNewHeadPath = newHeadPath;
                    for (AttrHistory h : attrHistories) {
                        if (("头像URL".equalsIgnoreCase(h.getAttr()) || "headimgurl".equalsIgnoreCase(h.getAttr()))
                                && Objects.equals(h.getUsername(), oldV.getUsername())) {
                            h.setNewval(finalNewHeadPath);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 问题14拆分3：仅负责 batchInsert，异常打印完整堆栈便于排查
     */
    private static void batchSaveAttrHistories(List<AttrHistory> attrHistories) {
        if (CollectionUtils.isEmpty(attrHistories)) {
            return;
        }
        try {
            // 问题7：使用 static final 字段，不再每次 SpringContextHolder.getBean
            attrHistoryMapper.batchInsert(attrHistories);
        } catch (Exception e) {
            log.error("批量保存AttrHistory失败：{}，数量：{}", e.getMessage(), attrHistories.size(), e);
        }
    }

    /**
     * map转string
     *
     * @param differenceMap
     * @return
     */
    private static String differenceMapToString(Map<String, Map<String, String>> differenceMap) {

        return differenceMap.entrySet().stream()
                .flatMap(firstMapEntry -> {
                    String key = firstMapEntry.getKey();
                    return firstMapEntry.getValue().entrySet().stream().map(
                            secondMapEntry -> {
                                StringBuilder str = new StringBuilder();
                                // 头像分支：统一 key 为"头像URL"或兼容历史的"headimgurl"；移除不存在的"头像更换"判断
                                if ("头像URL".equalsIgnoreCase(key) || "headimgurl".equalsIgnoreCase(key)) {
                                    str.append("\n【").append(key).append("】更换前后如下");
                                } else {
                                    str.append("\n【").append(key).append("】(\"").append(secondMapEntry.getKey()).append("\" -> \"").append(secondMapEntry.getValue()).append("\")");
                                }
                                return str;
                            }
                    );
                }).collect(Collectors.joining(""));
    }

    /**
     * 联系人相关map的put操作
     * put前统计哪些信息变了
     *
     * @param oldV 旧值
     * @param newV 新值
     */
    public static void compare(Contacts oldV, Contacts newV) {
        if (isRoomContact(oldV)) {
            compareGroup(oldV, newV);
        } else {
            compareContacts(oldV, newV);
        }
    }

    /**
     * 添加联系人
     */
    public static void addContacts(Contacts contacts) {

        contacts.setIscontacts(true);
        String userName = contacts.getUsername();
        String nickName = contacts.getNickname();

        if (contacts.getVerifyflag()!=null && (contacts.getVerifyflag() & 8) != 0) {
            // 公众号/服务号

            contacts.setType(Contacts.ContactsType.PUBLIC_USER);
        } else if (configuration.getSpecialUser().contains(userName)) {
            // 特殊账号

            contacts.setType(Contacts.ContactsType.SPECIAL_USER);
        } else if (userName.startsWith("@@")) {
            // 群聊

            contacts.setType(Contacts.ContactsType.GROUP_USER);
        } else {
            contacts.setType(Contacts.ContactsType.ORDINARY_USER);
            // 普通联系人
        }
        Core.getMemberMap().put(userName, contacts);
    }

    public static Optional<Contacts> findGroupMember(List<Contacts> members, Message message) {
        for (Contacts m : members) {
            if (Objects.equals(m.getUsername(), message.getFromMemberOfGroupUsername()) ||
                    Objects.equals(m.getRemarkname(), message.getFromRemarkname()) ||
                    Objects.equals(m.getDisplayname(), message.getFromMemberOfGroupDisplayname()) ||
                    Objects.equals(m.getNickname(), message.getFromMemberOfGroupNickname())) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public static Optional<Contacts> findGroupMemberByNickName(List<Contacts> members, String nickName) {
        for (Contacts m : members) {
            if (
                    Objects.equals(m.getNickname(),nickName)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public static Optional<Contacts> findContactsByString(String str) {
        for (Contacts m : Core.getMemberMap().values()) {
            if (
                    Objects.equals(m.getRemarkname(), str) ||
                    Objects.equals(m.getDisplayname(), str) ||
                    Objects.equals(m.getNickname(), str)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    /**
     * 第一次收到群消息 加载群成员详细细腻
     *
     * @param msg 消息
     */
    public static <M> Contacts loadUserInfo(String fromUserName, String toUserName, String memberName, M msg) {
        String userName = fromUserName;
        if (userName.equals(Core.getUserName())) {
            userName = toUserName;
        }

        if ("@placeholder_foldgroup".equals(userName)) {
            log.warn("折叠的群聊！");
            Contacts contacts = new Contacts();
            contacts.setUsername(userName);
            contacts.setNickname("折叠的群聊");
            contacts.setMutualCreate(true);
            return null;
        }

        Contacts contacts = Core.getMemberMap().get(userName);
        if (contacts != null && contacts.isMutualCreate()) {
            //手动创建的
            contacts = null;
        }
        if (contacts == null) {
            log.error("用户不存在！{}", userName);

            if (ContactsTools.isRoomContact(userName)) {
                DownloadTask<Void> task2 = new DownloadTask<>();
                task2.setTaskId("WebWxBatchGetContact:" + userName);
                task2.setGroupName(userName);
                task2.setType(DownloadType.GetBatchContacts);
                DownloadManager.submitAwait(task2);
            } else {
                DownloadTask<Void> task1 = new DownloadTask<>();
                task1.setTaskId("webWxGetContact");
                task1.setType(DownloadType.GetContacts);
                DownloadManager.submitAwait(task1);
            }

            contacts = Core.getMemberMap().get(userName);
            if (contacts != null) {
                log.info("获取成功：{},{}", userName, contacts);
            }
        } else if (ContactsTools.isRoomContact(userName)
                && StringUtils.isNotEmpty(memberName)) {
            //群成员发的消息
            if (Core.getMemberMap().containsKey(memberName)) {
                //群成员是我的好友，群信息没有当前成员则添加进去

                if (ContactsTools.getMemberOfGroup(userName, memberName) == null) {
                    Contacts memberContacts = Core.getMemberMap().get(memberName);
                    log.error("群用户或者群成员信息不完整，添加好友进去{}", memberContacts);
                    Core.getMemberMap().get(userName).getMemberlist().add(memberContacts);
                }

            } else if (CollectionUtils.isEmpty(contacts.getMemberlist())
                    || ContactsTools.getMemberOfGroup(userName, memberName) == null) {
                //群成员非好友 且 群里面没有该用户信息 则加载群成员数据

                log.error("群用户或者群成员信息不完整！{}，{}", contacts.getMemberlist().size(), userName);
                DownloadTask<Void> objectDownloadTask = new DownloadTask<>();
                objectDownloadTask.setTaskId("WebWxBatchGetContact:" + userName);
                objectDownloadTask.setGroupName(userName);
                objectDownloadTask.setType(DownloadType.GetBatchContacts);
                DownloadManager.submitAwait(objectDownloadTask);
                contacts = Core.getMemberMap().get(userName);
                if (contacts != null && ContactsTools.getMemberOfGroup(userName, memberName) != null) {
                    log.info("获取成功：{},{}", userName, contacts);
                }
                if (ContactsTools.getMemberOfGroup(userName, memberName) != null) {
                    log.info("成员获取成功：{},{}", userName, contacts);
                }
            }
        }
        if (contacts == null) {
            contacts = new Contacts();
            contacts.setUsername(userName);
            contacts.setNickname("未知");
            contacts.setMutualCreate(true);
            ContactsTools.addContacts(contacts);
            log.error("未知联系人消息{}", msg);
        }
        return contacts;

    }

    /**
     * 返回二个JSONObject的差异
     *
     * @param oldO 旧
     * @param newO 新
     * @return difference
     */
    public static Map<String, Map<String, String>> getDifferenceMap(Contacts oldO, Contacts newO) {

        Map<String, Map<String, String>> diffMap = new HashMap<>();

        if (oldO == null || newO == null) {
            throw new IllegalArgumentException("两个对象都不能为空");
        }

        Field[] fields = oldO.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object oldValue = field.get(oldO);
                Object newValue = field.get(newO);

                String fieldName = field.getName();

                if ("memberlist".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                if ("remarkpyinitial".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                if ("remarkpyquanpin".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                if ("pyquanpin".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                if ("pyinitial".equalsIgnoreCase(fieldName)) {
                    continue;
                }
                if ("attrstatus".equalsIgnoreCase(fieldName)) {
                    continue;
                }

                // 问题4：null 值规范化 —— String 字段做 null→"" 规范化，其他类型保持原样；仅二者都 null 时才跳过
                Object oldV;
                Object newV;
                if (oldValue instanceof String || newValue instanceof String) {
                    oldV = oldValue == null ? "" : oldValue;
                    newV = newValue == null ? "" : newValue;
                } else {
                    oldV = oldValue;
                    newV = newValue;
                }
                if (Objects.equals(oldV, newV)) {
                    continue;
                }

                if ("HeadImgUrl".equalsIgnoreCase(fieldName)) {
                    if (StringUtils.isNotEmpty((String) newV) && StringUtils.isNotEmpty((String) oldV)) {
                        Matcher matcherNew = pattern.matcher((String) newV);
                        Matcher matcherOld = pattern.matcher((String) oldV);
                        if (matcherNew.find() && matcherOld.find()) {
                            //头像相同
                            String groupNew = matcherNew.group(1);
                            String groupOld = matcherOld.group(1);
                            if (!groupNew.equals(groupOld)) {
                                Map<String, String> valueDiff = new HashMap<>();
                                valueDiff.put(String.valueOf(oldV), String.valueOf(newV));
                                // 问题8：统一用中文 key "头像URL"
                                String attrKey = attributeReverseMap.getOrDefault(fieldName, "头像URL");
                                diffMap.put(attrKey, valueDiff);
                            }
                        }
                    }
                } else {
                    String attrKey = attributeReverseMap.get(fieldName);
                    // 问题17 兜底：未在 attributeMap 注册的字段，不产生差异，避免 null key
                    if (attrKey == null) {
                        continue;
                    }
                    Map<String, String> valueDiff = new HashMap<>();
                    valueDiff.put(EmojiUtil.emojiFormatter(String.valueOf(oldV)),
                            EmojiUtil.emojiFormatter(String.valueOf(newV)));
                    diffMap.put(attrKey, valueDiff);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("字段访问异常: " + field.getName(), e);
            }
        }

        return diffMap;
    }
}
