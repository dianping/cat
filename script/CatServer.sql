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
