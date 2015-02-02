<#if count gt 0>
	<div id="alterationContent">
		-------------------
	    <h4>变更信息（变更总数： ${count}）</h4>
	    <table border="1" class="table table-bordered table-striped table-hover">
	      <thead>
	        <tr>
		      <th>变更类型</th>
		      <th>变更标题</th>
		      <th>主机名</th>
		      <th>变更时间</th>
		    </tr>
	      </thead>
	      <tbody>
	      	  <#list items as item>
	      	  	<tr>
	      	  		<td>${item.type}</td>
	      	  		<td>${item.title}</td>
	      	  		<td>${item.hostname}</td>
	      	  		<td>${item.date}</td>
	      	  	</tr>
	      	  </#list>
	      </tbody>
	    </table>
	</div>
</#if>