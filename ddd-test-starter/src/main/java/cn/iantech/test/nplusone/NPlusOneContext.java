package cn.iantech.test.nplusone;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 供 JDBC、Dubbo 和 Feign 观测器访问当前测试会话。
 */
public final class NPlusOneContext {
    private static final AtomicReference<ObservationSession> ACTIVE = new AtomicReference<>();

    private NPlusOneContext() {
    }

    static void start(ObservationSession session) {
        if (!ACTIVE.compareAndSet(null, session))
            throw new IllegalStateException("同一 JVM 不允许并行执行多个 N+1 检测测试");
    }

    static void clear(ObservationSession session) {
        ACTIVE.compareAndSet(session, null);
    }

    public static void recordSelect(String sql) {
        ObservationSession session = ACTIVE.get();
        if (session != null) session.recordSelect(sql);
    }

    public static void recordRemote(String operation) {
        ObservationSession session = ACTIVE.get();
        if (session != null) session.recordRemote(operation);
    }
}
