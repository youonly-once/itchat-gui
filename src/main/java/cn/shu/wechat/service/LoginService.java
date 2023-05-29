package cn.shu.wechat.service;

import cn.shu.wechat.dto.response.WxCreateRoomResp;
import cn.shu.wechat.entity.Contacts;
import com.alibaba.fastjson.JSONArray;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

/**
 * 登陆服务接口
 *
 * @author SXS
 * @version 1.1
 * @date 创建时间：2017年5月13日 上午12:07:21
 */
public interface LoginService {


    /**
     * 获取UUID
     *
     * @return
     * @author SXS
     * @date 2017年5月13日 上午12:21:40
     */
    String getUuid();

    /**
     * 下周登录二维码图片
     *
     * @param qrPath 二维码图片保存路径
     * @return {@code true} 获取成功 {@code false} 获取失败
     * @author SXS
     * @date 2017年5月13日 上午12:13:51
     */
    boolean getQR(String qrPath);


    /**
     * 登录二维码图片
     *
     * @author SXS
     * @date 2017年5月13日 上午12:13:51
     */
    BufferedImage getQR();

    /**
     * 登陆
     *
     * @return {@code true} 获取成功 {@code false} 获取失败
     * @author SXS
     * @date 2017年5月13日 上午12:14:07
     */
    boolean preLogin(LoginCallBack loginCallBack) throws Exception;
    interface LoginCallBack{
        void CallBack(String loginInfo);
        void avatar(String avatarBase64);
    }

    /**
     * web初始化
     *
     * @return {@code true} 成功 {@code false} 失败
     * @author SXS
     * @date 2017年5月13日 上午12:14:13
     */
    boolean webWxInit();

    /**
     * 微信状态通知
     *
     * @author SXS
     * @date 2017年5月13日 上午12:14:24
     */
    void wxStatusNotify();

    /**
     * 接收消息
     *
     * @author SXS
     * @date 2017年5月13日 上午12:14:37
     */
    void startReceiving();

    /**
     * 获取微信联系人
     *
     * @author SXS
     * @date 2017年5月13日 下午2:26:18
     */
    void webWxGetContact();

    /**
     * 批量获取群成员信息
     *
     * @date 2017年6月22日 下午11:24:35
     */
    void WebWxBatchGetContact();


    /**
     * 创建群聊
     * @param contacts 联系人列表
     * @return
     */
    WxCreateRoomResp webWxCreateRoom(List<Contacts> contacts) throws IOException, Exception;
    /**
     * 获取群成员信息
     * @param groupName 群名
     */
    List<Contacts> WebWxBatchGetContact(String groupName);

    /**
     * 批量获取群成员详细信息
     * 和上个方法的区别在于这个方法可以获取群成员的性别、省市等信息
     * 一次只能获取50个，群成员是好友的可以不用获取
     *
     * @param group 群对象
     * @return 群成员数组
     * @date 2017年6月22日 下午11:24:35
     */
    JSONArray WebWxBatchGetContactDetail(Contacts group);

}
