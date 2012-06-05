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

CREATE TABLE `graph` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(20) DEFAULT NULL COMMENT '报表来自于哪台cat-client机器ip, 空串表示合并同domain所有ip',
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
  `period` timestamp NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 3/csv, 默认3',
  `content` mediumtext NOT NULL COMMENT '绘图内容',
  `creation_date` timestamp NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放以小时为单位的绘图数据';

CREATE UNIQUE INDEX graph_period_ip_domain_name ON graph (period, ip, domain, name);

CREATE TABLE `dailyreport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `ip` varchar(20) NOT NULL COMMENT '报表来自于哪台cat-consumer机器',
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
  `period` timestamp NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 默认1',
  `content` mediumtext NOT NULL COMMENT '报表内容',
  `creation_date` timestamp NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放以天为单位的报表信息';

CREATE TABLE `dailygraph` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(20) DEFAULT NULL COMMENT '报表来自于哪台cat-client机器ip, 空串表示合并同domain所有ip',
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
  `period` timestamp NOT NULL  COMMENT '报表时间段',
  `type` tinyint(4) NOT NULL COMMENT '报表数据格式, 1/xml, 2/json, 3/csv, 默认3',
  `content` mediumtext NOT NULL COMMENT '绘图内容',
  `creation_date` timestamp NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放以天为单位的绘图数据';


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
  `happened` timestamp NOT NULL COMMENT '事件发生时间',
  `status`     tinyint(4) NOT NULL COMMENT '执行状态: 0/active, 1/inactive',
  `creation_date` timestamp NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放故障/事件信息';


CREATE TABLE `task` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producer` varchar(20) NOT NULL COMMENT '任务创建者ip',
  `consumer`  varchar(20) DEFAULT NULL COMMENT '任务执行者ip',
  `status`     tinyint(4) NOT NULL COMMENT '执行状态: 1/todo, 2/doing, 3/done 4/failed',
  `failure_count`  tinyint(4) NOT NULL COMMENT '任务失败次数',
  `report_name` varchar(20) NOT NULL COMMENT '报表名称, transaction, problem...',
  `report_domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',  
  `report_period` timestamp NOT NULL COMMENT '报表时间',
  `creation_date` timestamp NOT NULL COMMENT '任务创建时间',
  `start_date`    timestamp NOT NULL COMMENT '开始时间, 这次执行开始时间',
  `end_date`      timestamp NOT NULL COMMENT '结束时间, 这次执行结束时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放故障/事件信息';

