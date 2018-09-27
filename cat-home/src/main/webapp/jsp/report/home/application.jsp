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
    <li><a href="#userDocoument10" data-toggle="tab"><strong>Business</strong></a></a></li>
    <li><a href="#userDocoument11" data-toggle="tab"><strong>Dependency</strong></a></a></li>
    <li><a href="#userDocoument12" data-toggle="tab"><strong>Storage</strong></a></a></li>
  </ul>
  <div class="tab-content">
    <div class="tab-pane active" id="userDocoument1"><%@ include file="application/overall.jsp"%></div>
    <div class="tab-pane" id="userDocoument2"><%@ include file="application/logview.jsp"%></div>
    <div class="tab-pane" id="userDocoument3"><%@ include file="application/report.jsp"%></div>
    <div class="tab-pane" id="userDocoument4"><%@ include file="application/transaction.jsp"%></div>
    <div class="tab-pane" id="userDocoument5"><%@ include file="application/event.jsp"%></div>
    <div class="tab-pane" id="userDocoument6"><%@ include file="application/problem.jsp"%></div>
    <div class="tab-pane" id="userDocoument7"><%@ include file="application/heartbeat.jsp"%></div>
    <div class="tab-pane" id="userDocoument8"><%@ include file="application/cross.jsp"%></div>
    <div class="tab-pane" id="userDocoument9"><%@ include file="application/matrix.jsp"%></div>
    <div class="tab-pane" id="userDocoument10"><%@ include file="application/metric.jsp"%></div>
    <div class="tab-pane" id="userDocoument11"><%@ include file="application/dependency.jsp"%></div>
    <div class="tab-pane" id="userDocoument12"><%@ include file="application/storage.jsp"%></div>
    </div>
</div>
