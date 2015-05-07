<%@ page contentType="text/html; charset=utf-8"%>
<c:set var="report" value="${model.problemReport}" />
	<div class="report">
		<c:set var="navUrlPrefix" value="op=${payload.action.name}&query1=${payload.query1}"/> 
		<table style="width:100%;">
		<c:choose>
				<c:when test="${payload.action.name eq 'crashLog'}">
				<td><span class="text-danger title">【报表时间】</span><span class="text-success">${w:format(report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(report.endTime,'yyyy-MM-dd HH:mm:ss')}</span>
					</td><td align="right"><span class="text-danger switch"><a class="switch" href="${model.baseUri}?op=historyCrashLog&query1=AndroidCrashLog;;;;"><span class="text-danger">【切到历史模式】</span></a></span>
					<c:forEach var="nav" items="${model.navs}">
						&nbsp;[ <a href="${model.baseUri}?date=${model.date}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]
					</c:forEach>
					&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
					</td>
				</c:when>
		<c:otherwise>
			<c:if test="${payload.action.name eq 'historyCrashLog'}">
				<td><span class="text-danger title">【报表时间】</span><span class="text-success">${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</span>
				</td><td align="right"><span class="text-danger switch"><a class="switch" href="${model.baseUri}?op=crashLog&query1=AndroidCrashLog;;;;"><span class="text-danger">【切到小时模式】</span></a></span>
				<c:forEach var="nav" items="${model.historyNavs}">
				<c:choose>
					<c:when test="${nav.title eq payload.reportType}">
							&nbsp;[ <a href="?${navUrlPrefix}&reportType=${nav.title}&date=${model.date}" class="current">${nav.title}</a> ]
					</c:when>
					<c:otherwise>
							&nbsp;[ <a href="?${navUrlPrefix}&reportType=${nav.title}&date=${model.date}">${nav.title}</a> ]
					</c:otherwise>
				</c:choose>
				</c:forEach>
				&nbsp;[ <a href="?${navUrlPrefix}&date=${model.date}&reportType=${payload.reportType}&step=-1">${model.currentNav.last}</a> ]
				&nbsp;[ <a href="?${navUrlPrefix}&date=${model.date}&reportType=${payload.reportType}&step=1">${model.currentNav.next}</a> ]
				&nbsp;[ <a href="?${navUrlPrefix}&reportType=${payload.reportType}&nav=next">now</a> ]
				 </td>
			</c:if>
		</c:otherwise>
		</c:choose>
		</table>
		<br>
		<table class="table ">
		<tr><td width="100px;">
				平台类型</td><td><select id="platformType" style="width: 200px;">
					<c:forEach var="item" items="${model.crashLogDomains}">
						<option value='${item.id}'>
						<c:choose>
							<c:when test="${empty item.title }">
								${item.id}
							</c:when>
						<c:otherwise>
							${item.title}
						</c:otherwise>
						</c:choose>
						</option>
					</c:forEach>
					</select>&nbsp;&nbsp;&nbsp;<input class="btn btn-primary btn-sm "
					value="&nbsp;&nbsp;&nbsp;查询&nbsp;&nbsp;&nbsp;" onclick="query()"
					type="submit" /></td></tr>
					<tr><td width="60px;">APP版本</td><td>
						<div>
						<label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="appVersionAll" onclick="clickAll('${model.fieldsInfo.appVersions}', 'appVersion')" unchecked>All
		  				</label><c:forEach var="item" items="${model.fieldsInfo.appVersions}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="appVersion_${item}" value="${item}" onclick="clickMe('${model.fieldsInfo.appVersions}', 'appVersion')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
					<tr><td width="60px;">平台版本</td><td><div><label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="platformVersionAll" onclick="clickAll('${model.fieldsInfo.platVersions}', 'platformVersion')" unchecked>All
		  				</label><c:forEach var="item" items="${model.fieldsInfo.platVersions}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="platformVersion_${item}" value="${item}" onclick="clickMe('${model.fieldsInfo.platVersions}', 'platformVersion')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
					<tr><td width="60px;"> 模块</td><td><div>
						<label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="moduleAll" onclick="clickAll('${model.fieldsInfo.modules}', 'module')" unchecked>All
		  				</label><c:forEach var="item" items="${model.fieldsInfo.modules}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="module_${item}" value="${item}" onclick="clickMe('${model.fieldsInfo.modules}', 'module')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
					<tr><td width="60px;"> 级别</td><td><div>
						<label class="btn btn-info btn-sm">
		    				<input type="checkbox" id="levelAll" onclick="clickAll('${model.fieldsInfo.levels}', 'level')"  unchecked>All
		  				</label><c:forEach var="item" items="${model.fieldsInfo.levels}" varStatus="status"><label class="btn btn-info btn-sm"><input type="checkbox" id="level_${item}" value="${item}" onclick="clickMe('${model.fieldsInfo.levels}', 'level')" unchecked>${item}</label></c:forEach>
						</div>
						</td></tr>
	</table>
	</div>
	<br>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
<table class="table table-hover table-striped table-condensed"  style="width:100%">
	<tr>
		<th width="5%">Total</th>
		<th width="45%">Status</th>
		<th width="5%">Count</th>
		<th width="450%">SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${model.problemStatistics.types}"
		varStatus="typeIndex">
		<tr>
			<td rowspan="${w:size(statistics.value.status)*2}">${w:format(statistics.value.count,'#,###,###,###,##0')}&nbsp;</td>
			<c:forEach var="status" items="${statistics.value.status}"
				varStatus="index">
				<c:if test="${index.index != 0}">
					<tr>
				</c:if>
				<td>
					${status.value.status}
				</td>
				<td class="right">${w:format(status.value.count,'#,###,###,###,##0')}&nbsp;</td>
				<td>
					<c:forEach var="links" items="${status.value.links}" varStatus="linkIndex">
						<a href="/cat/r/m/${links}?domain=${model.domain}">${linkIndex.first?'L':(linkIndex.last?'g':'o')}</a>
					</c:forEach></td>
						
				<c:if test="${index.index != 0}">
				</tr>
				</c:if>
			</c:forEach>
		</tr>
		<tr class="graphs"><td colspan="5"><div id="${typeIndex.index}" style="display:none"></div></td></tr>
	</c:forEach>
</table>
