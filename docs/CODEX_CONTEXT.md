# CESD 综合测评系统后端 Codex 上下文

本文档是后续 Codex 继续开发 CESD 后端时必须优先阅读的长期上下文文件。后续任务应以当前代码和本文档为准，不依赖对话记忆。

更新时间：2026-05-11

## 1. 项目概览

- 项目名称：CESD Backend
- 项目用途：综合测评系统后端 API 服务
- 当前技术栈：Spring Boot、Spring Web、Validation、Actuator、Lombok、MyBatis-Plus、MySQL、springdoc-openapi
- Java 版本：21
- Spring Boot 版本：3.5.14
- 构建工具：Maven
- ORM 框架：MyBatis-Plus 3.5.14
- 数据库类型：MySQL
- 数据库名称：`comprehensive_evaluation`
- API 文档工具：springdoc-openapi，Swagger UI 路径 `/swagger-ui.html`，OpenAPI JSON 路径 `/v3/api-docs`
- 当前项目路径：`D:\CESD\backend`
- 当前真实根包名：`com.hjc.backend`
- 配置文件：实际存在的是 `src/main/resources/application.yaml`，不是 `application.yml`

主要 Maven 依赖：

- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `mysql-connector-j`
- `mybatis-plus-spring-boot3-starter`
- `mybatis-plus-jsqlparser`
- `springdoc-openapi-starter-webmvc-ui`
- `lombok`
- `spring-boot-configuration-processor`
- `spring-boot-starter-test`

## 2. 数据库设计概览

数据库建表 SQL 已保存到 `docs/database.sql`。后续涉及字段、表结构、逻辑外键和初始化数据校对时，应优先读取该文件。

- 数据库名：`comprehensive_evaluation`
- 表数量：14 张
- 是否使用物理外键：不使用
- 关系维护原则：通过逻辑外键字段、索引和 ServiceImpl 业务校验维护表关系
- 逻辑删除规则：`deleted = 0` 表示未删除，`deleted = 1` 表示已删除
- MyBatis-Plus 全局逻辑删除配置：
  - `logic-delete-field: deleted`
  - `logic-delete-value: 1`
  - `logic-not-delete-value: 0`
- 有 `deleted` 字段的 Entity 必须使用 `@TableLogic`
- 不使用 JPA，不使用 `@Entity`、`@Table`、`@Column`、`@Id`
- 不使用 JPA 关联注解，不使用 `@ManyToOne`、`@OneToMany`

包含 `deleted` 字段并已使用 `@TableLogic` 的表：

- `sys_role`
- `sys_user`
- `major_info`
- `class_info`
- `student`
- `evaluation_category`
- `evaluation_item`
- `material_application`
- `material_attachment`
- `score_summary`
- `score_category_summary`
- `system_config`
- `reviewer_scope`

没有 `deleted` 字段的表：

- `review_record`

特别注意：`review_record` 没有 `deleted` 字段，Entity 中不得添加 `deleted`，也不得使用 `@TableLogic`。

## 3. 数据库表清单

1. `sys_role`：角色表，存储学生、审核员、管理员等角色。
2. `sys_user`：用户表，存储登录账号、密码哈希、真实姓名、角色、联系方式等。
3. `major_info`：专业表，存储专业名称、专业编码、所属学院等基础数据。
4. `class_info`：班级表，存储班级名称、班级编码、所属专业、年级、辅导员等。
5. `student`：学生表，存储学生与用户、专业、班级之间的逻辑关系。
6. `evaluation_category`：综测分类表，存储综测分类、分类编码、最高分、排序等。
7. `evaluation_item`：综测项目表，存储分类下的具体综测项目、计分方式、分值、附件要求等。
8. `material_application`：材料申报表，存储学生提交的综测材料主信息。
9. `material_attachment`：材料附件表，存储材料对应的附件文件信息。
10. `review_record`：审核记录表，存储材料审核历史。
11. `score_summary`：总成绩统计表，存储学生总分、班级排名、专业排名等。
12. `score_category_summary`：分类成绩统计表，存储学生在各综测分类下的得分。
13. `system_config`：系统配置表，存储开关、上传限制等配置项。
14. `reviewer_scope`：审核范围表，存储审核员可审核的专业、班级、年级范围。

## 4. 核心逻辑外键关系

以下关系不依赖数据库 `FOREIGN KEY`，必须在 ServiceImpl 中手动校验：

- `sys_user.role_id -> sys_role.id`
- `class_info.major_id -> major_info.id`
- `student.user_id -> sys_user.id`
- `student.major_id -> major_info.id`
- `student.class_id -> class_info.id`
- `evaluation_item.category_id -> evaluation_category.id`
- `material_application.student_id -> student.id`
- `material_application.item_id -> evaluation_item.id`
- `material_attachment.material_id -> material_application.id`
- `review_record.material_id -> material_application.id`
- `review_record.reviewer_id -> sys_user.id`
- `score_summary.student_id -> student.id`
- `score_category_summary.student_id -> student.id`
- `score_category_summary.category_id -> evaluation_category.id`
- `reviewer_scope.reviewer_id -> sys_user.id`
- `reviewer_scope.major_id -> major_info.id`
- `reviewer_scope.class_id -> class_info.id`

当前已在 ServiceImpl 中实现的逻辑外键校验：

- `class_info.major_id -> major_info.id`
- `evaluation_item.category_id -> evaluation_category.id`
- `sys_user.role_id -> sys_role.id`
- `student.user_id -> sys_user.id`
- `student.major_id -> major_info.id`
- `student.class_id -> class_info.id`
- `student.major_id` 与 `class_info.major_id` 一致性校验
- `material_application.student_id -> student.id`
- `material_application.item_id -> evaluation_item.id`
- `material_attachment.material_id -> material_application.id`

## 5. 当前项目包结构

根包：`com.hjc.backend`

- `common`：通用响应、错误码、分页请求与分页结果。
- `config`：CORS、MyBatis-Plus、OpenAPI 配置。
- `exception`：业务异常和全局异常处理。
- `entity`：数据库表实体，使用 MyBatis-Plus 注解。
- `mapper`：MyBatis-Plus Mapper，继承 `BaseMapper<T>`。
- `service`：业务服务接口，继承 `IService<T>`，并声明业务方法。
- `service.impl`：业务服务实现，继承 `ServiceImpl<Mapper, Entity>`，业务校验写在这里。
- `controller`：REST API 控制器，只接收参数并返回结果。
- `dto`：请求参数对象。
- `vo`：返回前端的视图对象。
- `utils`：工具类包，当前仅保留包结构。

## 6. 已完成开发阶段

### 第一步：基础后端框架

状态：已完成

已完成内容：

- `ApiResponse`
- `ResultCode`
- `BusinessException`
- `GlobalExceptionHandler`
- `PageRequest`
- `PageResult`
- MyBatis-Plus 配置：`MybatisPlusConfig`
- CORS 配置：`CorsConfig`
- OpenAPI / Swagger 配置：`OpenApiConfig`
- 健康检查接口：`GET /api/health`
- 测试接口：
  - `GET /api/test/ping`
  - `POST /api/test/validate`
- `application.yaml` 基础配置

### 第二步：数据库基础映射

状态：已完成

已完成内容：

- 14 个 Entity
- 14 个 Mapper
- 14 个 Service
- 14 个 ServiceImpl
- Entity 已使用 `@TableName`
- 主键已使用 `@TableId(value = "id", type = IdType.AUTO)`
- 下划线字段按需使用 `@TableField`
- 有 `deleted` 字段的 Entity 已使用 `@TableLogic`
- `review_record` 未使用 `@TableLogic`
- 启动类已配置 `@MapperScan("com.hjc.backend.mapper")`
- 时间字段使用 `LocalDateTime`
- 分数和金额字段使用 `BigDecimal`

### 第三步：基础数据模块 CRUD

状态：已完成

已完成模块：

- `sys_role`
- `major_info`
- `class_info`
- `system_config`
- `evaluation_category`
- `evaluation_item`

每个模块已包含：

- Create DTO
- Update DTO
- VO
- Controller
- Service 业务方法
- 分页查询
- 详情查询
- 新增
- 修改
- 逻辑删除
- 唯一性校验

已实现的逻辑外键校验：

- `class_info.major_id -> major_info.id`
- `evaluation_item.category_id -> evaluation_category.id`

已保留 TODO：

- 删除专业前后续检查班级、学生依赖。
- 删除班级前后续检查学生依赖。
- 删除综测分类前后续检查综测项目依赖。
- 删除综测项目前后续检查材料申报依赖。

### 第四步：用户与学生模块 CRUD

状态：已完成

已完成模块：

- `sys_user`
- `student`

已完成内容：

- `CreateSysUserRequest`
- `UpdateSysUserRequest`
- `SysUserVO`
- `SysUserController`
- `SysUserService` 业务方法
- `SysUserServiceImpl` 业务实现
- `CreateStudentRequest`
- `UpdateStudentRequest`
- `StudentVO`
- `StudentController`
- `StudentService` 业务方法
- `StudentServiceImpl` 业务实现

关键规则：

- `SysUserVO` 不包含 `passwordHash`
- `SysUserVO` 不包含 `deleted`
- `StudentVO` 不包含 `deleted`
- 用户新增和修改校验 `roleId`
- 学生新增和修改校验 `userId`、`majorId`、`classId`
- 学生新增和修改校验 `studentNo` 唯一
- 用户新增和修改校验 `username` 唯一
- 学生新增和修改校验专业与班级所属专业一致

已保留 TODO：

- 后续登录注册接入时，`passwordHash` 应由后端使用 BCrypt 生成，不能由前端直接传明文或哈希。
- 删除用户前后续检查关联学生、审核记录等数据。
- 删除学生前后续检查材料申报、总成绩、分类成绩等依赖。
- 用户 VO 后续可联表返回 `roleName`。
- 学生 VO 后续可联表返回 `username`、`majorName`、`className`。

### 第五步：材料申报与附件元数据基础 CRUD

状态：已完成

已完成模块：

- `material_application`
- `material_attachment`

已完成内容：

- `CreateMaterialApplicationRequest`
- `UpdateMaterialApplicationRequest`
- `MaterialApplicationVO`
- `MaterialApplicationController`
- `MaterialApplicationService` 业务方法
- `MaterialApplicationServiceImpl` 业务实现
- `CreateMaterialAttachmentRequest`
- `UpdateMaterialAttachmentRequest`
- `MaterialAttachmentVO`
- `MaterialAttachmentController`
- `MaterialAttachmentService` 业务方法
- `MaterialAttachmentServiceImpl` 业务实现

关键规则：

- `MaterialApplicationVO` 不包含 `deleted`
- `MaterialAttachmentVO` 不包含 `deleted`
- 材料申报新增和修改校验 `studentId`
- 材料申报新增和修改校验 `itemId`
- 材料附件新增和修改校验 `materialId`
- 材料申报 `status` 仅允许 `DRAFT`、`SUBMITTED`、`APPROVED`、`REJECTED`、`CANCELLED`
- 新增材料申报时 `status` 为空默认 `DRAFT`
- 附件 `fileSize` 不允许小于 0

已保留 TODO：

- 暂不实现真实文件上传、`MultipartFile`、本地存储或云存储。
- 暂不实现材料提交、撤回、审核通过、审核驳回流程。
- 删除材料申报前后续检查附件和审核记录依赖。
- 目前不实现复杂联表查询与展示字段。

## 7. 当前已实现接口清单

### 健康检查

- `GET /api/health`

### 测试接口

- `GET /api/test/ping`
- `POST /api/test/validate`

### 角色管理

- `GET /api/roles/page`
- `GET /api/roles/{id}`
- `POST /api/roles`
- `PUT /api/roles/{id}`
- `DELETE /api/roles/{id}`

### 专业管理

- `GET /api/majors/page`
- `GET /api/majors/{id}`
- `POST /api/majors`
- `PUT /api/majors/{id}`
- `DELETE /api/majors/{id}`

### 班级管理

- `GET /api/classes/page`
- `GET /api/classes/{id}`
- `POST /api/classes`
- `PUT /api/classes/{id}`
- `DELETE /api/classes/{id}`

### 系统配置管理

- `GET /api/system-configs/page`
- `GET /api/system-configs/{id}`
- `POST /api/system-configs`
- `PUT /api/system-configs/{id}`
- `DELETE /api/system-configs/{id}`

### 综测分类管理

- `GET /api/evaluation-categories/page`
- `GET /api/evaluation-categories/{id}`
- `POST /api/evaluation-categories`
- `PUT /api/evaluation-categories/{id}`
- `DELETE /api/evaluation-categories/{id}`

### 综测项目管理

- `GET /api/evaluation-items/page`
- `GET /api/evaluation-items/{id}`
- `POST /api/evaluation-items`
- `PUT /api/evaluation-items/{id}`
- `DELETE /api/evaluation-items/{id}`

### 用户管理

- `GET /api/users/page`
- `GET /api/users/{id}`
- `POST /api/users`
- `PUT /api/users/{id}`
- `DELETE /api/users/{id}`

### 学生管理

- `GET /api/students/page`
- `GET /api/students/{id}`
- `POST /api/students`
- `PUT /api/students/{id}`
- `DELETE /api/students/{id}`

### 材料申报管理

- `GET /api/material-applications/page`
- `GET /api/material-applications/{id}`
- `POST /api/material-applications`
- `PUT /api/material-applications/{id}`
- `DELETE /api/material-applications/{id}`

### 材料附件管理

- `GET /api/material-attachments/page`
- `GET /api/material-attachments/{id}`
- `POST /api/material-attachments`
- `PUT /api/material-attachments/{id}`
- `DELETE /api/material-attachments/{id}`

## 8. 当前统一开发规范

1. Controller 只接收参数和返回结果。
2. 业务校验写在 ServiceImpl。
3. Mapper 只做数据库访问。
4. DTO 用于请求参数。
5. VO 用于返回前端。
6. 不直接返回 Entity。
7. 所有接口统一返回 `ApiResponse`。
8. 分页接口返回 `PageResult`。
9. 业务错误统一抛出 `BusinessException`。
10. 使用 `GlobalExceptionHandler` 统一异常处理。
11. 使用 MyBatis-Plus，不使用 JPA。
12. 不使用物理外键。
13. 不使用 `@ManyToOne`、`@OneToMany`。
14. 有 `deleted` 字段的表使用逻辑删除。
15. `review_record` 不使用 `@TableLogic`。
16. `SysUserVO` 不允许返回 `passwordHash`。
17. 不确定业务规则时写 TODO，不擅自扩展。
18. 新增接口必须添加 Swagger 注解。
19. 修改 Java 代码后必须执行 `mvn clean package`。
20. 只修改文档时可以不执行 Maven 编译，但最终说明原因。

## 9. 当前暂不实现的内容

当前阶段暂不实现：

- 登录
- 注册
- JWT
- Spring Security
- 角色权限控制
- 菜单权限
- 数据权限
- 文件真实上传
- 云存储
- 审核流程
- 成绩自动计算
- 排名计算
- 复杂联表查询
- Redis
- RabbitMQ
- Kafka
- Elasticsearch
- Docker 部署

## 10. 后续开发计划

当前项目已完成到第五步：材料申报与附件元数据基础 CRUD。

建议后续顺序：

1. 实现 `review_record` 查询与新增能力。
2. 实现材料提交、撤回、审核通过、审核驳回流程。
3. 实现 `score_summary` 和 `score_category_summary` 成绩统计与排名。
4. 实现登录、JWT、Spring Security。
5. 实现角色权限和数据权限。
6. 实现文件上传、云存储或本地存储策略。
7. 实现统计分析和复杂联表查询。

下一步优先建议：`review_record` 查询与新增能力，或先实现材料提交、撤回、审核状态流转的 Service 方法。

## 11. 后续 Codex 任务执行规则

每次 Codex 开发新任务前，必须先阅读：

- `docs/CODEX_CONTEXT.md`
- `docs/database.sql`，如果该文件存在
- `README.md`
- `pom.xml`
- `src/main/resources/application.yaml`
- 当前相关模块代码

每次任务必须做到：

1. 明确本次任务范围。
2. 不扩大任务范围。
3. 不修改无关模块。
4. 不重复创建已有类。
5. 不引入无关依赖。
6. 不修改数据库结构。
7. 不创建物理外键。
8. 不使用 JPA。
9. 不破坏已有接口。
10. 完成后说明修改了哪些文件。
11. 如果修改 Java 代码，执行 `mvn clean package`。
12. 如果只修改文档，可以说明未执行编译的原因。

## 12. 开发日志维护规则

每完成一个阶段或重要任务后，都要更新：

- 当前完成阶段
- 新增文件
- 修改文件
- 新增接口
- 已实现校验
- 保留 TODO
- 编译结果
- 下一步建议

阶段日志记录在 `docs/DEVELOPMENT_LOG.md`。

## 13. 当前已知文档与配置差异

- `README.md` 存在中文乱码，且数据库示例仍出现 `cesd`，实际 `application.yaml` 当前数据库名是 `comprehensive_evaluation`。
- 用户任务中常提到 `application.yml`，但当前项目实际文件名是 `application.yaml`。
- `docs/database.sql` 已存在，包含 14 张表建表 SQL 和初始化角色、系统配置数据。

## 14. 第六步补充：review_record 审核记录查询与新增能力

更新时间：2026-05-11

当前项目已完成第六步：`review_record` 审核记录查询与新增能力。

### 已新增文件

- `src/main/java/com/hjc/backend/dto/CreateReviewRecordRequest.java`
- `src/main/java/com/hjc/backend/dto/ReviewRecordPageRequest.java`
- `src/main/java/com/hjc/backend/vo/ReviewRecordVO.java`
- `src/main/java/com/hjc/backend/controller/ReviewRecordController.java`

### 已修改文件

- `src/main/java/com/hjc/backend/service/ReviewRecordService.java`
- `src/main/java/com/hjc/backend/service/impl/ReviewRecordServiceImpl.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

### 新增接口

审核记录模块基础路径：`/api/review-records`

- `GET /api/review-records/page`：审核记录分页查询
- `GET /api/review-records/{id}`：审核记录详情查询
- `POST /api/review-records`：新增审核记录
- `GET /api/review-records/material/{materialId}`：查询某条材料的审核历史

### 已实现能力

- 审核记录分页查询，支持 `materialId`、`reviewerId`、`reviewResult`、`startReviewTime`、`endReviewTime` 筛选。
- 审核记录详情查询。
- 新增审核记录。
- 根据 `materialId` 查询审核历史。
- 分页结果返回 `PageResult<ReviewRecordVO>`。
- 所有接口统一返回 `ApiResponse`。
- Controller 只接收参数并返回结果。
- Entity 到 VO 的转换在 `ReviewRecordServiceImpl` 的 `toVO` 方法中完成。
- 查询默认按 `reviewTime` 倒序、`id` 倒序。

### 已实现校验

- `review_record.material_id -> material_application.id` 逻辑外键校验。
- `review_record.reviewer_id -> sys_user.id` 逻辑外键校验。
- `materialId`、`reviewerId`、`beforeStatus`、`afterStatus`、`reviewResult` 必填校验。
- `beforeStatus`、`afterStatus` 只允许 `DRAFT`、`SUBMITTED`、`APPROVED`、`REJECTED`、`CANCELLED`。
- `reviewResult` 只允许 `APPROVED`、`REJECTED`。
- `reviewScore` 不允许小于 0。
- `reviewComment` 最大长度 500。
- `reviewTime` 为空时由后端设置为当前时间。
- 审核记录详情查询时，记录不存在抛出 `BusinessException`。
- 按材料 ID 查询审核历史时，材料不存在或已删除抛出 `BusinessException`。

### 重要边界

- `review_record` 表没有 `deleted` 字段。
- `ReviewRecord` Entity 不允许添加 `deleted` 字段。
- `ReviewRecord` Entity 不允许使用 `@TableLogic`。
- 本阶段只允许新增和查询审核记录，不提供修改接口和删除接口。
- 本阶段不修改 `material_application.status`。
- 本阶段不修改 `material_application.final_score`。
- 本阶段不修改 `material_application.reject_reason`。
- 本阶段不修改 `material_application.review_time`。
- 本阶段不实现材料提交、撤回、审核通过、审核驳回状态流转。
- 本阶段不实现成绩计算和排名。
- 本阶段不实现登录、JWT、Spring Security、权限控制。

### 当前进度判断

当前项目已完成到第六步：`review_record` 审核记录查询与新增能力。

下一步建议：实现材料提交、撤回、审核通过、审核驳回状态流转 Service 方法。该阶段应复用 `review_record` 新增能力，但需要统一处理 `material_application` 状态、最终分数、驳回原因和审核时间更新。

## 15. 第七步补充：材料提交、撤回、审核通过、审核驳回状态流转

更新时间：2026-05-11

当前项目已完成第七步：`material_application` 材料状态流转 Service 方法。

### 已新增文件

- `src/main/java/com/hjc/backend/dto/ApproveMaterialApplicationRequest.java`
- `src/main/java/com/hjc/backend/dto/RejectMaterialApplicationRequest.java`
- `src/main/java/com/hjc/backend/dto/WithdrawMaterialApplicationRequest.java`

### 已修改文件

- `src/main/java/com/hjc/backend/service/MaterialApplicationService.java`
- `src/main/java/com/hjc/backend/service/impl/MaterialApplicationServiceImpl.java`
- `src/main/java/com/hjc/backend/controller/MaterialApplicationController.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

### 新增接口

材料申报模块基础路径：`/api/material-applications`

- `POST /api/material-applications/{id}/submit`：提交材料
- `POST /api/material-applications/{id}/withdraw`：撤回材料
- `POST /api/material-applications/{id}/approve`：审核通过材料
- `POST /api/material-applications/{id}/reject`：审核驳回材料

### 新增 Service 方法

- `submitMaterialApplication(Long id)`
- `withdrawMaterialApplication(Long id, WithdrawMaterialApplicationRequest request)`
- `approveMaterialApplication(Long id, ApproveMaterialApplicationRequest request)`
- `rejectMaterialApplication(Long id, RejectMaterialApplicationRequest request)`

### 已实现状态流转规则

- `DRAFT` / `REJECTED` / `CANCELLED` 可以提交为 `SUBMITTED`。
- `SUBMITTED` 可以撤回为 `CANCELLED`。
- `SUBMITTED` 可以审核通过为 `APPROVED`。
- `SUBMITTED` 可以审核驳回为 `REJECTED`。
- 非允许状态的重复提交、重复撤回、重复审核会抛出 `BusinessException`。

提交材料时：
- 设置 `status = SUBMITTED`。
- 设置 `submitTime = LocalDateTime.now()`。
- 设置 `submitCount = 原 submitCount + 1`。
- 清空 `rejectReason`。
- 不写入 `review_record`。
- 不修改 `finalScore`。

撤回材料时：
- 仅允许 `SUBMITTED` 状态撤回。
- 设置 `status = CANCELLED`。
- 不写入 `review_record`。
- 不修改 `finalScore`。
- 不修改 `rejectReason`。

审核通过时：
- 仅允许 `SUBMITTED` 状态审核通过。
- 设置 `status = APPROVED`。
- 设置 `finalScore = reviewScore`。
- 清空 `rejectReason`。
- 设置 `reviewTime = LocalDateTime.now()`。
- 通过 `ReviewRecordService.createReviewRecord` 写入 `review_record`。
- `review_record.reviewResult = APPROVED`。

审核驳回时：
- 仅允许 `SUBMITTED` 状态审核驳回。
- 设置 `status = REJECTED`。
- 设置 `finalScore = null`。
- 设置 `rejectReason = request.rejectReason`。
- 设置 `reviewTime = LocalDateTime.now()`。
- 通过 `ReviewRecordService.createReviewRecord` 写入 `review_record`。
- `review_record.reviewResult = REJECTED`。

### 已实现校验

- 材料申报必须存在且未逻辑删除。
- 审核人 `reviewerId` 必须存在且未逻辑删除。
- 审核通过时 `reviewScore` 必填且不能小于 0。
- 审核意见 `reviewComment` 长度不能超过 500。
- 驳回原因 `rejectReason` 必填且长度不能超过 500。
- 审核通过 / 驳回和审核记录新增在同一事务中完成。

### 重要边界与 TODO

- 本阶段不实现登录、JWT、Spring Security，`reviewerId` 暂时从请求体传入。
- TODO：接入认证后，`reviewerId` 必须从当前登录用户上下文获取，不能由前端任意传入。
- 本阶段不实现成绩计算、`score_summary`、`score_category_summary`、排名。
- TODO：成绩统计模块完成后，审核通过可触发成绩重算。
- 本阶段不实现真实文件上传、云存储、权限控制、数据权限、复杂联表查询。
- TODO：高并发场景可在状态流转更新中加入条件更新或乐观锁。

### 当前进度判断

当前项目已完成到第七步：材料提交、撤回、审核通过、审核驳回状态流转。

下一步建议：实现 `score_summary` 和 `score_category_summary` 成绩统计与排名，或先补充待审核列表、我的申报列表等查询能力。

## 16. 第八步补充：score_summary 与 score_category_summary 成绩统计与排名

更新时间：2026-05-11

当前项目已完成第八步：成绩统计与排名基础能力。

### 已新增文件

- `src/main/java/com/hjc/backend/dto/ScoreSummaryPageRequest.java`
- `src/main/java/com/hjc/backend/vo/ScoreSummaryVO.java`
- `src/main/java/com/hjc/backend/vo/ScoreCategorySummaryVO.java`
- `src/main/java/com/hjc/backend/controller/ScoreSummaryController.java`

### 已修改文件

- `src/main/java/com/hjc/backend/service/ScoreSummaryService.java`
- `src/main/java/com/hjc/backend/service/impl/ScoreSummaryServiceImpl.java`
- `src/main/java/com/hjc/backend/service/impl/MaterialApplicationServiceImpl.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

### 新增接口

成绩模块基础路径：`/api/scores`

- `POST /api/scores/students/{studentId}/recalculate`：重算单个学生成绩
- `POST /api/scores/classes/{classId}/recalculate`：重算某个班级所有学生成绩并更新排名
- `POST /api/scores/majors/{majorId}/recalculate`：重算某个专业所有学生成绩并更新排名
- `POST /api/scores/recalculate-all`：重算全部学生成绩并更新排名
- `GET /api/scores/students/{studentId}`：查询学生总成绩
- `GET /api/scores/students/{studentId}/categories`：查询学生分类成绩
- `GET /api/scores/page`：分页查询成绩统计
- `GET /api/scores/classes/{classId}/ranking`：查询班级排名
- `GET /api/scores/majors/{majorId}/ranking`：查询专业排名

### 新增 Service 方法

- `recalculateStudentScore(Long studentId)`
- `recalculateClassScores(Long classId)`
- `recalculateMajorScores(Long majorId)`
- `recalculateAllScores()`
- `getStudentScore(Long studentId)`
- `listStudentCategoryScores(Long studentId)`
- `pageScoreSummaries(ScoreSummaryPageRequest request)`
- `listClassRanking(Long classId)`
- `listMajorRanking(Long majorId)`

### 已实现成绩计算规则

- 只统计 `material_application.status = APPROVED` 的材料。
- 每条通过材料优先使用 `material_application.final_score`。
- `final_score` 为空的 APPROVED 材料暂不参与计算，并保留 TODO。
- `final_score < 0` 时抛出 `BusinessException`。
- 通过 `material_application.item_id -> evaluation_item.id` 找到综测项目。
- 通过 `evaluation_item.category_id -> evaluation_category.id` 归入分类。
- 按 `category_id` 汇总分类得分。
- 如果 `evaluation_category.max_score` 不为空，分类累计分数封顶到 `max_score`。
- 分类得分写入或更新 `score_category_summary`。
- 本次无通过材料的旧分类成绩更新为 0，并保留后续清理策略 TODO。
- 学生 `total_score` 等于本次分类得分汇总。
- 总分写入或更新 `score_summary`。
- `calculate_time` 使用 `LocalDateTime.now()`。
- `score_summary.status` 设置为 1。

### 已实现排名规则

- 班级排名按同一 `class_id` 学生的 `total_score` 降序计算。
- 专业排名按同一 `major_id` 学生的 `total_score` 降序计算。
- 同分使用相同排名。
- 同分后的下一名使用竞争排名，例如 100 分第 1 名、100 分第 1 名、98 分第 3 名。
- 同分稳定排序使用 `student_no` 升序、`student.id` 升序。
- 没有 `score_summary` 的学生会创建 0 分记录后参与排名。
- 单个学生重算后会更新其所在班级和专业排名。
- 班级重算后会更新该班级排名，并同步更新班级所属专业排名。
- 专业重算后会更新该专业排名，并更新专业下各班级排名。

### 重要边界与 TODO

- 本阶段不在审核通过时自动触发成绩重算，只提供手动重算接口。
- `MaterialApplicationServiceImpl` 中已补充 TODO：后续可在审核通过后调用 `ScoreSummaryService.recalculateStudentScore(studentId)` 或异步重算。
- 本阶段不实现 Redis 缓存。
- 本阶段不引入消息队列。
- 本阶段不实现复杂规则引擎。
- 本阶段不实现 Excel 导入导出。
- 本阶段不实现登录、JWT、Spring Security、权限控制、数据权限。

### 当前进度判断

当前项目已完成到第八步：`score_summary` 与 `score_category_summary` 成绩统计与排名。

下一步建议：实现登录、JWT、Spring Security；或先补充“我的申报”“待审核列表”“成绩查询”等前端友好查询接口；也可以实现 Excel 导出。

## 17. 第九步补充：前端友好查询接口

更新时间：2026-05-11

当前项目已完成第九步：围绕前端页面联调补充“我的申报”“申报详情”“待审核列表”“我的成绩”“排名查询”等查询接口。

### 新增文件

- `src/main/java/com/hjc/backend/controller/FrontendQueryController.java`
- `src/main/java/com/hjc/backend/dto/MyMaterialApplicationPageRequest.java`
- `src/main/java/com/hjc/backend/dto/PendingMaterialApplicationPageRequest.java`
- `src/main/java/com/hjc/backend/vo/MyMaterialApplicationVO.java`
- `src/main/java/com/hjc/backend/vo/MaterialApplicationDetailVO.java`
- `src/main/java/com/hjc/backend/vo/PendingMaterialApplicationVO.java`
- `src/main/java/com/hjc/backend/vo/MyApplicationStatisticsVO.java`
- `src/main/java/com/hjc/backend/vo/FrontendScoreSummaryVO.java`

### 修改文件

- `src/main/java/com/hjc/backend/service/MaterialApplicationService.java`
- `src/main/java/com/hjc/backend/service/impl/MaterialApplicationServiceImpl.java`
- `src/main/java/com/hjc/backend/service/ScoreSummaryService.java`
- `src/main/java/com/hjc/backend/service/impl/ScoreSummaryServiceImpl.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

### 新增接口

前端查询模块基础路径：`/api/frontend`

- `GET /api/frontend/my-applications/page`：学生端“我的申报”分页查询。
- `GET /api/frontend/my-applications/statistics`：学生端“我的申报”统计概览。
- `GET /api/frontend/applications/{id}/detail`：申报详情 / 审核详情通用查询。
- `GET /api/frontend/audit/pending/page`：审核端“待审核列表”分页查询。
- `GET /api/frontend/my-score`：学生端“我的成绩”查询。
- `GET /api/frontend/my-score/categories`：学生端“我的分类成绩”查询。
- `GET /api/frontend/classes/{classId}/ranking/page`：班级排名分页查询。
- `GET /api/frontend/majors/{majorId}/ranking/page`：专业排名分页查询。

### 新增 Service 查询方法

`MaterialApplicationService`：

- `pageMyApplications(MyMaterialApplicationPageRequest request)`
- `getFrontendApplicationDetail(Long id)`
- `getMyApplicationStatistics(Long studentId)`
- `pagePendingApplications(PendingMaterialApplicationPageRequest request)`

`ScoreSummaryService`：

- `getMyScore(Long studentId)`
- `getMyCategoryScores(Long studentId)`
- `pageClassRankingForFrontend(Long classId, PageRequest request)`
- `pageMajorRankingForFrontend(Long majorId, PageRequest request)`

### 已实现查询能力

- “我的申报”按 `studentId` 分页查询，支持 `status`、`itemId`、`categoryId`、`keyword`、时间范围筛选。
- “我的申报”返回项目名、分类名、附件数量、提交时间、审核时间、驳回原因等前端展示字段。
- “我的申报统计”统计总数以及 `DRAFT`、`SUBMITTED`、`APPROVED`、`REJECTED`、`CANCELLED` 各状态数量。
- “申报详情 / 审核详情”返回材料主信息、学生信息、班级、专业、综测项目、综测分类、附件列表、审核记录列表。
- “待审核列表”默认查询 `SUBMITTED`，支持学生、班级、专业、项目、分类、关键字、提交时间范围筛选。
- “我的成绩”返回总分、班级排名、专业排名，并包含分类成绩列表。
- “我的分类成绩”复用现有分类成绩查询能力。
- 班级排名和专业排名提供分页结果，按排名升序、总分降序、学生 ID 升序排序。

### 已实现校验

- `studentId`、`classId`、`majorId` 对应记录不存在时抛出 `BusinessException`。
- 材料申报详情不存在时抛出 `BusinessException`。
- 材料状态只允许 `DRAFT`、`SUBMITTED`、`APPROVED`、`REJECTED`、`CANCELLED`。
- `categoryId` 查询会校验综测分类存在。
- 查询接口不直接返回 Entity，统一返回前端 VO。
- 列表接口统一返回 `PageResult`，接口外层统一返回 `ApiResponse`。

### 重要边界和 TODO

- 当前未实现登录、JWT、Spring Security，因此“我的”接口暂时通过显式 `studentId` 查询。
- TODO：后续接入认证后，`studentId` 应从当前登录用户上下文获取，不能由前端任意传入。
- 当前未实现审核员数据权限过滤。
- TODO：后续接入权限后，待审核列表应按 `reviewer_scope` 或当前审核员权限过滤。
- 当前不自动重算成绩，成绩查询只读取已有 `score_summary` 和 `score_category_summary`。
- 当前不实现 Excel 导出、真实文件上传、Redis 缓存、消息队列、复杂联表 SQL。

### 当前进度判断

当前项目已完成到第九步：前端友好查询接口。

下一步建议：优先进行前后端联调并按页面微调字段；随后可实现登录、JWT、Spring Security 和权限控制，或实现 Excel 导出。
