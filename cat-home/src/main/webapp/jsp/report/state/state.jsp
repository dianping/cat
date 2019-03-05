<%@ page contentType="text/html; charset=utf-8"%>
<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.state.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.state.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.state.Model"
	scope="request" />

<a:hourly_report title="CAT State Report" navUrlPrefix="domain=${model.domain}&ip=${model.ipAddress}&show=${payload.show}">
	<jsp:attribute name="subtitle">${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
	<jsp:body>	
<table class="machines">
	<tr style="text-align: left">
		<th>&nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?show=${payload.show}&domain=${model.domain}&date=${model.date}"
								class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?show=${payload.show}&domain=${model.domain}&date=${model.date}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?show=${payload.show}&domain=${model.domain}&ip=${ip}&date=${model.date}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?show=${payload.show}&domain=${model.domain}&ip=${ip}&date=${model.date}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>

<c:if test="${not empty model.message}">
	<h3 class="text-center text-danger">出问题CAT的服务端:${model.message}</h3>
</c:if>
<c:if test="${ empty model.message}">
	<h3 class="text-center text-success">CAT服务端正常</h3>
</c:if>

<table class="table table-hover table-striped table-condensed" width="100%">
	<tr>
		<th width="30%" colspan=2>指标</th>
		<th class="right" width="20%">值</th>
		<th width="50%">备注</th>
	</tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=total" data-status="total" class="state_graph_link">[:: show ::]</a></td>
		<td>处理消息总量</td>
		<td class="right">${w:format(model.state.machine.total,'#,###,###,###,##0.#')}</td>
		<td>服务器接受到消息总量</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="total" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=totalLoss" data-status="totalLoss" class="state_graph_link">[:: show ::]</a></td>
		<td>丢失消息总量</td>
		<c:choose>
			<c:when test="${model.state.machine.totalLoss > 0}"><td  class="right" style="color:red;">${w:format(model.state.machine.totalLoss,'#,###,###,###,##0.#')}</td></c:when>
			<c:otherwise><td class="right">${w:format(model.state.machine.totalLoss,'#,###,###,###,##0.#')}</td></c:otherwise>
		</c:choose>
		<td>服务器进行encode以及analyze处理来不及而丢失消息总量</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="totalLoss" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=avgTps" data-status="avgTps" class="state_graph_link">[:: show ::]</a></td>
		<td>每分钟平均处理数</td>
		<td class="right">${w:format(model.state.machine.avgTps,'###,###,###,##0')}</td>
		<td>平均每分钟处理消息量</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="avgTps" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=maxTps" data-status="maxTps" class="state_graph_link">[:: show ::]</a></td>
		<td>单台机器每分钟最大处理数</td>
		<td class="right">${w:format(model.state.machine.maxTps,'###,###,###,##0')}</td>
		<td>单台机器平均每分钟最大处理消息数目</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="maxTps" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=dump" data-status="dump" class="state_graph_link">[:: show ::]</a></td>
		<td>压缩成功消息数量</td>
		<td class="right">${w:format(model.state.machine.dump,'###,###,###,##0')}</td>
		<td>将消息进行压缩消息数目</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="dump" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=dumpLoss" data-status="dumpLoss" class="state_graph_link">[:: show ::]</a></td>
		<td>来不及压缩丢失消息数量</td>
		<c:choose>
			<c:when test="${model.state.machine.dumpLoss > 0}"><td class="right" style="color:red;">${w:format(model.state.machine.dumpLoss,'#,###,###,###,##0.#')}</td></c:when>
			<c:otherwise><td class="right">${w:format(model.state.machine.dumpLoss,'#,###,###,###,##0.#')}</td></c:otherwise>
		</c:choose>
		<td>将消息进行压缩，线程太忙而丢失消息丢失数目</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="dumpLoss" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=pigeonTimeError" data-status="pigeonTimeError" class="state_graph_link">[:: show ::]</a></td>
		<td>两台机器时钟不准导致消息存储丢失</td>
		<td class="right">${w:format(model.state.machine.pigeonTimeError,'###,###,###,##0')}</td>
		<td>这个场景用于Pigeon，服务端id是由客户端产生，客户端和服务端时钟差2小时，会导致存储丢失</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="pigeonTimeError" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=networkTimeError" data-status="networkTimeError" class="state_graph_link">[:: show ::]</a></td>
		<td>网络传输或者客户端延迟发送导致消息丢失</td>
		<td class="right">${w:format(model.state.machine.networkTimeError,'###,###,###,##0')}</td>
		<td>CAT分小时处理，当一个小时过去了，默认会延迟3分钟结束当前小时，在3分钟后还接受上个小时消息，直接丢弃</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="networkTimeError" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=blockTotal" data-status="blockTotal" class="state_graph_link">[:: show ::]</a></td>
		<td>存储消息块数量</td>
		<td class="right">${w:format(model.state.machine.blockTotal,'###,###,###,##0')}</td>
		<td>CAT是分块存储，消息块成功放入存储队列</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="blockTotal" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=blockLoss" data-status="blockLoss" class="state_graph_link">[:: show ::]</a></td>
		<td>存储消息块丢失数量</td>
		<c:choose>
			<c:when test="${model.state.machine.blockLoss > 0}"><td  class="right" style="color:red;">${w:format(model.state.machine.blockLoss,'#,###,###,###,##0.#')}</td></c:when>
			<c:otherwise><td class="right">${w:format(model.state.machine.blockLoss,'#,###,###,###,##0.#')}</td></c:otherwise>
		</c:choose>
		<td>将存储块写入磁盘的线程太忙，存储队列溢出的消息块数量</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="blockLoss" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=blockTime" data-status="blockTime" class="state_graph_link">[:: show ::]</a></td>
		<td>存储消息块花费时间(分钟)</td>
		<td class="right">${w:format(model.state.machine.blockTime/1000/60,'###,###,###,##0')}</td>
		<td>存储消息花费的CPU时间</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="blockTime" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=size" data-status="size" class="state_graph_link">[:: show ::]</a></td>
		<td>压缩前消息大小(GB)</td>
		<td class="right">${w:format(model.state.machine.size/1024/1024/1024,'0.00#')}</td>
		<td>压缩前所有存储消息的总大小</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="size" style="display:none"></div></td></tr>
	<tr>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=delayAvg" data-status="delayAvg" class="state_graph_link">[:: show ::]</a></td>
		<td>系统处理延迟(ms)</td>
		<td class="right">${w:format(model.state.machine.delayAvg,'0.#')}</td>
		<td>客户端产生消息，到服务端存储之间的时钟误差。（在机器时钟完全准确的情况下）</td>
	</tr>
	<tr></tr>
	<tr class="graphs"><td colspan="4" style="display:none"><div id="delayAvg" style="display:none"></div></td></tr>
</table>
</br>
<c:choose>
<c:when test="${payload.show == true}">
<table class="table table-hover table-striped table-condensed" width="100%">
	<tr>
		<td width="10%"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&sort=domain&show=true">处理项目列表</a></td>
		<td width="10%" class="right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&sort=total&show=true">处理消息总量</a></td>
		<td width="10%" class="right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&sort=loss&show=true">丢失消息总量</a></td>
		<td width="10%" class="right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&sort=size&show=true">压缩前消息大小(GB)</a></td>
		<td width="15%" class="right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&sort=avg&show=true">平均消息大小(KB)</a></td>
		<td width="5%" class="right"><a href="?domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&sort=machine&show=true">机器总数</a></td>
		<td width="45%">项目对应机器列表</td>
	</tr>
	<c:forEach var="item" items="${model.state.processDomains}"
				varStatus="status">
		<tr class="">
			<c:set var="lastIndex" value="${status.index}" />
			<td>${item.name}</td>
			<td style="text-align:right;">${w:format(item.total,'#,###,###,###,##0.#')}
			</br>
			<a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=${item.name}:total" data-status="${item.name}:total" class="state_graph_link">[:: show ::]</a>&nbsp;&nbsp;&nbsp;&nbsp;</td>
			<td style="text-align:right;">${w:format(item.totalLoss,'#,###,###,###,##0.#')}
			</br>
			<a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=${item.name}:totalLoss" data-status="${item.name}:totalLoss" class="state_graph_link">[:: show ::]</a></td>
			<td style="text-align:right;">${w:format(item.size/1024/1024/1024,'#,###,##0.000')}
			</br>
			<a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=${item.name}:size" data-status="${item.name}:size" class="state_graph_link">[:: show ::]</a></td>
			<td style="text-align:right;">${w:format(item.avg/1024,'#,###,##0.000')}</td>
			<td style="text-align:center;">${w:size(item.ips)}</td>
			<td style="white-space:normal">${item.ips}</td>
		</tr>
		<tr class="graphs"><td colspan="7"  style="display:none"><div id="${item.name}:total" style="display:none"></div></td></tr>
		<tr class="graphs"><td colspan="7"  style="display:none"><div id="${item.name}:totalLoss" style="display:none"></div></td></tr>
		<tr class="graphs"><td colspan="7"  style="display:none"><div id="${item.name}:size" style="display:none"></div></td></tr>
		<tr></tr>
	</c:forEach>
	<tr style="color: white;">
		<td>${lastIndex+1}</td>
		<td></td>
		<td></td>
		<td></td>
		<td></td>
		<td>${model.state.totalSize}</td>
	</tr>
</table></c:when></c:choose>
	<res:useJs value="${res.js.local['state_js']}" target="bottom-js" />
</jsp:body>
</a:hourly_report>

<script type="text/javascript" src="/cat/js/appendHostname.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		appendHostname(${model.ipToHostnameStr});
		$("#warp_search_group").hide();
	});
</script>

