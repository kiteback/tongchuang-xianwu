# 同窗闲物

面向高校校园场景的二手闲置物品交易后端。项目围绕用户认证、商品管理、并发下单、缓存、搜索、延迟消息和 AI Tool Calling 展开，并提供数据库迁移、容器编排、健康检查与持续集成配置。

## 技术栈

Java 17 / Spring Boot 4.1.1 / Spring AI 2.0.1 / MyBatis-Plus / MySQL 5.7 / Redis 7 / Elasticsearch 9.4.5 / RabbitMQ 4.3.5 / JWT / BCrypt / JUnit / Mockito / Flyway / Docker Compose / Nginx / GitHub Actions

## 核心能力

- JWT + BCrypt 登录认证；Filter 校验用户当前状态，Interceptor + `@RequireRole` 实现 USER/ADMIN 简单角色鉴权。
- 用户、商品、订单 RESTful API，使用 DTO、Bean Validation 和统一错误响应隔离数据库实体与接口协议。
- Redis Cache-Aside 用户缓存，包含正常 TTL、短期空值缓存、主动失效和 Redis 故障时的 MySQL 回源。
- Elasticsearch 商品关键词、分类、价格、状态与分页搜索；ES不可用时回退MySQL，并支持管理员重建索引。
- 商品状态条件更新与订单事务，保证多人同时购买同一商品时最多一个请求成功锁定。
- 支付、取消与超时消费统一按“订单行 -> 商品行”更新，降低相反加锁顺序导致的死锁风险。
- RabbitMQ 完整声明普通订单队列、TTL队列、DLX、超时处理队列和失败队列；条件更新保证重复超时消息幂等。
- Spring AI 自然语言商品查询，通过只读 Tool Calling 调用商品搜索和商品详情服务，模型不直接访问数据库。

## 架构

```text
Client
  |
Nginx :80
  |
Spring Boot :8081
  |-- MySQL         核心业务数据与条件更新
  |-- Redis         用户详情缓存
  |-- Elasticsearch 商品搜索索引
  `-- RabbitMQ      订单事件与超时取消
```

## 快速启动

### 1. 默认模式（AI关闭）

确认本机MySQL已启动并创建 `tongchuang_xianwu` 数据库，然后设置数据库密码、构建JAR并启动容器：

```powershell
$env:DB_PASSWORD="your-mysql-password"
.\mvnw.cmd clean package
docker compose up -d --build
```

Compose会启动Redis、RabbitMQ、Elasticsearch、Spring Boot和Nginx；应用通过 `host.docker.internal:3306` 使用Windows本机MySQL。Flyway会自动创建或升级用户、商品和订单表。

- API入口：`http://localhost`
- 应用直连：`http://localhost:8081`
- 健康检查：`http://localhost:8081/actuator/health`
- RabbitMQ控制台：`http://localhost:15672`（guest / guest）
- Elasticsearch：`http://localhost:9200`

默认密码仅用于本地开发。需要自定义时复制 `.env.example` 为 `.env` 并修改其中变量。

注册接口始终创建 `USER`，不会接受客户端传入的管理员角色。首次本地演示管理员接口时，可在确认目标用户后进入MySQL执行：

```sql
UPDATE `user` SET `role` = 'ADMIN' WHERE `username` = 'your-admin-user';
```

### 2. 开启AI商品查询

在 `.env` 中设置：

```dotenv
AI_CHAT_MODEL=openai
OPENAI_API_KEY=your-api-key
OPENAI_MODEL=gpt-5-mini
```

也可以通过 `OPENAI_BASE_URL` 接入兼容 OpenAI Chat Completions 的模型服务。未配置AI时，其余业务功能仍可正常启动，AI接口不会注册。

### 3. 本地测试

Windows：

```powershell
.\mvnw.cmd verify
```

Linux/macOS：

```bash
./mvnw verify
```

测试覆盖 JWT 正常/篡改/过期、Redis故障回源、禁用用户登录、100请求并发抢购、支付与超时竞争、重复超时消息以及AI工具参数和模型失败分支。

## 主要接口

| 方法 | 路径 | 说明 | 权限 |
| --- | --- | --- | --- |
| POST | `/register` | 用户注册 | 公开 |
| POST | `/login` | 用户登录 | 公开 |
| GET | `/products` | 商品分页 | 公开 |
| GET | `/products/{id}` | 商品详情 | 公开 |
| GET | `/products/search` | 多条件搜索 | 公开 |
| POST | `/products` | 发布商品 | 登录用户 |
| PUT | `/products/{id}` | 修改可售商品 | 所有者/管理员 |
| DELETE | `/products/{id}` | 删除可售商品 | 所有者/管理员 |
| POST | `/orders` | 创建订单 | 登录用户 |
| GET | `/orders/my` | 我的订单 | 登录用户 |
| PUT | `/orders/{id}/pay` | 支付订单 | 买家 |
| PUT | `/orders/{id}/cancel` | 取消订单 | 买家 |
| POST | `/products/search/rebuild` | 重建商品索引 | ADMIN |
| POST | `/ai/products/query` | 自然语言商品查询 | 登录用户、AI开启 |

完整请求示例位于 [`requests/api.http`](requests/api.http)。

## 并发下单设计

创建订单时不使用“先查询状态再直接更新”的结果作为成功依据，而是执行带旧状态条件的更新：

```sql
UPDATE product
SET status = 2
WHERE id = ? AND status = 1;
```

只有影响行数为1的请求可以继续创建订单，其余请求返回商品已被锁定。支付、取消与超时处理同样通过订单和商品状态条件更新限制合法迁移。

```text
商品：可售(1) -> 锁定(2) -> 售出(3)
                      `-> 取消/超时 -> 可售(1)

订单：待支付(1) -> 已支付(2)
               `-> 已取消(4)
```

## RabbitMQ超时链路

```text
订单事务提交
  |-- order.exchange -> order.queue
  `-- order.timeout.queue --TTL--> order.dlx
                                  `-> order.timeout.handler.queue
                                      `-> 条件取消订单并释放商品
```

消费失败会进行有限重试，耗尽后进入 `order.failed.queue`。相同超时消息再次投递时，订单条件更新影响0行，消费者直接忽略，不会重复释放商品。

## 错误响应

```json
{
  "code": "VALIDATION_ERROR",
  "message": "请求参数校验失败",
  "timestamp": "2026-09-16T20:00:00+08:00",
  "path": "/products",
  "fieldErrors": {
    "price": "商品价格必须大于0"
  }
}
```

- `401`：未登录、Token无效、用户不存在或已禁用。
- `403`：身份有效，但没有对应资源或角色权限。
- `404`：资源不存在。
- `409`：唯一键等数据库约束冲突。

## 数据一致性边界

- MySQL 是用户、商品和订单的事实数据源。
- Redis不可用时查询回源MySQL；缓存写入或删除失败会记录日志，不阻断主业务。
- ES同步采用尽力而为策略，失败时记录日志，可通过管理员接口从MySQL重建索引。
- RabbitMQ消息在数据库事务提交后发送，避免回滚事务产生幽灵消息；提交后发送仍存在极小概率的消息丢失窗口，生产级系统可进一步引入Outbox。
- AI只负责理解自然语言和选择工具，商品事实必须来自Java业务服务。

## 项目结构

```text
src/main/java/com/tcxw
|-- ai             Spring AI网关与商品工具
|-- annotation     角色注解
|-- config         MVC、MyBatis、RabbitMQ等配置
|-- consumer       订单消息消费者
|-- controller     HTTP接口
|-- dto            请求与响应模型
|-- entity         MySQL实体
|-- enums          订单/商品状态
|-- exception      业务异常与统一错误响应
|-- filter         JWT认证过滤器
|-- interceptor    角色鉴权拦截器
|-- mapper         MyBatis-Plus Mapper与条件更新SQL
|-- producer       事务提交后消息发送
|-- repository     Elasticsearch Repository
|-- service        业务、搜索与索引服务
`-- utils          JWT与Redis Key工具
```

## CI

GitHub Actions 在 push 和 pull request 时使用 Java 17 执行 `./mvnw verify`。AI在测试环境默认关闭，测试不会消耗模型额度或依赖外部API。
