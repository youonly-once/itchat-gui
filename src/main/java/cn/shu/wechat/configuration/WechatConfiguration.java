package cn.shu.wechat.configuration;

import cn.shu.wechat.core.Core;
import cn.shu.wechat.enums.OsNameEnum;
import cn.shu.wechat.utils.MD5Util;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "wechat")
public class WechatConfiguration {
    private String userAgent;
    private List<String> specialUser ;
    private String appVersion;
    private String basePath;
    private String autoChatPrefix;
    private String autoChatSuffix;
    /**
     * 是否模糊头像
     */
    private Boolean fuzzUpAvatar;
    private static WechatConfiguration instance;

    public WechatConfiguration() {
        instance = this;
    }

    public static WechatConfiguration getInstance(){
        return instance;
    }
    public String getLoginTitle() {
        return loginTitle;
    }

    public void setLoginTitle(String loginTitle) {
        this.loginTitle = loginTitle;
    }

    private String loginTitle;
    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getAutoChatPrefix() {
        return autoChatPrefix;
    }

    public void setAutoChatPrefix(String autoChatPrefix) {
        this.autoChatPrefix = autoChatPrefix;
    }

    public String getAutoChatSuffix() {
        return autoChatSuffix;
    }

    public void setAutoChatSuffix(String autoChatSuffix) {
        this.autoChatSuffix = autoChatSuffix;
    }

    /**
     * 获取系统平台
     *
     * @author ShuXinSheng
     * @date 2017年4月8日 下午10:27:53
     */
    public static OsNameEnum getOsNameEnum() {
        String os = System.getProperty("os.name").toUpperCase();
        if (os.contains(OsNameEnum.DARWIN.toString())) {
            return OsNameEnum.DARWIN;
        } else if (os.contains(OsNameEnum.WINDOWS.toString())) {
            return OsNameEnum.WINDOWS;
        } else if (os.contains(OsNameEnum.LINUX.toString())) {
            return OsNameEnum.LINUX;
        } else if (os.contains(OsNameEnum.MAC.toString())) {
            return OsNameEnum.MAC;
        }
        return OsNameEnum.OTHER;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public List<String> getSpecialUser() {
        return specialUser;
    }

    public void setSpecialUser(String specialUser) {
        String[] split = specialUser.split(",");
        this.specialUser = Arrays.asList(split);
    }

    public Boolean getFuzzUpAvatar() {
        return fuzzUpAvatar;
    }

    public void setFuzzUpAvatar(Boolean fuzzUpAvatar) {
        this.fuzzUpAvatar = fuzzUpAvatar;
    }
}
