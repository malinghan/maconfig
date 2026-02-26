# maconfig 配置中心功能迭代计划

## 项目定位

maconfig 是一个从零构建的轻量级分布式配置中心，参考 kkconfig 的架构设计思路，在保持简洁的同时逐步演进为生产可用的配置管理平台。

项目结构规划：
```
maconfig/
├── maconfig-server      # 配置服务端
├── maconfig-client      # 客户端 SDK
└── maconfig-demo        # 示例应用
```

---

## 核心版本（本期开发）

### v1.0 — MVP 核心跑通 ✅

> 目标：最小可用版本，能存、能取、能热更新。启动 server + demo，通过 API 更新配置，5 秒内 demo 接口返回新值。

#### S1-1 工程脚手架搭建 ✅

- 创建 Maven 多模块工程（maconfig-server / maconfig-client / maconfig-demo）
- server 依赖：Spring Boot 3.x、MyBatis、MySQL、Lombok
- client 依赖：Spring Context 6.x、Spring Cloud Context、Jackson、Lombok
- 统一 Java 17、编码 UTF-8、统一版本号管理

#### S1-2 数据库设计与初始化 ✅

- 设计 `configs` 表，四元组主键：`app + env + ns + pkey`
  ```sql
  CREATE TABLE configs (
      app   VARCHAR(64) NOT NULL,
      env   VARCHAR(64) NOT NULL,
      ns    VARCHAR(64) NOT NULL,
      pkey  VARCHAR(128) NOT NULL,
      pval  TEXT,
      PRIMARY KEY (app, env, ns, pkey)
  );
  ```
- 设计 `locks` 表，用于分布式锁
  ```sql
  CREATE TABLE locks (
      id  INT PRIMARY KEY,
      app VARCHAR(64) NOT NULL
  );
  INSERT INTO locks VALUES (1, 'maconfig-server');
  ```
- 提供 `schema.sql`，支持 Spring Boot 自动初始化（H2 开发模式）

#### S1-3 服务端核心 API ✅

实现三个核心 REST 接口：

| 接口 | 方法 | 说明 |
|------|------|------|
| `/v1/configs` | GET | 查询指定 app/env/ns 下的全量配置 |
| `/v1/configs` | POST | 批量写入/更新配置（upsert 语义） |
| `/v1/version` | GET | 查询当前版本号（时间戳） |

- 版本号用内存 `ConcurrentHashMap` 维护，key 为 `app-env-ns`，写入时更新为 `System.currentTimeMillis()`
- 使用 MyBatis 注解模式实现 `ConfigsMapper`（无 XML）
- 统一响应体封装 `Result<T>`，包含 code / message / data

#### S1-4 分布式锁（主备选举）✅

- 基于 MySQL `SELECT ... FOR UPDATE` 实现行锁
- 独立数据库连接，关闭自动提交，持锁期间不提交事务
- 定时任务每 5 秒尝试抢锁，失败则标记为 Standby
- 暴露 `/v1/status` 接口返回当前实例是否为 Master
- 应用关闭时 rollback 释放锁（`@PreDestroy`）

#### S1-5 客户端 SDK 核心 ✅

- `ConfigMeta`：封装 app / env / ns / serverUrl 四元信息
- `MaRepository`：接口 + 默认实现，负责 HTTP 拉取配置和版本轮询
  - 启动时立即拉取全量配置
  - 定时任务每 5 秒轮询 `/v1/version`，版本变化时重新拉取
  - 触发 `RepositoryChangeListener.onChange()`
- `MaConfigService`：持有配置 Map，计算变更 key 集合，发布 `EnvironmentChangeEvent`
- `MaPropertySource`：继承 `EnumerablePropertySource`，桥接 Spring Environment
- `PropertySourcesProcessor`：实现 `BeanFactoryPostProcessor`，以最高优先级将 `MaPropertySource` 注入 Environment
- `MaConfigRegistrar`：实现 `ImportBeanDefinitionRegistrar`，注册上述两个 Processor
- `@EnableMaConfig`：入口注解，`@Import(MaConfigRegistrar.class)`

#### S1-6 @Value 热更新 ✅

- `SpringValueProcessor`：实现 `BeanPostProcessor` + `ApplicationListener<EnvironmentChangeEvent>`
  - 启动时扫描所有 Bean 的 `@Value` 字段，存入 `MultiValueMap<key, SpringValue>`
  - 收到变更事件时，通过反射将新值写回字段
- `PlaceholderHelper`：解析 `${key:default}` 嵌套占位符，提取实际 key 列表
- `SpringValue`：持有 bean 实例、字段引用、占位符原文

#### S1-7 Demo 应用 ✅

- 演示 `@Value` 字段热更新
- 演示 `@ConfigurationProperties` 热更新（依赖 Spring Cloud 的 `ConfigurationPropertiesRebinder`）
- 提供 `/demo` 接口，实时返回当前配置值
- 提供 `configs.http` 测试文件，覆盖所有服务端接口

**验收标准：**
- 启动 server + demo，通过 POST 更新配置，5 秒内 demo 的 `/demo` 接口返回新值
- 启动 3 个 server 实例，只有一个 `/v1/status` 返回 true，停掉 Master 后另一个自动接管

---

### v2.0 — 可靠性增强

> 目标：解决 MVP 中的已知缺陷，使其具备基本的生产可用性。server 重启后客户端无感知，server 宕机时 demo 应用正常运行。

#### S2-1 版本号持久化

**问题**：MVP 版本号存内存，server 重启后版本归零，客户端误判"无变化"。

**方案**：
- `configs` 表增加 `updated_at BIGINT` 字段，写入时记录时间戳
- `/v1/version` 改为查询 `MAX(updated_at)`，无需内存维护
- 客户端版本比较逻辑不变

#### S2-2 客户端启动容错

**问题**：MVP 中 server 不可用时客户端启动直接报错。

**方案**：
- 拉取配置失败时，降级使用本地 `application.yml` 中的默认值，打印 WARN 日志
- 支持配置 `maconfig.fail-fast=false`（默认 false）控制是否允许降级启动
- 轮询失败时静默重试，不抛出异常

#### S2-3 配置删除支持

- 服务端新增 `DELETE /v1/configs` 接口，支持按 key 删除
- 客户端变更计算逻辑补充"key 被删除"场景（新配置中不含旧 key）
- 删除后触发 `EnvironmentChangeEvent`，相关 `@Value` 字段回退到 `application.yml` 默认值

#### S2-4 配置查询接口增强

- 新增 `GET /v1/configs/{pkey}` 单 key 查询接口
- 新增 `GET /v1/apps` 查询所有 app 列表（便于管理）
- 响应统一加 `X-Version` 响应头，客户端可直接从响应头读取版本

#### S2-5 日志规范化

- 统一日志前缀 `[MACONFIG]`
- 关键操作（配置写入、版本变更、锁状态变化）输出结构化日志
- 客户端轮询正常时不输出日志，仅在版本变化或异常时输出

#### S2-6 单元测试覆盖

- server：`ConfigsMapper` CRUD 测试（H2 内存库）
- server：`DistributedLocks` 状态机测试（Mock DataSource）
- client：`PlaceholderHelper` 占位符解析测试（纯单元测试，无 Spring 依赖）
- client：`MaConfigServiceImpl.calcChangedKeys()` 差异计算测试
- demo：`@SpringBootTest` 集成测试，验证配置注入正确性

**验收标准：**
- server 重启后客户端无感知，配置正常同步
- server 宕机时 demo 应用正常运行，不因轮询失败而崩溃
- 单元测试覆盖率 > 60%

---

## 后续规划（待开发）

> 以下功能已完成设计，将在核心版本稳定后按优先级推进。

---

### v3.0 — 运维与管理能力（P1）

> 目标：提供基本的可观测性和管理手段，从"能用"到"好用"。

#### S3-1 配置变更历史

- 新增 `config_history` 表，记录每次写入/删除操作
- 新增 `GET /v1/history` 接口，支持按 app/env/ns/pkey 查询变更历史

#### S3-2 配置回滚

- 新增 `POST /v1/rollback` 接口，指定 history_id 将配置回滚到该版本

#### S3-3 Actuator 集成

- server 暴露 `/actuator/health`，包含数据库连通性和锁状态
- client 暴露自定义 endpoint `/actuator/maconfig`，展示当前加载的配置快照和版本号

#### S3-4 配置导入导出

- 新增 `GET /v1/export` 和 `POST /v1/import` 接口，支持 JSON 格式批量操作
- 支持导入冲突策略：`overwrite` / `skip` / `merge`

#### S3-5 多命名空间继承

- 支持 `public` 命名空间作为公共配置，其他 ns 自动继承
- 服务端 `/v1/configs` 支持 `inherit=true` 参数控制是否合并 public 配置

---

### v4.0 — 安全与权限（P1）

> 目标：具备基本的访问控制，可在内网生产环境使用。

#### S4-1 Token 认证

- 写接口需携带 `Authorization: Bearer <token>`，client SDK 支持配置 token

#### S4-2 操作审计日志

- `config_history` 表的 `operator` 字段从 token 中解析填充

#### S4-3 敏感配置加密

- 支持 `ENC(...)` 格式存储加密内容，client 端自动解密（AES-256）

#### S4-4 HTTPS 支持

- server 支持配置 SSL 证书，client 支持信任自签名证书

---

### v5.0 — 高性能推送（P2）

> 目标：解决轮询延迟问题，支持更大规模的客户端连接。

#### S5-1 长轮询（Long Polling）

- server 新增 `GET /v1/watch` 接口，挂起请求最多 30 秒，有变更立即返回

#### S5-2 服务端推送（SSE）

- 新增 `GET /v1/stream` SSE 接口，配置变更时主动推送

#### S5-3 客户端连接数统计

- server 新增 `/v1/connections` 接口，按 app/env/ns 分组统计活跃连接

---

### v6.0 — 管理控制台（P2）

> 目标：提供可视化管理界面，降低使用门槛。

- 技术栈：Vue 3 + Vite + Element Plus，与 server 同进程部署
- 配置管理页面：列表筛选、单条/批量编辑、环境对比
- 变更历史页面：时间线展示、diff 查看、一键回滚
- 集群状态页面：Master/Standby 状态、活跃连接数、配置统计

---

### v7.0 — 生态扩展（P3）

> 目标：扩大适用场景，接入更多技术生态。

- Spring Boot Starter 封装，引入依赖即生效
- 支持 YAML / Properties 格式配置文件整体存储
- 抽象存储层，提供 Redis / PostgreSQL 实现
- Kubernetes 适配：ConfigMap 同步、Helm Chart、环境变量覆盖
- 配置灰度发布：按 IP / 实例标签进行灰度推送

---

## 迭代优先级总览

| 版本 | 主题 | 优先级 | 状态 |
|------|------|--------|------|
| v1.0 | MVP 核心跑通 | P0 | 本期开发 |
| v2.0 | 可靠性增强 | P0 | 本期开发 |
| v3.0 | 运维管理能力 | P1 | 待开发 |
| v4.0 | 安全与权限 | P1 | 待开发 |
| v5.0 | 高性能推送 | P2 | 待开发 |
| v6.0 | 管理控制台 | P2 | 待开发 |
| v7.0 | 生态扩展 | P3 | 待开发 |

---

## 技术债务与注意事项

在迭代过程中需持续关注以下问题，避免积累技术债：

1. **版本号精度**：时间戳版本号在高并发写入时可能出现同毫秒冲突，v2.0 阶段可引入数据库自增序列替代
2. **全量拉取开销**：版本变化时拉取全量配置，配置量大时网络开销显著，v5.0 阶段可改为增量推送
3. **内存泄漏风险**：`SpringValueProcessor` 持有 Bean 引用，需注意 Bean 销毁时清理对应的 SpringValue 记录
4. **锁续期问题**：MySQL 行锁依赖连接存活，网络抖动可能导致锁意外释放，需监控锁状态变化频率
5. **配置值长度**：MVP 阶段 `pval` 使用 TEXT 类型，需评估超大配置值（如证书内容）的存储和传输性能