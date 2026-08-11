# DDD 基础组件

`ddd-base` 为 DDD 样例工程和网关提供共享类型、依赖版本及构建约束。仓库自身不承载业务逻辑，也不负责启动应用。

## 模块说明

| 模块 | 产物 | 职责 |
| --- | --- | --- |
| `ddd-common` | JAR | 提供通用响应、分页模型、持久化基类、常量和应用异常 |
| `ddd-dependencies` | BOM | 统一第三方依赖版本，供基础 BOM 导入 |
| `ddd-base-bom` | BOM | 汇总第三方依赖版本和 `ddd-common` 版本 |
| `gateway-governance/gateway-platform-bom` | BOM | 汇总 Spring Boot、DDD 基础组件和业务标准工程版本 |
| `gateway-governance/gateway-parent` | Parent POM | 为网关工程提供依赖管理和 Spring Boot 构建插件版本 |

## 构建要求

- JDK 21，构建时由 Maven Enforcer 强制校验。
- Maven 3.9 或更高版本。
- 测试默认执行；仅在明确需要时通过 `-DskipTests` 跳过。

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

网关工程应继承 `gateway-parent`，统一使用平台 BOM 和构建插件版本。依赖版本只在相应 BOM 中维护，下游模块不重复声明已受管版本。
