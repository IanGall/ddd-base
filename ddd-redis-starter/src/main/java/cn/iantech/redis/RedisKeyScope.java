package cn.iantech.redis;

/**
 * Redis Cluster 用户级 Key 作用域。
 *
 * <p>作用域使用用户类型和用户 ID 组成 Hash Tag，确保同一用户的多个 Key
 * 被 Redis Cluster 路由到同一个 hash slot。</p>
 *
 * @param userType 用户类型
 * @param userId   用户 ID，必须为正数
 */
public record RedisKeyScope(String userType, long userId) {

    public RedisKeyScope {
        validateUserType(userType);
        if (userId <= 0) {
            throw new IllegalArgumentException("userId 必须为正数");
        }
    }

    private static void validateUserType(String value) {
        if (value == null || value.isEmpty() || !value.equals(value.trim())
                || containsWhitespace(value) || value.indexOf('{') >= 0 || value.indexOf('}') >= 0
                || value.indexOf(':') >= 0 || value.indexOf('-') >= 0) {
            throw new IllegalArgumentException("userType 非法");
        }
    }

    private static boolean containsWhitespace(String value) {
        return value.chars().anyMatch(Character::isWhitespace);
    }

    /**
     * 返回 Redis Cluster Hash Tag，格式为 {@code {userType-userId}}。
     *
     * @return 用户 Hash Tag
     */
    public String hashTag() {
        return "{" + userType + "-" + userId + "}";
    }
}
