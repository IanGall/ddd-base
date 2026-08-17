package cn.iantech.context.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContextCoreTest {

    // 验证嵌套作用域关闭后恢复父上下文，并在最外层关闭后完成清理
    @Test
    void shouldRestoreNestedScopeAndClearAfterClose() {
        RequestContext outer = new RequestContext("r1", "admin", null, null, null, "gateway", null);
        RequestContext inner = new RequestContext("r2", "service", null, null, "gray-a", "dubbo", "zh-CN");

        try (ContextScope ignored = ContextAccessor.open(outer)) {
            assertEquals(outer, ContextAccessor.current().orElseThrow());
            try (ContextScope nested = ContextAccessor.open(inner)) {
                assertEquals(inner, ContextAccessor.current().orElseThrow());
            }
            assertEquals(outer, ContextAccessor.current().orElseThrow());
        }
        assertFalse(ContextAccessor.current().isPresent());
    }

    // 验证上下文快照可传播任务上下文，并恢复执行线程原有上下文
    @Test
    void shouldPropagateContextWithSnapshotAndRestoreWorkerThreadContext() {
        RequestContext parent = new RequestContext("r1", "admin", null, null, null, "gateway", null);
        RequestContext worker = new RequestContext("r2", "worker", null, null, null, "worker", null);
        AtomicReference<RequestContext> captured = new AtomicReference<>();

        try (ContextScope ignored = ContextAccessor.open(parent)) {
            ContextSnapshot snapshot = ContextSnapshot.capture();
            try (ContextScope workerScope = ContextAccessor.open(worker)) {
                snapshot.wrap(() -> captured.set(ContextAccessor.current().orElseThrow())).run();
                assertEquals(worker, ContextAccessor.current().orElseThrow());
            }
        }
        assertEquals(parent, captured.get());
        assertFalse(ContextAccessor.current().isPresent());
    }

    // 验证非法字段不会进入请求上下文
    @Test
    void shouldIgnoreInvalidFieldsAndExcludeThemFromContext() {
        RequestContext context = new RequestContext("bad value", "admin", null, null, null, "gateway", "中文");
        assertNull(context.requestId());
        assertNull(context.locale());
    }
}
