<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<h3 class="text-error">监控规则配置文档</h3>
</br>
<h4 class="text-info">配置规则的要点</h4>
<h5>合理、灵活的监控规则可以帮助更快、更精确的发现业务线上故障。目前Cat的监控规则有五个要素，请按照以下五点要素制定规则：</h5>
<h5>a).时间段。同一项业务指标在每天不同的时段可能有不同的趋势。设定该项，可让Cat在每天不同的时间段执行不同的监控规则。</h5>
<h5>b).规则组合。在一个时间段中，可能指标触发了多个监控规则中的一个规则就要发出警报，也有可能指标要同时触发了多个监控规则才需要发出警报。这种关系好比电路图中的并联和串联。规则的组合合理有助于提高监控的准确度。</h5>
<h5>c).监控类型。通过以下六种类型对指标进行监控：下降百分比、下降数值、上升百分比、上升数值、最大值、最小值。</h5>
<h5>d).持续时间。设定时间后（单位为分钟），当指标在设定的时间长度内连续触发了监控规则，才会发出警报。</h5>
<h5>e).规则与指标的匹配。监控规则可以按照名称、正则表达式与监控的对象（指标）进行匹配。</h5>
</br>

<h4 class="text-info">监控规则的设定方法</h4>
<h4 class="text-success">第一步:进入配置页面</h4>
<p>点击导航栏右侧的"Config"选项</p>
<p>在新页面中点击左侧边栏下方的“业务告警配置”选项</p>
<p>右方的页面上会显示一个xml，对其进行下步的配置。</p>
</br>

<h4 class="text-success">第二步:更改配置信息</h4>
<h5>配置文件格式如下：</h5>
<img  class="img-polaroid"  width='60%' src="${model.webapp}/images/rule.png"/>
<h5>配置方式如下：</h5>
<p> 1).每一个rule元素为规则的基本单位。请按照需求添加一个rule元素或者对某一个rule进行修改</p>
<p> 2).rule元素由两个部分组成：监控对象与监控规则</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;a.监控对象：由metric－item元素匹配。metric-item元素可以有多个。metric-item支持两种匹配方式：</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(1) 按照id匹配。此种方式适用于匹配一个具体的监控指标。此时type为id，内容为 <b>产品线:项目名:指标</b></p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(2) 按照正则表达式匹配。此种情况适用于同时匹配多个监控对象。此时type为regex，内容仍形如 <b>产品线:项目名:指标</b>，三者用冒号分割。三者每一项都可以写为正则表达式。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;b.监控条件配置：由config元素组成，每个config代表一个时间段的规则，由starttime和endtime两个属性确定。时间的配置格式为：“hh:mm”，请注意hh为24小时制。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;config元素由多个监控条件组成，条件由condition元素表示。一个config下的多个condition为并联关系，当一个condition被触发，conditon所在的整个rule就被触发。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;condition元素中的minute属性表示该条件的持续时间。设定时间单位为分钟。当指标在设定的时间长度内连续触发了该条规则，才会出发该condition。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;condition由subcondition组成。一个condition下的多个subcondition为串联关系，只有当一个condition下的全部subcondition被触发，该condition才被触发。</p>
<p>&nbsp;&nbsp;&nbsp;&nbsp;subcondition有六个类型，由type属性指定。subcondition的内容为对应的阈值，请注意阈值只能由数字组成，当阈值表达百分比时，不能在最后加上百分号。六种类型如下：</p>
<table style="width:50%" class="table table-striped table-bordered table-condensed">
	<tr><th width="30%">类型</th><th>说明</th></tr>
	<tr><td>DescPer</td><td>下降百分比</td></tr>
	<tr><td>DescVal</td><td>下降数值</td></tr>
	<tr><td>AscPer</td><td>上升百分比</td></tr>
	<tr><td>AscVal</td><td>上升数值</td></tr>
	<tr><td>MaxVal</td><td>最大值</td></tr>
	<tr><td>MinVal</td><td>最小值</td></tr>
</table>
<p> 3).点击提交功能。如果标题“业务监控规则相关信息”上方出现“操作成功”的提示，代表规则添加成功，将会在下次执行时被应用</p>