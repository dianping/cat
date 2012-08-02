<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.monthreport.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.monthreport.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.monthreport.Model" scope="request" />

<a:body>
	<res:useCss value='${res.css.local.report_css}' target="head-css" />
	<res:useCss value='${res.css.local.monthreport_css}' target="head-css" />
	<body>

		<div class="report">
			<table class="header">
				<tr>
					<td class="title">&nbsp;&nbsp;<b> From
							${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to
							${w:format(payload.historyEndDate,'yyyy-MM-dd HH:mm:ss')} </b>
						&nbsp;&nbsp;
					</td>

					<td class="nav">&nbsp;[ <a
						href="?domain=${model.domain}&date=${model.date}&step=-1">-1m</a>
						]&nbsp; &nbsp;[ <a
						href="?domain=${model.domain}&date=${model.date}&step=1">+1m</a>
						]&nbsp;
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
				<tr class="odd">
					<th>项目名称</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<th>${item.domain}</th>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>统计天数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${item.days}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>URL请求平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>URL请求月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>URL请求平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>URL请求月出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.errorTotal,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>URL请求每天平均出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.errorAvg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>URL请求成功百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.url.successPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<th>&nbsp;</th>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Service请求平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Service请求月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Service请求平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Service请求月出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.errorTotal,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Service请求每天平均出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.errorAvg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Service请求成功百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.service.successPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<th>&nbsp;</th>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>远程调用平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>远程调用月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>远程调用平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>远程调用月出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.errorTotal,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>远程调用每天平均出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.errorAvg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>远程调用成功百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.call.successPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<th>&nbsp;</th>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>数据库平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>数据库月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>数据库平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>数据库月出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.errorTotal,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>数据库每天平均出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.errorAvg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>数据库成功百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.sql.successPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<th>&nbsp;</th>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>缓存平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.cache.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>缓存月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.cache.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>缓存平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.cache.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>缓存命中率</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.cache.hitPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<th>&nbsp;</th>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Exception月异常数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problem.exceptions,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Exception平均每天异常数目</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problem.avgExceptions,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Long-sql月总次数（大于100ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problem.longSqls,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Long-sql平均每天次数（大于100ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problem.avgLongSqls,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Long-sql百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.problem.longSqlPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Long-url月总次数（大于1000ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problem.longUrls,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Long-url平均每天次数（大于100ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problem.avgLongUrls,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Long-url百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.problem.longUrlPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>
			</table>
		</div>
	</body>
</a:body>

