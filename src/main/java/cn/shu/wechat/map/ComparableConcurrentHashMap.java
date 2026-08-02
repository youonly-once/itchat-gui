package cn.shu.wechat.map;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import cn.shu.wechat.utils.ExecutorServiceUtil;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ComparableConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {
    public ComparableConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public V put(K key, V newVal) {
        // 问题5A：compute 保证原子读-替换；compare 逻辑异步执行，避免重映射函数内做阻塞 IO 持有桶锁时间过长
        if (Core.isCompare() && newVal instanceof Contacts) {
            Contacts contacts = (Contacts) newVal;
            if (contacts.getType() == Contacts.ContactsType.ORDINARY_USER) {
                return super.compute(key, (k, oldVal) -> {
                    if (oldVal != null) {
                        Contacts old = (Contacts) oldVal;
                        ExecutorServiceUtil.getGlobalExecutorService().submit(() -> {
                            try {
                                ContactsTools.compare(old, contacts);
                            } catch (Exception e) {
                                log.error("异步比较联系人属性异常：{}，key={}", e.getMessage(), key, e);
                            }
                        });
                    }
                    return newVal;
                });
            }
        }
        return super.put(key, newVal);
    }
}
