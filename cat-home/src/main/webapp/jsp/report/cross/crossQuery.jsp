<%@ page contentType="text/html; charset=utf-8" %>
<script>
    function query() {
        var reportType = '${payload.reportType}';
        var domain = '${model.domain}';
        var date = '${model.date}';
        var method = $('#method').val();

        window.location.href = "?op=query&domain=" + domain + "&date=" + date + "&reportType=" + reportType + "&method=" + method;
    }
</script>
<table style="margin-top: 10px;">
    <tr>
        <th>
			<span class='text-danger' style="padding-left:5px;">查询当前这个时段内，一个方法被哪些应用调用</span>&nbsp;
            <input type="text" class='input-xlarge' id="method" style="height: 34px;" size="120"
                   value="${payload.method}">
            <input type="submit" class='btn btn-primary btn-sm' style="height: 34px;margin-left: -4px;margin-top: -2px"
                   onClick="query()"/>
		</th>
    </tr>
</table>

