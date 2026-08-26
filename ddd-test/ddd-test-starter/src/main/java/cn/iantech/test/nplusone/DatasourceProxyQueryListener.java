package cn.iantech.test.nplusone;

import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;

import java.util.List;

/**
 * JDBC 查询监听器，仅将 SELECT 交给测试观测会话。
 */
public final class DatasourceProxyQueryListener implements QueryExecutionListener {
    private static boolean isSelect(String sql) {
        String normalized = sql == null ? "" : sql.trim();
        return normalized.regionMatches(true, 0, "select", 0, 6) || normalized.regionMatches(true, 0, "with", 0, 4);
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        queryInfoList.stream().map(QueryInfo::getQuery).filter(DatasourceProxyQueryListener::isSelect).forEach(NPlusOneContext::recordSelect);
    }
}
