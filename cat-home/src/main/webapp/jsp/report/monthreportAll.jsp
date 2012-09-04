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

			<table class="monthreport">
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr class="odd">
					<th>项目名称</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<th>${item.domain}</th>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>统计天数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${item.day}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>URL请求平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.baseInfo.responseTime,'0.0','ms')}</td>
					</c:forEach>
					<%-- <td background="/cat/images/${item.url.baseInfo.responseTimeFlag}.jpeg">
                  <img width="15px" height="15px" src="/cat/images/${item.url.baseInfo.responseTimeFlag}.jpeg"></img>
               </td> --%>
				</tr>
				<tr class="even">
					<th>URL请求月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.baseInfo.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>URL请求平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.baseInfo.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>URL请求月出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.baseInfo.errorTotal,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>URL请求每天平均出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.url.baseInfo.errorAvg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>URL请求成功百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.url.baseInfo.successPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Service请求平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.baseInfo.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Service请求月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.baseInfo.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Service请求平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.baseInfo.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Service请求月出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.baseInfo.errorTotal,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Service请求每天平均出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.service.baseInfo.errorAvg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Service请求成功百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.service.baseInfo.successPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>远程调用平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.baseInfo.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>远程调用月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.baseInfo.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>远程调用平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.baseInfo.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>远程调用月出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.baseInfo.errorTotal,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>远程调用每天平均出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.call.baseInfo.errorAvg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>远程调用成功百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.call.baseInfo.successPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>数据库平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.baseInfo.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>数据库月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.baseInfo.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>数据库平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.baseInfo.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>数据库月出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.baseInfo.errorTotal,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>数据库每天平均出错次数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.sql.baseInfo.errorAvg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>数据库成功百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.sql.baseInfo.successPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>memcached缓存平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.memCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>memcached缓存月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.memCache.baseCacheInfo.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>memcached缓存平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.memCache.baseCacheInfo.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>memcached缓存命中率</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.memCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr class="odd">
					<th>kvdbCache缓存平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.kvdbCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>kvdbCache缓存月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.kvdbCache.baseCacheInfo.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>kvdbCache缓存平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.kvdbCache.baseCacheInfo.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>kvdbCache缓存命中率</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.kvdbCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr class="odd">
					<th>webCache缓存平均响应时间（ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.webCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>webCache缓存月总访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.webCache.baseCacheInfo.total,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>webCache缓存平均每天访问量</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.webCache.baseCacheInfo.avg,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>webCache缓存命中率</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.webCache.baseCacheInfo.hitPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Exception月异常数</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.exceptions,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Exception平均每天异常数目</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.avgExceptions,'0.0','')}</td>
					</c:forEach>
				</tr>

				<tr class="odd">
					<th>Long-url月总次数（大于1000ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.longUrls,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Long-url平均每天次数（大于100ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.avgLongUrls,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Long-url百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.problemInfo.longUrlPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Long-service月总次数（大于1000ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.longServices,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Long-service平均每天次数（大于100ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.avgLongServices,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Long-service百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.problemInfo.longServicePercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Long-sql月总次数（大于100ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.longSqls,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Long-sql平均每天次数（大于100ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.avgLongSqls,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Long-sql百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.problemInfo.longSqlPercent,'00.0000%')}</td>
					</c:forEach>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Long-cache月总次数（大于10ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.longCaches,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="even">
					<th>Long-cache平均每天次数（大于10ms）</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:formatNumber(item.problemInfo.avgLongCaches,'0.0','')}</td>
					</c:forEach>
				</tr>
				<tr class="odd">
					<th>Long-cache百分比</th>
					<c:forEach var="item" items="${model.reports}" varStatus="status">
						<td>${w:format(item.problemInfo.longCachePercent,'00.0000%')}</td>
					</c:forEach>
				</tr>
			</table>

		</div>
	</body>
</a:body>

