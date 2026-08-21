# DDD 基础组件

`ddd-base` 为 DDD 样例工程和网关提供共享类型、依赖版本及构建约束。仓库自身不承载业务逻辑，也不负责启动应用。

## 模块说明

| 模块                            | 产物     | 职责                                                 |
|---------------------------------|----------|------------------------------------------------------|
| `ddd-common`                    | JAR      | 提供通用响应、分页模型、持久化基类、常量和应用异常   |
| `ddd-context`                   | 聚合 POM | 聚合协议无关上下文及其边界适配器，不作为业务依赖引入 |
| `ddd-context/ddd-context-core`  | JAR      | 提供协议无关的请求上下文、访问器、作用域和快照能力   |
| `ddd-context/ddd-context-dubbo` | JAR      | 在 Dubbo 3 Attachment 与请求上下文之间进行转换       |
| `ddd-context/ddd-context-web`   | JAR      | 在 Spring Web 请求边界建立、回写并清理请求上下文     |
| `ddd-redis-starter`             | JAR      | 提供技术无关的 Redis API、Redisson 实现与自动装配    |
| `ddd-dependencies`              | BOM      | 统一第三方依赖版本，供基础 BOM 导入                  |
| `ddd-base-bom`                  | BOM      | 汇总第三方依赖版本及基础组件版本                     |

## 构建要求

- JDK 21，构建时由 Maven Enforcer 强制校验。
- Maven 3.9 或更高版本。
- 测试默认执行；仅在明确需要时通过 `-DskipTests` 跳过。
- 主源码和测试源码的方法名必须使用英文 lowerCamelCase，构建时由 Maven Checkstyle 强制校验。

完整构建并安装到本地 Maven 仓库：

```bash
mvn clean install
```

只验证编译和单元测试：

```bash
mvn verify
```

## 下游使用

业务工程通过 `dependencyManagement` 导入基础 BOM：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>cn.iantech</groupId>
            <artifactId>ddd-base-bom</artifactId>
            <version>1.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

需要统一构建约束的工程应继承 `ddd-base`，并按需导入 `ddd-base-bom`。依赖版本只在基础 BOM 中维护，下游模块不重复声明已受管版本。

需要 Redis 能力的微服务直接依赖公共 Starter，无需自行定义 Redis 接口或装配 Redisson：

```xml
<dependency>
    <groupId>cn.iantech</groupId>
    <artifactId>ddd-redis-starter</artifactId>
</dependency>
```

Starter 在容器中存在 `RedissonClient` 时自动提供 `cn.iantech.redis.IRedisService`。业务可以声明自己的
`IRedisService` Bean 覆盖默认实现；公共接口不会暴露 Redisson 的锁、队列、脚本等客户端类型。
