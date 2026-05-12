# CESD 后端开发日志

## 记录规则

每次完成一个阶段后，请追加记录：

- 日期
- 阶段名称
- 本次目标
- 新增文件
- 修改文件
- 新增接口
- 已实现校验
- 暂未实现内容
- 编译结果
- 下一步计划

## 当前阶段摘要

### 阶段 1：基础后端框架

状态：已完成

内容：

- 完成统一响应对象 `ApiResponse`。
- 完成统一错误码 `ResultCode`。
- 完成业务异常 `BusinessException`。
- 完成全局异常处理 `GlobalExceptionHandler`。
- 完成分页对象 `PageRequest`、`PageResult`。
- 完成 MyBatis-Plus、CORS、OpenAPI 配置。
- 完成健康检查接口和测试接口。
- 完成 `application.yaml` 基础配置。

编译结果：

- 已在历史阶段通过 `mvn clean package`。

### 阶段 2：数据库基础映射

状态：已完成

内容：

- 完成 14 个 Entity。
- 完成 14 个 Mapper。
- 完成 14 个 Service。
- 完成 14 个 ServiceImpl。
- 完成 `@MapperScan("com.hjc.backend.mapper")`。
- 完成 `@TableName`、`@TableId`、`@TableField`、`@TableLogic` 映射。
- `review_record` 未使用逻辑删除。
- 时间字段使用 `LocalDateTime`。
- 分数和金额字段使用 `BigDecimal`。

编译结果：

- 已在历史阶段通过 `mvn clean package`。

### 阶段 3：基础数据模块 CRUD

状态：已完成

内容：

- 完成角色管理 `sys_role` CRUD。
- 完成专业管理 `major_info` CRUD。
- 完成班级管理 `class_info` CRUD。
- 完成系统配置管理 `system_config` CRUD。
- 完成综测分类管理 `evaluation_category` CRUD。
- 完成综测项目管理 `evaluation_item` CRUD。
- 每个模块均包含 Create DTO、Update DTO、VO、Controller、Service 方法、分页、详情、新增、修改、逻辑删除。
- 完成唯一性校验。
- 完成 `class_info.major_id` 与 `evaluation_item.category_id` 逻辑外键校验。

暂未实现内容：

- 删除基础数据前的复杂依赖检查仍保留 TODO。
- 未实现复杂联表展示字段。

编译结果：

- 已在历史阶段通过 `mvn clean package`。

### 阶段 4：用户与学生模块 CRUD

状态：已完成

内容：

- 完成用户管理 `sys_user` CRUD。
- 完成学生管理 `student` CRUD。
- `SysUserVO` 不返回 `passwordHash`。
- 完成用户 `username` 唯一性校验。
- 完成用户 `roleId` 逻辑外键校验。
- 完成学生 `userId`、`studentNo` 唯一性校验。
- 完成学生 `userId`、`majorId`、`classId` 逻辑外键校验。
- 完成学生专业与班级所属专业一致性校验。

暂未实现内容：

- 登录、注册、JWT、Spring Security 未实现。
- 删除用户前关联学生、审核记录检查仍保留 TODO。
- 删除学生前材料申报、成绩统计依赖检查仍保留 TODO。
- `roleName`、`username`、`majorName`、`className` 等联表展示字段暂未实现。

编译结果：

- 已在历史阶段通过 `mvn clean package`。

## 2026-05-11：项目上下文文档整理

阶段名称：项目上下文总结与开发阶段记录整理

本次目标：

- 阅读现有项目文档、配置和代码结构。
- 完善 `docs/CODEX_CONTEXT.md`，作为后续 Codex 长期上下文。
- 创建 `docs/DEVELOPMENT_LOG.md`，记录阶段开发日志规则和当前阶段摘要。

新增文件：

- `docs/DEVELOPMENT_LOG.md`

修改文件：

- `docs/CODEX_CONTEXT.md`

新增接口：

- 无

已实现校验：

- 本次未修改业务代码，未新增业务校验。

暂未实现内容：

- `docs/database.sql` 当前不存在，后续应补充并与 Entity 字段核对。
- `README.md` 存在中文乱码和数据库名示例过期问题，后续可单独整理。

编译结果：

- 本次只修改文档，未执行 `mvn clean package`。

下一步计划：

- 优先实现 `material_application` 与 `material_attachment` 基础 CRUD。
- 暂不做真实文件上传、审核流程和成绩计算。

## 2026-05-11：补充数据库 SQL 文档

阶段名称：数据库建表 SQL 归档

本次目标：

- 将用户提供的综测系统一期 MySQL 建表 SQL 保存到 `docs/database.sql`。
- 更新 Codex 长期上下文中关于 `docs/database.sql` 的状态记录。

新增文件：

- `docs/database.sql`

修改文件：

- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

新增接口：

- 无

已实现校验：

- 本次未修改业务代码，未新增业务校验。

暂未实现内容：

- 未对 Java Entity 与 SQL 做逐字段自动化比对。
- 未执行 SQL，未修改数据库结构。

编译结果：

- 本次只修改文档，未执行 `mvn clean package`。

下一步计划：

- 后续开发新模块前优先读取 `docs/database.sql` 校对字段、索引、逻辑外键和初始化数据。

## 2026-05-11：材料申报与附件元数据基础 CRUD

阶段名称：阶段 5：material_application + material_attachment 基础 CRUD

本次目标：

- 实现材料申报基础 CRUD。
- 实现材料附件元数据基础 CRUD。
- 不实现真实文件上传、审核流程、成绩计算、登录认证和权限控制。

新增文件：

- `src/main/java/com/hjc/backend/dto/CreateMaterialApplicationRequest.java`
- `src/main/java/com/hjc/backend/dto/UpdateMaterialApplicationRequest.java`
- `src/main/java/com/hjc/backend/vo/MaterialApplicationVO.java`
- `src/main/java/com/hjc/backend/controller/MaterialApplicationController.java`
- `src/main/java/com/hjc/backend/dto/CreateMaterialAttachmentRequest.java`
- `src/main/java/com/hjc/backend/dto/UpdateMaterialAttachmentRequest.java`
- `src/main/java/com/hjc/backend/vo/MaterialAttachmentVO.java`
- `src/main/java/com/hjc/backend/controller/MaterialAttachmentController.java`

修改文件：

- `src/main/java/com/hjc/backend/service/MaterialApplicationService.java`
- `src/main/java/com/hjc/backend/service/impl/MaterialApplicationServiceImpl.java`
- `src/main/java/com/hjc/backend/service/MaterialAttachmentService.java`
- `src/main/java/com/hjc/backend/service/impl/MaterialAttachmentServiceImpl.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

新增接口：

- `GET /api/material-applications/page`
- `GET /api/material-applications/{id}`
- `POST /api/material-applications`
- `PUT /api/material-applications/{id}`
- `DELETE /api/material-applications/{id}`
- `GET /api/material-attachments/page`
- `GET /api/material-attachments/{id}`
- `POST /api/material-attachments`
- `PUT /api/material-attachments/{id}`
- `DELETE /api/material-attachments/{id}`

已实现校验：

- `material_application.student_id -> student.id`
- `material_application.item_id -> evaluation_item.id`
- `material_attachment.material_id -> material_application.id`
- 材料状态仅允许 `DRAFT`、`SUBMITTED`、`APPROVED`、`REJECTED`、`CANCELLED`
- 新增材料申报时状态为空默认 `DRAFT`
- 附件 `fileSize` 不允许小于 0
- 详情、修改、删除时记录不存在抛出 `BusinessException`

暂未实现内容：

- 真实文件上传
- `MultipartFile`
- 本地或云存储
- 材料提交、撤回、审核通过、审核驳回流程
- `review_record` 业务接口
- 成绩计算与排名
- 登录、JWT、Spring Security、权限控制
- 复杂联表查询

编译结果：

- 已执行 `mvn clean package`，结果：通过。
- 已启动应用做轻量冒烟验证：
  - Swagger 可访问。
  - `GET /api/material-applications/page` 返回 200。
  - `GET /api/material-attachments/page` 返回 200。
  - 无效材料状态返回统一校验响应。
  - 无效学生、无效材料外键返回业务错误。
  - 负数 `fileSize` 返回统一校验响应。

下一步计划：

- 实现 `review_record` 查询与新增能力。
- 或先实现材料提交、撤回、审核通过、审核驳回状态流转方法。

## 2026-05-11：审核记录 review_record 查询与新增能力

阶段名称：第六步：审核记录 review_record 查询与新增能力

本次目标：
- 实现审核记录分页查询。
- 实现审核记录详情查询。
- 实现新增审核记录。
- 实现根据 `materialId` 查询某条材料的审核历史。
- 只做审核历史查询与新增，不实现材料状态流转。

新增文件：
- `src/main/java/com/hjc/backend/dto/CreateReviewRecordRequest.java`
- `src/main/java/com/hjc/backend/dto/ReviewRecordPageRequest.java`
- `src/main/java/com/hjc/backend/vo/ReviewRecordVO.java`
- `src/main/java/com/hjc/backend/controller/ReviewRecordController.java`

修改文件：
- `src/main/java/com/hjc/backend/service/ReviewRecordService.java`
- `src/main/java/com/hjc/backend/service/impl/ReviewRecordServiceImpl.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

新增接口：
- `GET /api/review-records/page`
- `GET /api/review-records/{id}`
- `POST /api/review-records`
- `GET /api/review-records/material/{materialId}`

已实现校验：
- `review_record.material_id -> material_application.id`
- `review_record.reviewer_id -> sys_user.id`
- `materialId`、`reviewerId`、`beforeStatus`、`afterStatus`、`reviewResult` 必填。
- `beforeStatus`、`afterStatus` 只允许 `DRAFT`、`SUBMITTED`、`APPROVED`、`REJECTED`、`CANCELLED`。
- `reviewResult` 只允许 `APPROVED`、`REJECTED`。
- `reviewScore` 不允许小于 0。
- `reviewComment` 最大长度 500。
- `reviewTime` 为空时由后端设置为当前时间。
- 审核记录不存在、材料申报不存在、审核人用户不存在时抛出 `BusinessException`。

暂未实现内容：
- 不实现材料提交。
- 不实现材料撤回。
- 不实现审核通过和审核驳回状态流转。
- 不修改 `material_application.status`、`final_score`、`reject_reason`、`review_time`。
- 不实现成绩计算、排名、登录认证、JWT、Spring Security、权限控制。
- 不实现复杂联表查询。
- 不提供审核记录修改和删除接口。

编译结果：
- 已执行 `mvn clean package`，结果：通过。

下一步计划：
- 实现材料提交、撤回、审核通过、审核驳回状态流转 Service 方法。
- 状态流转阶段应复用本阶段新增审核记录能力，并统一更新材料状态、最终分数、驳回原因和审核时间。

## 2026-05-11：材料提交、撤回、审核通过、审核驳回状态流转

阶段名称：第七步：材料提交、撤回、审核通过、审核驳回状态流转

本次目标：
- 实现材料提交。
- 实现材料撤回。
- 实现审核通过。
- 实现审核驳回。
- 审核通过和审核驳回时复用 `ReviewRecordService.createReviewRecord` 写入审核记录。
- 不实现成绩计算、排名、登录认证、权限控制和真实文件上传。

新增文件：
- `src/main/java/com/hjc/backend/dto/ApproveMaterialApplicationRequest.java`
- `src/main/java/com/hjc/backend/dto/RejectMaterialApplicationRequest.java`
- `src/main/java/com/hjc/backend/dto/WithdrawMaterialApplicationRequest.java`

修改文件：
- `src/main/java/com/hjc/backend/service/MaterialApplicationService.java`
- `src/main/java/com/hjc/backend/service/impl/MaterialApplicationServiceImpl.java`
- `src/main/java/com/hjc/backend/controller/MaterialApplicationController.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

新增接口：
- `POST /api/material-applications/{id}/submit`
- `POST /api/material-applications/{id}/withdraw`
- `POST /api/material-applications/{id}/approve`
- `POST /api/material-applications/{id}/reject`

已实现校验：
- 材料申报必须存在且未逻辑删除。
- `DRAFT`、`REJECTED`、`CANCELLED` 才允许提交为 `SUBMITTED`。
- 只有 `SUBMITTED` 才允许撤回为 `CANCELLED`。
- 只有 `SUBMITTED` 才允许审核通过为 `APPROVED`。
- 只有 `SUBMITTED` 才允许审核驳回为 `REJECTED`。
- 审核人 `reviewerId` 必须存在且未逻辑删除。
- 审核通过时 `reviewScore` 必填且不能小于 0。
- 驳回原因 `rejectReason` 必填且长度不能超过 500。
- 审核意见 `reviewComment` 长度不能超过 500。
- 审核通过 / 驳回与新增审核记录在同一事务中完成。

暂未实现内容：
- 登录、JWT、Spring Security。
- 当前登录用户识别，`reviewerId` 暂由请求体传入。
- 角色权限控制、数据权限。
- 成绩计算、`score_summary`、`score_category_summary`、排名。
- 真实文件上传、云存储。
- 批量审核、多级审核。
- 复杂联表查询。
- 高并发下的条件更新或乐观锁。

编译结果：
- 已执行 `mvn clean package`，结果：通过。
- 测试结果：`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`。

下一步计划：
- 实现 `score_summary` 与 `score_category_summary` 成绩统计和排名。
- 或先补充待审核列表、我的申报列表、按学生/状态查询等面向页面的查询能力。

## 2026-05-11：成绩统计与排名

阶段名称：第八步：成绩统计与排名

本次目标：
- 实现单个学生成绩重算。
- 实现班级成绩重算与班级排名。
- 实现专业成绩重算与专业排名。
- 实现全量成绩重算。
- 实现学生总成绩查询、学生分类成绩查询、成绩分页查询。
- 不实现登录权限、Excel 导出、缓存、消息队列、复杂规则引擎。

新增文件：
- `src/main/java/com/hjc/backend/dto/ScoreSummaryPageRequest.java`
- `src/main/java/com/hjc/backend/vo/ScoreSummaryVO.java`
- `src/main/java/com/hjc/backend/vo/ScoreCategorySummaryVO.java`
- `src/main/java/com/hjc/backend/controller/ScoreSummaryController.java`

修改文件：
- `src/main/java/com/hjc/backend/service/ScoreSummaryService.java`
- `src/main/java/com/hjc/backend/service/impl/ScoreSummaryServiceImpl.java`
- `src/main/java/com/hjc/backend/service/impl/MaterialApplicationServiceImpl.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

新增接口：
- `POST /api/scores/students/{studentId}/recalculate`
- `POST /api/scores/classes/{classId}/recalculate`
- `POST /api/scores/majors/{majorId}/recalculate`
- `POST /api/scores/recalculate-all`
- `GET /api/scores/students/{studentId}`
- `GET /api/scores/students/{studentId}/categories`
- `GET /api/scores/page`
- `GET /api/scores/classes/{classId}/ranking`
- `GET /api/scores/majors/{majorId}/ranking`

已实现校验：
- `studentId`、`classId`、`majorId` 不能为空。
- 学生、班级、专业不存在或已逻辑删除时抛出 `BusinessException`。
- 综测项目或综测分类不存在时抛出 `BusinessException`。
- APPROVED 材料的 `finalScore` 小于 0 时抛出 `BusinessException`。
- 分类分和总分不允许出现负数。
- 学生成绩尚未计算时查询总成绩会返回业务错误。

已实现计算规则：
- 只统计 `status = APPROVED` 的材料。
- 使用 `finalScore` 作为认定分数。
- `finalScore` 为空时暂不参与计算，并保留 TODO。
- 通过综测项目归属到综测分类。
- 分类分按分类汇总，并按分类 `maxScore` 封顶。
- 更新或新增 `score_category_summary`。
- 本次没有通过材料的旧分类分更新为 0。
- 总分写入或更新 `score_summary`，`status = 1`。

已实现排名规则：
- 班级排名按 `totalScore` 降序。
- 专业排名按 `totalScore` 降序。
- 同分同排名，后续名次使用竞争排名。
- 同分时按 `studentNo`、`studentId` 稳定排序。
- 没有总成绩记录的学生创建 0 分记录后参与排名。

暂未实现内容：
- 审核通过后自动或异步重算成绩。
- Redis 缓存。
- 消息队列。
- 定时任务。
- Excel 导入导出。
- 复杂规则引擎。
- 登录、JWT、Spring Security、角色权限、数据权限。

编译结果：
- 已执行 `mvn clean package`，结果：通过。
- 测试结果：`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`。

下一步计划：
- 实现登录、JWT、Spring Security。
- 或先补充“我的申报”“待审核列表”“成绩查询”等前端友好查询接口。
- 或实现 Excel 导出。

## 2026-05-11：前端友好查询接口

阶段名称：第九步：前端友好查询接口

本次目标：
- 补充学生端“我的申报”分页查询。
- 补充学生端“我的申报”统计概览。
- 补充申报详情 / 审核详情通用查询。
- 补充审核端待审核列表分页查询。
- 补充学生端“我的成绩”和“我的分类成绩”查询。
- 补充班级排名、专业排名分页查询。
- 不实现登录、JWT、Spring Security、权限控制、Excel 导出、真实文件上传、缓存和消息队列。

新增文件：
- `src/main/java/com/hjc/backend/controller/FrontendQueryController.java`
- `src/main/java/com/hjc/backend/dto/MyMaterialApplicationPageRequest.java`
- `src/main/java/com/hjc/backend/dto/PendingMaterialApplicationPageRequest.java`
- `src/main/java/com/hjc/backend/vo/MyMaterialApplicationVO.java`
- `src/main/java/com/hjc/backend/vo/MaterialApplicationDetailVO.java`
- `src/main/java/com/hjc/backend/vo/PendingMaterialApplicationVO.java`
- `src/main/java/com/hjc/backend/vo/MyApplicationStatisticsVO.java`
- `src/main/java/com/hjc/backend/vo/FrontendScoreSummaryVO.java`

修改文件：
- `src/main/java/com/hjc/backend/service/MaterialApplicationService.java`
- `src/main/java/com/hjc/backend/service/impl/MaterialApplicationServiceImpl.java`
- `src/main/java/com/hjc/backend/service/ScoreSummaryService.java`
- `src/main/java/com/hjc/backend/service/impl/ScoreSummaryServiceImpl.java`
- `docs/CODEX_CONTEXT.md`
- `docs/DEVELOPMENT_LOG.md`

新增接口：
- `GET /api/frontend/my-applications/page`
- `GET /api/frontend/my-applications/statistics`
- `GET /api/frontend/applications/{id}/detail`
- `GET /api/frontend/audit/pending/page`
- `GET /api/frontend/my-score`
- `GET /api/frontend/my-score/categories`
- `GET /api/frontend/classes/{classId}/ranking/page`
- `GET /api/frontend/majors/{majorId}/ranking/page`

已实现校验：
- `studentId` 不存在时抛出 `BusinessException`。
- `classId` 不存在时抛出 `BusinessException`。
- `majorId` 不存在时抛出 `BusinessException`。
- 材料申报不存在时抛出 `BusinessException`。
- 材料状态只允许 `DRAFT`、`SUBMITTED`、`APPROVED`、`REJECTED`、`CANCELLED`。
- `categoryId` 查询时校验综测分类存在。
- 成绩尚未计算时，成绩查询抛出业务错误，不在查询接口中自动重算。

暂未实现内容：
- 登录、JWT、Spring Security。
- 当前登录用户上下文，“我的”接口暂时显式传入 `studentId`。
- 审核员数据权限过滤，待审核列表暂未按 `reviewer_scope` 过滤。
- Excel 导出。
- 真实文件上传。
- Redis 缓存。
- 消息队列。
- 自动审核通过后异步重算成绩。

编译结果：
- 已执行 `mvn clean package`，结果：通过。
- 测试结果：`Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`。

下一步计划：
- 优先进行前后端联调并按页面需要微调字段。
- 后续可实现登录、JWT、Spring Security 和权限控制。
- 也可以继续实现 Excel 导出。
