package cn.shu.wechat.swing.helper;

import cn.shu.wechat.swing.adapter.message.MessageMouseListener;
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
import cn.shu.wechat.swing.adapter.message.voice.MessageLeftVoiceViewHolder;
import cn.shu.wechat.swing.adapter.message.voice.MessageRightVoiceViewHolder;
import cn.shu.wechat.swing.panels.left.tabcontent.RoomsPanel;

import javax.swing.*;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

/**
 * 提供消息ViewHolder缓存
 * <p>
 * <p>对消息的ViewHolder进入缓存能大大加速消息列表的加载速度，在刚进入房间时，默认先加载10条消息，
 * 这10条消息的ViewHolder将从缓存中获取，避免了new ViewHolder花费的时间。</p>
 * <p>
 * <p>在新进入新的房间时，{@link RoomsPanel#enterRoom(String)}
 * 方法将会调用本类的{@link MessageViewHolderCacheHelper#reset()} 方法，
 * 对上一个房间所使用的ViewHolder对象进行释放，从而实现循环使用缓存的ViewHolder</p>
 * <p>
 * <p>默认初始缓存容量为10。</p>
 * <p>
 * Created by 舒新胜 on 2017/6/24.
 */
public class MessageViewHolderCacheHelper {
    private final int CACHE_CAPACITY = 10;

    private List<MessageRightTextViewHolder> rightTextViewHolders = new ArrayList<>();
    private List<MessageRightImageViewHolder> rightImageViewHolders = new ArrayList<>();
    private List<MessageRightAttachmentViewHolder> rightAttachmentViewHolders = new ArrayList<>();

    private final List<MessageLeftTextViewHolder> leftTextViewHolders = new ArrayList<>();
    private final List<MessageLeftImageViewHolder> leftImageViewHolders = new ArrayList<>();
    private final List<MessageLeftVideoViewHolder> leftVideoViewHolders = new ArrayList<>();
    private final List<MessageRightVideoViewHolder> rightVideoViewHolders = new ArrayList<>();
    private final List<MessageLeftVoiceViewHolder> leftVoiceViewHolders = new ArrayList<>();
    private final List<MessageRightVoiceViewHolder> rightVoiceViewHolders = new ArrayList<>();

    private final List<MessageLeftProgramOfAppViewHolder> leftProgramOfAppViewHolders = new ArrayList<>();
    private final List<MessageRightProgramOfAppViewHolder> rightProgramOfAppViewHolders = new ArrayList<>();
    private final List<MessageLeftAttachmentViewHolder> leftAttachmentViewHolders = new ArrayList<>();

    private List<MessageSystemMessageViewHolder> systemMessageViewHolders = new ArrayList<>();

    private int rightTextPosition = 0;
    private int rightImagePosition = 0;
    private int rightAttachmentPosition = 0;
    private int leftTextPosition = 0;
    private int leftImagePosition = 0;
    private int leftVideoPosition = 0;
    private int leftVoicePosition = 0;
    private int rightVideoPosition = 0;
    private int rightVoicePosition = 0;

    private int leftProgramOfAppPosition = 0;
    private int rightProgramOfAppPosition = 0;
    private int leftAttachmentPosition = 0;
    private int systemMessagePosition = 0;


    public MessageViewHolderCacheHelper() {
        initHolders();
    }

    private void initHolders() {
   /*     new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                long startTime = System.currentTimeMillis();
                initRightTextViewHolders();
                initRightImageViewHolders();
                initRightAttachmentViewHolders();
                initLeftTextViewHolders();
                initLeftImageViewHolders();
                initLeftAttachmentViewHolders();
                initSystemMessageViewHolders();
            }
        }).start();*/
    }

    private void initRightTextViewHolders() {
        for (int i = 0; i < CACHE_CAPACITY; i++) {
            rightTextViewHolders.add(new MessageRightTextViewHolder());
        }
    }

    private void initRightImageViewHolders() {
        for (int i = 0; i < CACHE_CAPACITY; i++) {
            rightImageViewHolders.add(new MessageRightImageViewHolder());
        }
    }

    private void initRightAttachmentViewHolders() {
        for (int i = 0; i < CACHE_CAPACITY; i++) {
            rightAttachmentViewHolders.add(new MessageRightAttachmentViewHolder());
        }
    }


    private void initLeftTextViewHolders(boolean isGroup) {
        for (int i = 0; i < CACHE_CAPACITY; i++) {
            leftTextViewHolders.add(new MessageLeftTextViewHolder(isGroup));
        }
    }

    private void initLeftImageViewHolders(boolean isGroup) {
        for (int i = 0; i < CACHE_CAPACITY; i++) {
            leftImageViewHolders.add(new MessageLeftImageViewHolder(isGroup));
        }
    }

    private void initLeftAttachmentViewHolders(boolean isGroup) {
        for (int i = 0; i < CACHE_CAPACITY; i++) {
            leftAttachmentViewHolders.add(new MessageLeftAttachmentViewHolder(isGroup));
        }
    }

    private void initSystemMessageViewHolders() {
        for (int i = 0; i < CACHE_CAPACITY; i++) {
            systemMessageViewHolders.add(new MessageSystemMessageViewHolder());
        }
    }

    public synchronized MessageRightTextViewHolder tryGetRightTextViewHolder() {
        MessageRightTextViewHolder holder = null;
        if (rightTextPosition < CACHE_CAPACITY && rightTextViewHolders.size() > 0) {
            holder = rightTextViewHolders.get(rightTextPosition);
            rightTextPosition++;
        }

        return holder;
    }

    public synchronized MessageRightImageViewHolder tryGetRightImageViewHolder() {
        MessageRightImageViewHolder holder = null;
        if (rightImagePosition < CACHE_CAPACITY && rightImageViewHolders.size() > 0) {
            holder = rightImageViewHolders.get(rightImagePosition);
            rightImagePosition++;
        }

        return holder;
    }

    public synchronized MessageRightAttachmentViewHolder tryGetRightAttachmentViewHolder() {
        MessageRightAttachmentViewHolder holder = null;
        if (rightAttachmentPosition < CACHE_CAPACITY && rightAttachmentViewHolders.size() > 0) {
            holder = rightAttachmentViewHolders.get(rightAttachmentPosition);
            rightAttachmentPosition++;
        }

        return holder;
    }

    public synchronized MessageLeftTextViewHolder tryGetLeftTextViewHolder() {
        MessageLeftTextViewHolder holder = null;
        if (leftTextPosition < CACHE_CAPACITY && leftTextViewHolders.size() > 0) {
            holder = leftTextViewHolders.get(leftTextPosition);
            leftTextPosition++;
        }

        return holder;
    }

    public synchronized MessageLeftImageViewHolder tryGetLeftImageViewHolder() {
        MessageLeftImageViewHolder holder = null;
        if (leftImagePosition < CACHE_CAPACITY && leftImageViewHolders.size() > 0) {
            holder = leftImageViewHolders.get(leftImagePosition);
            leftImagePosition++;
        }

        return holder;
    }
    public synchronized MessageLeftVideoViewHolder tryGetLeftVideoViewHolder() {
        MessageLeftVideoViewHolder holder = null;
        if (leftVideoPosition < CACHE_CAPACITY && leftVideoViewHolders.size() > 0) {
            holder = leftVideoViewHolders.get(leftVideoPosition);
            leftVideoPosition++;
        }

        return holder;
    }
    public synchronized MessageRightVideoViewHolder tryGetRightVideoViewHolder() {
        MessageRightVideoViewHolder holder = null;
        if (rightVideoPosition < CACHE_CAPACITY && rightVideoViewHolders.size() > 0) {
            holder = rightVideoViewHolders.get(rightVideoPosition);
            rightVideoPosition++;
        }

        return holder;
    }
    public synchronized MessageLeftVoiceViewHolder tryGetLeftVoiceViewHolder() {
        MessageLeftVoiceViewHolder holder = null;
        if (leftVoicePosition < CACHE_CAPACITY && leftVoiceViewHolders.size() > 0) {
            holder = leftVoiceViewHolders.get(leftVoicePosition);
            leftVoicePosition++;
        }

        return holder;
    }
    public synchronized MessageRightVoiceViewHolder tryGetRightVoiceViewHolder() {
        MessageRightVoiceViewHolder holder = null;
        if (rightVoicePosition < CACHE_CAPACITY && rightVoiceViewHolders.size() > 0) {
            holder = rightVoiceViewHolders.get(rightVoicePosition);
            rightVoicePosition++;
        }

        return holder;
    }
    public synchronized MessageLeftProgramOfAppViewHolder tryGetLeftProgramOfAppViewHolder() {
        MessageLeftProgramOfAppViewHolder holder = null;
        if (leftProgramOfAppPosition < CACHE_CAPACITY && leftProgramOfAppViewHolders.size() > 0) {
            holder = leftProgramOfAppViewHolders.get(leftProgramOfAppPosition);
            leftProgramOfAppPosition++;
        }

        return holder;
    }
    public synchronized MessageRightProgramOfAppViewHolder tryGetRightProgramOfAppViewHolder() {
        MessageRightProgramOfAppViewHolder holder = null;
        if (rightProgramOfAppPosition < CACHE_CAPACITY && rightProgramOfAppViewHolders.size() > 0) {
            holder = rightProgramOfAppViewHolders.get(rightProgramOfAppPosition);
            rightProgramOfAppPosition++;
        }

        return holder;
    }
    public synchronized MessageLeftAttachmentViewHolder tryGetLeftAttachmentViewHolder() {
        MessageLeftAttachmentViewHolder holder = null;
        if (leftAttachmentPosition < CACHE_CAPACITY && leftAttachmentViewHolders.size() > 0) {
            holder = leftAttachmentViewHolders.get(leftAttachmentPosition);
            leftAttachmentPosition++;
        }

        return holder;
    }

    public synchronized MessageSystemMessageViewHolder tryGetSystemMessageViewHolder() {
        MessageSystemMessageViewHolder holder = null;
        if (systemMessagePosition < CACHE_CAPACITY && systemMessageViewHolders.size() > 0) {
            holder = systemMessageViewHolders.get(systemMessagePosition);
            systemMessagePosition++;
        }

        return holder;
    }

    public synchronized void reset() {
        //long start = System.currentTimeMillis();

        //for (MessageRightTextViewHolder viewHolder : rightTextViewHolders)
        for (int i = 0; i < rightTextPosition; i++) {
            MessageRightTextViewHolder viewHolder = rightTextViewHolders.get(i);
            clearMouseListener(viewHolder.messageBubble);
            clearMouseListener(viewHolder.resend);
            clearMouseListener(viewHolder.text);
        }

        //System.out.println("花费时间 ：" + (System.currentTimeMillis() - start));

        //for (MessageRightImageViewHolder viewHolder : rightImageViewHolders)
        for (int i = 0; i < rightImagePosition; i++) {
            MessageRightImageViewHolder viewHolder = rightImageViewHolders.get(i);
            clearMouseListener(viewHolder.image);
            clearMouseListener(viewHolder.imageBubble);
            clearMouseListener(viewHolder.resend);
        }

        //for (MessageRightAttachmentViewHolder viewHolder : rightAttachmentViewHolders)
        for (int i = 0; i < rightAttachmentPosition; i++) {
            MessageRightAttachmentViewHolder viewHolder = rightAttachmentViewHolders.get(i);
            clearMouseListener(viewHolder.resend);
            clearMouseListener(viewHolder.messageBubble);
            clearMouseListener(viewHolder.attachmentPanel);
            clearMouseListener(viewHolder.attachmentTitle);
        }

        //for (MessageLeftTextViewHolder viewHolder : leftTextViewHolders)
        for (int i = 0; i < leftTextPosition; i++) {
            MessageLeftTextViewHolder viewHolder = leftTextViewHolders.get(i);

            clearMouseListener(viewHolder.text);
            clearMouseListener(viewHolder.messageBubble);
            clearMouseListener(viewHolder.avatar);
        }

        //for (MessageLeftImageViewHolder viewHolder : leftImageViewHolders)
        for (int i = 0; i < leftImagePosition; i++) {
            MessageLeftImageViewHolder viewHolder = leftImageViewHolders.get(i);

            clearMouseListener(viewHolder.image);
            clearMouseListener(viewHolder.imageBubble);
            clearMouseListener(viewHolder.avatar);
        }

        //for (MessageLeftAttachmentViewHolder viewHolder : leftAttachmentViewHolders)
        for (int i = 0; i < leftAttachmentPosition; i++) {
            MessageLeftAttachmentViewHolder viewHolder = leftAttachmentViewHolders.get(i);

            clearMouseListener(viewHolder.attachmentPanel);
            clearMouseListener(viewHolder.attachmentTitle);
            clearMouseListener(viewHolder.messageBubble);
            clearMouseListener(viewHolder.avatar);
        }


        rightTextPosition = 0;
        rightImagePosition = 0;
        rightAttachmentPosition = 0;

        leftTextPosition = 0;
        leftImagePosition = 0;
        leftAttachmentPosition = 0;

        systemMessagePosition = 0;
    }

    private void clearMouseListener(JComponent component) {
        for (MouseListener l : component.getMouseListeners()) {
            if (l instanceof MessageMouseListener) {
                component.removeMouseListener(l);
            }
        }
    }

}
