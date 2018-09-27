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
	<res:useJs value="${res.js.local['alarm_js']}" target="head-js" />
	<script type="text/javascript">
		$(document).ready(function() {
			$('#Web_config').addClass('active open');
			$('#speed').addClass('active');
		});
	</script>
		<form name="speedUpdate" id="form" method="post" action="${model.pageUri}?op=speedSubmit">
			<table style='width:100%;'  align="center" class="table table-striped table-condensed table-bordered ">
				<input type="hidden" class="input-xlarge"  name="step.pageid" value="${model.step.pageid}" />
				 <tr>
					<td>测速页面</td>
					<td>
					<c:choose>
					<c:when test="${model.step.pageid eq 0}">
						<input type="text" class="input-xlarge" name="step.page" required/>
					</c:when>
					<c:otherwise>
			  			<input type="text" class="input-xlarge" name="step.page" value="${model.step.page}" readonly/>
					</c:otherwise>
					</c:choose>
					<input class='btn btn-primary btn-sm' type="submit" name="submit" value="提交" />
					<span style="color:red">填写测试页面名称获取pageId</span>
					</td>
				</tr>
				<tr>
					<td>测速点名称</td>
					<td>
						<table class="table table-striped table-condensed table-bordered ">
							<tr><th>测速点编号</th><th>测速点名称</th></tr>
							<tr><td>20</td><td><input type="text" class="input-xlarge" name="step.step20" value="${model.step.step20}"/>
							<tr><td>21</td><td><input type="text" class="input-xlarge" name="step.step21" value="${model.step.step21}"/>
							<tr><td>22</td><td><input type="text" class="input-xlarge" name="step.step22" value="${model.step.step22}"/>
							<tr><td>23</td><td><input type="text" class="input-xlarge" name="step.step23" value="${model.step.step23}"/>
							<tr><td>24</td><td><input type="text" class="input-xlarge" name="step.step24" value="${model.step.step24}"/>
							<tr><td>25</td><td><input type="text" class="input-xlarge" name="step.step25" value="${model.step.step25}"/>
							<tr><td>26</td><td><input type="text" class="input-xlarge" name="step.step26" value="${model.step.step26}"/>
							<tr><td>27</td><td><input type="text" class="input-xlarge" name="step.step27" value="${model.step.step27}"/>
							<tr><td>28</td><td><input type="text" class="input-xlarge" name="step.step28" value="${model.step.step28}"/>
							<tr><td>29</td><td><input type="text" class="input-xlarge" name="step.step29" value="${model.step.step29}" />
							<tr><td>30</td><td><input type="text" class="input-xlarge" name="step.step30" value="${model.step.step30}"/>
							<tr><td>31</td><td><input type="text" class="input-xlarge" name="step.step31" value="${model.step.step31}"/>
							<tr><td>32</td><td><input type="text" class="input-xlarge" name="step.step32" value="${model.step.step32}"/>
							</td></tr>
							<c:choose>
							<c:when test="${model.step.pageid eq 0}">
							<tr><td>1</td><td><input type="text" class="input-xlarge" name="step.step1" value="unloadEventStart"/>
							<tr><td>2</td><td><input type="text" class="input-xlarge" name="step.step2" value="unloadEventEnd"/>
							<tr><td>3</td><td><input type="text" class="input-xlarge" name="step.step3" value="redirectStart"/>
							<tr><td>4</td><td><input type="text" class="input-xlarge" name="step.step4" value="redirectEnd"/>
							<tr><td>5</td><td><input type="text" class="input-xlarge" name="step.step5" value="fetchStart"/>
							<tr><td>6</td><td><input type="text" class="input-xlarge" name="step.step6" value="domainLookupStart"/>
							<tr><td>7</td><td><input type="text" class="input-xlarge" name="step.step7" value="domainLookupEnd"/>
							<tr><td>8</td><td><input type="text" class="input-xlarge" name="step.step8" value="connectStart"/>
							<tr><td>9</td><td><input type="text" class="input-xlarge" name="step.step9" value="connectEnd"/>
							<tr><td>10</td><td><input type="text" class="input-xlarge" name="step.step10" value="requestStart"/>
							<tr><td>11</td><td><input type="text" class="input-xlarge" name="step.step11" value="responseStart"/>
							<tr><td>12</td><td><input type="text" class="input-xlarge" name="step.step12" value="responseEnd"/>
							<tr><td>13</td><td><input type="text" class="input-xlarge" name="step.step13" value="domLoading"/>
							<tr><td>14</td><td><input type="text" class="input-xlarge" name="step.step14" value="domInteractive"/>
							<tr><td>15</td><td><input type="text" class="input-xlarge" name="step.step15" value="domContentLoadedEventStart"/>
							<tr><td>16</td><td><input type="text" class="input-xlarge" name="step.step16" value="domContentLoadedEventEnd"/>
							<tr><td>17</td><td><input type="text" class="input-xlarge" name="step.step17" value="domComplete"/>
							<tr><td>18</td><td><input type="text" class="input-xlarge" name="step.step18" value="loadEventStart"/>
							<tr><td>19</td><td><input type="text" class="input-xlarge" name="step.step19" value="loadEventEnd"/>
							</c:when>
							<c:otherwise>
							<tr><td>1</td><td><input type="text" class="input-xlarge" name="step.step1" value="${model.step.step1}"/>
							<tr><td>2</td><td><input type="text" class="input-xlarge" name="step.step2" value="${model.step.step2}"/>
							<tr><td>3</td><td><input type="text" class="input-xlarge" name="step.step3" value="${model.step.step3}"/>
							<tr><td>4</td><td><input type="text" class="input-xlarge" name="step.step4" value="${model.step.step4}"/>
							<tr><td>5</td><td><input type="text" class="input-xlarge" name="step.step5" value="${model.step.step5}"/>
							<tr><td>6</td><td><input type="text" class="input-xlarge" name="step.step6" value="${model.step.step6}"/>
							<tr><td>7</td><td><input type="text" class="input-xlarge" name="step.step7" value="${model.step.step7}"/>
							<tr><td>8</td><td><input type="text" class="input-xlarge" name="step.step8" value="${model.step.step8}"/>
							<tr><td>9</td><td><input type="text" class="input-xlarge" name="step.step9" value="${model.step.step9}"/>
							<tr><td>10</td><td><input type="text" class="input-xlarge" name="step.step10" value="${model.step.step10}"/>
							<tr><td>11</td><td><input type="text" class="input-xlarge" name="step.step11" value="${model.step.step11}"/>
							<tr><td>12</td><td><input type="text" class="input-xlarge" name="step.step12" value="${model.step.step12}"/>
							<tr><td>13</td><td><input type="text" class="input-xlarge" name="step.step13" value="${model.step.step13}"/>
							<tr><td>14</td><td><input type="text" class="input-xlarge" name="step.step14" value="${model.step.step14}"/>
							<tr><td>15</td><td><input type="text" class="input-xlarge" name="step.step15" value="${model.step.step15}"/>
							<tr><td>16</td><td><input type="text" class="input-xlarge" name="step.step16" value="${model.step.step16}"/>
							<tr><td>17</td><td><input type="text" class="input-xlarge" name="step.step17" value="${model.step.step17}"/>
							<tr><td>18</td><td><input type="text" class="input-xlarge" name="step.step18" value="${model.step.step18}"/>
							<tr><td>19</td><td><input type="text" class="input-xlarge" name="step.step19" value="${model.step.step19}"/>
							</c:otherwise>
							</c:choose>
							
						</table>
					</td>
				</tr>
			</table>
		</form>
</a:web_body>