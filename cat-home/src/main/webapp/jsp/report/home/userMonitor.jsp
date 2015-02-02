<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<div class="tabbable"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height: 50px;">
    <li class="active"><a href="#userDocoument1" data-toggle="tab"><strong>手机APP端监控</strong></a></li>
    <li><a href="#userDocoument2" data-toggle="tab"><strong>浏览器WEB端监控</strong></a></a></li>
  </ul>
  <div class="tab-content">
    <div class="tab-pane active" id="userDocoument1"><%@ include file="userMonitor/app.jsp"%></div>
    <div class="tab-pane" id="userDocoument2"><%@ include file="userMonitor/web.jsp"%></div>
    </div>
</div>
