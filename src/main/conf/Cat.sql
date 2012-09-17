CREATE TABLE `logview` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `message_id` varchar(64) NOT NULL COMMENT '消息ID',
  `data_path` varchar(200) NOT NULL COMMENT '消息路径',
  `data_offset` bigint(20) NOT NULL COMMENT '消息偏移量',
  `data_length` int(11) NOT NULL COMMENT '消息长度',
  `tag_thread` varchar(128) DEFAULT NULL COMMENT '消息的线程标志',
  `tag_session` varchar(128) DEFAULT NULL COMMENT '消息的会话标志',
  `tag_request` varchar(128) DEFAULT NULL COMMENT '消息请求标志',
  `creation_date` timestamp NOT NULL  COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `message_id` (`message_id`),
  KEY `tag_thread` (`tag_thread`),
  KEY `tag_session` (`tag_session`),
  KEY `tag_request` (`tag_request`)
) DEFAULT CHARSET=utf8 COMMENT='logview用于存放错误的消息的索引，用来标志它在HDFS上的存放路径';

CREATE TABLE `report` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
  `period` timestamp NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `content` mediumtext NOT NULL COMMENT '报表内容',
  `creation_date` timestamp NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放以小时为单位的报表信息';

CREATE TABLE `sqlreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(50) NOT NULL COMMENT 'SQL报表的内容',
  `name` varchar(100) NOT NULL COMMENT 'SQL的名称',
  `statement` varchar(3000) NOT NULL COMMENT 'SQL的简写',
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
) DEFAULT CHARSET=utf8 COMMENT='用于存放Hadoop处理的SQL报表信息';


CREATE TABLE `comment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(50) NOT NULL COMMENT '报表Domain',
  `author` varchar(32) NOT NULL  COMMENT '作者',
  `content` varchar(1024) NOT NULL COMMENT '事件内容',
  `status`     tinyint(4) NOT NULL COMMENT '执行状态: 0/active, 1/inactive',
  `happened`      datetime NOT NULL COMMENT '事件发生时间',  
  `creation_date` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放故障/事件信息';


CREATE TABLE `task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producer`      varchar(20) NOT NULL COMMENT '任务创建者ip',
  `consumer`      varchar(20) NULL COMMENT '任务执行者ip',
  `failure_count` tinyint(4) NOT NULL COMMENT '任务失败次数',
  `report_name`   varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `report_domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',  
  `report_period` datetime NOT NULL  COMMENT '报表时间',
  `status`        tinyint(4) NOT NULL COMMENT '执行状态: 1/todo, 2/doing, 3/done 4/failed',  
  `task_type`     tinyint(4) NOT NULL COMMENT '执行状态: /daily',
  `creation_date` datetime NOT NULL  COMMENT '任务创建时间',
  `start_date`    datetime NULL  COMMENT '开始时间, 这次执行开始时间',
  `end_date`      datetime NULL  COMMENT '结束时间, 这次执行结束时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放故障/事件信息';

CREATE UNIQUE INDEX task_period_domain_name ON task (report_period, report_domain, report_name);

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
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放以小时为单位的绘图数据';

CREATE UNIQUE INDEX graph_period_ip_domain_name ON graph (period, ip, domain, name);

CREATE TABLE `dailyreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
  `period` datetime NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `content` mediumtext NOT NULL COMMENT '报表内容',
  `creation_date` datetime NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放以天为单位的报表信息';

CREATE UNIQUE INDEX dailyreport_period_domain_name ON dailyreport (period, domain, name);

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
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放以天为单位的绘图数据';

CREATE UNIQUE INDEX dailygraph_period_ip_domain_name ON dailygraph (period, ip, domain, name);

CREATE TABLE `location` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `lat` double NOT NULL,
  `lng` double NOT NULL,
  `total` int(11) NOT NULL,
  `transaction_date` datetime NOT NULL,
  `creation_date` datetime NOT NULL,
  PRIMARY KEY (`id`)
) DEFAULT CHARSET=utf8 COMMENT='热点数据';

CREATE UNIQUE INDEX transaction_date_lat_lng ON location (transaction_date, lat, lng); 

CREATE TABLE `hostinfo` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ip` varchar(50) NOT NULL COMMENT '机器的IP信息',
  `domain` varchar(50) NOT NULL COMMENT 'IP对应项目信息',
  `creation_date` datetime NOT NULL,
  `last_modified_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ip_index` (`ip`)
) 

CREATE TABLE `sqltable` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(50) DEFAULT NULL COMMENT '这条sql语句的属于项目名',
  `sql_name` varchar(100) DEFAULT NULL COMMENT 'sql 语句的简写',
  `table_name` varchar(100) DEFAULT NULL COMMENT 'sql语句操作的表名',
  `sql_statement` varchar(1000) DEFAULT NULL COMMENT '具体的sql名称',
  `creation_date` datetime DEFAULT NULL COMMENT '创建时间',
  `modify_date` datetime DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `domain` (`domain`),
  KEY `sql_name` (`sql_name`)
)
