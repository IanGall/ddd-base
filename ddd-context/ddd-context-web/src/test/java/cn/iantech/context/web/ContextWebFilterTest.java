package cn.iantech.context.web;

import cn.iantech.context.core.ContextAccessor;
import cn.iantech.context.core.RequestContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ContextWebFilterTest {

    private final ContextWebFilter filter = new ContextWebFilter();

    // 每个测试结束后清理认证上下文，避免测试之间相互污染
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // 验证只从认证主体建立上下文，并忽略外部传入的身份请求头
    @Test
    void shouldBuildContextFromAuthenticatedPrincipalAndIgnoreExternalIdentityHeaders() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "test-admin", "password", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ContextWebFilter.REQUEST_ID_HEADER, "request-001");
        request.addHeader("X-User-Id", "forged-user");
        request.addHeader("X-Tenant-Id", "forged-tenant");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<RequestContext> captured = new AtomicReference<>();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
                captured.set(ContextAccessor.current().orElseThrow()));

        assertEquals("request-001", captured.get().requestId());
        assertEquals("test-admin", captured.get().principalName());
        assertEquals("gateway", captured.get().source());
        assertNull(captured.get().userId());
        assertNull(captured.get().tenantId());
        assertEquals("request-001", response.getHeader(ContextWebFilter.REQUEST_ID_HEADER));
        assertFalse(ContextAccessor.current().isPresent());
    }

    // 验证外部请求号非法时生成并回写新的请求号
    @Test
    void shouldGenerateNewRequestIdWhenIncomingRequestIdIsInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ContextWebFilter.REQUEST_ID_HEADER, "非法 请求号");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<RequestContext> captured = new AtomicReference<>();

        filter.doFilter(request, response, (ignoredRequest, ignoredResponse) ->
                captured.set(ContextAccessor.current().orElseThrow()));

        assertNotNull(captured.get().requestId());
        assertFalse(captured.get().requestId().isBlank());
        assertEquals(captured.get().requestId(), response.getHeader(ContextWebFilter.REQUEST_ID_HEADER));
        assertFalse(ContextAccessor.current().isPresent());
    }
}
