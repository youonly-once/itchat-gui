package cn.shu.wechat.api;


import cn.shu.wechat.constant.DownloadType;
import cn.shu.wechat.constant.WxConstant;
import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.AttrHistory;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.entity.Message;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.task.DownloadTask;
import cn.shu.wechat.utils.CommonTools;
import cn.shu.wechat.utils.JSONObjectUtil;
import cn.shu.wechat.utils.SpringContextHolder;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
        String remarkNameByPersonUserName = getContactRemarkNameByUserName(contacts);
        if (StringUtils.isNotEmpty(remarkNameByPersonUserName)) {
            return remarkNameByPersonUserName;
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
        return CommonTools.emojiFormatter(contactByUserName.getRemarkname());
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
        return CommonTools.emojiFormatter(contacts.getRemarkname());
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
        return CommonTools.emojiFormatter(contactByUserName.getNickname());
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
        return CommonTools.emojiFormatter(contacts.getNickname());
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
                ? CommonTools.emojiFormatter(memberOfGroup.getNickname())
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
                return CommonTools.emojiFormatter(displayName);
            }
            displayName = memberOfGroup.getDisplayname();
            if (!StringUtils.isEmpty(displayName)) {
                return CommonTools.emojiFormatter(displayName);
            }
            displayName = memberOfGroup.getNickname();
            if (!StringUtils.isEmpty(displayName)) {
                return CommonTools.emojiFormatter(displayName);
            }
        return userName;
    }


    public static String getSignatureNameOfGroup(String userName) {
        Contacts contacts = Core.getMemberMap().get(userName);
        if (contacts == null){
            return null;
        }
        return CommonTools.emojiFormatter(contacts.getSignature());
    }


    public static String getSignatureNameOfGroup(Contacts contacts) {
        if (contacts == null){
            return null;
        }
        return CommonTools.emojiFormatter(contacts.getSignature());
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
     * 是否消息免打扰
     * @param contacts 联系人
     * @return {@code false} 免打扰
     */
    public static boolean isMute(Contacts contacts){
        if (isRoomContact(contacts.getUsername())) {
            return (contacts.getStatues() == null||
                    contacts.getStatues().intValue()== WxConstant.ChatRoomMute.CHATROOM_NOTIFY_CLOSE.CODE);
        }else{
            return ((contacts.getContactflag().intValue() & WxConstant.ContactFlag.CONTACTFLAG_NOTIFYCLOSECONTACT.CODE)>0);
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
                    .collect(Collectors.toMap(Contacts::getUsername, Function.identity()));

            Map<String, Contacts> newMap = newMemberList.stream()
                    .collect(Collectors.toMap(Contacts::getUsername, Function.identity()));


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
            oldKeys.removeAll(removedIds);
            List<Message> messageList = oldKeys.stream()
                    .map(commonKey -> compareGroupMember(oldMap.get(commonKey), newMap.get(commonKey), oldGroup))
                    .filter(Objects::nonNull)
                    .toList();

            if (messageList.size() > 10) {
                //群成员信息变化
                log.error("群成员信息变化数量：{}", messageList.size());
                return;
            }
            MessageTools.sendMsgByUserId(messageList);
        } catch (Exception e) {
            e.printStackTrace();
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
        Map<String, Map<String, String>> differenceMap = JSONObjectUtil.getDifferenceMap(oldMember, newMember);
        if (differenceMap.isEmpty()) {
            return null;
        }
        //待发消息列表
        String differenceStr = differenceMapToString(differenceMap);
        //获取群成员姓名
        String memberDisplayNameOfGroup = ContactsTools.getMemberDisplayNameOfGroupObj(oldGroup, oldMember.getUsername());
        //获取群昵称
        String groupName = ContactsTools.getContactDisplayNameByUserName(oldGroup);

        Message message = Message.builder().content("群成员信息更改" + "：【" + groupName + "】" + "（" + memberDisplayNameOfGroup + "）属性更新：" + differenceStr)
                .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.TEXT.getCode())
                .toUsername("filehelper")
                .build();
        log.info("群成员信息更改" + "：【" + groupName + "】" + "（" + memberDisplayNameOfGroup + "）属性更新：" + differenceStr);
        //差异存到数据库
        store(differenceMap, oldMember, List.of(message));

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
            Map<String, Map<String, String>> differenceMap = JSONObjectUtil.getDifferenceMap(oldV, newV);
            if (differenceMap.isEmpty()) {
                return;
            }
            String s = differenceMapToString(differenceMap);
            String name = ContactsTools.getContactDisplayNameByUserName(newV.getUsername());
            ArrayList<Message> messages = new ArrayList<>();
             messages.add(Message.builder().content("普通联系人" + "（" + name + "）属性更新：" + s)
                    .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.TEXT.getCode())
                    .toUsername("filehelper")
                    .build());
            log.info("普通联系人" + "（" + name + "）属性更新：" + s);
            //差异存到数据库
            store(differenceMap, oldV, messages);
            MessageTools.sendMsgByUserId(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 保存修改记录到数据库
     *
     * @param differenceMap
     * @param oldV
     */
    private static void store(Map<String, Map<String, String>> differenceMap, Contacts oldV, List<Message> messages) {
        ArrayList<AttrHistory> attrHistories = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            for (Map.Entry<String, String> stringStringEntry : stringMapEntry.getValue().entrySet()) {
                if (stringMapEntry.getKey().equals("HeadImgUrl")
                        || stringMapEntry.getKey().equals("头像更换")
                        || stringMapEntry.getKey().equals("headimgurl")) {
                    String oldHeadPath = Core.getContactHeadImgPath().get(oldV.getUsername());


                    DownloadTask<String> downloadTask = new DownloadTask<>();
                    downloadTask.setRelativeUrl(stringStringEntry.getValue());
                    downloadTask.setType(DownloadType.HEAD_IMAGE_BIG);
                    downloadTask.setUserName(oldV.getUsername());
                    downloadTask.setTaskId(stringStringEntry.getValue()+oldV.getUsername());
                    String newHeadPath = DownloadManager.submitAwait(downloadTask,1000*60*5, TimeUnit.MILLISECONDS);

                    if (newHeadPath != null) {
                        Core.getContactHeadImgPath().put(oldV.getUsername(), newHeadPath);
                        //刷新头像
                        AvatarUtil.putUserAvatarCache(oldV.getUsername(), newHeadPath);
                    }
                    //更换头像需要发送图片
                    //更换前
                    messages.add(Message.builder()
                            .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.PIC.getCode())
                            .toUsername("filehelper")
                            .filePath(oldHeadPath).build());
                    //更换后
                    messages.add(Message.builder()
                            .toUsername("filehelper")
                            .msgType(WxReqParamsConstant.WXSendMsgCodeEnum.PIC.getCode())
                            .filePath(newHeadPath).build());

                    AttrHistory build = AttrHistory.builder()
                            .attr(stringMapEntry.getKey())
                            .oldval(oldHeadPath)
                            .newval(newHeadPath)
                            .id(0)
                            .nickname(oldV.getNickname())
                            .remarkname(oldV.getNickname())
                            .username(oldV.getUsername())
                            .createtime(new Date())
                            .build();
                    attrHistories.add(build);
                } else {
                    AttrHistory build = AttrHistory.builder()
                            .attr(stringMapEntry.getKey())
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
        try {
            SpringContextHolder.getBean(AttrHistoryMapper.class).batchInsert(attrHistories);
        } catch (Exception e) {
            log.warn(e.getMessage());
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
                                if (key.equals("头像更换") || key.equals("HeadImgUrl") || key.equals("headimgurl")) {
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

}
