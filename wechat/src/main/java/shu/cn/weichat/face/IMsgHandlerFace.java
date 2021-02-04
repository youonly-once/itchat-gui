package shu.cn.weichat.face;

import shu.cn.weichat.api.MessageTools;
import shu.cn.weichat.beans.BaseMsg;

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
	public List<MessageTools.Result> textMsgHandle(BaseMsg msg);

	/**
	 * 处理图片消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月21日 下午11:07:06
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> picMsgHandle(BaseMsg msg);

	/**
	 * 处理声音消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月22日 上午12:09:44
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> voiceMsgHandle(BaseMsg msg);

	/**
	 * 处理小视频消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年4月23日 下午12:19:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> videoMsgHandle(BaseMsg msg);

	/**
	 * 处理名片消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年5月1日 上午12:50:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> nameCardMsgHandle(BaseMsg msg);
	/**
	 * 处理撤回消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年5月1日 上午12:50:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> undoMsgHandle(BaseMsg msg) ;

	/**
	 * 处理好友确认消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年5月1日 上午12:50:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> addFriendMsgHandle(BaseMsg msg);

	/**
	 * 处理好友确认消息
	 *
	 * @author ShuXinSheng
	 * @date 2017年5月1日 上午12:50:50
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> systemMsgHandle(BaseMsg msg);

	/**
	 * 表情处理消息
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> emotionMsgHandle(BaseMsg msg);
	/**
	 * 分享链接信息
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> appMsgHandle(BaseMsg msg);

	/**
	 * 处理确认添加好友消息
	 *
	 * @date 2017年6月28日 下午10:15:30
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> verifyAddFriendMsgHandle(BaseMsg msg);

	/**
	 * 处理收到的文件消息
	 *
	 * @date 2017年7月21日 下午11:59:14
	 * @param msg
	 * @return
	 */
	public List<MessageTools.Result> mediaMsgHandle(BaseMsg msg);

}
