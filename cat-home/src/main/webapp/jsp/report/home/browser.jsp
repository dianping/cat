<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="tabbable">
  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height: 50px;">
    <li class="active"><a href="#report" data-toggle="tab"><strong>web访问</strong></a></li>
    <li><a href="#interface" data-toggle="tab"><strong>接口文档</strong></a></a></li>
    <li><a href="#alert" data-toggle="tab"><strong>JS告警配置</strong></a></li>
  </ul>
  
  <div class="tab-content">
    <div class="tab-pane active" id="report"><%@ include file="browser/web.jsp"%></div>
    <div class="tab-pane" id="interface"><%@ include file="browser/interface.jsp"%></div>
    <div class="tab-pane" id="alert"><%@ include file="browser/jsAlert.jsp"%></div>
   </div>
</div>
