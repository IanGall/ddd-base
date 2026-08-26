package cn.iantech.test.flow;

import java.time.Duration;
import java.time.Instant;

/**
 * 在网关测试中顺序执行业务流程并提供带步骤轨迹的失败信息。
 */
public final class BusinessFlowRunner {
    public FlowContext run(BusinessFlow flow) {
        return run(flow, new FlowContext());
    }

    public FlowContext run(BusinessFlow flow, FlowContext context) {
        Instant started = Instant.now();
        flow.steps().stream().forEach(step -> {
            Instant stepStarted = Instant.now();
            try {
                step.action().accept(context);
            } catch (RuntimeException | AssertionError ex) {
                throw new FlowExecutionException(flow, step.name(), Duration.between(started, Instant.now()), Duration.between(stepStarted, Instant.now()), ex);
            }
        });
        return context;
    }

    public static final class FlowExecutionException extends AssertionError {
        public FlowExecutionException(BusinessFlow flow, String step, Duration total, Duration stepDuration, Throwable cause) {
            super("业务流程「" + flow.name() + "」步骤「" + step + "」失败，总耗时=" + total.toMillis() + "ms，步骤耗时=" + stepDuration.toMillis() + "ms", cause);
        }
    }
}
