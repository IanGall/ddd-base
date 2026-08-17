package cn.iantech.context.core;

/**
 * 一次请求或 RPC 调用的不可变上下文，只承载跨边界所需的少量标量字段。
 */
public record RequestContext(
        String requestId,
        String principalName,
        String tenantId,
        String userId,
        String grayTag,
        String source,
        String locale) {

    public RequestContext {
        requestId = ContextValidator.validOrNull(requestId);
        principalName = ContextValidator.validOrNull(principalName);
        tenantId = ContextValidator.validOrNull(tenantId);
        userId = ContextValidator.validOrNull(userId);
        grayTag = ContextValidator.validOrNull(grayTag);
        source = ContextValidator.validOrNull(source);
        locale = ContextValidator.validLocaleOrNull(locale);
    }

    public static RequestContext empty() {
        return new RequestContext(null, null, null, null, null, null, null);
    }

    public boolean isEmpty() {
        return requestId == null && principalName == null && tenantId == null && userId == null
                && grayTag == null && source == null && locale == null;
    }
}
