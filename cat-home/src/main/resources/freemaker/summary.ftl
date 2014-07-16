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
    	<tr>
    	  <td class="text-success" rowspan="${categories.network?size + 1}"><strong>网络告警</strong></td>
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

    	<tr>
    	  <td class="text-success" rowspan="${categories.business?size + 1}"><strong>业务告警</strong></td>
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

    	<tr>
    	  <td class="text-success" rowspan="${categories.exception?size + 1}"><strong>异常告警</strong></td>
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

    	<tr>
    	  <td class="text-success" rowspan="${categories.dependency_business?size + 1}"><strong>超时依赖调用</strong></td>
    	  <th>依赖项目</th>
    	  <th>告警指标</th>
    	  <th>告警时间</th>
    	  <th>告警级别</th>
    	  <th>告警内容</th>
    	</tr>
    	<#list categories.dependency_business as item>
	    	<tr>
	    	  <td>${item.domain}</td>
	    	  <td>${item.metric}</td>
	    	  <td>${item.dateStr}</td>
	    	  <td>${item.type}</td>
	    	  <td class="alert-content">${item.context}</td>
	    	</tr>
	    </#list>

    	<tr>
    	  <td class="text-success" rowspan="${categories.dependency_exception?size + 1}"><strong>依赖异常告警</strong></td>
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

    	<tr>
    	  <td class="text-success" rowspan="${categories.system?size + 1}"><strong>系统告警</strong></td>
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
    </tbody>
    </table>
  </div>