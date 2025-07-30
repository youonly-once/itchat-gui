package cn.shu.wechat.swing.adapter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.util.Map;

/**
 * Created by 舒新胜 on 17-5-30.
 */
public abstract class BaseAdapter<T extends ViewHolder> {
    public int getCount() {
        return 0;
    }


    public abstract T onCreateViewHolder(int viewType,int subViewType, int position);

    public HeaderViewHolder onCreateHeaderViewHolder(int viewType, int position) {
        return null;
    }

    public int getItemViewType(int position) {
        return 0;
    }

    public boolean isGroup(int position) {
        return false;
    }

    public abstract void onBindViewHolder(T viewHolder, int position);

    public void onBindHeaderViewHolder(HeaderViewHolder viewHolder, int position) {
    }

    public Map<Integer, String> getPositionMap() {
        return null;
    }


    public int getItemSubViewType(int position){
        return 0;
    };

    public void removeAllListenersRecursively(Component comp) {
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                removeAllListenersRecursively(child);
            }
        }

        // 示例：移除常见的几种监听器（可扩展）
        if (comp instanceof JComponent jc  ) {
            for (MouseListener ml : jc.getMouseListeners()) {
                jc.removeMouseListener(ml);
            }
            for (KeyListener kl : jc.getKeyListeners()) {
                jc.removeKeyListener(kl);
            }
            for (FocusListener fl : jc.getFocusListeners()) {
                jc.removeFocusListener(fl);
            }
            for (FocusListener fl : jc.getFocusListeners()) {
                jc.removeFocusListener(fl);
            }
            // ... 其他类型监听器根据需要添加
        }
    }
}
