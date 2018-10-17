<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="tabbable">
  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height: 50px;">
    <li class="active"><a href="#overall" data-toggle="tab"><strong>综述</strong></a></li>
    <li><a href="#business" data-toggle="tab"><strong>业务告警</strong></a></a></li>
    <li><a href="#transaction" data-toggle="tab"><strong>响应时间告警</strong></a></li>
    <li><a href="#exception" data-toggle="tab"><strong>异常告警</strong></a></a></li>
<!--     <li><a href="#network" data-toggle="tab"><strong>网络告警</strong></a></a></li>
    <li><a href="#system" data-toggle="tab"><strong>系统告警</strong></a></a></li> -->
    <li><a href="#heartbeat" data-toggle="tab"><strong>心跳告警</strong></a></a></li>
<!--     <li><a href="#database" data-toggle="tab"><strong>数据库告警</strong></a></a></li>
    <li><a href="#thirdPartyException" data-toggle="tab"><strong>ping告警</strong></a></a></li>
 -->  </ul>
  
  <div class="tab-content">
    <div class="tab-pane active" id="overall"><%@ include file="alertDocument/overall.jsp"%></div>
    <div class="tab-pane" id="business"><%@ include file="alertDocument/business.jsp"%></div>
    <div class="tab-pane" id="transaction"><%@ include file="alertDocument/transaction.jsp"%></div>
    <div class="tab-pane" id="network"><%@ include file="alertDocument/network.jsp"%></div>
    <div class="tab-pane" id="system"><%@ include file="alertDocument/system.jsp"%></div>
    <div class="tab-pane" id="exception"><%@ include file="alertDocument/exception.jsp"%></div>
    <div class="tab-pane" id="heartbeat"><%@ include file="alertDocument/heartbeat.jsp"%></div>
    <div class="tab-pane" id="database"><%@ include file="alertDocument/database.jsp"%></div>
    <div class="tab-pane" id="thirdPartyException"><%@ include file="alertDocument/thirdPartyException.jsp"%></div>
   </div>
</div>
