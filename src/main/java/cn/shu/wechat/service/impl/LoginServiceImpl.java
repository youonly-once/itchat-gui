package cn.shu.wechat.service.impl;

import cn.shu.WeChatStater;
import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.api.DownloadTools;
import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.constant.StorageLoginInfoEnum;
import cn.shu.wechat.constant.WxURLEnum;
import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.core.MsgCenter;
import cn.shu.wechat.dto.request.*;
import cn.shu.wechat.dto.response.WxCreateRoomResp;
import cn.shu.wechat.exception.WebWXException;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import cn.shu.wechat.dto.response.sync.AddMsgList;
import cn.shu.wechat.dto.response.sync.WebWxSyncResp;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.dto.response.SyncCheckResp;
import cn.shu.wechat.dto.response.wxinit.WxInitResponse;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.utils.CommonTools;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.HttpUtil;
import cn.shu.wechat.utils.SleepUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 登陆服务实现类
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年5月13日 上午12:09:35
 */
@Log4j2
@Component
public class LoginServiceImpl implements LoginService {

    @Resource
    private WechatConfiguration config;

    @Resource
    private AttrHistoryMapper attrHistoryMapper;

    @Resource
    private MsgCenter msgCenter;


    private final Set<String> msgIds = new HashSet<>();

    /**
     * 检查登陆状态
     *
     * @param result 二维码扫描状态
     * @return 状态码
     */
    public WxRespConstant.CheckLoginResultCodeEnum checkQRCodeScanStatus(String result) throws Exception {
        String regEx = "window.code=(\\d+)";
        Matcher matcher = CommonTools.getMatcher(regEx, result);
        if (matcher.find()) {
            return WxRespConstant.CheckLoginResultCodeEnum.getByCode(Integer.parseInt(matcher.group(1)));
        } else {
            throw new Exception("获取二维码扫描状态码失败！");
        }
    }

    public static void main(String[] args) {
        String url = "window.redirect_uri=\"https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=A8XCLb3mURiL7HSW-Hwoqd3b@qrticket_0&uuid=wdhd2iiUGQ==&lang=zh_CN&scan=1685067009\"";
        Pattern pattern = Pattern.compile("(https?://[^/]+)");
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            String protocol = matcher.group(1);
            System.out.println("Protocol: " + protocol);
        }
    }

    /**
     * 处理登陆信息
     *
     * @param loginContent
     * @author SXS
     * @date 2017年4月9日 下午12:16:26
     */
    private String processQRScanInfo(String loginContent) throws Exception {
        //返回数据格式
        // window.code = 200;
        // window.redirect_uri = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage?ticket=A8XCLb3mURiL7HSW-Hwoqd3b@qrticket_0&uuid=wdhd2iiUGQ==&lang=zh_CN&scan=1685067009";

        String regEx = "window.redirect_uri=\"(\\S+)\";";
        Matcher matcher = CommonTools.getMatcher(regEx, loginContent);
        if (matcher.find()) {
            String originalUrl = matcher.group(1);
            String url = originalUrl.substring(0, originalUrl.lastIndexOf('/'));
            //获取主机名：https://wx2.qq.com/cgi-bin/mmwebwx-bin
            Core.getLoginResultData().setUrl(url);
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
                if (Core.getLoginResultData().getUrl().contains(indexUrl)) {
                    Core.getLoginResultData().setFileUrl(fileUrl);
                    Core.getLoginResultData().setSyncUrl(syncUrl);
                    break;
                }
            }
            if (Core.getLoginResultData().getFileUrl() == null
                    && Core.getLoginResultData().getSyncUrl() == null) {
                Core.getLoginResultData().setFileUrl(url);
                Core.getLoginResultData().setSyncUrl(url);
            }
            Core.getLoginResultData().setDeviceId("e" + String.valueOf(new Random().nextLong()).substring(1, 16)); // 生成15位随机数
            Core.getLoginResultData().setBaseRequest(new BaseRequest());
            Core.getLoginResultData().getBaseRequest().setDeviceId(Core.getLoginResultData().getDeviceId());
            return originalUrl;
        }
        throw new Exception("获取登录地址失败！");
    }

    /**
     * @param callBack
     * @return
     * @throws Exception
     */
    @Override
    public boolean preLogin(LoginCallBack callBack) throws Exception {

        boolean isLogin = false;
        // 组装参数和URL
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(WxReqParamsConstant.LoginParaEnum.LOGIN_ICON.para(), WxReqParamsConstant.LoginParaEnum.LOGIN_ICON.value()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.LoginParaEnum.UUID.para(), Core.getUuid()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.LoginParaEnum.TIP.para(), WxReqParamsConstant.LoginParaEnum.TIP.value()));

        while1:
        while (!isLogin) {

            long millis = System.currentTimeMillis();
            params.add(new BasicNameValuePair(WxReqParamsConstant.LoginParaEnum.R.para(), String.valueOf(millis / 1579L)));
            params.add(new BasicNameValuePair(WxReqParamsConstant.LoginParaEnum._.para(), String.valueOf(millis)));
            HttpEntity entity = HttpUtil.doGet(WxURLEnum.LOGIN_URL.getUrl(), params, true, null);

            try {
                String result = EntityUtils.toString(entity);
                WxRespConstant.CheckLoginResultCodeEnum codeEnum = checkQRCodeScanStatus(result);
                switch (codeEnum) {

                    case SUCCESS: {
                        String redirectUrl = processQRScanInfo(result);
                        doLogin(redirectUrl);
                        isLogin = true;
                        Core.setAlive(true);
                        callBack.CallBack(codeEnum.getMsg());
                        break while1;
                    }
                    case CANCEL:
                    case WAIT_CONFIRM:
                        //返回数据格式
                        //  * window.code = 201;
                        //  * window.userAvatar = '头像数据';
                        log.info(codeEnum.getMsg());
                        String avatar = getUserAvatar(result);
                        callBack.avatar(avatar);
                        callBack.CallBack(codeEnum.getMsg());
                        break;
                    case WAIT_SCAN: {
                        log.info(codeEnum.getMsg());
                        //TODO 刷新二维码
                        callBack.CallBack(codeEnum.getMsg());
                        break;
                    }
                    case NONE: {
                        log.info(codeEnum.getMsg());
                        break;
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                callBack.CallBack(e.getMessage());
                log.error("微信登陆异常：{}", e.getMessage());
            }
        }
        return isLogin;
    }

    /**
     * 登录
     *
     * @param redirectUrl
     */
    public void doLogin(String redirectUrl) throws Exception {
        try {
            HttpEntity entity = HttpUtil.doGet(redirectUrl, null, false, null);
            //结果格式：
            //<error>
            // <ret>0</ret>
            // <message></message>
            // <skey>@crypt_acc90d00_30f16e0f14fbf5bb094e7542866de58c</skey>
            // <wxsid>9fP/G/y4Ggnr4G2v</wxsid>
            // <wxuin>2955965517</wxuin>
            // <pass_ticket>KHZtdahInDUwtz486wGnaLKVAWJoVDZ6cxNJWs5KfWQ0qUW7F%2Ffqf1JebBG77B98</pass_ticket>
            // <isgrayscale>1</isgrayscale>
            // </error>
            String resultOfXml = EntityUtils.toString(entity);

            //如果登录被禁止时，则登录返回的message内容不为空，下面代码则判断登录内容是否为空，不为空则退出程序
            String msg = getLoginMessage(resultOfXml);
            if (!"".equals(msg)) {
                throw new Exception(msg);
            }
            //解析XML
            Document doc = CommonTools.xmlParser(resultOfXml);
            if (doc != null) {
                Core.getLoginResultData().getBaseRequest().setSKey(
                        doc.getElementsByTagName(StorageLoginInfoEnum.skey.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                Core.getLoginResultData().getBaseRequest().setWxSid(
                        doc.getElementsByTagName(StorageLoginInfoEnum.wxsid.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                Core.getLoginResultData().getBaseRequest().setWxUin(
                        doc.getElementsByTagName(StorageLoginInfoEnum.wxuin.getKey()).item(0).getFirstChild()
                                .getNodeValue());
                Core.getLoginResultData().setPassTicket(
                        doc.getElementsByTagName(StorageLoginInfoEnum.pass_ticket.getKey()).item(0).getFirstChild()
                                .getNodeValue());
            }
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }


    }

    @Override
    public String getUuid() {
        // 组装参数和URL
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
        params.add(new BasicNameValuePair(WxReqParamsConstant.UUIDParaEnum.APP_ID.para(), WxReqParamsConstant.UUIDParaEnum.APP_ID.value()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.UUIDParaEnum.FUN.para(), WxReqParamsConstant.UUIDParaEnum.FUN.value()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.UUIDParaEnum.LANG.para(), WxReqParamsConstant.UUIDParaEnum.LANG.value()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.UUIDParaEnum._.para(), String.valueOf(System.currentTimeMillis())));

        HttpEntity entity = HttpUtil.doGet(WxURLEnum.UUID_URL.getUrl(), params, true, null);

        try {
            String result = EntityUtils.toString(entity);
            String regEx = "window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"(\\S+?)\";";
            Matcher matcher = CommonTools.getMatcher(regEx, result);
            if (matcher.find()) {
                if (("200".equals(matcher.group(1)))) {
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

        String qrUrl = WxURLEnum.QRCODE_URL.getUrl() + Core.getUuid();
        HttpEntity entity = HttpUtil.doGet(qrUrl, null, true, null);
        try {
            //下载二维码图片
            OutputStream out = new FileOutputStream(qrPath);
            byte[] bytes = EntityUtils.toByteArray(entity);
            out.write(bytes);
            out.flush();
            out.close();
            //二维码地址
            String qrUrl2 = WxURLEnum.cAPI_qrcode.getUrl() + Core.getUuid();
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public BufferedImage getQR() {
        String qrUrl = WxURLEnum.QRCODE_URL.getUrl() + Core.getUuid();
        HttpEntity entity = HttpUtil.doGet(qrUrl, null, true, null);
        try {
            BufferedImage image = ImageIO.read(entity.getContent());
            return image;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean webWxInit() {
        Core.setAlive(true);
        Core.setLastNormalRetCodeTime(System.currentTimeMillis());
        // 组装请求URL和参数
        String url = String.format(WxURLEnum.INIT_URL.getUrl(),
                Core.getLoginResultData().getUrl(),
                System.currentTimeMillis() / 3158L,
                Core.getLoginResultData().getPassTicket());

        // 请求初始化接口
        WxInitReq wxInitReq = new WxInitReq();
        wxInitReq.setBaseRequest(Core.getLoginResultData().getBaseRequest());
        HttpEntity entity = HttpUtil.doPost(url, JSON.toJSONString(wxInitReq));
        try {
            String result = EntityUtils.toString(entity, Consts.UTF_8);
            WxInitResponse wxInitResponse = JSON.parseObject(result, WxInitResponse.class);
            Contacts me = wxInitResponse.getUser();
            ;

            Core.getLoginResultData().setInviteStartCount(wxInitResponse.getInviteStartCount());
            Core.getLoginResultData().setSyncKeyObject(wxInitResponse.getSyncKey());


            Core.setUserName(me.getUsername());
            Core.setNickName(me.getNickname());
            Core.setUserSelf(me);
            Core.getMemberMap().put(me.getUsername(), me);
            //初始化列表的联系人
            //最近聊天的联系人

            Set<String> recentContacts = Core.getRecentContacts();
            for (Contacts contacts : wxInitResponse.getContactList()) {
                //下载头像
                ExecutorServiceUtil.getHeadImageDownloadExecutorService().submit(() -> {
                    AvatarUtil.putUserAvatarCache(contacts.getUsername(), DownloadTools.downloadHeadImgByRelativeUrl(contacts.getHeadimgurl()));
                });
                addContacts(contacts, false);
                recentContacts.add(contacts.getUsername());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void wxStatusNotify() {
        // 组装请求URL和参数
        String url = String.format(WxURLEnum.STATUS_NOTIFY_URL.getUrl(),
                Core.getLoginResultData().getUrl(),
                Core.getLoginResultData().getPassTicket());

        WxStatusNotifyReq wxStatusNotifyReq = new WxStatusNotifyReq();
        wxStatusNotifyReq.setBaseRequest(Core.getLoginResultData().getBaseRequest());
        wxStatusNotifyReq.setCode(3);
        wxStatusNotifyReq.setFromUserName(Core.getUserName());
        wxStatusNotifyReq.setToUserName(Core.getUserName());
        wxStatusNotifyReq.setClientMsgId(System.currentTimeMillis());
        String paramStr = JSON.toJSONString(wxStatusNotifyReq);

        try {
            HttpEntity entity = HttpUtil.doPost(url, paramStr);
            EntityUtils.toString(entity, Consts.UTF_8);
        } catch (Exception e) {
            log.error("微信状态通知接口失败！", e);
        }

    }

    /**
     * 处理成功消息
     *
     * @param selector 类型
     */
    private void processSuccessMsg(String selector) throws Exception {
        // 最后收到正常报文时间
        Core.setLastNormalRetCodeTime(System.currentTimeMillis());
        //消息同步
        //JSONObject msgObj = webWxSync();
        WebWxSyncResp webWxSyncMsg = webWxSync();

        switch (WxRespConstant.SyncCheckSelectorEnum.getByCode(selector)) {
            case NORMAL:
                break;
            case MOD_CONTACT:
            case ADD_OR_DEL_CONTACT:
            case NEW_MSG:

                //新消息
                for (AddMsgList msg : webWxSyncMsg.getAddMsgList()) {
                    if (msgIds.contains(msg.getMsgId())) {
                        log.warn("消息重复：{}", msg);
                        continue;
                    }
                    msgIds.add(msg.getMsgId());
                    ExecutorServiceUtil.getGlobalExecutorService().execute(() -> {
                        msgCenter.handleNewMsg(msg);
                    });
                }
                //联系人修改
                msgCenter.handleModContact(webWxSyncMsg.getModContactList());
                for (Contacts contacts : webWxSyncMsg.getDelContactList()) {
                    log.info("联系人删除：{}", contacts);
                }

                break;

            case ENTER_OR_LEAVE_CHAT:
                webWxSync();
                break;

            case A:
                log.info("未知消息：{}", webWxSyncMsg);
                break;
            default:
                break;

        }
    }

    @Override
    public void startReceiving() {
        Core.setAlive(true);
        Runnable runnable = () -> {
            while (Core.isAlive()) {
                try {

                    //检测是否有新消息
                    SyncCheckResp syncCheckResp = syncCheck();
                    WxRespConstant.SyncCheckRetCodeEnum syncCheckRetCodeEnum = WxRespConstant.SyncCheckRetCodeEnum.getByCode(syncCheckResp.getRetCode());
                    switch (syncCheckRetCodeEnum) {

                        case SUCCESS: {
                            processSuccessMsg(syncCheckResp.getSelector());
                            break;
                        }
                        case UNKOWN: {
                            log.info(syncCheckRetCodeEnum.getType());
                            continue;
                        }
                        case LOGIN_OUT:
                        case LOGIN_OTHERWHERE:{
                            log.warn(syncCheckRetCodeEnum.getType());
                            //重启客户端
                            WeChatStater.restartApplication();
                            break;
                        }
                        case TICKET_ERROR:
                        case PARAM_ERROR:
                        case NOT_LOGIN_WARN:
                        case LOGIN_ENV_ERROR:
                        case TOO_OFEN: {
                            log.error(syncCheckRetCodeEnum.getType());
                            Core.setAlive(false);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("消息同步错误：{}", e.getMessage());
                    SleepUtils.sleep(1000);
                }

            }
        };
        ExecutorServiceUtil.getReceivingExecutorService().execute(runnable);
    }

    @Override
    public void webWxGetContact() {
        String url = String.format(WxURLEnum.WEB_WX_GET_CONTACT.getUrl(),
                Core.getLoginResultData().getUrl());
        HttpEntity entity = HttpUtil.doPost(url, JSON.toJSONString(Core.getLoginResultData().getBaseRequest()));
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
            // 循环获取seq直到为0，即获取全部好友列表
            // ==0：好友获取完毕
            // >0：好友未获取完毕，此时seq为已获取的字节数
            while (seq > 0) {
                // 设置seq传参
                params.add(new BasicNameValuePair("r", String.valueOf(currentTime)));
                params.add(new BasicNameValuePair("seq", String.valueOf(seq)));
                entity = HttpUtil.doGet(url, params, false, null);

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
                Contacts contacts = JSON.parseObject(JSON.toJSONString(o), Contacts.class);
                addContacts(contacts, true);
            }
            if (!Core.getMemberMap().containsKey("filehelper")) {
                Core.getMemberMap().put("filehelper",
                        Contacts.builder().username("filehelper").displayname("文件传输助手")
                                .type(Contacts.ContactsType.ORDINARY_USER).build());
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 添加联系人
     *
     */
    private void addContacts(Contacts contacts, boolean compare) {

        contacts.setIscontacts(true);
        String userName = contacts.getUsername();
        String nickName = contacts.getNickname();
        //保存之前的群信息 方便compare
        if (Core.getMemberMap().containsKey(contacts.getUsername())) {
            contacts.setMemberlist(Core.getMemberMap().get(contacts.getUsername()).getMemberlist());
        }
        Core.getMemberMap().put(userName, contacts);

        if ((contacts.getVerifyflag() & 8) != 0) {
            // 公众号/服务号
            if (!Core.getPublicUsersMap().containsKey(userName)) {
                log.info("新增公众号/服务号：{}", nickName);
            }
            Core.getPublicUsersMap().put(userName, contacts);
            contacts.setType(Contacts.ContactsType.PUBLIC_USER);
        } else if (config.getSpecialUser().contains(userName)) {
            // 特殊账号
            if (!Core.getSpecialUsersMap().containsKey(userName)) {
                log.info("新增特殊账号：{}", nickName);
            }
            Core.getSpecialUsersMap().put(userName, contacts);
            contacts.setType(Contacts.ContactsType.SPECIAL_USER);
        } else if (userName.startsWith("@@")) {
            // 群聊
            if (!Core.getGroupIdSet().contains(userName)) {
                log.info("新增群聊：{}", nickName);
                Core.getGroupIdSet().add(userName);
            }
            contacts.setType(Contacts.ContactsType.GROUP_USER);
        } else {
            contacts.setType(Contacts.ContactsType.ORDINARY_USER);
            //比较上次差异
            if (compare) {
                Contacts old = Core.getContactMap().get(userName);
                ContactsTools.compareContacts(old, contacts);
            }

            // 普通联系人
            Core.getContactMap().put(userName, contacts);
        }
    }

    @Override
    public void WebWxBatchGetContact() {
        String url = String.format(WxURLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                Core.getLoginResultData().getUrl(), new Date().getTime(),
                Core.getLoginResultData().getPassTicket());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("Count", Core.getGroupIdSet().size());
        List<Map<String, String>> list = new ArrayList<Map<String, String>>(Core.getGroupIdSet().size());
        for (String s : Core.getGroupIdSet()) {
            HashMap<String, String> map = new HashMap<String, String>(2);
            map.put("UserName", s);
            map.put("EncryChatRoomId", "");
            list.add(map);
        }
        paramMap.put("List", list);
        paramMap.put("BaseRequest",Core.getLoginResultData().getBaseRequest());
        HttpEntity entity = HttpUtil.doPost(url, JSON.toJSONString(paramMap));
        try {
            String text = EntityUtils.toString(entity, Consts.UTF_8);
            JSONObject obj = JSON.parseObject(text);
            //群列表
            JSONArray contactList = obj.getJSONArray("ContactList");
            for (int i = 0; i < contactList.size(); i++) {
                // 群好友
                JSONObject groupObject = contactList.getJSONObject(i);
                Contacts group = JSON.parseObject(JSON.toJSONString(groupObject), Contacts.class);
                String userName = group.getUsername();
                if (userName.startsWith("@@")) {
                    //以上接口返回的成员属性不全，以下的接口获取群成员详细属性
                    JSONArray memberArray = WebWxBatchGetContactDetail(group);
                    List<Contacts> memberList = JSON.parseArray(JSON.toJSONString(memberArray), Contacts.class);
                    group.setMemberlist(memberList);

                    //比较群成员信息
                    Contacts old = Core.getGroupMap().get(userName);
                    //比较上次差异
                    ContactsTools.compareGroup(old, group);

                    Core.getMemberMap().put(userName, group);
                    Core.getGroupMap().put(userName, group);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
    }

    @Override
    public List<Contacts> WebWxBatchGetContact(String groupName) {

        log.info("加载群成员开始：" + groupName);
        String url = String.format(WxURLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                Core.getLoginResultData().getUrl(), new Date().getTime(),
                Core.getLoginResultData().getPassTicket());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("Count", 1);
        List<Map<String, String>> list = new ArrayList<Map<String, String>>(1);
        HashMap<String, String> map = new HashMap<String, String>(2);
        map.put("UserName", groupName);
        map.put("EncryChatRoomId", "");
        list.add(map);
        paramMap.put("List", list);
        paramMap.put("BaseRequest",Core.getLoginResultData().getBaseRequest());
        HttpEntity entity = null;
        synchronized ((groupName + "WebWxBatchGetContact").intern()) {
            entity = HttpUtil.doPost(url, JSON.toJSONString(paramMap));
        }
        try {
            String text = EntityUtils.toString(entity, Consts.UTF_8);
            JSONObject obj = JSON.parseObject(text);
            //群列表
            JSONArray contactList = obj.getJSONArray("ContactList");
            for (int i = 0; i < contactList.size(); i++) {
                // 群好友
                JSONObject groupObject = contactList.getJSONObject(i);
                Contacts group = JSON.parseObject(JSON.toJSONString(groupObject), Contacts.class);
                group.setType(Contacts.ContactsType.GROUP_USER);
                String userName = group.getUsername();
                Core.getMemberMap().put(userName, group);
                if (userName.startsWith("@@")) {
                    //以上接口返回的成员属性不全，以下的接口获取群成员详细属性
                    JSONArray memberArray = WebWxBatchGetContactDetail(group);
                    List<Contacts> memberList = JSON.parseArray(JSON.toJSONString(memberArray), Contacts.class);
                    group.setMemberlist(memberList);
                    Core.getGroupMap().put(userName, group);
                    Core.getMemberMap().put(userName, group);
                    log.info("加载群成员结束：" + Core.getMemberMap().get(groupName).getMemberlist().size());
                    return memberList;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        log.info("加载群成员结束：0");
        return new ArrayList<>();
    }

    @Override
    public JSONArray WebWxBatchGetContactDetail(Contacts group) {
        String url = String.format(WxURLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                Core.getLoginResultData().getUrl(), System.currentTimeMillis(),
                Core.getLoginResultData().getPassTicket());
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("BaseRequest",Core.getLoginResultData().getBaseRequest());
        //保存获取的群成员详细信息
        ArrayList<Contacts> groupContactsList = new ArrayList<>();
        JSONArray memberArray = new JSONArray();
        //保存需要获取详细资料的群成员username
        List<Map<String, String>> list = new ArrayList<Map<String, String>>(group.getMemberlist().size());
        for (Contacts o : group.getMemberlist()) {
            //遍历群成员
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("UserName", o.getUsername());
            map.put("EncryChatRoomId", group.getUsername());
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

            HttpEntity entity = null;
            synchronized ((group.getUsername() + "WebWxBatchGetContact").intern()) {
                entity = HttpUtil.doPost(url, JSON.toJSONString(paramMap));
            }
            try {
                String text = EntityUtils.toString(entity, Consts.UTF_8);
                JSONObject obj = JSON.parseObject(text);
                JSONArray contactListArray = obj.getJSONArray("ContactList");
                memberArray.addAll(contactListArray);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        return memberArray;

    }


    /**
     * 检查登录人的头像
     *
     */
    public String getUserAvatar(String result) {
        String regEx = "window.userAvatar\\s*=\\s*'data:img/jpg;base64,(.+)'";
        Matcher matcher = CommonTools.getMatcher(regEx, result);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
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
     */
    private WebWxSyncResp webWxSync() throws Exception {
        String url = String.format(WxURLEnum.WEB_WX_SYNC_URL.getUrl(),
                Core.getLoginResultData().getUrl(),
                Core.getLoginResultData().getBaseRequest().getWxSid(),
                Core.getLoginResultData().getBaseRequest().getSKey(),
                Core.getLoginResultData().getPassTicket());

        WxSyncReq wxSyncReq = WxSyncReq.builder().SyncKey(Core.getLoginResultData().getSyncKeyObject())
                .rr(-System.currentTimeMillis() / 1000)
                .BaseRequest(Core.getLoginResultData().getBaseRequest()).build();
        String paramStr = JSON.toJSONString(wxSyncReq);


        HttpEntity entity = HttpUtil.doPost(url, paramStr);
        String text = EntityUtils.toString(entity, Consts.UTF_8);
        WebWxSyncResp webWxSyncMsg = JSON.parseObject(text, WebWxSyncResp.class);
        if (webWxSyncMsg.getBaseResponse().getRet() != 0) {
            throw new Exception("消息同步失败！");
        } else {
            Core.getLoginResultData().setSyncCheckKey(webWxSyncMsg.getSyncCheckKey());
            Core.getLoginResultData().setSyncKey(
                    webWxSyncMsg.getSyncKey()
                            .getList()
                            .stream()
                            .map(e -> e.getKey() + "_" + e.getVal())
                            .collect(Collectors.joining("|"))
            );
            Core.getLoginResultData().setSyncKeyObject(webWxSyncMsg.getSyncKey());
        }
        return webWxSyncMsg;
    }

    /**
     * 检查是否有新消息 check whether there's a message
     *
     */
    private SyncCheckResp syncCheck() throws Exception {
        // 组装请求URL和参数
        String url = String.format(WxURLEnum.SYNC_CHECK_URL.getUrl(), Core.getLoginResultData().getSyncUrl());
        List<BasicNameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair(WxReqParamsConstant.SyncCheckParaEnum.R.para(), String.valueOf(System.currentTimeMillis())));
        params.add(new BasicNameValuePair(WxReqParamsConstant.SyncCheckParaEnum.S_KEY.para(), Core.getLoginResultData().getBaseRequest().getSKey()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.SyncCheckParaEnum.SID.para(), Core.getLoginResultData().getBaseRequest().getWxSid()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.SyncCheckParaEnum.UIN.para(), Core.getLoginResultData().getBaseRequest().getWxUin()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.SyncCheckParaEnum.DEVICE_ID.para(), Core.getLoginResultData().getBaseRequest().getDeviceId()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.SyncCheckParaEnum.SYNC_KEY.para(), Core.getLoginResultData().getSyncKey()));
        params.add(new BasicNameValuePair(WxReqParamsConstant.SyncCheckParaEnum._.para(), String.valueOf(System.currentTimeMillis())));
        SleepUtils.sleep(7);
        HttpEntity entity = HttpUtil.doGetOfReceive(url, params, true, null);
        if (entity == null) {
            throw new Exception("Entity is null!");
        }
        String result = EntityUtils.toString(entity);
        String regEx = "window.synccheck=\\{retcode:\"(\\d+)\",selector:\"(\\d+)\"\\}";
        Matcher matcher = CommonTools.getMatcher(regEx, result);
        if (!matcher.find()) {
            throw new Exception("Unexpected sync check result: " + result);
        } else {
            return SyncCheckResp.builder().retCode(Integer.parseInt(matcher.group(1)))
                    .selector(matcher.group(2)).build();
        }
    }

    /**
     * 解析登录返回的消息，如果成功登录，则message为空
     *
     */
    public String getLoginMessage(String result) {
        String[] strArr = result.split("<message>");
        String[] rs = strArr[1].split("</message>");
        if (rs.length > 1) {
            return rs[0];
        }
        return "";
    }

    @Override
    public WxCreateRoomResp webWxCreateRoom(List<Contacts> contacts) throws Exception {
        if (contacts.isEmpty()){
            throw new WebWXException("contacts is empty.");
        };
        String url = String.format(WxURLEnum.WEB_WX_CREATE_ROOM.getUrl(),System.currentTimeMillis());
        WxCreateRoomReq createRoomReq = WxCreateRoomReq.builder().BaseRequest(Core.getLoginResultData().getBaseRequest())
                .MemberCount(contacts.size())
                .MemberList(contacts)
                .build();
        HttpEntity httpEntity = HttpUtil.doPost(url, JSON.toJSONString(createRoomReq));
        return JSON.parseObject(EntityUtils.toString(httpEntity), WxCreateRoomResp.class);
    }
}
