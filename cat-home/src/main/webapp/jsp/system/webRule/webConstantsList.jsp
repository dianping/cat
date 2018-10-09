<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.web.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.web.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.web.Model" scope="request" />

<a:web_body>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#webConstants').addClass('active');
			$("#tab-constant-网络类型").addClass('active');
			$("#tabContent-constant-网络类型").addClass('active');
		});
	</script>
	<div class="tabbable" id="content">
		<!-- Only required for left/right tabs -->
		<div class="tabbable tabs-left" id="content">
			<!-- Only required for left/right tabs -->
			<ul class="nav nav-tabs padding-12 ">
				<c:forEach var="entry" items="${model.webConfigItems}"
					varStatus="status">
					<li id="tab-constant-${entry.key}" class="text-right"><a
						href="#tabContent-constant-${entry.key}" data-toggle="tab">
							${entry.key}</a></li>
				</c:forEach>
			</ul>
			<div class="tab-content">
				<c:forEach var="entry" items="${model.webConfigItems}"
					varStatus="status">
					<div class="tab-pane" id="tabContent-constant-${entry.key}">
						<table
							class="table table-striped table-condensed table-bordered table-hover">
							<thead>
								<tr>
									<th>ID</th>
									<th>值</th>
								</tr>
							</thead>

							<c:forEach var="e" items="${entry.value.items}">
								<tr>
									<td>${e.value.id}</td>
									<td>${e.value.name}</td>
							</c:forEach>
						</table>
					</div>
				</c:forEach>
			</div>
		</div>
	</div>
</a:web_body>
