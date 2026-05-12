-- =====================================================
-- 综测系统一期数据库建表 SQL
-- 数据库类型：MySQL
-- 字符集：utf8mb4
-- 说明：
-- 1. 本 SQL 不建立物理外键约束
-- 2. 通过逻辑外键字段 + 索引 + 后端业务校验保证关系
-- 3. deleted = 0 表示未删除，deleted = 1 表示已删除
-- =====================================================

CREATE DATABASE IF NOT EXISTS comprehensive_evaluation
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE comprehensive_evaluation;

-- =====================================================
-- 1. 角色表 sys_role
-- =====================================================
CREATE TABLE IF NOT EXISTS sys_role (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
                                        role_name VARCHAR(50) NOT NULL COMMENT '角色名称，如学生、审核员、管理员',
                                        role_code VARCHAR(50) NOT NULL COMMENT '角色编码，如 STUDENT、REVIEWER、ADMIN',
                                        description VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
                                        status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1启用，0禁用',
                                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                        UNIQUE KEY uk_role_code (role_code),
                                        KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';


-- =====================================================
-- 2. 用户表 sys_user
-- =====================================================
CREATE TABLE IF NOT EXISTS sys_user (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                                        username VARCHAR(50) NOT NULL COMMENT '登录用户名，可用学号或工号',
                                        password_hash VARCHAR(255) NOT NULL COMMENT '加密后的密码',
                                        real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
                                        role_id BIGINT NOT NULL COMMENT '角色ID，逻辑关联 sys_role.id',
                                        phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                                        email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                                        avatar VARCHAR(500) DEFAULT NULL COMMENT '头像地址',
                                        status TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态，1正常，0禁用',
                                        last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
                                        create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                        update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                        deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                        UNIQUE KEY uk_username (username),
                                        KEY idx_role_id (role_id),
                                        KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';


-- =====================================================
-- 3. 专业表 major_info
-- =====================================================
CREATE TABLE IF NOT EXISTS major_info (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '专业ID',
                                          major_name VARCHAR(100) NOT NULL COMMENT '专业名称',
                                          major_code VARCHAR(50) DEFAULT NULL COMMENT '专业编码',
                                          college_name VARCHAR(100) NOT NULL COMMENT '所属学院名称',
                                          description VARCHAR(255) DEFAULT NULL COMMENT '专业说明',
                                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1启用，0停用',
                                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                          UNIQUE KEY uk_major_code (major_code),
                                          KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';


-- =====================================================
-- 4. 班级表 class_info
-- =====================================================
CREATE TABLE IF NOT EXISTS class_info (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '班级ID',
                                          class_name VARCHAR(100) NOT NULL COMMENT '班级名称，如软件2301班',
                                          class_code VARCHAR(50) DEFAULT NULL COMMENT '班级编码',
                                          major_id BIGINT NOT NULL COMMENT '所属专业ID，逻辑关联 major_info.id',
                                          grade VARCHAR(20) NOT NULL COMMENT '年级，如2023级',
                                          counselor_name VARCHAR(50) DEFAULT NULL COMMENT '辅导员姓名',
                                          status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1启用，0停用',
                                          create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                          UNIQUE KEY uk_class_code (class_code),
                                          KEY idx_major_id (major_id),
                                          KEY idx_grade (grade),
                                          KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';


-- =====================================================
-- 5. 学生表 student
-- =====================================================
CREATE TABLE IF NOT EXISTS student (
                                       id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '学生ID',
                                       user_id BIGINT NOT NULL COMMENT '绑定的用户ID，逻辑关联 sys_user.id',
                                       student_no VARCHAR(30) NOT NULL COMMENT '学号',
                                       name VARCHAR(50) NOT NULL COMMENT '学生姓名',
                                       gender CHAR(1) DEFAULT NULL COMMENT '性别，M男，F女',
                                       grade VARCHAR(20) NOT NULL COMMENT '年级，如2023级',
                                       major_id BIGINT NOT NULL COMMENT '专业ID，逻辑关联 major_info.id',
                                       class_id BIGINT NOT NULL COMMENT '班级ID，逻辑关联 class_info.id',
                                       phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                                       email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
                                       status TINYINT NOT NULL DEFAULT 1 COMMENT '学生状态，1正常，0停用',
                                       create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                       UNIQUE KEY uk_user_id (user_id),
                                       UNIQUE KEY uk_student_no (student_no),
                                       KEY idx_major_id (major_id),
                                       KEY idx_class_id (class_id),
                                       KEY idx_grade (grade),
                                       KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';


-- =====================================================
-- 6. 综测分类表 evaluation_category
-- =====================================================
CREATE TABLE IF NOT EXISTS evaluation_category (
                                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
                                                   category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
                                                   category_code VARCHAR(50) DEFAULT NULL COMMENT '分类编码',
                                                   max_score DECIMAL(8,2) DEFAULT NULL COMMENT '分类最高分',
                                                   sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
                                                   description VARCHAR(255) DEFAULT NULL COMMENT '分类说明',
                                                   status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1启用，0停用',
                                                   create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                   update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                   deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                                   UNIQUE KEY uk_category_code (category_code),
                                                   KEY idx_sort_no (sort_no),
                                                   KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测分类表';


-- =====================================================
-- 7. 综测项目表 evaluation_item
-- =====================================================
CREATE TABLE IF NOT EXISTS evaluation_item (
                                               id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '项目ID',
                                               category_id BIGINT NOT NULL COMMENT '所属分类ID，逻辑关联 evaluation_category.id',
                                               item_name VARCHAR(100) NOT NULL COMMENT '项目名称',
                                               item_code VARCHAR(50) DEFAULT NULL COMMENT '项目编码',
                                               score_type VARCHAR(30) NOT NULL DEFAULT 'FIXED' COMMENT '计分方式：FIXED固定分，MANUAL手动分，RANGE范围分',
                                               score DECIMAL(8,2) DEFAULT NULL COMMENT '默认分值',
                                               max_score DECIMAL(8,2) DEFAULT NULL COMMENT '该项目累计最高分',
                                               need_attachment TINYINT NOT NULL DEFAULT 1 COMMENT '是否需要附件，1是，0否',
                                               description VARCHAR(500) DEFAULT NULL COMMENT '项目说明',
                                               sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
                                               status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1启用，0停用',
                                               create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                               update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                               deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                               UNIQUE KEY uk_item_code (item_code),
                                               KEY idx_category_id (category_id),
                                               KEY idx_status (status),
                                               KEY idx_sort_no (sort_no),
                                               KEY idx_score_type (score_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='综测项目表';


-- =====================================================
-- 8. 材料申报表 material_application
-- =====================================================
CREATE TABLE IF NOT EXISTS material_application (
                                                    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '材料ID',
                                                    student_id BIGINT NOT NULL COMMENT '提交学生ID，逻辑关联 student.id',
                                                    item_id BIGINT NOT NULL COMMENT '申报项目ID，逻辑关联 evaluation_item.id',
                                                    title VARCHAR(200) NOT NULL COMMENT '材料标题',
                                                    description TEXT DEFAULT NULL COMMENT '材料说明',
                                                    apply_score DECIMAL(8,2) DEFAULT NULL COMMENT '学生申请分数',
                                                    final_score DECIMAL(8,2) DEFAULT NULL COMMENT '审核后最终认定分数',
                                                    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '材料状态：DRAFT草稿，SUBMITTED已提交，APPROVED已通过，REJECTED已驳回，CANCELLED已撤回',
                                                    reject_reason VARCHAR(500) DEFAULT NULL COMMENT '最新驳回原因',
                                                    submit_count INT NOT NULL DEFAULT 0 COMMENT '提交次数',
                                                    submit_time DATETIME DEFAULT NULL COMMENT '最近一次提交时间',
                                                    review_time DATETIME DEFAULT NULL COMMENT '最近一次审核时间',
                                                    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                    update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                    deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                                    KEY idx_student_id (student_id),
                                                    KEY idx_item_id (item_id),
                                                    KEY idx_status (status),
                                                    KEY idx_submit_time (submit_time),
                                                    KEY idx_review_time (review_time),
                                                    KEY idx_student_status (student_id, status),
                                                    KEY idx_status_submit_time (status, submit_time),
                                                    KEY idx_item_status (item_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材料申报表';


-- =====================================================
-- 9. 材料附件表 material_attachment
-- =====================================================
CREATE TABLE IF NOT EXISTS material_attachment (
                                                   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '附件ID',
                                                   material_id BIGINT NOT NULL COMMENT '所属材料ID，逻辑关联 material_application.id',
                                                   original_name VARCHAR(255) NOT NULL COMMENT '用户上传时的原始文件名',
                                                   stored_name VARCHAR(255) NOT NULL COMMENT '服务端保存的文件名',
                                                   file_path VARCHAR(500) NOT NULL COMMENT '文件存储路径',
                                                   file_url VARCHAR(500) DEFAULT NULL COMMENT '文件访问地址',
                                                   file_type VARCHAR(50) DEFAULT NULL COMMENT '文件类型，如jpg、png、pdf',
                                                   file_size BIGINT DEFAULT NULL COMMENT '文件大小，单位字节',
                                                   upload_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                                                   create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                   update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                   deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                                   KEY idx_material_id (material_id),
                                                   KEY idx_file_type (file_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='材料附件表';


-- =====================================================
-- 10. 审核记录表 review_record
-- =====================================================
CREATE TABLE IF NOT EXISTS review_record (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核记录ID',
                                             material_id BIGINT NOT NULL COMMENT '被审核材料ID，逻辑关联 material_application.id',
                                             reviewer_id BIGINT NOT NULL COMMENT '审核人用户ID，逻辑关联 sys_user.id',
                                             before_status VARCHAR(30) NOT NULL COMMENT '审核前状态',
                                             after_status VARCHAR(30) NOT NULL COMMENT '审核后状态',
                                             review_result VARCHAR(30) NOT NULL COMMENT '审核结果：APPROVED通过，REJECTED驳回',
                                             review_score DECIMAL(8,2) DEFAULT NULL COMMENT '本次审核认定分数',
                                             review_comment VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
                                             review_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
                                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

                                             KEY idx_material_id (material_id),
                                             KEY idx_reviewer_id (reviewer_id),
                                             KEY idx_review_time (review_time),
                                             KEY idx_review_result (review_result),
                                             KEY idx_material_review_time (material_id, review_time),
                                             KEY idx_reviewer_review_time (reviewer_id, review_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核记录表';


-- =====================================================
-- 11. 总成绩统计表 score_summary
-- =====================================================
CREATE TABLE IF NOT EXISTS score_summary (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '成绩统计ID',
                                             student_id BIGINT NOT NULL COMMENT '学生ID，逻辑关联 student.id',
                                             total_score DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '总分',
                                             class_rank INT DEFAULT NULL COMMENT '班级排名',
                                             major_rank INT DEFAULT NULL COMMENT '专业排名',
                                             calculate_time DATETIME DEFAULT NULL COMMENT '成绩计算时间',
                                             status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1有效，0无效',
                                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                             UNIQUE KEY uk_student_id (student_id),
                                             KEY idx_total_score (total_score),
                                             KEY idx_class_rank (class_rank),
                                             KEY idx_major_rank (major_rank),
                                             KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='总成绩统计表';


-- =====================================================
-- 12. 分类成绩统计表 score_category_summary
-- =====================================================
CREATE TABLE IF NOT EXISTS score_category_summary (
                                                      id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类成绩ID',
                                                      student_id BIGINT NOT NULL COMMENT '学生ID，逻辑关联 student.id',
                                                      category_id BIGINT NOT NULL COMMENT '分类ID，逻辑关联 evaluation_category.id',
                                                      category_score DECIMAL(8,2) NOT NULL DEFAULT 0.00 COMMENT '该分类得分',
                                                      calculate_time DATETIME DEFAULT NULL COMMENT '计算时间',
                                                      create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                                      update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                                      deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                                      UNIQUE KEY uk_student_category (student_id, category_id),
                                                      KEY idx_student_id (student_id),
                                                      KEY idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类成绩统计表';


-- =====================================================
-- 13. 系统配置表 system_config
-- =====================================================
CREATE TABLE IF NOT EXISTS system_config (
                                             id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
                                             config_key VARCHAR(100) NOT NULL COMMENT '配置键',
                                             config_value VARCHAR(500) NOT NULL COMMENT '配置值',
                                             description VARCHAR(255) DEFAULT NULL COMMENT '配置说明',
                                             status TINYINT NOT NULL DEFAULT 1 COMMENT '状态，1启用，0禁用',
                                             create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                             update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                             deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                             UNIQUE KEY uk_config_key (config_key),
                                             KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';


-- =====================================================
-- 14. 审核范围表 reviewer_scope
-- 如果一期不限制审核员范围，可以先建表但暂不使用
-- =====================================================
CREATE TABLE IF NOT EXISTS reviewer_scope (
                                              id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审核范围ID',
                                              reviewer_id BIGINT NOT NULL COMMENT '审核员用户ID，逻辑关联 sys_user.id',
                                              major_id BIGINT DEFAULT NULL COMMENT '专业ID，逻辑关联 major_info.id',
                                              class_id BIGINT DEFAULT NULL COMMENT '班级ID，逻辑关联 class_info.id',
                                              grade VARCHAR(20) DEFAULT NULL COMMENT '年级',
                                              create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                              update_time DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                              deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除，0未删除，1已删除',

                                              KEY idx_reviewer_id (reviewer_id),
                                              KEY idx_major_id (major_id),
                                              KEY idx_class_id (class_id),
                                              KEY idx_grade (grade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审核范围表';

-- =====================================================
-- 初始化角色数据
-- =====================================================
INSERT INTO sys_role (role_name, role_code, description, status)
VALUES
    ('学生', 'STUDENT', '学生角色，可以提交材料、查看成绩', 1),
    ('审核员', 'REVIEWER', '审核员角色，可以审核学生提交的材料', 1),
    ('管理员', 'ADMIN', '管理员角色，可以管理基础数据、查看成绩、导出数据', 1);


-- =====================================================
-- 初始化系统配置数据
-- =====================================================
INSERT INTO system_config (config_key, config_value, description, status)
VALUES
    ('apply_enabled', 'true', '是否开放学生申报', 1),
    ('review_enabled', 'true', '是否开放审核', 1),
    ('upload_max_size', '10485760', '最大上传文件大小，单位字节，默认10MB', 1),
    ('upload_file_types', 'jpg,jpeg,png,pdf,doc,docx', '允许上传的文件类型', 1);
