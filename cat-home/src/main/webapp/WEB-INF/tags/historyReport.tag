<%@ tag trimDirectiveWhitespaces="true"  pageEncoding="UTF-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ attribute name="title"%>
<%@ attribute name="navUrlPrefix"%>
<%@ attribute name="timestamp"%>
<%@ attribute name="subtitle" fragment="true"%>

<a:application>
<script>
	function showDomain() {
		var b = $('#switch').html();
		if (b == '全部') {
			$('.domainNavbar').slideDown();
			$('#switch').html("收起");
		} else {
			$('.domainNavbar').slideUp();
			$('#switch').html("全部");
		}
	}
	function showFrequent(){
		var b = $('#frequent').html();
		if (b == '常用') {
			$('.frequentNavbar').slideDown();
			$('#frequent').html("收起");
		} else {
			$('.frequentNavbar').slideUp();
			$('#frequent').html("常用");
		}
	}
	function buildHref(domain){
		var href = '<a href="?op=${payload.action.name}&domain='+domain+'&date=${model.date}">&nbsp;[&nbsp;'+domain+'&nbsp;]&nbsp;</a>';
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
			var newUrl = '${model.baseUri}?op=${payload.action.name}&domain='+$( "#search" ).val() +'&date=${model.date}';
			window.location.href = newUrl;
		});
		$('#wrap_search').submit(
			function(){
				var newUrl = '${model.baseUri}?op=${payload.action.name}&domain='+$( "#search" ).val() +'&date=${model.date}';
				window.location.href = newUrl;
				return false;
			}		
		);
		//custom autocomplete (category selection)
		$.widget( "custom.catcomplete", $.ui.autocomplete, {
			_renderMenu: function( ul, items ) {
				var that = this,
				currentCategory = "";
				$.each( items, function( index, item ) {
					if ( item.category != currentCategory ) {
						ul.append( "<li class='ui-autocomplete-category'>" + item.category + "</li>" );
						currentCategory = item.category;
					}
					that._renderItemData( ul, item );
				});
			}
		});
		
		var data = [];
		<c:forEach var="item" items="${model.domainGroups}">
			<c:set var="detail" value="${item.value}" />
				<c:forEach var="productline" items="${detail.projectLines}" varStatus="index">
				<c:forEach var="domain" items="${productline.value.lineDomains}">
						var item = {};
						item['label'] = '${domain}';
						item['category'] ='${productline.key}';
						
						data.push(item);
				</c:forEach>
		</c:forEach></c:forEach>
		
		$("#search").catcomplete({
			delay: 0,
			source: data
		});
	});
</script>
<div class="report">
	<div class="breadcrumbs" id="breadcrumbs">
		<table>
			<tr><td><span class="text-success"><jsp:invoke fragment="subtitle"/></span></td>
				<td><div id="warp_search_group" class="" style="width:250px;">
					<form id="wrap_search" style="margin-left:10px;margin-bottom:0px;">
					<div class="input-group">
						<span class="input-group-btn "><button class="btn btn-sm btn-default" onclick="showDomain()" type="button"  id="switch">全部</button></span>
						<span class="input-group-btn "><button class="btn btn-sm btn-default" onclick="showFrequent()" type="button"  id="frequent">常用</button></span>
						<span class="input-icon" style="width:200px;">
						<input id="search" type="text" value="${model.domain}" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
						<i class="ace-icon fa fa-search nav-search-icon"></i>
						</span>
						<span class="input-group-btn">
							<button class="btn btn-sm btn-pink" type="button" id="search_go">
								Go
							</button> 
						</span>
					</div>
				</form>
			</div></td>
			<td>
				<div class="nav-search nav" id="nav-search">
			<span class="text-danger">【<a href="?domain=${model.domain}" class="switch"><span class="text-danger">切到小时模式</span></a>】</span>
					&nbsp;&nbsp;<c:forEach var="nav" items="${model.historyNavs}">
					<c:choose>
						<c:when test="${nav.title eq payload.reportType}">
								<span>&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}" class="current">${nav.title}</a> ]</span>
						</c:when>
						<c:otherwise>
								<span>&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${nav.title}">${nav.title}</a> ]</span>
						</c:otherwise>
					</c:choose>
				</c:forEach>
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&step=-1&${navUrlPrefix}">${model.currentNav.last}</a> ]
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&date=${model.date}&reportType=${payload.reportType}&step=1&${navUrlPrefix}">${model.currentNav.next}</a> ]
				&nbsp;[ <a href="?op=history&domain=${model.domain}&ip=${model.ipAddress}&reportType=${payload.reportType}&nav=next&${navUrlPrefix}">now</a> ]
		</div>
			</td>
			</tr>
		</table>
	</div>
	
	<div class="domainNavbar" style="display:none;font-size:small">
		<table border="1" rules="all" >
			<c:forEach var="item" items="${model.domainGroups}">
				<tr>
					<c:set var="detail" value="${item.value}" />
					<td class="department" rowspan="${w:size(detail.projectLines)}">${item.key}</td>
					<c:forEach var="productline" items="${detail.projectLines}" varStatus="index">
							<c:if test="${index.index != 0}">
								<tr>
							</c:if>
							<td class="department">${productline.key}</td>
							<td><div class="domain"><c:forEach var="domain" items="${productline.value.lineDomains}">&nbsp;<c:choose><c:when test="${model.domain eq domain}"><a class='domainItem'
													href="?op=${payload.action.name}&domain=${domain}&date=${model.date}&reportType=${payload.reportType}"
													class="current">[&nbsp;${domain}&nbsp;]</a></c:when>
													<c:otherwise><a class='domainItem'
													href="?op=${payload.action.name}&domain=${domain}&date=${model.date}&reportType=${payload.reportType}">[&nbsp;${domain}&nbsp;]</a>
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
		<table border="1" rules="all">
			<tr>
				<td class="domain"  style="word-break:break-all" id="frequentNavbar"></td>
			<tr>
		</table>
	</div>
	<c:if test="${model.sample ne 1.0}" >
		<div class="ace-settings-container" id="ace-settings-container">
			<div class="btn btn-app btn-xs btn-warning ace-settings-btn" id="ace-settings-btn">采样</div>
	
			<div class="ace-settings-box clearfix" id="ace-settings-box">
				<div class="pull-left width-50">
					<!-- #section:settings.navbar -->
					<div class="ace-settings-item">
						<label class="lbl" for="ace-settings-navbar">${model.sample}</label>
					</div>
	
					<!-- /section:settings.container -->
				</div><!-- /.pull-left -->
			</div><!-- /.ace-settings-box -->
		</div>
	</c:if>
	<jsp:doBody />
	</div>
</a:application>
