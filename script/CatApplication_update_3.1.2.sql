ALTER TABLE `project` ADD COLUMN `dingtalk` varchar(200) DEFAULT NULL COMMENT '钉钉机器人Token';

ALTER TABLE `project` ADD COLUMN `wecom` varchar(200) DEFAULT NULL COMMENT '企业微信机器人Token';
