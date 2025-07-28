package cn.shu.wechat.map;

import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.entity.Contacts;
import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class ComparableConcurrentHashMap<K, V> extends ConcurrentHashMap<K, V> {
    public ComparableConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public V put(K key, V newVal) {
        V old = get(key);
        try {
            if (old != null && newVal instanceof Contacts && Core.isCompare()) {

                switch (((Contacts) newVal).getType()) {
                    case ORDINARY_USER -> {
                        ContactsTools.compare((Contacts) old, (Contacts) newVal);
                    }

                }

            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return super.put(key, newVal);
    }
}
