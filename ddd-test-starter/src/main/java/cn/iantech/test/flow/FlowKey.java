package cn.iantech.test.flow;

/**
 * 流程上下文中的强类型键。
 */
public record FlowKey<T>(String name, Class<T> type) {
    public FlowKey {
        if (name == null || name.isBlank() || type == null)
            throw new IllegalArgumentException("流程键名称和类型不能为空");
    }
}
