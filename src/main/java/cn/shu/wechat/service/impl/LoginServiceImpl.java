package cn.shu.wechat.service.impl;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.constant.*;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.core.MsgCenter;
import cn.shu.wechat.dto.request.*;
import cn.shu.wechat.dto.response.SyncCheckResp;
import cn.shu.wechat.dto.response.WxCreateRoomResp;
import cn.shu.wechat.dto.response.sync.AddMsgList;
import cn.shu.wechat.dto.response.sync.WebWxSyncResp;
import cn.shu.wechat.dto.response.wxinit.WxInitResponse;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.exception.WebWXException;
import cn.shu.wechat.mapper.AttrHistoryMapper;
import cn.shu.wechat.service.LoginService;
import cn.shu.wechat.swing.utils.AvatarUtil;
import cn.shu.wechat.task.DownloadManager;
import cn.shu.wechat.task.DownloadTask;
import cn.shu.wechat.utils.CommonTools;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.HttpUtil;
import cn.shu.wechat.utils.SleepUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Sets;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
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



    private final Set<String> msgIds = new HashSet<>();
    @Resource
    private AttrHistoryMapper attrHistoryMapper;
    @Resource
    private MsgCenter msgCenter;
    private volatile boolean WebWxBatchGetContactExcept;

    private Timer timer ;

    public static <T> List<List<T>> splitIntoGroups(List<T> input, int groupSize) {
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < input.size(); i += groupSize) {
            result.add(input.subList(i, Math.min(i + groupSize, input.size())));
        }
        return result;
    }

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
        HashMap<String, String> params = new HashMap<>();
        params.put (WxReqParamsConstant.LoginParaEnum.LOGIN_ICON.para(), WxReqParamsConstant.LoginParaEnum.LOGIN_ICON.value());
        params.put (WxReqParamsConstant.LoginParaEnum.UUID.para(), Core.getUuid());
        params.put (WxReqParamsConstant.LoginParaEnum.TIP.para(), WxReqParamsConstant.LoginParaEnum.TIP.value());

        while1:
        while (!isLogin) {

            long millis = System.currentTimeMillis();
            params.put (WxReqParamsConstant.LoginParaEnum.R.para(), String.valueOf(millis / 1579L));
            params.put(WxReqParamsConstant.LoginParaEnum.LINE.para(), String.valueOf(millis));

            try {
                String result = HttpUtil.doGet(WxURLEnum.LOGIN_URL.getUrl(), params,null,true, HttpResponse.BodyHandlers.ofString());
                WxRespConstant.CheckLoginResultCodeEnum codeEnum = checkQRCodeScanStatus(result);
                switch (codeEnum) {

                    case SUCCESS: {
                        String redirectUrl = processQRScanInfo(result);
                        doLogin(redirectUrl);
                        isLogin = true;
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
                callBack.CallBack(e.getMessage());
                log.error("微信登陆异常：{}", e.getMessage());
            }
            Thread.sleep(100);
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
//            String url = redirectUrl + "&fun=new&version=v2&mod=desktop&lang=zh_CN";
//            Map<String, String> header = new HashMap<>();
//            //UOS header
//            header.put("client-version", "2.0.0");
//            header.put("extspam", "Go8FCIkFEokFCggwMDAwMDAwMRAGGvAESySibk50w5Wb3uTl2c2h64jVVrV7gNs06GFlWplHQbY/5FfiO++1yH4ykCyNPWKXmco+wfQzK5R98D3so7rJ5LmGFvBLjGceleySrc3SOf2Pc1gVehzJgODeS0lDL3/I/0S2SSE98YgKleq6Uqx6ndTy9yaL9qFxJL7eiA/R3SEfTaW1SBoSITIu+EEkXff+Pv8NHOk7N57rcGk1w0ZzRrQDkXTOXFN2iHYIzAAZPIOY45Lsh+A4slpgnDiaOvRtlQYCt97nmPLuTipOJ8Qc5pM7ZsOsAPPrCQL7nK0I7aPrFDF0q4ziUUKettzW8MrAaiVfmbD1/VkmLNVqqZVvBCtRblXb5FHmtS8FxnqCzYP4WFvz3T0TcrOqwLX1M/DQvcHaGGw0B0y4bZMs7lVScGBFxMj3vbFi2SRKbKhaitxHfYHAOAa0X7/MSS0RNAjdwoyGHeOepXOKY+h3iHeqCvgOH6LOifdHf/1aaZNwSkGotYnYScW8Yx63LnSwba7+hESrtPa/huRmB9KWvMCKbDThL/nne14hnL277EDCSocPu3rOSYjuB9gKSOdVmWsj9Dxb/iZIe+S6AiG29Esm+/eUacSba0k8wn5HhHg9d4tIcixrxveflc8vi2/wNQGVFNsGO6tB5WF0xf/plngOvQ1/ivGV/C1Qpdhzznh0ExAVJ6dwzNg7qIEBaw+BzTJTUuRcPk92Sn6QDn2Pu3mpONaEumacjW4w6ipPnPw+g2TfywJjeEcpSZaP4Q3YV5HG8D6UjWA4GSkBKculWpdCMadx0usMomsSS/74QgpYqcPkmamB4nVv1JxczYITIqItIKjD35IGKAUwAA==");
//
//            HttpEntity entity = HttpUtil.doGet(url, null, false, header);
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
            String resultOfXml = HttpUtil.doGet(redirectUrl, null, null, false,HttpResponse.BodyHandlers.ofString());

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

    /**
     * 登录
     *
     * @param =
     */
    //TODO 未实现
    @Override
    public void doNoScanLogin(String uin) throws Exception {

//            String url = redirectUrl + "&fun=new&version=v2&mod=desktop&lang=zh_CN";
//            Map<String, String> header = new HashMap<>();
//            //UOS header
//            header.put("client-version", "2.0.0");
//            header.put("extspam", "Go8FCIkFEokFCggwMDAwMDAwMRAGGvAESySibk50w5Wb3uTl2c2h64jVVrV7gNs06GFlWplHQbY/5FfiO++1yH4ykCyNPWKXmco+wfQzK5R98D3so7rJ5LmGFvBLjGceleySrc3SOf2Pc1gVehzJgODeS0lDL3/I/0S2SSE98YgKleq6Uqx6ndTy9yaL9qFxJL7eiA/R3SEfTaW1SBoSITIu+EEkXff+Pv8NHOk7N57rcGk1w0ZzRrQDkXTOXFN2iHYIzAAZPIOY45Lsh+A4slpgnDiaOvRtlQYCt97nmPLuTipOJ8Qc5pM7ZsOsAPPrCQL7nK0I7aPrFDF0q4ziUUKettzW8MrAaiVfmbD1/VkmLNVqqZVvBCtRblXb5FHmtS8FxnqCzYP4WFvz3T0TcrOqwLX1M/DQvcHaGGw0B0y4bZMs7lVScGBFxMj3vbFi2SRKbKhaitxHfYHAOAa0X7/MSS0RNAjdwoyGHeOepXOKY+h3iHeqCvgOH6LOifdHf/1aaZNwSkGotYnYScW8Yx63LnSwba7+hESrtPa/huRmB9KWvMCKbDThL/nne14hnL277EDCSocPu3rOSYjuB9gKSOdVmWsj9Dxb/iZIe+S6AiG29Esm+/eUacSba0k8wn5HhHg9d4tIcixrxveflc8vi2/wNQGVFNsGO6tB5WF0xf/plngOvQ1/ivGV/C1Qpdhzznh0ExAVJ6dwzNg7qIEBaw+BzTJTUuRcPk92Sn6QDn2Pu3mpONaEumacjW4w6ipPnPw+g2TfywJjeEcpSZaP4Q3YV5HG8D6UjWA4GSkBKculWpdCMadx0usMomsSS/74QgpYqcPkmamB4nVv1JxczYITIqItIKjD35IGKAUwAA==");
//
//            HttpEntity entity = HttpUtil.doGet(url, null, false, header);
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
            String resultOfXml = HttpUtil.doGet("https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxpushloginurl?uin="+uin, null, null,false, HttpResponse.BodyHandlers.ofString());

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


    }

    @Override
    public String getUuid() throws IOException, InterruptedException, WebWXException {
        // 组装参数和URL
        HashMap<String, String> params = new HashMap<>();
        params.put (WxReqParamsConstant.UUIDParaEnum.APP_ID.para(), WxReqParamsConstant.UUIDParaEnum.APP_ID.value());
        params.put (WxReqParamsConstant.UUIDParaEnum.FUN.para(), WxReqParamsConstant.UUIDParaEnum.FUN.value());
        params.put (WxReqParamsConstant.UUIDParaEnum.LANG.para(), WxReqParamsConstant.UUIDParaEnum.LANG.value());
        params.put (WxReqParamsConstant.UUIDParaEnum.LINE.para(), String.valueOf(System.currentTimeMillis()));

            String result = HttpUtil.doGet(WxURLEnum.UUID_URL.getUrl(), params,null,true, HttpResponse.BodyHandlers.ofString());
            String regEx = "window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"(\\S+?)\";";
            Matcher matcher = CommonTools.getMatcher(regEx, result);
            if (matcher.find()) {
                if (("200".equals(matcher.group(1)))) {
                    Core.setUuid(matcher.group(2));
                }else{
                    throw new WebWXException("get uuid return code {}"+matcher.group(1));
                }
            }else{
                throw new WebWXException("matcher not find.");
            }

        return Core.getUuid();
    }

    @Override
    public void getQR(String qrPath) throws IOException, InterruptedException {

        String qrUrl = WxURLEnum.QRCODE_URL.getUrl() + Core.getUuid();
         HttpUtil.doGet(qrUrl, null,null, true, HttpResponse.BodyHandlers.ofFile(Path.of(qrPath)));
            //二维码地址
        String qrUrl2 = WxURLEnum.cAPI_qrcode.getUrl() + Core.getUuid();

    }

    @Override
    public BufferedImage getQR() throws IOException, InterruptedException {
        String qrUrl = WxURLEnum.QRCODE_URL.getUrl() + Core.getUuid();

        BufferedImage image = ImageIO.read(HttpUtil.doGet(qrUrl, null, null,true, HttpResponse.BodyHandlers.ofInputStream()));
        return image;

    }

    @Override
    public void webWxInit() throws IOException, InterruptedException {
        Core.setLastNormalRetCodeTime(System.currentTimeMillis());
        // 组装请求URL和参数
        String url = String.format(WxURLEnum.INIT_URL.getUrl(),
                Core.getLoginResultData().getUrl(),
                System.currentTimeMillis() / 3158L,
                Core.getLoginResultData().getPassTicket());

        // 请求初始化接口
        WxInitReq wxInitReq = new WxInitReq();
        wxInitReq.setBaseRequest(Core.getLoginResultData().getBaseRequest());
            WxInitResponse wxInitResponse = HttpUtil.doPost(url, JSON.toJSONString(wxInitReq),HttpUtil.getJsonEntityBodyHandler(WxInitResponse.class));
            Contacts me = wxInitResponse.getUser();
            ;

            Core.getLoginResultData().setInviteStartCount(wxInitResponse.getInviteStartCount());
            Core.getLoginResultData().setSyncKeyObject(wxInitResponse.getSyncKey());


            Core.setUserName(me.getUsername());
            Core.setNickName(me.getNickname());
            Core.setUserSelf(me);
            ContactsTools.addContacts(me);
            //初始化列表的联系人
            //最近聊天的联系人

        Set<String> recentContacts = Core.getRecentContacts();
        Core.setWxInitResponse(wxInitResponse);
        for (Contacts contacts : wxInitResponse.getContactList()) {
                //下载头像
                ExecutorServiceUtil.getHeadImageDownloadExecutorService().submit(() -> {
                    AvatarUtil.createOrLoadUserAvatar(contacts.getUsername());
                });
                ContactsTools.addContacts(contacts);
                recentContacts.add(contacts.getUsername());
            }

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
            HttpUtil.doPost(url, paramStr, HttpResponse.BodyHandlers.ofString());
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
                        //=============加载群成员==============
                        MsgCenter.groupMsgFormat(msg);
                        Contacts contacts = loadUserInfo(msg);
                        msgCenter.handleNewMsg(msg, contacts);
                    });
                }
                //联系人修改
                ExecutorServiceUtil.getGlobalExecutorService().execute(() -> {

                    msgCenter.handleModContact(webWxSyncMsg.getModContactList());
                });
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
                log.error("未知消息：{}", webWxSyncMsg);
                break;

        }
    }

    @Override
    public void startReceiving() {
        Runnable runnable = () -> {
            while (true) {
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
                            //WeChatStater.restartApplication();
                            break;
                        }
                        case TICKET_ERROR:
                        case PARAM_ERROR:
                        case NOT_LOGIN_WARN:
                        case LOGIN_ENV_ERROR:
                        case TOO_OFEN: {
                            log.error(syncCheckRetCodeEnum.getType());
                            break;
                        }
                        default:
                            log.error("未知消息：{}", syncCheckResp);
                    }
                } catch (Exception e) {
                    log.error("消息同步错误：{}", e.getMessage(), e);
                    SleepUtils.sleep(1000);
                }

            }
        };
        ExecutorServiceUtil.getReceivingExecutorService().execute(runnable);
    }

    /**
     * 第一次收到群消息 加载群成员详细细腻
     *
     * @param msg 消息
     */
    private Contacts loadUserInfo(AddMsgList msg) {
        String userName = msg.getFromUserName();
        if (userName.equals(Core.getUserName())) {
            userName = msg.getToUserName();
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

            if (ContactsTools.isRoomContact(userName) ) {
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
            if (contacts != null ) {
                log.error("获取成功：{},{}", userName, contacts);
            }
        } else if (ContactsTools.isRoomContact(userName)
                && StringUtils.isNotEmpty(msg.getMemberName())) {
            //群成员发的消息
            if (Core.getMemberMap().containsKey(msg.getMemberName())){
                //群成员是我的好友，群信息没有当前成员则添加进去

                contacts = Core.getMemberMap().get(msg.getMemberName());
                if (ContactsTools.getMemberOfGroup(userName,msg.getMemberName()) == null && contacts != null) {
                    log.error("群用户或者群成员信息不完整，添加好友进去{}", contacts);
                    Core.getMemberMap().get(userName).getMemberlist().add(contacts);
                }

            }else if (CollectionUtils.isEmpty(contacts.getMemberlist())
                    || ContactsTools.getMemberOfGroup(userName,msg.getMemberName()) == null) {
                //群成员非好友 且 群里面没有该用户信息 则加载群成员数据

                log.error("群用户或者群成员信息不完整！{}，{}", contacts.getMemberlist().size(),userName);
                DownloadTask<Void> objectDownloadTask = new DownloadTask<>();
                objectDownloadTask.setTaskId("WebWxBatchGetContact:" + userName);
                objectDownloadTask.setGroupName(userName);
                objectDownloadTask.setType(DownloadType.GetBatchContacts);
                DownloadManager.submitAwait(objectDownloadTask);
                contacts =  Core.getMemberMap().get(userName);
                if (contacts != null && ContactsTools.getMemberOfGroup(userName,msg.getMemberName())!=null) {
                    log.error("获取成功：{},{}", userName, contacts);
                }
                if (ContactsTools.getMemberOfGroup(userName,msg.getMemberName())!=null) {
                    log.error("成员获取成功：{},{}", userName, contacts);
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



    @Override
    public void webWxGetContact() throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_GET_CONTACT.getUrl(),
                Core.getLoginResultData().getUrl());

            JSONObject fullFriendsJsonList = HttpUtil.doPost(url, JSON.toJSONString(Core.getLoginResultData().getBaseRequest()),HttpUtil.getJsonEntityBodyHandler(JSONObject.class));
            // 查看seq是否为0，0表示好友列表已全部获取完毕，若大于0，则表示好友列表未获取完毕，当前的字节数（断点续传）
            long seq = 0;
            long currentTime = 0L;
        HashMap<String, String> params = new HashMap<>();
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
                params.put("r", String.valueOf(currentTime));
                params.put("seq", String.valueOf(seq));

                fullFriendsJsonList = HttpUtil.doGet(url, params, null,false, HttpUtil.getJsonEntityBodyHandler(JSONObject.class));

                if (fullFriendsJsonList.get("Seq") != null) {
                    seq = fullFriendsJsonList.getLong("Seq");
                    currentTime = System.currentTimeMillis();
                }

                // 累加好友列表
                member.addAll(fullFriendsJsonList.getJSONArray(StorageLoginInfoEnum.MemberList.getKey()));
            }
        member.forEach(value -> {

                JSONObject o = (JSONObject) value;
                Contacts contacts = JSON.parseObject(JSON.toJSONString(o), Contacts.class);
            ContactsTools.addContacts(contacts);
            });
            if (!Core.getMemberMap().containsKey("filehelper")) {
                ContactsTools.addContacts(Contacts.builder().username("filehelper").displayname("文件传输助手")
                        .type(Contacts.ContactsType.ORDINARY_USER).build());
            }


    }


    @Override
    public void WebWxBatchGetContact(Set<String> groupName) throws IOException, InterruptedException {
        if (WebWxBatchGetContactExcept) {
            if (timer != null) {
                log.error("微信异常信息，暂时停止访问");
                return;

            }
        }
        synchronized (this) {
            if (WebWxBatchGetContactExcept) {
                if (timer != null) {
                    return;
                }
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        WebWxBatchGetContactExcept = false;
                    }
                }, 1000 * 60 * 30);
                log.error("微信异常信息，暂时停止访问");
                return;
            }
        }

        String url = String.format(WxURLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                Core.getLoginResultData().getUrl(), new Date().getTime(),
                Core.getLoginResultData().getPassTicket());


        List<Map<String, String>> queryList = groupName.stream().map(s -> {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("UserName", s);
            map.put("EncryChatRoomId", "");
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("Count", groupName.size());
        paramMap.put("List", queryList);
        paramMap.put("BaseRequest",Core.getLoginResultData().getBaseRequest());

        JSONObject obj = HttpUtil.doPost(url, JSON.toJSONString(paramMap), HttpUtil.getJsonEntityBodyHandler(JSONObject.class));
        if (obj.getJSONObject("BaseResponse").getInteger("Ret") == 1205) {
            //太频繁
            log.error("获取群信息失败：{}", obj.getJSONObject("BaseResponse"));
            WebWxBatchGetContactExcept = true;
            return;
        }
        //群列表
        obj.getJSONArray("ContactList").forEach(groupObject -> {

                // 群好友
                Contacts group = JSON.parseObject(JSON.toJSONString(groupObject), Contacts.class);
                String userName = group.getUsername();
                if (ContactsTools.isRoomContact(userName)) {
                    try {
                        Map<String, Contacts> oldMemberList = group.getMemberlist().stream().collect(Collectors.toMap(Contacts::getUsername, Function.identity()));
                        //详情接口不返回DisplayName
                        JSONArray memberArray = WebWxBatchGetContactGroupMemberDetail(group);
                        List<Contacts> memberList = JSON.parseArray(JSON.toJSONString(memberArray), Contacts.class);
                        for (Contacts contacts : memberList) {
                            contacts.setGroupName(userName);
                            contacts.setDisplayname(oldMemberList.get(contacts.getUsername()).getDisplayname());
                        }
                        if (!memberList.isEmpty()) {
                            group.setMemberlist(memberList);
                        }
                    } catch (Exception e) {
                        log.error("获取群成员信息失败：{}", groupObject,e);
                    }

                    //以上接口返回的成员属性不全，以下的接口获取群成员详细属性
                    if (group.getMemberlist().isEmpty()){
                        group.setMemberlist( Core.getMemberMap().get(userName).getMemberlist());

                    }
                    ContactsTools.addContacts(group);
                }

        });


    }

    @Override
    public void WebWxBatchGetContact() throws IOException, InterruptedException {
        WebWxBatchGetContact(Core.getMemberMap().keySet().stream().filter(ContactsTools::isRoomContact).collect(Collectors.toSet()));
    }

    @Override
    public void WebWxBatchGetContact(String groupName) throws IOException, InterruptedException {
        WebWxBatchGetContact(Sets.newHashSet(groupName));
    }

    /**
     * 获取每个群的群成员详细信息
     *
     * @param group 群对象
     * @return
     */
    @Override
    public JSONArray WebWxBatchGetContactGroupMemberDetail(Contacts group) {

        String url = String.format(WxURLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                Core.getLoginResultData().getUrl(), System.currentTimeMillis(),
                Core.getLoginResultData().getPassTicket());

        List<Map<String, String>> list = new ArrayList<Map<String, String>>(group.getMemberlist().size());
        for (Contacts o : group.getMemberlist()) {
            //遍历群成员
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("UserName", o.getUsername());
            map.put("EncryChatRoomId", group.getUsername());
            list.add(map);
        }

        return splitIntoGroups(list, 50).parallelStream().map(subList -> {
            try {
                Map<String, Object> paramMap = new HashMap<>();
                paramMap.put("BaseRequest", Core.getLoginResultData().getBaseRequest());
                paramMap.put("Count", subList.size());
                paramMap.put("List", subList);
                JSONObject obj = HttpUtil.doPost(url, JSON.toJSONString(paramMap), HttpUtil.getJsonEntityBodyHandler(JSONObject.class));
                if (obj.getJSONObject("BaseResponse").getInteger("Ret") != 0) {
                    log.error("获取群信息失败：{}，{}", group.getNickname(), obj.getJSONObject("BaseResponse"));
                    throw new RuntimeException("获取群信息失败：" + group.getNickname() + ":" + obj.getJSONObject("BaseResponse"));
                }
                return obj.getJSONArray("ContactList");
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }).flatMap(Collection::stream).collect(Collectors.toCollection(JSONArray::new));

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
    private WebWxSyncResp webWxSync() throws IOException, InterruptedException, WebWXException {
        String url = String.format(WxURLEnum.WEB_WX_SYNC_URL.getUrl(),
                Core.getLoginResultData().getUrl(),
                Core.getLoginResultData().getBaseRequest().getWxSid(),
                Core.getLoginResultData().getBaseRequest().getSKey(),
                Core.getLoginResultData().getPassTicket());

        WxSyncReq wxSyncReq = WxSyncReq.builder().SyncKey(Core.getLoginResultData().getSyncKeyObject())
                .rr(-System.currentTimeMillis() / 1000)
                .BaseRequest(Core.getLoginResultData().getBaseRequest()).build();
        String paramStr = JSON.toJSONString(wxSyncReq);

        WebWxSyncResp webWxSyncMsg = HttpUtil.doPost(url, paramStr,HttpUtil.getJsonEntityBodyHandler(WebWxSyncResp.class));
        if (webWxSyncMsg.getBaseResponse().getRet() != 0) {
            throw new WebWXException("消息同步失败！");
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
    private SyncCheckResp syncCheck() throws IOException, InterruptedException, WebWXException {
        // 组装请求URL和参数
        String url = String.format(WxURLEnum.SYNC_CHECK_URL.getUrl(), Core.getLoginResultData().getSyncUrl());
        HashMap<String, String> params = new HashMap<>();
        params.put(WxReqParamsConstant.SyncCheckParaEnum.R.para(), String.valueOf(System.currentTimeMillis()));
        params.put (WxReqParamsConstant.SyncCheckParaEnum.S_KEY.para(), Core.getLoginResultData().getBaseRequest().getSKey());
        params.put (WxReqParamsConstant.SyncCheckParaEnum.SID.para(), Core.getLoginResultData().getBaseRequest().getWxSid());
        params.put (WxReqParamsConstant.SyncCheckParaEnum.UIN.para(), Core.getLoginResultData().getBaseRequest().getWxUin());
        params.put (WxReqParamsConstant.SyncCheckParaEnum.DEVICE_ID.para(), Core.getLoginResultData().getBaseRequest().getDeviceId());
        params.put(WxReqParamsConstant.SyncCheckParaEnum.SYNC_KEY.para(), Core.getLoginResultData().getSyncKey());
        params.put (WxReqParamsConstant.SyncCheckParaEnum.LINE.para(), String.valueOf(System.currentTimeMillis()));
        SleepUtils.sleep(7);
        String result = HttpUtil.doGet(url, params, true, HttpResponse.BodyHandlers.ofString(), 30 * 1000L);

        String regEx = "window.synccheck=\\{retcode:\"(\\d+)\",selector:\"(\\d+)\"\\}";
        Matcher matcher = CommonTools.getMatcher(regEx, result);
        if (!matcher.find()) {
            throw new WebWXException("Unexpected sync check result: " + result);
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
        String url = String.format(WxURLEnum.WEB_WX_CREATE_ROOM.getUrl(),
                Core.getLoginResultData().getUrl(),
                System.currentTimeMillis());
        WxCreateRoomReq createRoomReq = WxCreateRoomReq.builder().BaseRequest(Core.getLoginResultData().getBaseRequest())
                .MemberCount(contacts.size())
                .MemberList(contacts.stream()
                        .map(e-> WxCreateRoomReq.NewRoomMember
                                .builder()
                                .UserName(e.getUsername()).build())
                        .collect(Collectors.toList()))
                .Topic("")
                .build();
        return HttpUtil.doPost(url, JSON.toJSONString(createRoomReq),HttpUtil.getJsonEntityBodyHandler(WxCreateRoomResp.class));
    }
}
