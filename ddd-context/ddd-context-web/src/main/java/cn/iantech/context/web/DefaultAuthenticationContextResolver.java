package cn.iantech.context.web;

import cn.iantech.context.core.ContextValidator;

/**
 * 默认解析器只信任已认证主体，不推断租户和用户 ID。
 */
public final class DefaultAuthenticationContextResolver implements AuthenticationContextResolver {

    @Override
    public ResolvedAuthenticationContext resolve(String loginId) {
        if (loginId == null) {
            return ResolvedAuthenticationContext.empty();
        }
        String principalName = ContextValidator.validOrNull(loginId);
        if (principalName == null) {
            return ResolvedAuthenticationContext.empty();
        }
        return new ResolvedAuthenticationContext(principalName, null, null, null, null, null);
    }
}
