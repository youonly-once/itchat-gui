package cn.shu.wechat.constant;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/2/2021 3:16 PM
 */
public enum TulLingResultType {
    DEFAULT("default", "默认"),
    TEXT("text", "文本"),
    URL("url", "链接"),
    VOICE("voice", "语音"),
    VIDEO("video", "视频"),
    IMAGE("image", "图片"),
    NEWS("news", "图文");
    public String code;
    public String desc;

    TulLingResultType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TulLingResultType getByCode(String code) {
        for (TulLingResultType value : TulLingResultType.values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return DEFAULT;
    }
}
