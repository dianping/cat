<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.monthreport.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.monthreport.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.monthreport.Model" scope="request" />

<a:body>

	<res:useCss value='${res.css.local.report_css}' target="head-css" />
	<res:useCss value='${res.css.local.monthreport_css}' target="head-css" />

	<body>

		<div class="report">
			<table class="header">
				<tr>
					<td class="title">&nbsp;&nbsp;<b>From
							${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to
							${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</b>
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
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr class="odd">
					<th>报表月份</th>
					<td>${w:format(model.reportLastTwo.startTime,'yyyy-MM')}</td>
					<td>${w:format(model.reportLast.startTime,'yyyy-MM')}</td>
					<td>${w:format(model.report.startTime,'yyyy-MM')}</td>
				</tr>
				<tr class="even">
					<th>统计天数</th>
					<td>${model.reportLastTwo.day}</td>
					<td>${model.reportLast.day}</td>
					<td>${model.report.day}</td>
				</tr>
				<tr class="odd">
					<th>URL请求平均响应时间（ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.url.baseInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.reportLast.url.baseInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.report.url.baseInfo.responseTime,'0.0','ms')}</td>
					<%-- <td background="/cat/images/${model.report.url.baseInfo.responseTimeFlag}.jpeg">
						<img width="15px" height="15px" src="/cat/images/${model.report.url.baseInfo.responseTimeFlag}.jpeg"></img>
					</td> --%>
				</tr>
				<tr class="even">
					<th>URL请求月总访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.url.baseInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.url.baseInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.report.url.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>URL请求平均每天访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.url.baseInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.url.baseInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.url.baseInfo.avg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>URL请求月出错次数</th>
					<td>${w:formatNumber(model.reportLastTwo.url.baseInfo.errorTotal,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.url.baseInfo.errorTotal,'0.0','')}</td>
					<td>${w:formatNumber(model.report.url.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>URL请求每天平均出错次数</th>
					<td>${w:formatNumber(model.reportLastTwo.url.baseInfo.errorAvg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.url.baseInfo.errorAvg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.url.baseInfo.errorAvg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>URL请求成功百分比</th>
					<td>${w:format(model.reportLastTwo.url.baseInfo.successPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.url.baseInfo.successPercent,'00.0000%')}</td>
					<td>${w:format(model.report.url.baseInfo.successPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Service请求平均响应时间（ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.service.baseInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.reportLast.service.baseInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.report.service.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>Service请求月总访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.service.baseInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.service.baseInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.report.service.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>Service请求平均每天访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.service.baseInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.service.baseInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.service.baseInfo.avg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Service请求月出错次数</th>
					<td>${w:formatNumber(model.reportLastTwo.service.baseInfo.errorTotal,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.service.baseInfo.errorTotal,'0.0','')}</td>
					<td>${w:formatNumber(model.report.service.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>Service请求每天平均出错次数</th>
					<td>${w:formatNumber(model.reportLastTwo.service.baseInfo.errorAvg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.service.baseInfo.errorAvg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.service.baseInfo.errorAvg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Service请求成功百分比</th>
					<td>${w:format(model.reportLastTwo.service.baseInfo.successPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.service.baseInfo.successPercent,'00.0000%')}</td>
					<td>${w:format(model.report.service.baseInfo.successPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>远程调用平均响应时间（ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.call.baseInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.reportLast.call.baseInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.report.call.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>远程调用月总访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.call.baseInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.call.baseInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.report.call.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>远程调用平均每天访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.call.baseInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.call.baseInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.call.baseInfo.avg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>远程调用月出错次数</th>
					<td>${w:formatNumber(model.reportLastTwo.call.baseInfo.errorTotal,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.call.baseInfo.errorTotal,'0.0','')}</td>
					<td>${w:formatNumber(model.report.call.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>远程调用每天平均出错次数</th>
					<td>${w:formatNumber(model.reportLastTwo.call.baseInfo.errorAvg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.call.baseInfo.errorAvg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.call.baseInfo.errorAvg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>远程调用成功百分比</th>
					<td>${w:format(model.reportLastTwo.call.baseInfo.successPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.call.baseInfo.successPercent,'00.0000%')}</td>
					<td>${w:format(model.report.call.baseInfo.successPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>数据库平均响应时间（ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.sql.baseInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.reportLast.sql.baseInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.report.sql.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>数据库月总访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.sql.baseInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.sql.baseInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.report.sql.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>数据库平均每天访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.sql.baseInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.sql.baseInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.sql.baseInfo.avg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>数据库月出错次数</th>
					<td>${w:formatNumber(model.reportLastTwo.sql.baseInfo.errorTotal,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.sql.baseInfo.errorTotal,'0.0','')}</td>
					<td>${w:formatNumber(model.report.sql.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>数据库每天平均出错次数</th>
					<td>${w:formatNumber(model.reportLastTwo.sql.baseInfo.errorAvg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.sql.baseInfo.errorAvg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.sql.baseInfo.errorAvg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>数据库成功百分比</th>
					<td>${w:format(model.reportLastTwo.sql.baseInfo.successPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.sql.baseInfo.successPercent,'00.0000%')}</td>
					<td>${w:format(model.report.sql.baseInfo.successPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>memcached缓存平均响应时间（ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.memCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.reportLast.memCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.report.memCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>memcached缓存月总访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.memCache.baseCacheInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.memCache.baseCacheInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.report.memCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>memcached缓存平均每天访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.memCache.baseCacheInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.memCache.baseCacheInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.memCache.baseCacheInfo.avg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>memcached缓存命中率</th>
					<td>${w:format(model.reportLastTwo.memCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.memCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					<td>${w:format(model.report.memCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
				</tr>

				<tr class="odd">
					<th>kvdbCache缓存平均响应时间（ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.kvdbCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.reportLast.kvdbCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.report.kvdbCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>kvdbCache缓存月总访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.kvdbCache.baseCacheInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.kvdbCache.baseCacheInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.report.kvdbCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>kvdbCache缓存平均每天访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.kvdbCache.baseCacheInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.kvdbCache.baseCacheInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.kvdbCache.baseCacheInfo.avg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>kvdbCache缓存命中率</th>
					<td>${w:format(model.reportLastTwo.kvdbCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.kvdbCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					<td>${w:format(model.report.kvdbCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
				</tr>

				<tr class="odd">
					<th>webCache缓存平均响应时间（ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.webCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.reportLast.webCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					<td>${w:formatNumber(model.report.webCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>webCache缓存月总访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.webCache.baseCacheInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.webCache.baseCacheInfo.total,'0.0','')}</td>
					<td>${w:formatNumber(model.report.webCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>webCache缓存平均每天访问量</th>
					<td>${w:formatNumber(model.reportLastTwo.webCache.baseCacheInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.webCache.baseCacheInfo.avg,'0.0','')}</td>
					<td>${w:formatNumber(model.report.webCache.baseCacheInfo.avg,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>webCache缓存命中率</th>
					<td>${w:format(model.reportLastTwo.webCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.webCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					<td>${w:format(model.report.webCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Exception月异常数</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.exceptions,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.exceptions,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.exceptions,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Exception平均每天异常数目</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.avgExceptions,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.avgExceptions,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.avgExceptions,'0.0','')}</td>
				</tr>

				<tr class="odd">
					<th>Long-url月总次数（大于1000ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.longUrls,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.longUrls,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.longUrls,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Long-url平均每天次数（大于100ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.avgLongUrls,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.avgLongUrls,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.avgLongUrls,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>Long-url百分比</th>
					<td>${w:format(model.reportLastTwo.problemInfo.longUrlPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.problemInfo.longUrlPercent,'00.0000%')}</td>
					<td>${w:format(model.report.problemInfo.longUrlPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Long-service月总次数（大于1000ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.longServices,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.longServices,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.longServices,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Long-service平均每天次数（大于100ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.avgLongServices,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.avgLongServices,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.avgLongServices,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>Long-service百分比</th>
					<td>${w:format(model.reportLastTwo.problemInfo.longServicePercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.problemInfo.longServicePercent,'00.0000%')}</td>
					<td>${w:format(model.report.problemInfo.longServicePercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Long-sql月总次数（大于100ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.longSqls,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.longSqls,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.longSqls,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Long-sql平均每天次数（大于100ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.avgLongSqls,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.avgLongSqls,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.avgLongSqls,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>Long-sql百分比</th>
					<td>${w:format(model.reportLastTwo.problemInfo.longSqlPercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.problemInfo.longSqlPercent,'00.0000%')}</td>
					<td>${w:format(model.report.problemInfo.longSqlPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Long-cache月总次数（大于10ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.longCaches,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.longCaches,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.longCaches,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Long-cache平均每天次数（大于10ms）</th>
					<td>${w:formatNumber(model.reportLastTwo.problemInfo.avgLongCaches,'0.0','')}</td>
					<td>${w:formatNumber(model.reportLast.problemInfo.avgLongCaches,'0.0','')}</td>
					<td>${w:formatNumber(model.report.problemInfo.avgLongCaches,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>Long-cache百分比</th>
					<td>${w:format(model.reportLastTwo.problemInfo.longCachePercent,'00.0000%')}</td>
					<td>${w:format(model.reportLast.problemInfo.longCachePercent,'00.0000%')}</td>
					<td>${w:format(model.report.problemInfo.longCachePercent,'00.0000%')}</td>
				</tr>
			</table>
		</div>
	</body>
</a:body>



