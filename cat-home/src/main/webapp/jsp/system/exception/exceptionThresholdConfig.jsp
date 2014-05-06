<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="a" uri="/WEB-INF/app.tld"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<form name="exceptionConfig" id="form" method="post"
	action="${model.pageUri}?op=exceptionThresholdUpdateSubmit">
	<h4 class="text-center text-error" id="state">&nbsp;</h4>
	<h4 class="text-center text-error">修改异常报警配置信息</h4>
	<table class="table table-striped table-bordered table-condensed table-hover">
		<tr>
			<td style="text-align:right" class="text-success">项目名称</td>
			<td>
			<c:if test="${not empty model.domainList}">
				<select name="exceptionConfig.domain" id="domainId">
					<c:forEach var="item" items="${model.domainList}">
                        <option value="${item}">${item}</option> 							
					</c:forEach>
                </select>
			</c:if>
			</td>
		</tr>
	  

		<tr>
			<td style="text-align:right" class="text-success">异常名称</td>
			<td>
			
    <div id='content'>
        <div id='jqxcombobox'>
        </div>
    </div>
			
			   

 				<%-- <c:if test="${not empty model.exceptionList}">
						<select name="exceptionConfig.exception" id="exceptionId" onchange="exception.value=this.value;">
						<c:forEach var="item" items="${model.exceptionList}">
	                        <option value="${item}">${item}</option> 							
						</c:forEach>
						<option value="abc">abc</option>
						<option value="abc">abc</option>
						<option value="abc">abc</option>
						<option value="abc">abc</option>
						<option value="abc">abc</option>
						<option value="abc">abc</option>
                 	 </select>
				</c:if> --%>
				
		<!-- 		<div style="position:relative;"> 
    			<span style="margin-left:100px;width:18px;overflow:hidden;">  -->
<!--     			<select style="width:118px;margin-left:-100px;" onchange="this.parentNode.nextSibling.value=this.options[this.selectedIndex].text;"> 
 -->        			
     		<!-- 	<select style="width:118px;margin-left:-100px;" onchange="$('input#text4').val($(this).val());"> 
 				<option value="-1">123</option> 
        			<option value="1">456</option> 
    			</select>
   				</span>
    			<input id="text4" style="width:100px;position:absolute;left:0px;" value="choose"> 
				</div> -->
				
					<!-- <select name="select" id="hello" onChange="document.getElementByName('text').value=document.getElementByName('select').options[document.getElementByName('select').selectedIndex].value" 
						style="position:absolute;width:118px;clip:rect(0 120 22 100)">   
						<option value="Waxbird">Waxbird</option>   
						<option value="DrDoc">DrDOC</option>   
					</select>   
					<input type="text" name="text" onChange="document.exceptionConfig.select.selectedIndex=-1" style="position:absolute;width:100px;border-right:0">    -->
 			</td>
		</tr>
		
		<tr>
			<td style="text-align: right" class="text-success">warning阈值</td>
			<td><input id="warningThreshold" name="exceptionLimit.warning"
				value="${model.exceptionLimit.warning}" required /></td>
		</tr>
		
		<tr>
			<td style="text-align: right" class="text-success">error阈值</td>
			<td><input id="errorThreshold" name="exceptionLimit.error"
				value="${model.exceptionLimit.error}" required /></td>
		</tr>

		<tr>
			<td colspan='2'  style="text-align:center"><input class='btn btn-primary' id="addOrUpdateExceptionConfigSubmit" type="submit"
				name="submit" value="提交"/></td>
		</tr>
	</table>
</form>