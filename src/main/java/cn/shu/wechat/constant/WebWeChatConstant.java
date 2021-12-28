package cn.shu.wechat.constant;

/**
 * @Description
 * @Author SXS
 * @Date 2021/11/12 12:54
 */
public interface WebWeChatConstant {
    enum ContactFlag{
        /**
         * 联系人标志
         */
        CONTACTFLAG_CONTACT(0x01),
        CONTACTFLAG_CHATCONTACT ( 0x02),
        CONTACTFLAG_CHATROOMCONTACT ( 0x04),
        CONTACTFLAG_BLACKLISTCONTACT ( 0x08),
        CONTACTFLAG_DOMAINCONTACT ( 0x10),
        CONTACTFLAG_HIDECONTACT ( 0x20),
        CONTACTFLAG_FAVOURCONTACT ( 0x40),
        CONTACTFLAG_3RDAPPCONTACT ( 0x80),
        CONTACTFLAG_SNSBLACKLISTCONTACT ( 0x100),
        CONTACTFLAG_NOTIFYCLOSECONTACT ( 0x200),
        CONTACTFLAG_TOPCONTACT ( 0x800)
        ;
        public int CODE;

        ContactFlag(int code) {
            this.CODE = code;
        }
    }
    enum ChatRoomMute {
        /**
         * 消息免打扰
         */
        CHATROOM_NOTIFY_OPEN( 0x1),
        CHATROOM_NOTIFY_CLOSE ( 0x0);
        public int CODE;
        ChatRoomMute(int code) {
            this.CODE = code;
        }
    }
}
