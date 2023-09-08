/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE TABLE `ajax_data`
(
	`id`                int(11)     NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
	`period`            date        NOT NULL COMMENT '时间',
	`minute_order`      smallint(6) NOT NULL COMMENT '分钟',
	`city`              smallint(6) NOT NULL COMMENT '城市',
	`operator`          tinyint(4)  NOT NULL COMMENT '运营商',
	`code`              smallint(6) NOT NULL COMMENT '返回码',
	`network`           tinyint(4)  NOT NULL COMMENT '网络类型',
	`access_number`     bigint(20)  NOT NULL COMMENT '访问量',
	`response_sum_time` bigint(20)  NOT NULL COMMENT '响应时间大小',
	`request_sum_byte`  bigint(20)  NOT NULL COMMENT '发送字节',
	`response_sum_byte` bigint(20)  NOT NULL COMMENT '返回字节',
	`updatetime`        datetime    NOT NULL COMMENT '数据更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `IX_condition` (`period`, `minute_order`, `city`, `operator`, `code`, `network`),
	KEY `updatetime` (`updatetime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='web基本数据';

CREATE TABLE `ajax_data_0` LIKE `ajax_data`;
CREATE TABLE `ajax_data_1` LIKE `ajax_data`;

CREATE TABLE `js_error_log`
(
	`id`            int(11) unsigned NOT NULL AUTO_INCREMENT,
	`module`        varchar(50)      NOT NULL DEFAULT '' COMMENT '出错的js模块',
	`browser`       varchar(50)               DEFAULT NULL COMMENT '浏览器',
	`level`         tinyint(4)       NOT NULL COMMENT '错误级别',
	`msg`           varchar(200)     NOT NULL DEFAULT '' COMMENT '出错的简要信息，用于分类',
	`creation_date` datetime         NOT NULL COMMENT 'log创建时间',
	`error_time`    datetime         NOT NULL COMMENT '错误发生时间',
	`updatetime`    datetime         NOT NULL COMMENT '数据更新时间',
	`dpid`          varchar(200)              DEFAULT NULL COMMENT '用户ID值',
	PRIMARY KEY (`id`),
	KEY `IX_CONDITION` (`error_time`, `module`),
	KEY `updatetime` (`updatetime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `js_error_log_content`
(
	`id`            int(11) unsigned NOT NULL,
	`content`       longblob COMMENT '出错的详细信息',
	`creation_date` datetime         NOT NULL COMMENT 'log创建时间',
	`updatetime`    datetime         NOT NULL COMMENT '数据更新时间',
	PRIMARY KEY (`id`),
	KEY `updatetime` (`updatetime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `web_speed_data`
(
	`id`                  int(11)     NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
	`period`              date        NOT NULL COMMENT '时间',
	`minute_order`        smallint(6) NOT NULL COMMENT '分钟',
	`city`                smallint(6) NOT NULL COMMENT '城市',
	`platform`            smallint(6) NOT NULL COMMENT '平台',
	`operator`            smallint(6) NOT NULL COMMENT '运营商',
	`network`             smallint(6) NOT NULL COMMENT '网络类型',
	`source`              smallint(6) NOT NULL COMMENT '来源',
	`access_number1`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number2`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number3`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number4`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number5`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number6`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number7`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number8`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number9`      bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number10`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number11`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number12`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number13`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number14`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number15`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number16`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number17`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number18`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number19`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number20`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number21`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number22`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number23`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number24`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number25`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number26`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number27`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number28`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number29`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number30`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number31`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`access_number32`     bigint(20) DEFAULT '0' COMMENT '访问量',
	`response_sum_time1`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time2`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time3`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time4`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time5`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time6`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time7`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time8`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time9`  bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time10` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time11` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time12` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time13` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time14` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time15` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time16` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time17` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time18` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time19` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time20` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time21` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time22` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time23` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time24` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time25` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time26` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time27` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time28` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time29` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time30` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time31` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`response_sum_time32` bigint(20) DEFAULT '0' COMMENT '响应时间大小',
	`status`              smallint(6) NOT NULL COMMENT '数据状态',
	`updatetime`          datetime    NOT NULL COMMENT '数据更新时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `IX_condition` (`period`, `minute_order`, `city`, `operator`, `network`, `platform`, `source`),
	KEY `updatetime` (`updatetime`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 227
  DEFAULT CHARSET = utf8 COMMENT ='web测速数据';

CREATE TABLE `web_speed_data_0` LIKE `web_speed_data`;
CREATE TABLE `web_speed_data_1` LIKE `web_speed_data`;

CREATE TABLE `app_command_data`
(
	`id`                int(11)  NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
	`period`            date     NOT NULL COMMENT '时间',
	`minute_order`      smallint NOT NULL COMMENT '分钟',
	`city`              smallint NOT NULL COMMENT '城市',
	`operator`          tinyint  NOT NULL COMMENT '运营商',
	`network`           tinyint  NOT NULL COMMENT '网络类型',
	`app_version`       int      NOT NULL COMMENT '版本',
	`connect_type`      tinyint  NOT NULL COMMENT '访问类型，是否长连接',
	`code`              smallint NOT NULL COMMENT '返回码',
	`platform`          tinyint  NOT NULL COMMENT '平台',
	`access_number`     bigint   NOT NULL COMMENT '访问量',
	`response_sum_time` bigint   NOT NULL COMMENT '响应时间大小',
	`request_package`   bigint   NOT NULL COMMENT '请求包大小',
	`response_package`  bigint   NOT NULL COMMENT '响应包大小',
	`status`            smallint NOT NULL COMMENT '数据状态',
	`creation_date`     datetime NOT NULL COMMENT '数据插入时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `IX_condition` (`period`, `minute_order`, `city`, `operator`, `network`, `app_version`, `connect_type`,
							   `code`, `platform`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='app基本数据';

CREATE TABLE `app_command_data_0` LIKE `app_command_data`;
CREATE TABLE `app_command_data_1` LIKE `app_command_data`;
CREATE TABLE `app_command_data_2` LIKE `app_command_data`;
CREATE TABLE `app_command_data_3` LIKE `app_command_data`;
CREATE TABLE `app_command_data_4` LIKE `app_command_data`;

CREATE TABLE `app_speed_data`
(
	`id`                     int(11)  NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
	`period`                 date     NOT NULL COMMENT '时间',
	`minute_order`           smallint NOT NULL COMMENT '分钟',
	`city`                   smallint NOT NULL COMMENT '城市',
	`operator`               tinyint  NOT NULL COMMENT '运营商',
	`network`                tinyint  NOT NULL COMMENT '网络类型',
	`app_version`            int      NOT NULL COMMENT '版本',
	`platform`               tinyint  NOT NULL COMMENT '平台',
	`access_number`          bigint   NOT NULL COMMENT '访问量',
	`slow_access_number`     bigint   NOT NULL COMMENT '慢用户访问量',
	`response_sum_time`      bigint   NOT NULL COMMENT '响应时间大小',
	`slow_response_sum_time` bigint   NOT NULL COMMENT '慢用户响应时间大小',
	`status`                 smallint NOT NULL COMMENT '数据状态',
	`creation_date`          datetime NOT NULL COMMENT '数据插入时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `IX_condition` (period, minute_order, city, operator, network, app_version, platform)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='app测速数据';

CREATE TABLE `app_speed_data_0` LIKE `app_speed_data`;
CREATE TABLE `app_speed_data_1` LIKE `app_speed_data`;
CREATE TABLE `app_speed_data_2` LIKE `app_speed_data`;
CREATE TABLE `app_speed_data_3` LIKE `app_speed_data`;
CREATE TABLE `app_speed_data_4` LIKE `app_speed_data`;

CREATE TABLE `crash_log`
(
	`id`               int(11) unsigned NOT NULL AUTO_INCREMENT,
	`app_name`         varchar(100)     NOT NULL DEFAULT '' COMMENT 'app名称',
	`platform`         tinyint(4)       NOT NULL COMMENT '平台类型，1 for android, 2 for ios',
	`app_version`      varchar(50)      NOT NULL DEFAULT '' COMMENT 'app版本',
	`platform_version` varchar(50)      NOT NULL DEFAULT '' COMMENT '平台版本',
	`module`           varchar(50)      NOT NULL DEFAULT '' COMMENT 'crash模块',
	`level`            tinyint(4)       NOT NULL COMMENT '错误级别',
	`msg`              varchar(500)              DEFAULT NULL,
	`device_brand`     varchar(20)               DEFAULT NULL COMMENT '手机品牌',
	`device_model`     varchar(50)               DEFAULT NULL,
	`updatetime`       datetime         NOT NULL COMMENT '数据更新时间',
	`crash_time`       datetime         NOT NULL COMMENT 'crash时间',
	`dpid`             varchar(200)              DEFAULT NULL COMMENT 'dpid',
	`map_id`           varchar(200)              DEFAULT NULL COMMENT '混淆mapid',
	`tag`              tinyint(4)                DEFAULT NULL COMMENT 'tag',
	PRIMARY KEY (`id`),
	KEY `IX_CONDITION` (`crash_time`, `app_name`),
	KEY `updatetime` (`updatetime`),
	KEY `IX_tag_platform` (`tag`, `platform`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `crash_log_content`
(
	`id`             int(11) unsigned NOT NULL,
	`content`        longblob COMMENT 'crash详细log',
	`updatetime`     datetime         NOT NULL COMMENT '数据更新时间',
	`content_mapped` longblob COMMENT '反混淆之后的详细log',
	PRIMARY KEY (`id`),
	KEY `updatetime` (`updatetime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `map_file`
(
	`map_id`     varchar(100) NOT NULL,
	`content`    longblob COMMENT 'mapping文件内容',
	`updatetime` datetime     NOT NULL COMMENT '数据更新时间',
	PRIMARY KEY (`map_id`),
	KEY `updatetime` (`updatetime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `app_daily_report`
(
	`id`            int(11)     NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
	`name`          varchar(20) NOT NULL COMMENT '报表名称, app访问统计, crash统计...',
	`ip`            varchar(50) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
	`app_id`        int(11)     NOT NULL COMMENT '报表数据来自于哪个APP',
	`period`        datetime    NOT NULL COMMENT '报表时间段',
	`type`          tinyint(4)  NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
	`creation_date` datetime    NOT NULL COMMENT '报表创建时间',
	PRIMARY KEY (`id`),
	UNIQUE KEY `period` (`period`, `app_id`, `name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = COMPRESSED COMMENT ='天报表';

CREATE TABLE `app_daily_report_content`
(
	`report_id`     int(11)  NOT NULL COMMENT '报表ID',
	`content`       longblob NOT NULL COMMENT '二进制报表内容',
	`creation_date` datetime NOT NULL COMMENT '创建时间',
	PRIMARY KEY (`report_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  ROW_FORMAT = COMPRESSED COMMENT ='天报表二进制内容';

CREATE TABLE `symbolize`
(
	`id`         int(11)     NOT NULL AUTO_INCREMENT,
	`app_id`     int(11)     NOT NULL DEFAULT -1,
	`uuid1`      varchar(64) NOT NULL DEFAULT '',
	`uuid2`      varchar(64) NOT NULL DEFAULT '',
	`tag`        int(4)      NOT NULL DEFAULT 0,
	`path`       varchar(64) NOT NULL DEFAULT '',
	`updatetime` datetime    NOT NULL,
	PRIMARY KEY (`id`),
	KEY `updatetime` (`updatetime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `module_map_file`
(
	`id`         int(11)    NOT NULL AUTO_INCREMENT,
	`app_id`     int(11)    NOT NULL,
	`platform`   tinyint(4) NOT NULL COMMENT '平台类型，1 for android, 2 for ios',
	`content`    longblob COMMENT '模块映射文件内容',
	`updatetime` datetime   NOT NULL COMMENT '数据更新时间',
	PRIMARY KEY (`id`),
	KEY `updatetime` (`updatetime`),
	UNIQUE KEY `app_id_platform` (`app_id`, `platform`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `app_log`
(
	`id`               int(11) unsigned NOT NULL AUTO_INCREMENT,
	`app_id`           int(11)          NOT NULL COMMENT 'app名称',
	`platform`         tinyint(4)       NOT NULL COMMENT '平台类型，1 for android, 2 for ios',
	`app_version`      varchar(50)      NOT NULL DEFAULT '' COMMENT 'app版本',
	`platform_version` varchar(50)      NOT NULL DEFAULT '' COMMENT '平台版本',
	`level`            tinyint(4)       NOT NULL COMMENT '日志级别',
	`category`         varchar(500)     NOT NULL DEFAULT '' COMMENT '用于分类log',
	`device_brand`     varchar(20)               DEFAULT NULL COMMENT '手机品牌',
	`device_model`     varchar(50)               DEFAULT NULL COMMENT '手机型号',
	`unionId`          varchar(200)              DEFAULT NULL COMMENT 'unionId',
	`updatetime`       datetime         NOT NULL COMMENT '数据更新时间',
	`log_time`         datetime         NOT NULL COMMENT 'log时间',
	`tag`              tinyint(4)                DEFAULT NULL COMMENT 'tag',
	PRIMARY KEY (`id`),
	KEY `IX_CONDITION` (`log_time`, `app_id`),
	KEY `updatetime` (`updatetime`),
	KEY `IX_tag_platform` (`tag`, `platform`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `app_log_content`
(
	`id`         int(11) unsigned NOT NULL AUTO_INCREMENT,
	`content`    longblob COMMENT '详细log',
	`updatetime` datetime         NOT NULL COMMENT '数据更新时间',
	PRIMARY KEY (`id`),
	KEY `updatetime` (`updatetime`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `app_alarm_rule`
(
	`id`            int          NOT NULL AUTO_INCREMENT,
	`name`          varchar(100) NOT NULL DEFAULT '' COMMENT '告警名称',
	`app`           varchar(64)  NOT NULL DEFAULT '' COMMENT '数据聚合方式',
	`content`       longtext     NOT NULL COMMENT '配置的具体内容',
	`creation_date` datetime     NOT NULL COMMENT '配置创建时间',
	`creator`       varchar(100)          DEFAULT NULL COMMENT '配置创建人',
	`updatetime`    datetime     NOT NULL COMMENT '配置修改时间',
	PRIMARY KEY (`id`),
	KEY `updatetime` (`updatetime`),
	KEY `app` (`app`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8 COMMENT ='App告警的配置';
