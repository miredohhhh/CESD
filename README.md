   # CESD Backend

CESD Backend 是业务系统的后端 API 服务。本阶段只提供基础后端框架能力，包括统一接口返回、统一异常处理、参数校验、MyBatis-Plus 基础配置、跨域配置、OpenAPI 文档和基础健康检查接口。

## 技术栈

- Java 21
- Spring Boot 3.5.14
- Maven
- Spring Web
- Spring Validation
- Spring Boot Actuator
- Lombok
- MySQL Driver
- MyBatis-Plus
- springdoc-openapi

## 环境要求

- JDK 21+
- Maven 3.9+
- MySQL 8.x，后续接入数据库表时使用

## 项目结构

```text
src/main/java/com/hjc/backend
  common/       通用返回、错误码、分页对象
  config/       MyBatis-Plus、CORS、OpenAPI 配置
  controller/   API 控制器
  dto/          请求 DTO
  entity/       数据库实体
  exception/    业务异常和全局异常处理
  mapper/       MyBatis-Plus Mapper
  service/      业务服务接口
  service/impl/ 业务服务实现
  utils/        工具类
  vo/           响应 VO
```

## 本地启动

```bash
mvn clean package
mvn spring-boot:run
```

默认服务端口是 `8080`，可以通过环境变量覆盖：

```bash
SERVER_PORT=8081 mvn spring-boot:run
```

Windows PowerShell 示例：

```powershell
$env:SERVER_PORT="8081"
mvn spring-boot:run
```

## 数据库配置

默认配置位于 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/cesd?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
```

建议本地或部署环境通过环境变量覆盖数据库连接信息：

```powershell
$env:DB_URL="jdbc:mysql://localhost:3306/cesd?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
$env:DB_USERNAME="root"
$env:DB_PASSWORD="your-password"
mvn spring-boot:run
```

## 接口地址

健康检查：

```text
GET http://localhost:8080/api/health
```

测试接口：

```text
GET http://localhost:8080/api/test/ping
```

参数校验测试：

```text
POST http://localhost:8080/api/test/validate
Content-Type: application/json

{
  "name": "test",
  "email": "test@example.com"
}
```

Swagger UI：

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON：

```text
http://localhost:8080/v3/api-docs
```

## Maven 常用命令

```bash
mvn clean
mvn clean package
mvn test
mvn spring-boot:run
```

## 后续开发计划

1. 根据数据库表创建 `entity`。
2. 在 `mapper` 中创建继承 `BaseMapper<T>` 的 Mapper 接口。
3. 在 `service` 中定义业务接口。
4. 在 `service.impl` 中实现业务逻辑。
5. 在 `controller` 中暴露 CRUD API，并统一返回 `ApiResponse`。
6. 根据业务需要补充事务、字段填充、逻辑删除、数据权限和认证授权。
