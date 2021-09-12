package cn.shu.wechat.swing.tasks;


/**
 * Created by 舒新胜 on 08/06/2017.
 */
public interface HttpResponseListener<T extends Object> {
    void onSuccess(T ret);

    void onFailed();
}
