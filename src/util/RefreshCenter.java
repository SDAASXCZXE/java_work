/*
 * 文件：RefreshCenter.java
 * 说明：应用内的简单事件/刷新中心（发布-订阅模式的轻量实现）。
 * 用途：模块之间可以通过注册键（String）来订阅刷新回调，当某些数据更新时触发通知。
 * 注意：仅添加注释，不改动现有代码逻辑。
 */

package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefreshCenter {
    private static final Map<String, List<Runnable>> listeners = new HashMap<>();

    public static void register(String key, Runnable callback) {
        listeners.computeIfAbsent(key, k -> new ArrayList<>()).add(callback);
    }

    public static void notify(String key) {
        List<Runnable> list = listeners.get(key);
        if (list != null) {
            for (Runnable r : new ArrayList<>(list)) {
                try { r.run(); } catch (Exception ignored) {}
            }
        }
    }
}
