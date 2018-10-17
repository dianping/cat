<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="tabbable"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height: 50px;">
    <li class="active"><a href="#interfaceDocoument1" data-toggle="tab"><strong>用户侧监控</strong></a></li>
    <li><a href="#interfaceDocoument7" data-toggle="tab"><strong>心跳扩展接口</strong></a></li>
    <li><a href="#interfaceDocoument2" data-toggle="tab"><strong>线上变更</strong></a></li>
    <li><a href="#interfaceDocoument3" data-toggle="tab"><strong>邮件短信微信</strong></a></li>
    <li><a href="#interfaceDocoument4" data-toggle="tab"><strong>Metric-HTTP</strong></a></li>
    <li><a href="#interfaceDocoument6" data-toggle="tab"><strong>Zabbix告警</strong></a></li>
    <li><a href="#interfaceDocoument0" data-toggle="tab"><strong>报表接口</strong></a></li>
    <li><a href="#interfaceDocoument8" data-toggle="tab"><strong>项目接口</strong></a></li>
  </ul>
  <div class="tab-content">
    <div class="tab-pane"  id="interfaceDocoument0"><%@ include file="interface/report.jsp"%></div>
    <div class="tab-pane active"  id="interfaceDocoument1"><%@ include file="interface/userMonitor.jsp"%></div>
    <div class="tab-pane active"  id="interfaceDocoument7"><%@ include file="interface/heartbeat.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument2"><%@ include file="interface/alterationDocument.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument3"><%@ include file="interface/alertApi.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument4"><%@ include file="interface/metric.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument6"><%@ include file="interface/alertInterface.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument8"><%@ include file="interface/project.jsp"%></div>
    </div>
</div>
