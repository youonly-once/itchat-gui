package cn.shu.wechat.swing.adapter;

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
}
