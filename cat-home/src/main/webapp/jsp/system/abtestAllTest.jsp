<%@ page contentType="text/html; charset=utf-8"%>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="res" uri="http://www.unidal.org/webres"%>
<%@ taglib prefix="w" uri="http://www.unidal.org/web/core"%>

<jsp:useBean id="ctx" type="com.dianping.cat.system.page.abtest.Context" scope="request" />
<jsp:useBean id="payload" type="com.dianping.cat.system.page.abtest.Payload" scope="request" />
<jsp:useBean id="model" type="com.dianping.cat.system.page.abtest.Model" scope="request" />
	
<a:body>
	<script src="http://code.jquery.com/jquery-1.8.3.js"></script>
	<res:useCss value="${res.css.local['bootstrap.css']}" target="head-css" />
	<res:useJs value="${res.js.local['bootstrap.min.js']}" target="head-js" />

	<style>
	.statusSpan {
		float: right;
		margin-right: 0em;
	}
	
	.liHover > li {
		line-height: 2em;
	}
	
	input.search-query {
		-webkit-border-radius: 4px;
		-moz-border-radius: 4px;
		border-radius: 4px;
	}
	
	#search-submit {
		position: absolute;
		top: 6px;
		right: 10px;
		display: inline-block;
		width: 14px;
		height: 14px;
		*margin-right: .3em;
		line-height: 14px;
		text-indent: -9999px;
		vertical-align: text-top;
		cursor: pointer;
		background-color: transparent;
		background-image: url("${model.webapp}/img/glyphicons-halflings.png");
		background-position: -48px 0;
		background-repeat: no-repeat;
		border: 0 none;
		opacity: 0.75;
	}
	
	tr.middle > td {
		vertical-align: middle;
		padding-bottom: 0;
	}
	
	tr.center > td{
		text-align: center;
	}
	
	tr.centerth > th{
		text-align: center;
	}
	
	</style>

	<br>
	<div class="row-fluid clearfix">
		<div class="span2 column">
			<form class="navbar-search" action="">
				<input name="q" id="search" class="search-query"
					placeholder="Search..."> <input type="submit"
					value="Search" id="search-submit">
			</form>
			<div style="margin-top: 40px; margin-left: 6px; margin-right: 0px;">
				<ul class="nav nav-list well liHover">
					<li class="nav-header">ABTest Status</li>
					<li class="divider" />
					<li>
						<a href="#running">
						<img height="10" width="10" src="${res.img.local['RUNNING_black_small.png']}"> running
						<span class="badge statusSpan">1</span>
						</a>
					</li>
					<li>
						<a href="#disabled">
						<img height="10" width="10" src="${res.img.local['PAUSED_black_small.png']}"> disabled
						<span class="badge statusSpan">1</span>
						</a>
					</li>
					<li>
						<a href="#ready">
						<img height="10" width="10" src="${res.img.local['READY_black_small.png']}"> ready to start
						<span class="badge statusSpan">1 </span>
						</a>
					</li>
					<li>
						<a href="#stopped">
						<img height="10" width="10" src="${res.img.local['STOPPED_black_small.png']}"> stopped
						<span class="badge statusSpan">1</span>
						</a>
					</li>
				</ul>
			</div>
		</div>
		<div class="span10 column">
			<div style="margin-bottom: 10px;">
				<button class="btn" type="button">
					<label class="checkbox"> <input type="checkbox"></input></label>
				</button>
				<button class="btn" type="button">Start</button>
				<button class="btn" type="button">Disable</button>
				<button class="btn" type="button">Stop</button>
			</div>

			<table class="table table-striped table-format table-hover">
				<thead>
					<tr class="centerth">
						<th width="1%"></th>
						<th width="6%">ID</th>
						<th>Name</th>
						<th>PV</th>
						<th>ConversionRate</th>
						<th>Status</th>
						<th>Created On</th>
					</tr>
				</thead>
				<tbody>
					<tr class="middle center">
						<td style="padding-bottom: 8px"><input type="checkbox"/></td>
						<td>1001</td>
						<td>MockTest1</td>
						<td>1000</td>
						<td>10%</td>
						<td>
							<div>
								<img src="${res.img.local['RUNNING_colored_big.png']}" />
							</div> <small>Running</small>
						</td>
						<td>Apr 1, 2013</td>
					</tr>
					<tr class="middle center">
						<td style="padding-bottom: 8px"><input type="checkbox"></input></td>
						<td>1002</td>
						<td>MockTest2</td>
						<td>1000</td>
						<td>10%</td>
						<td>
							<div>
								<img src="${res.img.local['STOPPED_colored_big.png']}">
							</div> <small>Stopped</small>
						</td>
						<td>Apr 1, 2013</td>
					</tr>
					<tr class="middle center">
						<td style="padding-bottom: 8px"><input type="checkbox"></input></td>
						<td>1003</td>
						<td>MockTest3</td>
						<td>1000</td>
						<td>10%</td>
						<td>
							<div>
								<img src="${res.img.local['READY_colored_big.png']}">
							</div> <small>Ready to start</small>
						</td>
						<td>Apr 1, 2013</td>
					</tr>
					<tr class="middle center">
						<td style="padding-bottom: 8px"><input type="checkbox"></input></td>
						<td>1004</td>
						<td>MockTest4</td>
						<td>1000</td>
						<td>10%</td>
						<td>
							<div>
								<img src="${res.img.local['PAUSED_colored_big.png']}">
							</div> <small>Disabled</small>
						</td>
						<td>Apr 1, 2013</td>
					</tr>
				</tbody>
			</table>

		</div>
	</div>
</a:body>