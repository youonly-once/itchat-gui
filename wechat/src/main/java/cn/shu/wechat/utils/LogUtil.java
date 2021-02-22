package cn.shu.wechat.utils;

import cn.shu.wechat.beans.sync.AddMsgList;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.utils.tools.DownloadTools;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import cn.shu.wechat.api.WechatTools;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/1/2021 2:28 PM
 */
@Log4j2
public class LogUtil {

    /**
     * 打印发给自己的消息
     * @param msg 消息内容
     * @param path 消息为文件时的文件路径
     * @return 日志内容
     */
    public static String  printFromMeg(AddMsgList msg, String path, String title){
        String myUserName = Core.getUserName();
        String myNickName = Core.getNickName();
        //自己发的消息
        String fromUser ="";
        String toUser ="";
        if (myUserName.equals(msg.getFromUserName())){
            if (myUserName.equals(msg.getToUserName())){
                //自己发给自己的消息
                toUser = myNickName;
                fromUser = myNickName;
            }else{
                fromUser = myNickName;
                if (msg.getGroupMsg()){
                    toUser = WechatTools.getRemarkNameByGroupUserName(msg.getToUserName())+"(Group)";
                }else{
                    toUser =  WechatTools.getRemarkNameByUserName(msg.getToUserName());
                }
            }

        }else{
            toUser = myNickName;
            if (msg.getGroupMsg()){
                //群成员昵称
                String groupUserNickNameOfGroup = WechatTools.getMemberNickNameOfGroup(msg.getFromUserName(), msg.getMemberName());
                fromUser = WechatTools.getRemarkNameByGroupUserName(msg.getFromUserName())+"("+groupUserNickNameOfGroup+")";
            }else{
                fromUser = WechatTools.getRemarkNameByUserName(msg.getFromUserName());

            }
        }
        return String.format(title+"【%s ->>>>>>> %s: %s】 ===%s",fromUser,toUser
                        , StringUtils.isEmpty(path)?msg.getContent():path
                ,msg.toString());
    }

    /**
     * 打印发给自己的消息
     * @param msg 消息内容
     * @return 日志内容
     */
    public static String  printFromMeg(AddMsgList msg,String title){
        return printFromMeg(msg,"",title);
    }

    /**
     * 打印发给自己的消息
     * @param fromUserName 发送者
     * @param content 发送内容
     * @return 日志内容
     */
    public static String  printFromMeg(String fromUserName,String content){
        String myNickName = Core.getUserSelf().getString("NickName");
        Core.getUserSelf().getString("UserName");
        String fromRemarkName = WechatTools.getRemarkNameByUserName(fromUserName);
        return String.format("【%s ->>>>>>> %s: %s】",fromRemarkName,myNickName , content) ;
    }
    /**
     * 打印发给别人的消息
     * @param toUserName 发送者
     * @param content 发送内容
     * @return 日志内容
     */
    public static String  printToMeg(String toUserName,String content){
        if (toUserName.startsWith("@@")){
            toUserName = WechatTools.getRemarkNameByGroupUserName(toUserName)+"(Group)";
        }else{
            toUserName = WechatTools.getRemarkNameByUserName(toUserName);
        }

        return String.format("【%s ->>>>>>> %s】: %s",Core.getNickName(),toUserName , content) ;
    }
}
