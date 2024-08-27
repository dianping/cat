# Changelog

## 3.4.1 (2024-08-27)

### FEATURES

- 新增 JVM 内置参数，支持 G1、CMS、ZGC 垃圾回收器

### BUGFIX

- 修复 `HeartBeat` 监控告警交互问题
- 修复 Jira Software 兼容性问题

## 3.4.0 (2024-05-01)

### FEATURES

- 支持告警自动录入 Jira Software

### SCRIPTS

- docs/scripts/cat-upgrade-3.4.0.sql

## 3.3.2 (2024-01-26)

### BUGFIX

- 修复 Docker 环境下配置 CAT 集群的问题

## 3.3.1 (2023-09-07)

### BUGFIX

- 修复 `Server`、`Browser`、`Mobile` 监控跳转交互问题

## 3.3.0 (2023-06-30)

### FEATURES

- 新增 `Server`、`Browser`、`Mobile` 视图

### SCRIPTS

- docs/scripts/cat-upgrade-3.3.0.sql

### IMPROVEMENTS

- 优化消息树详情展示滚动条交互

## 3.2.0 (2023-04-04)

### FEATURES

- 支持邮件、钉钉、企业微信、飞书机器人推送，无需部署额外资源
- 新增应用大盘、数据库大盘、缓存大盘、服务大盘告警

### IMPROVEMENTS

- 优化 `Business`、`Exception`、`HeartBeat`、`Transaction`、`Event` 告警类型

## 3.1.0 (2023-03-03)

### FEATURES

- 新增链路跟踪，支持 `traceId` 检索（建议客户端集成 [`eden-architect`](https://github.com/shiyindaxiaojie/eden-architect.git) 框架）
- 新增服务 `Matrix` 性能报告

### IMPROVEMENTS

- 界面美化，遵循 Bootstrap 风格
- 中文汉化，适合初学者入门
