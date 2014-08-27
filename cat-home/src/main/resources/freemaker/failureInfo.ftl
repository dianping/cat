<#if (count)??>
	<div id="failureContent">
		-------------------
	    <h4>错误信息（错误总数： ${count}）</h4>
	    <table border="1" class="table table-bordered table-striped table-hover">
	      <thead>
	        <tr>
		      <th>错误名称</th>
		      <th>错误个数</th>
		    </tr>
	      </thead>
	      <tbody>
	      	  <#list statusMap?keys as status>
	      	  	<tr>
	      	  		<td>${status}</td>
	      	  		<td>${statusMap[status]}</td>
	      	  	</tr>
	      	  </#list>
	      </tbody>
	    </table>
	</div>
	
	<div id="distrubuteContent">
		-------------------
	    <h4>错误分布</h4>
	    <table border="1" class="table table-bordered table-striped table-hover">
	      <thead>
	        <tr>
		      <th>机器IP</th>
		      <th>错误个数</th>
		    </tr>
	      </thead>
	      <tbody>
	      	  <#list distributeMap?keys as ip>
		      	  <tr>
		      	  	<td>${ip}</td>
		      	  	<td>${distributeMap[ip]}</td>
		      	  </tr>
	      	  </#list>
	      </tbody>
	    </table>
	</div>
</#if>