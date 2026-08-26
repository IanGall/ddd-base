package cn.iantech.test.nplusone;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObservationSessionTest {
    private static ObservationSession session(String methodName) throws NoSuchMethodException {
        Method method = ObservationSessionTest.class.getDeclaredMethod(methodName);
        return new ObservationSession(method.getAnnotation(DetectNPlusOne.class));
    }

    @Test
    void shouldReportRepeatedSqlAndRemoteCalls() throws NoSuchMethodException {
        ObservationSession session = session("strictBudget");
        session.recordSelect("SELECT *  FROM user WHERE id = 1");
        session.recordSelect("SELECT * FROM user WHERE id = 2");
        session.recordRemote("UserService#query");
        session.recordRemote("UserService#query");
        assertThatThrownBy(session::assertWithinLimits).isInstanceOf(AssertionError.class)
                .hasMessageContaining("重复 SELECT").hasMessageContaining("重复远程调用");
    }

    @Test
    void shouldIgnoreConfiguredOperations() throws NoSuchMethodException {
        ObservationSession session = session("ignoredBudget");
        session.recordSelect("SELECT 1");
        session.recordSelect("SELECT 1");
        session.recordRemote("HealthApi#check");
        session.recordRemote("HealthApi#check");
        assertThatCode(session::assertWithinLimits).doesNotThrowAnyException();
    }

    @DetectNPlusOne(maxSelects = 2, maxRepeatedSelects = 1, maxRemoteCalls = 2, maxRepeatedRemoteCalls = 1)
    private void strictBudget() {
    }

    @DetectNPlusOne(ignoredSqlPatterns = "SELECT \\?", ignoredRemoteOperations = "HealthApi#check")
    private void ignoredBudget() {
    }
}
