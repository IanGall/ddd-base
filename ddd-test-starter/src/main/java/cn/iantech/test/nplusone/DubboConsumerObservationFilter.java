package cn.iantech.test.nplusone;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

/**
 * Dubbo 消费端调用观测过滤器。
 */
@Activate(group = CommonConstants.CONSUMER)
public final class DubboConsumerObservationFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        NPlusOneContext.recordRemote(invoker.getInterface().getName() + "#" + invocation.getMethodName());
        return invoker.invoke(invocation);
    }
}
