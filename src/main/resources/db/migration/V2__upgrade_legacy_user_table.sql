SET @role_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'user'
      AND COLUMN_NAME = 'role'
);

SET @add_role_sql = IF(
    @role_column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT ''USER'' COMMENT ''角色：USER/ADMIN'' AFTER `status`',
    'SELECT 1'
);

PREPARE add_role_statement FROM @add_role_sql;
EXECUTE add_role_statement;
DEALLOCATE PREPARE add_role_statement;
