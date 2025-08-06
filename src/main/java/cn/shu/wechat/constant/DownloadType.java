package cn.shu.wechat.constant;

public enum DownloadType {

    FN("其它资源", "fn"),
    HEAD_IMAGE_BIG("大头像", "head_big"),
    RESOURCE_BY_MSGID("根据消息ID下载资源", "resource_msgid"),
    ByAbsoluteUrl("绝对地址下载资源", "ByAbsoluteUrl"),
    ByRelativeUrl("相对地址下载头像资源", "ByRelativeUrl"),
    RESOURCE_BY_USERNAME("消息ID下载资源", "resource_by_username"),
    ImgByMsgID("消息ID下载图像", "ImgByMsgID"),
    ImgByteByMsgID("消息ID下载图像Bytes", "ImgByteByMsgID"),
    GetContacts("获取用户", "GetContacts"),
    GetBatchContacts("获取群成员", "GetBatchContacts"),
    GenerateVideoPic("生成视频缩略图", "GenerateVideoPic");


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
