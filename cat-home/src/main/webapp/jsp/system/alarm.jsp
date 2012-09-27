<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.alarm.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.alarm.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.alarm.Model" scope="request" />


<a:body>

	<res:useJs value="${res.js.local['dtree.js']}" target="head-js"/>
	<res:useCss value='${res.css.local.dtree_css}' target="head-css" />
	<res:useCss value='${res.css.local.alarm_css}' target="head-css" />
	
	<div class="body-content">
		<div class="content-left">
			<p align="center">
				<script type="text/javascript">
					d = new dTree('d');
					d.add(0, -1, 'CAT告警', '');
					d.add(1, 0, '个人告警记录', 'javascript:getJRobinGraphList()');
					d.add(2, 0, '告警规则订阅', '');
					d.add(3, 0, '告警模板配置', '');
					document.write(d);
					d.openAll();
				</script>
		</div>
		<div class="content-right">
			<br>
			TODO
		</div>
	</div>
</a:body>