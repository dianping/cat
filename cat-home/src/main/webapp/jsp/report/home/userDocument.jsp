<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">用户文档</h3>
<div class="tabbable"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs" style="background-color:#f5f5f5">
    <li class="active"><a href="#userDocoument1" data-toggle="tab"><strong>Overall</strong></a></li>
    <li><a href="#userDocoument2" data-toggle="tab"><strong>Logview</strong></a></a></li>
    <li><a href="#userDocoument3" data-toggle="tab"><strong>Transaction</strong></a></a></li>
    <li><a href="#userDocoument4" data-toggle="tab"><strong>Event</strong></a></a></li>
    <li><a href="#userDocoument5" data-toggle="tab"><strong>Problem</strong></a></a></li>
    <li><a href="#userDocoument6" data-toggle="tab"><strong>Heartbeat</strong></a></a></li>
    <li><a href="#userDocoument7" data-toggle="tab"><strong>Top</strong></a></a></li>
    <li><a href="#userDocoument8" data-toggle="tab"><strong>Dependency</strong></a></a></li>
  </ul>
  <div class="tab-content">
    <div class="tab-pane active" id="userDocoument1"><%@ include file="userDocument/overall.jsp"%></div>
    <div class="tab-pane" id="userDocoument2"><%@ include file="userDocument/logview.jsp"%></div>
    <div class="tab-pane" id="userDocoument3"><%@ include file="userDocument/transaction.jsp"%></div>
    <div class="tab-pane" id="userDocoument4"><%@ include file="userDocument/event.jsp"%></div>
    <div class="tab-pane" id="userDocoument5"><%@ include file="userDocument/problem.jsp"%></div>
    <div class="tab-pane" id="userDocoument6"><%@ include file="userDocument/heartbeat.jsp"%></div>
    <div class="tab-pane" id="userDocoument7"><%@ include file="userDocument/top.jsp"%></div>
    <div class="tab-pane" id="userDocoument8"><%@ include file="userDocument/dependency.jsp"%></div>
    </div>
</div>
