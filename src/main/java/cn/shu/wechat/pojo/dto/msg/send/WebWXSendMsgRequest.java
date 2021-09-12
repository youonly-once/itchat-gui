package cn.shu.wechat.pojo.dto.msg.send;


import cn.shu.wechat.pojo.dto.msg.BaseRequest;

/**
 * @作者 舒新胜
 * @项目 AutoWeChat
 * @创建时间 3/10/2021 2:28 PM
 */


/**
 * 场景：需要返回的json格式的参数字段是首字母大写，JSON.toJSONString 后都变成了小写
 * <p>
 * 原因：JSON源码里，除了所有的属性代写后字段返回的才是大写，其余皆是默认自动返回小写；
 * <p>
 * 解决办法：
 * <p>
 * 1:所有的属性命名首字母大写，不写get方法（不符合命名规范）
 */
public class WebWXSendMsgRequest {
    public BaseRequest BaseRequest = new BaseRequest();

    public WebWXSendingMsg Msg;

    public int Scene;

    public static void main(String[] args) {
        WebWXSendingMsg webWXSendingMsg = new WebWXSendingTextMsg();
        System.out.println(webWXSendingMsg.Type);
    }
}
