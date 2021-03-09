package cn.shu.wechat.face;

import cn.shu.wechat.api.MessageTools;

import cn.shu.wechat.beans.msg.sync.AddMsgList;

import java.util.List;

/**
 * 消息处理接口
 *
 * @author ShuXinSheng
 * @date 创建时间：2017年4月20日 上午12:13:49
 * @version 1.1
 *
 */
public interface IMsgHandlerFace {
	/**
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月20日 上午12:15:00
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> textMsgHandle(AddMsgList msg);

	/**
	 * 处理图片消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月21日 下午11:07:06
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> picMsgHandle(AddMsgList msg);

	/**
	 * 处理声音消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月22日 上午12:09:44
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> voiceMsgHandle(AddMsgList msg);

	/**
	 * 处理小视频消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月23日 下午12:19:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> videoMsgHandle(AddMsgList msg);

	/**
	 * 处理名片消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年5月1日 上午12:50:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> nameCardMsgHandle(AddMsgList msg);
	/**
	 * 处理撤回消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年5月1日 上午12:50:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> undoMsgHandle(AddMsgList msg) ;

	/**
	 * 处理好友确认消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年5月1日 上午12:50:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> addFriendMsgHandle(AddMsgList msg);

	/**
	 * 处理好友确认消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年5月1日 上午12:50:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> systemMsgHandle(AddMsgList msg);

	/**
	 * 表情处理消息
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> emotionMsgHandle(AddMsgList msg);
	/**
	 * 分享链接信息
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> appMsgHandle(AddMsgList msg);
	/**
	 * map消息
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> mapMsgHandle(AddMsgList msg);

	/**
	 * 处理确认添加好友消息
	 *
	 * @date 2017年6月28日 下午10:15:30
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> verifyAddFriendMsgHandle(AddMsgList msg);


}
