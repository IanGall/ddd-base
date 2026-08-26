package cn.iantech.test.flow;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程步骤间传递 DTO、Token 和业务 ID 的类型安全上下文。
 */
public final class FlowContext {
    private final Map<FlowKey<?>, Object> values = new ConcurrentHashMap<>();

    public <T> void put(FlowKey<T> key, T value) {
        values.put(key, key.type().cast(Objects.requireNonNull(value, "流程值不能为空")));
    }

    public <T> T require(FlowKey<T> key) {
        Object value = values.get(key);
        if (value == null) throw new IllegalStateException("流程上下文缺少键：" + key.name());
        return key.type().cast(value);
    }

    public <T> T get(FlowKey<T> key) {
        Object value = values.get(key);
        return value == null ? null : key.type().cast(value);
    }
}
