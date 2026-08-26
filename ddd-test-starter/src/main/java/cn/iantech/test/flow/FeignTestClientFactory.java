package cn.iantech.test.flow;

import cn.iantech.test.nplusone.NPlusOneContext;
import feign.Feign;
import feign.Request;
import feign.Retryer;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.time.Duration;

/**
 * 创建关闭重试、带远程调用观测的强类型 Feign 客户端。
 */
public final class FeignTestClientFactory {
    private FeignTestClientFactory() {
    }

    public static <T> T create(Class<T> apiType, URI baseUri) {
        return create(apiType, baseUri, Duration.ofSeconds(10), Duration.ofSeconds(30));
    }

    public static <T> T create(Class<T> apiType, URI baseUri, Duration connectTimeout, Duration readTimeout) {
        T delegate = Feign.builder().encoder(new GsonEncoder()).decoder(new GsonDecoder()).retryer(Retryer.NEVER_RETRY)
                .options(new Request.Options(connectTimeout, readTimeout, false))
                .target(apiType, baseUri.toString());
        InvocationHandler delegateHandler = Proxy.getInvocationHandler(delegate);
        Object proxy = Proxy.newProxyInstance(apiType.getClassLoader(), new Class<?>[]{apiType}, (ignored, method, args) -> {
            if (method.getDeclaringClass() != Object.class)
                NPlusOneContext.recordRemote(apiType.getName() + "#" + method.getName());
            return delegateHandler.invoke(delegate, method, args);
        });
        return apiType.cast(proxy);
    }
}
