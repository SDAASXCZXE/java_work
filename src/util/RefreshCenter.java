package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单的刷新中心，用于不同 UI 组件之间发送轻量级刷新/通知事件。
 * 组件可通过 register 注册 Runnable，当触发 notify 时会执行所有注册的回调。
 */
public class RefreshCenter {
    private static final Map<String, List<Runnable>> listeners = new HashMap<>();

    public static synchronized void register(String key, Runnable listener) {
        listeners.computeIfAbsent(key, k -> new ArrayList<>()).add(listener);
    }

    public static synchronized void unregister(String key, Runnable listener) {
        List<Runnable> list = listeners.get(key);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                listeners.remove(key);
            }
        }
    }

    public static synchronized void notify(String key) {
        List<Runnable> list = listeners.get(key);
        if (list == null) return;
        // 复制避免并发修改
        List<Runnable> copy = new ArrayList<>(list);
        for (Runnable r : copy) {
            try {
                r.run();
            } catch (Exception ignored) {
            }
        }
    }
}

