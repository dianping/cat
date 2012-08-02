<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.monthreport.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.monthreport.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.monthreport.Model" scope="request"/>

<a:body>

<res:useCss value='${res.css.local.report_css}' target="head-css" />
<res:useCss value='${res.css.local.monthreport_css}' target="head-css" />
<res:useCss value='${res.css.local.body_css}' target="head-css" />

<body>
		
<div class="report">
	<table class="header">
			<tr>
				<td class="title">&nbsp;&nbsp;<b>From ${w:format(model.report.start,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.end,'yyyy-MM-dd HH:mm:ss')}</b>
				&nbsp;&nbsp;共${model.report.days}天</td>
			
			<td class="nav">
					&nbsp;[ <a href="?domain=${model.domain}&date=${model.date}&step=-1">-1m</a> ]&nbsp;
					&nbsp;[ <a href="?domain=${model.domain}&date=${model.date}&step=1">+1m</a> ]&nbsp;
			</td>
		</table>
		
	<table class="navbar">
		<tr>
			<td class="domain">
				<div class="domain">
					<c:forEach var="domain" items="${model.domains}">
						&nbsp;<c:choose>
							<c:when test="${model.domain eq domain}">
								<a href="?domain=${domain}&date=${model.date}" class="current">[&nbsp;${domain}&nbsp;]</a>
							</c:when>
							<c:otherwise>
								<a href="?domain=${domain}&date=${model.date}">[&nbsp;${domain}&nbsp;]</a>
							</c:otherwise>
						</c:choose>&nbsp;
					</c:forEach>
				</div>
			</td>
		</tr>
	</table>
	
	<table class="monthreport">
		<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
		
		<tr><th>URL请求平均响应时间（ms）</th><td>${w:formatNumber(model.report.url.responseTime,'0.0','ms')}</td></tr>
		<tr><th>URL请求月总访问量 </th><td>${w:formatNumber(model.report.url.total,'0.0','')}</td></tr>
		<tr><th>URL请求平均每天访问量</th><td>${w:formatNumber(model.report.url.avg,'0.0','')}</td></tr>
		<tr><th>URL请求月出错次数</th><td>${w:formatNumber(model.report.url.errorTotal,'0.0','')}</td></tr>
		<tr><th>URL请求每天平均出错次数</th><td>${w:formatNumber(model.report.url.errorAvg,'0.0','')}</td></tr>
		<tr><th>URL请求成功百分比</th><td>${w:format(model.report.url.successPercent,'00.0000%')}</td></tr>
			
		<tr><th>&nbsp;</th><td>&nbsp;</td></tr>
		
		<tr><th>Service请求平均响应时间（ms）</th><td>${w:formatNumber(model.report.service.responseTime,'0.0','ms')}</td></tr>
		<tr><th>Service请求月总访问量 </th><td>${w:formatNumber(model.report.service.total,'0.0','')}</td></tr>
		<tr><th>Service请求平均每天访问量</th><td>${w:formatNumber(model.report.service.avg,'0.0','')}</td></tr>
		<tr><th>Service请求月出错次数</th><td>${w:formatNumber(model.report.service.errorTotal,'0.0','')}</td></tr>
		<tr><th>Service请求每天平均出错次数</th><td>${w:formatNumber(model.report.service.errorAvg,'0.0','')}</td></tr>
		<tr><th>Service请求成功百分比</th><td>${w:format(model.report.service.successPercent,'00.0000%')}</td></tr>
		
		<tr><th>&nbsp;</th><td>&nbsp;</td></tr>
		
		<tr><th>远程调用平均响应时间（ms）</th><td>${w:formatNumber(model.report.call.responseTime,'0.0','ms')}</td></tr>
		<tr><th>远程调用月总访问量 </th><td>${w:formatNumber(model.report.call.total,'0.0','')}</td></tr>
		<tr><th>远程调用平均每天访问量</th><td>${w:formatNumber(model.report.call.avg,'0.0','')}</td></tr>
		<tr><th>远程调用月出错次数</th><td>${w:formatNumber(model.report.call.errorTotal,'0.0','')}</td></tr>
		<tr><th>远程调用每天平均出错次数</th><td>${w:formatNumber(model.report.call.errorAvg,'0.0','')}</td></tr>
		<tr><th>远程调用成功百分比</th><td>${w:format(model.report.call.successPercent,'00.0000%')}</td></tr>
		
		<tr><th>&nbsp;</th><td>&nbsp;</td></tr>
		
		<tr><th>数据库平均响应时间（ms）</th><td>${w:formatNumber(model.report.sql.responseTime,'0.0','ms')}</td></tr>
		<tr><th>数据库月总访问量 </th><td>${w:formatNumber(model.report.sql.total,'0.0','')}</td></tr>
		<tr><th>数据库平均每天访问量</th><td>${w:formatNumber(model.report.sql.avg,'0.0','')}</td></tr>
		<tr><th>数据库月出错次数</th><td>${w:formatNumber(model.report.sql.errorTotal,'0.0','')}</td></tr>
		<tr><th>数据库每天平均出错次数</th><td>${w:formatNumber(model.report.sql.errorAvg,'0.0','')}</td></tr>
		<tr><th>数据库成功百分比</th><td>${w:format(model.report.sql.successPercent,'00.0000%')}</td></tr>
		
		<tr><th>&nbsp;</th><td>&nbsp;</td></tr>
		
		<tr><th>缓存平均响应时间（ms）</th><td>${w:formatNumber(model.report.cache.responseTime,'0.0','ms')}</td></tr>
		<tr><th>缓存月总访问量 </th><td>${w:formatNumber(model.report.cache.total,'0.0','')}</td></tr>
		<tr><th>缓存平均每天访问量</th><td>${w:formatNumber(model.report.cache.avg,'0.0','')}</td></tr>
		<tr><th>缓存命中率</th><td>${w:format(model.report.cache.hitPercent,'00.0000%')}</td></tr>
		
		<tr><th>&nbsp;</th><td>&nbsp;</td></tr>
		
		<tr><th>Exception月异常数</th><td>${w:formatNumber(model.report.problem.exceptions,'0.0','')}</td></tr>
		<tr><th>Exception平均每天异常数目</th><td>${w:formatNumber(model.report.problem.avgExceptions,'0.0','')}</td></tr>
		<tr><th>Long-sql月总次数（大于100ms）</th><td>${w:formatNumber(model.report.problem.longSqls,'0.0','')}</td></tr>
		<tr><th>Long-sql平均每天次数（大于100ms）</th><td>${w:formatNumber(model.report.problem.avgLongSqls,'0.0','')}</td></tr>
		<tr><th>Long-sql百分比</th><td>${w:format(model.report.problem.longSqlPercent,'00.0000%')}</td></tr>
		<tr><th>Long-url月总次数（大于1000ms）</th><td>${w:formatNumber(model.report.problem.longUrls,'0.0','')}</td></tr>
		<tr><th>Long-url平均每天次数（大于100ms）</th><td>${w:formatNumber(model.report.problem.avgLongUrls,'0.0','')}</td></tr>
		<tr><th>Long-url百分比</th><td>${w:format(model.report.problem.longUrlPercent,'00.0000%')}</td></tr>
	</table>
</div>
</body>
</a:body>



