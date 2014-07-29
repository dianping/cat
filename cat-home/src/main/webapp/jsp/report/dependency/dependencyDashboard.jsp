<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.dependency.Context" scope="request"/>
<jsp:useBean id="payload" type="com.dianping.cat.report.page.dependency.Payload" scope="request"/>
<jsp:useBean id="model" type="com.dianping.cat.report.page.dependency.Model" scope="request"/>

<res:bean id="res" />
<style>
	.tooltip-inner {
		max-width:36555px;
	 }
	.tab-content	table {
	  max-width: 100%;
	  background-color: transparent;
	  border-collapse: collapse;
	  border-spacing: 0; 
	}
</style>
 <c:choose>
	<c:when test="${payload.fullScreen}">
		<res:useCss value='${res.css.local.table_css}' target="head-css" />
		<res:useCss value='${res.css.local.body_css}' target="head-css" />
		<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
		<res:useJs value="${res.js.local['jquery-1.7.1.js']}" target="head-js" />
		<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['jquery.dataTables.min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['raphael-min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['dependencyConfig.js']}" target="head-js" />
		<res:useJs value="${res.js.local['jquery.validate.min.js']}" target="head-js" />
		<res:useJs value="${res.js.local['startopo.js']}" target="head-js" />
		<div class="report">
			<a href="javascript:showOpNav()" id="switch" class="btn btn-small btn-success">隐藏</a>
				<div class="opNav">
				<div class="row-fluid">
					<div class="span12 text-center">
						<%@ include file="dependencyOpNav.jsp"%>
				 		<%@ include file="dependencyTimeNavTab1.jsp"%>
				</div></div></div>
			<div id="fullScreenData">
				<div class="text-center" id="container" style="width:1400px;height:1600px;border:solid 1px #ccc;"></div>
				<br/>
			</div>
	    </div>
	</c:when>
	<c:otherwise>
		<res:useJs value="${res.js.local['startopo.js']}" target="head-js" />
			<a:report title="Dependency Report"
		navUrlPrefix="domain=${model.domain}&op=dashboard">
		<jsp:attribute name="subtitle">From ${w:format(model.reportStart,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.reportEnd,'yyyy-MM-dd HH:mm:ss')}</jsp:attribute>
		<jsp:body>
		<div class="report">
			<div class="row-fluid">
				<div class="span12 text-center">
					<%@ include file="dependencyOpNav.jsp"%>
			 		<%@ include file="dependencyTimeNavTab1.jsp"%>
			</div></div>
			<div id="fullScreenData">
				<div class="text-center" id="container" style="width:1400px;height:1600px;border:solid 1px #ccc;"></div>
				<br/>
			</div>
	    </div>
	</jsp:body>
	</a:report>
	</c:otherwise>
</c:choose>

<script type="text/javascript">
	$(document).ready(function() {
		$('#minute'+${model.minute}).addClass('disabled');
		$('.hreftip').tooltip({container:'body', html:true, delay:{show:0, hide:0}});
		$('.position').hide();
		$('.switch').hide();
		var data = ${model.dashboardGraph};
		
		new  StarTopoList('container', data ,{
			typeMap:{
				database:'circle',
				project:'rect',
				service:'lozenge'
			},
			colorMap:{
				 "1":'#2fbf2f',
				 "2":'#bfa22f',
				 "3":'#b94a48',
				 "4":'#772fbf'
                         },
	            legendMap:{
	            "1":"good",
	            "2":"warning",
	            "3":"error"
	        },
	        format: {
	            团购: {
	                "colInside": '5'
	            },支付: {
	                "colInside": '3'
	            },会员卡: {
	                "colInside": '2'
	            },预约预定: {
	                "colInside": '4'
	            },商户: {
	                "colInside": '4'
	            },广告: {
	                "colInside": '5'
	            },用户中心: {
	                "colInside": '4'
	            },移动: {
	                "colInside": '4'
	            },账号中心: {
	                "colInside": '4'
	            },社区: {
	                "colInside": '4'
	            }
	        }, 
	        
			paddingInside:10,
			col:3,
			colInside:5,
			//模块距上沿距离
			paddingUp: 10,
			//小方块间的间隔比率
			blockPaddingRatio: 0.2,
            leftTitlePaddingRatio: 0.05,
			showLeft: true,
			showUp: false
		});

		var hide =${payload.hideNav};
		if(hide){
			$('.opNav').slideUp();
			$('#switch').html("显示");
		}	
	});

	function showOpNav() {
		var b = $('#switch').html();
		if (b == '隐藏') {
			$('.opNav').slideUp();
			$('#switch').html("显示");
		} else {
			$('.opNav').slideDown();
			$('#switch').html("隐藏");
		}
	}
</script>
<style>
.pagination{
		margin:4px 0;
	}
	.pagination ul{
		margin-top:0px;
	}
	.pagination ul > li > a, .pagination ul > li > span{
		padding:3px 10px;
	}
</style>
