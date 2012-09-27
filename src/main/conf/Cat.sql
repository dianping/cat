
CREATE TABLE `dailygraph` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(20) NULL COMMENT '报表来自于哪台cat-client机器ip, 空串表示合并同domain所有ip',
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
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
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `content` mediumtext NOT NULL COMMENT '报表内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `period` (`period`,`domain`,`name`),
  KEY `type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=16027 DEFAULT CHARSET=utf8 COMMENT='天报表';

CREATE TABLE `graph` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(20) NULL COMMENT '报表来自于哪台cat-client机器ip, NULL表示合并同domain所有ip',
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 3/csv, 默认3',
  `detail_content` mediumtext NOT NULL COMMENT '详细绘图内容',
  `summary_content` mediumtext NOT NULL COMMENT '概要绘图内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `graph_period_ip_domain_name` (`period`,`ip`,`domain`,`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1588163 DEFAULT CHARSET=utf8 COMMENT='小时图表曲线';

CREATE TABLE `hostinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) NOT NULL COMMENT '部署机器IP',
  `domain` varchar(50) NOT NULL COMMENT '部署机器对应的项目名',
  `creation_date` datetime NOT NULL,
  `last_modified_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip_index` (`ip`)
) ENGINE=InnoDB AUTO_INCREMENT=226602 DEFAULT CHARSET=utf8;

CREATE TABLE `location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lat` double NOT NULL,
  `lng` double NOT NULL,
  `total` int(11) NOT NULL,
  `transaction_date` datetime NOT NULL,
  `creation_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `transaction_date_lat_lng` (`transaction_date`,`lat`,`lng`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='çƒ­ç‚¹æ•°æ®';

CREATE TABLE `maillog` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `address` varchar(500) NOT NULL COMMENT '收件人',
  `title` varchar(500) NOT NULL COMMENT '邮件标题',
  `content` text NOT NULL COMMENT '邮件内容',
  `sendtime` datetime NOT NULL COMMENT '发送时间',
  `status` int(11) NOT NULL COMMENT '发送状态（0、发送成功；1、发送失败）',
  `error` varchar(1000) DEFAULT NULL COMMENT '发送失败错误信息',
  `cc` varchar(500) DEFAULT NULL COMMENT '邮件抄送人',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2224 DEFAULT CHARSET=utf8 COMMENT='邮件提醒发送记录';

CREATE TABLE `monthreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) DEFAULT NULL,
  `domain` varchar(100) DEFAULT NULL,
  `period` datetime DEFAULT NULL,
  `content` mediumtext,
  `creation_date` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_2` (`name`,`domain`,`period`)
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8;


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
) ENGINE=InnoDB AUTO_INCREMENT=433981 DEFAULT CHARSET=utf8 ROW_FORMAT=COMPRESSED COMMENT='用于存放实时报表信息，处理之后的结果';

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
) ENGINE=InnoDB AUTO_INCREMENT=196645 DEFAULT CHARSET=utf8 COMMENT='用于存放Hadoop处理的SQL报表信息';


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
) ENGINE=InnoDB AUTO_INCREMENT=767 DEFAULT CHARSET=utf8;

CREATE TABLE `subscriber` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `domain` char(250) NOT NULL COMMENT '域名',
  `group` char(250) NOT NULL COMMENT '域名',
  `owner` char(250) NOT NULL COMMENT '域名',
  `address` varchar(1000) NOT NULL COMMENT '订阅者地址',
  `type` int(4) NOT NULL DEFAULT '0' COMMENT '订阅类型(0,表示邮件；1，表示短信)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8 COMMENT='通知订阅者表';

CREATE TABLE `task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producer`      varchar(20) NOT NULL COMMENT '任务创建者ip',
  `consumer`      varchar(20) NULL COMMENT '任务执行者ip',
  `failure_count` tinyint(4) NOT NULL COMMENT '任务失败次数',
  `report_name`   varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `report_domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',  
  `report_period` datetime NOT NULL  COMMENT '报表时间',
  `status`        tinyint(4) NOT NULL COMMENT '执行状态: 1/todo, 2/doing, 3/done 4/failed',  
  `task_type`     tinyint(4) NOT NULL DEFAULT '1' COMMENT '0表示小时任务，1表示天任务',
  `creation_date` datetime NOT NULL  COMMENT '任务创建时间',
  `start_date`    datetime NULL  COMMENT '开始时间, 这次执行开始时间',
  `end_date`      datetime NULL  COMMENT '结束时间, 这次执行结束时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_period_domain_name_type` (`report_period`,`report_domain`,`report_name`,`task_type`)
) ENGINE=InnoDB AUTO_INCREMENT=471692 DEFAULT CHARSET=utf8 COMMENT='后台任务';

