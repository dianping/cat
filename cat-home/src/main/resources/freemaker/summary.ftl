<div id="summaryContent">
    <style>
    	th, .alert-content {
    		white-space: nowrap;
    	}
    </style>

    <h4>
      项目名：&nbsp;${domain}&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;告警时间：&nbsp;${dateStr}
    </h4>

    <table class="table table-bordered table-striped table-hover">
      <thead>
        <tr>
	      <th>告警类型&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
	      <th colspan="5">详细警告信息</th>
	    </tr>
      </thead>
      <tbody>
		  <#assign length = categories.network?size />
		  <#if length == 0>
		 	<tr>
		  		<td class="text-success"><strong>网络告警</strong></td>
		  		<td class="text-success" colspan="5"><strong>网络状况正常</strong></td>
		  	</tr>
		  <#else>
		  	<tr>
			  <td class="text-success" rowspan="${length + 1}"><strong>网络告警</strong></td>
			  <th>告警设备</th>
			  <th>告警指标</th>
			  <th>告警时间</th>
			  <th>告警级别</th>
			  <th>告警内容</th>
			</tr>
			<#list categories.network as item>
		    	<tr>
		    	  <td>${item.domain}</td>
		    	  <td>${item.metric}</td>
		    	  <td>${item.dateStr}</td>
		    	  <td>${item.type}</td>
		    	  <td class="alert-content">${item.context}</td>
		    	</tr>
		    </#list>
		  </#if>

		  <#assign length = categories.business?size />
		  <#if length == 0>
		 	<tr>
		  		<td class="text-success"><strong>业务告警</strong></td>
		  		<td class="text-success" colspan="5"><strong>业务状况正常</strong></td>
		  	</tr>
		  <#else>
	    	<tr>
	    	  <td class="text-success" rowspan="${length + 1}"><strong>业务告警</strong></td>
	    	  <th colspan="2">告警指标</th>
	    	  <th>告警时间</th>
	    	  <th>告警级别</th>
	    	  <th>告警内容</th>
	    	</tr>
	    	<#list categories.business as item>
		    	<tr>
		    	  <td colspan="2">${item.metric}</td>
		    	  <td>${item.dateStr}</td>
		    	  <td>${item.type}</td>
		    	  <td class="alert-content">${item.context}</td>
		    	</tr>
		    </#list>
		  </#if>

		  <#assign length = categories.exception?size />
		  <#if length == 0>
		 	<tr>
		  		<td class="text-success"><strong>异常告警</strong></td>
		  		<td class="text-success" colspan="5"><strong>异常告警正常</strong></td>
		  	</tr>
		  <#else>
	    	<tr>
	    	  <td class="text-success" rowspan="${length + 1}"><strong>异常告警</strong></td>
	    	  <th colspan="2">异常名称</th>
	    	  <th>告警时间</th>
	    	  <th>告警级别</th>
	    	  <th>告警内容</th>
	    	</tr>
	    	<#list categories.exception as item>
		    	<tr>
		    	  <td colspan="2">${item.metric}</td>
		    	  <td>${item.dateStr}</td>
		    	  <td>${item.type}</td>
		    	  <td class="alert-content">${item.context}</td>
		    	</tr>
		    </#list>
		  </#if>

		  <#if categories.dependency_business_length == 0>
		 	<tr>
		  		<td class="text-success"><strong>超时依赖调用</strong></td>
		  		<td class="text-success" colspan="5"><strong>无超时依赖调用</strong></td>
		  	</tr>
		  <#else>
	    	<tr>
	    	  <td class="text-success" rowspan="${categories.dependency_business_length + 1}"><strong>超时依赖调用</strong></td>
	    	  <th>依赖项目</th>
	    	  <th>告警指标</th>
	    	  <th>告警时间</th>
	    	  <th>告警级别</th>
	    	  <th>告警内容</th>
	    	</tr>
	    	<#list categories.dependency_business?keys as key>
	    		<#list categories.dependency_business[key] as value>
			    	<tr>
			    	  <#if value_index == 0>
			    	    <td rowspan="${categories.dependency_business[key]?size}">${key}</td>
			    	  </#if>
			    	  <td>${value.metric}</td>
			    	  <td>${value.dateStr}</td>
			    	  <td>${value.type}</td>
			    	  <td class="alert-content">${value.context}</td>
			    	</tr>
		    	</#list>
		    </#list>
	      </#if>

 		  <#assign length = categories.dependency_exception?size />
		  <#if length == 0>
		 	<tr>
		  		<td class="text-success"><strong>依赖异常告警</strong></td>
		  		<td class="text-success" colspan="5"><strong>依赖项目正常</strong></td>
		  	</tr>
		  <#else>
	    	<tr>
	    	  <td class="text-success" rowspan="${length + 1}"><strong>依赖异常告警</strong></td>
	    	  <th>依赖项目</th>
	    	  <th>异常名称</th>
	    	  <th>告警时间</th>
	    	  <th>告警级别</th>
	    	  <th>告警内容</th>
	    	</tr>
	    	<#list categories.dependency_exception as item>
		    	<tr>
		    	  <td>${item.domain}</td>
		    	  <td>${item.metric}</td>
		    	  <td>${item.dateStr}</td>
		    	  <td>${item.type}</td>
		    	  <td class="alert-content">${item.context}</td>
		    	</tr>
		    </#list>
		  </#if>

		<#assign length = categories.system?size />
		  <#if length == 0>
		 	<tr>
		  		<td class="text-success"><strong>系统告警</strong></td>
		  		<td class="text-success" colspan="5"><strong>系统状态正常</strong></td>
		  	</tr>
		  <#else>
	    	<tr>
	    	  <td class="text-success" rowspan="${length + 1}"><strong>系统告警</strong></td>
	    	  <th colspan="2">告警参数-机器</th>
	    	  <th>告警时间</th>
	    	  <th>告警级别</th>
	    	  <th>告警内容</th>
	    	</tr>
	    	<#list categories.system as item>
		    	<tr>
		    	  <td colspan="2">${item.metric}</td>
		    	  <td>${item.dateStr}</td>
		    	  <td>${item.type}</td>
		    	  <td class="alert-content">${item.context}</td>
		    	</tr>
		    </#list>
		  </#if>
    </tbody>
    </table>
  </div>