CREATE TABLE `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL COMMENT '用户名',
                        `password` VARCHAR(100) NOT NULL COMMENT '密码',
                        `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
                        `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
                        `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
                        `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1正常，0禁用',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`),
                        UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';