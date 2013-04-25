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

<a:report title="CAT State Report" navUrlPrefix="domain=${model.domain}&ip=${model.ipAddress}">
	<jsp:attribute name="subtitle">From ${w:format(model.report.startTime,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.report.endTime,'yyyy-MM-dd HH:mm:ss')}  &nbsp;&nbsp;&nbsp;&nbsp;CAT项目指标</jsp:attribute>
	<jsp:body>	
	<res:useCss value="${res.css.local.matrix_css}" target="head-css" />
	<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
	<res:useJs value="${res.js.local['flotr2_js']}" target="head-js" />
	<res:useJs value="${res.js.local['trendStateGraph_js']}" target="head-js" />
<br>

<table class="machines">
	<tr style="text-align: left">
		<th>Machines: &nbsp;[&nbsp; <c:choose>
				<c:when test="${model.ipAddress eq 'All'}">
					<a href="?domain=${model.domain}&date=${model.date}"
								class="current">All</a>
				</c:when>
				<c:otherwise>
					<a href="?domain=${model.domain}&date=${model.date}">All</a>
				</c:otherwise>
			</c:choose> &nbsp;]&nbsp; <c:forEach var="ip" items="${model.ips}">
   	  		&nbsp;[&nbsp;
   	  		<c:choose>
					<c:when test="${model.ipAddress eq ip}">
						<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}"
									class="current">${ip}</a>
					</c:when>
					<c:otherwise>
						<a href="?domain=${model.domain}&ip=${ip}&date=${model.date}">${ip}</a>
					</c:otherwise>
				</c:choose>
   	 		&nbsp;]&nbsp;
			 </c:forEach>
		</th>
	</tr>
</table>
</br>

<table width="80%">
	<tr>
		<td width="5%"></td>
		<td width="30%">指标</td>
		<td width="20%">值</td>
		<td width="45%">备注</td>
	</tr>
	<tr class='odd'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=total" data-status="total" class="graph_link">[::show::]</a></td>
		<td>处理消息总量</td>
		<td>${w:format(model.state.total.total,'#,###,###,###,##0.#')}</td>
		<td>服务器接受到消息总量</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="total" style="display:none"></div></td></tr>
	<tr class='even'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=totalLoss" data-status="totalLoss" class="graph_link">[::show::]</a></td>
		<td>丢失消息总量</td>
		<c:choose>
			<c:when test="${model.state.total.totalLoss > 0}"><td style="color:red;">${w:format(model.state.total.totalLoss,'#,###,###,###,##0.#')}</td></c:when>
			<c:otherwise><td>${w:format(model.state.total.totalLoss,'#,###,###,###,##0.#')}</td></c:otherwise>
		</c:choose>
		<td>服务器进行encode以及analyze处理来不及而丢失消息总量</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="totalLoss" style="display:none"></div></td></tr>
	<tr class='odd'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=avgTps" data-status="avgTps" class="graph_link">[::show::]</a></td>
		<td>每分钟平均处理数</td>
		<td>${w:format(model.state.total.avgTps,'###,###,###,##0')}</td>
		<td>平均每分钟处理消息量</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="avgTps" style="display:none"></div></td></tr>
	<tr class='even'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=maxTps" data-status="maxTps" class="graph_link">[::show::]</a></td>
		<td>单台机器每分钟最大处理数</td>
		<td>${w:format(model.state.total.maxTps,'###,###,###,##0')}</td>
		<td>单台机器平均每分钟最大处理消息数目</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="maxTps" style="display:none"></div></td></tr>
	<tr class='odd'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=dump" data-status="dump" class="graph_link">[::show::]</a></td>
		<td>gzip压缩成功消息数量</td>
		<td>${w:format(model.state.total.dump,'###,###,###,##0')}</td>
		<td>将消息进行gzip压缩消息数目</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="dump" style="display:none"></div></td></tr>
	<tr class='even'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=dumpLoss" data-status="dumpLoss" class="graph_link">[::show::]</a></td>
		<td>gzip来不及压缩丢失消息数量</td>
		<c:choose>
			<c:when test="${model.state.total.dumpLoss > 0}"><td style="color:red;">${w:format(model.state.total.dumpLoss,'#,###,###,###,##0.#')}</td></c:when>
			<c:otherwise><td>${w:format(model.state.total.dumpLoss,'#,###,###,###,##0.#')}</td></c:otherwise>
		</c:choose>
		<td>将消息进行gzip压缩，gzip线程太忙而丢失消息丢失数目</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="dumpLoss" style="display:none"></div></td></tr>
	<tr class='odd'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=pigeonTimeError" data-status="pigeonTimeError" class="graph_link">[::show::]</a></td>
		<td>两台机器时钟不准导致消息存储丢失</td>
		<td>${w:format(model.state.total.pigeonTimeError,'###,###,###,##0')}</td>
		<td>这个场景用于Pigeon，服务端id是由客户端产生，客户端和服务端时钟差2小时，会导致存储丢失</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="pigeonTimeError" style="display:none"></div></td></tr>
	<tr class='even'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=networkTimeError" data-status="networkTimeError" class="graph_link">[::show::]</a></td>
		<td>网络传输或者客户端延迟发送导致消息丢失</td>
		<td>${w:format(model.state.total.networkTimeError,'###,###,###,##0')}</td>
		<td>CAT分小时处理，当一个小时过去了，默认会延迟3分钟结束当前小时，在3分钟后还接受上个小时消息，直接丢弃</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="networkTimeError" style="display:none"></div></td></tr>
	<tr class='odd'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=blockTotal" data-status="blockTotal" class="graph_link">[::show::]</a></td>
		<td>存储消息块数量</td>
		<td>${w:format(model.state.total.blockTotal,'###,###,###,##0')}</td>
		<td>CAT是分块存储，消息块成功放入存储队列</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="blockTotal" style="display:none"></div></td></tr>
	<tr class='even'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=blockLoss" data-status="blockLoss" class="graph_link">[::show::]</a></td>
		<td>存储消息块丢失数量</td>
		<td>${w:format(model.state.total.blockLoss,'###,###,###,##0')}</td>
		<td>将存储块写入磁盘的线程太忙，存储队列溢出的消息块数量</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="blockLoss" style="display:none"></div></td></tr>
	<tr class='odd'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=blockTime" data-status="blockTime" class="graph_link">[::show::]</a></td>
		<td>存储消息块花费时间(分钟)</td>
		<td>${w:format(model.state.total.blockTime/1000/60,'###,###,###,##0')}</td>
		<td>存储消息花费的CPU时间</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="blockTime" style="display:none"></div></td></tr>
	<tr class='even'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=size" data-status="size" class="graph_link">[::show::]</a></td>
		<td>压缩前消息大小(GB)</td>
		<td>${w:format(model.state.total.size/1024/1024/1024,'0.00#')}</td>
		<td>压缩前所有存储消息的总大小</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="size" style="display:none"></div></td></tr>
	<tr class='odd'>
		<td><a href="?op=graph&ip=${model.ipAddress}&date=${model.date}&key=delayAvg" data-status="delayAvg" class="graph_link">[::show::]</a></td>
		<td>系统处理延迟(ms)</td>
		<td>${w:format(model.state.total.delayAvg,'0.#')}</td>
		<td>客户端产生消息，到服务端存储之间的时钟误差。（在机器时钟完全准确的情况下）</td>
	</tr>
	<tr class="graphs"><td colspan="4"><div id="delayAvg" style="display:none"></div></td></tr>
</table>
</br>
<table width="100%">
	<tr class='odd'>
		<td width="15%">处理项目列表</td>
				<td width="15%">机器总数</td>
				<td>项目对应机器列表</td>
	</tr>
	<c:forEach var="item" items="${model.state.processDomains}"
				varStatus="status">
		<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
			<c:set var="lastIndex" value="${status.index}" />
			<td>${item.name}</td>
			<td>${w:size(item.ips)}</td>
			<td style="white-space: normal">${item.ips}</td>
		</tr>
	</c:forEach>
	<tr style="color: white;">
				<td>${lastIndex+1}</td>
				<td>${model.state.totalSize}</td>
			</tr>
</table>
<br>
	<res:useJs value="${res.js.local['state_js']}" target="bottom-js" />
</jsp:body>
</a:report>

<script type="text/javascript">
	$(document).ready(function() {
		$('.position').hide();
	});
</script>

