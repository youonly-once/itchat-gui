package cn.shu.wechat.enums;


/**
 * 检测是否扫描登录接口 返回状态码
 * @author SXS
 */

public enum CheckLoginResultEnum {
    /**
     * 获取UUID成功....
     */
    SUCCESS("200", "成功"),
    /**
     * 扫描成功 但为确认
     */
    WAIT_CONFIRM("201", "请在手机上点击确认"),
    /**
     * 未扫描
     */
    WAIT_SCAN("400", "请扫描二维码");

    private String code;
    private String msg;

    CheckLoginResultEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }


}
