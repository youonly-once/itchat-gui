package cn.shu.wechat.swing.helper;

import cn.shu.wechat.entity.Message;
import cn.shu.wechat.swing.adapter.message.app.MessageLeftAttachmentViewHolder;
import cn.shu.wechat.swing.adapter.message.app.MessageLeftProgramOfAppViewHolder;
import cn.shu.wechat.swing.adapter.message.app.MessageRightAttachmentViewHolder;
import cn.shu.wechat.swing.adapter.message.app.MessageRightProgramOfAppViewHolder;
import cn.shu.wechat.swing.adapter.message.image.MessageLeftImageViewHolder;
import cn.shu.wechat.swing.adapter.message.image.MessageRightImageViewHolder;
import cn.shu.wechat.swing.adapter.message.system.MessageSystemMessageViewHolder;
import cn.shu.wechat.swing.adapter.message.text.MessageLeftTextViewHolder;
import cn.shu.wechat.swing.adapter.message.text.MessageRightTextViewHolder;
import cn.shu.wechat.swing.adapter.message.video.MessageLeftVideoViewHolder;
import cn.shu.wechat.swing.adapter.message.video.MessageRightVideoViewHolder;
import cn.shu.wechat.swing.adapter.message.video.MessageVideoViewHolder;
import cn.shu.wechat.swing.adapter.message.voice.MessageLeftVoiceViewHolder;
import cn.shu.wechat.swing.adapter.message.voice.MessageRightVoiceViewHolder;
import cn.shu.wechat.utils.IconUtil;
import lombok.Getter;


public class MessageViewHolderCacheHelper {
    @Getter
    private static MessageViewHolderCacheHelper messageViewHolderCacheHelper = new MessageViewHolderCacheHelper();
    private MessageViewHolderCacheHelper() {

    }


    public MessageRightTextViewHolder tryGetRightTextViewHolder() {
        return new MessageRightTextViewHolder();

    }

    public MessageRightImageViewHolder tryGetRightImageViewHolder(Message messageItem) {
        if (messageItem.getImgWidth() == null||messageItem.getImgHeight() == null) {
            return new MessageRightImageViewHolder(null);
        }
        return new MessageRightImageViewHolder(IconUtil.getScaleDimension(messageItem.getImgWidth(),messageItem.getImgHeight()
                ,MessageRightImageViewHolder.maxWidth,MessageRightImageViewHolder.maxHeight));

    }

    public MessageRightAttachmentViewHolder tryGetRightAttachmentViewHolder() {

        return new MessageRightAttachmentViewHolder();

    }

    public MessageLeftTextViewHolder tryGetLeftTextViewHolder(Message messageItem) {

        return new MessageLeftTextViewHolder(messageItem.isGroup());

    }

    public MessageLeftImageViewHolder tryGetLeftImageViewHolder(Message messageItem) {
        if (messageItem.getImgWidth() == null||messageItem.getImgHeight() == null) {
            return new MessageLeftImageViewHolder(messageItem.isGroup(),null);
        }
        return new MessageLeftImageViewHolder(messageItem.isGroup(),IconUtil.getScaleDimension(messageItem.getImgWidth(),messageItem.getImgHeight()
                ,MessageRightImageViewHolder.maxWidth,MessageRightImageViewHolder.maxHeight));

    }

    public MessageLeftVideoViewHolder tryGetLeftVideoViewHolder(Message messageItem) {

        return new MessageLeftVideoViewHolder(messageItem.isGroup(),
                IconUtil.getScaleDimension(messageItem.getImgWidth()
                        , messageItem.getImgHeight(), MessageVideoViewHolder.maxWidth,MessageVideoViewHolder.maxHeight));

    }

    public MessageRightVideoViewHolder tryGetRightVideoViewHolder(Message messageItem) {

        return new MessageRightVideoViewHolder(
                IconUtil.getScaleDimension(messageItem.getImgWidth()
                        , messageItem.getImgHeight(),MessageVideoViewHolder.maxWidth,MessageVideoViewHolder.maxHeight));

    }

    public MessageLeftVoiceViewHolder tryGetLeftVoiceViewHolder(Message messageItem) {

        return new MessageLeftVoiceViewHolder(messageItem.isGroup());

    }

    public MessageRightVoiceViewHolder tryGetRightVoiceViewHolder() {

        return new MessageRightVoiceViewHolder();

    }

    public MessageLeftProgramOfAppViewHolder tryGetLeftProgramOfAppViewHolder(Message messageItem) {

        return new MessageLeftProgramOfAppViewHolder(messageItem.isGroup());

    }

    public MessageRightProgramOfAppViewHolder tryGetRightProgramOfAppViewHolder() {
        return new MessageRightProgramOfAppViewHolder();

    }

    public MessageLeftAttachmentViewHolder tryGetLeftAttachmentViewHolder(Message messageItem) {

        return new MessageLeftAttachmentViewHolder(messageItem.isGroup());
    }

    public MessageSystemMessageViewHolder tryGetSystemMessageViewHolder() {
        return new MessageSystemMessageViewHolder();
    }


}
