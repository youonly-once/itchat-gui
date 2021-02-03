package shu.cn.weichat.utils;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import shu.cn.weichat.api.WechatTools;
import shu.cn.weichat.beans.BaseMsg;
import shu.cn.weichat.core.Core;

/**
 * @作者 舒新胜
 * @项目 weixin
 * @创建时间 2/1/2021 2:28 PM
 */
@Log4j2
public class LogUtil {
    private static Core core = Core.getInstance();

    /**
     * 打印发给自己的消息
     * @param msg 消息内容
     * @param path 消息为文件时的文件路径
     * @return 日志内容
     */
    public static String  printFromMeg(BaseMsg msg,String path){
        String myUserName = core.getUserName();
        String myNickName = core.getNickName();
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
                    toUser = WechatTools.getGroupRemarkNameByUserName(msg.getToUserName())+"(Group)";
                }else{
                    toUser =  WechatTools.getContactRemarkNameByUserName(msg.getToUserName());
                }
            }

        }else{
            toUser = myNickName;
            if (msg.getGroupMsg()){
                //群成员昵称
                String groupUserNickNameOfGroup = WechatTools.getGroupUserNickNameOfGroup(msg.getFromUserName(), msg.getMemberName());
                fromUser = WechatTools.getGroupRemarkNameByUserName(msg.getFromUserName())+"("+groupUserNickNameOfGroup+")";
            }else{
                fromUser = WechatTools.getContactRemarkNameByUserName(msg.getFromUserName());

            }
        }
        return String.format("【%s ->>>>>>> %s: %s】 ===%s",fromUser,toUser
                        , StringUtils.isEmpty(path)?msg.getContent():path
                ,msg.toString());
    }

    /**
     * 打印发给自己的消息
     * @param msg 消息内容
     * @return 日志内容
     */
    public static String  printFromMeg(BaseMsg msg){
        return printFromMeg(msg,"");
    }

    /**
     * 打印发给自己的消息
     * @param fromUserName 发送者
     * @param content 发送内容
     * @return 日志内容
     */
    public static String  printFromMeg(String fromUserName,String content){
        String myNickName = core.getUserSelf().getString("NickName");
        core.getUserSelf().getString("UserName");
        String fromRemarkName = WechatTools.getContactRemarkNameByUserName(fromUserName);
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
            toUserName = WechatTools.getGroupRemarkNameByUserName(toUserName)+"(Group)";
        }else{
            toUserName = WechatTools.getContactRemarkNameByUserName(toUserName);
        }

        return String.format("【%s ->>>>>>> %s】: %s",core.getNickName(),toUserName , content) ;
    }
}
