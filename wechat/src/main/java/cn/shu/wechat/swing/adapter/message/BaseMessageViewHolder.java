package cn.shu.wechat.swing.adapter.message;

import cn.shu.wechat.swing.adapter.ViewHolder;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author 舒新胜
 * @date 13/06/2017
 */
public class BaseMessageViewHolder extends ViewHolder {
    public JLabel avatar = new JLabel();
    public JLabel time = new JLabel();
}
