# 同窗闲物

> 基于 Spring Boot 的校园二手交易平台

面向高校校园场景的二手闲置物品交易平台，提供用户认证、商品管理、商品搜索、订单交易等功能。

项目围绕认证授权、缓存、全文检索、消息队列及容器化部署等后端场景进行实践。

## 技术栈

**Java 17 · Spring Boot 4.1.1 · MyBatis-Plus 3.5.17 · MySQL · JWT · RBAC · BCrypt · Redis · Elasticsearch 9.4.5 · RabbitMQ 4.3.5 · Docker · Nginx · Maven · Git**

## 核心功能

* **用户与权限**：JWT 登录认证、RBAC 权限控制、USER / ADMIN 角色管理、BCrypt 密码加密
* **商品管理**：商品发布、查询、修改、删除、分页及用户归属校验
* **商品搜索**：基于 Elasticsearch 实现关键词及条件搜索
* **订单管理**：订单创建、支付、取消及订单状态管理
* **订单超时**：基于 RabbitMQ + TTL + 死信交换机实现未支付订单自动取消
* **缓存**：基于 Redis Cache-Aside 模式实现用户数据缓存及缓存失效
* **部署**：使用 Docker 部署应用及基础中间件，通过 Nginx 实现反向代理

## 系统架构

```text
                    Client
                      │
                      ▼
                 ┌─────────┐
                 │  Nginx  │
                 │   :80   │
                 └────┬────┘
                      │
                      ▼
              ┌───────────────┐
              │ Spring Boot   │
              │     :8081     │
              └───────┬───────┘
                      │
        ┌─────────────┼─────────────┬─────────────┐
        │             │             │             │
        ▼             ▼             ▼             ▼
     MySQL          Redis    Elasticsearch     RabbitMQ
```

项目采用分层架构：

```text
Controller
    ↓
Service
    ↓
Mapper
    ↓
MySQL
```

Redis、Elasticsearch 和 RabbitMQ 根据不同业务场景提供缓存、搜索及异步消息处理能力。

## 核心技术实现

### JWT + RBAC

通过自定义 JWT Filter 解析请求中的 Token，获取用户身份及角色信息，并结合 RBAC 实现 USER / ADMIN 权限控制。

同时对资源归属进行校验，限制普通用户只能操作自身权限范围内的数据。

### Redis

用户查询采用 Cache-Aside 缓存策略：

```text
查询 Redis
   │
   ├── 命中 → 返回缓存
   │
   └── 未命中
          ↓
       查询 MySQL
          ↓
       写入 Redis
```

用户数据发生修改或删除后主动删除对应缓存。

### Elasticsearch

商品核心数据存储于 MySQL，同时同步商品文档至 Elasticsearch。

商品新增、修改、删除时同步更新 Elasticsearch，实现商品关键词及多条件搜索。

### RabbitMQ

订单创建后发送异步消息，并为未支付订单设置 TTL。

```text
订单创建
   ↓
RabbitMQ
   ↓
TTL
   ↓
消息过期
   ↓
Dead Letter Exchange
   ↓
超时消费者
   ↓
取消订单 + 释放商品
```

通过消息队列实现订单异步处理，降低订单业务与超时处理之间的耦合。

### 事务与状态控制

订单创建、支付、取消等核心操作使用 Spring `@Transactional`。

商品状态通过数据库条件更新进行控制：

```text
在售 → 锁定 → 已售出
         │
         └── 超时/取消 → 在售
```

降低并发场景下重复购买及状态错误的风险。

## 项目部署

项目使用 Docker 进行容器化部署：

```text
Docker Environment
├── Nginx
├── tongchuang-xianwu
├── Redis
├── RabbitMQ
└── Elasticsearch

MySQL
└── Host Machine
```

应用与 Redis、Elasticsearch、RabbitMQ 通过 Docker Network 进行容器间通信，Nginx 作为统一 HTTP 入口。

当前请求链路：

```text
Client
  ↓
Nginx :80
  ↓
Spring Boot :8081
  ↓
业务服务
  ↓
MySQL / Redis / Elasticsearch / RabbitMQ
```

项目配置中的数据库密码及 JWT 密钥通过环境变量注入，避免敏感信息直接写入源码。

## 接口测试

项目使用 IntelliJ IDEA HTTP Client 进行接口测试，根目录提供 `.http` 测试文件。

主要测试内容：

* JWT 登录及权限验证
* 商品 CRUD
* Redis 缓存
* Elasticsearch 商品搜索
* 订单创建 / 支付 / 取消
* RabbitMQ 消息生产与消费
* 订单 TTL 超时及死信处理
* Docker 服务连接
* Nginx 反向代理

## 项目结构

```text
tongchuang-xianwu
├── src
│   └── main
│       ├── java/com/tcxw
│       │   ├── config
│       │   ├── consumer
│       │   ├── controller
│       │   ├── document
│       │   ├── dto
│       │   ├── entity
│       │   ├── exception
│       │   ├── filter
│       │   ├── interceptor
│       │   ├── mapper
│       │   ├── message
│       │   ├── producer
│       │   ├── repository
│       │   ├── service
│       │   └── utils
│       └── resources
│           └── application.yaml
├── Dockerfile
├── nginx.conf
├── pom.xml
└── README.md
```
