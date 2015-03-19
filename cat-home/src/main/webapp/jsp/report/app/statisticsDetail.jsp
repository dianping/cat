<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<style>
.tab-content>.active {
  display: flex;
}
.dataTables_wrapper {
  width: 100%;	
}
</style>
<div class="tabbable"> <!-- Only required for left/right tabs -->
  <ul class="nav nav-tabs padding-12 tab-color-blue background-blue" style="height:50px;">
	    <li class="text-right" id="li-all"><a href="#tabContent-all" data-toggle="tab"><strong>访问情况</strong></a></li>
   	    <li class="text-right" id="li-code"><a href="#tabContent-code" data-toggle="tab"><strong>返回码统计</strong></a></li>
  </ul>
  <div class="tab-content">
  <div class="tab-pane" id="tabContent-all">
	<table class="table table-striped table-condensed table-hover" id="contents-all" style="width:100%">
		<thead>
		<tr>
			<th width="20%">命令字</th>
			<th>项目</th>
			<th>访问量</th>
			<th>平均延时(ms)</th>
			<th>平均成功率(%)</th>
			<th>平均发包(B)</th>
			<th>平均回包(B)</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="entry" items="${model.appReport.commands}" varStatus="status">
			<tr>
			<c:choose>
				<c:when test="${not empty entry.value.title }">
					<td>${entry.value.title }</td>
				</c:when>
			<c:otherwise>
					<td>${entry.key }</td>
			</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${not empty entry.value.domain}">
					<td>${entry.value.domain}</td>
				</c:when>
				<c:otherwise>
					<td>无</td>
				</c:otherwise>
				</c:choose>
			<td class="right">${w:format(entry.value.count,'#0')}</td>
			<td class="right">${w:format(entry.value.avg,'#0.0')}</td>
			<td class="right">${w:format(entry.value.successRatio,'#0.000')}</td>
			<td class="right">${w:format(entry.value.requestAvg,'#0.0')}</td>
			<td class="right">${w:format(entry.value.responseAvg,'#0.0')}</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	</div>
	<div class="tab-pane" id="tabContent-code">
	<table class="table table-striped table-condensed table-hover table-bordered" id="contents-code" style="width:100%">
		<thead>
		<tr>
			<th width="20%"><a href="/cat/r/app?op=statistics&domain=${model.domain}&day=${payload.day}&sort=command&type=code">命令字</a></th>
			<th><a href="/cat/r/app?op=statistics&domain=${model.domain}&day=${payload.day}&sort=domain&type=code">项目</a></th>
			<c:forEach var="code" items="${model.codeDistributions}">
			<th style="width:50px;" class="right"><a href="/cat/r/app?op=statistics&domain=${model.domain}&day=${payload.day}&sort=${code}&type=code">${code}</a></th>
			</c:forEach>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="e" items="${model.appReport.commands}">
		<c:set var="command" value="${model.appReport.commands[e.key]}" />
		<tr>
			<c:choose>
				<c:when test="${not empty e.value.title }">
					<td>${e.value.title }</td>
				</c:when>
			<c:otherwise>
					<td>${e.key }</td>
			</c:otherwise>
			</c:choose>
			<c:choose>
				<c:when test="${not empty e.value.domain}">
					<td>${e.value.domain}</td>
				</c:when>
				<c:otherwise>
					<td>无</td>
				</c:otherwise>
			</c:choose>
			<c:forEach var="code" items="${model.codeDistributions}" varStatus="status">
				<c:set var="data" value="${command.codes[code]}" />
				<c:choose>
				<c:when test="${data == null}">
					<td class="right">0</td>
				</c:when>
				<c:otherwise>
					<td data-rel="tooltip" data-placement="left" title="${command.codes[code].title}"  class="right">${w:format(command.codes[code].count,'#,###,###,###,##0')}</td>
				</c:otherwise>
				</c:choose>
			</c:forEach>
		</tr>
		</c:forEach>
		</tbody>
	</table>
	</div>
	</div>
</div>