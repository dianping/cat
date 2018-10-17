<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.statistics.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.statistics.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.statistics.Model" scope="request"/>

<a:application>
<res:useCss value='${res.css.local.table_css}' target="head-css" />
<script type="text/javascript">
	$(document).ready(function() {
		$('#Offline_report').addClass('active open');
		$('#alert_report').addClass('active');
		
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
	<div class="breadcrumbs" id="breadcrumbs">
		<script type="text/javascript">
			try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
		</script>
		<span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${w:format(payload.historyStartDate,'yyyy-MM-dd HH:mm:ss')} to ${w:format(payload.historyDisplayEndDate,'yyyy-MM-dd HH:mm:ss')}</span>
		<div class="nav-search nav" id="nav-search">
			<a class="switch" href="?domain=${model.domain}&op=alert"><span class="text-danger">【切到小时模式】</span></a>
					<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq payload.reportType}">
								&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]
						</c:when>
						<c:otherwise>
								&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]&nbsp;&nbsp;
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&step=-1">${model.currentNav.last}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&step=1">${model.currentNav.next}</a> ]&nbsp;&nbsp;
				&nbsp;&nbsp;[ <a href="?op=historyAlert&domain=${model.domain}&ip=${model.ipAddress}&reportType=${payload.reportType}&nav=next">now</a> ]&nbsp;&nbsp;
		</div>
	</div>
	<div class="row-fluid">
		<div class="report">
			<%@ include file="detailHistory.jsp"%>
		</div>
</div>
</div>
</a:application>
