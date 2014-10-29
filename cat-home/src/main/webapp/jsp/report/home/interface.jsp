<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">接口文档</h3>
<div class="tabbable"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs" style="background-color:#f5f5f5">
    <li class="active"><a href="#interfaceDocoument1" data-toggle="tab"><strong>端到端监控接口</strong></a></li>
    <li><a href="#interfaceDocoument2" data-toggle="tab"><strong>变更接口</strong></a></li>
    <li><a href="#interfaceDocoument3" data-toggle="tab"><strong>邮件短信接口</strong></a></li>
    <li><a href="#interfaceDocoument4" data-toggle="tab"><strong>APP监控配置接口</strong></a></li>
    <li><a href="#interfaceDocoument5" data-toggle="tab"><strong>监控报表接口</strong></a></li>
  </ul>
  <div class="tab-content">
    <div class="tab-pane active"  id="interfaceDocoument1"><%@ include file="interface/userMonitor.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument2"><%@ include file="interface/alterationDocument.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument3"><%@ include file="interface/alertApi.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument4"><%@ include file="interface/appCommand.jsp"%></div>
    <div class="tab-pane" id="interfaceDocoument5"><%@ include file="interface/report.jsp"%></div>
    </div>
</div>
