package cn.iantech.id;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdGeneratorPropertiesTest {

    @Test
    void shouldUseSafeDistributedDefaults() {
        IdGeneratorProperties properties = new IdGeneratorProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getNamespace()).isEqualTo("ddd-global-id");
        assertThat(properties.getWorkerIdBitLength()).isEqualTo(10);
        assertThat(properties.getSequenceBitLength()).isEqualTo(12);
        assertThat(properties.getLeaseDuration()).isEqualTo(Duration.ofSeconds(30));
        assertThat(properties.getRenewInterval()).isEqualTo(Duration.ofSeconds(10));
        assertThat(properties.workerPoolSize()).isEqualTo(1024);
    }

    @Test
    void shouldRejectInvalidNamespace() {
        IdGeneratorProperties properties = new IdGeneratorProperties();
        properties.setNamespace("ddd:{invalid}");

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IdGenerationException.class)
                .hasMessageContaining("namespace");
    }

    @Test
    void shouldRejectNamespaceContainingColon() {
        IdGeneratorProperties properties = new IdGeneratorProperties();
        properties.setNamespace("ddd:id-generator");

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IdGenerationException.class)
                .hasMessageContaining("namespace");
    }

    @Test
    void shouldRejectBitLengthOverflow() {
        IdGeneratorProperties properties = new IdGeneratorProperties();
        properties.setWorkerIdBitLength(11);

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IdGenerationException.class)
                .hasMessageContaining("必须等于 22");
    }

    @Test
    void shouldRejectRenewIntervalNotShorterThanLease() {
        IdGeneratorProperties properties = new IdGeneratorProperties();
        properties.setLeaseDuration(Duration.ofSeconds(10));
        properties.setRenewInterval(Duration.ofSeconds(10));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IdGenerationException.class)
                .hasMessageContaining("renew-interval");
    }

    @TestFactory
    Stream<DynamicTest> shouldRejectEveryInvalidNamespaceShape() {
        return Stream.of(null, "", " ddd", "ddd ", "ddd{id", "ddd}id", "ddd:id", "ddd id")
                .map(namespace -> DynamicTest.dynamicTest("namespace=" + namespace, () -> {
                    IdGeneratorProperties properties = new IdGeneratorProperties();
                    properties.setNamespace(namespace);
                    assertThatThrownBy(properties::validate)
                            .isInstanceOf(IdGenerationException.class)
                            .hasMessageContaining("namespace");
                }));
    }

    @TestFactory
    Stream<DynamicTest> shouldRejectInvalidBitLengths() {
        return Stream.of(
                        new int[]{0, 22},
                        new int[]{16, 6},
                        new int[]{1, 2},
                        new int[]{1, 22})
                .map(bits -> DynamicTest.dynamicTest(bits[0] + ":" + bits[1], () -> {
                    IdGeneratorProperties properties = new IdGeneratorProperties();
                    properties.setWorkerIdBitLength(bits[0]);
                    properties.setSequenceBitLength(bits[1]);
                    assertThatThrownBy(properties::validate)
                            .isInstanceOf(IdGenerationException.class);
                }));
    }

    @TestFactory
    Stream<DynamicTest> shouldRejectInvalidLeaseDurations() {
        return Stream.of(null, Duration.ZERO, Duration.ofMillis(-1), Duration.ofNanos(1))
                .map(duration -> DynamicTest.dynamicTest(String.valueOf(duration), () -> {
                    IdGeneratorProperties properties = new IdGeneratorProperties();
                    properties.setLeaseDuration(duration);
                    assertThatThrownBy(properties::validate)
                            .isInstanceOf(IdGenerationException.class);
                }));
    }

    @TestFactory
    Stream<DynamicTest> shouldRejectInvalidRenewIntervals() {
        return Stream.of(null, Duration.ZERO, Duration.ofMillis(-1), Duration.ofNanos(1))
                .map(duration -> DynamicTest.dynamicTest(String.valueOf(duration), () -> {
                    IdGeneratorProperties properties = new IdGeneratorProperties();
                    properties.setRenewInterval(duration);
                    assertThatThrownBy(properties::validate)
                            .isInstanceOf(IdGenerationException.class);
                }));
    }

    @Test
    void shouldRejectDurationOverflow() {
        IdGeneratorProperties properties = new IdGeneratorProperties();
        properties.setLeaseDuration(Duration.ofSeconds(Long.MAX_VALUE));
        properties.setRenewInterval(Duration.ofSeconds(1));

        assertThatThrownBy(properties::validate)
                .isInstanceOf(IdGenerationException.class)
                .hasMessageContaining("超出支持范围");
    }
}
