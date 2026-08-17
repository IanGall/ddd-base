package cn.iantech.context.web;

import cn.iantech.context.core.ContextAccessor;
import cn.iantech.context.core.ContextScope;
import cn.iantech.context.core.ContextValidator;
import cn.iantech.context.core.RequestContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * 在 Spring Security 认证完成后建立协议无关的请求上下文。
 */
public final class ContextWebFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestId = validOrGenerate(request.getHeader(REQUEST_ID_HEADER));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = authentication == null || !authentication.isAuthenticated()
                ? null : ContextValidator.validOrNull(authentication.getName());

        RequestContext context = new RequestContext(
                requestId,
                principalName,
                null,
                null,
                null,
                "gateway",
                null);

        try (ContextScope ignored = ContextAccessor.open(context)) {
            response.setHeader(REQUEST_ID_HEADER, requestId);
            filterChain.doFilter(request, response);
        }
    }

    private String validOrGenerate(String value) {
        String validated = ContextValidator.validOrNull(value);
        return validated == null ? UUID.randomUUID().toString() : validated;
    }
}
