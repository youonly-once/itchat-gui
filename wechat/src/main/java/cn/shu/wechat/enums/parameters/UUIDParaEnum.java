package cn.shu.wechat.enums.parameters;

/**
 * 获取UUID需要的参数
 * <p>
 *
 * @author xiaoxiaomo
 * @date 2017/5/7
 */
public enum UUIDParaEnum {

    APP_ID("appid", "wx782c26e4c19acffb"),
    FUN("fun", "new"),
    LANG("lang", "zh_CN"),
    _("_", "时间戳");

    private String para;
    private String value;

    UUIDParaEnum(String para, String value) {
        this.para = para;
        this.value = value;
    }

    public String para() {
        return para;
    }

    public String value() {
        return value;
    }
}
