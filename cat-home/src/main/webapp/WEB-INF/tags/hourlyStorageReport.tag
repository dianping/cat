<%@ tag trimDirectiveWhitespaces="true" pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="navUrlPrefix"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true"%>

<a:storage_body>
<script>
	function buildHref(id){
		var href = '<a href="?op=${payload.action.name}&type=${payload.type}&domain=${model.domain}&id='+id+'&date=${model.date}">&nbsp;[&nbsp;'+id+'&nbsp;]&nbsp;</a>';
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
			var newUrl = '${model.baseUri}?op=${payload.action.name}&type=${payload.type}&domain=${model.domain}&id='+$( "#search" ).val() +'&date=${model.date}';
			window.location.href = newUrl;
		});
		$('#wrap_search').submit(
			function(){
				var newUrl = '${model.baseUri}?op=${payload.action.name}&type=${payload.type}&domain=${model.domain}&id='+$( "#search" ).val() +'&date=${model.date}';
				window.location.href = newUrl;
				return false;
			}		
		);
	});
</script>
<div class="report">
	<div class="domainNavbar" style="display:none;font-size:small">
		<table border="1" rules="all" >
			<c:forEach var="item" items="${model.departments}">
				<tr>
					<c:set var="detail" value="${item.value}" />
					<td class="department" rowspan="${w:size(detail.productlines)}">${item.key}</td>
					<c:forEach var="productline" items="${detail.productlines}" varStatus="index">
							<c:if test="${index.index != 0}">
								<tr>
							</c:if>
							<td class="department">${productline.key}</td>
							<td><div class="domain"><c:forEach var="id" items="${productline.value.storages}">&nbsp;<c:choose><c:when test="${payload.id eq id}"><a class='domainItem'
													href="?op=${payload.action.name}&type=${payload.type}&domain=${model.domain}&id=${id}&date=${model.date}&reportType=${payload.reportType}"
													class="current">[&nbsp;${id}&nbsp;]</a></c:when>
													<c:otherwise><a class='domainItem'
													href="?op=${payload.action.name}&type=${payload.type}&domain=${model.domain}&id=${id}&date=${model.date}&reportType=${payload.reportType}">[&nbsp;${id}&nbsp;]</a>
											</c:otherwise></c:choose>&nbsp;
									</c:forEach>
								</div>
							</td><c:if test="${index.index != 0}"></tr></c:if>
					</c:forEach>
				</tr>
			</c:forEach>
		</table>
	</div>
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
		<span class="text-danger title">【报表时间】</span><span class="text-success"><jsp:invoke fragment="subtitle"/></span>
		<div class="nav-search nav" id="nav-search">
			<span class="text-danger switch">【<a class="switch" href="${model.baseUri}?op=history&type=${payload.type}&domain=${model.domain}&id=${payload.id}&ip=${model.ipAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
			<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?date=${model.date}&ip=${model.ipAddress}&step=${nav.hours}&${navUrlPrefix}">${nav.title}</a> ]
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?${navUrlPrefix}">now</a> ]&nbsp;
		</div><!-- /.nav-search -->
	</div>
	<jsp:doBody />
</div>
</a:storage_body>