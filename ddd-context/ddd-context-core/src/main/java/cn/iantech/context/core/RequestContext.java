package cn.iantech.context.core;

/**
 * 一次请求或 RPC 调用的不可变上下文，只承载跨边界所需的少量标量字段。
 */
public record RequestContext(
        String requestId,
        String principalName,
        String tenantId,
        String userId,
        String subjectType,
        String clientId,
        String grayTag,
        String source,
        String locale) {

    /**
     * 保留原有七字段构造方式，内部统一映射到无认证主体上下文。
     */
    public RequestContext(String requestId, String principalName, String tenantId, String userId,
                          String grayTag, String source, String locale) {
        this(requestId, principalName, tenantId, userId, null, null, grayTag, source, locale);
    }

    public RequestContext {
        requestId = ContextValidator.validOrNull(requestId);
        principalName = ContextValidator.validOrNull(principalName);
        tenantId = ContextValidator.validOrNull(tenantId);
        userId = ContextValidator.validOrNull(userId);
        subjectType = ContextValidator.validOrNull(subjectType);
        clientId = ContextValidator.validOrNull(clientId);
        grayTag = ContextValidator.validOrNull(grayTag);
        source = ContextValidator.validOrNull(source);
        locale = ContextValidator.validLocaleOrNull(locale);
    }

    public static RequestContext empty() {
        return new RequestContext(null, null, null, null, null, null, null, null, null);
    }

    public boolean isEmpty() {
        return requestId == null && principalName == null && tenantId == null && userId == null
                && subjectType == null && clientId == null && grayTag == null && source == null && locale == null;
    }
}
