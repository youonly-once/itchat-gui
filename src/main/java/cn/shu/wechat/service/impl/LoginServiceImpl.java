package cn.shu.wechat.service.impl;

import cn.shu.WeChatStater;
import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.configuration.WechatConfiguration;
import cn.shu.wechat.constant.StorageLoginInfoEnum;
import cn.shu.wechat.constant.WxReqParamsConstant;
import cn.shu.wechat.constant.WxRespConstant;
import cn.shu.wechat.constant.WxURLEnum;
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
import cn.shu.wechat.utils.CommonTools;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import cn.shu.wechat.utils.HttpUtil;
import cn.shu.wechat.utils.SleepUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import jakarta.annotation.Resource;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.file.Path;
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
            WxInitResponse wxInitResponse = HttpUtil.doPost(url, JSON.toJSONString(wxInitReq),HttpUtil.getJsonEntityBodyHandler(WxInitResponse.class));
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
                    AvatarUtil.createOrLoadUserAvatar(contacts.getUsername());
                });
                addContacts(contacts);
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
                        msgCenter.handleNewMsg(msg);
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
            member.parallelStream().forEach(value -> {

                JSONObject o = (JSONObject) value;
                Contacts contacts = JSON.parseObject(JSON.toJSONString(o), Contacts.class);
                addContacts(contacts);
            });
            if (!Core.getMemberMap().containsKey("filehelper")) {
                Core.getMemberMap().put("filehelper",
                        Contacts.builder().username("filehelper").displayname("文件传输助手")
                                .type(Contacts.ContactsType.ORDINARY_USER).build());
            }

    }

    /**
     * 添加联系人
     */
    private void addContacts(Contacts contacts) {

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
            // 普通联系人
            Core.getContactMap().put(userName, contacts);
        }
    }

    @Override
    public void WebWxBatchGetContact() throws IOException, InterruptedException {
        String url = String.format(WxURLEnum.WEB_WX_BATCH_GET_CONTACT.getUrl(),
                Core.getLoginResultData().getUrl(), new Date().getTime(),
                Core.getLoginResultData().getPassTicket());
        Map<String, Object> paramMap = new HashMap<>();
        Core.getGroupIdSet().addAll(Core.getGroupMap().keySet());
        paramMap.put("Count", Core.getGroupIdSet().size());
        List<Map<String, String>> list = Core.getGroupIdSet().parallelStream().map(s -> {
            HashMap<String, String> map = new HashMap<String, String>(2);
            map.put("UserName", s);
            map.put("EncryChatRoomId", "");
            return map;
        }).collect(Collectors.toList());
        paramMap.put("List", list);
        paramMap.put("BaseRequest",Core.getLoginResultData().getBaseRequest());


            JSONObject obj =  HttpUtil.doPost(url, JSON.toJSONString(paramMap),HttpUtil.getJsonEntityBodyHandler(JSONObject.class));
            //群列表
            obj.getJSONArray("ContactList").parallelStream().forEach(groupObject -> {
                // 群好友
                Contacts group = JSON.parseObject(JSON.toJSONString(groupObject), Contacts.class);
                String userName = group.getUsername();
                if (ContactsTools.isRoomContact(userName)) {
                    //以上接口返回的成员属性不全，以下的接口获取群成员详细属性
                    JSONArray memberArray = WebWxBatchGetContactDetail(group);
                    List<Contacts> memberList = JSON.parseArray(JSON.toJSONString(memberArray), Contacts.class);
                    group.setMemberlist(memberList);
                    Core.getMemberMap().put(userName, group);
                    Core.getGroupMap().put(userName, group);
                }
            });


    }

    @Override
    public List<Contacts> WebWxBatchGetContact(String groupName) throws IOException, InterruptedException {

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
        paramMap.put("BaseRequest", Core.getLoginResultData().getBaseRequest());


            JSONObject obj = HttpUtil.doPost(url, JSON.toJSONString(paramMap),HttpUtil.getJsonEntityBodyHandler(JSONObject.class));
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

            try {
                JSONObject obj = HttpUtil.doPost(url, JSON.toJSONString(paramMap),HttpUtil.getJsonEntityBodyHandler(JSONObject.class));
                JSONArray contactListArray = obj.getJSONArray("ContactList");
                memberArray.addAll(contactListArray);
            } catch (IOException | InterruptedException e) {
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
        String result = HttpUtil.doGet(url, params, null,true, HttpResponse.BodyHandlers.ofString());

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
