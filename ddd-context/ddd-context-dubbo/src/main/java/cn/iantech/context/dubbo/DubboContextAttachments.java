package cn.iantech.context.dubbo;

import cn.iantech.context.core.ContextKeys;
import cn.iantech.context.core.ContextValidator;
import cn.iantech.context.core.RequestContext;
import org.apache.dubbo.rpc.RpcContextAttachment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Dubbo 请求上下文附件协议。
 *
 * <p>该类只接收固定白名单字段，避免业务对象、登录凭证或任意附件进入 RPC 请求头。</p>
 */
public final class DubboContextAttachments {

    public static final String REQUEST_ID = ContextKeys.REQUEST_ID;
    public static final String PRINCIPAL_NAME = ContextKeys.PRINCIPAL_NAME;
    public static final String TENANT_ID = ContextKeys.TENANT_ID;
    public static final String USER_ID = ContextKeys.USER_ID;
    public static final String SUBJECT_TYPE = ContextKeys.SUBJECT_TYPE;
    public static final String CLIENT_ID = ContextKeys.CLIENT_ID;
    public static final String GRAY_TAG = ContextKeys.GRAY_TAG;
    public static final String SOURCE = ContextKeys.SOURCE;
    public static final String LOCALE = ContextKeys.LOCALE;

    private static final List<Binding> BINDINGS = List.of(
            new Binding(REQUEST_ID, RequestContext::requestId, ContextValidator::validOrNull),
            new Binding(PRINCIPAL_NAME, RequestContext::principalName, ContextValidator::validOrNull),
            new Binding(TENANT_ID, RequestContext::tenantId, ContextValidator::validOrNull),
            new Binding(USER_ID, RequestContext::userId, ContextValidator::validOrNull),
            new Binding(SUBJECT_TYPE, RequestContext::subjectType, ContextValidator::validOrNull),
            new Binding(CLIENT_ID, RequestContext::clientId, ContextValidator::validOrNull),
            new Binding(GRAY_TAG, RequestContext::grayTag, ContextValidator::validOrNull),
            new Binding(SOURCE, RequestContext::source, ContextValidator::validOrNull),
            new Binding(LOCALE, RequestContext::locale, ContextValidator::validLocaleOrNull)
    );

    private static final Set<String> KEYS = BINDINGS.stream()
            .map(Binding::key)
            .collect(Collectors.toUnmodifiableSet());

    private DubboContextAttachments() {
    }

    /**
     * 返回不可变的上下文附件白名单。
     */
    public static Set<String> keys() {
        return KEYS;
    }

    static RequestContext read(RpcContextAttachment attachment) {
        return new RequestContext(
                read(attachment, REQUEST_ID),
                read(attachment, PRINCIPAL_NAME),
                read(attachment, TENANT_ID),
                read(attachment, USER_ID),
                read(attachment, SUBJECT_TYPE),
                read(attachment, CLIENT_ID),
                read(attachment, GRAY_TAG),
                read(attachment, SOURCE),
                read(attachment, LOCALE)
        );
    }

    static void write(RequestContext context, RpcContextAttachment attachment) {
        clear(attachment);
        BINDINGS.stream()
                .map(binding -> new Value(binding, normalize(binding, binding.extractor().apply(context))))
                .filter(value -> value.value() != null)
                .forEach(value -> attachment.setAttachment(value.binding().key(), value.value()));
    }

    static Map<String, String> snapshot(RpcContextAttachment attachment) {
        return BINDINGS.stream()
                .filter(binding -> attachment.getAttachment(binding.key()) != null)
                .collect(Collectors.toMap(
                        Binding::key,
                        binding -> attachment.getAttachment(binding.key()),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
    }

    static void restore(RpcContextAttachment attachment, Map<String, String> snapshot) {
        clear(attachment);
        snapshot.forEach(attachment::setAttachment);
    }

    static void clear(RpcContextAttachment attachment) {
        BINDINGS.stream()
                .map(Binding::key)
                .forEach(attachment::removeAttachment);
    }

    private static String read(RpcContextAttachment attachment, String key) {
        Binding binding = BINDINGS.stream()
                .filter(candidate -> candidate.key().equals(key))
                .findFirst()
                .orElseThrow();
        return normalize(binding, attachment.getAttachment(key));
    }

    private static String normalize(Binding binding, String value) {
        return binding.validator().apply(value);
    }

    private record Binding(
            String key,
            Function<RequestContext, String> extractor,
            Function<String, String> validator
    ) {
    }

    private record Value(Binding binding, String value) {
    }
}
