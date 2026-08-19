package cn.iantech.common.constant;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResponseCodeTest {

    @Test
    void shouldUseUniqueSemanticResponseCodes() {
        assertTrue(Arrays.stream(Constants.ResponseCode.values())
                .allMatch(responseCode -> responseCode.name().equals(responseCode.getCode())));
    }
}
