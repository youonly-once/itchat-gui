package cn.shu.wechat.enums;


/**
 * 检测是否扫描登录接口 返回状态码
 *
 * @author SXS
 */

public enum CheckLoginResultEnum {
    /**
     * 获取UUID成功....
     */
    SUCCESS("200", "登录成功"),
    /**
     * 扫描成功 但为确认
     */
    WAIT_CONFIRM("201", "请在手机上点击确认登录"),
    /**
     * 未扫描
     */
    WAIT_SCAN("400", "请使用微信扫一扫以登录"),
    /**
     * 未扫描
     */
    NONE("408", "未知");
    private final String code;
    private final String msg;

    CheckLoginResultEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public static CheckLoginResultEnum getByCode(String code){
        for (CheckLoginResultEnum value : CheckLoginResultEnum.values()) {
            if (value.code.equals(code)){
                return value;
            }
        }
        throw new RuntimeException("未知类型："+code);
    }
    public String getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }

}
