/**
 * Copyright 2021 bejson.com
 */
package cn.shu.wechat.beans.msg.sync;


import cn.shu.wechat.enums.WXReceiveMsgCodeEnum;
import lombok.Data;

/**
 * Auto-generated: 2021-02-22 13:35:59
 *
 * @author bejson.com (i@bejson.com)
 * @website http://www.bejson.com/java2pojo/
 */
@Data
public class AddMsgList {

    private String MsgId;
    private String FromUserName;
    private String ToUserName;
    private int MsgType;
    private String Content;
    private int Status;
    private int ImgStatus;
    private long CreateTime;
    private int VoiceLength;
    private int PlayLength;
    private String FileName;
    private String FileSize;
    private String MediaId;
    private String Url;
    private int AppMsgType;
    private int StatusNotifyCode;
    private String StatusNotifyUserName;
    private RecommendInfo RecommendInfo;
    private int ForwardFlag;
    private AppInfo AppInfo;
    private int HasProductId;
    private String Ticket;
    private int ImgHeight;
    private int ImgWidth;
    private int SubMsgType;
    private long NewMsgId;
    private String OriContent;
    private String EncryFileName;


    /**
     * 自己添加的变量
     */
    private String mentionMeUserNickName;
    private boolean mentionMe;
    private boolean groupMsg;
    /**
     * 文本消息内容
     **/
    private String text;
    private WXReceiveMsgCodeEnum Type;
    private String memberName;
    private String filePath;

}