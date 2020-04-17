DROP TABLE IF EXISTS `user_scope`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `scope`;

CREATE TABLE IF NOT EXISTS `user` (
    `user_id`         varchar(30)  NOT NULL COMMENT '사용자아이디',
    `password`        varchar(255) NOT NULL COMMENT '비밀번호(ENC)',
    `update_date`     timestamp    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자정보';


CREATE TABLE IF NOT EXISTS `scope` (
    `authority`       varchar(30)  NOT NULL COMMENT '권한',
    `description`     varchar(100) NOT NULL COMMENT '권한 설명',
    `update_date`     timestamp    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`authority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='권한 리스트';


CREATE TABLE IF NOT EXISTS `user_scope` (
    `user_id`         varchar(30)  NOT NULL COMMENT '사용자아이디',
    `authority`       varchar(30)  NOT NULL COMMENT '권한',
    `update_date`     timestamp    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `authority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 권한 맵핑';