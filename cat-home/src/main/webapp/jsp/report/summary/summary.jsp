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
<link rel="stylesheet" type="text/css" href="${model.webapp}/js/jquery.datetimepicker.css"/>
<script src="${model.webapp}/js/jquery.datetimepicker.js"></script>
<script type="text/javascript">
	$(document).ready(function() {
		$('#Offline_report').addClass('active open');
		$('#summary_report').addClass('active');
		$('#summarytime').datetimepicker({
			format:'Y-m-d H:i',
			step:30,
			maxDate:0
		});
		
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
<div class="row-fluid">
		<div class="report">
			<%@ include file="detail.jsp"%>
		</div>
</div>
</a:application>
