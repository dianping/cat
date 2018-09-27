<%@ page session="false" language="java" pageEncoding="UTF-8"%>

<h4 class="text-success">1. 项目配置</h4>
<p>接口调用请求说明</p>
<pre>
	http请求方式: GET或POST（请使用http协议）
	http://cat.dianpingoa.com/cat/s/project?op=projectUpdate
</pre>
<p>参数说明</p>
<table style="width:100%" class="table table-bordered table-striped table-condensed  ">
	<tr><th width="30%">参数</th><th>说明</th></tr>
	<tr><td>project.domain</td><td>CAT上的项目名，<span class="text-danger">必需</span></td></tr>
	<tr><td>project.cmdbDomain</td><td>cmdb中统一项目名，<span class="text-danger">必需</span></td></tr>
	<tr><td>project.level</td><td>cmdb中项目统一级别，<span class="text-danger">必需，数字</span></td></tr>
	<tr><td>project.bu</td><td>cmdb中项目所属事业部名称，<span class="text-danger">必需</span></td></tr>
	<tr><td>project.cmdbProductline</td><td>cmdb中项目所属产品线名称，<span class="text-danger">必须</span></td></tr>
	<tr><td>project.owner</td><td>项目负责人，<span class="text-danger">必需</span></td></tr>
	<tr><td>project.email</td><td>多个英文逗号分割，<span class="text-danger">必需</span></td></tr>
	<tr><td>project.phone</td><td>多个英文逗号分割，<span class="text-danger">必需</span></td></tr>
</table>

<p> 示例：
<pre>
	http://cat.dianpingoa.com/cat/s/project?op=projectUpdate&project.domain=cat&project.cmdbDomain=cat&project.level=1&project.bu=平台技术中心&project.cmdbProductline=平台架构&project.owner=尤勇&project.email=yong.you@dianping.com,jialin.sun@dianping.com&project.phone=18616671676,15201789489
</pre>
<p>返回说明</p>
<pre>
	<span class="text-danger">{"status":500, "info":"internal error"} ——> 失败</span>
	<span class="text-success">{"status":200, "info":"success"}        ——> 成功</span>
</pre>

<h4 class="text-success">2. 获取项目名</h4>
<p>接口调用请求说明</p>
<pre>
	http请求方式: GET（请使用http协议）
	http://cat.dianpingoa.com/cat/s/project?op=domains
</pre>
<p>返回Json数据</p>
<pre>
	{
		domains: [
			"account-all-service",
			"receipt-verify-service",
			"tuangou-mapi-web",
			"pet-server",
			"cortex-search-web",
		]
	}
</pre>
