package cn.shu.wechat.service.impl;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;

import cn.shu.wechat.api.MessageTools;
import cn.shu.wechat.beans.pojo.AttrHistory;
import cn.shu.wechat.beans.msg.sync.AddMsgList;
import cn.shu.wechat.beans.msg.sync.WebWxSyncMsg;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.core.MsgCenter;
import cn.shu.wechat.enums.*;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import cn.shu.wechat.service.ILoginService;
import cn.shu.wechat.utils.*;
import cn.shu.wechat.enums.parameters.BaseParaEnum;
import cn.shu.wechat.enums.parameters.LoginParaEnum;
import cn.shu.wechat.enums.parameters.StatusNotifyParaEnum;
import cn.shu.wechat.enums.parameters.UUIDParaEnum;
import cn.shu.wechat.utils.CommonTools;
import cn.shu.wechat.api.DownloadTools;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.annotation.Resource;

/**
 * 登陆服务实现类
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年5月13日 上午12:09:35
 */
@Log4j2
@Component
public class LoginServiceImpl implements ILoginService {


    @Resource
    private AttrHistoryMapper attrHistoryMapper;

    @Resource
    private MsgCenter msgCenter;


    @Override
    public boolean login() {

        boolean isLogin = false;
        // 组装参数和URL
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(LoginParaEnum.LOGIN_ICON.para(), LoginParaEnum.LOGIN_ICON.value()));
        params.add(new BasicNameValuePair(LoginParaEnum.UUID.para(), Core.getUuid()));
        params.add(new BasicNameValuePair(LoginParaEnum.TIP.para(), LoginParaEnum.TIP.value()));

        // long time = 4000;
        while (!isLogin) {
            // SleepUtils.sleep(time += 1000);
            long millis = System.currentTimeMillis();
            params.add(new BasicNameValuePair(LoginParaEnum.R.para(), String.valueOf(millis / 1579L)));
            params.add(new BasicNameValuePair(LoginParaEnum._.para(), String.valueOf(millis)));
            HttpEntity entity = MyHttpClient.doGet(URLEnum.LOGIN_URL.getUrl(), params, true, null);

            try {
                String result = EntityUtils.toString(entity);
                String status = checklogin(result);

                if (CheckLoginResultEnum.SUCCESS.getCode().equals(status)) {
                    processLoginInfo(result); // 处理结果
                    isLogin = true;
                    Core.setAlive(isLogin);
                    break;
                }
                if (CheckLoginResultEnum.WAIT_CONFIRM.getCode().equals(status)) {
                    log.info("请点击微信确认按钮，进行登陆");
                }

            } catch (Exception e) {
                log.error("微信登陆异常！", e);
            }
        }
        return isLogin;
    }

    @Override
    public String getUuid() {
        // 组装参数和URL
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(UUIDParaEnum.APP_ID.para(), UUIDParaEnum.APP_ID.value()));
        params.add(new BasicNameValuePair(UUIDParaEnum.FUN.para(), UUIDParaEnum.FUN.value()));
        params.add(new BasicNameValuePair(UUIDParaEnum.LANG.para(), UUIDParaEnum.LANG.value()));
        params.add(new BasicNameValuePair(UUIDParaEnum._.para(), String.valueOf(System.currentTimeMillis())));

        HttpEntity entity = MyHttpClient.doGet(URLEnum.UUID_URL.getUrl(), params, true, null);

        try {
            String result = EntityUtils.toString(entity);
            String regEx = "window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"(\\S+?)\";";
            Matcher matcher = CommonTools.getMatcher(regEx, result);
            if (matcher.find()) {
                if ((CheckLoginResultEnum.SUCCESS.getCode().equals(matcher.group(1)))) {
                    Core.setUuid(matcher.group(2));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return Core.getUuid();
    }

    @Override
    public boolean getQR(String qrPath) {

        String qrUrl = URLEnum.QRCODE_URL.getUrl() + Core.getUuid();
        HttpEntity entity = MyHttpClient.doGet(qrUrl, null, true, null);
        try {
            //下载二维码图片
            OutputStream out = new FileOutputStream(qrPath);
            byte[] bytes = EntityUtils.toByteArray(entity);
            out.write(bytes);
            out.flush();
            out.close();
            //二维码地址
            String qrUrl2 = URLEnum.cAPI_qrcode.getUrl() + Core.getUuid();
            //控制台打印二维码
            String qrString = QRterminal.getQr(qrUrl2);
            //System.out.println("\n" + qrString);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean webWxInit() {
        Core.setAlive(true);
        Core.setLastNormalRetCodeTime(System.currentTimeMillis());
        // 组装请求URL和参数
        String url = String.format(URLEnum.INIT_URL.getUrl(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()),
                System.currentTimeMillis() / 3158L,
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));

        Map<String, Object> paramMap = Core.getParamMap();

        // 请求初始化接口
        HttpEntity entity = MyHttpClient.doPost(url, JSON.toJSONString(paramMap));
        try {
            String result = EntityUtils.toString(entity, Consts.UTF_8);
            JSONObject obj = JSON.parseObject(result);

            JSONObject user = obj.getJSONObject(StorageLoginInfoEnum.User.getKey());
            JSONObject syncKey = obj.getJSONObject(StorageLoginInfoEnum.SyncKey.getKey());

            Core.getLoginInfoMap().put(StorageLoginInfoEnum.InviteStartCount.getKey(),
                    obj.getInteger(StorageLoginInfoEnum.InviteStartCount.getKey()));
            Core.getLoginInfoMap().put(StorageLoginInfoEnum.SyncKey.getKey(), syncKey);

            JSONArray syncArray = syncKey.getJSONArray("List");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < syncArray.size(); i++) {
                sb.append(syncArray.getJSONObject(i).getString("Key") + "_"
                        + syncArray.getJSONObject(i).getString("Val") + "|");
            }
            // 1_661706053|2_661706420|3_661706415|1000_1494151022|
            String synckey = sb.toString();

            // 1_661706053|2_661706420|3_661706415|1000_1494151022
            // 1_656161336|2_656161626|3_656161313|11_656159955|13_656120033|201_1492273724|1000_1492265953|1001_1492250432|1004_1491805192
            Core.getLoginInfoMap().put(StorageLoginInfoEnum.synckey.getKey(), synckey.substring(0, synckey.length() - 1));
            Core.setUserName(user.getString("UserName"));
            Core.setNickName(user.getString("NickName"));
            Core.setUserSelf(obj.getJSONObject("User"));

            //初始化列表的联系人
            //最近聊天的联系人
            String chatSet = obj.getString("ChatSet");
            String[] chatSetArray = chatSet.split(",");
     /*       for (String s : chatSetArray) {
                if (s.startsWith("@@")) {
                    // 更新GroupIdList
                    Core.getGroupIdSet().add(s);
                }
            }*/

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void wxStatusNotify() {
        // 组装请求URL和参数
        String url = String.format(URLEnum.STATUS_NOTIFY_URL.getUrl(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));

        Map<String, Object> paramMap = Core.getParamMap();
        paramMap.put(StatusNotifyParaEnum.CODE.para(), StatusNotifyParaEnum.CODE.value());
        paramMap.put(StatusNotifyParaEnum.FROM_USERNAME.para(), Core.getUserName());
        paramMap.put(StatusNotifyParaEnum.TO_USERNAME.para(), Core.getUserName());
        paramMap.put(StatusNotifyParaEnum.CLIENT_MSG_ID.para(), System.currentTimeMillis());
        String paramStr = JSON.toJSONString(paramMap);

        try {
            HttpEntity entity = MyHttpClient.doPost(url, paramStr);
            EntityUtils.toString(entity, Consts.UTF_8);
        } catch (Exception e) {
            log.error("微信状态通知接口失败！", e);
        }

    }

    @Override
    public void startReceiving() {
        Core.setAlive(true);
        Runnable runnable = new Runnable() {
            int retryCount = 0;

            @Override
            public void run() {
                while (Core.isAlive()) {
                    try {
                        //检测是否有新消息
                        Map<String, String> resultMap = syncCheck();
                        String retcode = resultMap.get("retcode");
                        String selector = resultMap.get("selector");
                        if (retcode.equals(SyncCheckRetCodeEnum.UNKOWN.getCode())) {
                            //好像搜狗输入法按语音键盘松手会触发
                            log.info(SyncCheckRetCodeEnum.UNKOWN.getType());
                            continue;
                        } else if (retcode.equals(SyncCheckRetCodeEnum.LOGIN_OUT.getCode())) {
                            // 退出
                            log.info(SyncCheckRetCodeEnum.LOGIN_OUT.getType());
                            break;
                        } else if (retcode.equals(SyncCheckRetCodeEnum.LOGIN_OTHERWHERE.getCode())) {
                            // 其它地方登陆
                            log.info(SyncCheckRetCodeEnum.LOGIN_OTHERWHERE.getType());
                            break;
                        } else if (retcode.equals(SyncCheckRetCodeEnum.MOBILE_LOGIN_OUT.getCode())) {
                            // 移动端退出
                            log.info(SyncCheckRetCodeEnum.MOBILE_LOGIN_OUT.getType());
                            break;
                        } else if (retcode.equals(SyncCheckRetCodeEnum.SUCCESS.getCode())) {
                            // 最后收到正常报文时间
                            Core.setLastNormalRetCodeTime(System.currentTimeMillis());
                            //消息同步
                            JSONObject msgObj = webWxSync();
                            switch (SyncCheckSelectorEnum.getByCode(selector)) {
                                case NORMAL:
                                    break;
                                case NEW_MSG:
                                    if (msgObj != null) {
                                        try {
                                            //新消息
                                            WebWxSyncMsg webWxSyncMsg = JSON.parseObject(JSON.toJSONString(msgObj), WebWxSyncMsg.class);
                                            List<AddMsgList> addMsgLists = webWxSyncMsg.getAddMsgList();
                                            for (AddMsgList msg : addMsgLists) {
                                                ExecutorsUtil.getGlobalExecutorService().submit(() -> {
                                                    try {
                                                        msgCenter.handleNewMsg(msg);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        log.error(e.getMessage());
                                                    }

                                                });
                                            }
                                            //联系人修改消息
                                            //MsgCenter.produceModContactMsg(webWxSyncMsg.getModContactList());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            log.info(e.getMessage());
                                        }
                                    }
                                    break;
                                case ADD_OR_DEL_CONTACT:
                                    if (msgObj != null) {
                                        try {
                                            JSONArray msgList = msgObj.getJSONArray("AddMsgList");
                                            JSONArray modContactList = msgObj.getJSONArray("ModContactList"); // 存在删除或者新增的好友信息
                                            for (int j = 0; j < msgList.size(); j++) {
                                                JSONObject userInfo = modContactList.getJSONObject(j);
                                                // 存在主动加好友之后的同步联系人到本地
                                                Core.getContactMap().put(userInfo.getString("UserName"), userInfo);
                                            }
                                        } catch (Exception e) {

                                            log.info(e.getMessage());
                                        }
                                    }


                                    break;
                                case ENTER_OR_LEAVE_CHAT:
                                    webWxSync();
                                    break;
                                case MOD_CONTACT:
                                case A:
                                    System.out.println("哈哈");
                                    break;
                                default:
                                    break;

                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        log.error("消息同步错误：{}", e.getMessage());
                        retryCount += 1;
                        if (Core.getReceivingRetryCount() < retryCount) {
                            //Core.setAlive(false);
                        } else {
                            SleepUtils.sleep(1000);
                        }
                    }

                }
            }
        };
        ExecutorsUtil.getGlobalExecutorService().submit(runnable);


    }

    @Override
    public void webWxGetContact() {
        String url = String.format(URLEnum.WEB_WX_GET_CONTACT.getUrl(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()));
        Map<String, Object> paramMap = Core.getParamMap();
        HttpEntity entity = MyHttpClient.doPost(url, JSON.toJSONString(paramMap));
        if (entity == null) {
            return;
        }
        try {
            String result = EntityUtils.toString(entity, Consts.UTF_8);
            JSONObject fullFriendsJsonList = JSON.parseObject(result);
            // 查看seq是否为0，0表示好友列表已全部获取完毕，若大于0，则表示好友列表未获取完毕，当前的字节数（断点续传）
            long seq = 0;
            long currentTime = 0L;
            List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
            if (fullFriendsJsonList.get("Seq") != null) {
                seq = fullFriendsJsonList.getLong("Seq");
                currentTime = System.currentTimeMillis();
            }
            JSONArray member = fullFriendsJsonList.getJSONArray(StorageLoginInfoEnum.MemberList.getKey());
            // 循环获取seq直到为0，即获取全部好友列表 ==0：好友获取完毕 >0：好友未获取完毕，此时seq为已获取的字节数
            while (seq > 0) {
                // 设置seq传参
                params.add(new BasicNameValuePair("r", String.valueOf(currentTime)));
                params.add(new BasicNameValuePair("seq", String.valueOf(seq)));
                entity = MyHttpClient.doGet(url, params, false, null);

                params.remove(new BasicNameValuePair("r", String.valueOf(currentTime)));
                params.remove(new BasicNameValuePair("seq", String.valueOf(seq)));

                result = EntityUtils.toString(entity, Consts.UTF_8);
                fullFriendsJsonList = JSON.parseObject(result);

                if (fullFriendsJsonList.get("Seq") != null) {
                    seq = fullFriendsJsonList.getLong("Seq");
                    currentTime = System.currentTimeMillis();
                }

                // 累加好友列表
                member.addAll(fullFriendsJsonList.getJSONArray(StorageLoginInfoEnum.MemberList.getKey()));
            }

            for (Object value : member) {
                JSONObject o = (JSONObject) value;
                String userName = o.getString("UserName");
                if ((o.getInteger("VerifyFlag") & 8) != 0) {
                    // 公众号/服务号
                    Core.getPublicUsersMap().put(userName, o);
                } else if (Config.API_SPECIAL_USER.contains(userName)) {
                    // 特殊账号
                    Core.getSpecialUsersMap().put(userName, o);
                } else if (userName.startsWith("@@")) {
                    // 群聊
                    if (!Core.getGroupIdSet().contains(userName)) {
                        log.info("新增群聊：{}", userName);
                        Core.getGroupIdSet().add(userName);
                    }
                } else if (userName.equals(Core.getUserName())) {
                    // 自己
                    Core.getContactMap().remove(userName);
                } else {
                        //比较上次差异
                    compareOld(Core.getContactMap(), userName, o, "普通联系人");
                    // 普通联系人
                    Core.getContactMap().put(userName, o);
                }

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void WebWxBatchGetContact() {
        String url = String.format(URLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()), new Date().getTime(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));
        Map<String, Object> paramMap = Core.getParamMap();
        paramMap.put("Count", Core.getGroupIdSet().size());
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (String s : Core.getGroupIdSet()) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("UserName", s);
            map.put("EncryChatRoomId", "");
            list.add(map);
        }
        paramMap.put("List", list);
        HttpEntity entity = MyHttpClient.doPost(url, JSON.toJSONString(paramMap));
        try {
            String text = EntityUtils.toString(entity, Consts.UTF_8);
            JSONObject obj = JSON.parseObject(text);
            //群列表
            JSONArray contactList = obj.getJSONArray("ContactList");
            for (int i = 0; i < contactList.size(); i++) {
                // 群好友
                JSONObject groupObject = contactList.getJSONObject(i);
                String userName = groupObject.getString("UserName");
                if (userName.startsWith("@@")) {
                    //以上接口返回的成员属性不全，以下的接口获取群成员详细属性
                    JSONArray memberArray = WebWxBatchGetContactDetail(groupObject);
                    Core.getGroupMemberMap().put(userName, memberArray);
                    Core.getGroupMap().put(userName, groupObject);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    @Override
    public JSONArray WebWxBatchGetContactDetail(JSONObject groupObject) {
        String url = String.format(URLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()), System.currentTimeMillis(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));
        Map<String, Object> paramMap = Core.getParamMap();


        String groupUserName = groupObject.getString("UserName");
        //保存获取的群成员详细信息
        JSONArray memberArray = new JSONArray();
        //保存需要获取详细资料的群成员username
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Object o : groupObject.getJSONArray("MemberList")) {
            //遍历群成员
            JSONObject memberO = (JSONObject) o;
            if (Core.getContactMap().containsKey(memberO.getString("UserName"))) {
                memberArray.add(Core.getContactMap().get(memberO.getString("UserName")));
                continue;
            }
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("UserName", memberO.getString("UserName"));
            map.put("EncryChatRoomId", groupUserName);
            list.add(map);
        }
        if (list.isEmpty()) {
            return memberArray;
        }
        //每次请求50个
        int ceil = (int) (Math.ceil((list.size() / 50.0)));
        for (int i = 0; i < ceil; i++) {
            List<Map<String, String>> subList = null;
            if (i < ceil - 1) {
                subList = list.subList(i * 50, i * 50 + 50);
            } else {
                subList = list.subList(i * 50, list.size());
            }
            paramMap.put("Count", subList.size());
            paramMap.put("List", subList);
            HttpEntity entity = MyHttpClient.doPost(url, JSON.toJSONString(paramMap));
            try {
                String text = EntityUtils.toString(entity, Consts.UTF_8);
                JSONObject obj = JSON.parseObject(text);
                JSONArray contactList = obj.getJSONArray("ContactList");
                memberArray.addAll(contactList);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return memberArray;

    }

    /**
     * 检查登陆状态
     *
     * @param result
     * @return
     */
    public String checklogin(String result) {
        String regEx = "window.code=(\\d+)";
        Matcher matcher = CommonTools.getMatcher(regEx, result);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 处理登陆信息
     *
     * @param loginContent
     * @author SXS
     * @date 2017年4月9日 下午12:16:26
     */
    private void processLoginInfo(String loginContent) {
        String regEx = "window.redirect_uri=\"(\\S+)\";";
        Matcher matcher = CommonTools.getMatcher(regEx, loginContent);
        if (matcher.find()) {
            String originalUrl = matcher.group(1);
            // https://wx2.qq.com/cgi-bin/mmwebwx-bin
            String url = originalUrl.substring(0, originalUrl.lastIndexOf('/'));
            Core.getLoginInfoMap().put("url", url);
            Map<String, List<String>> possibleUrlMap = this.getPossibleUrlMap();
            Iterator<Entry<String, List<String>>> iterator = possibleUrlMap.entrySet().iterator();
            Map.Entry<String, List<String>> entry;
            String fileUrl;
            String syncUrl;
            while (iterator.hasNext()) {
                entry = iterator.next();
                String indexUrl = entry.getKey();
                fileUrl = "https://" + entry.getValue().get(0) + "/cgi-bin/mmwebwx-bin";
                syncUrl = "https://" + entry.getValue().get(1) + "/cgi-bin/mmwebwx-bin";
                if (Core.getLoginInfoMap().get("url").toString().contains(indexUrl)) {
                    Core.getLoginInfoMap().put("fileUrl", fileUrl);
                    Core.getLoginInfoMap().put("syncUrl", syncUrl);
                    break;
                }
            }
            if (Core.getLoginInfoMap().get("fileUrl") == null && Core.getLoginInfoMap().get("syncUrl") == null) {
                Core.getLoginInfoMap().put("fileUrl", url);
                Core.getLoginInfoMap().put("syncUrl", url);
            }
            Core.getLoginInfoMap().put("deviceid", "e" + String.valueOf(new Random().nextLong()).substring(1, 16)); // 生成15位随机数
            Core.getLoginInfoMap().put("BaseRequest", new ArrayList<String>());
            String text = "";

            try {
                HttpEntity entity = MyHttpClient.doGet(originalUrl, null, false, null);
                text = EntityUtils.toString(entity);
            } catch (Exception e) {
                log.info(e.getMessage());
                return;
            }
            //add by 默非默 2017-08-01 22:28:09
            //如果登录被禁止时，则登录返回的message内容不为空，下面代码则判断登录内容是否为空，不为空则退出程序
            String msg = getLoginMessage(text);
            if (!"".equals(msg)) {
                log.info(msg);
                System.exit(0);
            }
            Document doc = CommonTools.xmlParser(text);
            if (doc != null) {
                Core.getLoginInfoMap().put(StorageLoginInfoEnum.skey.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.skey.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                Core.getLoginInfoMap().put(StorageLoginInfoEnum.wxsid.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.wxsid.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                Core.getLoginInfoMap().put(StorageLoginInfoEnum.wxuin.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.wxuin.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                Core.getLoginInfoMap().put(StorageLoginInfoEnum.pass_ticket.getKey(),
                        doc.getElementsByTagName(StorageLoginInfoEnum.pass_ticket.getKey()).item(0).getFirstChild()
                                .getNodeValue());
            }

        }
    }

    private Map<String, List<String>> getPossibleUrlMap() {
        Map<String, List<String>> possibleUrlMap = new HashMap<String, List<String>>();
        possibleUrlMap.put("wx.qq.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.wx.qq.com");
                add("webpush.wx.qq.com");
            }
        });

        possibleUrlMap.put("wx2.qq.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.wx2.qq.com");
                add("webpush.wx2.qq.com");
            }
        });
        possibleUrlMap.put("wx8.qq.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.wx8.qq.com");
                add("webpush.wx8.qq.com");
            }
        });

        possibleUrlMap.put("web2.wechat.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.web2.wechat.com");
                add("webpush.web2.wechat.com");
            }
        });
        possibleUrlMap.put("wechat.com", new ArrayList<String>() {
            /**
             *
             */
            private static final long serialVersionUID = 1L;

            {
                add("file.web.wechat.com");
                add("webpush.web.wechat.com");
            }
        });
        return possibleUrlMap;
    }

    /**
     * 同步消息 sync the messages
     *
     * @return
     * @author SXS
     * @date 2017年5月12日 上午12:24:55
     */
    private JSONObject webWxSync() {
        JSONObject result = null;
        String url = String.format(URLEnum.WEB_WX_SYNC_URL.getUrl(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.url.getKey()),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.wxsid.getKey()),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.skey.getKey()),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.pass_ticket.getKey()));
        Map<String, Object> paramMap = Core.getParamMap();
        paramMap.put(StorageLoginInfoEnum.SyncKey.getKey(),
                Core.getLoginInfoMap().get(StorageLoginInfoEnum.SyncKey.getKey()));
        paramMap.put("rr", -System.currentTimeMillis() / 1000);
        String paramStr = JSON.toJSONString(paramMap);
        try {
            HttpEntity entity = MyHttpClient.doPost(url, paramStr);
            String text = EntityUtils.toString(entity, Consts.UTF_8);
            JSONObject obj = JSON.parseObject(text);
            if (obj.getJSONObject("BaseResponse").getInteger("Ret") != 0) {
                result = null;
            } else {
                result = obj;
                Core.getLoginInfoMap().put(StorageLoginInfoEnum.SyncKey.getKey(), obj.getJSONObject("SyncCheckKey"));
                JSONArray syncArray = obj.getJSONObject(StorageLoginInfoEnum.SyncKey.getKey()).getJSONArray("List");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < syncArray.size(); i++) {
                    sb.append(syncArray.getJSONObject(i).getString("Key") + "_"
                            + syncArray.getJSONObject(i).getString("Val") + "|");
                }
                String synckey = sb.toString();
                // 1_656161336|2_656161626|3_656161313|11_656159955|13_656120033|201_1492273724|1000_1492265953|1001_1492250432|1004_1491805192
                Core.getLoginInfoMap().put(StorageLoginInfoEnum.synckey.getKey(),
                        synckey.substring(0, synckey.length() - 1));
            }
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return result;

    }

    /**
     * 检查是否有新消息 check whether there's a message
     *
     * @return
     * @author SXS
     * @date 2017年4月16日 上午11:11:34
     */
    private Map<String, String> syncCheck() {
        Map<String, String> resultMap = new HashMap<String, String>();
        // 组装请求URL和参数
        String url = Core.getLoginInfoMap().get(StorageLoginInfoEnum.syncUrl.getKey()) + URLEnum.SYNC_CHECK_URL.getUrl();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        for (BaseParaEnum baseRequest : BaseParaEnum.values()) {
            params.add(new BasicNameValuePair(baseRequest.para().toLowerCase(),
                    Core.getLoginInfoMap().get(baseRequest.value()).toString()));
        }
        params.add(new BasicNameValuePair("r", String.valueOf(System.currentTimeMillis())));
        params.add(new BasicNameValuePair("synckey", (String) Core.getLoginInfoMap().get("synckey")));
        params.add(new BasicNameValuePair("_", String.valueOf(System.currentTimeMillis())));
        SleepUtils.sleep(7);
        try {
            HttpEntity entity = MyHttpClient.doGet(url, params, true, null);
            if (entity == null) {
                resultMap.put("retcode", "9999");
                resultMap.put("selector", "9999");
                return resultMap;
            }
            String text = EntityUtils.toString(entity);
            String regEx = "window.synccheck=\\{retcode:\"(\\d+)\",selector:\"(\\d+)\"\\}";
            Matcher matcher = CommonTools.getMatcher(regEx, text);
            if (!matcher.find() || matcher.group(1).equals("2")) {
                log.info(String.format("Unexpected sync check result: %s", text));
            } else {
                resultMap.put("retcode", matcher.group(1));
                resultMap.put("selector", matcher.group(2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 解析登录返回的消息，如果成功登录，则message为空
     *
     * @param result
     * @return
     */
    public String getLoginMessage(String result) {
        String[] strArr = result.split("<message>");
        String[] rs = strArr[1].split("</message>");
        if (rs != null && rs.length > 1) {
            return rs[0];
        }
        return "";
    }

    /**
     * 联系人相关map的put操作
     * put前统计哪些信息变了
     *
     * @param map  保存联系人信息的map
     * @param key  联系人key
     * @param newV 新值
     * @param tip  提示信息
     */
    private void compareOld(Map<String, JSONObject> map, String key, JSONObject newV, String tip) {
        JSONObject oldV = map.get(key);

        String name = newV.getString("NickName");
        if (StringUtils.isEmpty(name)) {
            name = newV.getString("UserName");
        }
        if (oldV == null) {
            log.info("新增{}（{}）：{}", tip, name, newV);
            return;
        }
        Map<String, Map<String, String>> differenceMap = JSONObjectUtil.getDifferenceMap(oldV, newV);
        if (differenceMap.size() > 0) {
            //待发消息列表
            ArrayList<MessageTools.Result> results = new ArrayList<>();
            String s = mapToString(differenceMap);
            //发送消息
            results.add(MessageTools.Result.builder().content(tip + "（" + name + "）属性更新：" + s)
                    .replyMsgTypeEnum(WXSendMsgCodeEnum.TEXT)
                    .build());
            //Old与New存在差异
            log.info("{}（{}）属性更新：{}", tip, name, s);
            //差异存到数据库
            store(differenceMap, oldV, results);
        }

    }

    /**
     * 保存修改记录到数据库
     *
     * @param differenceMap
     * @param oldV
     */
    private void store(Map<String, Map<String, String>> differenceMap, JSONObject oldV, ArrayList<MessageTools.Result> results) {
        ArrayList<AttrHistory> attrHistories = new ArrayList<>();
        for (Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            for (Entry<String, String> stringStringEntry : stringMapEntry.getValue().entrySet()) {
                if (stringMapEntry.getKey().equals("HeadImgUrl")
                        || stringMapEntry.getKey().equals("头像更换")) {
                    String oldHeadPath = Core.getContactHeadImgPath().get(oldV.getString("UserName"));
                    String newHeadPath = DownloadTools.downloadHeadImg(stringStringEntry.getValue()
                            , oldV.getString("UserName"));
                    Core.getContactHeadImgPath().put(oldV.getString("UserName"), newHeadPath);
                    //更换头像需要发送图片
                    //更换前
                    results.add(MessageTools.Result.builder()
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                            .filePath(oldHeadPath).build());
                    //更换后
                    results.add(MessageTools.Result.builder()
                            .replyMsgTypeEnum(WXSendMsgCodeEnum.PIC)
                            .filePath(newHeadPath).build());
                    AttrHistory build = AttrHistory.builder()
                            .attr(stringMapEntry.getKey())
                            .oldval(oldHeadPath)
                            .newval(newHeadPath)
                            .id(0)
                            .nickname(oldV.getString("NickName"))
                            .remarkname(oldV.getString("RemarkName"))
                            .username(oldV.getString("UserName"))
                            .createtime(new Date())
                            .build();
                    attrHistories.add(build);
                } else {
                    AttrHistory build = AttrHistory.builder()
                            .attr(stringMapEntry.getKey())
                            .oldval(stringStringEntry.getKey())
                            .newval(stringStringEntry.getValue())
                            .id(0)
                            .nickname(oldV.getString("NickName"))
                            .remarkname(oldV.getString("RemarkName"))
                            .username(oldV.getString("UserName"))
                            .createtime(new Date())
                            .build();
                    attrHistories.add(build);
                }

            }
        }
        try {
            attrHistoryMapper.batchInsert(attrHistories);
        } catch (Exception e) {

        }
    }

    /**
     * map转string
     *
     * @param differenceMap
     * @return
     */
    private String mapToString(Map<String, Map<String, String>> differenceMap) {
        String str = "";
        for (Entry<String, Map<String, String>> stringMapEntry : differenceMap.entrySet()) {
            Map<String, String> value = stringMapEntry.getValue();
            for (Entry<String, String> stringStringEntry : value.entrySet()) {
                if (stringMapEntry.getKey().equals("头像更换")
                        || stringMapEntry.getKey().equals("HeadImgUrl")) {
                    str = str + "\n【" + stringMapEntry.getKey() + "】更换前后如下";
                } else {
                    str = str + "\n【" + stringMapEntry.getKey() + "】(\"" + stringStringEntry.getKey() + "\" -> \"" + stringStringEntry.getValue() + "\")";
                }
            }
        }
        return str;
    }
}
