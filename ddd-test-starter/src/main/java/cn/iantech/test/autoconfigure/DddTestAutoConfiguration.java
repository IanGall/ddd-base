package cn.iantech.test.autoconfigure;

import cn.iantech.test.nplusone.DatasourceProxyQueryListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

/**
 * 测试 Starter 自动装配。
 */
@AutoConfiguration
public class DddTestAutoConfiguration {
    @Bean
    public static BeanPostProcessor dddTestDataSourcePostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (!(bean instanceof DataSource dataSource)) return bean;
                if (bean instanceof net.ttddyy.dsproxy.support.ProxyDataSource) return bean;
                return ProxyDataSourceBuilder.create(dataSource).name("ddd-test-" + beanName)
                        .listener(new DatasourceProxyQueryListener()).build();
            }
        };
    }
}
