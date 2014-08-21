<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">告警文档</h3>
<div class="tabbable">
  <ul class="nav nav-tabs" style="background-color:#f5f5f5">
    <li class="active"><a href="#overall" data-toggle="tab"><strong>Overall</strong></a></li>
    <li><a href="#business" data-toggle="tab"><strong>Business</strong></a></a></li>
    <li><a href="#network" data-toggle="tab"><strong>Network</strong></a></a></li>
    <li><a href="#system" data-toggle="tab"><strong>System</strong></a></a></li>
    <li><a href="#exception" data-toggle="tab"><strong>Exception</strong></a></a></li>
    <li><a href="#thirdPartyException" data-toggle="tab"><strong>ThirdPartyException</strong></a></a></li>
    <li><a href="#frontendException" data-toggle="tab"><strong>FrontendException</strong></a></a></li>
    <li><a href="#api" data-toggle="tab"><strong>Alert HTTP API</strong></a></a></li>
  </ul>
  
  <div class="tab-content">
    <div class="tab-pane active" id="overall"><%@ include file="alertDocument/overall.jsp"%></div>
    <div class="tab-pane" id="business"><%@ include file="alertDocument/business.jsp"%></div>
    <div class="tab-pane" id="network"><%@ include file="alertDocument/network.jsp"%></div>
    <div class="tab-pane" id="system"><%@ include file="alertDocument/system.jsp"%></div>
    <div class="tab-pane" id="exception"><%@ include file="alertDocument/exception.jsp"%></div>
    <div class="tab-pane" id="api"><%@ include file="alertDocument/api.jsp"%></div>
   </div>
</div>
