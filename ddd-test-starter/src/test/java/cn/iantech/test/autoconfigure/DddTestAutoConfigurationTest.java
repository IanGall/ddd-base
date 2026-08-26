package cn.iantech.test.autoconfigure;

import net.ttddyy.dsproxy.support.ProxyDataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class DddTestAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DddTestAutoConfiguration.class));

    private static DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:auto_configuration");
        return dataSource;
    }

    @Test
    void shouldDecorateExistingDataSource() {
        contextRunner.withBean(DataSource.class, DddTestAutoConfigurationTest::dataSource).run(context -> {
            assertThat(context).hasSingleBean(DataSource.class);
            assertThat(context.getBean(DataSource.class)).isInstanceOf(ProxyDataSource.class);
        });
    }

    @Test
    void shouldStartWithoutDataSource() {
        contextRunner.run(context -> assertThat(context).doesNotHaveBean(DataSource.class));
    }
}
