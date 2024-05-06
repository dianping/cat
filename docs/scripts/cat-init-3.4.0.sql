CREATE TABLE `dailyreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(50) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `period` (`period`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='天报表';

CREATE TABLE `weeklyreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(50) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `period` (`period`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='周报表';

CREATE TABLE `monthreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(50) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `period` (`period`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='月报表';

CREATE TABLE `hostinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) NOT NULL COMMENT '部署机器IP',
  `domain` varchar(200) NOT NULL COMMENT '部署机器对应的项目名',
  `hostname` varchar(200) DEFAULT NULL COMMENT '机器域名',
  `creation_date` datetime NOT NULL,
  `last_modified_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip_index` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='IP和项目名的对应关系';

CREATE TABLE `hourlyreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` tinyint(4) NOT NULL COMMENT '报表类型, 1/xml, 9/binary 默认1',
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(50) DEFAULT NULL COMMENT '报表来自于哪台机器',
  `domain` varchar(50) NOT NULL  COMMENT '报表项目',
  `period` datetime NOT NULL COMMENT '报表时间段',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  KEY `IX_Domain_Name_Period` (`domain`,`name`,`period`),
  KEY `IX_Name_Period` (`name`,`period`),
  KEY `IX_Period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='用于存放实时报表信息，处理之后的结果';

CREATE TABLE `hourly_report_content` (
  `report_id` int(11) NOT NULL COMMENT '报表ID',
  `content` longblob NOT NULL COMMENT '二进制报表内容',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`report_id`),
  KEY `IX_Period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='小时报表二进制内容';

CREATE TABLE `daily_report_content` (
  `report_id` int(11) NOT NULL COMMENT '报表ID',
  `content` longblob NOT NULL COMMENT '二进制报表内容',
  `period` datetime  COMMENT '报表时间段',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`report_id`),
  KEY `IX_Period` (`period`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='天报表二进制内容';

CREATE TABLE `weekly_report_content` (
  `report_id` int(11) NOT NULL COMMENT '报表ID',
  `content` longblob NOT NULL COMMENT '二进制报表内容',
  `period` datetime  COMMENT '报表时间段',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`report_id`),
  KEY `IX_Period` (`period`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='周报表二进制内容';

CREATE TABLE `monthly_report_content` (
  `report_id` int(11) NOT NULL COMMENT '报表ID',
  `content` longblob NOT NULL COMMENT '二进制报表内容',
  `period` datetime  COMMENT '报表时间段',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`report_id`),
  KEY `IX_Period` (`period`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='月报表二进制内容';

CREATE TABLE `businessReport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` tinyint(4) NOT NULL COMMENT '报表类型 报表数据格式, 1/Binary, 2/xml , 3/json',
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(50) NOT NULL COMMENT '报表来自于哪台机器',
  `productLine` varchar(50) NOT NULL COMMENT '指标来源于哪个产品组',
  `period` datetime NOT NULL COMMENT '报表时间段',
  `content` longblob COMMENT '用于存放报表的具体内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  KEY `IX_Period_productLine_name` (`period`,`productLine`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='用于存放业务监控实时报表信息，处理之后的结果';

CREATE TABLE `task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producer`      varchar(20) NOT NULL COMMENT '任务创建者ip',
  `consumer`      varchar(20) NULL COMMENT '任务执行者ip',
  `failure_count` tinyint(4) NOT NULL COMMENT '任务失败次数',
  `report_name`   varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `report_domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `report_period` datetime NOT NULL  COMMENT '报表时间',
  `status`        tinyint(4) NOT NULL COMMENT '执行状态: 1/todo, 2/doing, 3/done 4/failed',
  `task_type`     tinyint(4) NOT NULL DEFAULT '1' COMMENT '0表示小时任务，1表示天任务',
  `creation_date` datetime NOT NULL  COMMENT '任务创建时间',
  `start_date`    datetime NULL  COMMENT '开始时间, 这次执行开始时间',
  `end_date`      datetime NULL  COMMENT '结束时间, 这次执行结束时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_period_domain_name_type` (`report_period`,`report_domain`,`report_name`,`task_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='后台任务';

CREATE TABLE `project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(200) NOT NULL COMMENT '项目名称',
  `cmdb_domain` varchar(200) DEFAULT  NULL COMMENT 'cmdb项目名称',
  `level` int(5) DEFAULT NULL COMMENT '项目级别',
  `bu` varchar(50) DEFAULT NULL COMMENT 'CMDB事业部',
  `cmdb_productline` varchar(50) DEFAULT NULL COMMENT 'CMDB产品线',
  `owner` varchar(50)  DEFAULT NULL COMMENT '项目负责人',
  `email` longtext  DEFAULT NULL COMMENT '项目组邮件',
  `phone` longtext  DEFAULT NULL COMMENT '联系电话',
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `domain` (`domain`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目基本信息';

CREATE TABLE `topologyGraph` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) NOT NULL COMMENT '报表来自于哪台cat-client机器ip',
  `period` datetime NOT NULL  COMMENT '报表时间段,精确到分钟',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 3/binary',
  `content` longblob COMMENT '用于存放报表的具体内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  KEY `period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于存储历史的拓扑图曲线';

CREATE TABLE `config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '配置名称',
  `content` longtext COMMENT '配置的具体内容',
  `creation_date` datetime NOT NULL COMMENT '配置创建时间',
  `modify_date` datetime NOT NULL COMMENT '配置修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于存储系统的全局配置信息';

CREATE TABLE `baseline` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `report_name` varchar(100) DEFAULT NULL,
  `index_key` varchar(100) DEFAULT NULL,
  `report_period` datetime DEFAULT NULL,
  `data` blob,
  `creation_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `period_name_key` (`report_period`,`report_name`,`index_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `alteration` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `type` varchar(64) NOT NULL COMMENT '分类',
  `title` varchar(128) NOT NULL COMMENT '变更标题',
  `domain` varchar(128) NOT NULL COMMENT '变更项目',
  `hostname` varchar(128) NOT NULL COMMENT '变更机器名',
  `ip` varchar(128) DEFAULT NULL COMMENT '变更机器IP',
  `date` datetime NOT NULL COMMENT '变更时间',
  `user` varchar(45) NOT NULL COMMENT '变更用户',
  `alt_group` varchar(45) DEFAULT NULL COMMENT '变更组别',
  `content` longtext NOT NULL COMMENT '变更内容',
  `url` varchar(200) DEFAULT NULL COMMENT '变更链接',
  `status` tinyint(4) DEFAULT '0' COMMENT '变更状态',
  `creation_date` datetime NOT NULL COMMENT '数据库创建时间',
  PRIMARY KEY (`id`),
  KEY `ind_date_domain_host` (`date`,`domain`,`hostname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='变更表';

CREATE TABLE `alert` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `domain` varchar(128) NOT NULL COMMENT '告警项目',
  `alert_time` datetime NOT NULL COMMENT '告警时间',
  `category` varchar(64) NOT NULL COMMENT '告警分类:network/business/system/exception -alert',
  `type` varchar(64) NOT NULL COMMENT '告警类型:error/warning',
  `content` longtext NOT NULL COMMENT '告警内容',
  `metric` varchar(128) NOT NULL COMMENT '告警指标',
  `creation_date` datetime NOT NULL COMMENT '数据插入时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='存储告警信息';

CREATE TABLE `alert_summary` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `domain` varchar(128) NOT NULL COMMENT '告警项目',
  `alert_time` datetime NOT NULL COMMENT '告警时间',
  `content` longtext NOT NULL COMMENT '统一告警内容',
  `creation_date` datetime NOT NULL COMMENT '数据插入时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='统一告警信息';

CREATE TABLE `operation` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `user` varchar(128) NOT NULL COMMENT '用户名',
  `module` varchar(128) NOT NULL COMMENT '模块',
  `operation` varchar(128) NOT NULL COMMENT '操作',
  `time` datetime NOT NULL COMMENT '修改时间',
  `content` longtext NOT NULL COMMENT '修改内容',
  `creation_date` datetime NOT NULL COMMENT '数据插入时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户操作日志';

CREATE TABLE `overload` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `report_id` int(11) NOT NULL COMMENT '报告id',
  `report_type` tinyint(4) NOT NULL COMMENT '报告类型 1:hourly 2:daily 3:weekly 4:monthly',
  `report_size` double NOT NULL COMMENT '报告大小 单位MB',
  `period` datetime NOT NULL COMMENT '报表时间',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='过大容量表';

CREATE TABLE `config_modification` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `user_name` varchar(64) NOT NULL COMMENT '用户名',
  `account_name` varchar(64) NOT NULL COMMENT '账户名',
  `action_name` varchar(64) NOT NULL COMMENT 'action名',
  `argument` longtext COMMENT '参数内容',
  `date` datetime NOT NULL COMMENT '修改时间',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='配置修改记录表';

CREATE TABLE `user_define_rule` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增长ID',
  `content` text NOT NULL COMMENT '用户定义规则',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户定义规则表';

CREATE TABLE `business_config` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL DEFAULT '' COMMENT '配置名称',
  `domain` varchar(50) NOT NULL DEFAULT '' COMMENT '项目',
  `content` longtext COMMENT '配置内容',
  `updatetime` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `updatetime` (`updatetime`),
  KEY `name_domain` (`name`,`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `metric_screen` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '配置名称',
  `graph_name` varchar(50) NOT NULL DEFAULT '' COMMENT 'Graph名称',
  `view` varchar(50) NOT NULL DEFAULT '' COMMENT '视角',
  `endPoints` longtext NOT NULL,
  `measurements` longtext NOT NULL COMMENT '配置的指标',
  `content` longtext NOT NULL COMMENT '配置的具体内容',
  `creation_date` datetime NOT NULL COMMENT '配置创建时间',
  `updatetime` datetime NOT NULL COMMENT '配置修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_graph` (`name`,`graph_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统监控的screen配置';

CREATE TABLE `metric_graph` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `graph_id` int(11) NOT NULL COMMENT '大盘ID',
      `name` varchar(50) NOT NULL COMMENT '配置ID',
      `content` longtext COMMENT '配置的具体内容',
      `creation_date` datetime NOT NULL COMMENT '配置创建时间',
      `updatetime` datetime NOT NULL COMMENT '配置修改时间',
      PRIMARY KEY (`id`),
      UNIQUE `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统监控的graph配置';

CREATE TABLE `server_alarm_rule` (
      `id` int(11) NOT NULL AUTO_INCREMENT,
      `category` varchar(50) NOT NULL COMMENT '监控分类',
      `endPoint` varchar(200) NOT NULL COMMENT '监控对象ID',
      `measurement` varchar(200) NOT NULL COMMENT '监控指标',
      `tags` varchar(200) NOT NULL DEFAULT '' COMMENT '监控指标标签',
      `content` longtext NOT NULL COMMENT '配置的具体内容',
      `type` varchar(20) NOT NULL DEFAULT '' COMMENT '数据聚合方式',
      `creator` varchar(100) DEFAULT '' COMMENT '创建人',
      `creation_date` datetime NOT NULL COMMENT '配置创建时间',
      `updatetime` datetime NOT NULL COMMENT '配置修改时间',
      PRIMARY KEY (`id`),
      KEY `updatetime` (`updatetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统告警的配置';

CREATE TABLE `ajax_data_0`
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

CREATE TABLE `ajax_data_1`
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

CREATE TABLE `web_speed_data_0`
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

CREATE TABLE `web_speed_data_1`
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

CREATE TABLE `app_command_data_0`
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

CREATE TABLE `app_command_data_1`
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

CREATE table app_command_data_2 like app_command_data_1;
CREATE table app_command_data_3 like app_command_data_1;
CREATE table app_command_data_4 like app_command_data_1;

CREATE TABLE `app_speed_data_0`
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

CREATE TABLE `app_speed_data_1`
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

CREATE TABLE app_speed_data_2 like app_speed_data_1;
CREATE TABLE app_speed_data_3 like app_speed_data_1;
CREATE TABLE app_speed_data_4 like app_speed_data_1;

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

ALTER TABLE `project` ADD COLUMN `assigner` varchar(20) DEFAULT NULL COMMENT '项目经办人';

ALTER TABLE `project` ADD COLUMN `key` varchar(20) DEFAULT NULL COMMENT '项目键';
