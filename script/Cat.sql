CREATE TABLE `dailygraph` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(20) NULL COMMENT '报表来自于哪台cat-client机器ip, 空串表示合并同domain所有ip',
  `domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 3/csv, 默认3',
  `detail_content` mediumtext NOT NULL COMMENT '详细绘图内容',
  `summary_content` mediumtext NOT NULL COMMENT '概要绘图内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `dailygraph_period_ip_domain_name` (`period`,`ip`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于月报的画图曲线';

CREATE TABLE `dailyreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `content` mediumtext NOT NULL COMMENT '报表内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `period` (`period`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='天报表';

CREATE TABLE `weeklyreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `content` mediumtext NOT NULL COMMENT '报表内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `period` (`period`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='周报表';

CREATE TABLE `monthreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `content` mediumtext NOT NULL COMMENT '报表内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `period` (`period`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='月报表';

#CREATE TABLE `monthlyreport` (
#  `id` int(11) NOT NULL AUTO_INCREMENT,
#  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
#  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
#  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
#  `period` datetime NOT NULL  COMMENT '报表开始时间',
#  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
#  `content` mediumtext NOT NULL COMMENT '报表内容',
#  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
#  PRIMARY KEY (`id`),
#  UNIQUE KEY `period` (`period`,`domain`,`name`)
#) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='月报表';

CREATE TABLE `graph` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(20) NULL COMMENT '报表来自于哪台cat-client机器ip, NULL表示合并同domain所有ip',
  `domain` varchar(50) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 3/csv, 默认3',
  `detail_content` mediumtext NOT NULL COMMENT '详细绘图内容',
  `summary_content` mediumtext NOT NULL COMMENT '概要绘图内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `graph_period_ip_domain_name` (`period`,`ip`,`domain`,`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='小时图表曲线';

CREATE TABLE `hostinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) NOT NULL COMMENT '部署机器IP',
  `domain` varchar(50) NOT NULL COMMENT '部署机器对应的项目名',
  `creation_date` datetime NOT NULL,
  `last_modified_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip_index` (`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='IP和项目名的对应关系';

CREATE TABLE `location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lat` double NOT NULL,
  `lng` double NOT NULL,
  `total` int(11) NOT NULL,
  `transaction_date` datetime NOT NULL,
  `creation_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transaction_date_lat_lng` (`transaction_date`,`lat`,`lng`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于热点图地理位置表';


CREATE TABLE `report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` tinyint(4) NOT NULL COMMENT '报表类型',
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(20) DEFAULT NULL COMMENT '报表来自于哪台机器',
  `domain` varchar(50) NOT NULL,
  `period` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '报表时间段',
  `content` longtext NOT NULL,
  `creation_date` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00' COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  KEY `IX_Domain_Name_Period` (`domain`,`name`,`period`),
  KEY `IX_Name_Period` (`name`,`period`),
  KEY `IX_Period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='用于存放实时报表信息，处理之后的结果';

CREATE TABLE `sqlreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(50) NOT NULL,
  `name` varchar(100) NOT NULL COMMENT 'SQL的名称',
  `statement` varchar(3000) NOT NULL COMMENT 'SQLçš„ç®€å†™',
  `total_count` int(11) NOT NULL COMMENT '总数',
  `failure_count` int(11) NOT NULL COMMENT '错误数目',
  `long_sqls` int(11) NOT NULL COMMENT 'longsql的数目',
  `min_value` double NOT NULL COMMENT '最小值',
  `max_value` double NOT NULL COMMENT '最大值',
  `avg2_value` double NOT NULL COMMENT '90%的平均值',
  `sum_value` double NOT NULL COMMENT '和',
  `sum2_value` double NOT NULL COMMENT '平方和',
  `sample_link` varchar(64) NOT NULL COMMENT '链接',
  `transaction_date` datetime NOT NULL COMMENT 'SQL的时间段',
  `creation_date` datetime NOT NULL COMMENT '数据库创建时间',
  `duration_distribution` varchar(512) NOT NULL COMMENT 'SQL处理的时间分布信息',
  `hits_over_time` varchar(512) NOT NULL COMMENT 'SQL在当前一个小时处理信息',
  `duration_over_time` varchar(512) NOT NULL COMMENT 'SQL处理时间的信息',
  `failure_over_time` varchar(512) NOT NULL COMMENT '在一个小时内的错误分布',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于存放Hadoop处理的SQL报表信息';


CREATE TABLE `sqltable` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(50) DEFAULT NULL COMMENT '这条sql语句的属于项目名',
  `sql_name` varchar(100) DEFAULT NULL COMMENT 'sql 语句的简写',
  `table_name` varchar(100) DEFAULT NULL COMMENT 'sql语句操作的表名',
  `sql_statement` text,
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `domain` (`domain`),
  KEY `sql_name` (`sql_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于存放SQL和操作的表名的关系';

CREATE TABLE `subscriber` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `domain` char(250) NOT NULL COMMENT '域名',
  `group` char(250) NOT NULL COMMENT '域名',
  `owner` char(250) NOT NULL COMMENT '域名',
  `address` varchar(1000) NOT NULL COMMENT '订阅者地址',
  `type` int(4) NOT NULL DEFAULT '0' COMMENT '订阅类型(0,表示邮件；1，表示短信)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='通知订阅者表';

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

CREATE TABLE `alarmTemplate` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL COMMENT '告警模板名称',
  `content` text DEFAULT NULL COMMENT '告警模板内容',
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='告警模板';

CREATE TABLE `alarmRule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `template_id` int(11) NOT NULL   COMMENT '告警模板ID' ,
  `domain` varchar(64) DEFAULT NULL COMMENT '具体项目名称',
  `content` text DEFAULT NULL COMMENT '自定义告警模板内容',
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='告警规则实例表';

CREATE TABLE `alarmRuleSubscription` (
  `alarm_rule_id` int(11) NOT NULL COMMENT '告警规则ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`alarm_rule_id`,`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户订阅告警记录表';

CREATE TABLE `mailRecord` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `type` int(11) NOT NULL COMMENT '发送邮件的内容，1表示告警邮件，2表示每日报表邮件',
  `rule_id` int(11) NOT NULL COMMENT '用户订阅ID外间',
  `receivers` varchar(500) NOT NULL COMMENT '接受用户邮件列表',
  `title` varchar(200) NOT NULL COMMENT '邮件标题',
  `content` text NOT NULL COMMENT '邮件具体内容',
  `status` int(11) NOT NULL COMMENT '1表示发送成功、2表示发送失败',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='邮件通知记录表';

CREATE TABLE `scheduledReport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(64) DEFAULT NULL COMMENT '具体项目名称',
  `names` varchar(200) DEFAULT NULL COMMENT '发送具体的报表,逗号分割',
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='告警规则实例表';

CREATE TABLE `scheduledReportSubscription` (
  `scheduled_report_id` int(11) NOT NULL COMMENT '定时任务ID',
  `user_id` int(11) NOT NULL COMMENT '用户ID',
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`scheduled_report_id`,`user_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户订阅定时报表记录表';

CREATE TABLE `project` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(50) NOT NULL COMMENT '项目名称',
  `project_line` varchar(50)  DEFAULT NULL COMMENT '关联产品线名称',
  `department` varchar(50) DEFAULT NULL COMMENT '关联项目组名称',  
  `owner` varchar(50)  DEFAULT NULL COMMENT '项目负责人',
  `email` varchar(200)  DEFAULT NULL COMMENT '项目组邮件',
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='项目基本信息';

CREATE TABLE `groupStrategy` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL COMMENT 'GroupStrategy的名字',
  `alias` varchar(45) NOT NULL COMMENT 'GroupStrategy的英文名',
  `className` varchar(45) NOT NULL COMMENT 'GroupStrategy的class名字',
  `description` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8$$

CREATE TABLE `abtest` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(45) NOT NULL COMMENT 'abtest的名字',
  `start_date` datetime DEFAULT NULL COMMENT '开始时间',
  `end_date` datetime DEFAULT NULL COMMENT '结束时间',
  `isDisabled` tinyint(4) NOT NULL COMMENT '是否有效',
  `domains` varchar(100) NOT NULL COMMENT '属于的domain，用分号分割',
  `strategy_id` int(11) NOT NULL COMMENT '分组策略ID',
  `strategy_config` blob COMMENT '策略配置',
  `description` varchar(100) DEFAULT NULL COMMENT '实验描述',
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8$$














