# MaConfig - 分布式配置中心

<p align="center">
  <img src="https://img.shields.io/badge/Version-v2.0.0-blue.svg" alt="Version">
  <img src="https://img.shields.io/badge/License-Apache%202.0-green.svg" alt="License">
  <img src="https://img.shields.io/badge/Build-Passing-brightgreen.svg" alt="Build Status">
  <img src="https://img.shields.io/badge/Coverage-60%25-yellow.svg" alt="Coverage">
</p>

## 📖 项目简介

MaConfig 是一个轻量级、高可用的分布式配置中心，专为 Spring Boot 应用设计。它提供配置的统一管理、实时推送、版本控制等功能，帮助开发者轻松管理多环境下的应用配置。

### 核心特性

- 🔧 **零侵入集成**：通过注解方式无缝集成 Spring Boot 应用
- ⚡ **实时推送**：配置变更秒级生效，无需重启应用
- 🛡️ **高可用设计**：支持主备选举、故障自动切换
- 🔄 **配置版本化**：完整的版本控制和变更历史追踪
- 🎯 **多环境支持**：应用/环境/命名空间三级配置隔离
- 🔍 **丰富查询接口**：支持全量、单 key、应用列表等多种查询方式

## 🏗️ 系统架构

```
┌─────────────┐    HTTP Polling    ┌─────────────┐
│   Client    │ ←─────────────────→ │   Server    │
│ Application │                    │  Config DB  │
└─────────────┘                    └─────────────┘
       │                                  │
       │         Configuration            │
       └───────── Updates ───────────────┘
```

### 组件说明

- **Server**：配置管理中心，提供 RESTful API 和 Web UI
- **Client SDK**：Spring Boot Starter，负责配置拉取和注入
- **Database**：存储配置数据（默认 H2，支持 MySQL 等）

## 🚀 快速开始

### 环境要求

- Java 17+
- Maven 3.6+
- Spring Boot 3.x

### 服务端部署

```bash
# 克隆项目
git clone https://github.com/malinghan/maconfig.git
cd maconfig

# 编译打包
./mvnw clean package -DskipTests

# 启动服务端
java -jar maconfig-server/target/maconfig-server-1.0.0-SNAPSHOT.jar
```

服务启动后访问：
- API 文档：http://localhost:9091/swagger-ui.html
- 健康检查：http://localhost:9091/actuator/health

### 客户端集成

1. **添加依赖**

```xml
<dependency>
    <groupId>com.malinghan</groupId>
    <artifactId>maconfig-client</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

2. **启用配置中心**

```java
@SpringBootApplication
@EnableMaConfig(serverUrl = "http://localhost:9091")
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

3. **使用配置**

```java
@RestController
public class DemoController {
    
    @Value("${demo.message:Hello World}")
    private String message;
    
    @GetMapping("/demo")
    public Map<String, String> getDemo() {
        return Map.of("message", message);
    }
}
```

## 📚 使用指南

### 配置管理 API

#### 写入配置
```bash
curl -X POST "http://localhost:9091/v1/configs?app=demo-app&env=dev&ns=default" \
  -H "Content-Type: application/json" \
  -d '{"demo.message":"Hello MaConfig","demo.version":"1.0.0"}'
```

#### 查询配置
```bash
# 查询应用所有配置
curl "http://localhost:9091/v1/configs?app=demo-app&env=dev&ns=default"

# 查询单个配置项
curl "http://localhost:9091/v1/configs/single?app=demo-app&env=dev&ns=default&pkey=demo.message"

# 查询应用列表
curl "http://localhost:9091/v1/apps"
```

#### 删除配置
```bash
curl -X DELETE "http://localhost:9091/v1/configs?app=demo-app&env=dev&ns=default&pkey=demo.message"
```

### 客户端配置

```yaml
# application.yml
maconfig:
  server-url: http://localhost:9091
  app: ${spring.application.name}
  env: ${spring.profiles.active:default}
  namespace: default
  fail-fast: false  # 启动容错模式
  poll-interval: 5000  # 轮询间隔(ms)
```

## 🛠️ 开发指南

### 项目结构

```
maconfig/
├── maconfig-client/     # 客户端 SDK
│   ├── src/main/java/
│   │   └── com/malinghan/maconfig/client/
│   │       ├── annotation/     # 注解定义
│   │       ├── processor/      # 处理器
│   │       ├── repository/     # 数据访问
│   │       └── MaConfigServiceImpl.java  # 核心实现
│   └── pom.xml
├── maconfig-server/     # 服务端
│   ├── src/main/java/
│   │   └── com/malinghan/maconfig/server/
│   │       ├── controller/     # 控制器
│   │       ├── service/        # 业务逻辑
│   │       ├── mapper/         # 数据映射
│   │       └── model/          # 数据模型
│   └── src/main/resources/
│       ├── application.yml     # 配置文件
│       └── schema.sql          # 数据库脚本
├── maconfig-demo/       # 示例应用
└── pom.xml             # 父项目配置
```

### 构建项目

```bash
# 清理并编译
./mvnw clean compile

# 运行测试
./mvnw test

# 打包所有模块
./mvnw clean package -DskipTests

# 启动示例应用
cd maconfig-demo && ../mvnw spring-boot:run
```

### 本地开发

```bash
# 启动服务端（开发模式）
./mvnw spring-boot:run -pl maconfig-server

# 启动示例客户端
cd maconfig-demo && ../mvnw spring-boot:run
```

## 🧪 测试验证

### 验收测试流程

#### 场景一：Server 重启无感知

```bash
# 1. 启动服务
java -jar maconfig-server/target/maconfig-server-1.0.0-SNAPSHOT.jar
cd maconfig-demo && ../mvnw spring-boot:run

# 2. 写入配置
curl -X POST "http://localhost:9091/v1/configs?app=demo-app&env=dev&ns=default" \
  -H "Content-Type: application/json" \
  -d '{"demo.message":"v2.0 restart test","demo.version":"2.0.0"}'

# 3. 验证配置获取
curl http://localhost:8080/demo

# 4. 重启 Server
kill $(lsof -ti:9091)
java -jar maconfig-server/target/maconfig-server-1.0.0-SNAPSHOT.jar

# 5. 验证无感知加载
curl http://localhost:8080/demo
```

#### 场景二：Server 宕机容错

```bash
# 1. 启动客户端（Server 未启动）
cd maconfig-demo && ../mvnw spring-boot:run

# 2. 验证使用本地默认值
curl http://localhost:8080/demo

# 3. 启动 Server 并验证同步
java -jar maconfig-server/target/maconfig-server-1.0.0-SNAPSHOT.jar
# 等待 5 秒后验证配置同步
```

详细测试流程请参考 [v2.0.md](v2.0.md)

## 📊 版本演进

### v2.0 主要改进 ✨

- ✅ **版本号持久化**：基于 `updated_at` 时间戳实现版本连续性
- ✅ **启动容错机制**：`fail-fast=false` 模式下优雅降级
- ✅ **配置删除支持**：完整的配置生命周期管理
- ✅ **查询接口增强**：新增单 key 查询、应用列表等接口
- ✅ **日志规范化**：统一的日志格式和级别
- ✅ **测试覆盖率提升**：从 ~30% 提升至 >60%

### 版本对比

| 功能特性 | v1.0 | v2.0 |
|---------|------|------|
| 启动容错 | ❌ | ✅ |
| 重启感知 | ❌ | ✅ |
| 配置删除 | ❌ | ✅ |
| 查询能力 | 基础 | 增强 |
| 测试覆盖率 | ~30% | >60% |

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

### 开发流程

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/your-feature`
3. 提交更改：`git commit -am 'Add some feature'`
4. 推送分支：`git push origin feature/your-feature`
5. 创建 Pull Request

### 代码规范

- 遵循 Google Java Style Guide
- 添加必要的单元测试
- 更新相关文档
- 保持向后兼容性

## 📄 许可证

本项目采用 Apache License 2.0 许可证，详情请见 [LICENSE](LICENSE) 文件。

## 📞 联系方式

- 项目主页：[https://github.com/malinghan/maconfig](https://github.com/malinghan/maconfig)
- Issues：[https://github.com/malinghan/maconfig/issues](https://github.com/malinghan/maconfig/issues)
- 邮箱：malinghan@example.com

---

<p align="center">
  Made with ❤️ by MaConfig Team
</p>