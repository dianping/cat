<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="res" uri="http://www.ebay.com/webres"%>
<jsp:useBean id="ctx"
	type="com.dianping.cat.report.page.failure.Context" scope="request" />
<jsp:useBean id="payload"
	type="com.dianping.cat.report.page.failure.Payload" scope="request" />
<jsp:useBean id="model"
	type="com.dianping.cat.report.page.failure.Model" scope="request" />

<a:body>

	<res:useCss value='${res.css.local.default_css}' target="head-css" />
	<res:useCss value='${res.css.local.jquery_css}' target="head-css" />
	<res:useCss value='${res.css.local.jqgrid_css}' target="head-css" />
	<res:useCss value='${res.css.local.style_css}' target="head-css" />
	<res:useCss value='${res.css.local.failure_css}' target="head-css" />

	<res:useJs value='${res.js.local.jquery_min_js}' target="head-js" />
	<res:useJs value='${res.js.local.jquery_ui_min_js}' target="head-js" />
	<res:useJs value='${res.js.local.grid_js}' target="head-js" />
	<res:useJs value='${res.js.local.jqgrid_min_js}' target="head-js" />
	<res:useJs value='${res.js.local.sql_scripts_js}' target="head-js" />

	<script type="text/javascript">
	 var jsonData = ${model.reportInJson};
</script>

	<table width="100%" border="0" cellpadding="6" cellspacing="0"
		class="fancy-header">
		<tbody>
			<tr>
				<td nowrap="">Dian Ping CAT Reports: Report For XXXXXX Current: ${model.current}</td>
				<td width="100%" align="right" nowrap="">Generated: XXXXXX</td>
			</tr>
		</tbody>
	</table>
	<table width="100%" border="0" cellpadding="6" cellspacing="0"
		class="navbar">
		<tbody>
			<tr>
				<td nowrap="nowrap" align="left" class="seealso">Domains: 
				  [ <a href="/cat/r/f?domain=Shop">Shop</a>
				] [ <a href="/cat/r/f?domain=User">User</a>
				] [ <a href="/cat/r/f?domain=Pic">Pic</a>
				] [ <a href="/cat/r/f?domain=Review">Review</a>
				] [ <a href="/cat/r/f?domain=Group">Group</a>
				] [ <a href="/cat/r/f?domain=Tuan">Tuan</a>
				]<br>
				</td>
				<td nowrap="nowrap" align="right" class="seealso">
				  [ <a href="/cat/r/f?domain=${model.domain}&start=${model.current}&method=-24">-1d</a>
				] [ <a href="/cat/r/f?domain=${model.domain}&start=${model.current}&method=-2">-2H</a>
				] [ <a href="/cat/r/f?domain=${model.domain}&start=${model.current}&method=-1">-1H</a>
				] [ <a href="/cat/r/f?domain=${model.domain}&start=${model.current}&method=1">+1H</a>
				] [ <a href="/cat/r/f?domain=${model.domain}&start=${model.current}&method=2">+2H</a>
				] [ <a href="/cat/r/f?domain=${model.domain}&start=${model.current}&method=24">+1d</a>
				]<br>
				</td>
			</tr>
		</tbody>
	</table>
	<br/>
	<table id="failureTable" width="100%" border="0" cellspacing="0"></table>
	<br/>
	<table width="100%" border="0" cellpadding="6" cellspacing="0"
		class="fancy-footer">
		<tbody>
			<tr>
				<td nowrap="" width="100%">[ end ]</td>
			</tr>
		</tbody>
	</table>
	<res:useJs value="${res.js.local.failure_js}" target="bottom-js" />

</a:body>