<%@ tag trimDirectiveWhitespaces="true"  pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="navUrlPrefix"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true"%>

<a:body_with_nav>
<script>
	function buildHref(domain){
		var href = '<a href="?op=history&domain='+domain+'&date=${model.date}">&nbsp;[&nbsp;'+domain+'&nbsp;]&nbsp;</a>';
		return href;
	}
	$(document).ready(function() {
		var domains= getcookie('CAT_DOMAINS');
		var domainArray =domains.split("|");
		var html = '';
		var length =domainArray.length;
		
		for(var i=0;i<length;i++){
			var href = buildHref(domainArray[i])
			html+= href;
		}
		$('#frequentNavbar').html(html);
		$("#search_go").bind("click",function(e){
			var newUrl = '${model.baseUri}?op=history&domain='+$( "#search" ).val() +'&date=${model.date}';
			window.location.href = newUrl;
		});
	});
</script>
<div class="report">
	<div class="frequentNavbar" style="display:none;font-size:small">
		<table class="table table-striped table-hover table-bordered table-condensed" border="1" rules="all">
			<tr>
				<td class="domain"  style="word-break:break-all" id="frequentNavbar"></td>
			<tr>
		</table>
	</div>
	<div class="breadcrumbs" id="breadcrumbs">
		<script type="text/javascript">
			try{ace.settings.check('breadcrumbs' , 'fixed')}catch(e){}
		</script>
		<span class="text-danger title">【时段】</span><span class="text-success"><jsp:invoke fragment="subtitle"/></span>
		<!-- #section:basics/content.searchbox -->
		<div class="nav-search nav" id="nav-search">
			<span class="text-danger">【<a href="?domain=${model.domain}" class="switch"><span class="text-danger">切到小时</span></a>】</span>
					&nbsp;&nbsp;<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq model.reportType}">
								<span>&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]</span>
						</c:when>
						<c:otherwise>
								<span>&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]</span>
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=-1&${navUrlPrefix}">${model.currentNav.last}</a> ]
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${model.reportType}&step=1&${navUrlPrefix}">${model.currentNav.next}</a> ]
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&reportType=${model.reportType}&nav=next&${navUrlPrefix}">now</a> ]
		</div><!-- /.nav-search -->
	</div>
	<jsp:doBody />
	</div>
</a:body_with_nav>
