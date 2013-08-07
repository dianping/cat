<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context"
	scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model"
	scope="request" />

<a:body>
	<div style="width: 1200px; margin: 0 auto; margin-bottom: 250px;">
		<h4 style="margin: 0 auto;">A/B Test Caculator</h4>
		<form id="form" class="form-horizontal">
			<div class="control-group">
				<label class="control-label">EXPECTED CONVERSION RATE<i
					tips="sss" data-trigger="hover" class="icon-question-sign"
					data-toggle="popover" data-placement="top"
					data-original-title="tips"
					data-content="sss"></i>
				</label>
				<div class="controls">
					<input type="text" name="name" id="abName" placeholder="Test1"
						class="input-xlarge" check-type="required"
						required-message="Name is required!" >
				</div>
			</div>
		</form>
	</div>
</a:body>

<script type="text/javascript">
</script>