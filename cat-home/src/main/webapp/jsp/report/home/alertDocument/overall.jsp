<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h4 class="text-success">告警模块介绍</h4>
<h5>告警模块按照规则对收集的信息进行监控，并在规则被触发时通知相应的联系人。</h5>
<h5>能否有效的使用告警模块直接影响着监控的质量。通过对监控规则、告警策略、默认联系人等元素进行合理的配置，告警模块能够更快、更准确、更灵活的发现线上故障，并更有效的通知对应联系人。</h5>
<h4 class="text-success">本页内容</h4>
<ul>
	<li>监控规则配置</li>
	<li>告警策略</li>
	<li>默认联系人配置</li>
</ul>
<p>本页面对告警模块的通用概念进行介绍。如需了解某一个具体的告警类型，请阅读完本页的通用概念后再阅读对应的标签页。</p>
<h4 class="text-success">1. 监控规则配置</h4>
<h5>合理、灵活的监控规则可以帮助更快、更精确的发现业务线上故障。目前Cat的监控规则有五个要素，请按照以下五点要素制定规则：</h5>
<p>a).时间段。同一项业务指标在每天不同的时段可能有不同的趋势。设定该项，可让Cat在每天不同的时间段执行不同的监控规则。</p>
<p>b).规则组合。在一个时间段中，可能指标触发了多个监控规则中的一个规则就要发出警报，也有可能指标要同时触发了多个监控规则才需要发出警报。这种关系好比电路图中的并联和串联。规则的组合合理有助于提高监控的准确度。</p>
<p>c).监控规则类型。通过以下八种类型对指标进行监控：下降百分比、下降数值、上升百分比、上升数值、最大值、最小值、波动百分比最大值、波动百分比最小值。</p>
<p>d).持续时间。设定时间后（单位为分钟），当指标在设定的时间长度内连续触发了监控规则，才会发出警报。</p>
<p>e).规则与被监控指标的匹配。监控规则可以按照名称、正则表达式与监控的对象（指标）进行匹配。</p>
<h5>监控规则模型如下图所示：</h5>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alert/alertConfig.png" />
<h5>具体解释如下：</h5>
<p>1).一个rule元素为规则的基本单位，由唯一的id标示</p>
<p>2).rule元素由两个部分组成：监控对象与监控规则</p>
<p>&nbsp;&nbsp;a.监控对象：由metric－item元素匹配，与图中的匹配对象相对应。匹配对象是两级的，每一级都支持正则匹配</p>
<p>&nbsp;&nbsp;b.监控条件配置：由config元素组成，与图中监控规则相对应。每个config代表一个时间段的规则，由starttime和endtime两个属性确定。时间的配置格式为：“hh:mm”，请注意hh为24小时制。</p>
<p>&nbsp;&nbsp;config元素由多个监控条件组成，条件由condition元素表示。一个config下的多个condition为并联关系，当一个condition被触发，conditon所在的整个rule就被触发。</p>
<p>&nbsp;&nbsp;condition元素中的minute属性表示该条件的持续时间。设定时间单位为分钟。当指标在设定的时间长度内连续触发了该条规则，才会触发该condition。</p>
<p>&nbsp;&nbsp;condition由subcondition组成。subcondition与图中的子条件相对应。一个condition下的多个subcondition为串联关系，只有当一个condition下的全部subcondition被触发，该condition才被触发。</p>
<p>&nbsp;&nbsp;subcondition有八种类型，由type属性指定。subcondition的内容为对应的阈值，请注意阈值只能由数字组成，当阈值表达百分比时，不能在最后加上百分号。八种类型如下：</p>
<table style="width: 50%"
	class="table table-bordered table-striped table-condensed  ">
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
	<tr>
		<td>SumMaxVal</td>
		<td>总和最大值，请与告警分钟总和考虑</td>
	</tr>
	<tr>
		<td>SumMinVal</td>
		<td>总和最小值，请与告警分钟总和考虑</td>
	</tr>
</table>
<p>&nbsp;&nbsp;点击"如何使用?"按钮，将会出现信息介绍设置规则的流程</p>
<h4 class="text-success">2. 告警策略</h4>
<h5>为了将告警信息更有效的发送给对应联系人，请考虑以下五个要素制定告警策略：</h5>
<p>a).告警类型。Cat将告警分为六种类型：业务告警(项目指标的监控)、网络告警(网络设备监控)、系统告警(服务器状态监控)、异常告警(Exception数量监控)、第三方监控(对给定的网址，根据HTTP请求的返回码监控)、前端监控。由于告警策略是按照类型划分的，制定告警策略前首先请确定目前采用的是哪种类型的监控。</p>
<p>b).告警级别。告警级别即为该告警的优先级。不同级别的告警在通知渠道、暂停告警时间上可以有所差别。对告警进行合理的分级能够帮助我们将更多的精力放在更重要的问题上。</p>
<p>c).告警渠道。目前有三种告警渠道：邮件、微信、短信。</p>
<p>d).暂停告警时间。设定暂停告警时间(suspendMinute)后，某一指标在一次告警之后的指定时间段内不会再次发送告警信息。</p>
<p>e).恢复通知。设定恢复通知时间(recoverMinute)后，当一个指标在某一分钟告警并且在以后的指定时间段内没有再次告警时，Cat会发出恢复通知，表明该指标在这个时间段的状态是正常的。默认的恢复通知时间段为一分钟。</p>
<h5>告警策略模型如下图所示：</h5>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alert/alertPolicy.png" />
<h5>具体解释如下：</h5>
<p>1).alert-policy元素对应着Cat上的全部告警策略信息。每个type元素对应着一种告警类型，由id可以得知type与告警类型的对应关系</p>
<p>2).type元素下每一个group元素对应着一个项目或是一个产品线的告警策略。对于异常监控以及第三方监控，此处的group请填写项目名；其它类型请填写产品线名</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;当group元素的id为default时，该group元素即为该告警类型的默认告警策略。当没有其它group命中时，会采用默认告警策略。</p>
<p>2).group元素下的level元素对应着告警级别。level元素与监控规则中的alertType属性是对应的，请两者配合使用。level元素有两个属性</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;send属性。该属性对应着发送渠道，发送渠道之间用逗号分割</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;suspendMinute属性。该属性为暂停告警时间，单位为分钟</p>
<h5>配置方法</h5>
<p>1).点击导航栏Config－－监控告警配置－－告警类型设置</p>
<p>2).编辑文本框的内容，点击提交</p>
<p>3).当出现"操作成功"提示时表明策略已经生效</p>
<h4 class="text-success">3. 默认联系人设置</h4>
<h5>此处仅建议Cat开发者使用。主要有以下两个功能：</h5>
<p>a).控制某一个类型的所有告警信息是否发送</p>
<p>b).添加默认通知人。该通知人会收到某类型的所有告警</p>
<h5>默认发送人模型如下图所示：</h5>
<img class="img-polaroid" width='60%'
	src="${model.webapp}/images/alert/defaultReceiver.png" />
<h5>配置方法</h5>
<p>1).点击导航栏Config－－监控告警配置－－默认告警配置</p>
<p>2).编辑文本框的内容，点击提交</p>
<p>3).当出现"操作成功"提示时表明规则已经生效</p>