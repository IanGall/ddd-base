package cn.iantech.test.flow;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BusinessFlowRunnerTest {
    @Test
    void shouldPassTypedValueBetweenSteps() {
        FlowKey<String> token = new FlowKey<>("token", String.class);
        BusinessFlow flow = BusinessFlow.builder("登录并查询")
                .then("登录", context -> context.put(token, "access-token"))
                .then("查询", context -> assertThat(context.require(token)).isEqualTo("access-token"))
                .build();
        FlowContext context = new BusinessFlowRunner().run(flow);
        assertThat(context.require(token)).isEqualTo("access-token");
    }

    @Test
    void shouldStopAfterFailedStep() {
        AtomicBoolean nextStepExecuted = new AtomicBoolean();
        BusinessFlow flow = BusinessFlow.builder("失败流程")
                .then("失败步骤", context -> {
                    throw new AssertionError("业务断言失败");
                })
                .then("后续步骤", context -> nextStepExecuted.set(true))
                .build();
        assertThatThrownBy(() -> new BusinessFlowRunner().run(flow)).isInstanceOf(BusinessFlowRunner.FlowExecutionException.class)
                .hasMessageContaining("失败流程").hasMessageContaining("失败步骤");
        assertThat(nextStepExecuted).isFalse();
    }
}
