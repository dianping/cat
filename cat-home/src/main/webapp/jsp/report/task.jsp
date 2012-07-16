<%@ page contentType="text/html; charset=utf-8"%>

<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>

<jsp:useBean id="ctx" type="com.dianping.cat.report.page.task.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.task.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.task.Model" scope="request" />
<jsp:useBean id="navBar" class="com.dianping.cat.report.view.NavigationBar" scope="page"/>


<html>
	<head>
		<title>CAT - ${model.page.description}</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<link rel="stylesheet" href="../css/body.css" type="text/css">
		<link rel="stylesheet" href="../css/report.css" type="text/css">
		<link rel="stylesheet" href="../css/transaction.css" type="text/css">
		<link rel="stylesheet" type="text/css" href="../css/style.css" media="screen"/>
				<script src="../js/jquery-1.3.2.js" type="text/javascript"></script>
		<script src="../js/jquery.paginate.js" type="text/javascript"></script>
		<script type="text/javascript">
		$(function() {
		
			$("#demo3").paginate({
				count 		: 10,
				start 		: 1,
				display     : 10,
				border					: true,
				border_color			: '#BEF8B8',
				text_color  			: '#68BA64',
				background_color    	: '#E3F2E1',	
				border_hover_color		: '#68BA64',
				text_hover_color  		: 'black',
				background_hover_color	: '#CAE6C6', 
				rotate      : false,
				images		: false,
				mouse		: 'press',
				onChange: function(currentPage) {
				//点击页码时,执行的函数(作ajax异步请求数据)
					alert(currentPage);
				//取得分页数据
				//getPaginateData(pageSize, currentPage);
			}
			});
			show
		});
		</script>
	</head>
	<body>
<div class="report">

	<ul class="tabs">
	<c:forEach var="page" items="${navBar.visiblePages}">
		<c:if test="${page.standalone}">
			<li ${model.page.name == page.name ? 'class="selected"' : ''}><a href="${model.webapp}/${page.moduleName}/${page.path}?domain=${model.domain}&date=${model.date}">${page.title}</a></li>
		</c:if>
		<c:if test="${not page.standalone and model.page.name == page.name}">
			<li class="selected">${page.title}</li>
		</c:if>
	</c:forEach>
	</ul>
	<table class="header">
		<tr>
			<td class="title">Task Manage Platform</td>
		</tr>
	</table>
	<table class="navbar">
		<tr>
			<td class="domain">
			<div class="domain">
			<b>domain:</b><c:forEach var="domain" items="${model.domains}">
			  &nbsp;<c:choose>
						<c:when test="${model.domain eq domain}">
							<a href="domain=${domain}" class="current">[&nbsp;${domain}&nbsp;]</a>
						</c:when>
					<c:otherwise>
						<a href="domain=${domain}">[&nbsp;${domain}&nbsp;]</a>
					</c:otherwise>
					</c:choose>&nbsp;
			</c:forEach><br/>
			<b>name:&nbsp;&nbsp;&nbsp;</b><c:forEach var="name" items="${model.names}">
			  &nbsp;<c:choose>
						<c:when test="${model.name eq name}">
							<a href="name=${name}" class="current">[&nbsp;${name}&nbsp;]</a>
						</c:when>
					<c:otherwise>
						<a href="name=${name}">[&nbsp;${name}&nbsp;]</a>
					</c:otherwise>
					</c:choose>&nbsp;
			</c:forEach>
			</div>
			</td>
		</tr>
		<tr>
			<td>From:&nbsp;&nbsp;<input type="text" name="from"/> To:&nbsp;&nbsp;<input type="text" name="to">
				<b>status:&nbsp;&nbsp;</b><select>
				  <option value ="todo">todo</option>
				  <option value ="doing">doing</option>
				  <option value="done">done</option>
				  <option value="failed">failed</option>
				</select>
				<b>type:&nbsp;&nbsp;</b><select>
				  <option value ="daily">daily</option>
				  <option value ="hour">hour</option>
				</select>
				&nbsp;&nbsp;<input type='button' value=' search '  width=20 height=10></input>
			</td>
		</tr>
		
	</table>

	<div class="content">
		<div class="demo">
               <div id="demo3"> </div>                    
         </div>
    </div>
	<table class="footer">
		<tr>
			<td>[ end ]</td>
		</tr>
	</table>
</div>
	</body>
</html>


