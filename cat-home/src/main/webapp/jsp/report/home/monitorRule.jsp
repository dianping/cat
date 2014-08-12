<%@ page session="false" language="java" pageEncoding="UTF-8"%>
<h3 class="text-error">监控规则配置文档</h3>
</br>
<h4 class="text-info">配置规则的要点</h4>
<h5>合理、灵活的监控规则可以帮助更快、更精确的发现业务线上故障。目前Cat的监控规则有五个要素，请按照以下五点要素制定规则：</h5>
<h5>a).时间段。同一项业务指标在每天不同的时段可能有不同的趋势。设定该项，可让Cat在每天不同的时间段执行不同的监控规则。</h5>
<h5>b).规则组合。在一个时间段中，可能指标触发了多个监控规则中的一个规则就要发出警报，也有可能指标要同时触发了多个监控规则才需要发出警报。这种关系好比电路图中的并联和串联。规则的组合合理有助于提高监控的准确度。</h5>
<h5>c).监控规则类型。通过以下八种类型对指标进行监控：下降百分比、下降数值、上升百分比、上升数值、最大值、最小值、波动百分比最大值、波动百分比最小值。</h5>
<h5>d).持续时间。设定时间后（单位为分钟），当指标在设定的时间长度内连续触发了监控规则，才会发出警报。</h5>
<h5>e).规则与指标的匹配。监控规则可以按照名称、正则表达式与监控的对象（指标）进行匹配。</h5>
</br>

<h4 class="text-info">监控规则的设定方法</h4>
<h4 class="text-success">第一步:进入配置页面</h4>
<p>点击导航栏右侧的"Config"选项</p>
<p>在新页面中点击左侧边栏下方的“业务监控规则”选项</p>
<p>右方的页面上会显示一个产品线和项目列表。分别选择产品线和产品线下的项目，点击最右侧的按钮 “告警规则”，然后对弹出的页面进行下步的配置。</p>
</br>

<h4 class="text-success">第二步:更改配置信息</h4>
<h5>配置文件格式如下：</h5>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alertConfig.png" />
<h5>配置方式如下：</h5>
<p>1).一个rule元素为规则的基本单位</p>
<p>2).rule元素由两个部分组成：监控对象与监控规则</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;a.监控对象：由metric－item元素匹配</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;b.监控条件配置：由config元素组成，每个config代表一个时间段的规则，由starttime和endtime两个属性确定。时间的配置格式为：“hh:mm”，请注意hh为24小时制。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;config元素由多个监控条件组成，条件由condition元素表示。一个config下的多个condition为并联关系，当一个condition被触发，conditon所在的整个rule就被触发。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;condition元素中的minute属性表示该条件的持续时间。设定时间单位为分钟。当指标在设定的时间长度内连续触发了该条规则，才会出发该condition。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;condition由subcondition组成。一个condition下的多个subcondition为串联关系，只有当一个condition下的全部subcondition被触发，该condition才被触发。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;subcondition有八种类型，由type属性指定。subcondition的内容为对应的阈值，请注意阈值只能由数字组成，当阈值表达百分比时，不能在最后加上百分号。六种类型如下：</p>
<table style="width: 50%"
	class="table table-striped table-bordered table-condensed">
	<tr>
		<th width="30%">类型</th>
		<th>说明</th>
	</tr>
	<tr>
		<td>DescPer</td>
		<td>下降百分比</td>
	</tr>
	<tr>
		<td>DescVal</td>
		<td>下降数值</td>
	</tr>
	<tr>
		<td>AscPer</td>
		<td>上升百分比</td>
	</tr>
	<tr>
		<td>AscVal</td>
		<td>上升数值</td>
	</tr>
	<tr>
		<td>MaxVal</td>
		<td>最大值</td>
	</tr>
	<tr>
		<td>MinVal</td>
		<td>最小值</td>
	</tr>
	<tr>
		<td>FluAscPer</td>
		<td>波动百分比最大值。即当前分钟值比监控周期内其它分钟值的增加百分比都大于设定的百分比时触发警报</td>
	</tr>
	<tr>
		<td>FluDescPer</td>
		<td>波动百分比最小值。即当前分钟值比监控周期内其它分钟值的减少百分比都大于设定的百分比时触发警报</td>
	</tr>
</table>
<p>3).点击提交功能。如果标题上方出现“操作成功”的提示，代表规则添加成功，将会在下次执行时被应用</p>
<br/>

<h4 class="text-info">告警HTTP API</h4>
<p>Cat支持通过调用HTTP API来发送告警信息。目前支持三种发送渠道：邮件、短信、微信（需要邮箱和“爱点评”微信订阅号绑定）</p>
<pre>
	http请求方式: GET或者POST
	http://主机域名:端口/cat/r/alert?
</pre>
<p>参数说明</p>
<table style="width:50%" class="table table-striped table-bordered table-condensed">
	<tr><th width="30%">参数</th><th width="70%">说明</th></tr>	
	<tr><td>op</td><td>执行操作<span class="text-error">  必需[唯一值：alert]</span></td></tr>
	<tr><td>channel</td><td>渠道类型<span class="text-error">  必需[可能值：mail, sms, weixin]</span></td></tr>
	<tr><td>title</td><td>告警标题<span class="text-error">  必需</span></td></tr>
	<tr><td>content</td><td>告警内容<span class="text-error">  短信可选，邮件、微信必需</span></td></tr>
	<tr><td>group</td><td>告警组名<span class="text-error">  微信必需，短信、邮件可选</span></td></tr>	
	<tr><td>type</td><td>告警类型<span class="text-error">  必需[可能值：network, business, exception, system, thirdParty, frontEndException]</span></td></tr>
	<tr><td>receivers</td><td>接收人<span class="text-error">  必需[邮箱地址或者手机号；如有多个接收人，用半角逗号分割]</span></td></tr>
</table>

<p> url示例（get方式）</p>
<pre>
	http://主机域名:端口/cat/r/alert?op=alert&channel=mail&title=test&content=testcontent&group=cat&type=test&receivers=leon.li@dianping.com
</pre>
<p>返回说明</p>
<pre>
	<span class="text-success">{"status":200} ——> 成功</span>
	<span class="text-error">{"status":500, "errorMessage":"lack receivers"} ——> 失败 [接收人receivers未填写或者格式错误]</span>
	<span class="text-error">{"status":500, "errorMessage":"send failed, please check your channel argument"} ——> 失败 [渠道channel错误，请指定mail,sms,weixin三者中的一种渠道]</span>
	<span class="text-error">{"status":500, "errorMessage":"send failed, please retry again"} ——> 失败 [发送异常]</span>
</pre>
</br>