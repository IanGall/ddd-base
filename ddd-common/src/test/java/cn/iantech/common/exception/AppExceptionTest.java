package cn.iantech.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppExceptionTest {

    @Test
    void shouldExposeCodeAsMessageWhenOnlyCodeIsProvided() {
        AppException exception = new AppException("E001");

        assertEquals("E001", exception.getCode());
        assertEquals("E001", exception.getMessage());
        assertNull(exception.getInfo());
        assertEquals(AppException.class.getName() + ": E001", exception.toString());
    }

    @Test
    void shouldExposeBusinessMessageAndCause() {
        IllegalArgumentException cause = new IllegalArgumentException("参数错误");

        AppException exception = new AppException("E002", "业务处理失败", cause);

        assertEquals("E002", exception.getCode());
        assertEquals("业务处理失败", exception.getInfo());
        assertEquals("业务处理失败", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldExposeCauseWhenConstructedWithoutBusinessMessage() {
        IllegalStateException cause = new IllegalStateException("系统异常");

        AppException exception = new AppException("E003", cause);

        assertEquals("E003", exception.getCode());
        assertEquals("E003", exception.getMessage());
        assertNull(exception.getInfo());
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldUseCodeWhenNullableBusinessMessageIsAbsent() {
        IllegalArgumentException cause = new IllegalArgumentException("参数错误");

        AppException withoutCause = new AppException("E004", (String) null);
        AppException withCause = new AppException("E005", null, cause);

        assertEquals("E004", withoutCause.getMessage());
        assertNull(withoutCause.getInfo());
        assertEquals("E005", withCause.getMessage());
        assertNull(withCause.getInfo());
        assertSame(cause, withCause.getCause());
    }

}
