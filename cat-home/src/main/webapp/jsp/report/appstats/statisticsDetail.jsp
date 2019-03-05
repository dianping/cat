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
   	    <li class="text-right" id="li-codeDist"><a href="#tabContent-codeDist" data-toggle="tab"><strong>返回码分布</strong></a></li>
  </ul>
  <div class="tab-content">
  <div class="tab-pane" id="tabContent-all">
	<table class="table table-striped table-condensed table-hover table-bordered" id="contents-all" style="width:100%">
		<thead>
		<tr>
			<th width="20%"><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=command">命令字</a></th>
			<th><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=domain">项目</a></th>
			<th><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=count">访问量</a></th>
			<th><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=avg">平均延时(ms)</a></th>
			<th><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=success">平均成功率(%)</a></th>
			<th><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=request">平均发包(B)</th>
			<th><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=response">平均回包(B)</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="entry" items="${model.displayCommands.commands}" varStatus="status">
			<tr><td><a href="/cat/r/app?query1=${payload.day};${entry.value.id};;;;;;;;;00:00;23:59&commandId=${entry.value.name}" target="_blank">
			<c:choose>
				<c:when test="${not empty entry.value.title}">
					${entry.value.title}
				</c:when>
			<c:otherwise>
					${entry.value.name}
			</c:otherwise>
			</c:choose></a></td>
			<c:choose>
				<c:when test="${not empty entry.value.domain}">
					<td>
					${entry.value.domain}</td>
				</c:when>
				<c:otherwise>
					<td>无</td>
				</c:otherwise>
				</c:choose>
			<td class="right">${w:format(entry.value.count,'#,###,###,###,##0')}</td>
			<td class="right">${w:format(entry.value.avg,'#,###,###,###,##0.0')}</td>
			<td class="right">${w:format(entry.value.successRatio,'#0.000')}</td>
			<td class="right">${w:format(entry.value.requestAvg,'#,###,###,###,##0.0')}</td>
			<td class="right">${w:format(entry.value.responseAvg,'#,###,###,###,##0.0')}</td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
	</div>
	<div class="tab-pane" id="tabContent-code">
	<table class="table table-striped table-condensed table-hover table-bordered" id="contents-code" style="width:100%">
		<thead>
		<tr>
			<th width="20%"><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=command&type=code">命令字</a></th>
			<th><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=domain&type=code">项目</a></th>
			<c:forEach var="code" items="${model.codeDistributions}">
			<th style="width:50px;" class="right"><a href="/cat/r/appstats?appId=${payload.appId}&domain=${model.domain}&day=${payload.day}&sort=${code}&type=code">${code}</a></th>
			</c:forEach>
		</tr>
		</thead>
		<tbody>
		<c:forEach var="e" items="${model.displayCommands.commands}">
		<c:set var="command" value="${model.displayCommands.commands[e.key]}" />
		<tr><td><a href="/cat/r/app?op=piechart&query1=${payload.day};${e.value.id};;;;;;;;;00:00;23:59&commandId=${e.value.name}" target="_blank">
			<c:choose>
				<c:when test="${not empty e.value.title }">
					${e.value.title}
				</c:when>
			<c:otherwise>
					${e.value.name}
			</c:otherwise>
			</c:choose></a></td>
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
	
	<div class="tab-pane" id="tabContent-codeDist">
		<table id="contents-code" style="width:100%" >
		<c:forEach var="entry" items="${model.piecharts}" varStatus="status">
			<tr>	
				<td  class="center">
				<div id="piechart_${entry.key}" ></div>
				</td>
			</tr>
		</c:forEach>
		</table>
		
	</div>
	</div>
</div>