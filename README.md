# DDD 基础组件

`ddd-base` 为 DDD 样例工程和网关提供共享类型、依赖版本及构建约束。仓库自身不承载业务逻辑，也不负责启动应用。

## 模块说明

| 模块                                | 产物     | 职责                                                 |
|-------------------------------------|----------|------------------------------------------------------|
| `ddd-common`                        | JAR      | 提供通用响应、分页模型、持久化基类、常量和应用异常   |
| `ddd-context`                       | 聚合 POM | 聚合协议无关上下文及其边界适配器，不作为业务依赖引入 |
| `ddd-context/ddd-context-core`      | JAR      | 提供协议无关的请求上下文、访问器、作用域和快照能力   |
| `ddd-context/ddd-context-dubbo`     | JAR      | 在 Dubbo 3 Attachment 与请求上下文之间进行转换       |
| `ddd-context/ddd-context-web`       | JAR      | 在 Spring Web 请求边界建立、回写并清理请求上下文     |
| `ddd-redis-starter`                 | JAR      | 提供技术无关的 Redis API、Redisson 实现与自动装配    |
| `ddd-id-generator-starter`          | JAR      | 基于 Redis 租约分配机器号并生成全局唯一的 64 位 ID   |
| `ddd-test`                          | 聚合 POM | 聚合业务服务与网关的测试 Starter，不作为业务依赖引入 |
| `ddd-test/ddd-test-starter`         | JAR      | 检测业务服务中的 SQL/Dubbo N+1                       |
| `ddd-test/ddd-gateway-test-starter` | JAR      | 在网关层编排强类型 Feign 多接口业务流程              |
| `ddd-dependencies`                  | BOM      | 统一第三方依赖版本，供基础 BOM 导入                  |
| `ddd-base-bom`                      | BOM      | 汇总第三方依赖版本及基础组件版本                     |

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

需要将同一全局用户的多个 Key 固定到一个 Redis Cluster slot 时，使用 `RedisKeyBuilder.scope(userId)`。生成的 Hash Tag 为
`{userId}`；业务不得自行拼接花括号，也不得把非全局唯一的局部 ID 用作用户作用域。

### 全局唯一 ID

需要全局唯一 ID 的微服务依赖公共 Starter。版本已经由 `ddd-base-bom` 管理，下游不得重复声明：

```xml
<dependency>
    <groupId>cn.iantech</groupId>
    <artifactId>ddd-id-generator-starter</artifactId>
</dependency>
```

Starter 复用应用现有的 `RedissonClient`，通过 Redis 租约在同一命名空间内自动分配 Yitter WorkerId，无需为 Pod、容器或
物理机手工配置机器号。默认配置如下：

```yaml
ddd:
  id-generator:
    enabled: true
    namespace: ddd-global-id
    worker-id-bit-length: 10
    sequence-bit-length: 12
    lease-duration: 30s
    renew-interval: 10s
```

业务通过构造器注入公共契约生成 ID：

```java
import cn.iantech.id.GlobalIdGenerator;

public class OrderIdService {

    private final GlobalIdGenerator globalIdGenerator;

    public OrderIdService(GlobalIdGenerator globalIdGenerator) {
        this.globalIdGenerator = globalIdGenerator;
    }

    public long nextOrderId() {
        return globalIdGenerator.nextId();
    }
}
```

配置约束：

- `worker-id-bit-length` 默认使用 10 位，可在同一 `namespace` 下同时租用最多 1024 个 WorkerId。
- `namespace` 不能包含空白、花括号或冒号。默认 Redis Key 使用 `{ddd-global-id}:worker:cursor`、
  `{ddd-global-id}:worker:layout` 和 `{ddd-global-id}:worker:lease:<workerId>` 格式；花括号内的 namespace 作为 Redis
  Cluster Hash Tag，确保租约脚本涉及的 Key 位于同一 Slot。
- `worker-id-bit-length` 与 `sequence-bit-length` 之和必须等于 22；默认 10/12 用满可用位数，在实例容量与单实例吞吐之间取得平衡。
- `renew-interval` 必须小于 `lease-duration`。实例会在租约有效期内续约，正常关闭时主动释放 WorkerId。
- 所有需要保证 ID 全局唯一的实例必须连接同一个 Redis，并使用相同的 `namespace` 和位长配置。不同系统应使用不同
  `namespace`，避免互相占用 WorkerId。
- 生产环境可以通过 `DDD_ID_GENERATOR_NAMESPACE`、`DDD_ID_GENERATOR_LEASE_DURATION` 和
  `DDD_ID_GENERATOR_RENEW_INTERVAL` 等环境变量覆盖 Spring Boot 配置。
- Redis 不可用、WorkerId 已耗尽、租约丢失或续约失败时，生成器会严格停发并抛出异常，不会退化为本地默认机器号，防止生成 重复
  ID。调用方不得吞掉异常后自行生成替代 ID。
- 设置 `ddd.id-generator.enabled=false` 会关闭自动装配；关闭后容器中不会提供 `GlobalIdGenerator` Bean。

### 测试 Starter

业务服务只在测试范围引入 N+1 检测底座：

```xml
<dependency>
    <groupId>cn.iantech</groupId>
    <artifactId>ddd-test-starter</artifactId>
    <scope>test</scope>
</dependency>
```

在 Spring 集成测试上声明查询和远程调用预算。Starter 会代理已有 `DataSource`，并统计同一 JVM 中发生的 SELECT 和 Dubbo
Consumer 调用：

```java
@Test
@DetectNPlusOne(maxSelects = 2, maxRepeatedSelects = 1,
        maxRemoteCalls = 3, maxRepeatedRemoteCalls = 1)
void shouldQueryOrderWithoutNPlusOne() {
    orderService.queryOrders();
}
```

业务流程测试只允许在网关层编写。网关测试改为引入组合 Starter，它依赖上述 N+1 检测能力并增加强类型 Feign 流程 DSL：

```xml
<dependency>
    <groupId>cn.iantech</groupId>
    <artifactId>ddd-gateway-test-starter</artifactId>
    <scope>test</scope>
</dependency>
```

流程接口必须使用 OpenFeign 和 DTO，不使用 `Map` 传输数据。`URI` 可以指向网关随机端口或远程测试环境：

```java
interface OrderApi {
    @RequestLine("POST /api/login")
    LoginResponse login(LoginRequest request);

    @RequestLine("GET /api/orders/{id}")
    OrderResponse query(@Param("id") String id);
}

OrderApi api = FeignTestClientFactory.create(OrderApi.class, URI.create(baseUrl));
FlowKey<LoginResponse> login = new FlowKey<>("login", LoginResponse.class);

BusinessFlow flow = BusinessFlow.builder("订单流程")
        .then("登录", context -> context.put(login, api.login(new LoginRequest(username, password))))
        .then("查询订单", context -> assertNotNull(api.query(orderId)))
        .build();

new BusinessFlowRunner().run(flow);
```

流程严格按顺序执行，失败后停止后续步骤且不自动重试。标准服务和标准服务 Archetype 不依赖
`ddd-gateway-test-starter`，从依赖层面阻止在 Provider 内编排 HTTP 业务流程。远程黑盒测试只能观察测试进程发出的 Feign 调用，
不能读取远端服务内部 SQL 或 Dubbo 调用。
