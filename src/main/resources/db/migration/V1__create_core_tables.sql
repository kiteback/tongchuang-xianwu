CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt密码哈希',
    `nickname` VARCHAR(50) NOT NULL COMMENT '昵称',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态：1正常，0禁用',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    CONSTRAINT `chk_user_status` CHECK (`status` IN (0, 1)),
    CONSTRAINT `chk_user_role` CHECK (`role` IN ('USER', 'ADMIN'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '卖家ID',
    `title` VARCHAR(100) NOT NULL COMMENT '商品标题',
    `description` VARCHAR(2000) DEFAULT NULL COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `category` VARCHAR(50) NOT NULL COMMENT '商品分类',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1可售，2锁定，3售出',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_product_user_status` (`user_id`, `status`),
    KEY `idx_product_category_price` (`category`, `price`),
    KEY `idx_product_status_create_time` (`status`, `create_time`),
    CONSTRAINT `fk_product_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `chk_product_price` CHECK (`price` > 0),
    CONSTRAINT `chk_product_status` CHECK (`status` IN (1, 2, 3))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

CREATE TABLE IF NOT EXISTS `orders` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单编号',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '成交价格快照',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1待支付，2已支付，3已完成，4已取消',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orders_order_no` (`order_no`),
    KEY `idx_orders_buyer_create_time` (`buyer_id`, `create_time`),
    KEY `idx_orders_seller_create_time` (`seller_id`, `create_time`),
    KEY `idx_orders_product_status` (`product_id`, `status`),
    CONSTRAINT `fk_orders_buyer` FOREIGN KEY (`buyer_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_orders_seller` FOREIGN KEY (`seller_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_orders_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
    CONSTRAINT `chk_orders_status` CHECK (`status` IN (1, 2, 3, 4))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
