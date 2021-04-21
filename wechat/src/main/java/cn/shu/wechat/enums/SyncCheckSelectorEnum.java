package cn.shu.wechat.enums;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 3/7/2021 10:36 PM
 */
public enum SyncCheckSelectorEnum {
    NORMAL("0", "正常"),
    NEW_MSG("2", "有新消息"),
    MOD_CONTACT("4", "有人修改了自己的昵称或你修改了别人的备注等信息"),
    A("3", "未知"),
    ADD_OR_DEL_CONTACT("6", "存在删除或者新增的好友信息"),
    ENTER_OR_LEAVE_CHAT("7", "进入或离开聊天界面"),
    DEFAULT("D", "");
    private String code;
    private String type;

    SyncCheckSelectorEnum(String code, String type) {
        this.code = code;
        this.type = type;
    }

    public static SyncCheckSelectorEnum getByCode(String code) {
        for (SyncCheckSelectorEnum value : SyncCheckSelectorEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return SyncCheckSelectorEnum.DEFAULT;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }
}
