package cn.shu.wechat.constant;

public enum DownloadType {

    FN("缩略图", "fn"),
    HEAD_IMAGE_BIG("大头像", "head_big"),
    RESOURCE_BY_MSGID("根据消息ID下载资源", "resource_msgid"),
    ByAbsoluteUrl("根据消息相对地址下载资源", "ByAbsoluteUrl"),
    ByRelativeUrl("根据绝对地址下载资源", "ByRelativeUrl"),
    RESOURCE_BY_USERNAME("根据消息ID下载资源", "resource_by_username"),
    ImgByMsgID("ImgByMsgID", "ImgByMsgID"),
    ImgByteByMsgID("ImgByteByMsgID", "ImgByteByMsgID");


    private final String description;
    private final String code;

    DownloadType(String description, String code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public static DownloadType fromCode(String code) {
        for (DownloadType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
