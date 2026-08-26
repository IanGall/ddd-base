package cn.iantech.test.nplusone;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 扩展，在测试执行前后建立跨线程观测窗口。
 */
public final class NPlusOneExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(NPlusOneExtension.class);

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        DetectNPlusOne annotation = context.getElement().map(e -> e.getAnnotation(DetectNPlusOne.class)).orElse(null);
        if (annotation == null)
            annotation = context.getTestClass().map(c -> c.getAnnotation(DetectNPlusOne.class)).orElse(null);
        if (annotation != null) {
            ObservationSession session = new ObservationSession(annotation);
            NPlusOneContext.start(session);
            context.getStore(NAMESPACE).put("session", session);
        }
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        ObservationSession session = context.getStore(NAMESPACE).remove("session", ObservationSession.class);
        if (session == null) return;
        NPlusOneContext.clear(session);
        context.publishReportEntry("n-plus-one", session.summary());
        if (context.getExecutionException().isEmpty()) session.assertWithinLimits();
    }
}
