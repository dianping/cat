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
  `type` tinyint(4) NOT NULL COMMENT '报表类型',
  `name` varchar(20) NOT NULL COMMENT '报表名称',
  `ip` varchar(20) DEFAULT NULL COMMENT '报表来自于哪台机器',
  `domain` varchar(20) NOT NULL COMMENT '报表处理的Domain信息',
  `period` timestamp NOT NULL  COMMENT '报表时间段',
  `content` mediumtext NOT NULL COMMENT '报表内容',
  `creation_date` timestamp NOT NULL COMMENT '报表创建时间',
  PRIMARY KEY (`id`)
)  DEFAULT CHARSET=utf8 COMMENT='用于存放实时报表信息，处理之后的结果';


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


