<%@ page contentType="text/html; charset=utf-8"%>

<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<jsp:useBean id="ctx" type="com.dianping.cat.report.page.task.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.report.page.task.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.report.page.task.Model" scope="request" />
<jsp:useBean id="navBar" class="com.dianping.cat.report.view.NavigationBar" scope="page"/>

<a:simpleReport title="Task Manage Platform">

<link rel="stylesheet" href="../css/body.css" type="text/css">
<link rel="stylesheet" href="../css/report.css" type="text/css">
<link rel="stylesheet" href="../css/task.css" type="text/css">
<link rel="stylesheet" type="text/css" href="../css/style.css" media="screen"/>
<script src="../js/jquery-1.7.1.js" type="text/javascript"></script>
<script src="../js/task.js" type="text/javascript"></script>
<body>
		
<div class="report">
	<table class="navbar">
		<tr>
			<td class="domain">
				<div class="domain">
					<c:forEach var="domain" items="${model.domains}">
						&nbsp;<c:choose>
							<c:when test="${model.domain eq domain}">
								<a href="${model.baseUri}?domain=${domain}&date=${model.date}" class="current">[&nbsp;${domain}&nbsp;]</a>
							</c:when>
							<c:otherwise>
								<a href="${model.baseUri}?domain=${domain}&date=${model.date}">[&nbsp;${domain}&nbsp;]</a>
							</c:otherwise>
						</c:choose>&nbsp;
					</c:forEach>
				</div>
			</td>
			<td class="nav">
				<c:forEach var="nav" items="${model.navs}">
					&nbsp;[ <a href="${model.baseUri}?domain=${domain}&date=${model.date}&name=${name}&step=${nav.hours}">${nav.title}</a> ]&nbsp;
				</c:forEach>
				&nbsp;[ <a href="${model.baseUri}?domain=${domain}&name=${name}">now</a> ]&nbsp;
			</td>
		</tr>
		<tr>
			<td><b>From ${w:format(model.from,'yyyy-MM-dd HH:mm:ss')} to ${w:format(model.to,'yyyy-MM-dd HH:mm:ss')}</b></td>
		</tr>
	</table>

	<table class="task">
		<tr><td colspan='3'>
				<b>Report Type:&nbsp;&nbsp;</b>
				<select id="name">
				  <option value="All">All</option>
				  <option value ="Transaction">Transaction</option>
				  <option value ="Event">Event</option>
				  <option value ="Problem">Problem</option>
				  <option value="Heartbeat">Heartbeat</option>
				</select>
				<b>Status:&nbsp;&nbsp;</b>
				<select id="status">
				  <option value ="0">All</option>
				  <option value ="1">todo</option>
				  <option value ="2">doing</option>
				  <option value="3">done</option>
				  <option value="4">failed</option>
				</select>
				<b>Type:&nbsp;&nbsp;</b><select id="type">
				  <option value ="-1">All</option>
				  <option value ="0">hour</option>
				  <option value ="1">daily</option>
				</select>
				&nbsp;&nbsp;<input type='button' value=' search '  width=20 height=10 onclick="searchTask('${model.domain}','${model.name}','${payload.date}','${payload.step}')"></input>
			</td>
			<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>total tasks:</b>&nbsp;&nbsp;${model.totalNumOfTasks}</td>
			<td clospan="2">
				<b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;failure tasks:&nbsp;&nbsp;</b>${model.numOfFailureTasks}
			</td>
			</tr>
		<tr>
			<th >Producer</th>
			<th >Consumer</th>
			<th >Domain</th>
			<th >Name</th>
			<th >Report Period</th>
			
			
			<th >Start Date</th>
			<th >End Date</th>
			<th >Status</th>
			<th >Type</th>
			<th >Operation</th>
		</tr>
		<c:forEach var="task" items="${model.tasks}" varStatus="status">
			<tr class="${status.index mod 2 != 0 ? 'odd' : 'even'}">
				<td>${task.producer}</td>
				<td>${task.consumer}</td>
				<td>${task.reportDomain}</td>
				<td>${task.reportName}</td>
				<td>${w:format(task.reportPeriod,'yyyy-MM-dd HH:mm:ss')} </td>
				<td>${w:format(task.startDate,'yyyy-MM-dd HH:mm:ss')}</td>
				<td>${w:format(task.endDate,'yyyy-MM-dd HH:mm:ss')} </td>
				<td class="class${task.status}">
					<c:if test="${task.status==1}">todo</c:if>
					<c:if test="${task.status==2}">doing</c:if>
					<c:if test="${task.status==3}">done</c:if>
					<c:if test="${task.status==4}">failure</c:if>
				</td>
				<td>
					<c:if test="${task.taskType==0}">hour</c:if>
					<c:if test="${task.taskType==1}">day</c:if>
				</td>
				<td > 
				<a href="${model.baseUri}?&op=redo&taskID=${task.id}" target="_blank">redo</a></td>
		</tr>
		</c:forEach>
		<tr>
			<td colspan="9" id="pager"> </td>
		</tr>
	</table>
</div>

<script type="text/javascript">
 	$(".class1").css("background-color","#33FF66");
 	$(".class2").css("background-color","#99CCFF");
 	$(".class4").css("background-color","#FF6633");
	var type=${model.type};
	var status=${model.status};
	var name='${model.name}';
	$("#type").val(type) ;
	$("#status").val(status) ;
	$("#name").val(name) ;
	var totalPages=${model.totalpages};
	var url="&domain="+"${model.domain}"+"&name="+"${model.name}"+"&date="+${payload.date}+"&step="+${payload.step};
</script>
</body>
</a:simpleReport>



