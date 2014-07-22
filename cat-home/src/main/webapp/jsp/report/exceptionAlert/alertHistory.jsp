<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.statistics.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.statistics.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.statistics.Model" scope="request"/>

<a:body>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<script type="text/javascript">
	$(document).ready(function() {
		$('#alert').addClass('active');
		
		$(document).delegate('.detail', 'click', function(e){
			var anchor = this,
				el = $(anchor);
			
			if(e.ctrlKey || e.metaKey){
				return true;
			}else{
				e.preventDefault();
			}
			$.ajax({
				type: "get",
				url: anchor.href,
				success : function(response, textStatus) {
					$('#myModal .modal-body').html(response);
					$('#myModal').modal().css({
					    width: 'auto',
					    'margin-left': function () {
					        return -($(this).width() / 2);
					    }
					});
				}
			});
		});
	});
</script>
<div class="report">
	<table class="header">
		<tr>
			<td class="title"><span class="text-success"><span class="text-error">【报表时间】</span><span class="text-success">&nbsp;&nbsp;From ${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</span></td>
			<td class="nav">
					<a class="switch" href="?domain=${model.domain}&op=alert"><span class="text-error">【切到小时模式】</span></a>
					<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq model.reportType}">
								&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]
						</c:when>
						<c:otherwise>
								&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]&nbsp;&nbsp;
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=-1">${model.currentNav.last}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=1">${model.currentNav.next}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&reportType=${model.reportType}&nav=next">now</a> ]&nbsp;&nbsp;
			</td>
		</tr>
	</table>
	<div class="row-fluid">
    <div class="span2">
		<%@include file="../bugTree.jsp"%>
	</div>
	<div class="span10">
		<div class="report">
			<%@ include file="detailHistory.jsp"%>
		</div>
	</div>
</div>
</div>
</a:body>
