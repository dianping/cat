<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.health.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.health.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.health.Model" scope="request" />

<a:report title="Health Report" navUrlPrefix="domain=${model.domain}">
	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>	
<br>
	<res:useJs value="${res.js.local['svgchart.latest.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>


<div style="float: left;width:100%">
	<div style="width: 33%; float: left;">
				<table class="health">
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				<tr class="odd">
					<th>URL平均响应时间</th>
					<td>${w:formatNumber(model.report.url.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>URL总访问量</th>
					<td>${w:formatNumber(model.report.url.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>URL出错次数</th>
					<td>${w:formatNumber(model.report.url.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>URL成功百分比</th>
					<td>${w:format(model.report.url.baseInfo.successPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>(服务端看)Service平均响应时间</th>
					<td>${w:formatNumber(model.report.service.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>(服务端看)Service总访问量</th>
					<td>${w:formatNumber(model.report.service.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>(服务端看)Service出错次数</th>
					<td>${w:formatNumber(model.report.service.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>(服务端看)Service成功百分比</th>
					<td>${w:format(model.report.service.baseInfo.successPercent,'00.0000%')}</td>
				</tr>
				
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>(客户端看)Service平均响应时间</th>
					<td>${w:formatNumber(model.report.clientService.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>(客户端看)Service总访问量</th>
					<td>${w:formatNumber(model.report.clientService.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>(客户端看)Service出错次数</th>
					<td>${w:formatNumber(model.report.clientService.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>(客户端看)Service成功百分比</th>
					<td>${w:format(model.report.clientService.baseInfo.successPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>远程调用平均响应时间</th>
					<td>${w:formatNumber(model.report.call.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>远程调用总访问量</th>
					<td>${w:formatNumber(model.report.call.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>远程调用出错次数</th>
					<td>${w:formatNumber(model.report.call.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>远程调用成功百分比</th>
					<td>${w:format(model.report.call.baseInfo.successPercent,'00.0000%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>数据库平均响应时间</th>
					<td>${w:formatNumber(model.report.sql.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>数据库总访问量</th>
					<td>${w:formatNumber(model.report.sql.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>数据库出错次数</th>
					<td>${w:formatNumber(model.report.sql.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>数据库成功百分比</th>
					<td>${w:format(model.report.sql.baseInfo.successPercent,'00.0000%')}</td>
				</tr>
				</table>
			</div>
	<div style="width: 33%; float:left">
				<table class="health">
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				<tr class="odd">
					<th>Memcached缓存平均响应时间</th>
					<td>${w:formatNumber(model.report.memCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>Memcached缓存总访问量</th>
					<td>${w:formatNumber(model.report.memCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>Memcached缓存命中率</th>
					<td>${w:format(model.report.memCache.baseCacheInfo.hitPercent,'00.0%')}</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr class="odd">
					<th>KvdbCache缓存平均响应时间</th>
					<td>${w:formatNumber(model.report.kvdbCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>KvdbCache缓存总访问量</th>
					<td>${w:formatNumber(model.report.kvdbCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>KvdbCache缓存命中率</th>
					<td>${w:format(model.report.kvdbCache.baseCacheInfo.hitPercent,'00.0%')}</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr class="odd">
					<th>WebCache缓存平均响应时间</th>
					<td>${w:formatNumber(model.report.webCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="even">
					<th>WebCache缓存总访问量</th>
					<td>${w:formatNumber(model.report.webCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>WebCache缓存命中率</th>
					<td>${w:format(model.report.webCache.baseCacheInfo.hitPercent,'00.0%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Exception异常数</th>
					<td>${w:formatNumber(model.report.problemInfo.exceptions,'0.0','')}</td>
				</tr>

				<tr class="even">
					<th>Long-url总次数（>1000ms）</th>
					<td>${w:formatNumber(model.report.problemInfo.longUrls,'0.0','')}</td>
				</tr>
				<tr class="odd">
					<th>Long-url百分比</th>
					<td>${w:format(model.report.problemInfo.longUrlPercent,'00.0%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Long-service总次数（>50ms）</th>
					<td>${w:formatNumber(model.report.problemInfo.longServices,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Long-service百分比</th>
					<td>${w:format(model.report.problemInfo.longServicePercent,'00.0%')}</td>
				</tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<th>Long-sql总次数（>100ms）</th>
					<td>${w:formatNumber(model.report.problemInfo.longSqls,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Long-sql百分比</th>
					<td>${w:format(model.report.problemInfo.longSqlPercent,'00.0%')}</td>
				</tr>

				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>

				<tr class="odd">
					<th>Long-cache总次数（>10ms）</th>
					<td>${w:formatNumber(model.report.problemInfo.longCaches,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>Long-cache百分比</th>
					<td>${w:format(model.report.problemInfo.longCachePercent,'00.0%')}</td>
				</tr>
			</table>
			</div>
			<div style="width: 33%; float:left">
				<table class="health">
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				<tr class="odd">
					<th>部署数量</th>
					<td>${model.report.machineInfo.numbers}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<th>平均负载</th>
					<td>${w:formatNumber(model.report.machineInfo.avgLoad,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>最大负载</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxLoad,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>最大负载IP</th>
					<td>${model.report.machineInfo.avgMaxLoadMachine}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<th>平均每分钟OldGc</th>
					<td>${w:formatNumber(model.report.machineInfo.avgOldgc,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>平均最大每分钟OldGc</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxOldgc,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>最大OldGcIP</th>
					<td>${model.report.machineInfo.avgMaxOldgcMachine}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<th>平均HTTP线程</th>
					<td>${w:formatNumber(model.report.machineInfo.avgHttp,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>平均最大HTTP线程</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxHttp,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>最大HTTPIP</th>
					<td>${model.report.machineInfo.avgMaxHttpMachine}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<th>平均Pigeon线程</th>
					<td>${w:formatNumber(model.report.machineInfo.avgPigeon,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>平均最大Pigeon线程</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxPigeon,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>最大PigeonIP</th>
					<td>${model.report.machineInfo.avgMaxPigeonMachine}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<th>平均内存使用（MB）</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMemoryUsed,'0.0','')}</td>
				</tr>
				<tr class="even">
					<th>平均最大内存使用（MB）</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxMemoryUsed,'0.0','')}</td>
				</tr><tr class="even">
					<th>最大内存使用IP</th>
					<td>${model.report.machineInfo.avgMaxMemoryUsedMachine}</td>
				</tr>
			</table>
			</div>
</div>

</jsp:body>
</a:report>

