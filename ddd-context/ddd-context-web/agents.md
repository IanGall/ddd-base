# ddd-context-web 模块协作说明

## 模块定位

- 本模块在 Spring Web 请求边界建立协议无关的 `RequestContext`。
- `ContextWebFilter` 负责读取请求信息、安装上下文、回写请求标识并在请求结束后恢复父上下文。
- 本模块只适用于 Gateway 或显式启用 HTTP Profile 的标准服务，Application 层应只依赖 Core。

## HTTP 与认证约束

- `ContextWebFilter` 必须在 `BasicAuthenticationFilter` 之后由 `SecurityFilterChain` 显式执行。
- 自动配置不得让 Servlet 容器重复注册 Filter，避免认证前执行或同一请求执行两次。
- `requestId` 读取并校验 `X-Request-Id`，缺失或非法时生成 UUID，并回写同名响应头。
- `principalName` 只能读取已认证的 `Authentication.getName()`，禁止信任外部身份 Header。
- 当前 Basic Auth 不提供可信 `tenantId` 和 `userId`，必须保持为空。
- 根 HTTP 上下文的 `source` 固定为 `gateway`；匿名和健康检查请求也必须建立请求上下文。

## 安全与清理

- 禁止读取或传播外部 `X-User-Id`、`X-Tenant-Id`、Authorization、Cookie、密码、权限列表和业务对象。
- `grayTag`、`locale` 只有在明确来源白名单和格式校验完成后才允许接入，且不得作为授权依据。
- 整个过滤链必须由 `ContextScope` 包裹，正常返回或异常时都要恢复进入请求前的上下文。
- 不在 Controller 中手工设置 Dubbo Attachment，RPC 传播由 `ddd-context-dubbo` 负责。

## 测试与构建

- 测试方法名必须使用英文，中文场景说明使用代码注释；不得使用中文方法名。
- 必须覆盖 Basic Auth 用户名提取、外部身份 Header 忽略、请求 ID 生成与回写、匿名请求和请求间隔离。
- 修改后执行 `mvn -q -pl ddd-context-web test`；涉及 Core 协议时执行 `mvn -q install`。
- 自动配置或过滤器顺序变更必须增加对应 Spring Security 集成测试。
