package cn.iantech.test.nplusone;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 跨线程共享的单次测试观测会话。
 */
final class ObservationSession {
    private final int maxSelects;
    private final int maxRepeatedSelects;
    private final int maxRemoteCalls;
    private final int maxRepeatedRemoteCalls;
    private final Pattern[] ignoredSqlPatterns;
    private final String[] ignoredRemoteOperations;
    private final AtomicInteger selects = new AtomicInteger();
    private final AtomicInteger remoteCalls = new AtomicInteger();
    private final Map<String, AtomicInteger> sqlCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> remoteCounts = new ConcurrentHashMap<>();

    ObservationSession(DetectNPlusOne annotation) {
        this.maxSelects = annotation.maxSelects();
        this.maxRepeatedSelects = annotation.maxRepeatedSelects();
        this.maxRemoteCalls = annotation.maxRemoteCalls();
        this.maxRepeatedRemoteCalls = annotation.maxRepeatedRemoteCalls();
        this.ignoredSqlPatterns = java.util.Arrays.stream(annotation.ignoredSqlPatterns()).map(Pattern::compile).toArray(Pattern[]::new);
        this.ignoredRemoteOperations = annotation.ignoredRemoteOperations();
    }

    private static String normalize(String sql) {
        return sql == null ? "<null>" : sql.replaceAll("\\s+", " ").trim().replaceAll("\\b\\d+\\b", "?");
    }

    private static String budgetViolation(String type, String key, int actual, int expected) {
        return actual > expected ? formatViolation(type, key, actual, expected) : null;
    }

    private static String formatViolation(String type, String key, int actual, int expected) {
        return type + "超过阈值，actual=" + actual + ", expected=" + expected + ", operation=" + key;
    }

    void recordSelect(String sql) {
        String normalized = normalize(sql);
        if (java.util.Arrays.stream(ignoredSqlPatterns).anyMatch(p -> p.matcher(normalized).find())) return;
        selects.incrementAndGet();
        sqlCounts.computeIfAbsent(normalized, ignored -> new AtomicInteger()).incrementAndGet();
    }

    void recordRemote(String operation) {
        if (java.util.Arrays.stream(ignoredRemoteOperations).anyMatch(operation::equals)) return;
        remoteCalls.incrementAndGet();
        remoteCounts.computeIfAbsent(operation, ignored -> new AtomicInteger()).incrementAndGet();
    }

    void assertWithinLimits() {
        Stream<String> totalViolations = Stream.of(
                budgetViolation("SELECT 总数", "全部 SELECT", selects.get(), maxSelects),
                budgetViolation("远程调用总数", "全部远程调用", remoteCalls.get(), maxRemoteCalls)).filter(value -> value != null);
        Stream<String> sqlViolations = sqlCounts.entrySet().stream()
                .filter(entry -> entry.getValue().get() > maxRepeatedSelects)
                .map(entry -> formatViolation("重复 SELECT", entry.getKey(), entry.getValue().get(), maxRepeatedSelects));
        Stream<String> remoteViolations = remoteCounts.entrySet().stream()
                .filter(entry -> entry.getValue().get() > maxRepeatedRemoteCalls)
                .map(entry -> formatViolation("重复远程调用", entry.getKey(), entry.getValue().get(), maxRepeatedRemoteCalls));
        String violations = Stream.of(totalViolations, sqlViolations, remoteViolations).flatMap(stream -> stream).collect(Collectors.joining(System.lineSeparator()));
        if (!violations.isBlank()) throw new AssertionError("检测到 N+1：" + System.lineSeparator() + violations);
    }

    String summary() {
        return "selects=" + selects + ", remoteCalls=" + remoteCalls + ", sql=" + sqlCounts.entrySet().stream().map(e -> e.getKey() + " x" + e.getValue()).collect(Collectors.joining("; "))
                + ", remote=" + remoteCounts.entrySet().stream().map(e -> e.getKey() + " x" + e.getValue()).collect(Collectors.joining("; "));
    }
}
