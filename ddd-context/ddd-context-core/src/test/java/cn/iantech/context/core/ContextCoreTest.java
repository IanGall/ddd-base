package cn.iantech.context.core;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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

    // 验证渠道认证上下文字段会被规范化，非法值不会进入可信上下文
    @Test
    void shouldNormalizeChannelCredentialContextFields() {
        RequestContext context = new RequestContext(
                "request-1", null, null, null, "CLIENT", "channel-a",
                null, "gateway", null, " 9223372036854775807 ", "external:access", " 12 ");

        assertEquals("9223372036854775807", context.ownerAccountId());
        assertEquals("external:access", context.authorizedScope());
        assertEquals("12", context.credentialVersion());

        RequestContext invalid = new RequestContext(
                "request-2", null, null, null, "CLIENT", "channel-a",
                null, "gateway", null, "owner account", "integration access", "version\n2");
        assertNull(invalid.ownerAccountId());
        assertNull(invalid.authorizedScope());
        assertNull(invalid.credentialVersion());
    }

    @Test
    void shouldRecognizeEmptyAndEverySupportedContextField() {
        assertTrue(RequestContext.empty().isEmpty());

        Stream.of(
                        new RequestContext("request", null, null, null, null, null, null),
                        new RequestContext(null, "principal", null, null, null, null, null),
                        new RequestContext(null, null, "tenant", null, null, null, null),
                        new RequestContext(null, null, null, "user", null, null, null),
                        new RequestContext(null, null, null, null, "CLIENT", null, null, null, null),
                        new RequestContext(null, null, null, null, null, "client", null, null, null),
                        new RequestContext(null, null, null, null, null, null, null, null, null,
                                "owner", null, null),
                        new RequestContext(null, null, null, null, null, null, null, null, null,
                                null, "scope", null),
                        new RequestContext(null, null, null, null, null, null, null, null, null,
                                null, null, "version"),
                        new RequestContext(null, null, null, null, "gray", null, null),
                        new RequestContext(null, null, null, null, null, "source", null),
                        new RequestContext(null, null, null, null, null, null, "zh-CN"))
                .forEach(context -> assertFalse(context.isEmpty()));
    }

    @Test
    void shouldClearContextWhenOpeningNullOrEmptyContext() {
        RequestContext parent = new RequestContext("parent", null, null, null, null, null, null);

        try (ContextScope ignored = ContextAccessor.open(parent)) {
            try (ContextScope emptyScope = ContextAccessor.open(RequestContext.empty())) {
                assertTrue(ContextAccessor.current().isEmpty());
            }
            assertEquals(parent, ContextAccessor.current().orElseThrow());

            try (ContextScope nullScope = ContextAccessor.open(null)) {
                assertTrue(ContextAccessor.current().isEmpty());
            }
            assertEquals(parent, ContextAccessor.current().orElseThrow());
        }
    }

    @Test
    void shouldWrapCallableAndSupplierAndRestoreAfterFailure() throws Exception {
        RequestContext captured = new RequestContext("captured", null, null, null, null, null, null);
        ContextSnapshot snapshot;
        try (ContextScope ignored = ContextAccessor.open(captured)) {
            snapshot = ContextSnapshot.capture();
        }

        assertFalse(snapshot.isEmpty());
        assertEquals(captured, snapshot.context());
        Callable<RequestContext> callable = snapshot.wrap(
                (Callable<RequestContext>) () -> ContextAccessor.current().orElseThrow());
        assertEquals(captured, callable.call());

        Supplier<RequestContext> supplier = snapshot.wrap(
                (Supplier<RequestContext>) () -> ContextAccessor.current().orElseThrow());
        assertEquals(captured, supplier.get());

        RuntimeException failure = assertThrows(RuntimeException.class,
                () -> snapshot.wrap((Runnable) () -> {
                    throw new RuntimeException("任务失败");
                }).run());
        assertEquals("任务失败", failure.getMessage());
        assertTrue(ContextAccessor.current().isEmpty());
    }

    @Test
    void shouldRepresentEmptySnapshotAndRejectNullTasks() {
        ContextSnapshot snapshot = ContextSnapshot.capture();

        assertTrue(snapshot.isEmpty());
        assertTrue(snapshot.context().isEmpty());
        assertThrows(NullPointerException.class, () -> snapshot.wrap((Runnable) null));
        assertThrows(NullPointerException.class, () -> snapshot.wrap((java.util.concurrent.Callable<?>) null));
        assertThrows(NullPointerException.class, () -> snapshot.wrap((Supplier<?>) null));
    }
}
