package cn.iantech.redis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class RedisKeyBuilderTest {

    @Test
    void sameScopeKeysShareHashTag() {
        RedisKeyScope scope = RedisKeyBuilder.scope("PRIMARY", 1001);
        String profile = RedisKeyBuilder.key("auth:session:v3", scope, "session", "s1");
        String refresh = RedisKeyBuilder.key("auth:session:v3", scope, "refresh", "r1");

        assertThat(profile).contains("{PRIMARY-1001}");
        assertThat(refresh).contains("{PRIMARY-1001}");
        assertThat(RedisKeyBuilder.scopedPrefix("auth:session:v3", scope))
                .isEqualTo("auth:session:v3:{PRIMARY-1001}");
    }

    @Test
    void differentTypesAndIdsDoNotCollide() {
        assertThat(RedisKeyBuilder.hashTag(RedisKeyBuilder.scope("PRIMARY", 1001)))
                .isNotEqualTo(RedisKeyBuilder.hashTag(RedisKeyBuilder.scope("CUSTOMER", 1001)))
                .isNotEqualTo(RedisKeyBuilder.hashTag(RedisKeyBuilder.scope("PRIMARY", 1002)));
    }

    @Test
    void invalidScopeIsRejected() {
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.scope(null, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.scope(" ", 1));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.scope("A:B", 1));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.scope("A-B", 1));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.scope("A{B", 1));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.scope("A B", 1));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.scope("A", 0));
    }

    @Test
    void invalidKeySegmentsAreRejected() {
        RedisKeyScope scope = RedisKeyBuilder.scope("CUSTOMER", 3);
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.key("", scope, "r", "i"));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.key("ns", scope, "r r", "i"));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.key("ns", scope, "r", "i}"));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.key("ns", null, "r", "i"));
    }

    @Test
    void unscopedKeysUseTheSameSegmentValidation() {
        assertThat(RedisKeyBuilder.key("auth:risk:v1", "ip", "hash"))
                .isEqualTo("auth:risk:v1:ip:hash");
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.key("ns", "r", "i i"));
        assertThatIllegalArgumentException().isThrownBy(() -> RedisKeyBuilder.key("ns", "r", "i:i"));
    }
}
