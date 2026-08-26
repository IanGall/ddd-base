package cn.iantech.test.flow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 网关层可组合的多接口业务流程定义。
 */
public final class BusinessFlow {
    private final String name;
    private final List<Step> steps;

    private BusinessFlow(String name, List<Step> steps) {
        this.name = name;
        this.steps = List.copyOf(steps);
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public String name() {
        return name;
    }

    public List<Step> steps() {
        return steps;
    }

    public record Step(String name, Consumer<FlowContext> action) {
    }

    public static final class Builder {
        private final String name;
        private final List<Step> steps = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder then(String stepName, Consumer<FlowContext> action) {
            if (stepName == null || stepName.isBlank() || action == null)
                throw new IllegalArgumentException("步骤名称和动作不能为空");
            if (steps.stream().anyMatch(step -> step.name().equals(stepName)))
                throw new IllegalArgumentException("步骤名称不能重复：" + stepName);
            steps.add(new Step(stepName, action));
            return this;
        }

        public BusinessFlow build() {
            if (name == null || name.isBlank() || steps.isEmpty())
                throw new IllegalArgumentException("流程名称和步骤不能为空");
            return new BusinessFlow(name, steps);
        }
    }
}
