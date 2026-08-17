package cn.iantech.context.web;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * Web 上下文过滤器自动装配。过滤器顺序由应用的 SecurityFilterChain 显式控制。
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.security.web.authentication.www.BasicAuthenticationFilter")
public class ContextWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ContextWebFilter contextWebFilter() {
        return new ContextWebFilter();
    }

    @Bean
    public FilterRegistrationBean<ContextWebFilter> contextWebFilterRegistration(ContextWebFilter filter) {
        FilterRegistrationBean<ContextWebFilter> registration = new FilterRegistrationBean<>(filter);
        // 只允许 SecurityFilterChain 注册，避免在认证前被 Servlet 容器重复执行。
        registration.setEnabled(false);
        return registration;
    }
}
