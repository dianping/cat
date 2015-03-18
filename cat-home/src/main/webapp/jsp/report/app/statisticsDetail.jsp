<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<style>
.tab-content>.active {
  display: flex;
}
</style>
<div class="tabbable"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height:50px;">
	    <li class="text-right active"><a href="#tabContent-all" data-toggle="tab"><strong>访问情况</strong></a></li>
   	    <li class="text-right"><a href="#tabContent-code" data-toggle="tab"><strong>返回码统计</strong></a></li>
  </ul>
  <div class="tab-content">
  <div class="tab-pane active" id="tabContent-all">
	<table class="table table-striped table-condensed table-hover" id="contents-all" style="width:100%">
		<thead>
		<tr>
			<th>命令字</th>
			<th>项目</th>
			<th>标题</th>
			<th>访问量</th>
			<th>平均延时</th>
			<th>平均成功率</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="entry" items="${model.appReport.commands}" varStatus="status">
			<tr>
			<td>${entry.key}</td>
			<td>${entry.value.domain }</td>
			<td>${entry.value.title }</td>
			<td>${w:format(entry.value.count,'#,###,###,###,##0')}</td>
			<td>${w:format(entry.value.avg,'###,##0.000')}</td>
			<td>${w:format(entry.value.successRatio,'#0.000')}%</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	</div>
	<div class="tab-pane" id="tabContent-code">
	<table class="table table-striped table-condensed table-hover" id="contents-code" style="width:100%">
		<thead>
		<tr>
			<th>命令字</th>
			<th>项目</th>
			<th>标题</th>
			<c:forEach var="entry" items="${model.appReport.commands['All'].codes}">
			<th>${entry.key}</th>
			</c:forEach>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="e" items="${model.appReport.commands}">
		<c:set var="command" value="${model.appReport.commands[e.key]}" />
		<tr>
			<td>${e.key}</td>
			<td>${e.value.domain }</td>
			<td>${e.value.title }</td>
		<c:forEach var="entry" items="${model.appReport.commands['All'].codes}" varStatus="status">
			<td>${w:format(command.codes[entry.key].count,'#,###,###,###,##0')}</td>
		</c:forEach>
		</tr>
		</c:forEach>
		</tbody>
	</table>
	</div>
	</div>
</div>