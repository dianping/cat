<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="tabbable">
  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height: 50px;">
    <li class="active"><a href="#start" data-toggle="tab"><strong>从零接入App监控步骤</strong></a></li>
    <li><a href="#report" data-toggle="tab"><strong>API访问</strong></a></li>
    <li><a href="#speed" data-toggle="tab"><strong>APP测速</strong></a></li>
    <li><a href="#interface" data-toggle="tab"><strong>接口文档</strong></a></a></li>
    <li><a href="#sdk" data-toggle="tab"><strong>sdk接入</strong></a></li>
  </ul>
  
  <div class="tab-content">
    <div class="tab-pane active" id="start"><%@ include file="mobile/start.jsp"%></div>
    <div class="tab-pane" id="report"><%@ include file="mobile/app.jsp"%></div>
    <div class="tab-pane" id="speed"><%@ include file="mobile/speed.jsp"%></div>
    <div class="tab-pane" id="interface"><%@ include file="mobile/dataInterface.jsp"%></div>
     <div class="tab-pane" id="sdk"><%@ include file="mobile/sdk.jsp"%></div>
   </div>
</div>



