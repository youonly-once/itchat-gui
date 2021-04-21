package cn.shu.wechat.controller;

import cn.shu.wechat.api.WeChatTool;
import cn.shu.wechat.utils.*;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.service.ILoginService;
import cn.shu.wechat.api.DownloadTools;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登陆控制器
 * <p>
 * <p>
 * 1、获取UUID
 * 2、下载二维码图片
 * 3、扫描登录
 * 4、初始化
 * 5、状态通知
 * 6、获取联系人
 * 7、监听消息
 * 8、下载头像
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年5月13日 下午12:56:07
 */
@Log4j2
@Controller
public class LoginController {
    /**
     * 登陆服务实现类
     */
    @Resource
    private ILoginService loginService;

    private static int count = 0;
    public void login(boolean dHImg) {
        // 防止SSL错误
        System.setProperty("jsse.enableSNIExtension", "false");
        String qrPath = Config.QR_PATH;
        boolean mkdirs = new File(qrPath).getParentFile().mkdirs();
        // 登陆
        while (true) {
            Process process = null;
            for (int count = 0; count < 10; count++) {
                log.info("获取UUID");
                while (true) {
                    log.info("1. 获取微信UUID");
                    String uuid = loginService.getUuid();
                    if (uuid != null) {
                        break;
                    }
                    log.warn("1.1. 获取微信UUID失败，两秒后重新获取");
                    SleepUtils.sleep(2000);
                }

                log.info("2. 获取登陆二维码图片");

                if (loginService.getQR(qrPath)) {
                    try {
                        // 使用图片查看器打开登陆二维码图片
                        process = CommonTools.printQr(qrPath);
                    } catch (Exception e) {
                        log.info(e.getMessage());
                        log.info("请手动打开二维码图片进行扫码登录：" + qrPath);
                    }
                    break;
                } else if (count == 9) {
                    log.error("2.2. 获取登陆二维码图片失败，系统退出");
                    System.exit(0);
                }
            }
            log.info("3. 请扫描二维码图片，并在手机上确认");
            if (!Core.isAlive()) {
                try {
                    loginService.login();
                } catch (Exception e) {
                    log.error(e.getMessage());
                    return;
                }
                //TODO 登录成功，关闭打开的二维码图片，暂时没有成功
                CommonTools.closeQr(process);
                Core.setAlive(true);
                log.info(("4、登陆成功"));
                break;
            }
            log.info("4. 登陆超时，请重新扫描二维码图片");
        }

        log.info("5. 登陆成功，微信初始化");
        if (!loginService.webWxInit()) {
            log.info("6. 微信初始化异常");
            System.exit(0);
        }

        log.info("6. 开启微信状态通知");
        loginService.wxStatusNotify();

        log.info("7. 清除。。。。");
        CommonTools.clearScreen();
        log.info(String.format("欢迎回来， %s", Core.getNickName()));

        log.info("8. 获取联系人信息");
        loginService.webWxGetContact();

        log.info("9. 获取群好友及群好友列表");
        loginService.WebWxBatchGetContact();

        log.info("10. 开始接收消息");
        loginService.startReceiving();


        log.info("11. 缓存本次登陆好友相关消息");
 /*       // 登陆成功后缓存本次登陆好友相关消息（NickName, UserName）
        WeChatTool.setUserInfo();*/
        //删除无效头像
        HeadImageDelete.deleteLoseEfficacyHeadImg(Config.PIC_DIR + "/headimg/");
        if (dHImg) {
            log.info("13. 下载联系人头像");
            for (Map.Entry<String, JSONObject> entry : Core.getMemberMap().entrySet()) {
                ExecutorServiceUtil.getHeadImageDownloadExecutorService().execute(
                        () -> {
                            Core.getContactHeadImgPath().put(entry.getValue().getString("UserName"), DownloadTools.downloadHeadImg(entry.getValue().getString("HeadImgUrl"), entry.getValue().getString("UserName")));
                            log.info("下载头像：({}):{}", entry.getValue().getString("NickName"), entry.getValue().getString("HeadImgUrl"));
                        });

            }
        }


        ExecutorServiceUtil.getHeadImageDownloadExecutorService().shutdown();
        ExecutorServiceUtil.getGlobalExecutorService().execute(() -> {
            try {
                //等待头像下载完成
                boolean b = ExecutorServiceUtil.getHeadImageDownloadExecutorService().awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("头像下载完成");
        });


    }

    /**
     * @param dHImg 是否下载头像
     * @return 提示信息
     */
    @RequestMapping("/reLogin")
    @ResponseBody
    public String reLogin(boolean dHImg) {
        //取消上次登录
        Core.setCancelPreLogin(true);
        ExecutorServiceUtil.getGlobalExecutorService().execute(
                () -> login(dHImg)
        );
        return "请扫描二维码登录！";
    }


    @RequestMapping("/test")
    @ResponseBody
    public String test() {
        log.info(++count);
        return "ok";
    }
}