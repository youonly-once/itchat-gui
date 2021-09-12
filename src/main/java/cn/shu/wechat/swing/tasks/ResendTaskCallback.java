package cn.shu.wechat.swing.tasks;

/**
 * Created by 舒新胜 on 14/06/2017.
 */
public abstract class ResendTaskCallback {
    long time;

    public ResendTaskCallback(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public abstract void onNeedResend(String retJson);
}