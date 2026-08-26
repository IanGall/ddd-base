package cn.iantech.test.nplusone;

import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

class DatasourceProxyQueryListenerTest {
    private static DataSource dataSource() {
        JdbcDataSource delegate = new JdbcDataSource();
        delegate.setURL("jdbc:h2:mem:ddd_test;DB_CLOSE_DELAY=-1");
        return ProxyDataSourceBuilder.create(delegate).listener(new DatasourceProxyQueryListener()).build();
    }

    private static void executeSelect(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeQuery("SELECT 1");
        } catch (SQLException ex) {
            throw new CompletionException(ex);
        }
    }

    @Test
    @DetectNPlusOne(maxSelects = 1)
    void shouldCaptureSelectAcrossThreads() throws Exception {
        DataSource dataSource = dataSource();
        CompletableFuture.runAsync(() -> executeSelect(dataSource)).join();
    }

}
