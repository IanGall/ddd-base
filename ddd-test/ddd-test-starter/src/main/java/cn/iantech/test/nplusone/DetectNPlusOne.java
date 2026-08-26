package cn.iantech.test.nplusone;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 在测试方法或测试类上启用 SQL 与远程调用预算检测。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(NPlusOneExtension.class)
public @interface DetectNPlusOne {
    int maxSelects() default Integer.MAX_VALUE;

    int maxRepeatedSelects() default 1;

    int maxRemoteCalls() default Integer.MAX_VALUE;

    int maxRepeatedRemoteCalls() default 1;

    String[] ignoredSqlPatterns() default {};

    String[] ignoredRemoteOperations() default {};
}
