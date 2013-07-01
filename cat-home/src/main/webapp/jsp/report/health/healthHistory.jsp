<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.health.Context" scope="request" />
<jsp:useBean id="payload"type="com.dianping.cat.report.page.health.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.health.Model" scope="request" />

<a:historyReport title="Health Report" navUrlPrefix="">
	<jsp:attribute name="subtitle">From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>	
	<res:useJs value="${res.js.local['svgchart.latest.min.js']}" target="head-js"/>
	<res:useJs value="${res.js.local['baseGraph.js']}" target="head-js"/>
<br>
<div style="float: left;width:100%">
	<div style="width: 33%; float: left;">
				<table class="health">
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=UrlResponseTime" class="health_history_graph_link" data-status="UrlResponseTime">[:: show ::]</a></td>
					<th>URL平均响应时间</th>
					<td>${w:formatNumber(model.report.url.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="UrlResponseTime" style="display:none"></div></td></tr>
				<tr>
				</tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=UrlTotal" class="health_history_graph_link" data-status="UrlTotal">[:: show ::]</a></td>
					<th>URL总访问量</th>
					<td>${w:formatNumber(model.report.url.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="UrlTotal" style="display:none"></div></td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=UrlErrorTotal" class="health_history_graph_link" data-status="UrlErrorTotal">[:: show ::]</a></td>
					<th>URL出错次数</th>
					<td>${w:formatNumber(model.report.url.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="UrlErrorTotal" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=UrlSuccessPercent" class="health_history_graph_link" data-status="UrlSuccessPercent">[:: show ::]</a></td>
					<th>URL成功百分比</th>
					<td>${w:format(model.report.url.baseInfo.successPercent,'00.0000%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="UrlSuccessPercent" style="display:none"></div></td></tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=ServiceResponseTime" class="health_history_graph_link" data-status="ServiceResponseTime">[:: show ::]</a></td>
					<th>(服务端看)Service平均响应时间</th>
					<td>${w:formatNumber(model.report.service.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="ServiceResponseTime" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=ServiceTotal" class="health_history_graph_link" data-status="ServiceTotal">[:: show ::]</a></td>
					<th>(服务端看)Service总访问量</th>
					<td>${w:formatNumber(model.report.service.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="ServiceTotal" style="display:none"></div></td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=ServiceErrorTotal" class="health_history_graph_link" data-status="ServiceErrorTotal">[:: show ::]</a></td>
					<th>(服务端看)Service出错次数</th>
					<td>${w:formatNumber(model.report.service.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="ServiceErrorTotal" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=ServiceSuccessPercent" class="health_history_graph_link" data-status="ServiceSuccessPercent">[:: show ::]</a></td>
					<th>(服务端看)Service成功百分比</th>
					<td>${w:format(model.report.service.baseInfo.successPercent,'00.0000%')}</td>
				</tr><tr class="graphs"><td colspan="5"><div id="ServiceSuccessPercent" style="display:none"></div></td></tr>
				
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=ClientServiceResponseTime" class="health_history_graph_link" data-status="ClientServiceResponseTime">[:: show ::]</a></td>
					<th>(客户端看)Service平均响应时间</th>
					<td>${w:formatNumber(model.report.clientService.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="ClientServiceResponseTime" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=ClientServiceTotal" class="health_history_graph_link" data-status="ClientServiceTotal">[:: show ::]</a></td>
					<th>(客户端看)Service总访问量</th>
					<td>${w:formatNumber(model.report.clientService.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="ClientServiceTotal" style="display:none"></div></td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=ClientServiceErrorTotal" class="health_history_graph_link" data-status="ClientServiceErrorTotal">[:: show ::]</a></td>
					<th>(客户端看)Service出错次数</th>
					<td>${w:formatNumber(model.report.clientService.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="ClientServiceErrorTotal" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=ClientServiceSuccessPercent" class="health_history_graph_link" data-status="ClientServiceSuccessPercent">[:: show ::]</a></td>
					<th>(客户端看)Service成功百分比</th>
					<td>${w:format(model.report.clientService.baseInfo.successPercent,'00.0000%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="ClientServiceSuccessPercent" style="display:none"></div></td></tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=CallResponseTime" class="health_history_graph_link" data-status="CallResponseTime">[:: show ::]</a></td>
					<th>远程调用平均响应时间</th>
					<td>${w:formatNumber(model.report.call.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="CallResponseTime" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=CallTotal" class="health_history_graph_link" data-status="CallTotal">[:: show ::]</a></td>
					<th>远程调用总访问量</th>
					<td>${w:formatNumber(model.report.call.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="CallTotal" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=CallErrorTotal" class="health_history_graph_link" data-status="CallErrorTotal">[:: show ::]</a></td>
					<th>远程调用出错次数</th>
					<td>${w:formatNumber(model.report.call.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="CallErrorTotal" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=CallSuccessPercent" class="health_history_graph_link" data-status="CallSuccessPercent">[:: show ::]</a></td>
					<th>远程调用成功百分比</th>
					<td>${w:format(model.report.call.baseInfo.successPercent,'00.0000%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="CallSuccessPercent" style="display:none"></div></td></tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=SqlResponseTime" class="health_history_graph_link" data-status="SqlResponseTime">[:: show ::]</a></td>
					<th>数据库平均响应时间</th>
					<td>${w:formatNumber(model.report.sql.baseInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="SqlResponseTime" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=SqlTotal" class="health_history_graph_link" data-status="SqlTotal">[:: show ::]</a></td>
					<th>数据库总访问量</th>
					<td>${w:formatNumber(model.report.sql.baseInfo.total,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="SqlTotal" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=SqlErrorTotal" class="health_history_graph_link" data-status="SqlErrorTotal">[:: show ::]</a></td>
					<th>数据库出错次数</th>
					<td>${w:formatNumber(model.report.sql.baseInfo.errorTotal,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="SqlErrorTotal" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=SqlSuccessPercent" class="health_history_graph_link" data-status="SqlSuccessPercent">[:: show ::]</a></td>
					<th>数据库成功百分比</th>
					<td>${w:format(model.report.sql.baseInfo.successPercent,'00.0000%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="SqlSuccessPercent" style="display:none"></div></td></tr>
				</table>
			</div>
			<div style="width: 33%; float:left">
				<table class="health">
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MemResponseTime" class="health_history_graph_link" data-status="MemResponseTime">[:: show ::]</a></td>
					<th>Memcached缓存平均响应时间</th>
					<td>${w:formatNumber(model.report.memCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MemResponseTime" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MemTotal" class="health_history_graph_link" data-status="MemTotal">[:: show ::]</a></td>
					<th>Memcached缓存总访问量</th>
					<td>${w:formatNumber(model.report.memCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MemTotal" style="display:none"></div></td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MemHitPercent" class="health_history_graph_link" data-status="MemHitPercent">[:: show ::]</a></td>
					<th>Memcached缓存命中率</th>
					<td>${w:format(model.report.memCache.baseCacheInfo.hitPercent,'00.0%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MemHitPercent" style="display:none"></div></td></tr>
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=KvdbResponseTime" class="health_history_graph_link" data-status="KvdbResponseTime">[:: show ::]</a></td>
					<th>KvdbCache缓存平均响应时间</th>
					<td>${w:formatNumber(model.report.kvdbCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="KvdbResponseTime" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=KvdbTotal" class="health_history_graph_link" data-status="KvdbTotal">[:: show ::]</a></td>
					<th>KvdbCache缓存总访问量</th>
					<td>${w:formatNumber(model.report.kvdbCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="KvdbTotal" style="display:none"></div></td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=KvdbHitPercent" class="health_history_graph_link" data-status="KvdbHitPercent">[:: show ::]</a></td>
					<th>KvdbCache缓存命中率</th>
					<td>${w:format(model.report.kvdbCache.baseCacheInfo.hitPercent,'00.0%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="KvdbHitPercent" style="display:none"></div></td></tr>
				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=WebResponseTime" class="health_history_graph_link" data-status="WebResponseTime">[:: show ::]</a></td>
					<th>WebCache缓存平均响应时间</th>
					<td>${w:formatNumber(model.report.webCache.baseCacheInfo.responseTime,'0.0','ms')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="WebResponseTime" style="display:none"></div></td></tr>
				<tr class="even">
				<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=WebTotal" class="health_history_graph_link" data-status="WebTotal">[:: show ::]</a></td>
					<th>WebCache缓存总访问量</th>
					<td>${w:formatNumber(model.report.webCache.baseCacheInfo.total,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="WebTotal" style="display:none"></div></td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=WebHitPercent" class="health_history_graph_link" data-status="WebHitPercent">[:: show ::]</a></td>
					<th>WebCache缓存命中率</th>
					<td>${w:format(model.report.webCache.baseCacheInfo.hitPercent,'00.0%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="WebHitPercent" style="display:none"></div></td></tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=Exceptions" class="health_history_graph_link" data-status="Exceptions">[:: show ::]</a></td>
					<th>Exception异常数</th>
					<td>${w:formatNumber(model.report.problemInfo.exceptions,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="Exceptions" style="display:none"></div></td></tr>
				
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=LongUrls" class="health_history_graph_link" data-status="LongUrls">[:: show ::]</a></td>
					<th>Long-url总次数（>1000ms）</th>
					<td>${w:formatNumber(model.report.problemInfo.longUrls,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="LongUrls" style="display:none"></div></td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=LongUrlPercent" class="health_history_graph_link" data-status="LongUrlPercent">[:: show ::]</a></td>
					<th>Long-url百分比</th>
					<td>${w:format(model.report.problemInfo.longUrlPercent,'00.0%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="LongUrlPercent" style="display:none"></div></td></tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=LongServices" class="health_history_graph_link" data-status="LongServices">[:: show ::]</a></td>
					<th>Long-service总次数（>50ms）</th>
					<td>${w:formatNumber(model.report.problemInfo.longServices,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="LongServices" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=LongServicePercent" class="health_history_graph_link" data-status="LongServicePercent">[:: show ::]</a></td>
					<th>Long-service百分比</th>
					<td>${w:format(model.report.problemInfo.longServicePercent,'00.0%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="LongServicePercent" style="display:none"></div></td></tr>

				<tr>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>

				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=LongSqls" class="health_history_graph_link" data-status="LongSqls">[:: show ::]</a></td>
					<th>Long-sql总次数（>100ms）</th>
					<td>${w:formatNumber(model.report.problemInfo.longSqls,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="LongSqls" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=LongSqlPercent" class="health_history_graph_link" data-status="LongSqlPercent">[:: show ::]</a></td>
					<th>Long-sql百分比</th>
					<td>${w:format(model.report.problemInfo.longSqlPercent,'00.0%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="LongSqlPercent" style="display:none"></div></td></tr>

				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>

				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=LongCaches" class="health_history_graph_link" data-status="LongCaches">[:: show ::]</a></td>
					<th>Long-cache总次数（>10ms）</th>
					<td>${w:formatNumber(model.report.problemInfo.longCaches,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="LongCaches" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=LongCachePercent" class="health_history_graph_link" data-status="LongCachePercent">[:: show ::]</a></td>
					<th>Long-cache百分比</th>
					<td>${w:format(model.report.problemInfo.longCachePercent,'00.0%')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="LongCachePercent" style="display:none"></div></td></tr>
			</table>
			</div>
			<div style="width: 33%; float:left">
				<table class="health">
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineNumbers" class="health_history_graph_link" data-status="MahineNumbers">[:: show ::]</a></td>
					<th>部署数量</th>
					<td>${model.report.machineInfo.numbers}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineNumbers" style="display:none"></div></td></tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineAvgLoad" class="health_history_graph_link" data-status="MahineAvgLoad">[:: show ::]</a></td>
					<th>平均负载</th>
					<td>${w:formatNumber(model.report.machineInfo.avgLoad,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineAvgLoad" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineAvgMaxLoad" class="health_history_graph_link" data-status="MahineAvgMaxLoad">[:: show ::]</a></td>
					<th>最大负载</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxLoad,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineAvgMaxLoad" style="display:none"></div></td></tr>
				<tr class="even">
					<td></td>
					<th>最大负载IP</th>
					<td>${model.report.machineInfo.avgMaxLoadMachine}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineAvgOldgc" class="health_history_graph_link" data-status="MahineAvgOldgc">[:: show ::]</a></td>
					<th>平均每分钟OldGc</th>
					<td>${w:formatNumber(model.report.machineInfo.avgOldgc,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineAvgOldgc" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineAvgMaxOldgc" class="health_history_graph_link" data-status="MahineAvgMaxOldgc">[:: show ::]</a></td>
					<th>平均最大每分钟OldGc</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxOldgc,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineAvgMaxOldgc" style="display:none"></div></td></tr>
				<tr class="even">
					<td></td>
					<th>最大OldGcIP</th>
					<td>${model.report.machineInfo.avgMaxOldgcMachine}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineAvgHttp" class="health_history_graph_link" data-status="MahineAvgHttp">[:: show ::]</a></td>
					<th>平均HTTP线程</th>
					<td>${w:formatNumber(model.report.machineInfo.avgHttp,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineAvgHttp" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineAvgMaxHttp" class="health_history_graph_link" data-status="MahineAvgMaxHttp">[:: show ::]</a></td>
					<th>平均最大HTTP线程</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxHttp,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineAvgMaxHttp" style="display:none"></div></td></tr>
				<tr class="even">
					<td></td>
					<th>最大HTTPIP</th>
					<td>${model.report.machineInfo.avgMaxHttpMachine}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineAvgPigeon" class="health_history_graph_link" data-status="MahineAvgPigeon">[:: show ::]</a></td>
					<th>平均Pigeon线程</th>
					<td>${w:formatNumber(model.report.machineInfo.avgPigeon,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineAvgPigeon" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineAvgMaxPigeon" class="health_history_graph_link" data-status="MahineAvgMaxPigeon">[:: show ::]</a></td>
					<th>平均最大Pigeon线程</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxPigeon,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineAvgMaxPigeon" style="display:none"></div></td></tr>
				<tr class="even">
					<td></td>
					<th>最大PigeonIP</th>
					<td>${model.report.machineInfo.avgMaxPigeonMachine}</td>
				</tr>
				<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
				
				<tr class="odd">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineMemoryUsed" class="health_history_graph_link" data-status="MahineMemoryUsed">[:: show ::]</a></td>
					<th>平均内存使用（MB）</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMemoryUsed,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineMemoryUsed" style="display:none"></div></td></tr>
				<tr class="even">
					<td><a href="?op=historyGraph&domain=${model.domain}&reportType=${payload.reportType}&date=${model.date}&key=MahineMaxMemoryUsed" class="health_history_graph_link" data-status="MahineMaxMemoryUsed">[:: show ::]</a></td>
					<th>平均最大内存使用（MB）</th>
					<td>${w:formatNumber(model.report.machineInfo.avgMaxMemoryUsed,'0.0','')}</td>
				</tr>
				<tr class="graphs"><td colspan="5"><div id="MahineMaxMemoryUsed" style="display:none"></div></td></tr>
				<tr class="even">
					<td></td>
					<th>最大内存使用IP</th>
					<td>${model.report.machineInfo.avgMaxMemoryUsedMachine}</td>
				</tr>
			</table>
			</div>
</div>

<res:useJs value="${res.js.local.health_js}" target="bottom-js" />
</jsp:body>
</a:historyReport>

