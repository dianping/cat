<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="tabbable"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height: 50px;">
    <li class="active"><a href="#userDocoument1" data-toggle="tab"><strong>Overall</strong></a></li>
    <li><a href="#userDocoument2" data-toggle="tab"><strong>Logview</strong></a></a></li>
    <li><a href="#userDocoument3" data-toggle="tab"><strong>Report</strong></a></a></li>
    <li><a href="#userDocoument4" data-toggle="tab"><strong>Transaction</strong></a></a></li>
    <li><a href="#userDocoument5" data-toggle="tab"><strong>Event</strong></a></a></li>
    <li><a href="#userDocoument6" data-toggle="tab"><strong>Problem</strong></a></a></li>
    <li><a href="#userDocoument7" data-toggle="tab"><strong>Heartbeat</strong></a></a></li>
    <li><a href="#userDocoument8" data-toggle="tab"><strong>Cross</strong></a></a></li>
    <li><a href="#userDocoument9" data-toggle="tab"><strong>Matrix</strong></a></a></li>
    <li><a href="#userDocoument10" data-toggle="tab"><strong>Metric</strong></a></a></li>
    <li><a href="#userDocoument11" data-toggle="tab"><strong>Dependency</strong></a></a></li>
    <li><a href="#userDocoument12" data-toggle="tab"><strong>Storage</strong></a></a></li>
  </ul>
  <div class="tab-content">
    <div class="tab-pane active" id="userDocoument1"><%@ include file="userDocument/overall.jsp"%></div>
    <div class="tab-pane" id="userDocoument2"><%@ include file="userDocument/logview.jsp"%></div>
    <div class="tab-pane" id="userDocoument3"><%@ include file="userDocument/report.jsp"%></div>
    <div class="tab-pane" id="userDocoument4"><%@ include file="userDocument/transaction.jsp"%></div>
    <div class="tab-pane" id="userDocoument5"><%@ include file="userDocument/event.jsp"%></div>
    <div class="tab-pane" id="userDocoument6"><%@ include file="userDocument/problem.jsp"%></div>
    <div class="tab-pane" id="userDocoument7"><%@ include file="userDocument/heartbeat.jsp"%></div>
    <div class="tab-pane" id="userDocoument8"><%@ include file="userDocument/cross.jsp"%></div>
    <div class="tab-pane" id="userDocoument9"><%@ include file="userDocument/matrix.jsp"%></div>
    <div class="tab-pane" id="userDocoument10"><%@ include file="userDocument/metric.jsp"%></div>
    <div class="tab-pane" id="userDocoument11"><%@ include file="userDocument/dependency.jsp"%></div>
    <div class="tab-pane" id="userDocoument12"><%@ include file="userDocument/storage.jsp"%></div>
    </div>
</div>
